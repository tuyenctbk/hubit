package com.example.fetcher

import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import java.io.ByteArrayInputStream

object AdBlocker {
    private val adDomains = setOf(
        "doubleclick.net", "googleadservices.com", "googlesyndication.com",
        "adservice.google.com", "pagead2.googlesyndication.com",
        "popads.net", "popcash.net", "adsterra.com", "exoclick.com",
        "juicyads.com", "adnxs.com", "criteo.com", "adform.net"
    )

    fun isAdUrl(url: String): Boolean {
        val lower = url.lowercase()
        return adDomains.any { lower.contains(it) } || lower.contains("/ads/") || lower.contains("/pop/") || lower.contains("adserver")
    }

    fun createEmptyResponse(): WebResourceResponse {
        return WebResourceResponse("text/plain", "UTF-8", ByteArrayInputStream(ByteArray(0)))
    }

    fun sniffMediaOrApkLink(url: String): String? {
        val lower = url.lowercase()
        return when {
            lower.contains(".m3u8") -> "M3U8 Streaming"
            lower.contains(".mp4") -> "Video MP4"
            lower.contains(".mkv") -> "Video MKV"
            lower.contains(".apk") -> "Cài đặt APK"
            lower.contains(".zip") || lower.contains(".rar") -> "File nén ZIP"
            else -> null
        }
    }
}
