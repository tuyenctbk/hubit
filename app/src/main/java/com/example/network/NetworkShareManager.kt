package com.example.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPFile
import java.io.File
import java.io.FileOutputStream
import java.time.Duration

data class NetworkShareConfig(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val protocol: String = "FTP", // FTP or SMB
    val host: String,
    val port: Int = 21,
    val username: String = "",
    val password: String = "",
    val basePath: String = "/"
)

data class NetworkFileItem(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val size: Long,
    val lastModified: Long,
    val protocol: String,
    val host: String,
    val port: Int
)

class NetworkShareManager {

    /**
     * Test connection and list files from FTP server
     */
    suspend fun listFtpFiles(config: NetworkShareConfig, currentPath: String): Result<List<NetworkFileItem>> =
        withContext(Dispatchers.IO) {
            val ftp = FTPClient()
            try {
                ftp.connectTimeout = 8000
                ftp.dataTimeout = Duration.ofMillis(8000)
                ftp.connect(config.host, config.port)
                val user = config.username.ifBlank { "anonymous" }
                val pass = config.password.ifBlank { "anonymous" }
                val loginSuccess = ftp.login(user, pass)

                if (!loginSuccess) {
                    ftp.disconnect()
                    return@withContext Result.failure(Exception("Đăng nhập FTP thất bại. Vui lòng kiểm tra tên người dùng/mật khẩu."))
                }

                ftp.enterLocalPassiveMode()
                ftp.setFileType(FTP.BINARY_FILE_TYPE)

                val targetPath = if (currentPath.isBlank()) config.basePath.ifBlank { "/" } else currentPath
                ftp.changeWorkingDirectory(targetPath)

                val files: Array<FTPFile> = ftp.listFiles(targetPath) ?: emptyArray()
                val items = files.filter { it.name != "." && it.name != ".." }.map { file ->
                    val fullPath = if (targetPath.endsWith("/")) "$targetPath${file.name}" else "$targetPath/${file.name}"
                    NetworkFileItem(
                        name = file.name,
                        path = fullPath,
                        isDirectory = file.isDirectory,
                        size = file.size,
                        lastModified = file.timestamp?.timeInMillis ?: 0L,
                        protocol = config.protocol,
                        host = config.host,
                        port = config.port
                    )
                }.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))

                ftp.logout()
                ftp.disconnect()
                Result.success(items)
            } catch (e: Exception) {
                try {
                    if (ftp.isConnected) ftp.disconnect()
                } catch (_: Exception) {}
                Result.failure(e)
            }
        }

    /**
     * Download or buffer a network file to a local cache location for ExoPlayer streaming
     */
    suspend fun prepareMediaForStreaming(
        config: NetworkShareConfig,
        item: NetworkFileItem,
        cacheDir: File
    ): Result<String> = withContext(Dispatchers.IO) {
        val ftp = FTPClient()
        try {
            ftp.connectTimeout = 10000
            ftp.dataTimeout = Duration.ofMillis(15000)
            ftp.connect(config.host, config.port)
            val user = config.username.ifBlank { "anonymous" }
            val pass = config.password.ifBlank { "anonymous" }
            ftp.login(user, pass)
            ftp.enterLocalPassiveMode()
            ftp.setFileType(FTP.BINARY_FILE_TYPE)

            val localFile = File(cacheDir, "stream_${System.currentTimeMillis()}_${item.name}")
            FileOutputStream(localFile).use { output ->
                ftp.retrieveFile(item.path, output)
            }

            ftp.logout()
            ftp.disconnect()
            Result.success(localFile.absolutePath)
        } catch (e: Exception) {
            try {
                if (ftp.isConnected) ftp.disconnect()
            } catch (_: Exception) {}
            Result.failure(e)
        }
    }
}
