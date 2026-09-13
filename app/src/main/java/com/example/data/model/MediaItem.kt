package com.example.data.model

enum class MediaType {
    MOVIE,
    SERIES
}

data class EpisodeData(
    val id: String,
    val episodeNumber: Int,
    val title: String,
    val overview: String,
    val thumbnailUrl: String,
    val durationMinutes: Int,
    val streamUrl: String
)

data class SeasonData(
    val seasonNumber: Int,
    val title: String,
    val episodes: List<EpisodeData>
)

data class MediaItem(
    val id: String,
    val title: String,
    val description: String,
    val bannerUrl: String,
    val posterUrl: String,
    val type: MediaType = MediaType.MOVIE,
    val category: String = "Trending Now",
    val genres: List<String> = listOf("Action", "Drama"),
    val releaseYear: Int = 2024,
    val rating: String = "TV-MA",
    val matchPercentage: Int = 98,
    val durationText: String = "2h 15m",
    val directStreamUrl: String = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
    val isFeatured: Boolean = false,
    val cast: List<String> = listOf("Alex Turner", "Elena Rostova", "Marcus Vance"),
    val seasons: List<SeasonData> = emptyList(),
    val isTop10: Boolean = false,
    val badgeLabel: String? = null
)
