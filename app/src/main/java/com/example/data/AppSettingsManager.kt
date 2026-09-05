package com.example.data

import android.content.Context
import android.content.SharedPreferences
import android.os.Environment
import com.example.data.model.AppSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class AppSettingsManager(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("hubit_preferences", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings = _settings.asStateFlow()

    private fun loadSettings(): AppSettings {
        val defaultPath = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "Hubit").absolutePath
        return AppSettings(
            storageLocationName = prefs.getString("storage_name", "Bộ nhớ trong (/Download/Hubit)") ?: "Bộ nhớ trong (/Download/Hubit)",
            customStoragePath = prefs.getString("custom_storage_path", defaultPath) ?: defaultPath,
            autoCategorizeDownloads = prefs.getBoolean("auto_categorize", true),
            autoOptimizeEnabled = prefs.getBoolean("auto_optimize_enabled", true),
            autoOptimizeSchedule = prefs.getString("auto_optimize_schedule", "STARTUP") ?: "STARTUP",
            autoCleanApkAfterInstall = prefs.getBoolean("auto_clean_apk", true),
            autoCleanCacheOnExit = prefs.getBoolean("auto_clean_cache_exit", true),
            webServerPort = prefs.getInt("web_server_port", 8080),
            autoStartWebServer = prefs.getBoolean("auto_start_web_server", true),
            maxDownloadThreads = prefs.getInt("max_download_threads", 4),
            defaultViewMode = prefs.getString("default_view_mode", "GRID") ?: "GRID"
        )
    }

    fun updateSettings(newSettings: AppSettings) {
        prefs.edit()
            .putString("storage_name", newSettings.storageLocationName)
            .putString("custom_storage_path", newSettings.customStoragePath)
            .putBoolean("auto_categorize", newSettings.autoCategorizeDownloads)
            .putBoolean("auto_optimize_enabled", newSettings.autoOptimizeEnabled)
            .putString("auto_optimize_schedule", newSettings.autoOptimizeSchedule)
            .putBoolean("auto_clean_apk", newSettings.autoCleanApkAfterInstall)
            .putBoolean("auto_clean_cache_exit", newSettings.autoCleanCacheOnExit)
            .putInt("web_server_port", newSettings.webServerPort)
            .putBoolean("auto_start_web_server", newSettings.autoStartWebServer)
            .putInt("max_download_threads", newSettings.maxDownloadThreads)
            .putString("default_view_mode", newSettings.defaultViewMode)
            .apply()

        _settings.value = newSettings
    }

    fun getDownloadDirectory(): File {
        val path = _settings.value.customStoragePath
        val dir = if (path.isNotBlank()) File(path) else File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "Hubit")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }
}
