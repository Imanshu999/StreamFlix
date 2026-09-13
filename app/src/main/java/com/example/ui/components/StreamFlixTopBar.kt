package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.*

@Composable
fun StreamFlixTopBar(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    userAvatarUrl: String,
    isGuest: Boolean,
    isAdmin: Boolean = false,
    onProfileClick: () -> Unit,
    onAdminClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isSearchExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.65f),
                        Color.Black.copy(alpha = 0.25f),
                        Color.Transparent
                    )
                )
            )
            .statusBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Brand Logo: Styled Text ("Stream" in Yellow + "Flix" in Red, No Icon)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.testTag("brand_logo_row")
            ) {
                Text(
                    text = "Stream",
                    color = Color(0xFFFFD700), // Vibrant Yellow (#FFD700)
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Flix",
                    color = NetflixRed, // Vibrant Red (#E50914)
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
            }

            // Action Icons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Search toggle
                IconButton(
                    onClick = { isSearchExpanded = !isSearchExpanded },
                    modifier = Modifier.testTag("search_toggle_button")
                ) {
                    Icon(
                        imageVector = if (isSearchExpanded) Icons.Default.Close else Icons.Default.Search,
                        contentDescription = "Search",
                        tint = NetflixWhite
                    )
                }

                // Admin direct access button - STRICTLY HIDDEN FOR NON-ADMINS
                if (isAdmin) {
                    IconButton(
                        onClick = onAdminClick,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(AccentBlue.copy(alpha = 0.2f))
                            .testTag("admin_header_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "Admin CMS",
                            tint = AccentGlow,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // User Avatar (Sleek Instagram-style minimal vector avatar)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.5.dp, NetflixCardBorder, CircleShape)
                        .clickable(onClick = onProfileClick)
                        .testTag("user_avatar_button"),
                    contentAlignment = Alignment.Center
                ) {
                    AppUserAvatar(
                        avatarUrl = userAvatarUrl,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // Expandable Search Bar
        if (isSearchExpanded || searchQuery.isNotBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Search by title, genre, or keywords...", color = NetflixLightGrey, fontSize = 13.sp) },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = NetflixRed,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear search",
                                tint = NetflixLightGrey,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = NetflixWhite,
                    unfocusedTextColor = NetflixWhite,
                    focusedContainerColor = NetflixCardSurface,
                    unfocusedContainerColor = NetflixCardSurface,
                    focusedBorderColor = NetflixRed,
                    unfocusedBorderColor = NetflixCardBorder
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_input_field")
            )

            // Quick search suggestions chips
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Action", "Sci-Fi", "Crime", "Thriller", "Drama").forEach { genreKeyword ->
                    val isCurrent = searchQuery.equals(genreKeyword, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isCurrent) NetflixRed.copy(alpha = 0.3f) else NetflixCardSurface.copy(alpha = 0.6f))
                            .clickable {
                                if (isCurrent) onSearchQueryChange("") else onSearchQueryChange(genreKeyword)
                            }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "#$genreKeyword",
                            color = if (isCurrent) NetflixRed else NetflixLightGrey,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Category Filter Chips (Tightly aligned near top, translucent aesthetic)
        Spacer(modifier = Modifier.height(6.dp))
        val categories = listOf("All", "TV Shows", "Movies", "Trending", "My List")
        val categoryScrollState = rememberScrollState()

        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(categoryScrollState)
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                categories.forEach { category ->
                    val isSelected = selectedCategory == category
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) NetflixRed else Color.Black.copy(alpha = 0.45f)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) NetflixRed else Color.White.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable { onCategorySelected(category) }
                            .padding(horizontal = 14.dp, vertical = 5.dp)
                            .testTag("category_chip_$category")
                    ) {
                        Text(
                            text = category,
                            color = if (isSelected) Color.White else NetflixWhite.copy(alpha = 0.85f),
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
                // Generous end padding spacer so "My List" never overflows or clips
                Spacer(modifier = Modifier.width(28.dp))
            }
        }
    }
}
