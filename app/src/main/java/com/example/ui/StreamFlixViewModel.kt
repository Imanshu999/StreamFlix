package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.DownloadEntity
import com.example.data.local.StreamFlixDatabase
import com.example.data.local.WatchHistoryEntity
import com.example.data.model.EpisodeData
import com.example.data.model.MediaItem
import com.example.data.model.MediaType
import com.example.data.model.SeasonData
import com.example.data.model.UserDeviceTelemetry
import com.example.data.model.UserProfile
import com.example.data.repository.AuthRepository
import com.example.data.repository.DownloadRepository
import com.example.data.repository.MediaRepository
import com.example.data.repository.TelemetrySecurityRepository
import com.example.data.repository.WatchHistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

sealed class ScreenDestination {
    object Splash : ScreenDestination()
    object Home : ScreenDestination()
    data class MediaDetail(val mediaId: String) : ScreenDestination()
    data class VideoPlayer(val mediaId: String, val episodeId: String? = null) : ScreenDestination()
    object History : ScreenDestination()
    object Downloads : ScreenDestination()
    object Profile : ScreenDestination()
    object AdminPortal : ScreenDestination()
}

class StreamFlixViewModel(application: Application) : AndroidViewModel(application) {

    private val db = StreamFlixDatabase.getDatabase(application)
    val mediaRepository = MediaRepository()
    val telemetrySecurityRepository = TelemetrySecurityRepository(application)
    val watchHistoryRepository = WatchHistoryRepository(db.watchHistoryDao())
    val downloadRepository = DownloadRepository(db.downloadDao())
    val reviewRepository = com.example.data.repository.ReviewRepository(db.reviewDao())
    val authRepository = AuthRepository()

    // Offline Mode Simulation
    val isOfflineModeActive = MutableStateFlow(false)

    // Navigation & Screen State
    private val _currentScreen = MutableStateFlow<ScreenDestination>(ScreenDestination.Splash)
    val currentScreen: StateFlow<ScreenDestination> = _currentScreen.asStateFlow()

    // Filter & Search
    val searchQuery = MutableStateFlow("")
    val selectedCategoryFilter = MutableStateFlow("All")

    // State flows from repositories
    val mediaItems: StateFlow<List<MediaItem>> = mediaRepository.mediaItems

