package com.example.fetcher

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.db.AppDatabase
import com.example.data.db.HubItemEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

class DownloadService : Service() {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    private var downloader: MultiThreadDownloader? = null
    private var observeJob: Job? = null

    companion object {
        const val CHANNEL_ID = "hubit_downloads_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START_DOWNLOAD = "com.example.fetcher.action.START"
        const val ACTION_PAUSE_DOWNLOAD = "com.example.fetcher.action.PAUSE"
        const val ACTION_RESUME_DOWNLOAD = "com.example.fetcher.action.RESUME"
        const val ACTION_CANCEL_DOWNLOAD = "com.example.fetcher.action.CANCEL"
        const val ACTION_RETRY_DOWNLOAD = "com.example.fetcher.action.RETRY"

        const val EXTRA_URL = "extra_url"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_TARGET_DIR = "extra_target_dir"
        const val EXTRA_ITEM_ID = "extra_item_id"

        fun startDownload(
            context: Context,
            url: String,
            title: String? = null,
            targetDir: String? = null
        ) {
            val intent = Intent(context, DownloadService::class.java).apply {
                action = ACTION_START_DOWNLOAD
                putExtra(EXTRA_URL, url)
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_TARGET_DIR, targetDir)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun pauseDownload(context: Context, itemId: Int) {
            val intent = Intent(context, DownloadService::class.java).apply {
                action = ACTION_PAUSE_DOWNLOAD
                putExtra(EXTRA_ITEM_ID, itemId)
            }
            context.startService(intent)
        }

        fun resumeDownload(context: Context, itemId: Int) {
            val intent = Intent(context, DownloadService::class.java).apply {
                action = ACTION_RESUME_DOWNLOAD
                putExtra(EXTRA_ITEM_ID, itemId)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun cancelDownload(context: Context, itemId: Int) {
            val intent = Intent(context, DownloadService::class.java).apply {
                action = ACTION_CANCEL_DOWNLOAD
                putExtra(EXTRA_ITEM_ID, itemId)
            }
            context.startService(intent)
        }

        fun retryDownload(context: Context, itemId: Int) {
            val intent = Intent(context, DownloadService::class.java).apply {
                action = ACTION_RETRY_DOWNLOAD
                putExtra(EXTRA_ITEM_ID, itemId)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val dao = AppDatabase.getDatabase(applicationContext).hubDao()
        downloader = MultiThreadDownloader(applicationContext, dao)
        startForeground(NOTIFICATION_ID, buildInitialNotification())

        observeActiveDownloads()
    }

    private fun observeActiveDownloads() {
        val dao = AppDatabase.getDatabase(applicationContext).hubDao()
        observeJob = serviceScope.launch {
            dao.getActiveDownloads().collectLatest { activeList ->
                if (activeList.isNotEmpty()) {
                    val first = activeList.first()
                    updateNotification(first, activeList.size)
                } else {
                    // Check paused or recently completed tasks before stopping
                    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    val doneNotification = NotificationCompat.Builder(this@DownloadService, CHANNEL_ID)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("Hubit! Tải xuống")
                        .setContentText("Tất cả các tác vụ tải đã hoàn tất")
                        .setOngoing(false)
                        .setAutoCancel(true)
                        .build()
                    notificationManager.notify(NOTIFICATION_ID, doneNotification)
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Hubit Background Downloads",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Hiển thị tiến độ tải đa luồng khi ứng dụng chạy ngầm"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildInitialNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Hubit! Multi-Thread Downloader")
            .setContentText("Trình quản lý tải tệp nền đang chạy...")
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(item: HubItemEntity, activeCount: Int) {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (activeCount > 1) {
            "Hubit! (${activeCount} tác vụ) - ${item.title}"
        } else {
            "Hubit! - ${item.title}"
        }

        val speedText = if (item.downloadSpeed.isNotBlank()) item.downloadSpeed else "${item.progress}%"
        val contentText = "${item.progress}% • $speedText"

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(contentText)
            .setProgress(100, item.progress.coerceIn(0, 100), item.progress == 0)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        when (action) {
            ACTION_START_DOWNLOAD -> {
                val url = intent.getStringExtra(EXTRA_URL)
                val title = intent.getStringExtra(EXTRA_TITLE)
                val targetDir = intent.getStringExtra(EXTRA_TARGET_DIR)?.let { File(it) }
                if (!url.isNullOrBlank()) {
                    downloader?.startDownload(url, title, targetDir, serviceScope)
                }
            }
            ACTION_PAUSE_DOWNLOAD -> {
                val itemId = intent.getIntExtra(EXTRA_ITEM_ID, -1)
                if (itemId != -1) {
                    downloader?.pauseDownload(itemId, serviceScope)
                }
            }
            ACTION_RESUME_DOWNLOAD -> {
                val itemId = intent.getIntExtra(EXTRA_ITEM_ID, -1)
                if (itemId != -1) {
                    downloader?.resumeDownload(itemId, serviceScope)
                }
            }
            ACTION_CANCEL_DOWNLOAD -> {
                val itemId = intent.getIntExtra(EXTRA_ITEM_ID, -1)
                if (itemId != -1) {
                    downloader?.cancelDownload(itemId, serviceScope)
                }
            }
            ACTION_RETRY_DOWNLOAD -> {
                val itemId = intent.getIntExtra(EXTRA_ITEM_ID, -1)
                if (itemId != -1) {
                    downloader?.retryDownload(itemId, serviceScope)
                }
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        observeJob?.cancel()
        serviceScope.cancel()
    }
}
