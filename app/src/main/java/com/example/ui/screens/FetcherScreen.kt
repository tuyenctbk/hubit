package com.example.ui.screens

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.db.HubItemEntity
import com.example.fetcher.AdBlocker
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkCard
import com.example.ui.theme.EmeraldTertiary
import com.example.ui.theme.IndigoSecondary
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.util.dpadFocusable
import com.example.ui.viewmodel.HubitViewModel

@Composable
fun FetcherScreen(viewModel: HubitViewModel) {
    var subTab by remember { mutableIntStateOf(0) } // 0: Downloader, 1: Browser

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // Sub-Tab selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface, RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                onClick = { subTab = 0 },
                shape = RoundedCornerShape(10.dp),
                color = if (subTab == 0) CyanPrimary else Color.Transparent,
                modifier = Modifier
                    .weight(1f)
                    .dpadFocusable(shape = RoundedCornerShape(10.dp), onClick = { subTab = 0 })
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.RocketLaunch,
                        contentDescription = null,
                        tint = if (subTab == 0) Color.Black else TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.quick_action_fetcher),
                        color = if (subTab == 0) Color.Black else TextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            Surface(
                onClick = { subTab = 1 },
                shape = RoundedCornerShape(10.dp),
                color = if (subTab == 1) CyanPrimary else Color.Transparent,
                modifier = Modifier
                    .weight(1f)
                    .dpadFocusable(shape = RoundedCornerShape(10.dp), onClick = { subTab = 1 })
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Language,
                        contentDescription = null,
                        tint = if (subTab == 1) Color.Black else TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Web Sniffer & Ad-Blocker",
                        color = if (subTab == 1) Color.Black else TextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (subTab == 0) {
            DownloaderSubScreen(viewModel)
        } else {
            BrowserSubScreen(viewModel)
        }
    }
}

