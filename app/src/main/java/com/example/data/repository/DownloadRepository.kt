package com.example.data.repository

import com.example.data.local.DownloadDao
import com.example.data.local.DownloadEntity
import com.example.data.model.EpisodeData
import com.example.data.model.MediaItem
import kotlinx.coroutines.flow.Flow

class DownloadRepository(private val downloadDao: DownloadDao) {

    val allDownloads: Flow<List<DownloadEntity>> = downloadDao.getAllDownloads()

    suspend fun addMovieDownload(media: MediaItem) {
        val entity = DownloadEntity(
            id = media.id,
            mediaId = media.id,
            title = media.title,
            posterUrl = media.posterUrl,
            fileSizeBytes = 1_420_000_000L, // ~1.4 GB
            downloadProgress = 1.0f,
            downloadStatus = "COMPLETED",
            downloadedAt = System.currentTimeMillis(),
            streamUrl = media.directStreamUrl
        )
        downloadDao.insert(entity)
    }

    suspend fun addEpisodeDownload(media: MediaItem, episode: EpisodeData) {
        val entity = DownloadEntity(
            id = "${media.id}_${episode.id}",
            mediaId = media.id,
            episodeId = episode.id,
            title = media.title,
            episodeTitle = "E${episode.episodeNumber}: ${episode.title}",
            posterUrl = episode.thumbnailUrl.ifEmpty { media.posterUrl },
            fileSizeBytes = 450_000_000L, // ~450 MB
            downloadProgress = 1.0f,
            downloadStatus = "COMPLETED",
            downloadedAt = System.currentTimeMillis(),
            streamUrl = episode.streamUrl
        )
        downloadDao.insert(entity)
    }

    suspend fun removeDownload(id: String) {
        downloadDao.deleteById(id)
    }

    suspend fun clearAllDownloads() {
        downloadDao.deleteAll()
    }

    suspend fun isDownloaded(id: String): Boolean {
        return downloadDao.isDownloaded(id)
    }
}
