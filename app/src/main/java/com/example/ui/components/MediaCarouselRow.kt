package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.WatchHistoryEntity
import com.example.data.model.MediaItem
import com.example.ui.theme.NetflixWhite

@Composable
fun MediaCarouselRow(
    title: String,
    items: List<MediaItem>,
    onItemClick: (MediaItem) -> Unit,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = NetflixWhite,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.3.sp
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("carousel_${title.replace(" ", "_").lowercase()}")
        ) {
            items(items, key = { it.id }) { media ->
                MediaPosterCard(
                    media = media,
                    onClick = { onItemClick(media) }
                )
            }
        }
    }
}

@Composable
fun ContinueWatchingCarousel(
    title: String,
    historyItems: List<WatchHistoryEntity>,
    onItemClick: (WatchHistoryEntity) -> Unit,
    onResumeClick: (WatchHistoryEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    if (historyItems.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = NetflixWhite,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.3.sp
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("continue_watching_carousel")
        ) {
            items(historyItems, key = { it.id }) { item ->
                ContinueWatchingCard(
                    historyItem = item,
                    onClick = { onItemClick(item) },
                    onPlayClick = { onResumeClick(item) }
                )
            }
        }
    }
}
