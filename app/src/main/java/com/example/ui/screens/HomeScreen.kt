package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.WatchHistoryEntity
import com.example.data.model.MediaItem
import com.example.data.model.MediaType
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    mediaItems: List<MediaItem>,
    featuredMedia: MediaItem?,
    watchHistory: List<WatchHistoryEntity>,
    favoriteIds: Set<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    userAvatarUrl: String,
    isGuest: Boolean,
    isAdmin: Boolean = false,
    onMediaClick: (MediaItem) -> Unit,
    onPlayClick: (MediaItem) -> Unit,
    onResumeHistory: (WatchHistoryEntity) -> Unit,
    onMyListToggle: (String) -> Unit,
    onProfileClick: () -> Unit,
    onAdminClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Filter items based on category and search query
    val filteredItems = remember(mediaItems, selectedCategory, searchQuery) {
        var list = mediaItems
        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim()
            list = list.filter {
                it.title.contains(q, ignoreCase = true) ||
                it.description.contains(q, ignoreCase = true) ||
                it.genres.any { g -> g.contains(q, ignoreCase = true) } ||
                it.cast.any { c -> c.contains(q, ignoreCase = true) } ||
                it.category.contains(q, ignoreCase = true) ||
                (it.badgeLabel?.contains(q, ignoreCase = true) == true)
            }
        } else {
            when (selectedCategory) {
                "TV Shows" -> list = list.filter { it.type == MediaType.SERIES }
                "Movies" -> list = list.filter { it.type == MediaType.MOVIE }
                "Trending" -> list = list.filter { it.category == "Trending Now" || it.isTop10 }
                "My List" -> list = list.filter { favoriteIds.contains(it.id) }
            }
        }
        list
    }

    // Grouping for carousels
    val trendingItems = remember(mediaItems) { mediaItems.filter { it.category == "Trending Now" } }
    val popularMovies = remember(mediaItems) { mediaItems.filter { it.type == MediaType.MOVIE } }
    val topSeries = remember(mediaItems) { mediaItems.filter { it.type == MediaType.SERIES } }
    val actionItems = remember(mediaItems) { mediaItems.filter { it.genres.contains("Action") || it.genres.contains("Sci-Fi") } }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NetflixBlack)
            .testTag("home_screen_root")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // Spacer only in Search or My List mode so results start below the floating top bar
            if (searchQuery.isNotBlank() || selectedCategory == "My List") {
                item {
                    Spacer(modifier = Modifier.height(95.dp))
                }
            }

            // Search Mode vs Normal Mode
            if (searchQuery.isNotBlank() || selectedCategory == "My List") {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (searchQuery.isNotBlank()) "Top Results for \"$searchQuery\" (${filteredItems.size})" else "My List (${filteredItems.size})",
                            color = NetflixWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        if (searchQuery.isNotBlank()) {
                            androidx.compose.material3.TextButton(
                                onClick = { onSearchQueryChange("") }
                            ) {
                                Text("Clear", color = NetflixRed, fontSize = 13.sp)
                            }
                        }
                    }
                }

                if (filteredItems.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (searchQuery.isNotBlank()) "No titles found matching \"$searchQuery\"" else "Your list is empty.",
                                color = NetflixWhite,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (searchQuery.isNotBlank()) "Try searching for another movie, TV show, genre (e.g. Action, Sci-Fi) or actor." else "Browse movies and TV shows and tap '+ My List' to save them here.",
                                color = NetflixLightGrey,
                                fontSize = 13.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            androidx.compose.material3.Button(
                                onClick = {
                                    onSearchQueryChange("")
                                    onCategorySelected("Trending")
                                },
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                    containerColor = NetflixRed
                                )
                            ) {
                                Text("Explore Trending Titles", color = Color.White)
                            }
                        }
                    }
                } else {
                    item {
                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            // 3-column wrap or chunked rows
                            filteredItems.chunked(3).forEach { rowItems ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    rowItems.forEach { media ->
                                        MediaPosterCard(
                                            media = media,
                                            onClick = { onMediaClick(media) },
                                            modifier = Modifier.weight(1f),
                                            height = 160.dp
                                        )
                                    }
                                    // Filler spacing for partial row
                                    repeat(3 - rowItems.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Top Hero Featured Banner
                if (featuredMedia != null) {
                    item {
                        HeroBanner(
                            media = featuredMedia,
                            isFavorite = favoriteIds.contains(featuredMedia.id),
                            onPlayClick = { onPlayClick(featuredMedia) },
                            onMyListToggle = { onMyListToggle(featuredMedia.id) },
                            onInfoClick = { onMediaClick(featuredMedia) }
                        )
                    }
                }

                // Continue Watching Section (if user has watch history)
                if (watchHistory.isNotEmpty()) {
                    item {
                        ContinueWatchingCarousel(
                            title = "Continue Watching",
                            historyItems = watchHistory,
                            onItemClick = { item ->
                                val media = mediaItems.find { it.id == item.mediaId }
                                if (media != null) onMediaClick(media)
                            },
                            onResumeClick = onResumeHistory
                        )
                    }
                }

                // Horizontal Scrolling Carousels
                item {
                    MediaCarouselRow(
                        title = "Trending Now",
                        items = trendingItems,
                        onItemClick = onMediaClick
                    )
                }

                item {
                    MediaCarouselRow(
                        title = "Popular Movies",
                        items = popularMovies,
                        onItemClick = onMediaClick
                    )
                }

                item {
                    MediaCarouselRow(
                        title = "Binge-Worthy TV Series",
                        items = topSeries,
                        onItemClick = onMediaClick
                    )
                }

                item {
                    MediaCarouselRow(
                        title = "Action & Sci-Fi Thrillers",
                        items = actionItems,
                        onItemClick = onMediaClick
                    )
                }
            }
        }

        // Floating Netflix Top Bar with gradient backdrop
        StreamFlixTopBar(
            selectedCategory = selectedCategory,
            onCategorySelected = onCategorySelected,
            searchQuery = searchQuery,
            onSearchQueryChange = onSearchQueryChange,
            userAvatarUrl = userAvatarUrl,
            isGuest = isGuest,
            isAdmin = isAdmin,
            onProfileClick = onProfileClick,
            onAdminClick = onAdminClick
        )
    }
}
