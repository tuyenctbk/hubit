package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hub_items")
data class HubItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val type: String, // FILE, LINK, TEXT, APK, VIDEO, AUDIO, IMAGE
    val pathOrUrl: String,
    val fileSize: Long = 0L,
    val receivedSource: String = "WEB_DASHBOARD",
    val status: String = "COMPLETED", // COMPLETED, DOWNLOADING, PAUSED, FAILED
    val progress: Int = 100,
    val downloadSpeed: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "clipboard_items")
data class ClipboardEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val content: String,
    val senderName: String = "Unknown",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "browser_history")
data class BrowserHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val url: String,
    val isBookmark: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
