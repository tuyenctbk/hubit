package com.example.bridge

import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.net.URLDecoder
import java.util.concurrent.Executors

class LocalWebDashboardServer(
    private val context: Context,
    private val port: Int = 8080,
    private val onFileReceived: (fileName: String, filePath: String, fileSize: Long) -> Unit,
    private val onUrlReceived: (url: String) -> Unit,
    private val onClipboardReceived: (text: String) -> Unit,
    private val onRemoteKey: (key: String) -> Unit
) {
    private var serverSocket: ServerSocket? = null
    private var executor = Executors.newFixedThreadPool(8)

    @Volatile
    var isRunning = false
        private set

    fun start() {
        if (isRunning) return
        Thread {
            try {
                serverSocket = ServerSocket(port)
                isRunning = true
                Log.d("HubitServer", "Server started on port $port")

                while (isRunning && serverSocket?.isClosed == false) {
                    try {
                        val clientSocket = serverSocket?.accept() ?: break
                        executor.submit { handleClient(clientSocket) }
                    } catch (e: Exception) {
                        if (isRunning) {
                            Log.e("HubitServer", "Error accepting client connection", e)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("HubitServer", "Error starting server socket on port $port", e)
                isRunning = false
            }
        }.start()
    }

    fun stop() {
        isRunning = false
        try {
            serverSocket?.close()
            serverSocket = null
            executor.shutdownNow()
            executor = Executors.newFixedThreadPool(8)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun handleClient(socket: Socket) {
        try {
            socket.use { s ->
                val input = BufferedInputStream(s.getInputStream())
                val output = s.getOutputStream()

                // Read HTTP Header bytes up to \r\n\r\n
                val headerBytes = readHeaderBytes(input) ?: return
                val headerText = String(headerBytes, Charsets.UTF_8)
                val lines = headerText.split("\r\n")
                if (lines.isEmpty()) return

                val firstLine = lines[0]
                val parts = firstLine.split(" ")
                if (parts.size < 2) return

                val method = parts[0].uppercase()
                val path = parts[1]

                // Parse headers
                val headers = mutableMapOf<String, String>()
                for (i in 1 until lines.size) {
                    val line = lines[i]
                    if (line.isEmpty()) continue
                    val colonIdx = line.indexOf(":")
                    if (colonIdx > 0) {
                        val name = line.substring(0, colonIdx).trim().lowercase()
                        val value = line.substring(colonIdx + 1).trim()
                        headers[name] = value
                    }
                }

                if (method == "OPTIONS") {
                    sendHeaders(output, 200, "text/plain", 0)
                    return
                }

                when {
                    method == "GET" && (path == "/" || path.startsWith("/index")) -> {
                        val html = getWebDashboardHtml()
                        val bytes = html.toByteArray(Charsets.UTF_8)
                        sendHeaders(output, 200, "text/html; charset=utf-8", bytes.size.toLong())
                        output.write(bytes)
                        output.flush()
                    }
                    method == "POST" && path == "/api/upload" -> {
                        handleUpload(headers, input, output)
                    }
                    method == "POST" && path == "/api/url" -> {
                        val body = readBodyString(headers, input)
                        val url = parsePayloadValue(body, "url")
                        if (url.isNotEmpty()) {
                            onUrlReceived(url)
                            sendJsonResponse(output, 200, """{"status":"success","url":"$url"}""")
                        } else {
                            sendJsonResponse(output, 400, """{"status":"error","message":"Empty URL"}""")
                        }
                    }
                    method == "POST" && path == "/api/clipboard" -> {
                        val body = readBodyString(headers, input)
                        val text = parsePayloadValue(body, "text")
                        if (text.isNotEmpty()) {
                            onClipboardReceived(text)
                            sendJsonResponse(output, 200, """{"status":"success","text":"$text"}""")
                        } else {
                            sendJsonResponse(output, 400, """{"status":"error","message":"Empty text"}""")
                        }
                    }
                    method == "POST" && path == "/api/remote" -> {
                        val body = readBodyString(headers, input)
                        val key = parsePayloadValue(body, "key")
                        if (key.isNotEmpty()) {
                            onRemoteKey(key)
                            sendJsonResponse(output, 200, """{"status":"success","key":"$key"}""")
                        } else {
                            sendJsonResponse(output, 400, """{"status":"error","message":"Empty key"}""")
                        }
                    }
                    else -> {
                        sendJsonResponse(output, 404, """{"status":"error","message":"Not found"}""")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("HubitServer", "Error handling client request", e)
        }
    }

    private fun readHeaderBytes(input: InputStream): ByteArray? {
        val baos = ByteArrayOutputStream()
        var matchCount = 0
        val maxHeaderSize = 65536 // 64 KB safety limit
        while (baos.size() < maxHeaderSize) {
            val b = input.read()
            if (b == -1) {
                if (baos.size() == 0) return null
                break
            }
            baos.write(b)
            if ((matchCount == 0 || matchCount == 2) && b == '\r'.code) {
                matchCount++
            } else if ((matchCount == 1 || matchCount == 3) && b == '\n'.code) {
                matchCount++
                if (matchCount == 4) break
            } else {
                matchCount = if (b == '\r'.code) 1 else 0
            }
        }
        return baos.toByteArray()
    }

    private fun handleUpload(headers: Map<String, String>, input: InputStream, output: OutputStream) {
        try {
            val targetDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "Hubit")
            if (!targetDir.exists()) targetDir.mkdirs()

            val filenameHeader = headers["x-file-name"]
            val fileName = if (!filenameHeader.isNullOrEmpty()) {
                try {
                    URLDecoder.decode(filenameHeader, "UTF-8")
                } catch (e: Exception) {
                    filenameHeader
                }
            } else {
                "Hubit_Received_${System.currentTimeMillis()}.bin"
            }

            // Sanitize file name
            val safeFileName = fileName.replace(Regex("[\\\\/:*?\"<>|]"), "_")
            val outputFile = File(targetDir, safeFileName)
            val contentLength = headers["content-length"]?.toLongOrNull() ?: -1L
            var bytesReadTotal = 0L

            FileOutputStream(outputFile).use { fos ->
                val buffer = ByteArray(32768)
                var read: Int
                if (contentLength > 0) {
                    var remaining = contentLength
                    while (remaining > 0) {
                        val toRead = if (remaining < buffer.size) remaining.toInt() else buffer.size
                        read = input.read(buffer, 0, toRead)
                        if (read == -1) break
                        fos.write(buffer, 0, read)
                        bytesReadTotal += read
                        remaining -= read
                    }
                } else {
                    while (input.read(buffer).also { read = it } != -1) {
                        fos.write(buffer, 0, read)
                        bytesReadTotal += read
                    }
                }
                fos.flush()
            }

            onFileReceived(safeFileName, outputFile.absolutePath, bytesReadTotal)
            sendJsonResponse(output, 200, """{"status":"success","message":"File uploaded successfully","file":"$safeFileName"}""")
        } catch (e: Exception) {
            Log.e("HubitServer", "Upload error", e)
            sendJsonResponse(output, 500, """{"status":"error","message":"${e.localizedMessage}"}""")
        }
    }

    private fun readBodyString(headers: Map<String, String>, input: InputStream): String {
        val contentLength = headers["content-length"]?.toIntOrNull() ?: 0
        if (contentLength <= 0) return ""
        val bytes = ByteArray(contentLength)
        var totalRead = 0
        while (totalRead < contentLength) {
            val read = input.read(bytes, totalRead, contentLength - totalRead)
            if (read == -1) break
            totalRead += read
        }
        return String(bytes, 0, totalRead, Charsets.UTF_8)
    }

    private fun parsePayloadValue(body: String, key: String): String {
        val trimmed = body.trim()
        // Check JSON format: {"key": "value"}
        val jsonPattern = Regex("\"$key\"\\s*:\\s*\"([^\"]*)\"")
        val match = jsonPattern.find(trimmed)
        if (match != null) {
            return match.groupValues[1]
        }
        // Check URL-encoded format: key=value
        if (trimmed.contains("$key=")) {
            val parts = trimmed.split("&")
            for (p in parts) {
                if (p.startsWith("$key=")) {
                    return try {
                        URLDecoder.decode(p.substring(key.length + 1), "UTF-8")
                    } catch (e: Exception) {
                        p.substring(key.length + 1)
                    }
                }
            }
        }
        return if (!trimmed.startsWith("{") && !trimmed.contains("=")) trimmed else ""
    }

    private fun sendHeaders(output: OutputStream, status: Int, contentType: String, contentLength: Long) {
        val response = StringBuilder()
            .append("HTTP/1.1 $status OK\r\n")
            .append("Content-Type: $contentType\r\n")
            .append("Content-Length: $contentLength\r\n")
            .append("Access-Control-Allow-Origin: *\r\n")
            .append("Access-Control-Allow-Headers: *\r\n")
            .append("Access-Control-Allow-Methods: GET, POST, OPTIONS\r\n")
            .append("Connection: close\r\n")
            .append("\r\n")
        output.write(response.toString().toByteArray(Charsets.UTF_8))
    }

    private fun sendJsonResponse(output: OutputStream, code: Int, json: String) {
        val bytes = json.toByteArray(Charsets.UTF_8)
        sendHeaders(output, code, "application/json; charset=utf-8", bytes.size.toLong())
        output.write(bytes)
        output.flush()
    }

    private fun getWebDashboardHtml(): String {
        return """<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <title>Hubit! – TV Web Dashboard</title>
    <style>
        * { box-sizing: border-box; margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; }
        body { background: #0b0f19; color: #f8fafc; padding: 16px; min-height: 100vh; }
        .header { text-align: center; margin-bottom: 24px; padding-top: 10px; }
        .header h1 { font-size: 28px; color: #00e5ff; font-weight: 800; letter-spacing: -0.5px; }
        .header p { color: #94a3b8; font-size: 14px; margin-top: 4px; }
        .card { background: #151d2a; border: 1px solid #1e293b; border-radius: 16px; padding: 20px; margin-bottom: 20px; box-shadow: 0 10px 25px rgba(0,0,0,0.3); }
        .card-title { font-size: 16px; font-weight: 700; color: #00e5ff; margin-bottom: 12px; display: flex; align-items: center; gap: 8px; }
        .drop-zone { border: 2px dashed #00e5ff; border-radius: 12px; padding: 30px 16px; text-align: center; background: rgba(0, 229, 255, 0.03); cursor: pointer; transition: 0.2s; }
        .drop-zone:hover, .drop-zone.active { background: rgba(0, 229, 255, 0.1); border-color: #38bdf8; }
        .drop-icon { font-size: 40px; margin-bottom: 8px; }
        .drop-text { color: #cbd5e1; font-size: 14px; font-weight: 600; }
        .drop-subtext { color: #64748b; font-size: 12px; margin-top: 4px; }
        input[type="file"] { display: none; }
        .input-group { display: flex; gap: 8px; margin-top: 8px; }
        input[type="text"] { flex: 1; background: #0b0f19; border: 1px solid #334155; border-radius: 10px; padding: 12px 14px; color: #fff; font-size: 14px; outline: none; }
        input[type="text"]:focus { border-color: #00e5ff; }
        button { background: linear-gradient(135deg, #00e5ff, #0284c7); color: #000; border: none; border-radius: 10px; padding: 12px 20px; font-weight: 700; cursor: pointer; transition: 0.15s; }
        button:active { transform: scale(0.96); }
        .remote-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px; max-width: 280px; margin: 0 auto; }
        .btn-remote { background: #1e293b; color: #f8fafc; border: 1px solid #334155; height: 54px; border-radius: 12px; font-size: 18px; font-weight: bold; display: flex; align-items: center; justify-content: center; }
        .btn-remote:active { background: #00e5ff; color: #000; }
        .status-toast { position: fixed; bottom: 20px; left: 50%; transform: translateX(-50%); background: #10b981; color: #fff; padding: 10px 20px; border-radius: 20px; font-weight: 600; font-size: 14px; display: none; box-shadow: 0 4px 15px rgba(0,0,0,0.5); z-index: 100; }
        .progress-bar-container { display: none; margin-top: 12px; background: #1e293b; border-radius: 8px; overflow: hidden; height: 8px; }
        .progress-bar-fill { background: #00e5ff; height: 100%; width: 0%; transition: width 0.2s; }
    </style>
</head>
<body>
    <div class="header">
        <h1>Hubit! TV Bridge</h1>
        <p>Kết nối không rào cản – Tải file, Gửi link & Điều khiển TV</p>
    </div>

    <!-- Drop Zone File Upload -->
    <div class="card">
        <div class="card-title">📁 Kéo thả & Gửi file lên TV</div>
        <div class="drop-zone" id="dropZone" onclick="document.getElementById('fileInput').click()">
            <div class="drop-icon">📤</div>
            <div class="drop-text">Chạm để chọn file hoặc kéo thả vào đây</div>
            <div class="drop-subtext">Hỗ trợ file APK, Video, Phim 4K, Nhạc, Nhận tức thì</div>
        </div>
        <input type="file" id="fileInput" onchange="uploadSelectedFile(this.files[0])">
        <div class="progress-bar-container" id="uploadProgressContainer">
            <div class="progress-bar-fill" id="uploadProgressBar"></div>
        </div>
    </div>

    <!-- Link Downloader -->
    <div class="card">
        <div class="card-title">🚀 Gửi Link cho TV tự tải về</div>
        <div class="input-group">
            <input type="text" id="urlInput" placeholder="Dán link http(s):// file hoặc mp4, apk...">
            <button onclick="sendUrl()">Tải về</button>
        </div>
    </div>

    <!-- Universal Clipboard -->
    <div class="card">
        <div class="card-title">📋 Gửi văn bản vào Bộ nhớ tạm TV</div>
        <div class="input-group">
            <input type="text" id="clipboardInput" placeholder="Nhập mật khẩu, link, văn bản...">
            <button onclick="sendClipboard()">Gửi ngay</button>
        </div>
    </div>

    <!-- Remote Control -->
    <div class="card">
        <div class="card-title">🎮 Remote điều khiển TV từ xa</div>
        <div class="remote-grid">
            <div></div>
            <button class="btn-remote" onclick="sendRemote('UP')">▲</button>
            <div></div>
            <button class="btn-remote" onclick="sendRemote('LEFT')">◀</button>
            <button class="btn-remote" style="background:#00e5ff; color:#000;" onclick="sendRemote('ENTER')">OK</button>
            <button class="btn-remote" onclick="sendRemote('RIGHT')">▶</button>
            <div></div>
            <button class="btn-remote" onclick="sendRemote('DOWN')">▼</button>
            <div></div>
            <button class="btn-remote" onclick="sendRemote('BACK')">↩ Back</button>
            <button class="btn-remote" onclick="sendRemote('HOME')">🏠 Home</button>
            <button class="btn-remote" onclick="sendRemote('VOL_UP')">🔊 Vol+</button>
        </div>
    </div>

    <div class="status-toast" id="toast"></div>

    <script>
        function showToast(msg, isError = false) {
            const t = document.getElementById('toast');
            t.innerText = msg;
            t.style.background = isError ? '#f43f5e' : '#10b981';
            t.style.display = 'block';
            setTimeout(() => { t.style.display = 'none'; }, 3000);
        }

        function uploadSelectedFile(file) {
            if (!file) return;
            const progressContainer = document.getElementById('uploadProgressContainer');
            const progressBar = document.getElementById('uploadProgressBar');
            progressContainer.style.display = 'block';
            progressBar.style.width = '0%';
            showToast('Đang gửi: ' + file.name);

            const xhr = new XMLHttpRequest();
            xhr.open('POST', '/api/upload', true);
            xhr.setRequestHeader('X-File-Name', encodeURIComponent(file.name));
            xhr.setRequestHeader('Content-Type', 'application/octet-stream');

            xhr.upload.onprogress = function(e) {
                if (e.lengthComputable) {
                    const percent = Math.round((e.loaded / e.total) * 100);
                    progressBar.style.width = percent + '%';
                }
            };

            xhr.onload = function() {
                progressContainer.style.display = 'none';
                if (xhr.status === 200) {
                    showToast('✅ Đã gửi xong: ' + file.name);
                } else {
                    showToast('❌ Lỗi gửi file', true);
                }
                document.getElementById('fileInput').value = '';
            };

            xhr.onerror = function() {
                progressContainer.style.display = 'none';
                showToast('❌ Mất kết nối tới TV', true);
                document.getElementById('fileInput').value = '';
            };

            xhr.send(file);
        }

        function sendUrl() {
            const input = document.getElementById('urlInput');
            const val = input.value.trim();
            if (!val) return;
            showToast('Đang gửi link tải tới TV...');
            fetch('/api/url', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ url: val })
            })
            .then(res => res.json())
            .then(data => {
                showToast('✅ TV đã nhận lệnh tải!');
                input.value = '';
            })
            .catch(() => showToast('❌ Gửi link thất bại', true));
        }

        function sendClipboard() {
            const input = document.getElementById('clipboardInput');
            const val = input.value.trim();
            if (!val) return;
            showToast('Đang đồng bộ Clipboard...');
            fetch('/api/clipboard', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ text: val })
            })
            .then(res => res.json())
            .then(data => {
                showToast('📋 Đã dán vào TV!');
                input.value = '';
            })
            .catch(() => showToast('❌ Gửi Clipboard thất bại', true));
        }

        function sendRemote(key) {
            fetch('/api/remote', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ key: key })
            }).catch(() => {});
        }

        // Drag and drop handlers
        const dropZone = document.getElementById('dropZone');
        ['dragenter', 'dragover'].forEach(name => {
            dropZone.addEventListener(name, (e) => {
                e.preventDefault();
                dropZone.classList.add('active');
            }, false);
        });
        ['dragleave', 'drop'].forEach(name => {
            dropZone.addEventListener(name, (e) => {
                e.preventDefault();
                dropZone.classList.remove('active');
            }, false);
        });
        dropZone.addEventListener('drop', (e) => {
            const dt = e.dataTransfer;
            const files = dt.files;
            if (files.length > 0) {
                uploadSelectedFile(files[0]);
            }
        });
    </script>
</body>
</html>"""
    }
}
