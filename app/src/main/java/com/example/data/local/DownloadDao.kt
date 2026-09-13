package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {
    @Query("SELECT * FROM downloads ORDER BY downloadedAt DESC")
    fun getAllDownloads(): Flow<List<DownloadEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: DownloadEntity)

    @Query("DELETE FROM downloads WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM downloads")
    suspend fun deleteAll()

    @Query("SELECT EXISTS(SELECT 1 FROM downloads WHERE id = :id)")
    suspend fun isDownloaded(id: String): Boolean
}
