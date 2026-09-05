package com.example.ui.screens

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.AddLink
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.foundation.clickable
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.example.data.db.HubItemEntity
import com.example.data.model.InstalledAppInfo
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
fun HubScreen(viewModel: HubitViewModel) {
    var mainTab by remember { mutableIntStateOf(0) } // 0: Library, 1: IPTV, 2: Sideload Launcher

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // Tab Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface, RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                onClick = { mainTab = 0 },
                shape = RoundedCornerShape(10.dp),
                color = if (mainTab == 0) CyanPrimary else Color.Transparent,
                modifier = Modifier
                    .weight(1f)
                    .dpadFocusable(shape = RoundedCornerShape(10.dp), onClick = { mainTab = 0 })
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Folder,
                        contentDescription = null,
                        tint = if (mainTab == 0) Color.Black else TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.hub_tab_library),
                        color = if (mainTab == 0) Color.Black else TextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            Surface(
                onClick = { mainTab = 1 },
                shape = RoundedCornerShape(10.dp),
                color = if (mainTab == 1) CyanPrimary else Color.Transparent,
                modifier = Modifier
                    .weight(1f)
                    .dpadFocusable(shape = RoundedCornerShape(10.dp), onClick = { mainTab = 1 })
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Tv,
                        contentDescription = null,
                        tint = if (mainTab == 1) Color.Black else TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.hub_tab_iptv),
                        color = if (mainTab == 1) Color.Black else TextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            Surface(
                onClick = { mainTab = 2 },
                shape = RoundedCornerShape(10.dp),
                color = if (mainTab == 2) CyanPrimary else Color.Transparent,
                modifier = Modifier
                    .weight(1f)
                    .dpadFocusable(shape = RoundedCornerShape(10.dp), onClick = { mainTab = 2 })
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Apps,
                        contentDescription = null,
                        tint = if (mainTab == 2) Color.Black else TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.hub_tab_hidden_apps),
                        color = if (mainTab == 2) Color.Black else TextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            Surface(
                onClick = { mainTab = 3 },
                shape = RoundedCornerShape(10.dp),
                color = if (mainTab == 3) CyanPrimary else Color.Transparent,
                modifier = Modifier
                    .weight(1f)
                    .dpadFocusable(shape = RoundedCornerShape(10.dp), onClick = { mainTab = 3 })
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.FolderOpen,
                        contentDescription = null,
                        tint = if (mainTab == 3) Color.Black else TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.hub_tab_tv_files),
                        color = if (mainTab == 3) Color.Black else TextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (mainTab) {
            0 -> SmartLibrarySubScreen(viewModel)
            1 -> IptvLiveTvSubScreen(viewModel)
            2 -> SideloadLauncherSubScreen(viewModel)
            3 -> TvFileBrowserSubScreen(viewModel)
        }
    }
}

