package com.example.ui.screens

import android.os.Environment
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppSettings
import com.example.ui.theme.*
import com.example.ui.util.dpadFocusable
import com.example.ui.viewmodel.HubitViewModel
import java.io.File

@Composable
fun SettingsScreen(viewModel: HubitViewModel) {
    val context = LocalContext.current
    val settings by viewModel.appSettings.collectAsState()
    val optProgress by viewModel.optimizationProgress.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Screen Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = CyanPrimary.copy(alpha = 0.2f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Settings, contentDescription = null, tint = CyanPrimary)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.settings_main_title),
                            color = TextPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        )
                        Text(
                            text = stringResource(R.string.settings_main_subtitle),
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                // Quick Optimization Trigger Button
                Button(
                    onClick = { viewModel.startBackgroundOptimization() },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldTertiary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.dpadFocusable(onClick = { viewModel.startBackgroundOptimization() })
                ) {
                    Icon(Icons.Default.Speed, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.settings_quick_opt_btn), color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Section 1: Storage Path Preferences
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    SettingsSectionHeader(
                        icon = Icons.Default.FolderSpecial,
                        title = stringResource(R.string.settings_storage_section_title),
                        subtitle = stringResource(R.string.settings_storage_section_sub)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Current Active Path Display
                    Surface(
                        color = DarkCard,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Storage, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(stringResource(R.string.settings_current_storage_label), color = TextSecondary, fontSize = 11.sp)
                                Text(settings.storageLocationName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(settings.customStoragePath, color = CyanPrimary, fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(stringResource(R.string.settings_storage_quick_select), color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val defaultHubitPath = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "Hubit").absolutePath
                        val publicDownloadsPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath

                        val internalHubitLabel = stringResource(R.string.settings_storage_internal_hubit)
                        val publicDownloadLabel = stringResource(R.string.settings_storage_public_download)
                        val usbDriveLabel = stringResource(R.string.settings_storage_usb_drive)

                        StorageOptionButton(
                            name = internalHubitLabel,
                            path = defaultHubitPath,
                            isSelected = settings.customStoragePath == defaultHubitPath,
                            onClick = {
                                viewModel.updateSettings(
                                    settings.copy(
                                        storageLocationName = "$internalHubitLabel (/Download/Hubit)",
                                        customStoragePath = defaultHubitPath
                                    )
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )

                        StorageOptionButton(
                            name = publicDownloadLabel,
                            path = publicDownloadsPath,
                            isSelected = settings.customStoragePath == publicDownloadsPath,
                            onClick = {
                                viewModel.updateSettings(
                                    settings.copy(
                                        storageLocationName = "$publicDownloadLabel (/storage/emulated/0/Download)",
                                        customStoragePath = publicDownloadsPath
                                    )
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )

                        // USB option
                        StorageOptionButton(
                            name = usbDriveLabel,
                            path = "/storage/usbdrive",
                            isSelected = settings.customStoragePath.contains("usb", ignoreCase = true),
                            onClick = {
                                viewModel.updateSettings(
                                    settings.copy(
                                        storageLocationName = "$usbDriveLabel (/storage/usbdrive/Hubit)",
                                        customStoragePath = "/storage/usbdrive/Hubit"
                                    )
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Auto-categorize toggle
                    SettingsToggleRow(
                        title = stringResource(R.string.settings_auto_categorize_title),
                        description = stringResource(R.string.settings_auto_categorize_desc),
                        checked = settings.autoCategorizeDownloads,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(autoCategorizeDownloads = it)) }
                    )
                }
            }
        }

        // Section 2: Automated Optimization Schedules
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldTertiary.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    SettingsSectionHeader(
                        icon = Icons.Default.AutoMode,
                        title = stringResource(R.string.settings_schedule_section_title),
                        subtitle = stringResource(R.string.settings_schedule_section_sub)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    SettingsToggleRow(
                        title = stringResource(R.string.settings_auto_optimize_title),
                        description = stringResource(R.string.settings_auto_optimize_desc),
                        checked = settings.autoOptimizeEnabled,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(autoOptimizeEnabled = it)) }
                    )

                    if (settings.autoOptimizeEnabled) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(stringResource(R.string.settings_schedule_frequency_label), color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val schedules = listOf(
                                "STARTUP" to stringResource(R.string.settings_schedule_startup),
                                "HOURS_6" to stringResource(R.string.settings_schedule_hours_6),
                                "HOURS_12" to stringResource(R.string.settings_schedule_hours_12),
                                "DAILY" to stringResource(R.string.settings_schedule_daily),
                                "RAM_85" to stringResource(R.string.settings_schedule_ram_85)
                            )

                            schedules.forEach { (code, label) ->
                                val isSelected = settings.autoOptimizeSchedule == code
                                Surface(
                                    onClick = { viewModel.updateSettings(settings.copy(autoOptimizeSchedule = code)) },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) EmeraldTertiary else DarkCard,
                                    modifier = Modifier
                                        .weight(1f)
                                        .dpadFocusable(
                                            shape = RoundedCornerShape(8.dp),
                                            onClick = { viewModel.updateSettings(settings.copy(autoOptimizeSchedule = code)) }
                                        )
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            color = if (isSelected) Color.White else TextPrimary,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    SettingsToggleRow(
                        title = stringResource(R.string.settings_auto_delete_apk_title),
                        description = stringResource(R.string.settings_auto_delete_apk_desc),
                        checked = settings.autoCleanApkAfterInstall,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(autoCleanApkAfterInstall = it)) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    SettingsToggleRow(
                        title = stringResource(R.string.settings_auto_clean_exit_title),
                        description = stringResource(R.string.settings_auto_clean_exit_desc),
                        checked = settings.autoCleanCacheOnExit,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(autoCleanCacheOnExit = it)) }
                    )
                }
            }
        }

        // Section 3: Downloader & Web Bridge Server
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, IndigoSecondary.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    SettingsSectionHeader(
                        icon = Icons.Default.Tune,
                        title = stringResource(R.string.settings_performance_section_title),
                        subtitle = stringResource(R.string.settings_performance_section_sub)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Parallel Threads Option
                    Text(stringResource(R.string.settings_threads_label), color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf(2, 4, 8, 16).forEach { threadCount ->
                            val isSelected = settings.maxDownloadThreads == threadCount
                            Surface(
                                onClick = { viewModel.updateSettings(settings.copy(maxDownloadThreads = threadCount)) },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) CyanPrimary else DarkCard,
                                modifier = Modifier
                                    .weight(1f)
                                    .dpadFocusable(
                                        shape = RoundedCornerShape(10.dp),
                                        onClick = { viewModel.updateSettings(settings.copy(maxDownloadThreads = threadCount)) }
                                    )
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = stringResource(R.string.settings_threads_format, threadCount),
                                        color = if (isSelected) Color.Black else TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = if (threadCount >= 8) stringResource(R.string.settings_threads_speed_turbo) else stringResource(R.string.settings_threads_speed_standard),
                                        color = if (isSelected) Color.Black.copy(alpha = 0.7f) else TextSecondary,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    SettingsToggleRow(
                        title = stringResource(R.string.settings_auto_web_title, settings.webServerPort),
                        description = stringResource(R.string.settings_auto_web_desc),
                        checked = settings.autoStartWebServer,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(autoStartWebServer = it)) }
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SettingsSectionHeader(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                color = CyanPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                letterSpacing = 1.sp
            )
            Text(
                text = subtitle,
                color = TextSecondary,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun StorageOptionButton(
    name: String,
    path: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) CyanPrimary else DarkCard,
        modifier = modifier.dpadFocusable(
            shape = RoundedCornerShape(10.dp),
            onClick = onClick
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = name,
                color = if (isSelected) Color.Black else TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = path,
                color = if (isSelected) Color.Black.copy(alpha = 0.7f) else TextSecondary,
                fontSize = 9.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
fun SettingsToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .dpadFocusable(
                shape = RoundedCornerShape(8.dp),
                onClick = { onCheckedChange(!checked) }
            )
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text(description, color = TextSecondary, fontSize = 11.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black,
                checkedTrackColor = CyanPrimary,
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = DarkCard
            )
        )
    }
}
