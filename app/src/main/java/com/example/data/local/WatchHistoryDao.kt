package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchHistoryDao {
    @Query("SELECT * FROM watch_history ORDER BY lastWatchedTimestamp DESC")
    fun getAllWatchHistory(): Flow<List<WatchHistoryEntity>>

    @Query("SELECT * FROM watch_history WHERE mediaId = :mediaId LIMIT 1")
    suspend fun getByMediaId(mediaId: String): WatchHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: WatchHistoryEntity)

    @Query("DELETE FROM watch_history WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM watch_history")
    suspend fun clearAll()
}