@Composable
fun SmartLibrarySubScreen(viewModel: HubitViewModel) {
    val allItems by viewModel.allHubItems.collectAsState()
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filteredItems = when (selectedFilter) {
        "APK" -> allItems.filter { it.type == "APK" }
        "VIDEO" -> allItems.filter { it.type == "VIDEO" }
        "AUDIO" -> allItems.filter { it.type == "AUDIO" }
        "IMAGE" -> allItems.filter { it.type == "IMAGE" }
        else -> allItems
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            FilterChip(
                selected = selectedFilter == "ALL",
                onClick = { selectedFilter = "ALL" },
                label = { Text(stringResource(R.string.hub_filter_all, allItems.size)) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = CyanPrimary, selectedLabelColor = Color.Black)
            )
            FilterChip(
                selected = selectedFilter == "VIDEO",
                onClick = { selectedFilter = "VIDEO" },
                label = { Text(stringResource(R.string.hub_filter_video)) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = AccentOrange, selectedLabelColor = Color.White)
            )
            FilterChip(
                selected = selectedFilter == "APK",
                onClick = { selectedFilter = "APK" },
                label = { Text(stringResource(R.string.hub_filter_apk)) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = EmeraldTertiary, selectedLabelColor = Color.White)
            )
        }

        if (filteredItems.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(modifier = Modifier.padding(32.dp), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.hub_empty_library), color = TextSecondary)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(filteredItems) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = when (item.type) {
                                        "APK" -> EmeraldTertiary.copy(alpha = 0.2f)
                                        "VIDEO" -> AccentOrange.copy(alpha = 0.2f)
                                        else -> IndigoSecondary.copy(alpha = 0.2f)
                                    },
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = when (item.type) {
                                                "APK" -> Icons.Default.Android
                                                "VIDEO" -> Icons.Default.Movie
                                                "AUDIO" -> Icons.Default.MusicNote
                                                "IMAGE" -> Icons.Default.Image
                                                else -> Icons.Default.Folder
                                            },
                                            contentDescription = null,
                                            tint = when (item.type) {
                                                "APK" -> EmeraldTertiary
                                                "VIDEO" -> AccentOrange
                                                else -> IndigoSecondary
                                            }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column {
                                    Text(
                                        text = item.title,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "${item.receivedSource} • ${formatFileSize(item.fileSize)}",
                                        color = TextSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (item.type == "VIDEO") {
                                    Button(
                                        onClick = { viewModel.playVideo(item.title, item.pathOrUrl) },
                                        colors = ButtonDefaults.buttonColors(containerColor = AccentOrange)
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(stringResource(R.string.hub_play_exo), fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                } else if (item.type == "APK") {
                                    Button(
                                        onClick = { viewModel.installApkFile(item.pathOrUrl) },
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldTertiary)
                                    ) {
                                        Icon(Icons.Default.Android, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(stringResource(R.string.hub_install_apk), fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                }

                                IconButton(onClick = { viewModel.deleteHubItem(item.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SideloadLauncherSubScreen(viewModel: HubitViewModel) {
    val installedApps by viewModel.installedApps.collectAsState()
    var filterMode by remember { mutableIntStateOf(0) } // 0: All, 1: Phone Apps (Hidden Sideloaded), 2: TV Apps

    val filteredApps = when (filterMode) {
        1 -> installedApps.filter { !it.isTvApp }
        2 -> installedApps.filter { it.isTvApp }
        else -> installedApps
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            FilterChip(
                selected = filterMode == 0,
                onClick = { filterMode = 0 },
                label = { Text(stringResource(R.string.hub_filter_all_apps, installedApps.size)) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = CyanPrimary, selectedLabelColor = Color.Black)
            )
            FilterChip(
                selected = filterMode == 1,
                onClick = { filterMode = 1 },
                label = { Text(stringResource(R.string.hub_filter_sideloaded)) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = IndigoSecondary, selectedLabelColor = Color.White)
            )
            FilterChip(
                selected = filterMode == 2,
                onClick = { filterMode = 2 },
                label = { Text(stringResource(R.string.hub_filter_tv_apps)) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = EmeraldTertiary, selectedLabelColor = Color.White)
            )
        }

        if (filteredApps.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(modifier = Modifier.padding(32.dp), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.hub_no_apps_found), color = TextSecondary)
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredApps) { app ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (app.iconDrawable is Drawable) {
                                val bitmap = (app.iconDrawable as Drawable).toBitmap(96, 96)
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = app.appName,
                                    modifier = Modifier.size(52.dp)
                                )
                            } else {
                                Icon(
                                    Icons.Default.Android,
                                    contentDescription = null,
                                    tint = CyanPrimary,
                                    modifier = Modifier.size(52.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = app.appName,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                maxLines = 1
                            )

                            Text(
                                text = if (app.isTvApp) stringResource(R.string.hub_app_tv) else stringResource(R.string.hub_app_sideload),
                                color = if (app.isTvApp) EmeraldTertiary else IndigoSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = { viewModel.launchInstalledApp(app.packageName) },
                                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Launch, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(R.string.hub_btn_open_app), color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IptvLiveTvSubScreen(viewModel: HubitViewModel) {
    val allItems by viewModel.allHubItems.collectAsState()
    val iptvItems = allItems.filter { it.type == "IPTV" }

    var m3uUrlInput by remember { mutableStateOf("https://iptv.live/vietnam.m3u") }
    var selectedGroup by remember { mutableStateOf("TẤT CẢ") }

    // Parse channel groups
    val groups = remember(iptvItems) {
        val set = mutableSetOf("TẤT CẢ")
        iptvItems.forEach { item ->
            val parts = item.receivedSource.split("|")
            if (parts.size >= 2 && parts[1].isNotBlank()) {
                set.add(parts[1])
            }
        }
        set.toList()
    }

    val filteredChannels = if (selectedGroup == "TẤT CẢ") {
        iptvItems
    } else {
        iptvItems.filter { item ->
            val parts = item.receivedSource.split("|")
            parts.size >= 2 && parts[1] == selectedGroup
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Playlist Import Bar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LiveTv, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.hub_iptv_title), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Text(stringResource(R.string.hub_iptv_channels_count, iptvItems.size), color = CyanPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = m3uUrlInput,
                        onValueChange = { m3uUrlInput = it },
                        placeholder = { Text(stringResource(R.string.hub_iptv_url_placeholder), fontSize = 12.sp, color = TextSecondary) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Button(
                        onClick = { viewModel.importIptvPlaylist(m3uUrlInput) },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.AddLink, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.hub_iptv_btn_load), color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Category Filter Chips
        if (groups.size > 1) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                groups.forEach { group ->
                    FilterChip(
                        selected = selectedGroup == group,
                        onClick = { selectedGroup = group },
                        label = { Text(group, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanPrimary,
                            selectedLabelColor = Color.Black,
                            labelColor = TextSecondary
                        )
                    )
                }
            }
        }

        if (filteredChannels.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(modifier = Modifier.padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Tv, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(stringResource(R.string.hub_iptv_no_channels), color = TextSecondary, fontSize = 14.sp)
                        Text(stringResource(R.string.hub_iptv_no_channels_hint), color = TextSecondary, fontSize = 12.sp)
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 220.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredChannels) { channel ->
                    val parts = channel.receivedSource.split("|")
                    val groupName = if (parts.size >= 2) parts[1] else "Live TV"

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = AccentOrange.copy(alpha = 0.2f),
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Tv, contentDescription = null, tint = AccentOrange, modifier = Modifier.size(24.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Text(
                                        text = channel.title,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = groupName,
                                        color = AccentOrange,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Button(
                                    onClick = { viewModel.playVideo(channel.title, channel.pathOrUrl) },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(stringResource(R.string.hub_iptv_play), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }

                                IconButton(onClick = { viewModel.deleteHubItem(channel.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Xóa", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

