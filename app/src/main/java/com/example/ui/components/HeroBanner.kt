package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.MediaItem
import com.example.ui.theme.*

@Composable
fun HeroBanner(
    media: MediaItem,
    isFavorite: Boolean,
    onPlayClick: () -> Unit,
    onMyListToggle: () -> Unit,
    onInfoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(440.dp)
            .clickable(onClick = onInfoClick)
            .testTag("hero_banner_container")
    ) {
        // Hero Backdrop Image
        AsyncImage(
            model = media.bannerUrl,
            contentDescription = media.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Gradient Shadows (Clean bottom fade into content background, no top shadow artifact)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.7f),
                            NetflixBlack
                        ),
                        startY = 0f,
                        endY = 1200f
                    )
                )
        )

        // Content Info Overlaid at bottom
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Badges row (TOP 10, Match %, Rating)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (media.isTop10) {
                    Surface(
                        color = NetflixRed,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = media.badgeLabel ?: "TOP 10",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = "${media.matchPercentage}% Match",
                    color = BadgeGreen,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = media.rating,
                    color = NetflixWhite.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                )

                Text(
                    text = media.durationText,
                    color = NetflixLightGrey,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Main Title
            Text(
                text = media.title,
                color = NetflixWhite,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Genre tags
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = media.genres.joinToString(" • "),
                color = NetflixLightGrey,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(14.dp))

            // CTA Buttons Row (Play, My List, Info)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // My List Button
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable(onClick = onMyListToggle)
                        .padding(8.dp)
                        .testTag("hero_my_list_button")
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Check else Icons.Default.Add,
                        contentDescription = "My List",
                        tint = if (isFavorite) BadgeGreen else NetflixWhite,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isFavorite) "Added" else "My List",
                        color = NetflixWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.width(18.dp))

                // Primary Play Button (Signature Netflix White pill)
                Button(
                    onClick = onPlayClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp),
                    modifier = Modifier.testTag("hero_play_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Play",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(18.dp))

                // Info Button
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable(onClick = onInfoClick)
                        .padding(8.dp)
                        .testTag("hero_info_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info",
                        tint = NetflixWhite,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Info",
                        color = NetflixWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
