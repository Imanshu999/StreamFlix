package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.WatchHistoryEntity
import com.example.data.model.MediaItem
import com.example.ui.theme.*

@Composable
fun MediaPosterCard(
    media: MediaItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    width: Dp = 125.dp,
    height: Dp = 185.dp
) {
    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, NetflixCardBorder.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
            .background(NetflixCardSurface)
            .clickable(onClick = onClick)
            .testTag("media_card_${media.id}")
    ) {
        AsyncImage(
            model = media.posterUrl,
            contentDescription = media.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Gradient subtle shadow at bottom
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                        startY = 300f
                    )
                )
        )

        // Top 10 or Category Badge
        if (media.isTop10) {
            Surface(
                color = NetflixRed,
                shape = RoundedCornerShape(topStart = 6.dp, bottomEnd = 6.dp),
                modifier = Modifier.align(Alignment.TopStart)
            ) {
                Text(
                    text = media.badgeLabel ?: "TOP 10",
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                )
            }
        }

        // Title at bottom of card
        Text(
            text = media.title,
            color = NetflixWhite,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 6.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun ContinueWatchingCard(
    historyItem: WatchHistoryEntity,
    onClick: () -> Unit,
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier,
    width: Dp = 140.dp,
    height: Dp = 195.dp
) {
    Column(
        modifier = modifier
            .width(width)
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, NetflixCardBorder, RoundedCornerShape(8.dp))
            .background(NetflixCardSurface)
            .clickable(onClick = onClick)
            .testTag("continue_watching_card_${historyItem.id}")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            AsyncImage(
                model = historyItem.posterUrl,
                contentDescription = historyItem.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Play overlay circle button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.Center)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.65f))
                    .border(1.5.dp, Color.White, CircleShape)
                    .clickable(onClick = onPlayClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Resume",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }

            // Progress bar at the bottom of the poster
            LinearProgressIndicator(
                progress = { historyItem.progressPercentage.coerceIn(0.05f, 1f) },
                color = NetflixRed,
                trackColor = Color.White.copy(alpha = 0.3f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.5.dp)
                    .align(Alignment.BottomCenter)
            )
        }

        // Info bar below image
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Text(
                text = historyItem.title,
                color = NetflixWhite,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (historyItem.episodeTitle != null) {
                Text(
                    text = historyItem.episodeTitle,
                    color = NetflixLightGrey,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                Text(
                    text = "${(historyItem.progressPercentage * 100).toInt()}% watched",
                    color = NetflixLightGrey,
                    fontSize = 10.sp
                )
            }
        }
    }
}
