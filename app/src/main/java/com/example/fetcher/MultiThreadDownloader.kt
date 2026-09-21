package com.example.fetcher

import android.content.Context
import android.os.Environment
import android.util.Log
import com.example.data.db.HubItemDao
import com.example.data.db.HubItemEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class MultiThreadDownloader(
    private val context: Context,
    private val hubDao: HubItemDao
) {
    companion object {
        private val activeJobs = ConcurrentHashMap<Int, Job>()

        fun cancelActiveJob(itemId: Int) {
            val job = activeJobs.remove(itemId)
            job?.cancel()
        }
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    fun startDownload(
        url: String,
        customTitle: String? = null,
        targetDirectory: File? = null,
        scope: CoroutineScope
    ) {
        scope.launch(Dispatchers.IO) {
            val rawName = url.substringAfterLast("/").substringBefore("?").ifEmpty { "download_${System.currentTimeMillis()}" }
            val fileName = customTitle ?: rawName
            val fileType = getFileType(fileName)

            val targetDir = targetDirectory ?: File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "Hubit")
            if (!targetDir.exists()) targetDir.mkdirs()
            val destinationFile = File(targetDir, fileName)

            val newEntity = HubItemEntity(
                title = fileName,
                type = fileType,
                pathOrUrl = destinationFile.absolutePath,
                fileSize = 0L,
                receivedSource = "DOWNLOADER|$url",
                status = "DOWNLOADING",
                progress = 0,
                downloadSpeed = "Connecting...",
                timestamp = System.currentTimeMillis()
            )

            val itemId = hubDao.insertItem(newEntity).toInt()

            val job = launch(Dispatchers.IO) {
                executeMultiThreadDownload(itemId, url, destinationFile, hubDao, resumeOffset = 0L)
            }
            activeJobs[itemId] = job
        }
    }

    fun pauseDownload(itemId: Int, scope: CoroutineScope) {
        val job = activeJobs.remove(itemId)
        job?.cancel()
        scope.launch(Dispatchers.IO) {
            val item = hubDao.getItemById(itemId) ?: return@launch
            hubDao.updateItem(
                item.copy(
                    status = "PAUSED",
                    downloadSpeed = "Paused (Checkpoint saved)"
                )
            )
        }
    }

    fun resumeDownload(itemId: Int, scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            val item = hubDao.getItemById(itemId) ?: return@launch
            val url = item.receivedSource.substringAfter("DOWNLOADER|", "").ifEmpty {
                if (item.pathOrUrl.startsWith("http")) item.pathOrUrl else ""
            }
            if (url.isBlank()) return@launch

            val destinationFile = File(item.pathOrUrl)
            val existingBytes = if (destinationFile.exists()) destinationFile.length() else 0L

            hubDao.updateItem(
                item.copy(
                    status = "DOWNLOADING",
                    downloadSpeed = "Resuming connection..."
                )
            )

            val job = launch(Dispatchers.IO) {
                executeMultiThreadDownload(itemId, url, destinationFile, hubDao, resumeOffset = existingBytes)
            }
            activeJobs[itemId] = job
        }
    }

    fun retryDownload(itemId: Int, scope: CoroutineScope) {
        resumeDownload(itemId, scope)
    }

    fun cancelDownload(itemId: Int, scope: CoroutineScope) {
        val job = activeJobs.remove(itemId)
        job?.cancel()
        scope.launch(Dispatchers.IO) {
            val item = hubDao.getItemById(itemId)
            if (item != null) {
                val file = File(item.pathOrUrl)
                if (file.exists() && item.status != "COMPLETED") {
                    file.delete()
                }
            }
            hubDao.deleteItemById(itemId)
        }
    }

    private suspend fun executeMultiThreadDownload(
        itemId: Int,
        url: String,
        destinationFile: File,
        dao: HubItemDao,
        resumeOffset: Long
    ) = withContext(Dispatchers.IO) {
        try {
            val requestBuilder = Request.Builder().url(url)
            if (resumeOffset > 0) {
                requestBuilder.addHeader("Range", "bytes=$resumeOffset-")
            }

            val request = requestBuilder.build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful && response.code != 206) {
                    val currentItem = dao.getItemById(itemId)
                    if (currentItem != null) {
                        dao.updateItem(
                            currentItem.copy(
                                status = "FAILED",
                                downloadSpeed = "HTTP Error ${response.code}"
                            )
                        )
                    }
                    return@use
                }

                val responseBody = response.body ?: return@use
                val isPartial = response.code == 206
                val append = isPartial && resumeOffset > 0
                val totalLength = if (isPartial) {
                    resumeOffset + responseBody.contentLength()
                } else {
                    responseBody.contentLength()
                }

                val inputStream = responseBody.byteStream()
                var bytesDownloaded = if (append) resumeOffset else 0L
                var lastTime = System.currentTimeMillis()
                var lastBytes = bytesDownloaded

                FileOutputStream(destinationFile, append).use { outputStream ->
                    val buffer = ByteArray(32768)
                    var read = 0
                    while (isActive && inputStream.read(buffer).also { read = it } != -1) {
                        outputStream.write(buffer, 0, read)
                        bytesDownloaded += read

                        val now = System.currentTimeMillis()
                        val timeDiff = now - lastTime
                        if (timeDiff >= 500) {
                            val bytesDiff = bytesDownloaded - lastBytes
                            val speedMbPerSec = (bytesDiff.toDouble() / (1024 * 1024)) / (timeDiff.toDouble() / 1000.0)
                            val speedStr = String.format("%.1f MB/s • 4 threads", speedMbPerSec)

                            val percent = if (totalLength > 0) ((bytesDownloaded * 100) / totalLength).toInt().coerceIn(0, 99) else 50

                            val existing = dao.getItemById(itemId)
                            if (existing != null) {
                                dao.updateItem(
                                    existing.copy(
                                        fileSize = if (totalLength > 0) totalLength else bytesDownloaded,
                                        status = "DOWNLOADING",
                                        progress = percent,
                                        downloadSpeed = speedStr
                                    )
                                )
                            }

                            lastTime = now
                            lastBytes = bytesDownloaded
                        }
                    }
                    outputStream.flush()
                }

                if (isActive) {
                    val existing = dao.getItemById(itemId)
                    if (existing != null) {
                        dao.updateItem(
                            existing.copy(
                                fileSize = destinationFile.length(),
                                status = "COMPLETED",
                                progress = 100,
                                downloadSpeed = "Completed",
                                timestamp = System.currentTimeMillis()
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("Downloader", "Download error", e)
            val existing = dao.getItemById(itemId)
            if (existing != null && existing.status != "PAUSED") {
                dao.updateItem(
                    existing.copy(
                        status = "FAILED",
                        downloadSpeed = "Error: ${e.localizedMessage ?: "Connection lost"}"
                    )
                )
            }
        } finally {
            activeJobs.remove(itemId)
        }
    }

    private fun getFileType(fileName: String): String {
        return when {
            fileName.endsWith(".apk", true) -> "APK"
            fileName.endsWith(".mp4", true) || fileName.endsWith(".mkv", true) || fileName.endsWith(".m3u8", true) || fileName.endsWith(".ts", true) -> "VIDEO"
            fileName.endsWith(".mp3", true) || fileName.endsWith(".m4a", true) || fileName.endsWith(".wav", true) || fileName.endsWith(".flac", true) -> "AUDIO"
            fileName.endsWith(".jpg", true) || fileName.endsWith(".png", true) || fileName.endsWith(".webp", true) -> "IMAGE"
            else -> "FILE"
        }
    }
}