@Composable
fun DownloaderSubScreen(viewModel: HubitViewModel) {
    var inputUrl by remember { mutableStateOf("") }
    val allItems by viewModel.allHubItems.collectAsState()

    val downloads = allItems.filter { it.receivedSource == "DOWNLOADER" || it.status == "DOWNLOADING" }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = CyanPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.fetcher_title_download),
                            color = CyanPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inputUrl,
                            onValueChange = { inputUrl = it },
                            placeholder = { Text(stringResource(R.string.fetcher_url_placeholder), color = TextSecondary) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanPrimary,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Button(
                            onClick = {
                                viewModel.downloadUrl(inputUrl)
                                inputUrl = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .height(56.dp)
                                .dpadFocusable(onClick = {
                                    viewModel.downloadUrl(inputUrl)
                                    inputUrl = ""
                                })
                        ) {
                            Icon(Icons.Default.RocketLaunch, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.fetcher_btn_download), color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = stringResource(R.string.fetcher_active_tasks_title, downloads.size),
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        if (downloads.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(36.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(stringResource(R.string.fetcher_no_tasks), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(stringResource(R.string.fetcher_no_tasks_hint), color = TextSecondary, fontSize = 12.sp)
                    }
                }
            }
        } else {
            items(downloads, key = { it.id }) { download ->
                MultiThreadDownloadItemCard(
                    download = download,
                    onPause = { viewModel.pauseDownload(download.id) },
                    onResume = { viewModel.resumeDownload(download.id) },
                    onRetry = { viewModel.retryDownload(download.id) },
                    onCancel = { viewModel.cancelDownload(download.id) },
                    onAction = {
                        when (download.type) {
                            "APK" -> viewModel.installApkFile(download.pathOrUrl)
                            "VIDEO", "AUDIO" -> viewModel.playVideo(download.title, download.pathOrUrl)
                            else -> viewModel.showToast("File đã lưu tại: ${download.pathOrUrl}")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun MultiThreadDownloadItemCard(
    download: HubItemEntity,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onRetry: () -> Unit,
    onCancel: () -> Unit,
    onAction: () -> Unit
) {
    val statusColor = when (download.status) {
        "DOWNLOADING" -> CyanPrimary
        "PAUSED" -> AccentOrange
        "COMPLETED" -> EmeraldTertiary
        else -> Color(0xFFEF4444)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .dpadFocusable(
                shape = RoundedCornerShape(14.dp),
                onClick = {
                    if (download.status == "COMPLETED") onAction()
                    else if (download.status == "DOWNLOADING") onPause()
                    else if (download.status == "PAUSED") onResume()
                }
            ),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                        shape = RoundedCornerShape(10.dp),
                        color = statusColor.copy(alpha = 0.15f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = when (download.type) {
                                    "APK" -> Icons.Default.Android
                                    "VIDEO" -> Icons.Default.Movie
                                    "AUDIO" -> Icons.Default.PlayArrow
                                    else -> Icons.Default.FolderZip
                                },
                                contentDescription = null,
                                tint = statusColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = download.title,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = download.downloadSpeed.ifEmpty { stringResource(R.string.status_ready) },
                                color = statusColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (download.fileSize > 0) {
                                Text(" • ", color = TextSecondary, fontSize = 11.sp)
                                Text(
                                    text = formatFileSize(download.fileSize),
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                // Action buttons for pause/resume/cancel/install
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    when (download.status) {
                        "DOWNLOADING" -> {
                            // Pause button
                            IconButton(
                                onClick = onPause,
                                modifier = Modifier
                                    .background(AccentOrange.copy(alpha = 0.2f), CircleShape)
                                    .dpadFocusable(shape = CircleShape, onClick = onPause)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(Modifier.size(3.dp, 12.dp).background(AccentOrange, RoundedCornerShape(1.dp)))
                                    Box(Modifier.size(3.dp, 12.dp).background(AccentOrange, RoundedCornerShape(1.dp)))
                                }
                            }
                            // Cancel button
                            IconButton(
                                onClick = onCancel,
                                modifier = Modifier
                                    .background(Color.Red.copy(alpha = 0.2f), CircleShape)
                                    .dpadFocusable(shape = CircleShape, onClick = onCancel)
                            ) {
                                Icon(Icons.Default.Cancel, contentDescription = "Cancel", tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
                            }
                        }
                        "PAUSED" -> {
                            // Resume button
                            IconButton(
                                onClick = onResume,
                                modifier = Modifier
                                    .background(EmeraldTertiary.copy(alpha = 0.2f), CircleShape)
                                    .dpadFocusable(shape = CircleShape, onClick = onResume)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Resume", tint = EmeraldTertiary, modifier = Modifier.size(20.dp))
                            }
                            // Cancel button
                            IconButton(
                                onClick = onCancel,
                                modifier = Modifier
                                    .background(Color.Red.copy(alpha = 0.2f), CircleShape)
                                    .dpadFocusable(shape = CircleShape, onClick = onCancel)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
                            }
                        }
                        "FAILED" -> {
                            // Retry button
                            IconButton(
                                onClick = onRetry,
                                modifier = Modifier
                                    .background(CyanPrimary.copy(alpha = 0.2f), CircleShape)
                                    .dpadFocusable(shape = CircleShape, onClick = onRetry)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Retry", tint = CyanPrimary, modifier = Modifier.size(20.dp))
                            }
                            IconButton(
                                onClick = onCancel,
                                modifier = Modifier
                                    .background(Color.Red.copy(alpha = 0.2f), CircleShape)
                                    .dpadFocusable(shape = CircleShape, onClick = onCancel)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
                            }
                        }
                        "COMPLETED" -> {
                            Button(
                                onClick = onAction,
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldTertiary),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.dpadFocusable(shape = RoundedCornerShape(8.dp), onClick = onAction)
                            ) {
                                Icon(
                                    imageVector = if (download.type == "APK") Icons.Default.Android else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (download.type == "APK") stringResource(R.string.fetcher_action_install) else stringResource(R.string.fetcher_action_open),
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            IconButton(
                                onClick = onCancel,
                                modifier = Modifier
                                    .background(DarkCard, CircleShape)
                                    .dpadFocusable(shape = CircleShape, onClick = onCancel)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextSecondary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            if (download.status == "DOWNLOADING" || download.status == "PAUSED") {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (download.status == "PAUSED") stringResource(R.string.fetcher_paused_status, download.progress) else stringResource(R.string.fetcher_progress_label, download.progress),
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                    Text(
                        text = stringResource(R.string.fetcher_threads_active),
                        color = CyanPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { download.progress / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = statusColor,
                    trackColor = DarkBorder
                )
            }
        }
    }
}

@Composable
fun BrowserSubScreen(viewModel: HubitViewModel) {
    var webUrl by remember { mutableStateOf("https://www.google.com") }
    var currentUrl by remember { mutableStateOf("https://www.google.com") }
    var adBlockerEnabled by remember { mutableStateOf(true) }
    var detectedMediaUrl by remember { mutableStateOf<String?>(null) }
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }

    val bookmarks = listOf(
        "Google" to "https://www.google.com",
        "APKPure" to "https://apkpure.net",
        "FShare" to "https://www.fshare.vn",
        "PhimMoi" to "https://phimmoichillv.net"
    )

    Column(modifier = Modifier.fillMaxSize()) {
        // Address Bar & Controls
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { webViewInstance?.goBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                    IconButton(onClick = { webViewInstance?.goForward() }) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "Forward", tint = TextPrimary)
                    }
                    IconButton(onClick = { webViewInstance?.reload() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reload", tint = TextPrimary)
                    }

                    OutlinedTextField(
                        value = webUrl,
                        onValueChange = { webUrl = it },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            currentUrl = if (!webUrl.startsWith("http")) "https://$webUrl" else webUrl
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                    ) {
                        Text(stringResource(R.string.fetcher_btn_go), color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(bookmarks) { (name, url) ->
                            FilterChip(
                                selected = false,
                                onClick = {
                                    webUrl = url
                                    currentUrl = url
                                },
                                label = { Text(name, fontSize = 11.sp, color = TextPrimary) },
                                colors = FilterChipDefaults.filterChipColors(containerColor = DarkBorder)
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = EmeraldTertiary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.fetcher_adblocker_title), fontSize = 11.sp, color = EmeraldTertiary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Switch(
                            checked = adBlockerEnabled,
                            onCheckedChange = { adBlockerEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = EmeraldTertiary)
                        )
                    }
                }
            }
        }

        // Sniffer Pop-up Alert
        AnimatedVisibility(visible = detectedMediaUrl != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = AccentOrange.copy(alpha = 0.2f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, AccentOrange)
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
                        Icon(Icons.Default.RocketLaunch, contentDescription = null, tint = AccentOrange)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(stringResource(R.string.fetcher_sniffer_detected), color = AccentOrange, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(detectedMediaUrl ?: "", color = TextPrimary, fontSize = 11.sp, maxLines = 1)
                        }
                    }

                    Button(
                        onClick = {
                            detectedMediaUrl?.let { viewModel.downloadUrl(it) }
                            detectedMediaUrl = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentOrange)
                    ) {
                        Text(stringResource(R.string.fetcher_btn_download), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // WebView
        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(12.dp)
        ) {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        webViewClient = object : WebViewClient() {
                            override fun shouldInterceptRequest(
                                view: WebView?,
                                request: android.webkit.WebResourceRequest?
                            ): android.webkit.WebResourceResponse? {
                                val url = request?.url?.toString() ?: ""
                                if (adBlockerEnabled && AdBlocker.isAdUrl(url)) {
                                    return AdBlocker.createEmptyResponse()
                                }

                                val sniffType = AdBlocker.sniffMediaOrApkLink(url)
                                if (sniffType != null) {
                                    detectedMediaUrl = url
                                }

                                return super.shouldInterceptRequest(view, request)
                            }
                        }
                        loadUrl(currentUrl)
                        webViewInstance = this
                    }
                },
                update = { wv ->
                    if (wv.url != currentUrl) {
                        wv.loadUrl(currentUrl)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
