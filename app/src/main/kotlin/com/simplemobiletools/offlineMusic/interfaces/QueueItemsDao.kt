package com.simplemobiletools.offlineMusic.interfaces

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.simplemobiletools.offlineMusic.models.QueueItem

@Dao
interface QueueItemsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(queueItems: List<QueueItem>)

    @Query("SELECT * FROM queue_items ORDER BY track_order")
    fun getAll(): List<QueueItem>

    @Query("UPDATE queue_items SET is_current = 0")
    fun resetCurrent()

    @Query("SELECT * FROM queue_items WHERE is_current = 1")
    fun getCurrent(): QueueItem?

    @Query("UPDATE queue_items SET is_current = 1 WHERE track_id = :trackId")
    fun saveCurrentTrack(trackId: Long)
    
    @Query("UPDATE queue_items SET is_current = 1, last_position = :lastPosition WHERE track_id = :trackId")
    fun saveCurrentTrackProgress(trackId: Long, lastPosition: Int)

    @Query("UPDATE queue_items SET track_order = :order WHERE track_id = :trackId")
    fun setOrder(trackId: Long, order: Int)

    @Query("DELETE FROM queue_items WHERE track_id = :trackId")
    fun removeQueueItem(trackId: Long)

    @Query("DELETE FROM queue_items")
    fun deleteAllItems()
}
