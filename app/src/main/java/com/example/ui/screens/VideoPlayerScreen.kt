package com.example.ui.screens

import android.app.Activity
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.media.MediaPlayer
import android.net.Uri
import android.widget.FrameLayout
import android.widget.VideoView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.example.data.model.EpisodeData
import com.example.data.model.MediaItem
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun VideoPlayerScreen(
    media: MediaItem,
    episode: EpisodeData? = null,
    initialPositionMs: Long = 0L,
    onBackClick: () -> Unit,
    onProgressUpdate: (currentMs: Long, durationMs: Long) -> Unit,
    onNextEpisode: (() -> Unit)? = null,
    onEpisodeSelect: ((EpisodeData) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Auto-Rotation: Switch to Landscape on Play, revert to Portrait on Exit
    val activity = remember(context) {
        var ctx = context
        while (ctx is ContextWrapper) {
            if (ctx is Activity) return@remember ctx
            ctx = ctx.baseContext
        }
        null
    }

    DisposableEffect(activity) {
        val originalOrientation = activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    var videoViewRef by remember { mutableStateOf<VideoView?>(null) }
    var currentEpisode by remember(episode) { mutableStateOf(episode) }
    var isEpisodeDrawerOpen by remember { mutableStateOf(false) }
    var selectedSeasonIndex by remember { mutableIntStateOf(0) }

    var isPlaying by remember { mutableStateOf(true) }
    var isBuffering by remember { mutableStateOf(true) }
    var currentPositionMs by remember { mutableLongStateOf(initialPositionMs) }
    var totalDurationMs by remember { mutableLongStateOf(0L) }
    var showControls by remember { mutableStateOf(true) }
    var isLocked by remember { mutableStateOf(false) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }
    var showSpeedDialog by remember { mutableStateOf(false) }
    var showQualityDialog by remember { mutableStateOf(false) }
    var selectedQuality by remember { mutableStateOf("Auto (1080p)") }

    val streamUrl = currentEpisode?.streamUrl ?: media.directStreamUrl
    val titleText = if (currentEpisode != null) "${media.title} • E${currentEpisode?.episodeNumber}: ${currentEpisode?.title}" else media.title

    // Auto-hide HUD after 4 seconds of idle
    LaunchedEffect(showControls, isPlaying) {
        if (showControls && isPlaying) {
            delay(4000)
            showControls = false
        }
    }

    // Polling playback position loop
    LaunchedEffect(videoViewRef) {
        while (true) {
            videoViewRef?.let { vv ->
                try {
                    if (vv.isPlaying) {
                        currentPositionMs = vv.currentPosition.toLong()
                        totalDurationMs = vv.duration.toLong().coerceAtLeast(1L)
                        onProgressUpdate(currentPositionMs, totalDurationMs)
                    }
                } catch (_: Exception) {}
            }
            delay(1000)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                showControls = !showControls
            }
            .testTag("video_player_container")
    ) {
        // Native Video View Embedded
        AndroidView(
            factory = { ctx ->
                VideoView(ctx).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                    setVideoURI(Uri.parse(streamUrl))
                    setOnPreparedListener { mp ->
                        isBuffering = false
                        totalDurationMs = duration.toLong()
                        if (initialPositionMs > 0) {
                            seekTo(initialPositionMs.toInt())
                        }
                        start()
                        isPlaying = true
                        try {
                            mp.playbackParams = mp.playbackParams.setSpeed(playbackSpeed)
                        } catch (_: Exception) {}
                    }
                    setOnCompletionListener {
                        isPlaying = false
                        showControls = true
                        onProgressUpdate(duration.toLong(), duration.toLong())
                    }
                    setOnErrorListener { _, _, _ ->
                        isBuffering = false
                        true
                    }
                    videoViewRef = this
                }
            },
            update = { vv ->
                videoViewRef = vv
            },
            modifier = Modifier.fillMaxSize()
        )

        // Buffering Spinner
        if (isBuffering) {
            CircularProgressIndicator(
                color = NetflixRed,
                strokeWidth = 3.dp,
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.Center)
            )
        }

        // Overlay Netflix HUD Controls
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
            ) {
                // Top Bar: Back, Title, Quality, Speed, Lock
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            videoViewRef?.let { vv ->
                                onProgressUpdate(vv.currentPosition.toLong(), vv.duration.toLong())
                            }
                            onBackClick()
                        },
                        modifier = Modifier.testTag("player_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = titleText,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )

                    // Lock button
                    IconButton(
                        onClick = { isLocked = !isLocked },
                        modifier = Modifier.testTag("player_lock_button")
                    ) {
                        Icon(
                            imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = "Lock",
                            tint = if (isLocked) NetflixRed else Color.White
                        )
                    }

                    if (!isLocked) {
                        // Quality button
                        TextButton(
                            onClick = { showQualityDialog = true },
                            modifier = Modifier.testTag("player_quality_button")
                        ) {
                            Text(
                                text = selectedQuality.split(" ").first(),
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Speed button
                        TextButton(
                            onClick = { showSpeedDialog = true },
                            modifier = Modifier.testTag("player_speed_button")
                        ) {
                            Text(
                                text = "${playbackSpeed}x",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (!isLocked) {
                    // Center Controls: Rewind 10s, Play/Pause, Forward 10s
                    Row(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth(0.75f),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Rewind 10s
                        IconButton(
                            onClick = {
                                videoViewRef?.let { vv ->
                                    val newPos = (vv.currentPosition - 10000).coerceAtLeast(0)
                                    vv.seekTo(newPos)
                                    currentPositionMs = newPos.toLong()
                                }
                            },
                            modifier = Modifier
                                .size(50.dp)
                                .testTag("player_rewind_10s")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Replay10,
                                contentDescription = "Rewind 10 seconds",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        // Play/Pause Center Pill
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                                .clickable {
                                    videoViewRef?.let { vv ->
                                        if (vv.isPlaying) {
                                            vv.pause()
                                            isPlaying = false
                                        } else {
                                            vv.start()
                                            isPlaying = true
                                        }
                                    }
                                }
                                .testTag("player_play_pause_toggle"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = Color.White,
                                modifier = Modifier.size(44.dp)
                            )
                        }

                        // Forward 10s
                        IconButton(
                            onClick = {
                                videoViewRef?.let { vv ->
                                    val newPos = (vv.currentPosition + 10000).coerceAtMost(vv.duration)
                                    vv.seekTo(newPos)
                                    currentPositionMs = newPos.toLong()
                                }
                            },
                            modifier = Modifier
                                .size(50.dp)
                                .testTag("player_forward_10s")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Forward10,
                                contentDescription = "Forward 10 seconds",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    // Bottom Bar: Slider Scrub bar, Time Elapsed / Remaining, Next Episode
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        // Time Labels Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = formatTime(currentPositionMs),
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Text(
                                text = "-" + formatTime((totalDurationMs - currentPositionMs).coerceAtLeast(0L)),
                                color = NetflixLightGrey,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Scrub Slider
                        val sliderValue = if (totalDurationMs > 0) {
                            (currentPositionMs.toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f)
                        } else 0f

                        Slider(
                            value = sliderValue,
                            onValueChange = { frac ->
                                val target = (frac * totalDurationMs).toLong()
                                currentPositionMs = target
                            },
                            onValueChangeFinished = {
                                videoViewRef?.seekTo(currentPositionMs.toInt())
                            },
                            colors = SliderDefaults.colors(
                                thumbColor = NetflixRed,
                                activeTrackColor = NetflixRed,
                                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("player_scrub_slider")
                        )

                        // Bottom Actions: Audio & Subtitles, Next Episode
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable { /* Audio & Subs modal */ }
                                    .padding(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Subtitles,
                                    contentDescription = "Audio and Subtitles",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Audio & Subtitles",
                                    color = Color.White,
                                    fontSize = 12.sp
                                )
                            }

                            if (onNextEpisode != null) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clickable(onClick = onNextEpisode)
                                        .padding(4.dp)
                                        .testTag("player_next_episode_button")
                                ) {
                                    Text(
                                        text = "Next Episode",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.SkipNext,
                                        contentDescription = "Next Episode",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Playback Speed Dialog
        if (showSpeedDialog) {
            AlertDialog(
                onDismissRequest = { showSpeedDialog = false },
                title = { Text("Playback Speed", color = NetflixWhite) },
                text = {
                    Column {
                        listOf(0.75f, 1.0f, 1.25f, 1.5f).forEach { speed ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        playbackSpeed = speed
                                        videoViewRef?.let { vv ->
                                            try {
                                                val mpField = VideoView::class.java.getDeclaredField("mMediaPlayer")
                                                mpField.isAccessible = true
                                                val mp = mpField.get(vv) as? MediaPlayer
                                                mp?.let { p -> p.playbackParams = p.playbackParams.setSpeed(speed) }
                                            } catch (_: Exception) {}
                                        }
                                        showSpeedDialog = false
                                    }
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = playbackSpeed == speed,
                                    onClick = null,
                                    colors = RadioButtonDefaults.colors(selectedColor = NetflixRed)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (speed == 1.0f) "1.0x (Normal)" else "${speed}x",
                                    color = NetflixWhite,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showSpeedDialog = false }) {
                        Text("Done", color = NetflixRed)
                    }
                },
                containerColor = NetflixCardSurface
            )
        }

        // Video Quality Dialog
        if (showQualityDialog) {
            AlertDialog(
                onDismissRequest = { showQualityDialog = false },
                title = { Text("Stream Quality", color = NetflixWhite) },
                text = {
                    Column {
                        listOf("Auto (1080p)", "High (1080p HDR)", "Medium (720p)", "Data Saver (480p)").forEach { q ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedQuality = q
                                        showQualityDialog = false
                                    }
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedQuality == q,
                                    onClick = null,
                                    colors = RadioButtonDefaults.colors(selectedColor = NetflixRed)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = q,
                                    color = NetflixWhite,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showQualityDialog = false }) {
                        Text("Apply", color = NetflixRed)
                    }
                },
                containerColor = NetflixCardSurface
            )
        }

        // Sleek Toggleable Arrow Icon on the Left Side
        if (media.seasons.isNotEmpty() && !isLocked) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 12.dp)
            ) {
                Surface(
                    color = Color.Black.copy(alpha = 0.75f),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NetflixCardBorder),
                    modifier = Modifier
                        .clickable { isEpisodeDrawerOpen = !isEpisodeDrawerOpen }
                        .testTag("toggle_episode_drawer_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = if (isEpisodeDrawerOpen) Icons.AutoMirrored.Filled.ArrowBack else Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Toggle Episode Drawer",
                            tint = AccentGlow,
                            modifier = Modifier.size(18.dp)
                        )
                        if (showControls) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Episodes",
                                color = NetflixWhite,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Sleek Semi-Transparent Episode Drawer / Sidebar Panel
        if (isEpisodeDrawerOpen && media.seasons.isNotEmpty()) {
            // Dismiss Backdrop (tap outside to close drawer)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
                    .clickable { isEpisodeDrawerOpen = false }
            )

            // Sliding Panel
            AnimatedVisibility(
                visible = isEpisodeDrawerOpen,
                enter = slideInHorizontally(initialOffsetX = { -it }) + fadeIn(),
                exit = slideOutHorizontally(targetOffsetX = { -it }) + fadeOut(),
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(min = 280.dp, max = 360.dp)
                    .align(Alignment.CenterStart)
            ) {
                Surface(
                    color = NetflixBlack.copy(alpha = 0.95f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NetflixCardBorder),
                    modifier = Modifier
                        .fillMaxHeight()
                        .testTag("episode_selection_drawer")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Drawer Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Episodes",
                                    color = NetflixWhite,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = media.title,
                                    color = NetflixLightGrey,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            IconButton(
                                onClick = { isEpisodeDrawerOpen = false },
                                modifier = Modifier.testTag("close_episode_drawer_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close Drawer",
                                    tint = NetflixWhite
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Season Selector if multiple seasons
                        if (media.seasons.size > 1) {
                            ScrollableTabRow(
                                selectedTabIndex = selectedSeasonIndex.coerceIn(0, media.seasons.lastIndex),
                                containerColor = Color.Transparent,
                                contentColor = NetflixRed,
                                edgePadding = 0.dp,
                                divider = {}
                            ) {
                                media.seasons.forEachIndexed { sIdx, season ->
                                    Tab(
                                        selected = selectedSeasonIndex == sIdx,
                                        onClick = { selectedSeasonIndex = sIdx },
                                        text = {
                                            Text(
                                                text = "S${season.seasonNumber}",
                                                fontSize = 13.sp,
                                                fontWeight = if (selectedSeasonIndex == sIdx) FontWeight.Bold else FontWeight.Normal,
                                                color = if (selectedSeasonIndex == sIdx) NetflixWhite else NetflixLightGrey
                                            )
                                        }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        val activeSeason = media.seasons.getOrNull(selectedSeasonIndex.coerceIn(0, media.seasons.lastIndex)) ?: media.seasons.firstOrNull()

                        if (activeSeason == null || activeSeason.episodes.isEmpty()) {
                            Box(
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No episodes released for this season yet.",
                                    color = NetflixLightGrey,
                                    fontSize = 12.sp
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(activeSeason.episodes, key = { it.id }) { ep ->
                                    val isCurrentPlaying = currentEpisode?.id == ep.id || (currentEpisode == null && ep == activeSeason.episodes.firstOrNull())
                                    Surface(
                                        color = if (isCurrentPlaying) NetflixCardBorder.copy(alpha = 0.5f) else NetflixCardSurface,
                                        shape = RoundedCornerShape(8.dp),
                                        border = if (isCurrentPlaying) androidx.compose.foundation.BorderStroke(1.5.dp, NetflixRed) else null,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                currentEpisode = ep
                                                currentPositionMs = 0L
                                                isBuffering = true
                                                videoViewRef?.let { vv ->
                                                    vv.stopPlayback()
                                                    vv.setVideoURI(Uri.parse(ep.streamUrl))
                                                    vv.start()
                                                    isPlaying = true
                                                }
                                                onEpisodeSelect?.invoke(ep)
                                                isEpisodeDrawerOpen = false
                                            }
                                            .testTag("drawer_episode_${ep.episodeNumber}")
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(width = 60.dp, height = 40.dp)
                                                    .clip(RoundedCornerShape(4.dp))
                                            ) {
                                                AsyncImage(
                                                    model = ep.thumbnailUrl.ifEmpty { media.bannerUrl },
                                                    contentDescription = ep.title,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                                if (isCurrentPlaying) {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .background(Color.Black.copy(alpha = 0.45f)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.PlayArrow,
                                                            contentDescription = "Playing",
                                                            tint = NetflixRed,
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.width(10.dp))

                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "E${ep.episodeNumber}: ${ep.title}",
                                                        color = if (isCurrentPlaying) AccentGlow else NetflixWhite,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    Text(
                                                        text = "${ep.durationMinutes}m",
                                                        color = NetflixLightGrey,
                                                        fontSize = 10.sp
                                                    )
                                                }

                                                if (ep.overview.isNotEmpty()) {
                                                    Text(
                                                        text = ep.overview,
                                                        color = NetflixLightGrey,
                                                        fontSize = 10.sp,
                                                        maxLines = 2,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val hours = minutes / 60
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes % 60, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
