package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey val id: String,
    val mediaId: String,
    val episodeId: String? = null,
    val title: String,
    val episodeTitle: String? = null,
    val posterUrl: String,
    val fileSizeBytes: Long,
    val downloadProgress: Float = 1.0f,
    val downloadStatus: String = "COMPLETED", // QUEUED, DOWNLOADING, COMPLETED
    val downloadedAt: Long = System.currentTimeMillis(),
    val streamUrl: String
)
