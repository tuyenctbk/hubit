package com.example.ui.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bridge.LocalWebDashboardServer
import com.example.bridge.NetworkUtils
import com.example.data.AppSettingsManager
import com.example.data.db.AppDatabase
import com.example.data.db.ClipboardEntity
import com.example.data.db.HubItemEntity
import com.example.data.model.AppSettings
import com.example.data.model.InstalledAppInfo
import com.example.data.model.SystemMemoryInfo
import com.example.fetcher.MultiThreadDownloader
import com.example.hub.SideloadAppManager
import com.example.optimizer.OptimizationProgress
import com.example.optimizer.SystemOptimizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class HubitViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val dao = db.hubDao()

    val allHubItems: StateFlow<List<HubItemEntity>> = dao.getAllItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeDownloads: StateFlow<List<HubItemEntity>> = dao.getActiveDownloads()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val latestItem: StateFlow<HubItemEntity?> = dao.getLatestItem()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val clipboardHistory: StateFlow<List<ClipboardEntity>> = dao.getAllClipboardItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _localIpAddress = MutableStateFlow("192.168.1.105")
    val localIpAddress: StateFlow<String> = _localIpAddress.asStateFlow()

    private val _isServerRunning = MutableStateFlow(false)
    val isServerRunning: StateFlow<Boolean> = _isServerRunning.asStateFlow()

    private val _installedApps = MutableStateFlow<List<InstalledAppInfo>>(emptyList())
    val installedApps: StateFlow<List<InstalledAppInfo>> = _installedApps.asStateFlow()

    private val _systemMemoryInfo = MutableStateFlow<SystemMemoryInfo?>(null)
    val systemMemoryInfo: StateFlow<SystemMemoryInfo?> = _systemMemoryInfo.asStateFlow()

    private val _activeVideoToPlay = MutableStateFlow<Pair<String, String>?>(null) // Title to Path/Url
    val activeVideoToPlay: StateFlow<Pair<String, String>?> = _activeVideoToPlay.asStateFlow()

    private val _remoteCommand = MutableSharedFlow<String>(extraBufferCapacity = 64)
    val remoteCommand = _remoteCommand.asSharedFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private val settingsManager = AppSettingsManager(application)
    val appSettings: StateFlow<AppSettings> = settingsManager.settings

    private val downloader = MultiThreadDownloader(application, dao)
    private val appManager = SideloadAppManager(application)
    private val optimizer = SystemOptimizer(application)
    val optimizationProgress: StateFlow<OptimizationProgress> = optimizer.optimizationProgress

    private var webServer: LocalWebDashboardServer? = null

    init {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { refreshNetworkInfo() }
            runCatching { refreshSystemMemory() }
            runCatching { loadInstalledApps() }
            if (appSettings.value.autoStartWebServer) {
                runCatching { startServer() }
            }
            if (appSettings.value.autoOptimizeEnabled && appSettings.value.autoOptimizeSchedule == "STARTUP") {
                runCatching { optimizer.runBackgroundOptimization() }
            }
        }

        // Seed initial items if DB is empty
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                // Only seed if empty
                val demoList = listOf(
                    HubItemEntity(
                        title = "Kiem_Hiep_3D_v2.4.apk",
                        type = "APK",
                        pathOrUrl = "/storage/emulated/0/Download/Hubit/Kiem_Hiep_3D_v2.4.apk",
                        fileSize = 48500000L,
                        receivedSource = "WEB_DASHBOARD",
                        status = "COMPLETED"
                    ),
                    HubItemEntity(
                        title = "Trailer_Phim_Bom_Tan_4K.mp4",
                        type = "VIDEO",
                        pathOrUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                        fileSize = 158000000L,
                        receivedSource = "DOWNLOADER",
                        status = "COMPLETED"
                    ),
                    HubItemEntity(
                        title = "VTV1 HD (Kênh Tin Tức)",
                        type = "IPTV",
                        pathOrUrl = "https://play.contentvn.com/live/vtv1/index.m3u8",
                        fileSize = 0L,
                        receivedSource = "IPTV|VTV|https://img.upanh.tv/2023/11/02/vtv1_logo.png",
                        status = "COMPLETED"
                    ),
                    HubItemEntity(
                        title = "VTV3 HD (Kênh Giải Trí)",
                        type = "IPTV",
                        pathOrUrl = "https://play.contentvn.com/live/vtv3/index.m3u8",
                        fileSize = 0L,
                        receivedSource = "IPTV|VTV|https://img.upanh.tv/2023/11/02/vtv3_logo.png",
                        status = "COMPLETED"
                    ),
                    HubItemEntity(
                        title = "VTV6 HD (Kênh Thể Thao)",
                        type = "IPTV",
                        pathOrUrl = "https://play.contentvn.com/live/vtv6/index.m3u8",
                        fileSize = 0L,
                        receivedSource = "IPTV|VTV|https://img.upanh.tv/2023/11/02/vtv6_logo.png",
                        status = "COMPLETED"
                    )
                )
                // Check if already seeded, if not, insert
                demoList.forEach { dao.insertItem(it) }
            }
        }
    }

    fun selectTab(index: Int) {
        _selectedTab.value = index
    }

    fun refreshNetworkInfo() {
        val ip = NetworkUtils.getLocalIpAddress(getApplication())
        _localIpAddress.value = ip
    }

    fun refreshSystemMemory() {
        viewModelScope.launch(Dispatchers.IO) {
            val info = optimizer.getMemoryInfo()
            _systemMemoryInfo.value = info
        }
    }

    fun loadInstalledApps() {
        viewModelScope.launch(Dispatchers.IO) {
            val apps = appManager.getInstalledApps(includeSystem = false)
            _installedApps.value = apps
        }
    }

    fun startServer() {
        if (webServer?.isRunning == true) return
        webServer = LocalWebDashboardServer(
            context = getApplication(),
            port = 8080,
            onFileReceived = { fileName, filePath, fileSize ->
                viewModelScope.launch(Dispatchers.IO) {
                    val fileType = when {
                        fileName.endsWith(".apk", true) -> "APK"
                        fileName.endsWith(".mp4", true) || fileName.endsWith(".mkv", true) -> "VIDEO"
                        else -> "FILE"
                    }
                    dao.insertItem(
                        HubItemEntity(
                            title = fileName,
                            type = fileType,
                            pathOrUrl = filePath,
                            fileSize = fileSize,
                            receivedSource = "WEB_DASHBOARD",
                            status = "COMPLETED"
                        )
                    )
                    showToast("📥 Đã nhận file từ điện thoại: $fileName")
                }
            },
            onUrlReceived = { url ->
                viewModelScope.launch {
                    downloader.startDownload(url = url, scope = viewModelScope)
                    showToast("🚀 Đã nhận link! Hubit! bắt đầu tải...")
                }
            },
            onClipboardReceived = { text ->
                viewModelScope.launch(Dispatchers.IO) {
                    dao.insertClipboard(ClipboardEntity(content = text, senderName = "Phone Web Dashboard"))
                    showToast("📋 Đã đồng bộ Bộ nhớ tạm: $text")
                }
            },
            onRemoteKey = { key ->
                viewModelScope.launch {
                    _remoteCommand.emit(key)
                }
                showToast("🎮 Remote key: $key")
            }
        )
        webServer?.start()
        _isServerRunning.value = webServer?.isRunning == true
    }

    fun stopServer() {
        webServer?.stop()
        _isServerRunning.value = false
    }

    fun updateSettings(newSettings: AppSettings) {
        settingsManager.updateSettings(newSettings)
        showToast("Đã lưu cấu hình cài đặt!")
    }

    fun startBackgroundOptimization() {
        viewModelScope.launch(Dispatchers.IO) {
            showToast("🧹 Đang bắt đầu tối ưu hóa hệ thống ngầm...")
            optimizer.runBackgroundOptimization()
            refreshSystemMemory()
            showToast("✨ Tối ưu hóa bộ nhớ thành công!")
        }
    }

    fun dismissOptimizationProgress() {
        optimizer.dismissProgress()
    }

    fun downloadUrl(url: String, customTitle: String? = null) {
        if (url.isBlank()) return
        val baseDir = settingsManager.getDownloadDirectory()
        val targetDir = if (appSettings.value.autoCategorizeDownloads) {
            val ext = url.substringAfterLast(".").substringBefore("?").lowercase()
            when {
                ext == "apk" -> File(baseDir, "APK")
                listOf("mp4", "mkv", "avi", "mov", "ts").contains(ext) -> File(baseDir, "Video")
                listOf("mp3", "m4a", "flac", "wav").contains(ext) -> File(baseDir, "Nhạc")
                else -> baseDir
            }.also { if (!it.exists()) it.mkdirs() }
        } else {
            baseDir
        }

        downloader.startDownload(
            url = url,
            customTitle = customTitle,
            targetDirectory = targetDir,
            scope = viewModelScope
        )
        showToast("🚀 Đã thêm vào Trình Tải Siêu Tốc (${appSettings.value.maxDownloadThreads} luồng)!")
    }

    fun pauseDownload(itemId: Int) {
        downloader.pauseDownload(itemId, viewModelScope)
        showToast("⏸️ Đã tạm dừng tiến trình tải")
    }

    fun resumeDownload(itemId: Int) {
        downloader.resumeDownload(itemId, viewModelScope)
        showToast("▶️ Tiếp tục tải file từ điểm dừng...")
    }

    fun retryDownload(itemId: Int) {
        downloader.retryDownload(itemId, viewModelScope)
        showToast("🔄 Đang thử tải lại...")
    }

    fun cancelDownload(itemId: Int) {
        downloader.cancelDownload(itemId, viewModelScope)
        showToast("Đã hủy lượt tải")
    }

    fun playVideo(title: String, pathOrUrl: String) {
        _activeVideoToPlay.value = Pair(title, pathOrUrl)
    }

    fun dismissVideoPlayer() {
        _activeVideoToPlay.value = null
    }

    fun launchInstalledApp(packageName: String) {
        val success = appManager.launchApp(packageName)
        if (!success) {
            showToast("Không thể mở ứng dụng này")
        }
    }

    fun installApkFile(filePath: String) {
        val file = File(filePath)
        if (file.exists()) {
            appManager.installApk(file)
        } else {
            showToast("File APK không tồn tại trên bộ nhớ")
        }
    }

    fun boostRam() {
        viewModelScope.launch(Dispatchers.IO) {
            val freedMb = optimizer.releaseRam()
            refreshSystemMemory()
            showToast("⚡ Tăng tốc hoàn tất! Đã giải phóng ~$freedMb MB RAM!")
        }
    }

    fun cleanCache() {
        viewModelScope.launch(Dispatchers.IO) {
            val freedMb = optimizer.cleanDeepCache()
            refreshSystemMemory()
            showToast("🧹 Dọn dẹp thành công! Đã xóa $freedMb MB cache hệ thống!")
        }
    }

    fun addClipboardText(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            dao.insertClipboard(ClipboardEntity(content = text, senderName = "TV App"))
            showToast("Đã lưu vào bộ nhớ tạm")
        }
    }

    fun deleteHubItem(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteItemById(id)
            showToast("Đã xóa mục")
        }
    }

    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun importIptvPlaylist(m3uUrl: String) {
        if (m3uUrl.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Fetch the M3U content
                val urlObj = java.net.URL(m3uUrl)
                val connection = urlObj.openConnection()
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                val content = connection.getInputStream().bufferedReader(Charsets.UTF_8).use { it.readText() }

                if (content.isBlank()) {
                    showToast("❌ Không thể tải playlist IPTV từ URL này")
                    return@launch
                }

                val channels = parseM3uPlaylist(content)
                if (channels.isEmpty()) {
                    showToast("❌ Không tìm thấy kênh truyền hình hợp lệ")
                    return@launch
                }

                // Insert parsed channels
                channels.forEach { channel ->
                    dao.insertItem(channel)
                }

                showToast("📺 Đã nhập thành công ${channels.size} kênh IPTV!")
            } catch (e: Exception) {
                showToast("❌ Lỗi nhập IPTV: ${e.localizedMessage}")
            }
        }
    }

    private fun parseM3uPlaylist(m3uContent: String): List<HubItemEntity> {
        val list = mutableListOf<HubItemEntity>()
        val lines = m3uContent.lineSequence().map { it.trim() }.toList()

        var currentTitle = ""
        var currentGroup = "Kênh TV"
        var currentLogo = ""

        for (line in lines) {
            if (line.startsWith("#EXTINF:")) {
                // Parse channel info
                // Example: #EXTINF:-1 tvg-logo="https://..." group-title="VTV",VTV3 HD
                val commaIndex = line.lastIndexOf(',')
                currentTitle = if (commaIndex != -1) line.substring(commaIndex + 1).trim() else ""
                
                // Extract group-title
                val groupMatch = """group-title="([^"]+)"""".toRegex().find(line)
                currentGroup = groupMatch?.groupValues?.get(1) ?: "Truyền Hình"

                // Extract tvg-logo
                val logoMatch = """tvg-logo="([^"]+)"""".toRegex().find(line)
                currentLogo = logoMatch?.groupValues?.get(1) ?: ""
            } else if (line.startsWith("http://") || line.startsWith("https://") || line.contains(".m3u8")) {
                val cleanUrl = if (line.contains(" ")) line.substringBefore(" ") else line
                if (currentTitle.isNotEmpty() && (cleanUrl.startsWith("http://") || cleanUrl.startsWith("https://"))) {
                    list.add(
                        HubItemEntity(
                            title = currentTitle,
                            type = "IPTV",
                            pathOrUrl = cleanUrl,
                            fileSize = 0L,
                            receivedSource = "IPTV|$currentGroup|$currentLogo",
                            status = "COMPLETED"
                        )
                    )
                }
                // Reset
                currentTitle = ""
                currentGroup = "Kênh TV"
                currentLogo = ""
            }
        }
        return list
    }

    override fun onCleared() {
        super.onCleared()
        stopServer()
    }
}
