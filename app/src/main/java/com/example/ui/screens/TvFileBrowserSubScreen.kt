package com.example.ui.screens

import android.content.Context
import android.os.Environment
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.util.dpadFocusable
import com.example.ui.viewmodel.HubitViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

data class StorageDrive(
    val name: String,
    val path: String,
    val icon: ImageVector,
    val isRemovable: Boolean
)

data class FileItem(
    val file: File,
    val name: String,
    val isDirectory: Boolean,
    val sizeBytes: Long,
    val lastModified: Long,
    val extension: String
)

@Composable
fun TvFileBrowserSubScreen(viewModel: HubitViewModel) {
    val context = LocalContext.current

    // Available storage roots (Internal, USB, App Downloads)
    val storageDrives = remember { detectStorageDrives(context) }
    var selectedDrive by remember { mutableStateOf(storageDrives.firstOrNull()?.path ?: "") }
    var currentDirectory by remember {
        mutableStateOf(
            File(selectedDrive.ifEmpty { context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath ?: "/storage/emulated/0" })
        )
    }

    var isGridView by remember { mutableStateOf(true) }
    var filterType by remember { mutableStateOf("ALL") } // ALL, APK, VIDEO, AUDIO, FOLDERS
    var searchQuery by remember { mutableStateOf("") }

    // Read files in current directory
    val fileList = remember(currentDirectory, filterType, searchQuery) {
        try {
            val files = currentDirectory.listFiles()?.toList() ?: emptyList()
            files.map { f ->
                FileItem(
                    file = f,
                    name = f.name,
                    isDirectory = f.isDirectory,
                    sizeBytes = if (f.isDirectory) 0L else f.length(),
                    lastModified = f.lastModified(),
                    extension = f.extension.lowercase()
                )
            }.filter { item ->
                val matchesSearch = searchQuery.isBlank() || item.name.contains(searchQuery, ignoreCase = true)
                val matchesFilter = when (filterType) {
                    "FOLDERS" -> item.isDirectory
                    "APK" -> item.extension == "apk"
                    "VIDEO" -> listOf("mp4", "mkv", "avi", "mov", "webm", "ts").contains(item.extension)
                    "AUDIO" -> listOf("mp3", "m4a", "flac", "wav", "aac").contains(item.extension)
                    else -> true
                }
                matchesSearch && matchesFilter
            }.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
        } catch (e: Exception) {
            emptyList()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Storage drive selector & view mode toolbar
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        storageDrives.forEach { drive ->
                            val isSelected = selectedDrive == drive.path
                            Surface(
                                onClick = {
                                    selectedDrive = drive.path
                                    currentDirectory = File(drive.path)
                                },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) CyanPrimary else DarkCard,
                                modifier = Modifier.dpadFocusable(
                                    shape = RoundedCornerShape(10.dp),
                                    onClick = {
                                        selectedDrive = drive.path
                                        currentDirectory = File(drive.path)
                                    }
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        drive.icon,
                                        contentDescription = null,
                                        tint = if (isSelected) Color.Black else TextPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        drive.name,
                                        color = if (isSelected) Color.Black else TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }

                    // Layout Mode Switch (List vs Grid)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { isGridView = false },
                            modifier = Modifier
                                .background(if (!isGridView) CyanPrimary else DarkCard, RoundedCornerShape(8.dp))
                                .dpadFocusable(shape = RoundedCornerShape(8.dp), onClick = { isGridView = false })
                        ) {
                            Icon(
                                Icons.Default.ViewList,
                                contentDescription = stringResource(R.string.fb_view_list),
                                tint = if (!isGridView) Color.Black else TextPrimary
                            )
                        }

                        IconButton(
                            onClick = { isGridView = true },
                            modifier = Modifier
                                .background(if (isGridView) CyanPrimary else DarkCard, RoundedCornerShape(8.dp))
                                .dpadFocusable(shape = RoundedCornerShape(8.dp), onClick = { isGridView = true })
                        ) {
                            Icon(
                                Icons.Default.GridView,
                                contentDescription = stringResource(R.string.fb_view_grid),
                                tint = if (isGridView) Color.Black else TextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Breadcrumb navigation & Parent Directory button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val canGoUp = currentDirectory.parentFile != null && currentDirectory.parentFile?.canRead() == true
                    Button(
                        onClick = {
                            currentDirectory.parentFile?.let { parent ->
                                if (parent.canRead()) currentDirectory = parent
                            }
                        },
                        enabled = canGoUp,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = IndigoSecondary,
                            disabledContainerColor = DarkCard
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.dpadFocusable(
                            shape = RoundedCornerShape(10.dp),
                            onClick = {
                                currentDirectory.parentFile?.let { parent ->
                                    if (parent.canRead()) currentDirectory = parent
                                }
                            }
                        )
                    ) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = stringResource(R.string.file_browser_up_level), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.file_browser_up_level), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Surface(
                        color = DarkCard,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.FolderOpen, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = currentDirectory.absolutePath,
                                color = TextSecondary,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Filter chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(
                        "ALL" to stringResource(R.string.file_filter_all),
                        "FOLDERS" to stringResource(R.string.file_filter_folders),
                        "APK" to stringResource(R.string.file_filter_apk),
                        "VIDEO" to stringResource(R.string.file_filter_video),
                        "AUDIO" to stringResource(R.string.file_filter_audio)
                    ).forEach { (type, label) ->
                        FilterChip(
                            selected = filterType == type,
                            onClick = { filterType = type },
                            label = { Text(label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanPrimary,
                                selectedLabelColor = Color.Black
                            ),
                            modifier = Modifier.dpadFocusable(
                                shape = RoundedCornerShape(8.dp),
                                onClick = { filterType = type }
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Files container
        if (fileList.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.FolderOff, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(stringResource(R.string.file_empty_directory), color = TextSecondary, fontSize = 14.sp)
                }
            }
        } else if (isGridView) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 180.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(fileList, key = { it.file.absolutePath }) { item ->
                    TvFileGridItem(
                        item = item,
                        onClick = {
                            if (item.isDirectory) {
                                currentDirectory = item.file
                            } else {
                                handleOpenFile(item, viewModel, context)
                            }
                        }
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(fileList, key = { it.file.absolutePath }) { item ->
                    TvFileListItem(
                        item = item,
                        onClick = {
                            if (item.isDirectory) {
                                currentDirectory = item.file
                            } else {
                                handleOpenFile(item, viewModel, context)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TvFileGridItem(
    item: FileItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .dpadFocusable(
                shape = RoundedCornerShape(14.dp),
                onClick = onClick
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isDirectory) DarkCard else DarkSurface
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = getIconBgColor(item),
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = getFileIcon(item),
                        contentDescription = null,
                        tint = getIconColor(item),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = item.name,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (item.isDirectory) stringResource(R.string.file_type_directory) else formatFileSize(item.sizeBytes),
                color = TextSecondary,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun TvFileListItem(
    item: FileItem,
    onClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val dateStr = remember(item.lastModified) { dateFormat.format(Date(item.lastModified)) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .dpadFocusable(
                shape = RoundedCornerShape(12.dp),
                onClick = onClick
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isDirectory) DarkCard else DarkSurface
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = getIconBgColor(item),
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = getFileIcon(item),
                        contentDescription = null,
                        tint = getIconColor(item),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${if (item.isDirectory) stringResource(R.string.file_type_subdirectory) else formatFileSize(item.sizeBytes)} • $dateStr",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }

            if (!item.isDirectory) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = getIconColor(item).copy(alpha = 0.15f),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = item.extension.uppercase().ifEmpty { "FILE" },
                        color = getIconColor(item),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

private fun getFileIcon(item: FileItem): ImageVector {
    return when {
        item.isDirectory -> Icons.Default.Folder
        item.extension == "apk" -> Icons.Default.Android
        listOf("mp4", "mkv", "avi", "mov", "webm", "ts").contains(item.extension) -> Icons.Default.Movie
        listOf("mp3", "m4a", "flac", "wav", "aac").contains(item.extension) -> Icons.Default.Audiotrack
        listOf("jpg", "jpeg", "png", "webp", "gif").contains(item.extension) -> Icons.Default.Image
        listOf("zip", "rar", "7z", "tar", "gz").contains(item.extension) -> Icons.Default.FolderZip
        listOf("txt", "log", "json", "pdf", "xml").contains(item.extension) -> Icons.Default.Description
        else -> Icons.Default.InsertDriveFile
    }
}

private fun getIconColor(item: FileItem): Color {
    return when {
        item.isDirectory -> CyanPrimary
        item.extension == "apk" -> EmeraldTertiary
        listOf("mp4", "mkv", "avi", "mov", "webm", "ts").contains(item.extension) -> AccentOrange
        listOf("mp3", "m4a", "flac", "wav", "aac").contains(item.extension) -> IndigoSecondary
        listOf("jpg", "jpeg", "png", "webp", "gif").contains(item.extension) -> Color(0xFFE879F9)
        else -> Color.LightGray
    }
}

private fun getIconBgColor(item: FileItem): Color {
    return getIconColor(item).copy(alpha = 0.15f)
}

private fun handleOpenFile(item: FileItem, viewModel: HubitViewModel, context: Context) {
    when (item.extension) {
        "apk" -> viewModel.installApkFile(item.file.absolutePath)
        "mp4", "mkv", "avi", "mov", "webm", "ts" -> viewModel.playVideo(item.name, item.file.absolutePath)
        "mp3", "m4a", "flac", "wav" -> viewModel.playVideo(item.name, item.file.absolutePath)
        else -> viewModel.showToast(context.getString(R.string.file_selected_toast, item.name, formatFileSize(item.sizeBytes)))
    }
}

private fun detectStorageDrives(context: Context): List<StorageDrive> {
    val drives = mutableListOf<StorageDrive>()

    // Hubit downloads dir
    val hubitDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "Hubit")
    if (!hubitDir.exists()) hubitDir.mkdirs()
    drives.add(StorageDrive(context.getString(R.string.settings_storage_internal_hubit), hubitDir.absolutePath, Icons.Default.Download, false))

    // Internal primary storage
    val internalStorage = Environment.getExternalStorageDirectory()
    drives.add(StorageDrive(context.getString(R.string.storage_internal), internalStorage.absolutePath, Icons.Default.Storage, false))

    // System download folder
    val publicDownload = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    if (publicDownload.exists()) {
        drives.add(StorageDrive(context.getString(R.string.storage_tv_downloads), publicDownload.absolutePath, Icons.Default.FolderSpecial, false))
    }

    // External USB/SD card detection
    try {
        val storageRoot = File("/storage")
        if (storageRoot.exists() && storageRoot.isDirectory) {
            storageRoot.listFiles()?.forEach { file ->
                if (file.isDirectory && file.canRead() && file.name != "emulated" && file.name != "self") {
                    drives.add(
                        StorageDrive(
                            name = context.getString(R.string.storage_usb, file.name),
                            path = file.absolutePath,
                            icon = Icons.Default.Usb,
                            isRemovable = true
                        )
                    )
                }
            }
        }
    } catch (e: Exception) {
        // Ignored
    }

    return drives
}
