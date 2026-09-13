package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.DownloadEntity
import com.example.data.local.ReviewEntity
import com.example.data.model.EpisodeData
import com.example.data.model.MediaItem
import com.example.data.model.MediaType
import com.example.data.model.UserProfile
import com.example.ui.components.AppUserAvatar
import com.example.ui.components.MediaPosterCard
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaDetailScreen(
    media: MediaItem,
    allMedia: List<MediaItem>,
    isFavorite: Boolean,
    downloads: List<DownloadEntity> = emptyList(),
    reviews: List<ReviewEntity> = emptyList(),
    currentUser: UserProfile,
    onBackClick: () -> Unit,
    onPlayMovie: (MediaItem) -> Unit,
    onPlayEpisode: (MediaItem, EpisodeData) -> Unit,
    onDownloadClick: (MediaItem, EpisodeData?) -> Unit,
    onDeleteDownload: (String) -> Unit = {},
    onSubmitReview: (rating: Int, reviewText: String) -> Unit = { _, _ -> },
    onDeleteReview: (String) -> Unit = {},
    onSignInWithGoogle: () -> Unit = {},
    onMyListToggle: () -> Unit,
    onSelectRecommended: (MediaItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSeasonIndex by remember { mutableIntStateOf(0) }
    var isSeasonDropdownExpanded by remember { mutableStateOf(false) }
    var downloadToast by remember { mutableStateOf<String?>(null) }

    // Review Submission State
    var userRating by remember { mutableIntStateOf(5) }
    var reviewInputText by remember { mutableStateOf("") }
    var isSubmittingReview by remember { mutableStateOf(false) }

    val isMovieDownloaded = remember(downloads, media.id) {
        downloads.any { it.id == media.id }
    }

    val currentSeason = remember(media, selectedSeasonIndex) {
        if (media.type == MediaType.SERIES && media.seasons.isNotEmpty()) {
            media.seasons.getOrElse(selectedSeasonIndex) { media.seasons.first() }
        } else null
    }

    val recommendedItems = remember(media, allMedia) {
        allMedia.filter { it.id != media.id && (it.category == media.category || it.type == media.type) }.take(6)
    }

    val avgRating = remember(reviews) {
        if (reviews.isNotEmpty()) {
            reviews.map { it.rating }.average()
        } else 4.8
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NetflixBlack)
            .testTag("media_detail_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 70.dp)
        ) {
            // Big Hero Backdrop
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                ) {
                    AsyncImage(
                        model = media.bannerUrl,
                        contentDescription = media.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Gradient overlays
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.6f),
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.8f),
                                        NetflixBlack
                                    )
                                )
                            )
                    )

                    // Center Big Play Icon
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .align(Alignment.Center)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                            .border(2.dp, Color.White, CircleShape)
                            .clickable {
                                if (media.type == MediaType.SERIES && currentSeason?.episodes?.isNotEmpty() == true) {
                                    onPlayEpisode(media, currentSeason.episodes.first())
                                } else {
                                    onPlayMovie(media)
                                }
                            }
                            .testTag("detail_backdrop_play_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    // Back button at top
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .statusBarsPadding()
                            .padding(start = 12.dp, top = 8.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                            .testTag("detail_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                }
            }

            // Title & Meta Info
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = media.title,
                        color = NetflixWhite,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Rating and Metrics row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // User Star Rating Summary
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF2E2405))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                .testTag("star_rating_summary")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Rating",
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = String.format(Locale.US, "%.1f", avgRating),
                                color = Color(0xFFFFD54F),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "(${reviews.size})",
                                color = NetflixLightGrey,
                                fontSize = 11.sp
                            )
                        }

                        Text(
                            text = "${media.matchPercentage}% Match",
                            color = BadgeGreen,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = media.releaseYear.toString(),
                            color = NetflixLightGrey,
                            fontSize = 13.sp
                        )

                        Text(
                            text = media.rating,
                            color = NetflixWhite.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                                .padding(horizontal = 5.dp, vertical = 1.5.dp)
                        )

                        Text(
                            text = media.durationText,
                            color = NetflixLightGrey,
                            fontSize = 13.sp
                        )

                        // 4K Ultra HD Badge
                        Text(
                            text = "4K Ultra HD",
                            color = NetflixLightGrey,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .border(1.dp, NetflixLightGrey.copy(alpha = 0.5f), RoundedCornerShape(3.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Primary Play Button (Full Width)
                    Button(
                        onClick = {
                            if (media.type == MediaType.SERIES && currentSeason?.episodes?.isNotEmpty() == true) {
                                onPlayEpisode(media, currentSeason.episodes.first())
                            } else {
                                onPlayMovie(media)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("detail_primary_play_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (media.type == MediaType.SERIES) "Resume Watching" else "Play",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Secondary Download Button (Full Width with dynamic downloaded state)
                    Button(
                        onClick = {
                            if (isMovieDownloaded) {
                                onDeleteDownload(media.id)
                                downloadToast = "Removed '${media.title}' from offline downloads"
                            } else {
                                onDownloadClick(media, null)
                                downloadToast = "Downloaded '${media.title}' for offline playback"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isMovieDownloaded) BadgeGreen.copy(alpha = 0.2f) else NetflixCardSurface,
                            contentColor = if (isMovieDownloaded) BadgeGreen else NetflixWhite
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("detail_primary_download_button")
                    ) {
                        Icon(
                            imageVector = if (isMovieDownloaded) Icons.Default.CheckCircle else Icons.Default.Download,
                            contentDescription = null,
                            tint = if (isMovieDownloaded) BadgeGreen else NetflixWhite,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isMovieDownloaded) "Downloaded (1.4 GB) • Tap to Remove" else "Download (1.4 GB)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Synopsis / Description
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = media.description,
                        color = NetflixWhite.copy(alpha = 0.9f),
                        fontSize = 14.sp,
                        lineHeight = 21.sp
                    )

                    // Cast & Genres
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Starring: " + media.cast.joinToString(", "),
                        color = NetflixLightGrey,
                        fontSize = 12.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Genres: " + media.genres.joinToString(", "),
                        color = NetflixLightGrey,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Action Icons Bar (My List, Rate, Share)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable(onClick = onMyListToggle)
                                .padding(8.dp)
                                .testTag("detail_my_list_toggle")
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Check else Icons.Default.Add,
                                contentDescription = "My List",
                                tint = if (isFavorite) BadgeGreen else NetflixWhite,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isFavorite) "In My List" else "My List",
                                color = NetflixLightGrey,
                                fontSize = 11.sp
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { downloadToast = "Marked as Favorite title!" }
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ThumbUp,
                                contentDescription = "Liked",
                                tint = NetflixWhite,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Liked",
                                color = NetflixLightGrey,
                                fontSize = 11.sp
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { downloadToast = "Share link copied to clipboard!" }
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = NetflixWhite,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Share",
                                color = NetflixLightGrey,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Divider(
                        color = NetflixCardBorder,
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            }

            // For TV Series: Episodic Structure & Season Selector
            if (media.type == MediaType.SERIES && media.seasons.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        // Season Selector Dropdown
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(NetflixCardSurface)
                                .border(1.dp, NetflixCardBorder, RoundedCornerShape(8.dp))
                                .clickable { isSeasonDropdownExpanded = true }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                                .testTag("season_picker_dropdown")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = currentSeason?.title ?: "Season 1",
                                    color = NetflixWhite,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Select Season",
                                    tint = NetflixWhite
                                )
                            }

                            DropdownMenu(
                                expanded = isSeasonDropdownExpanded,
                                onDismissRequest = { isSeasonDropdownExpanded = false },
                                modifier = Modifier.background(NetflixCardSurface)
                            ) {
                                media.seasons.forEachIndexed { index, season ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = "${season.title} (${season.episodes.size} Episodes)",
                                                color = if (index == selectedSeasonIndex) NetflixRed else NetflixWhite,
                                                fontWeight = if (index == selectedSeasonIndex) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        onClick = {
                                            selectedSeasonIndex = index
                                            isSeasonDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                    }
                }

                // Episodes List with offline download tracking
                currentSeason?.let { season ->
                    items(season.episodes, key = { it.id }) { episode ->
                        val isEpDownloaded = downloads.any { it.id == "${media.id}_${episode.id}" }
                        EpisodeListItem(
                            episode = episode,
                            isDownloaded = isEpDownloaded,
                            onPlayClick = { onPlayEpisode(media, episode) },
                            onDownloadClick = {
                                if (isEpDownloaded) {
                                    onDeleteDownload("${media.id}_${episode.id}")
                                    downloadToast = "Removed Episode ${episode.episodeNumber} from downloads"
                                } else {
                                    onDownloadClick(media, episode)
                                    downloadToast = "Downloaded Episode ${episode.episodeNumber} (450 MB)"
                                }
                            }
                        )
                    }
                }
            }

            // Ratings & Reviews Section
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                        .testTag("ratings_and_reviews_section")
                ) {
                    Text(
                        text = "Ratings & Reviews",
                        color = NetflixWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Rating Breakdown Card
                    Surface(
                        color = NetflixCardSurface,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = String.format(Locale.US, "%.1f", avgRating),
                                        color = NetflixWhite,
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Row {
                                        repeat(5) { i ->
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = null,
                                                tint = if (i < avgRating.toInt()) Color(0xFFFFC107) else NetflixLightGrey.copy(alpha = 0.4f),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${reviews.size} reviews",
                                        color = NetflixLightGrey,
                                        fontSize = 11.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(20.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Audience Consensus",
                                        color = NetflixWhite,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Acclaimed by StreamFlix subscribers for high production value, riveting narrative, and stellar cinematography.",
                                        color = NetflixLightGrey,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    )
                                }
                            }

                            Divider(
                                color = NetflixCardBorder,
                                thickness = 1.dp,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )

                            // Submit Review Form
                            if (currentUser.isGuest) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.Black.copy(alpha = 0.4f))
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = "Want to rate or review this title?",
                                        color = NetflixWhite,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Sign in to submit your 1-5 star rating and review.",
                                        color = NetflixLightGrey,
                                        fontSize = 11.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = onSignInWithGoogle,
                                        colors = ButtonDefaults.buttonColors(containerColor = NetflixRed),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.testTag("signin_to_review_button")
                                    ) {
                                        Text("Sign In to Post Review", fontSize = 12.sp)
                                    }
                                }
                            } else {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = "Leave a Rating & Review",
                                        color = NetflixWhite,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Signed in as: ${currentUser.displayName} (${currentUser.email})",
                                        color = AccentGlow,
                                        fontSize = 11.sp
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Interactive 5-Star Picker
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        (1..5).forEach { starIndex ->
                                            IconButton(
                                                onClick = { userRating = starIndex },
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .testTag("star_picker_$starIndex")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = "Rate $starIndex stars",
                                                    tint = if (starIndex <= userRating) Color(0xFFFFC107) else Color.White.copy(alpha = 0.3f),
                                                    modifier = Modifier.size(28.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(6.dp))

                                        Text(
                                            text = when (userRating) {
                                                5 -> "Masterpiece! ★★★★★"
                                                4 -> "Great ★★★★☆"
                                                3 -> "Good ★★★☆☆"
                                                2 -> "Mediocre ★★☆☆☆"
                                                else -> "Terrible ★☆☆☆☆"
                                            },
                                            color = Color(0xFFFFD54F),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedTextField(
                                        value = reviewInputText,
                                        onValueChange = { reviewInputText = it },
                                        placeholder = { Text("Write a short review about the plot, cast, or pacing...", color = NetflixLightGrey, fontSize = 12.sp) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = NetflixWhite,
                                            unfocusedTextColor = NetflixWhite,
                                            focusedContainerColor = Color.Black.copy(alpha = 0.5f),
                                            unfocusedContainerColor = Color.Black.copy(alpha = 0.5f),
                                            focusedBorderColor = NetflixRed,
                                            unfocusedBorderColor = NetflixCardBorder
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("review_input_field")
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Button(
                                        onClick = {
                                            if (reviewInputText.isNotBlank()) {
                                                onSubmitReview(userRating, reviewInputText)
                                                downloadToast = "Review posted successfully!"
                                                reviewInputText = ""
                                            }
                                        },
                                        enabled = reviewInputText.isNotBlank(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = NetflixRed,
                                            disabledContainerColor = NetflixRed.copy(alpha = 0.4f)
                                        ),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier
                                            .align(Alignment.End)
                                            .testTag("submit_review_button")
                                    ) {
                                        Text("Submit Review", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Reviews List
                    if (reviews.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Recent Audience Reviews (${reviews.size})",
                            color = NetflixWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        reviews.forEach { rev ->
                            Surface(
                                color = NetflixCardSurface.copy(alpha = 0.7f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .testTag("review_card_${rev.id}")
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.White)
                                                    .border(1.dp, NetflixCardBorder, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                AppUserAvatar(
                                                    avatarUrl = rev.userAvatarUrl,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = rev.userName,
                                                        color = NetflixWhite,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    if (rev.userEmail.equals("n4062226@gmail.com", ignoreCase = true)) {
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(
                                                            text = "ADMIN",
                                                            color = NetflixRed,
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Black,
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(3.dp))
                                                                .background(NetflixRed.copy(alpha = 0.2f))
                                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                                        )
                                                    }
                                                }
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    repeat(rev.rating) {
                                                        Icon(
                                                            imageVector = Icons.Default.Star,
                                                            contentDescription = null,
                                                            tint = Color(0xFFFFC107),
                                                            modifier = Modifier.size(12.dp)
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    val dateStr = SimpleDateFormat("MMM d, yyyy", Locale.US).format(Date(rev.timestamp))
                                                    Text(
                                                        text = dateStr,
                                                        color = NetflixLightGrey,
                                                        fontSize = 10.sp
                                                    )
                                                }
                                            }
                                        }

                                        // Delete option if author or admin
                                        if (rev.userEmail == currentUser.email || currentUser.isAdmin) {
                                            IconButton(
                                                onClick = { onDeleteReview(rev.id) },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete review",
                                                    tint = NetflixLightGrey,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = rev.reviewText,
                                        color = NetflixWhite.copy(alpha = 0.9f),
                                        fontSize = 12.sp,
                                        lineHeight = 17.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // "More Like This" Recommendations
            if (recommendedItems.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "More Like This",
                            color = NetflixWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        recommendedItems.chunked(3).forEach { rowItems ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                rowItems.forEach { item ->
                                    MediaPosterCard(
                                        media = item,
                                        onClick = { onSelectRecommended(item) },
                                        modifier = Modifier.weight(1f),
                                        height = 160.dp
                                    )
                                }
                                repeat(3 - rowItems.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Notification Toast
        downloadToast?.let { msg ->
            Surface(
                color = AccentBlue.copy(alpha = 0.95f),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
            ) {
                Text(
                    text = msg,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
                )
            }
            LaunchedEffect(msg) {
                kotlinx.coroutines.delay(2500)
                downloadToast = null
            }
        }
    }
}

@Composable
fun EpisodeListItem(
    episode: EpisodeData,
    isDownloaded: Boolean = false,
    onPlayClick: () -> Unit,
    onDownloadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onPlayClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag("episode_item_${episode.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Episode Thumbnail with Play icon
            Box(
                modifier = Modifier
                    .size(width = 110.dp, height = 65.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(NetflixCardSurface)
            ) {
                AsyncImage(
                    model = episode.thumbnailUrl,
                    contentDescription = episode.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .align(Alignment.Center)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.7f))
                        .border(1.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play Episode",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Title and duration
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${episode.episodeNumber}. ${episode.title}",
                    color = NetflixWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${episode.durationMinutes}m",
                        color = NetflixLightGrey,
                        fontSize = 12.sp
                    )
                    if (isDownloaded) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Downloaded",
                            color = BadgeGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Download Icon (shows CheckCircle if already downloaded)
            IconButton(
                onClick = onDownloadClick,
                modifier = Modifier.testTag("download_episode_${episode.id}")
            ) {
                Icon(
                    imageVector = if (isDownloaded) Icons.Default.CheckCircle else Icons.Default.Download,
                    contentDescription = if (isDownloaded) "Downloaded Episode" else "Download Episode",
                    tint = if (isDownloaded) BadgeGreen else NetflixWhite,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // Synopsis
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = episode.overview,
            color = NetflixLightGrey,
            fontSize = 12.sp,
            lineHeight = 17.sp,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
    }
}
