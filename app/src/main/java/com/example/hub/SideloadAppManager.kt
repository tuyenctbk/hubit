package com.example.hub

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import com.example.data.model.InstalledAppInfo
import java.io.File

class SideloadAppManager(private val context: Context) {

    fun getInstalledApps(includeSystem: Boolean = false): List<InstalledAppInfo> {
        val pm = context.packageManager
        val apps = mutableListOf<InstalledAppInfo>()

        try {
            val packages = pm.getInstalledPackages(PackageManager.GET_META_DATA)
            for (pkg in packages) {
                val appInfo = pkg.applicationInfo ?: continue
                val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0

                if (!includeSystem && isSystem) continue

                val appName = pm.getApplicationLabel(appInfo).toString()
                val pkgName = pkg.packageName

                // Check if Leanback TV intent filter exists
                val tvIntent = pm.getLeanbackLaunchIntentForPackage(pkgName)
                val isTvApp = tvIntent != null

                apps.add(
                    InstalledAppInfo(
                        packageName = pkgName,
                        appName = appName,
                        isSystemApp = isSystem,
                        isTvApp = isTvApp,
                        versionName = pkg.versionName ?: "1.0",
                        iconDrawable = pm.getApplicationIcon(appInfo)
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return apps.sortedBy { it.appName.lowercase() }
    }

    fun launchApp(packageName: String): Boolean {
        val pm = context.packageManager
        try {
            var intent = pm.getLeanbackLaunchIntentForPackage(packageName)
            if (intent == null) {
                intent = pm.getLaunchIntentForPackage(packageName)
            }
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun installApk(apkFile: File): Boolean {
        try {
            if (!apkFile.exists()) return false
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(
                    getUriForFile(apkFile),
                    "application/vnd.android.package-archive"
                )
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.startActivity(intent)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    private fun getUriForFile(file: File): Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } else {
            Uri.fromFile(file)
        }
    }
}
