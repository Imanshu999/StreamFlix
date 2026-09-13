package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.example.data.local.WatchHistoryEntity
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    historyList: List<WatchHistoryEntity>,
    isCloudSynced: Boolean,
    userEmail: String,
    onResumeItem: (WatchHistoryEntity) -> Unit,
    onDeleteItem: (String) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NetflixBlack)
            .statusBarsPadding()
            .testTag("history_screen_root")
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Watch History",
                    color = NetflixWhite,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "${historyList.size} titles in your history",
                    color = NetflixLightGrey,
                    fontSize = 12.sp
                )
            }

            if (historyList.isNotEmpty()) {
                TextButton(
                    onClick = onClearAll,
                    colors = ButtonDefaults.textButtonColors(contentColor = NetflixLightGrey),
                    modifier = Modifier.testTag("clear_history_button")
                ) {
                    Text("Clear All")
                }
            }
        }

        // Cloud Sync Banner
        Surface(
            color = if (isCloudSynced) AccentBlue.copy(alpha = 0.15f) else NetflixCardSurface,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CloudDone,
                    contentDescription = null,
                    tint = if (isCloudSynced) AccentGlow else NetflixLightGrey,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isCloudSynced) "Firebase Cloud Sync Active • $userEmail" else "Guest Mode: Watch history saved locally on device",
                    color = if (isCloudSynced) AccentGlow else NetflixLightGrey,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (historyList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = NetflixLightGrey.copy(alpha = 0.4f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No Watch History Yet",
                        color = NetflixWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Titles you start watching will automatically appear here with your saved resume positions.",
                        color = NetflixLightGrey,
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(historyList, key = { it.id }) { item ->
                    HistoryItemCard(
                        item = item,
                        formattedDate = dateFormat.format(Date(item.lastWatchedTimestamp)),
                        onResume = { onResumeItem(item) },
                        onDelete = { onDeleteItem(item.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryItemCard(
    item: WatchHistoryEntity,
    formattedDate: String,
    onResume: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = NetflixCardSurface,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onResume)
            .testTag("history_item_${item.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail with Progress bar
            Box(
                modifier = Modifier
                    .size(width = 100.dp, height = 62.dp)
                    .clip(RoundedCornerShape(6.dp))
            ) {
                AsyncImage(
                    model = item.posterUrl,
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Play overlay
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .align(Alignment.Center)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Resume",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Progress bar
                LinearProgressIndicator(
                    progress = { item.progressPercentage.coerceIn(0.05f, 1f) },
                    color = NetflixRed,
                    trackColor = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .align(Alignment.BottomCenter)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    color = NetflixWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (item.episodeTitle != null) {
                    Text(
                        text = item.episodeTitle,
                        color = NetflixLightGrey,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${(item.progressPercentage * 100).toInt()}% watched",
                        color = if (item.isCompleted) BadgeGreen else NetflixRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "•",
                        color = NetflixLightGrey,
                        fontSize = 10.sp
                    )
                    Text(
                        text = formattedDate,
                        color = NetflixLightGrey,
                        fontSize = 11.sp
                    )
                }
            }

            // Remove action
            IconButton(
                onClick = onDelete,
                modifier = Modifier.testTag("delete_history_${item.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove from history",
                    tint = NetflixLightGrey,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
