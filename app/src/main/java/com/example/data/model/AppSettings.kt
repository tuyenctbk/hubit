package com.example.data.model

data class AppSettings(
    val storageLocationName: String = "Bộ nhớ trong (/Download/Hubit)",
    val customStoragePath: String = "",
    val autoCategorizeDownloads: Boolean = true,
    val autoOptimizeEnabled: Boolean = true,
    val autoOptimizeSchedule: String = "STARTUP", // STARTUP, HOURS_6, HOURS_12, DAILY, RAM_85
    val autoCleanApkAfterInstall: Boolean = true,
    val autoCleanCacheOnExit: Boolean = true,
    val webServerPort: Int = 8080,
    val autoStartWebServer: Boolean = true,
    val maxDownloadThreads: Int = 4,
    val defaultViewMode: String = "GRID" // GRID or LIST for TV File Browser
)
