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
import com.example.data.local.DownloadEntity
import com.example.ui.theme.*

@Composable
fun DownloadsScreen(
    downloads: List<DownloadEntity>,
    isOfflineModeActive: Boolean = false,
    onToggleOfflineMode: (Boolean) -> Unit = {},
    onPlayDownload: (DownloadEntity) -> Unit,
    onDeleteDownload: (String) -> Unit,
    onDeleteAllDownloads: () -> Unit = {},
    onFindSomethingToDownload: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("All") }
    var showDeleteAllDialog by remember { mutableStateOf(false) }

    val totalBytes = downloads.sumOf { it.fileSizeBytes }
    val totalMb = (totalBytes / (1024 * 1024)).coerceAtLeast(0)

    val filteredDownloads = remember(downloads, selectedFilter) {
        when (selectedFilter) {
            "Movies" -> downloads.filter { it.episodeId == null }
            "TV Shows" -> downloads.filter { it.episodeId != null }
            else -> downloads
        }
    }

    val moviesCount = remember(downloads) { downloads.count { it.episodeId == null } }
    val seriesCount = remember(downloads) { downloads.count { it.episodeId != null } }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NetflixBlack)
            .statusBarsPadding()
            .testTag("downloads_screen_root")
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Offline Downloads",
                    color = NetflixWhite,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "${downloads.size} titles ready for offline viewing",
                    color = NetflixLightGrey,
                    fontSize = 12.sp
                )
            }

            if (downloads.isNotEmpty()) {
                TextButton(
                    onClick = { showDeleteAllDialog = true },
                    modifier = Modifier.testTag("clear_all_downloads_button")
                ) {
                    Text("Clear All", color = NetflixRed, fontSize = 13.sp)
                }
            }
        }

        // Offline Simulation Toggle Banner
        Surface(
            color = if (isOfflineModeActive) Color(0xFF261D05) else NetflixCardSurface,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (isOfflineModeActive) Icons.Default.AirplanemodeActive else Icons.Default.CloudDone,
                        contentDescription = null,
                        tint = if (isOfflineModeActive) Color(0xFFFFB300) else BadgeGreen,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isOfflineModeActive) "Offline Mode (Airplane Mode)" else "Offline Playback Ready",
                            color = NetflixWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (isOfflineModeActive) "Network disconnected. Playing from local encrypted vault." else "Downloads are secured & stored locally on device.",
                            color = NetflixLightGrey,
                            fontSize = 11.sp
                        )
                    }
                }

                Switch(
                    checked = isOfflineModeActive,
                    onCheckedChange = onToggleOfflineMode,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFFFFB300),
                        checkedTrackColor = Color(0xFF5C3F00),
                        uncheckedThumbColor = NetflixLightGrey,
                        uncheckedTrackColor = NetflixCardBorder
                    ),
                    modifier = Modifier.testTag("offline_mode_toggle")
                )
            }
        }

        // Storage Usage Card
        Surface(
            color = NetflixCardSurface,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = AccentGlow,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Secure Vault Storage",
                            color = NetflixWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = "$totalMb MB Used",
                        color = AccentGlow,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { (totalMb.toFloat() / 15000f).coerceIn(0.04f, 1f) },
                    color = AccentBlue,
                    trackColor = Color.White.copy(alpha = 0.1f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Smart Downloads: ON (Auto-deletes watched episodes)",
                        color = NetflixLightGrey,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "48.2 GB Free",
                        color = NetflixLightGrey,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Filter chips (All, Movies, TV Shows)
        if (downloads.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "All" to downloads.size,
                    "Movies" to moviesCount,
                    "TV Shows" to seriesCount
                ).forEach { (filterName, count) ->
                    val isSelected = selectedFilter == filterName
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) NetflixRed else NetflixCardSurface)
                            .clickable { selectedFilter = filterName }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("filter_chip_$filterName")
                    ) {
                        Text(
                            text = "$filterName ($count)",
                            color = if (isSelected) Color.White else NetflixLightGrey,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        if (downloads.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(NetflixCardSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            tint = NetflixLightGrey,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Never Be Without StreamFlix",
                        color = NetflixWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Download movies and TV series so you can watch offline anywhere—on a flight, commute, or off-grid retreat.",
                        color = NetflixLightGrey,
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = onFindSomethingToDownload,
                        colors = ButtonDefaults.buttonColors(containerColor = NetflixRed),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("find_downloads_button")
                    ) {
                        Text("Explore Catalog to Download", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredDownloads, key = { it.id }) { item ->
                    Surface(
                        color = NetflixCardSurface,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPlayDownload(item) }
                            .testTag("download_item_${item.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(width = 95.dp, height = 60.dp)
                                    .clip(RoundedCornerShape(6.dp))
                            ) {
                                AsyncImage(
                                    model = item.posterUrl,
                                    contentDescription = item.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )

                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .align(Alignment.Center)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.6f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Play offline",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

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

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = BadgeGreen,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "${item.fileSizeBytes / (1024 * 1024)} MB • Ready Offline",
                                        color = NetflixLightGrey,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            IconButton(
                                onClick = { onDeleteDownload(item.id) },
                                modifier = Modifier.testTag("delete_download_${item.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete download",
                                    tint = NetflixLightGrey,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete All Confirmation Dialog
    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = { Text("Delete All Downloads?", color = NetflixWhite) },
            text = {
                Text(
                    text = "This will remove all downloaded movies and episodes from your local device storage. You will need an internet connection to stream or download them again.",
                    color = NetflixLightGrey
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteAllDownloads()
                        showDeleteAllDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = NetflixRed)
                ) {
                    Text("Delete All", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteAllDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = NetflixWhite)
                ) {
                    Text("Cancel")
                }
            },
            containerColor = NetflixCardSurface
        )
    }
}
