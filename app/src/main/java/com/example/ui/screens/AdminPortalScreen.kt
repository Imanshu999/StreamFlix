package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.BanRecord
import com.example.data.model.EpisodeData
import com.example.data.model.MediaItem
import com.example.data.model.MediaType
import com.example.data.model.SeasonData
import com.example.data.model.UserDeviceTelemetry
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPortalScreen(
    mediaItems: List<MediaItem>,
    activeDevices: List<UserDeviceTelemetry>,
    bannedRecords: List<BanRecord>,
    currentDeviceId: String,
    currentIpAddress: String,
    adminNotification: String?,
    onClearNotification: () -> Unit,
    onBackClick: () -> Unit,
    onAddMedia: (
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
    ) -> Unit,
    onSaveMedia: (MediaItem) -> Unit = {},
    onSetFeatured: (id: String) -> Unit = {},
    onToggleTop10: (id: String) -> Unit = {},
    onDeleteMedia: (id: String) -> Unit,
    onResetCatalog: () -> Unit,
    onBanDevice: (deviceId: String, reason: String) -> Unit,
    onUnbanDevice: (deviceId: String) -> Unit,
    onBanIp: (ip: String, reason: String) -> Unit,
    onUnbanIp: (ip: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: CMS, 1: Security Dashboard

    // Form states for CMS
    var editingItem by remember { mutableStateOf<MediaItem?>(null) }
    var titleInput by remember { mutableStateOf("") }
    var descriptionInput by remember { mutableStateOf("") }
    var bannerUrlInput by remember { mutableStateOf("https://images.unsplash.com/photo-1574375927938-d5a98e8ffe85?w=1200&q=80") }
    var posterUrlInput by remember { mutableStateOf("https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=500&q=80") }
    var categoryInput by remember { mutableStateOf("Trending Now") }
    var mediaTypeInput by remember { mutableStateOf(MediaType.MOVIE) }
    var streamUrlInput by remember { mutableStateOf("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4") }
    var ratingInput by remember { mutableStateOf("TV-MA") }
    var durationInput by remember { mutableStateOf("2h 15m") }
    var releaseYearInput by remember { mutableStateOf("2024") }
    var seasonsList by remember { mutableStateOf<List<SeasonData>>(emptyList()) }
    var isFeaturedInput by remember { mutableStateOf(false) }
    var isTop10Input by remember { mutableStateOf(false) }
    var badgeLabelInput by remember { mutableStateOf("") }
    var formErrorMessage by remember { mutableStateOf<String?>(null) }

    val cmsListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val dateFormat = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NetflixBlack)
            .statusBarsPadding()
            .testTag("admin_portal_root")
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.testTag("admin_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = NetflixWhite
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "StreamFlix Admin Panel",
                    color = NetflixWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Real-Time Cloud CMS & Security Moderation",
                    color = AccentGlow,
                    fontSize = 11.sp
                )
            }

            // Quick Reset Button
            IconButton(
                onClick = onResetCatalog,
                modifier = Modifier.testTag("reset_catalog_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Restore,
                    contentDescription = "Reset Catalog",
                    tint = NetflixLightGrey
                )
            }
        }

        // Admin Notification Banner
        adminNotification?.let { msg ->
            Surface(
                color = AccentBlue,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = msg, color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    IconButton(onClick = onClearNotification, modifier = Modifier.size(24.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
            }
        }

        // Tab Selector: CMS vs Security
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = NetflixDarkGrey,
            contentColor = NetflixRed,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.MovieFilter, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Content CMS")
                    }
                },
                selectedContentColor = NetflixRed,
                unselectedContentColor = NetflixLightGrey
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Security & Bans")
                    }
                },
                selectedContentColor = AccentBlue,
                unselectedContentColor = NetflixLightGrey
            )
        }

        if (selectedTab == 0) {
            // Content Management System (CMS) Tab
            LazyColumn(
                state = cmsListState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header & Edit Status Indicator
                item {
                    if (editingItem != null) {
                        Surface(
                            color = AccentBlue.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AccentBlue),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = null,
                                        tint = AccentGlow,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Editing Mode: ${editingItem?.title}",
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "ID: ${editingItem?.id} • Modifying metadata & episodes",
                                            color = AccentGlow,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                                TextButton(
                                    onClick = {
                                        editingItem = null
                                        titleInput = ""
                                        descriptionInput = ""
                                        streamUrlInput = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
                                        categoryInput = "Trending Now"
                                        mediaTypeInput = MediaType.MOVIE
                                        seasonsList = emptyList()
                                        isFeaturedInput = false
                                        isTop10Input = false
                                        badgeLabelInput = ""
                                        formErrorMessage = null
                                    }
                                ) {
                                    Text("Cancel Edit", color = NetflixLightGrey, fontSize = 12.sp)
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "Publish New Movie or TV Series",
                            color = NetflixWhite,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Published content immediately syncs across all client app feeds.",
                            color = NetflixLightGrey,
                            fontSize = 12.sp
                        )
                    }
                }

                // CMS Form Card
                item {
                    Surface(
                        color = NetflixCardSurface,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Title
                            OutlinedTextField(
                                value = titleInput,
                                onValueChange = { titleInput = it },
                                label = { Text("Title (e.g. Stranger Signals)", color = NetflixLightGrey) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = NetflixWhite,
                                    unfocusedTextColor = NetflixWhite,
                                    focusedBorderColor = NetflixRed,
                                    unfocusedBorderColor = NetflixCardBorder
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("cms_title_input")
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Description
                            OutlinedTextField(
                                value = descriptionInput,
                                onValueChange = { descriptionInput = it },
                                label = { Text("Synopsis / Description", color = NetflixLightGrey) },
                                minLines = 2,
                                maxLines = 4,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = NetflixWhite,
                                    unfocusedTextColor = NetflixWhite,
                                    focusedBorderColor = NetflixRed,
                                    unfocusedBorderColor = NetflixCardBorder
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("cms_desc_input")
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Type Selection: Movie vs Series
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clickable {
                                            mediaTypeInput = MediaType.MOVIE
                                        }
                                        .padding(4.dp)
                                ) {
                                    RadioButton(
                                        selected = mediaTypeInput == MediaType.MOVIE,
                                        onClick = { mediaTypeInput = MediaType.MOVIE },
                                        colors = RadioButtonDefaults.colors(selectedColor = NetflixRed)
                                    )
                                    Text("Movie", color = NetflixWhite, fontSize = 13.sp)
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clickable {
                                            mediaTypeInput = MediaType.SERIES
                                            if (seasonsList.isEmpty()) {
                                                seasonsList = listOf(
                                                    SeasonData(
                                                        seasonNumber = 1,
                                                        title = "Season 1",
                                                        episodes = listOf(
                                                            EpisodeData(
                                                                id = "ep_${System.currentTimeMillis()}_1",
                                                                episodeNumber = 1,
                                                                title = "Episode 1: Chapter One",
                                                                overview = "The adventure begins with an unexpected transmission.",
                                                                thumbnailUrl = bannerUrlInput,
                                                                durationMinutes = 48,
                                                                streamUrl = streamUrlInput
                                                            )
                                                        )
                                                    )
                                                )
                                            }
                                        }
                                        .padding(4.dp)
                                ) {
                                    RadioButton(
                                        selected = mediaTypeInput == MediaType.SERIES,
                                        onClick = {
                                            mediaTypeInput = MediaType.SERIES
                                            if (seasonsList.isEmpty()) {
                                                seasonsList = listOf(
                                                    SeasonData(
                                                        seasonNumber = 1,
                                                        title = "Season 1",
                                                        episodes = listOf(
                                                            EpisodeData(
                                                                id = "ep_${System.currentTimeMillis()}_1",
                                                                episodeNumber = 1,
                                                                title = "Episode 1: Chapter One",
                                                                overview = "The adventure begins with an unexpected transmission.",
                                                                thumbnailUrl = bannerUrlInput,
                                                                durationMinutes = 48,
                                                                streamUrl = streamUrlInput
                                                            )
                                                        )
                                                    )
                                                )
                                            }
                                        },
                                        colors = RadioButtonDefaults.colors(selectedColor = NetflixRed)
                                    )
                                    Text("TV Series (Seasons & Episodes)", color = NetflixWhite, fontSize = 13.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Category Presets
                            Text("Category Placement:", color = NetflixLightGrey, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val catPresets = listOf("Trending Now", "Popular on StreamFlix", "Top Rated", "Blockbusters", "Action & Sci-Fi", "Crime & Thriller", "Comedy Hits")
                                catPresets.forEach { cat ->
                                    val isSelected = categoryInput == cat
                                    Surface(
                                        color = if (isSelected) NetflixRed else NetflixBlack,
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, if (isSelected) NetflixRed else NetflixCardBorder),
                                        modifier = Modifier.clickable { categoryInput = cat }
                                    ) {
                                        Text(
                                            text = cat,
                                            color = if (isSelected) Color.White else NetflixLightGrey,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Category & Release Year
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = categoryInput,
                                    onValueChange = { categoryInput = it },
                                    label = { Text("Category", color = NetflixLightGrey) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = NetflixWhite,
                                        unfocusedTextColor = NetflixWhite,
                                        focusedBorderColor = NetflixRed,
                                        unfocusedBorderColor = NetflixCardBorder
                                    ),
                                    modifier = Modifier.weight(1.2f)
                                )

                                OutlinedTextField(
                                    value = ratingInput,
                                    onValueChange = { ratingInput = it },
                                    label = { Text("Rating (TV-MA)", color = NetflixLightGrey) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = NetflixWhite,
                                        unfocusedTextColor = NetflixWhite,
                                        focusedBorderColor = NetflixRed,
                                        unfocusedBorderColor = NetflixCardBorder
                                    ),
                                    modifier = Modifier.weight(0.8f)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Direct Stream URL / Video Stream Link
                            OutlinedTextField(
                                value = streamUrlInput,
                                onValueChange = { streamUrlInput = it },
                                label = {
                                    Text(
                                        if (mediaTypeInput == MediaType.MOVIE) "Movie Stream URL (.mp4 / HLS)" else "Default/Trailer Video Stream URL",
                                        color = NetflixLightGrey
                                    )
                                },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = NetflixWhite,
                                    unfocusedTextColor = NetflixWhite,
                                    focusedBorderColor = NetflixRed,
                                    unfocusedBorderColor = NetflixCardBorder
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("cms_stream_url_input")
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Banner URL
                            OutlinedTextField(
                                value = bannerUrlInput,
                                onValueChange = { bannerUrlInput = it },
                                label = { Text("Banner Image URL", color = NetflixLightGrey) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = NetflixWhite,
                                    unfocusedTextColor = NetflixWhite,
                                    focusedBorderColor = NetflixRed,
                                    unfocusedBorderColor = NetflixCardBorder
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            // STRUCTURED SEASONS & EPISODES MANAGER (FOR TV SERIES)
                            if (mediaTypeInput == MediaType.SERIES) {
                                Spacer(modifier = Modifier.height(14.dp))
                                HorizontalDivider(color = NetflixCardBorder)
                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Seasons & Episodes Hierarchy",
                                            color = AccentGlow,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${seasonsList.size} Season(s) Configured",
                                            color = NetflixLightGrey,
                                            fontSize = 11.sp
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            val nextSeasonNumber = seasonsList.size + 1
                                            seasonsList = seasonsList + SeasonData(
                                                seasonNumber = nextSeasonNumber,
                                                title = "Season $nextSeasonNumber",
                                                episodes = listOf(
                                                    EpisodeData(
                                                        id = "ep_${System.currentTimeMillis()}_${nextSeasonNumber}_1",
                                                        episodeNumber = 1,
                                                        title = "Episode 1",
                                                        overview = "Premiere of Season $nextSeasonNumber",
                                                        thumbnailUrl = bannerUrlInput,
                                                        durationMinutes = 45,
                                                        streamUrl = streamUrlInput
                                                    )
                                                )
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.testTag("cms_add_season_button")
                                    ) {
                                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("+ Add Season", fontSize = 12.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Seasons List UI
                                seasonsList.forEachIndexed { sIndex, season ->
                                    Surface(
                                        color = NetflixBlack.copy(alpha = 0.6f),
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, NetflixCardBorder),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                // Season Title Input
                                                OutlinedTextField(
                                                    value = season.title,
                                                    onValueChange = { newTitle ->
                                                        val updated = seasonsList.toMutableList()
                                                        updated[sIndex] = season.copy(title = newTitle)
                                                        seasonsList = updated
                                                    },
                                                    label = { Text("Season Title (or e.g. Season ${season.seasonNumber} (Coming Soon))", fontSize = 10.sp) },
                                                    singleLine = true,
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedTextColor = NetflixWhite,
                                                        unfocusedTextColor = NetflixWhite,
                                                        focusedBorderColor = AccentBlue,
                                                        unfocusedBorderColor = NetflixCardBorder
                                                    ),
                                                    modifier = Modifier.weight(1f)
                                                )

                                                Spacer(modifier = Modifier.width(8.dp))

                                                // Delete Season Button
                                                IconButton(
                                                    onClick = {
                                                        val updated = seasonsList.toMutableList()
                                                        updated.removeAt(sIndex)
                                                        seasonsList = updated
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Delete Season",
                                                        tint = DangerRed,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(6.dp))

                                            // Modular Unreleased Season Feature
                                            if (season.episodes.isEmpty()) {
                                                Surface(
                                                    color = NetflixCardSurface,
                                                    shape = RoundedCornerShape(6.dp),
                                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(8.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Text(
                                                                text = "Modular Status: Unreleased / None",
                                                                color = AccentGlow,
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                            Text(
                                                                text = "No episodes published yet. Ready for future release.",
                                                                color = NetflixLightGrey,
                                                                fontSize = 10.sp
                                                            )
                                                        }
                                                        Button(
                                                            onClick = {
                                                                val updated = seasonsList.toMutableList()
                                                                updated[sIndex] = season.copy(
                                                                    episodes = listOf(
                                                                        EpisodeData(
                                                                            id = "ep_${System.currentTimeMillis()}_${season.seasonNumber}_1",
                                                                            episodeNumber = 1,
                                                                            title = "Episode 1",
                                                                            overview = "Episode description",
                                                                            thumbnailUrl = bannerUrlInput,
                                                                            durationMinutes = 45,
                                                                            streamUrl = streamUrlInput
                                                                        )
                                                                    )
                                                                )
                                                                seasonsList = updated
                                                            },
                                                            colors = ButtonDefaults.buttonColors(containerColor = NetflixRed),
                                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                                        ) {
                                                            Text("Add Episodes", fontSize = 11.sp)
                                                        }
                                                    }
                                                }
                                            } else {
                                                // Episodes in this Season
                                                Text(
                                                    text = "Episodes (${season.episodes.size})",
                                                    color = NetflixLightGrey,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )

                                                season.episodes.forEachIndexed { epIndex, episode ->
                                                    Surface(
                                                        color = NetflixCardSurface,
                                                        shape = RoundedCornerShape(6.dp),
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(vertical = 3.dp)
                                                    ) {
                                                        Column(modifier = Modifier.padding(8.dp)) {
                                                            Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Text(
                                                                    text = "E${episode.episodeNumber}",
                                                                    color = AccentGlow,
                                                                    fontSize = 12.sp,
                                                                    fontWeight = FontWeight.Bold
                                                                )
                                                                Spacer(modifier = Modifier.width(6.dp))
                                                                OutlinedTextField(
                                                                    value = episode.title,
                                                                    onValueChange = { newTitle ->
                                                                        val updatedSeasons = seasonsList.toMutableList()
                                                                        val updatedEpisodes = season.episodes.toMutableList()
                                                                        updatedEpisodes[epIndex] = episode.copy(title = newTitle)
                                                                        updatedSeasons[sIndex] = season.copy(episodes = updatedEpisodes)
                                                                        seasonsList = updatedSeasons
                                                                    },
                                                                    label = { Text("Episode Title", fontSize = 9.sp) },
                                                                    singleLine = true,
                                                                    colors = OutlinedTextFieldDefaults.colors(
                                                                        focusedTextColor = NetflixWhite,
                                                                        unfocusedTextColor = NetflixWhite,
                                                                        focusedBorderColor = NetflixRed,
                                                                        unfocusedBorderColor = NetflixCardBorder
                                                                    ),
                                                                    modifier = Modifier.weight(1f)
                                                                )
                                                                Spacer(modifier = Modifier.width(6.dp))
                                                                IconButton(
                                                                    onClick = {
                                                                        val updatedSeasons = seasonsList.toMutableList()
                                                                        val updatedEpisodes = season.episodes.toMutableList()
                                                                        updatedEpisodes.removeAt(epIndex)
                                                                        updatedSeasons[sIndex] = season.copy(episodes = updatedEpisodes)
                                                                        seasonsList = updatedSeasons
                                                                    },
                                                                    modifier = Modifier.size(28.dp)
                                                                ) {
                                                                    Icon(
                                                                        imageVector = Icons.Default.Close,
                                                                        contentDescription = "Remove Episode",
                                                                        tint = NetflixLightGrey,
                                                                        modifier = Modifier.size(16.dp)
                                                                    )
                                                                }
                                                            }

                                                            Spacer(modifier = Modifier.height(4.dp))

                                                            // Episode Stream URL
                                                            OutlinedTextField(
                                                                value = episode.streamUrl,
                                                                onValueChange = { newUrl ->
                                                                    val updatedSeasons = seasonsList.toMutableList()
                                                                    val updatedEpisodes = season.episodes.toMutableList()
                                                                    updatedEpisodes[epIndex] = episode.copy(streamUrl = newUrl)
                                                                    updatedSeasons[sIndex] = season.copy(episodes = updatedEpisodes)
                                                                    seasonsList = updatedSeasons
                                                                },
                                                                label = { Text("Episode Stream URL", fontSize = 9.sp) },
                                                                singleLine = true,
                                                                colors = OutlinedTextFieldDefaults.colors(
                                                                    focusedTextColor = NetflixWhite,
                                                                    unfocusedTextColor = NetflixWhite,
                                                                    focusedBorderColor = NetflixRed,
                                                                    unfocusedBorderColor = NetflixCardBorder
                                                                ),
                                                                modifier = Modifier.fillMaxWidth()
                                                            )
                                                        }
                                                    }
                                                }

                                                // Add episode button
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    TextButton(
                                                        onClick = {
                                                            val updatedSeasons = seasonsList.toMutableList()
                                                            val nextEpNum = season.episodes.size + 1
                                                            val newEp = EpisodeData(
                                                                id = "ep_${System.currentTimeMillis()}_${season.seasonNumber}_$nextEpNum",
                                                                episodeNumber = nextEpNum,
                                                                title = "Episode $nextEpNum",
                                                                overview = "Synopsis for episode $nextEpNum",
                                                                thumbnailUrl = bannerUrlInput,
                                                                durationMinutes = 45,
                                                                streamUrl = streamUrlInput
                                                            )
                                                            updatedSeasons[sIndex] = season.copy(episodes = season.episodes + newEp)
                                                            seasonsList = updatedSeasons
                                                        }
                                                    ) {
                                                        Icon(Icons.Default.Add, contentDescription = null, tint = AccentGlow, modifier = Modifier.size(14.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("+ Add Episode to S${season.seasonNumber}", color = AccentGlow, fontSize = 11.sp)
                                                    }

                                                    TextButton(
                                                        onClick = {
                                                            // Set season to unreleased / none
                                                            val updatedSeasons = seasonsList.toMutableList()
                                                            updatedSeasons[sIndex] = season.copy(
                                                                title = "Season ${season.seasonNumber} (Coming Soon)",
                                                                episodes = emptyList()
                                                            )
                                                            seasonsList = updatedSeasons
                                                        }
                                                    ) {
                                                        Text("Set as Unreleased (None)", color = NetflixLightGrey, fontSize = 10.sp)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // TOP MOVIE / TV SHOW SELECTION & RANKING CONTROLS
                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(color = NetflixCardBorder)
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Featured & Top Rankings (Placement Controls)",
                                color = Color(0xFFFFD700),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Select whether this movie or TV show is highlighted as the primary Top Hero Banner or in the Top 10 rankings.",
                                color = NetflixLightGrey,
                                fontSize = 11.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Top Hero Banner Switch Card
                            Surface(
                                color = if (isFeaturedInput) Color(0xFFFFD700).copy(alpha = 0.12f) else NetflixBlack,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, if (isFeaturedInput) Color(0xFFFFD700) else NetflixCardBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isFeaturedInput = !isFeaturedInput }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = if (isFeaturedInput) Color(0xFFFFD700) else NetflixLightGrey,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "Top Featured Hero Banner",
                                                color = if (isFeaturedInput) Color(0xFFFFD700) else NetflixWhite,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Pin this as the #1 main showcase title on the Home Screen banner.",
                                                color = NetflixLightGrey,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                    Switch(
                                        checked = isFeaturedInput,
                                        onCheckedChange = { isFeaturedInput = it },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color(0xFFFFD700),
                                            checkedTrackColor = Color(0xFFFFD700).copy(alpha = 0.5f)
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Top 10 Ranked Selection Switch Card
                            Surface(
                                color = if (isTop10Input) NetflixRed.copy(alpha = 0.12f) else NetflixBlack,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, if (isTop10Input) NetflixRed else NetflixCardBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isTop10Input = !isTop10Input }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.EmojiEvents,
                                            contentDescription = null,
                                            tint = if (isTop10Input) NetflixRed else NetflixLightGrey,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "Top 10 Ranked Title",
                                                color = if (isTop10Input) NetflixRed else NetflixWhite,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Include in Top 10 carousels with official TOP 10 rank indicator.",
                                                color = NetflixLightGrey,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                    Switch(
                                        checked = isTop10Input,
                                        onCheckedChange = { isTop10Input = it },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = NetflixRed,
                                            checkedTrackColor = NetflixRed.copy(alpha = 0.5f)
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Custom Badge / Ranking Label Presets
                            Text("Top Badge Label (Quick Selection):", color = NetflixLightGrey, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val rankPresets = listOf(
                                    if (mediaTypeInput == MediaType.SERIES) "#1 in TV Shows Today" else "#1 in Movies Today",
                                    "TOP 10",
                                    "TRENDING NOW",
                                    "MUST WATCH",
                                    "NEW EPISODES",
                                    "BLOCKBUSTER"
                                )
                                rankPresets.forEach { preset ->
                                    val isCurrent = badgeLabelInput == preset
                                    Surface(
                                        color = if (isCurrent) NetflixRed else NetflixBlack,
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, if (isCurrent) NetflixRed else NetflixCardBorder),
                                        modifier = Modifier.clickable {
                                            if (isCurrent) {
                                                badgeLabelInput = ""
                                            } else {
                                                badgeLabelInput = preset
                                                if (preset.contains("TOP 10", ignoreCase = true) || preset.contains("#1", ignoreCase = true)) {
                                                    isTop10Input = true
                                                }
                                            }
                                        }
                                    ) {
                                        Text(
                                            text = preset,
                                            color = if (isCurrent) Color.White else NetflixLightGrey,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            OutlinedTextField(
                                value = badgeLabelInput,
                                onValueChange = { badgeLabelInput = it },
                                label = { Text("Custom Badge Label (Optional)", color = NetflixLightGrey) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = NetflixWhite,
                                    unfocusedTextColor = NetflixWhite,
                                    focusedBorderColor = NetflixRed,
                                    unfocusedBorderColor = NetflixCardBorder
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            formErrorMessage?.let { err ->
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = err, color = DangerRed, fontSize = 12.sp)
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Submit Button (Handles Publish or Save Updates)
                            Button(
                                onClick = {
                                    if (titleInput.isBlank()) {
                                        formErrorMessage = "Please enter a valid title."
                                        return@Button
                                    }
                                    if (streamUrlInput.isBlank()) {
                                        formErrorMessage = "Please provide a streaming URL."
                                        return@Button
                                    }

                                    formErrorMessage = null

                                    if (editingItem != null) {
                                        // UPDATE EXISTING ITEM
                                        val updated = editingItem!!.copy(
                                            title = titleInput.trim(),
                                            description = descriptionInput.ifBlank { "StreamFlix Official Release." },
                                            bannerUrl = bannerUrlInput.trim().ifBlank { "https://images.unsplash.com/photo-1574375927938-d5a98e8ffe85?w=1200&q=80" },
                                            posterUrl = posterUrlInput.trim().ifBlank { "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=500&q=80" },
                                            category = categoryInput.trim(),
                                            type = mediaTypeInput,
                                            directStreamUrl = streamUrlInput.trim(),
                                            rating = ratingInput.trim(),
                                            durationText = if (mediaTypeInput == MediaType.SERIES) "${seasonsList.size} Season(s)" else durationInput.trim(),
                                            isFeatured = isFeaturedInput,
                                            isTop10 = isTop10Input,
                                            badgeLabel = badgeLabelInput.trim().ifBlank { if (isTop10Input) "TOP 10" else null },
                                            seasons = if (mediaTypeInput == MediaType.SERIES) seasonsList else emptyList()
                                        )
                                        onSaveMedia(updated)
                                        editingItem = null
                                    } else {
                                        // PUBLISH NEW ITEM
                                        val newItem = MediaItem(
                                            id = "cms_${System.currentTimeMillis()}",
                                            title = titleInput.trim(),
                                            description = descriptionInput.ifBlank { "StreamFlix Official Release." },
                                            bannerUrl = bannerUrlInput.trim().ifBlank { "https://images.unsplash.com/photo-1574375927938-d5a98e8ffe85?w=1200&q=80" },
                                            posterUrl = posterUrlInput.trim().ifBlank { "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=500&q=80" },
                                            type = mediaTypeInput,
                                            category = categoryInput.trim(),
                                            genres = listOf("Trending", "Original"),
                                            releaseYear = 2024,
                                            rating = ratingInput.trim(),
                                            matchPercentage = 98,
                                            durationText = if (mediaTypeInput == MediaType.SERIES) "${seasonsList.size} Season(s)" else durationInput.trim(),
                                            directStreamUrl = streamUrlInput.trim(),
                                            isFeatured = isFeaturedInput,
                                            isTop10 = isTop10Input,
                                            badgeLabel = badgeLabelInput.trim().ifBlank { if (isTop10Input) "TOP 10" else null },
                                            seasons = if (mediaTypeInput == MediaType.SERIES) seasonsList else emptyList()
                                        )
                                        onSaveMedia(newItem)
                                    }

                                    // Reset inputs
                                    titleInput = ""
                                    descriptionInput = ""
                                    seasonsList = emptyList()
                                    isFeaturedInput = false
                                    isTop10Input = false
                                    badgeLabelInput = ""
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (editingItem != null) AccentBlue else NetflixRed
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("cms_publish_button")
                            ) {
                                Icon(
                                    imageVector = if (editingItem != null) Icons.Default.Check else Icons.Default.CloudUpload,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (editingItem != null) "Save & Update Live Catalog" else "Publish to Live Feed",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Live Catalog Header
                item {
                    Text(
                        text = "Current Catalog (${mediaItems.size} Titles)",
                        color = NetflixWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Existing Items with Edit and Delete functionality
                items(mediaItems, key = { it.id }) { item ->
                    Surface(
                        color = NetflixCardSurface,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(width = 60.dp, height = 45.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            ) {
                                AsyncImage(
                                    model = item.bannerUrl,
                                    contentDescription = item.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    color = NetflixWhite,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${item.category} • ${if (item.type == MediaType.SERIES) "${item.seasons.size} Season(s)" else "Movie"}",
                                    color = NetflixLightGrey,
                                    fontSize = 11.sp
                                )

                                // Badges for Featured & Top 10
                                Row(
                                    modifier = Modifier.padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (item.isFeatured) {
                                        Surface(
                                            color = Color(0xFFFFD700).copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(3.dp),
                                            border = BorderStroke(1.dp, Color(0xFFFFD700))
                                        ) {
                                            Text(
                                                text = "⭐ TOP HERO",
                                                color = Color(0xFFFFD700),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                    if (item.isTop10) {
                                        Surface(
                                            color = NetflixRed.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(3.dp),
                                            border = BorderStroke(1.dp, NetflixRed)
                                        ) {
                                            Text(
                                                text = "🏆 TOP 10",
                                                color = NetflixRed,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                    if (!item.badgeLabel.isNullOrBlank()) {
                                        Surface(
                                            color = NetflixCardBorder,
                                            shape = RoundedCornerShape(3.dp)
                                        ) {
                                            Text(
                                                text = item.badgeLabel,
                                                color = NetflixWhite,
                                                fontSize = 9.sp,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // QUICK ACTION: TOGGLE TOP HERO BANNER
                            IconButton(
                                onClick = { onSetFeatured(item.id) },
                                modifier = Modifier.testTag("set_hero_${item.id}")
                            ) {
                                Icon(
                                    imageVector = if (item.isFeatured) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "Set as Top Hero Banner",
                                    tint = if (item.isFeatured) Color(0xFFFFD700) else NetflixLightGrey.copy(alpha = 0.6f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // QUICK ACTION: TOGGLE TOP 10 RANK
                            IconButton(
                                onClick = { onToggleTop10(item.id) },
                                modifier = Modifier.testTag("toggle_top10_${item.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = "Toggle Top 10 Rank",
                                    tint = if (item.isTop10) NetflixRed else NetflixLightGrey.copy(alpha = 0.6f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // EDIT BUTTON
                            IconButton(
                                onClick = {
                                    editingItem = item
                                    titleInput = item.title
                                    descriptionInput = item.description
                                    mediaTypeInput = item.type
                                    categoryInput = item.category
                                    streamUrlInput = item.directStreamUrl
                                    bannerUrlInput = item.bannerUrl
                                    posterUrlInput = item.posterUrl
                                    ratingInput = item.rating
                                    durationInput = item.durationText
                                    seasonsList = item.seasons
                                    isFeaturedInput = item.isFeatured
                                    isTop10Input = item.isTop10
                                    badgeLabelInput = item.badgeLabel ?: ""
                                    coroutineScope.launch {
                                        cmsListState.animateScrollToItem(0)
                                    }
                                },
                                modifier = Modifier.testTag("edit_media_${item.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Item",
                                    tint = AccentBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // DELETE BUTTON
                            IconButton(
                                onClick = { onDeleteMedia(item.id) },
                                modifier = Modifier.testTag("delete_media_${item.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = DangerRed,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Security Dashboard & Telemetry Moderation Tab
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Text(
                        text = "Real-Time Telemetry & Access Control",
                        color = NetflixWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Monitors device downloads and IP activity. Banning an active device triggers immediate client lockout.",
                        color = NetflixLightGrey,
                        fontSize = 12.sp
                    )
                }

                // Current Device Security Card
                item {
                    Surface(
                        color = AccentBlue.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Your Current Device",
                                    color = AccentGlow,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Surface(
                                    color = AccentBlue.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "HOST CLIENT",
                                        color = AccentGlow,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = "Device ID: $currentDeviceId", color = NetflixWhite, fontSize = 12.sp)
                            Text(text = "IP Address: $currentIpAddress", color = NetflixWhite, fontSize = 12.sp)

                            Spacer(modifier = Modifier.height(10.dp))

                            // Test Instant Ban on Current Device
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        onBanDevice(currentDeviceId, "Admin manual security trigger")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.weight(1f).testTag("test_ban_current_device_button")
                                ) {
                                    Text("Test Ban This Device", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        onUnbanDevice(currentDeviceId)
                                        onUnbanIp(currentIpAddress)
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = BadgeGreen),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.weight(1f).testTag("unban_current_device_button")
                                ) {
                                    Text("Unban", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Active Devices Telemetry Table Header
                item {
                    Text(
                        text = "Active Devices Fleet (${activeDevices.size})",
                        color = NetflixWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(activeDevices, key = { it.deviceId }) { device ->
                    Surface(
                        color = NetflixCardSurface,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = device.deviceModel,
                                    color = NetflixWhite,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Surface(
                                    color = if (device.isBanned) DangerRed.copy(alpha = 0.2f) else BadgeGreen.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = if (device.isBanned) "BANNED" else "ACTIVE",
                                        color = if (device.isBanned) DangerRed else BadgeGreen,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "ID: ${device.deviceId}", color = NetflixLightGrey, fontSize = 11.sp)
                            Text(text = "IP: ${device.ipAddress} • ${device.osVersion}", color = NetflixLightGrey, fontSize = 11.sp)
                            Text(
                                text = "Last Active: ${dateFormat.format(Date(device.lastActiveTimestamp))}",
                                color = NetflixLightGrey,
                                fontSize = 11.sp
                            )

                            if (device.banReason != null) {
                                Text(
                                    text = "Reason: ${device.banReason}",
                                    color = DangerRed,
                                    fontSize = 11.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (device.isBanned) {
                                    Button(
                                        onClick = {
                                            onUnbanDevice(device.deviceId)
                                            onUnbanIp(device.ipAddress)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = BadgeGreen),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Text("Lift Ban", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            onBanDevice(device.deviceId, "Admin Telemetry Action")
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.height(34.dp).testTag("ban_device_${device.deviceId}")
                                    ) {
                                        Text("Block Device", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    OutlinedButton(
                                        onClick = {
                                            onBanIp(device.ipAddress, "Admin IP Blacklist Action")
                                        },
                                        shape = RoundedCornerShape(6.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Text("Block IP", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // Blacklist Records Section
                if (bannedRecords.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Blacklist Enforcement Records (${bannedRecords.size})",
                            color = NetflixWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    items(bannedRecords, key = { it.targetKey }) { record ->
                        Surface(
                            color = DangerRed.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${record.targetType}: ${record.targetKey}",
                                        color = DangerRed,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${record.reason} • ${dateFormat.format(Date(record.bannedAt))}",
                                        color = NetflixLightGrey,
                                        fontSize = 11.sp
                                    )
                                }

                                TextButton(
                                    onClick = {
                                        if (record.targetType == "DEVICE") onUnbanDevice(record.targetKey)
                                        else onUnbanIp(record.targetKey)
                                    }
                                ) {
                                    Text("Unban", color = BadgeGreen, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