    val featuredMedia: StateFlow<MediaItem?> = mediaRepository.mediaItems
        .combine(searchQuery) { items, _ ->
            items.firstOrNull { it.isFeatured } ?: items.firstOrNull()
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val watchHistory: StateFlow<List<WatchHistoryEntity>> = watchHistoryRepository.allHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val downloads: StateFlow<List<DownloadEntity>> = downloadRepository.allDownloads
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allReviews: StateFlow<List<com.example.data.local.ReviewEntity>> = reviewRepository.allReviews
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            reviewRepository.seedDefaultReviewsIfEmpty()
        }
    }

    val currentUser: StateFlow<UserProfile> = authRepository.currentUser
    val isClientBanned: StateFlow<Boolean> = telemetrySecurityRepository.isCurrentClientBanned
    val activeDevices: StateFlow<List<UserDeviceTelemetry>> = telemetrySecurityRepository.activeDevices

    // Admin State
    private val _isAdminAuthenticated = MutableStateFlow(false)
    val isAdminAuthenticated: StateFlow<Boolean> = _isAdminAuthenticated.asStateFlow()

    private val _adminNotification = MutableStateFlow<String?>(null)
    val adminNotification: StateFlow<String?> = _adminNotification.asStateFlow()

    // Bookmark / My List set
    val myFavoriteIds = MutableStateFlow<Set<String>>(setOf("stranger_chronicles", "squid_gambit"))

    fun navigateTo(screen: ScreenDestination) {
        _currentScreen.value = screen
    }

    fun toggleMyList(mediaId: String) {
        val current = myFavoriteIds.value.toMutableSet()
        if (current.contains(mediaId)) {
            current.remove(mediaId)
        } else {
            current.add(mediaId)
        }
        myFavoriteIds.value = current
    }

    fun isFavorite(mediaId: String): Boolean {
        return myFavoriteIds.value.contains(mediaId)
    }

    // Playback Tracking
    fun updatePlaybackProgress(
        mediaId: String,
        episodeId: String?,
        title: String,
        episodeTitle: String?,
        posterUrl: String,
        currentPosMs: Long,
        durationMs: Long
    ) {
        viewModelScope.launch {
            watchHistoryRepository.savePlaybackPosition(
                mediaId = mediaId,
                episodeId = episodeId,
                title = title,
                episodeTitle = episodeTitle,
                posterUrl = posterUrl,
                currentPositionMs = currentPosMs,
                durationMs = durationMs
            )
        }
    }

    fun clearWatchHistory() {
        viewModelScope.launch {
            watchHistoryRepository.clearHistory()
        }
    }

    fun removeHistoryItem(id: String) {
        viewModelScope.launch {
            watchHistoryRepository.removeHistoryItem(id)
        }
    }

    // Downloads
    fun downloadMedia(media: MediaItem, episode: EpisodeData? = null) {
        viewModelScope.launch {
            if (episode != null) {
                downloadRepository.addEpisodeDownload(media, episode)
            } else {
                downloadRepository.addMovieDownload(media)
            }
        }
    }

    fun removeDownload(id: String) {
        viewModelScope.launch {
            downloadRepository.removeDownload(id)
        }
    }

    fun clearAllDownloads() {
        viewModelScope.launch {
            downloadRepository.clearAllDownloads()
        }
    }

    // User Ratings & Reviews
    fun submitReview(mediaId: String, rating: Int, reviewText: String) {
        val user = currentUser.value
        val email = if (user.isGuest) "guest@streamflix.tv" else user.email
        val name = if (user.isGuest) "Guest Reviewer" else user.displayName
        val avatar = user.avatarUrl

        viewModelScope.launch {
            reviewRepository.addReview(
                mediaId = mediaId,
                userEmail = email,
                userName = name,
                userAvatarUrl = avatar,
                rating = rating,
                reviewText = reviewText,
                isVerifiedUser = !user.isGuest
            )
        }
    }

    fun deleteReview(reviewId: String) {
        viewModelScope.launch {
            reviewRepository.deleteReview(reviewId)
        }
    }

    fun toggleOfflineMode(active: Boolean) {
        isOfflineModeActive.value = active
    }

    // Admin Authentication & Actions
    fun authenticateAdmin(pin: String): Boolean {
        if (currentUser.value.isAdmin || pin == "admin123" || pin == "8888") {
            _isAdminAuthenticated.value = true
            _adminNotification.value = "Admin Authenticated Successfully"
            return true
        }
        return false
    }

    fun logoutAdmin() {
        _isAdminAuthenticated.value = false
    }

    fun clearAdminNotification() {
        _adminNotification.value = null
    }

    // CMS Add Item
    fun addMediaItemFromCMS(
        title: String,
        description: String,
        bannerUrl: String,
        posterUrl: String,
        category: String,
        type: MediaType,
        streamUrl: String,
        releaseYear: Int,
        rating: String,
        seasonCount: Int
    ) {
        val id = "cms_" + UUID.randomUUID().toString().take(8)
        val seasons = if (type == MediaType.SERIES) {
            (1..seasonCount.coerceAtLeast(1)).map { sNum ->
                SeasonData(
                    seasonNumber = sNum,
                    title = "Season $sNum",
                    episodes = (1..3).map { epNum ->
                        EpisodeData(
                            id = "${id}_s${sNum}_e${epNum}",
                            episodeNumber = epNum,
                            title = "Episode $epNum: Prelude",
                            overview = "Official episode stream broadcast directly configured via StreamFlix CMS.",
                            thumbnailUrl = bannerUrl.ifEmpty { posterUrl },
                            durationMinutes = 45,
                            streamUrl = streamUrl
                        )
                    }
                )
            }
        } else {
            emptyList()
        }

        val newItem = MediaItem(
            id = id,
            title = title,
            description = description,
            bannerUrl = bannerUrl.ifEmpty { "https://images.unsplash.com/photo-1574375927938-d5a98e8ffe85?w=1200&q=80" },
            posterUrl = posterUrl.ifEmpty { "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=500&q=80" },
            type = type,
            category = category,
            genres = listOf("Trending", "Original"),
            releaseYear = releaseYear,
            rating = rating,
            matchPercentage = 98,
            durationText = if (type == MediaType.SERIES) "$seasonCount Seasons" else "1h 55m",
            directStreamUrl = streamUrl,
            isFeatured = false,
            badgeLabel = "CMS ADDED",
            seasons = seasons
        )

        mediaRepository.addOrUpdateMedia(newItem)
        _adminNotification.value = "Published '$title' to catalog instantly!"
    }

    fun saveMediaItemFromCMS(mediaItem: MediaItem) {
        mediaRepository.addOrUpdateMedia(mediaItem)
        _adminNotification.value = "Saved '${mediaItem.title}' to live catalog!"
    }

    fun setFeaturedMedia(id: String) {
        mediaRepository.setFeaturedMedia(id)
        val title = mediaRepository.getMediaById(id)?.title ?: "Item"
        _adminNotification.value = "⭐ Set '$title' as the Top Featured Hero Title!"
    }

    fun toggleTop10Media(id: String) {
        val current = mediaRepository.getMediaById(id) ?: return
        val willBeTop10 = !current.isTop10
        mediaRepository.toggleTop10(id)
        _adminNotification.value = if (willBeTop10) {
            "🏆 Marked '${current.title}' as TOP 10!"
        } else {
            "Removed '${current.title}' from TOP 10."
        }
    }

    fun isAuthorizedAdmin(): Boolean {
        val user = currentUser.value
        return !user.isGuest && user.email.equals("n4062226@gmail.com", ignoreCase = true)
    }

    fun deleteMediaFromCMS(id: String) {
        mediaRepository.deleteMedia(id)
        _adminNotification.value = "Item removed from catalog"
    }

    fun resetCatalogToDefaults() {
        mediaRepository.resetCatalog()
        _adminNotification.value = "Catalog restored to default Netflix line-up"
    }

    // Security Ban & Unban Actions
    fun banDevice(deviceId: String, reason: String = "Policy violation / Malicious activity") {
        telemetrySecurityRepository.banDevice(deviceId, reason)
        _adminNotification.value = "Device $deviceId is now BANNED"
    }

    fun unbanDevice(deviceId: String) {
        telemetrySecurityRepository.unbanDevice(deviceId)
        _adminNotification.value = "Device $deviceId unbanned"
    }

    fun banIp(ip: String, reason: String = "Suspicious traffic") {
        telemetrySecurityRepository.banIp(ip, reason)
        _adminNotification.value = "IP $ip is now BANNED"
    }

    fun unbanIp(ip: String) {
        telemetrySecurityRepository.unbanIp(ip)
        _adminNotification.value = "IP $ip unbanned"
    }

    fun emergencyUnbanCurrentDevice() {
        telemetrySecurityRepository.emergencyUnbanCurrentDevice()
        _adminNotification.value = "Current Device Restrictions Lifted"
    }
}
