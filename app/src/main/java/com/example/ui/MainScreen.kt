package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.EpisodeData
import com.example.data.model.MediaItem
import com.example.data.model.MediaType
import com.example.ui.screens.*
import com.example.ui.theme.*

enum class BottomTab {
    HOME, HISTORY, DOWNLOADS, PROFILE
}

@Composable
fun MainScreen(
    viewModel: StreamFlixViewModel,
    modifier: Modifier = Modifier
) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val isClientBanned by viewModel.isClientBanned.collectAsStateWithLifecycle()

    val mediaItems by viewModel.mediaItems.collectAsStateWithLifecycle()
    val featuredMedia by viewModel.featuredMedia.collectAsStateWithLifecycle()
    val watchHistory by viewModel.watchHistory.collectAsStateWithLifecycle()
    val downloads by viewModel.downloads.collectAsStateWithLifecycle()
    val allReviews by viewModel.allReviews.collectAsStateWithLifecycle()
    val isOfflineModeActive by viewModel.isOfflineModeActive.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val favoriteIds by viewModel.myFavoriteIds.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategoryFilter.collectAsStateWithLifecycle()
    val activeDevices by viewModel.activeDevices.collectAsStateWithLifecycle()
    val adminNotification by viewModel.adminNotification.collectAsStateWithLifecycle()

    var activeBottomTab by remember { mutableStateOf(BottomTab.HOME) }

    // Check ban first: if banned, show lockout screen immediately
    if (isClientBanned) {
        BannedLockoutScreen(
            deviceId = viewModel.telemetrySecurityRepository.deviceId,
            ipAddress = viewModel.telemetrySecurityRepository.clientIp,
            onEmergencyOverride = { pin ->
                if (pin == "admin123" || pin == "8888") {
                    viewModel.emergencyUnbanCurrentDevice()
                    true
                } else false
            }
        )
        return
    }

    // If splash screen
    if (currentScreen is ScreenDestination.Splash) {
        SplashScreen(
            onSplashFinished = {
                viewModel.navigateTo(ScreenDestination.Home)
            }
        )
        return
    }

    // Video Player is Fullscreen (no bottom bar)
    if (currentScreen is ScreenDestination.VideoPlayer) {
        val vp = currentScreen as ScreenDestination.VideoPlayer
        val media = mediaItems.find { it.id == vp.mediaId } ?: mediaItems.firstOrNull()
        if (media != null) {
            val allEpisodes = media.seasons.flatMap { it.episodes }
            val episode = allEpisodes.find { it.id == vp.episodeId } ?: (if (vp.episodeId == null && media.seasons.isNotEmpty()) allEpisodes.firstOrNull() else null)
            val existingHistory = watchHistory.find { it.mediaId == media.id }

            BackHandler {
                viewModel.navigateTo(ScreenDestination.Home)
            }

            VideoPlayerScreen(
                media = media,
                episode = episode,
                initialPositionMs = existingHistory?.currentPositionMs ?: 0L,
                onBackClick = {
                    viewModel.navigateTo(ScreenDestination.Home)
                },
                onProgressUpdate = { currentMs, durationMs ->
                    viewModel.updatePlaybackProgress(
                        mediaId = media.id,
                        episodeId = episode?.id,
                        title = media.title,
                        episodeTitle = episode?.let { "E${it.episodeNumber}: ${it.title}" },
                        posterUrl = episode?.thumbnailUrl?.ifEmpty { media.posterUrl } ?: media.posterUrl,
                        currentPosMs = currentMs,
                        durationMs = durationMs
                    )
                },
                onNextEpisode = if (episode != null) {
                    val currentIndex = allEpisodes.indexOf(episode)
                    if (currentIndex != -1 && currentIndex + 1 < allEpisodes.size) {
                        val nextEp = allEpisodes[currentIndex + 1]
                        { viewModel.navigateTo(ScreenDestination.VideoPlayer(media.id, nextEp.id)) }
                    } else null
                } else null,
                onEpisodeSelect = { selectedEp ->
                    viewModel.navigateTo(ScreenDestination.VideoPlayer(media.id, selectedEp.id))
                }
            )
            return
        }
    }

    // Detail Screen (No bottom bar for immersive preview)
    if (currentScreen is ScreenDestination.MediaDetail) {
        val detail = currentScreen as ScreenDestination.MediaDetail
        val media = mediaItems.find { it.id == detail.mediaId }
        if (media != null) {
            BackHandler {
                viewModel.navigateTo(ScreenDestination.Home)
            }

            MediaDetailScreen(
                media = media,
                allMedia = mediaItems,
                isFavorite = favoriteIds.contains(media.id),
                downloads = downloads,
                reviews = allReviews.filter { it.mediaId == media.id },
                currentUser = currentUser,
                onBackClick = {
                    viewModel.navigateTo(ScreenDestination.Home)
                },
                onPlayMovie = { m ->
                    viewModel.navigateTo(ScreenDestination.VideoPlayer(m.id))
                },
                onPlayEpisode = { m, ep ->
                    viewModel.navigateTo(ScreenDestination.VideoPlayer(m.id, ep.id))
                },
                onDownloadClick = { m, ep ->
                    viewModel.downloadMedia(m, ep)
                },
                onDeleteDownload = { id ->
                    viewModel.removeDownload(id)
                },
                onSubmitReview = { rating, text ->
                    viewModel.submitReview(media.id, rating, text)
                },
                onDeleteReview = { reviewId ->
                    viewModel.deleteReview(reviewId)
                },
                onSignInWithGoogle = {
                    activeBottomTab = BottomTab.PROFILE
                    viewModel.navigateTo(ScreenDestination.Profile)
                },
                onMyListToggle = {
                    viewModel.toggleMyList(media.id)
                },
                onSelectRecommended = { rec ->
                    viewModel.navigateTo(ScreenDestination.MediaDetail(rec.id))
                }
            )
            return
        }
    }

    // Admin Portal Screen
    if (currentScreen is ScreenDestination.AdminPortal) {
        val isStrictAdmin = !currentUser.isGuest && currentUser.email.equals("n4062226@gmail.com", ignoreCase = true)
        if (!isStrictAdmin) {
            LaunchedEffect(Unit) {
                viewModel.navigateTo(ScreenDestination.Home)
            }
            return
        }

        BackHandler {
            viewModel.navigateTo(ScreenDestination.Profile)
        }

        AdminPortalScreen(
            mediaItems = mediaItems,
            activeDevices = activeDevices,
            bannedRecords = viewModel.telemetrySecurityRepository.bannedRecords.collectAsStateWithLifecycle().value,
            currentDeviceId = viewModel.telemetrySecurityRepository.deviceId,
            currentIpAddress = viewModel.telemetrySecurityRepository.clientIp,
            adminNotification = adminNotification,
            onClearNotification = { viewModel.clearAdminNotification() },
            onBackClick = { viewModel.navigateTo(ScreenDestination.Profile) },
            onAddMedia = { title, desc, banner, poster, cat, type, stream, year, rating, seasons ->
                viewModel.addMediaItemFromCMS(
                    title, desc, banner, poster, cat, type, stream, year, rating, seasons
                )
            },
            onSaveMedia = { mediaItem -> viewModel.saveMediaItemFromCMS(mediaItem) },
            onSetFeatured = { id -> viewModel.setFeaturedMedia(id) },
            onToggleTop10 = { id -> viewModel.toggleTop10Media(id) },
            onDeleteMedia = { id -> viewModel.deleteMediaFromCMS(id) },
            onResetCatalog = { viewModel.resetCatalogToDefaults() },
            onBanDevice = { id, reason -> viewModel.banDevice(id, reason) },
            onUnbanDevice = { id -> viewModel.unbanDevice(id) },
            onBanIp = { ip, reason -> viewModel.banIp(ip, reason) },
            onUnbanIp = { ip -> viewModel.unbanIp(ip) }
        )
        return
    }

    // Primary App Scaffold with Netflix Dark Bottom Navigation Bar
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = NetflixBlack,
        bottomBar = {
            NavigationBar(
                containerColor = NetflixBottomBar,
                contentColor = NetflixWhite,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("main_bottom_nav_bar")
            ) {
                // Home Tab
                NavigationBarItem(
                    selected = activeBottomTab == BottomTab.HOME,
                    onClick = {
                        activeBottomTab = BottomTab.HOME
                        viewModel.navigateTo(ScreenDestination.Home)
                    },
                    icon = {
                        Icon(imageVector = Icons.Default.Home, contentDescription = "Home")
                    },
                    label = { Text("Home", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NetflixRed,
                        selectedTextColor = NetflixWhite,
                        unselectedIconColor = NetflixLightGrey,
                        unselectedTextColor = NetflixLightGrey,
                        indicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.testTag("nav_tab_home")
                )

                // History Tab
                NavigationBarItem(
                    selected = activeBottomTab == BottomTab.HISTORY,
                    onClick = {
                        activeBottomTab = BottomTab.HISTORY
                        viewModel.navigateTo(ScreenDestination.History)
                    },
                    icon = {
                        Icon(imageVector = Icons.Default.History, contentDescription = "History")
                    },
                    label = { Text("History", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NetflixRed,
                        selectedTextColor = NetflixWhite,
                        unselectedIconColor = NetflixLightGrey,
                        unselectedTextColor = NetflixLightGrey,
                        indicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.testTag("nav_tab_history")
                )

                // Downloads Tab
                NavigationBarItem(
                    selected = activeBottomTab == BottomTab.DOWNLOADS,
                    onClick = {
                        activeBottomTab = BottomTab.DOWNLOADS
                        viewModel.navigateTo(ScreenDestination.Downloads)
                    },
                    icon = {
                        Icon(imageVector = Icons.Default.Download, contentDescription = "Downloads")
                    },
                    label = { Text("Downloads", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NetflixRed,
                        selectedTextColor = NetflixWhite,
                        unselectedIconColor = NetflixLightGrey,
                        unselectedTextColor = NetflixLightGrey,
                        indicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.testTag("nav_tab_downloads")
                )

                // Profile Tab
                NavigationBarItem(
                    selected = activeBottomTab == BottomTab.PROFILE,
                    onClick = {
                        activeBottomTab = BottomTab.PROFILE
                        viewModel.navigateTo(ScreenDestination.Profile)
                    },
                    icon = {
                        Icon(imageVector = Icons.Default.Person, contentDescription = "Profile")
                    },
                    label = { Text("Profile", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NetflixRed,
                        selectedTextColor = NetflixWhite,
                        unselectedIconColor = NetflixLightGrey,
                        unselectedTextColor = NetflixLightGrey,
                        indicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.testTag("nav_tab_profile")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = activeBottomTab, label = "tab_crossfade") { tab ->
                when (tab) {
                    BottomTab.HOME -> {
                        HomeScreen(
                            mediaItems = mediaItems,
                            featuredMedia = featuredMedia,
                            watchHistory = watchHistory,
                            favoriteIds = favoriteIds,
                            selectedCategory = selectedCategory,
                            onCategorySelected = { cat -> viewModel.selectedCategoryFilter.value = cat },
                            searchQuery = searchQuery,
                            onSearchQueryChange = { q -> viewModel.searchQuery.value = q },
                            userAvatarUrl = currentUser.avatarUrl,
                            isGuest = currentUser.isGuest,
                            onMediaClick = { media ->
                                viewModel.navigateTo(ScreenDestination.MediaDetail(media.id))
                            },
                            onPlayClick = { media ->
                                viewModel.navigateTo(ScreenDestination.VideoPlayer(media.id))
                            },
                            onResumeHistory = { history ->
                                viewModel.navigateTo(
                                    ScreenDestination.VideoPlayer(history.mediaId, history.episodeId)
                                )
                            },
                            onMyListToggle = { id -> viewModel.toggleMyList(id) },
                            onProfileClick = {
                                activeBottomTab = BottomTab.PROFILE
                                viewModel.navigateTo(ScreenDestination.Profile)
                            },
                            onAdminClick = {
                                viewModel.navigateTo(ScreenDestination.AdminPortal)
                            }
                        )
                    }
                    BottomTab.HISTORY -> {
                        HistoryScreen(
                            historyList = watchHistory,
                            isCloudSynced = currentUser.cloudSyncEnabled,
                            userEmail = currentUser.email,
                            onResumeItem = { history ->
                                viewModel.navigateTo(
                                    ScreenDestination.VideoPlayer(history.mediaId, history.episodeId)
                                )
                            },
                            onDeleteItem = { id -> viewModel.removeHistoryItem(id) },
                            onClearAll = { viewModel.clearWatchHistory() }
                        )
                    }
                    BottomTab.DOWNLOADS -> {
                        DownloadsScreen(
                            downloads = downloads,
                            isOfflineModeActive = isOfflineModeActive,
                            onToggleOfflineMode = { active -> viewModel.toggleOfflineMode(active) },
                            onPlayDownload = { dl ->
                                viewModel.navigateTo(ScreenDestination.VideoPlayer(dl.mediaId, dl.episodeId))
                            },
                            onDeleteDownload = { id -> viewModel.removeDownload(id) },
                            onDeleteAllDownloads = { viewModel.clearAllDownloads() },
                            onFindSomethingToDownload = {
                                activeBottomTab = BottomTab.HOME
                                viewModel.navigateTo(ScreenDestination.Home)
                            }
                        )
                    }
                    BottomTab.PROFILE -> {
                        ProfileScreen(
                            user = currentUser,
                            deviceId = viewModel.telemetrySecurityRepository.deviceId,
                            deviceIp = viewModel.telemetrySecurityRepository.clientIp,
                            isBanned = isClientBanned,
                            onSignIn = { email, password -> viewModel.authRepository.signInWithCredentials(email, password) },
                            onSignOutGuest = { viewModel.authRepository.signOutToGuest() },
                            onSelectAvatar = { url -> viewModel.authRepository.updateProfileAvatar(url) },
                            onOpenAdminPortal = { viewModel.navigateTo(ScreenDestination.AdminPortal) }
                        )
                    }
                }
            }
        }
    }
}
