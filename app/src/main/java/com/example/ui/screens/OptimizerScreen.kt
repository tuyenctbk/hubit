package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.example.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun OptimizerScreen(viewModel: HubitViewModel) {
    val memoryInfo by viewModel.systemMemoryInfo.collectAsState()
    val allItems by viewModel.allHubItems.collectAsState()
    val optProgress by viewModel.optimizationProgress.collectAsState()

    val installedApkResiduals = allItems.filter { it.type == "APK" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Section: Active Optimization Progress
        if (optProgress.isRunning || optProgress.isCompleted) {
            item {
                BackgroundOptimizationProgressCard(
                    progress = optProgress,
                    onDismiss = { viewModel.dismissOptimizationProgress() }
                )
            }
        }

        // Section: System Health Summary Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, EmeraldTertiary.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Speed, contentDescription = null, tint = EmeraldTertiary, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(stringResource(R.string.optimizer_header_title), color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Text(stringResource(R.string.optimizer_header_subtitle), color = TextSecondary, fontSize = 12.sp)
                            }
                        }

                        Button(
                            onClick = { viewModel.refreshSystemMemory() },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldTertiary),
                            modifier = Modifier.dpadFocusable(onClick = { viewModel.refreshSystemMemory() })
                        ) {
                            Icon(Icons.Default.AutoMode, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.optimizer_btn_rescan), color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // RAM Gauge Box
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                                .padding(16.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Memory, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(stringResource(R.string.optimizer_ram_box_title), color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                val usedRam = memoryInfo?.usedRamMb ?: 1850
                                val totalRam = memoryInfo?.totalRamMb ?: 3000
                                Text("${usedRam}MB / ${totalRam}MB", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { (memoryInfo?.ramUsagePercent ?: 62) / 100f },
                                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                                    color = CyanPrimary,
                                    trackColor = DarkBorder
                                )
                            }
                        }

                        // Storage Gauge Box
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                                .padding(16.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Storage, contentDescription = null, tint = EmeraldTertiary, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(stringResource(R.string.optimizer_storage_box_title), color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                val usedGb = memoryInfo?.usedStorageGb ?: 8.4
                                val totalGb = memoryInfo?.totalStorageGb ?: 16.0
                                Text("${usedGb}GB / ${totalGb}GB", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { (memoryInfo?.storageUsagePercent ?: 52) / 100f },
                                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                                    color = EmeraldTertiary,
                                    trackColor = DarkBorder
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section: One-Click Boost Action Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, IndigoSecondary)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Icon(Icons.Default.RocketLaunch, contentDescription = null, tint = IndigoSecondary, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(stringResource(R.string.optimizer_ram_booster_title), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(stringResource(R.string.optimizer_ram_booster_desc), color = TextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.boostRam() },
                            colors = ButtonDefaults.buttonColors(containerColor = IndigoSecondary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .dpadFocusable(onClick = { viewModel.boostRam() }),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(stringResource(R.string.optimizer_btn_boost_ram), fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldTertiary)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Icon(Icons.Default.CleaningServices, contentDescription = null, tint = EmeraldTertiary, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(stringResource(R.string.optimizer_cache_clean_title), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(stringResource(R.string.optimizer_cache_clean_desc), color = TextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.cleanCache() },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldTertiary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .dpadFocusable(onClick = { viewModel.cleanCache() }),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(stringResource(R.string.optimizer_btn_clean_cache_mb, memoryInfo?.cacheSizeMb ?: 380), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Section: Full Background Cleaner Trigger Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = CyanPrimary,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Speed, contentDescription = null, tint = Color.Black)
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(stringResource(R.string.optimizer_service_title), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(stringResource(R.string.optimizer_service_desc), color = TextSecondary, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Button(
                        onClick = { viewModel.startBackgroundOptimization() },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                        modifier = Modifier.dpadFocusable(onClick = { viewModel.startBackgroundOptimization() }),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.optimizer_btn_start_cleaner), color = Color.Black, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }

        // Section: Post-Action Cleanup (Delete Installed APK files)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, AccentOrange.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = AccentOrange)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.optimizer_post_action_title),
                            color = AccentOrange,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(R.string.optimizer_post_action_desc),
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (installedApkResiduals.isEmpty()) {
                        Text(stringResource(R.string.optimizer_no_residuals_msg), color = EmeraldTertiary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    } else {
                        installedApkResiduals.forEach { apkItem ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Android, contentDescription = null, tint = EmeraldTertiary)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(apkItem.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(formatFileSize(apkItem.fileSize), color = TextSecondary, fontSize = 11.sp)
                                    }
                                }

                                Button(
                                    onClick = { viewModel.deleteHubItem(apkItem.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f))
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(stringResource(R.string.optimizer_btn_delete_apk), fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
