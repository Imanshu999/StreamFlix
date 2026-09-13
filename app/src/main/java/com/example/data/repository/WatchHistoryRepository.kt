package com.example.data.repository

import com.example.data.local.WatchHistoryDao
import com.example.data.local.WatchHistoryEntity
import kotlinx.coroutines.flow.Flow

class WatchHistoryRepository(private val watchHistoryDao: WatchHistoryDao) {

    val allHistory: Flow<List<WatchHistoryEntity>> = watchHistoryDao.getAllWatchHistory()

    suspend fun savePlaybackPosition(
        mediaId: String,
        episodeId: String? = null,
        title: String,
        episodeTitle: String? = null,
        posterUrl: String,
        currentPositionMs: Long,
        durationMs: Long
    ) {
        val id = if (episodeId != null) "${mediaId}_$episodeId" else mediaId
        val progress = if (durationMs > 0) (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
        val isCompleted = progress >= 0.92f

        val entity = WatchHistoryEntity(
            id = id,
            mediaId = mediaId,
            episodeId = episodeId,
            title = title,
            episodeTitle = episodeTitle,
            posterUrl = posterUrl,
            currentPositionMs = currentPositionMs,
            durationMs = durationMs,
            progressPercentage = progress,
            lastWatchedTimestamp = System.currentTimeMillis(),
            isCompleted = isCompleted
        )
        watchHistoryDao.upsert(entity)
    }

    suspend fun getByMediaId(mediaId: String): WatchHistoryEntity? {
        return watchHistoryDao.getByMediaId(mediaId)
    }

    suspend fun removeHistoryItem(id: String) {
        watchHistoryDao.deleteById(id)
    }

    suspend fun clearHistory() {
        watchHistoryDao.clearAll()
    }
}
