package com.example.data.model

enum class ItemType {
    FILE, LINK, TEXT, APK, VIDEO, AUDIO, IMAGE
}

enum class ItemSource {
    WEB_DASHBOARD, P2P, DOWNLOADER, SYSTEM
}

enum class DownloadStatus {
    COMPLETED, DOWNLOADING, PAUSED, FAILED
}

data class SystemMemoryInfo(
    val totalRamMb: Long,
    val usedRamMb: Long,
    val freeRamMb: Long,
    val ramUsagePercent: Int,
    val totalStorageGb: Double,
    val usedStorageGb: Double,
    val freeStorageGb: Double,
    val storageUsagePercent: Int,
    val cacheSizeMb: Long
)

data class InstalledAppInfo(
    val packageName: String,
    val appName: String,
    val isSystemApp: Boolean,
    val isTvApp: Boolean,
    val versionName: String,
    val iconDrawable: Any? = null
)
