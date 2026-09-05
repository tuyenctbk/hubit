package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HubItemDao {
    @Query("SELECT * FROM hub_items ORDER BY timestamp DESC")
    fun getAllItems(): Flow<List<HubItemEntity>>

    @Query("SELECT * FROM hub_items WHERE type = :type ORDER BY timestamp DESC")
    fun getItemsByType(type: String): Flow<List<HubItemEntity>>

    @Query("SELECT * FROM hub_items WHERE status = 'DOWNLOADING' OR status = 'PAUSED' ORDER BY timestamp DESC")
    fun getActiveDownloads(): Flow<List<HubItemEntity>>

    @Query("SELECT * FROM hub_items ORDER BY timestamp DESC LIMIT 1")
    fun getLatestItem(): Flow<HubItemEntity?>

    @Query("SELECT * FROM hub_items WHERE id = :id LIMIT 1")
    suspend fun getItemById(id: Int): HubItemEntity?

    @Query("SELECT COUNT(*) FROM hub_items")
    suspend fun getItemCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: HubItemEntity): Long

    @Update
    suspend fun updateItem(item: HubItemEntity)

    @Query("DELETE FROM hub_items WHERE id = :id")
    suspend fun deleteItemById(id: Int)

    @Query("DELETE FROM hub_items")
    suspend fun clearAllItems()

    // Clipboard
    @Query("SELECT * FROM clipboard_items ORDER BY timestamp DESC")
    fun getAllClipboardItems(): Flow<List<ClipboardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClipboard(item: ClipboardEntity): Long

    @Query("DELETE FROM clipboard_items")
    suspend fun clearClipboard()

    // Browser History
    @Query("SELECT * FROM browser_history ORDER BY timestamp DESC LIMIT 50")
    fun getBrowserHistory(): Flow<List<BrowserHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(item: BrowserHistoryEntity): Long
}
