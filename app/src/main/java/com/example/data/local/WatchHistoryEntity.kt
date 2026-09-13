package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watch_history")
data class WatchHistoryEntity(
    @PrimaryKey val id: String, // mediaId or mediaId_episodeId
    val mediaId: String,
    val episodeId: String? = null,
    val title: String,
    val episodeTitle: String? = null,
    val posterUrl: String,
    val currentPositionMs: Long,
    val durationMs: Long,
    val progressPercentage: Float,
    val lastWatchedTimestamp: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false
)
