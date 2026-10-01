package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.example.R
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.foundation.layout.PaddingValues
import com.example.data.db.HubItemEntity
import com.example.optimizer.OptimizationProgress
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.EmeraldTertiary
import com.example.ui.theme.IndigoSecondary
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.util.dpadFocusable
import com.example.ui.viewmodel.HubitViewModel

@Composable
fun HomeScreen(
    viewModel: HubitViewModel,
    onNavigateTab: (Int) -> Unit
) {
    val localIp by viewModel.localIpAddress.collectAsState()
    val isServerRunning by viewModel.isServerRunning.collectAsState()
    val latestItem by viewModel.latestItem.collectAsState()
    val activeDownloads by viewModel.activeDownloads.collectAsState()
    val memoryInfo by viewModel.systemMemoryInfo.collectAsState()
    val optProgress by viewModel.optimizationProgress.collectAsState()

    val webDashboardUrl = "http://$localIp:8080"

    val infiniteTransition = rememberInfiniteTransition(label = "serverStatusPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Live Background Optimization Service Indicator
        if (optProgress.isRunning || optProgress.isCompleted) {
            item {
                BackgroundOptimizationProgressCard(
                    progress = optProgress,
                    onDismiss = { viewModel.dismissOptimizationProgress() }
                )
            }
        }

        // Hero Web Bridge Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, CyanPrimary.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF0F172A),
                                    Color(0xFF1E1B4B),
                                    Color(0xFF0284C7).copy(alpha = 0.3f)
                                )
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isServerRunning) EmeraldTertiary.copy(alpha = if (isServerRunning) pulseAlpha * 0.4f else 0.2f) else Color.Red.copy(alpha = 0.2f),
                                    modifier = Modifier.padding(end = 10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .size(10.dp)
                                            .background(
                                                color = if (isServerRunning) EmeraldTertiary.copy(alpha = pulseAlpha) else Color.Red,
                                                shape = CircleShape
                                            )
                                    )
                                }
                                Text(
                                    text = if (isServerRunning) stringResource(R.string.bridge_status_running) else stringResource(R.string.bridge_status_stopped),
                                    color = if (isServerRunning) EmeraldTertiary else Color.Red,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }

                            Row {
                                IconButton(onClick = { viewModel.refreshNetworkInfo() }) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Refresh IP", tint = TextSecondary)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = stringResource(R.string.bridge_hero_desc),
                            color = TextSecondary,
                            fontSize = 14.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = webDashboardUrl,
                                color = CyanPrimary,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            )

                            Button(
                                onClick = { onNavigateTab(1) },
                                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                                modifier = Modifier
                                    .testTag("home_open_qr_button")
                                    .dpadFocusable()
                            ) {
                                Icon(Icons.Default.QrCode, contentDescription = "QR Code Icon", tint = Color.Black)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(stringResource(R.string.bridge_btn_open), color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = stringResource(R.string.bridge_hero_title),
                            color = Color(0xFF94A3B8),
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Section: Vừa nhận (Contextual Action Card)
        item {
            Column {
                Text(
                    text = "⚡ " + stringResource(R.string.bridge_history_title, 1),
                    color = CyanPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (latestItem != null) {
                    RecentItemContextCard(
                        item = latestItem!!,
                        onPlayVideo = { title, path -> viewModel.playVideo(title, path) },
                        onInstallApk = { path -> viewModel.installApkFile(path) },
                        onDownloadLink = { url -> viewModel.downloadUrl(url) }
                    )
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Wifi, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(stringResource(R.string.bridge_history_empty), color = TextPrimary, fontWeight = FontWeight.SemiBold)
                            Text(stringResource(R.string.bridge_qr_hint), color = TextSecondary, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Section: Active Downloads Overview
        if (activeDownloads.isNotEmpty()) {
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🚀 " + stringResource(R.string.fetcher_active_tasks_title, activeDownloads.size),
                            color = AccentOrange,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        OutlinedButton(onClick = { onNavigateTab(2) }) {
                            Text(stringResource(R.string.quick_action_fetcher), color = AccentOrange, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    activeDownloads.forEach { download ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkSurface),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(download.title, color = TextPrimary, fontWeight = FontWeight.Bold, maxLines = 1)
                                    Text(download.downloadSpeed, color = CyanPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = { download.progress / 100f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = CyanPrimary,
                                    trackColor = DarkBorder
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section: System Health Metrics & Quick Optimizers
        item {
            Column {
                Text(
                    text = "🛠 " + stringResource(R.string.system_status_title),
                    color = CyanPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // RAM Card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.boostRam() },
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Speed, contentDescription = null, tint = CyanPrimary)
                                Text(stringResource(R.string.system_ram_label), color = TextSecondary, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            val ramUsed = memoryInfo?.usedRamMb ?: 1850
                            val ramTotal = memoryInfo?.totalRamMb ?: 3000
                            Text("${ramUsed}MB / ${ramTotal}MB", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { (memoryInfo?.ramUsagePercent ?: 62) / 100f },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                                color = if ((memoryInfo?.ramUsagePercent ?: 62) > 80) Color.Red else CyanPrimary,
                                trackColor = DarkBorder
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { viewModel.boostRam() },
                                colors = ButtonDefaults.buttonColors(containerColor = IndigoSecondary),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .dpadFocusable(onClick = { viewModel.boostRam() })
                            ) {
                                Icon(Icons.Default.RocketLaunch, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(R.string.optimizer_btn_boost), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Storage Card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.cleanCache() },
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CleaningServices, contentDescription = null, tint = EmeraldTertiary)
                                Text(stringResource(R.string.system_storage_label), color = TextSecondary, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            val usedGb = memoryInfo?.usedStorageGb ?: 8.4
                            val totalGb = memoryInfo?.totalStorageGb ?: 16.0
                            Text("${usedGb}GB / ${totalGb}GB", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { (memoryInfo?.storageUsagePercent ?: 52) / 100f },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                                color = EmeraldTertiary,
                                trackColor = DarkBorder
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { viewModel.cleanCache() },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldTertiary),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .dpadFocusable(onClick = { viewModel.cleanCache() })
                            ) {
                                Icon(Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(R.string.optimizer_btn_clean_cache), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecentItemContextCard(
    item: HubItemEntity,
    onPlayVideo: (String, String) -> Unit,
    onInstallApk: (String) -> Unit,
    onDownloadLink: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = when (item.type) {
                    "APK" -> EmeraldTertiary.copy(alpha = 0.2f)
                    "VIDEO" -> AccentOrange.copy(alpha = 0.2f)
                    "LINK" -> CyanPrimary.copy(alpha = 0.2f)
                    else -> IndigoSecondary.copy(alpha = 0.2f)
                },
                modifier = Modifier.size(54.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = when (item.type) {
                            "APK" -> Icons.Default.Android
                            "VIDEO" -> Icons.Default.Movie
                            "LINK" -> Icons.Default.Download
                            else -> Icons.Default.FolderZip
                        },
                        contentDescription = null,
                        tint = when (item.type) {
                            "APK" -> EmeraldTertiary
                            "VIDEO" -> AccentOrange
                            "LINK" -> CyanPrimary
                            else -> IndigoSecondary
                        },
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = stringResource(R.string.bridge_source_prefix, item.receivedSource) + " • " + formatFileSize(item.fileSize),
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Contextual Button Action
            when (item.type) {
                "APK" -> {
                    Button(
                        onClick = { onInstallApk(item.pathOrUrl) },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldTertiary),
                        modifier = Modifier.dpadFocusable()
                    ) {
                        Icon(Icons.Default.Android, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.fetcher_action_install), fontWeight = FontWeight.Bold)
                    }
                }
                "VIDEO" -> {
                    Button(
                        onClick = { onPlayVideo(item.title, item.pathOrUrl) },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                        modifier = Modifier.dpadFocusable()
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.hub_iptv_play), fontWeight = FontWeight.Bold)
                    }
                }
                "LINK" -> {
                    Button(
                        onClick = { onDownloadLink(item.pathOrUrl) },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                        modifier = Modifier.dpadFocusable()
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.fetcher_btn_download), color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
                else -> {
                    OutlinedButton(
                        onClick = { },
                        modifier = Modifier.dpadFocusable()
                    ) {
                        Text(stringResource(R.string.fetcher_action_open), color = TextPrimary)
                    }
                }
            }
        }
    }
}

fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "Unknown"
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    return when {
        gb >= 1.0 -> String.format("%.2f GB", gb)
        mb >= 1.0 -> String.format("%.1f MB", mb)
        else -> String.format("%.0f KB", kb)
    }
}

@Composable
fun BackgroundOptimizationProgressCard(
    progress: OptimizationProgress,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .dpadFocusable(shape = RoundedCornerShape(16.dp), onClick = onDismiss),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (progress.isCompleted) EmeraldTertiary.copy(alpha = 0.15f) else DarkSurface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            if (progress.isCompleted) EmeraldTertiary else CyanPrimary
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (progress.isCompleted) EmeraldTertiary else CyanPrimary,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (progress.isCompleted) Icons.Default.CheckCircle else Icons.Default.Speed,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (progress.isCompleted) stringResource(R.string.opt_banner_completed) else stringResource(R.string.opt_banner_running),
                            color = if (progress.isCompleted) EmeraldTertiary else CyanPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = progress.stage,
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (progress.isCompleted) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldTertiary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(stringResource(R.string.opt_banner_dismiss), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }

            if (progress.isRunning) {
                Spacer(modifier = Modifier.height(14.dp))
                LinearProgressIndicator(
                    progress = { progress.progressPercent / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = CyanPrimary,
                    trackColor = DarkBorder
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.opt_progress_label, progress.progressPercent),
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                    Text(
                        text = stringResource(R.string.opt_freed_cache, progress.freedCacheMb),
                        color = CyanPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
