package com.example.data.repository

import com.example.data.model.EpisodeData
import com.example.data.model.MediaItem
import com.example.data.model.MediaType
import com.example.data.model.SeasonData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class MediaRepository {

    private val _mediaItems = MutableStateFlow<List<MediaItem>>(getInitialCatalog())
    val mediaItems: StateFlow<List<MediaItem>> = _mediaItems.asStateFlow()

    fun getMediaById(id: String): MediaItem? {
        return _mediaItems.value.find { it.id == id }
    }

    fun addOrUpdateMedia(item: MediaItem) {
        val current = _mediaItems.value.toMutableList()
        val index = current.indexOfFirst { it.id == item.id }
        if (index >= 0) {
            current[index] = item
        } else {
            current.add(0, item) // Add at top of catalog
        }
        if (item.isFeatured) {
            _mediaItems.value = current.map {
                if (it.id == item.id) it else it.copy(isFeatured = false)
            }
        } else {
            _mediaItems.value = current
        }
    }

    fun setFeaturedMedia(id: String) {
        val current = _mediaItems.value.map {
            if (it.id == id) it.copy(isFeatured = true) else it.copy(isFeatured = false)
        }
        _mediaItems.value = current
    }

    fun toggleTop10(id: String) {
        val current = _mediaItems.value.map {
            if (it.id == id) it.copy(isTop10 = !it.isTop10) else it
        }
        _mediaItems.value = current
    }

    fun deleteMedia(id: String) {
        _mediaItems.value = _mediaItems.value.filter { it.id != id }
    }

    fun resetCatalog() {
        _mediaItems.value = getInitialCatalog()
    }

    companion object {
        // High quality direct playable test streams from open test servers (Big Buck Bunny, Tears of Steel, Sintel)
        const val STREAM_BUNNY = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
        const val STREAM_TEARS = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4"
        const val STREAM_SINTEL = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4"
        const val STREAM_ELEPHANTS = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"

        fun getInitialCatalog(): List<MediaItem> {
            return listOf(
                MediaItem(
                    id = "stranger_chronicles",
                    title = "Stranger Chronicles",
                    description = "When a young boy vanishes from a mysterious Midwestern town, a determined sheriff, frantic friends, and a supernatural girl uncover secret experiments and terrifying alternate dimensions.",
                    bannerUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=1200&q=80",
                    posterUrl = "https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=500&q=80",
                    type = MediaType.SERIES,
                    category = "Trending Now",
                    genres = listOf("Sci-Fi", "Mystery", "Supernatural"),
                    releaseYear = 2024,
                    rating = "TV-MA",
                    matchPercentage = 99,
                    durationText = "4 Seasons",
                    directStreamUrl = STREAM_BUNNY,
                    isFeatured = true,
                    isTop10 = true,
                    badgeLabel = "TOP 1",
                    cast = listOf("Millie Bobby Brown", "David Harbour", "Winona Ryder"),
                    seasons = listOf(
                        SeasonData(
                            seasonNumber = 1,
                            title = "Season 1",
                            episodes = listOf(
                                EpisodeData(
                                    id = "sc_s1_e1",
                                    episodeNumber = 1,
                                    title = "Chapter One: The Vanishing",
                                    overview = "On his way home from a friend's house, young Will sees something terrifying. Nearby, a sinister secret lurks in the depths of a government lab.",
                                    thumbnailUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=500&q=80",
                                    durationMinutes = 48,
                                    streamUrl = STREAM_BUNNY
                                ),
                                EpisodeData(
                                    id = "sc_s1_e2",
                                    episodeNumber = 2,
                                    title = "Chapter Two: The Weirdo",
                                    overview = "Lucas, Mike and Dustin try to talk to the girl they found in the woods. Hopper questions an anxious Joyce about an unsettling phone call.",
                                    thumbnailUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=500&q=80",
                                    durationMinutes = 55,
                                    streamUrl = STREAM_TEARS
                                ),
                                EpisodeData(
                                    id = "sc_s1_e3",
                                    episodeNumber = 3,
                                    title = "Chapter Three: Holly, Jolly",
                                    overview = "An increasingly concerned Nancy looks for Barb and finds out what Jonathan has been up to. Joyce is convinced Will is trying to communicate.",
                                    thumbnailUrl = "https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?w=500&q=80",
                                    durationMinutes = 51,
                                    streamUrl = STREAM_SINTEL
                                )
                            )
                        ),
                        SeasonData(
                            seasonNumber = 2,
                            title = "Season 2",
                            episodes = listOf(
                                EpisodeData(
                                    id = "sc_s2_e1",
                                    episodeNumber = 1,
                                    title = "Chapter One: MADMAX",
                                    overview = "As the town preps for Halloween, a high-scoring rival shakes up the arcade, and a skeptical Hopper inspects a field of rotting pumpkins.",
                                    thumbnailUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=500&q=80",
                                    durationMinutes = 48,
                                    streamUrl = STREAM_ELEPHANTS
                                ),
                                EpisodeData(
                                    id = "sc_s2_e2",
                                    episodeNumber = 2,
                                    title = "Chapter Two: Trick or Treat, Freak",
                                    overview = "After Will sees something terrible on Halloween night, Mike wonders if Eleven is still out there. Nancy wrestles with the truth about Barb.",
                                    thumbnailUrl = "https://images.unsplash.com/photo-1514565131-fce0801e5785?w=500&q=80",
                                    durationMinutes = 56,
                                    streamUrl = STREAM_BUNNY
                                )
                            )
                        )
                    )
                ),
                MediaItem(
                    id = "cyberpunk_neon",
                    title = "Cyberpunk: Neon District",
                    description = "In a dystopian metropolis obsessed with cybernetic body modifications, a street kid fighting to survive turns into an outlaw mercenary known as an Edgerunner.",
                    bannerUrl = "https://images.unsplash.com/photo-1542751371-adc38448a05e?w=1200&q=80",
                    posterUrl = "https://images.unsplash.com/photo-1508739773434-c26b3d09e071?w=500&q=80",
                    type = MediaType.SERIES,
                    category = "Trending Now",
                    genres = listOf("Anime", "Cyberpunk", "Action"),
                    releaseYear = 2024,
                    rating = "TV-MA",
                    matchPercentage = 97,
                    durationText = "1 Season",
                    directStreamUrl = STREAM_TEARS,
                    isFeatured = false,
                    isTop10 = true,
                    badgeLabel = "TOP 2",
                    cast = listOf("KENN", "Aoi Yuki", "Hiroki Touchi"),
                    seasons = listOf(
                        SeasonData(
                            seasonNumber = 1,
                            title = "Season 1",
                            episodes = listOf(
                                EpisodeData(
                                    id = "cp_s1_e1",
                                    episodeNumber = 1,
                                    title = "Episode 1: Let You Down",
                                    overview = "David's mother works endless hours to afford his tuition at an elite academy. When tragedy strikes, David makes a desperate cyberware choice.",
                                    thumbnailUrl = "https://images.unsplash.com/photo-1508739773434-c26b3d09e071?w=500&q=80",
                                    durationMinutes = 24,
                                    streamUrl = STREAM_TEARS
                                ),
                                EpisodeData(
                                    id = "cp_s1_e2",
                                    episodeNumber = 2,
                                    title = "Episode 2: Like a Boy",
                                    overview = "Armed with a military-grade Sandevistan spine implant, David crosses paths with a cunning netrunner named Lucy on the metro.",
                                    thumbnailUrl = "https://images.unsplash.com/photo-1542751371-adc38448a05e?w=500&q=80",
                                    durationMinutes = 25,
                                    streamUrl = STREAM_BUNNY
                                )
                            )
                        )
                    )
                ),
                MediaItem(
                    id = "interstellar_odyssey",
                    title = "Cosmic Horizon: Interstellar",
                    description = "With humanity facing famine on an arid Earth, a courageous team of explorers embarks on a journey through a newly discovered wormhole beyond our solar system.",
                    bannerUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=1200&q=80",
                    posterUrl = "https://images.unsplash.com/photo-1446776811953-b23d57bd21aa?w=500&q=80",
                    type = MediaType.MOVIE,
                    category = "Popular Movies",
                    genres = listOf("Sci-Fi", "Adventure", "Drama"),
                    releaseYear = 2023,
                    rating = "PG-13",
                    matchPercentage = 98,
                    durationText = "2h 49m",
                    directStreamUrl = STREAM_SINTEL,
                    isFeatured = false,
                    isTop10 = true,
                    badgeLabel = "TOP 3",
                    cast = listOf("Matthew McConaughey", "Anne Hathaway", "Jessica Chastain")
                ),
                MediaItem(
                    id = "squid_gambit",
                    title = "The Survival Game",
                    description = "Hundreds of cash-strapped players accept a strange invitation to compete in children's games. Inside, a tempting 45.6 billion won prize awaits with deadly high stakes.",
                    bannerUrl = "https://images.unsplash.com/photo-1511512578047-dfb367046420?w=1200&q=80",
                    posterUrl = "https://images.unsplash.com/photo-1618336753974-aae8e04506aa?w=500&q=80",
                    type = MediaType.SERIES,
                    category = "Trending Now",
                    genres = listOf("Thriller", "Suspense", "Drama"),
                    releaseYear = 2024,
                    rating = "TV-MA",
                    matchPercentage = 96,
                    durationText = "2 Seasons",
                    directStreamUrl = STREAM_ELEPHANTS,
                    isFeatured = false,
                    isTop10 = true,
                    badgeLabel = "NEW SEASON",
                    cast = listOf("Lee Jung-jae", "Park Hae-soo", "Wi Ha-joon"),
                    seasons = listOf(
                        SeasonData(
                            seasonNumber = 1,
                            title = "Season 1",
                            episodes = listOf(
                                EpisodeData(
                                    id = "sg_s1_e1",
                                    episodeNumber = 1,
                                    title = "Red Light, Green Light",
                                    overview = "Hoping to win easy money, a broke and desperate Gi-hun agrees to take part in an enigmatic game. Not long into the first round, unexpected horrors unfold.",
                                    thumbnailUrl = "https://images.unsplash.com/photo-1618336753974-aae8e04506aa?w=500&q=80",
                                    durationMinutes = 60,
                                    streamUrl = STREAM_ELEPHANTS
                                )
                            )
                        )
                    )
                ),
                MediaItem(
                    id = "dark_knight_legacy",
                    title = "Shadow of Gotham",
                    description = "When a sadistic serial killer begins murdering key political figures in Gotham, the city's masked vigilante must investigate hidden corruption and question his family's involvement.",
                    bannerUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=1200&q=80",
                    posterUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=500&q=80",
                    type = MediaType.MOVIE,
                    category = "Popular Movies",
                    genres = listOf("Action", "Crime", "Mystery"),
                    releaseYear = 2024,
                    rating = "PG-13",
                    matchPercentage = 95,
                    durationText = "2h 56m",
                    directStreamUrl = STREAM_TEARS,
                    isFeatured = false,
                    isTop10 = false,
                    cast = listOf("Robert Pattinson", "Zoë Kravitz", "Paul Dano")
                ),
                MediaItem(
                    id = "arcane_destiny",
                    title = "Arcane: Legends of Zaun",
                    description = "Amidst the stark discord of twin cities Piltover and Zaun, two sisters fight on rival sides of a war between magic technologies and incompatible convictions.",
                    bannerUrl = "https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?w=1200&q=80",
                    posterUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=500&q=80",
                    type = MediaType.SERIES,
                    category = "Top TV Series",
                    genres = listOf("Animation", "Fantasy", "Action"),
                    releaseYear = 2024,
                    rating = "TV-14",
                    matchPercentage = 99,
                    durationText = "2 Seasons",
                    directStreamUrl = STREAM_BUNNY,
                    isFeatured = false,
                    isTop10 = true,
                    badgeLabel = "EMMY WINNER",
                    cast = listOf("Hailee Steinfeld", "Ella Purnell", "Kevin Alejandro"),
                    seasons = listOf(
                        SeasonData(
                            seasonNumber = 1,
                            title = "Season 1",
                            episodes = listOf(
                                EpisodeData(
                                    id = "arc_s1_e1",
                                    episodeNumber = 1,
                                    title = "Welcome to the Playground",
                                    overview = "Orphan sisters Vi and Powder lead a daring heist in the upscale district of Piltover, triggering dangerous repercussions.",
                                    thumbnailUrl = "https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?w=500&q=80",
                                    durationMinutes = 43,
                                    streamUrl = STREAM_BUNNY
                                )
                            )
                        )
                    )
                ),
                MediaItem(
                    id = "dune_rebellion",
                    title = "Dune: Imperial Prophecy",
                    description = "Paul Atreides unites with Chani and the Fremen while seeking revenge against the conspirators who destroyed his family, facing a choice between love and the fate of the universe.",
                    bannerUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=1200&q=80",
                    posterUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=500&q=80",
                    type = MediaType.MOVIE,
                    category = "Popular Movies",
                    genres = listOf("Sci-Fi", "Epic", "Adventure"),
                    releaseYear = 2024,
                    rating = "PG-13",
                    matchPercentage = 97,
                    durationText = "2h 46m",
                    directStreamUrl = STREAM_SINTEL,
                    isFeatured = false,
                    isTop10 = false,
                    cast = listOf("Timothée Chalamet", "Zendaya", "Rebecca Ferguson")
                ),
                MediaItem(
                    id = "the_witcher_chronicles",
                    title = "The White Wolf",
                    description = "Geralt of Rivia, a mutated monster-hunter for hire, journeys toward his destiny in a turbulent world where people often prove more wicked than beasts.",
                    bannerUrl = "https://images.unsplash.com/photo-1514565131-fce0801e5785?w=1200&q=80",
                    posterUrl = "https://images.unsplash.com/photo-1508739773434-c26b3d09e071?w=500&q=80",
                    type = MediaType.SERIES,
                    category = "Top TV Series",
                    genres = listOf("Fantasy", "Action", "Adventure"),
                    releaseYear = 2023,
                    rating = "TV-MA",
                    matchPercentage = 94,
                    durationText = "3 Seasons",
                    directStreamUrl = STREAM_ELEPHANTS,
                    isFeatured = false,
                    isTop10 = false,
                    cast = listOf("Henry Cavill", "Anya Chalotra", "Freya Allan"),
                    seasons = listOf(
                        SeasonData(
                            seasonNumber = 1,
                            title = "Season 1",
                            episodes = listOf(
                                EpisodeData(
                                    id = "ww_s1_e1",
                                    episodeNumber = 1,
                                    title = "The End's Beginning",
                                    overview = "Hostile townsfolk and a sly mage greet Geralt in the town of Blaviken. Ciri finds her royal world upended when Nilfgaard attacks.",
                                    thumbnailUrl = "https://images.unsplash.com/photo-1514565131-fce0801e5785?w=500&q=80",
                                    durationMinutes = 61,
                                    streamUrl = STREAM_ELEPHANTS
                                )
                            )
                        )
                    )
                )
            )
        }
    }
}
