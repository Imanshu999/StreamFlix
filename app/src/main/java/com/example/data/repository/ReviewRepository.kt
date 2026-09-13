package com.example.data.repository

import com.example.data.local.ReviewDao
import com.example.data.local.ReviewEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class ReviewRepository(private val reviewDao: ReviewDao) {

    fun getReviewsForMedia(mediaId: String): Flow<List<ReviewEntity>> {
        return reviewDao.getReviewsForMedia(mediaId)
    }

    val allReviews: Flow<List<ReviewEntity>> = reviewDao.getAllReviews()

    suspend fun addReview(
        mediaId: String,
        userEmail: String,
        userName: String,
        userAvatarUrl: String,
        rating: Int,
        reviewText: String,
        isVerifiedUser: Boolean = true
    ) {
        val review = ReviewEntity(
            id = "rev_" + UUID.randomUUID().toString().take(8),
            mediaId = mediaId,
            userEmail = userEmail,
            userName = userName,
            userAvatarUrl = userAvatarUrl,
            rating = rating.coerceIn(1, 5),
            reviewText = reviewText.trim(),
            timestamp = System.currentTimeMillis(),
            isVerifiedUser = isVerifiedUser
        )
        reviewDao.insertReview(review)
    }

    suspend fun deleteReview(id: String) {
        reviewDao.deleteReview(id)
    }

    suspend fun seedDefaultReviewsIfEmpty() {
        if (reviewDao.getReviewCount() == 0) {
            val defaultReviews = listOf(
                ReviewEntity(
                    id = "seed_sc_1",
                    mediaId = "stranger_chronicles",
                    userEmail = "n4062226@gmail.com",
                    userName = "Alex Turner (Admin)",
                    userAvatarUrl = "avatar_crown",
                    rating = 5,
                    reviewText = "Masterpiece storytelling with an incredible synth soundtrack. The 80s homage and supernatural thriller vibes are unmatched!",
                    timestamp = System.currentTimeMillis() - 86400000L * 2,
                    isVerifiedUser = true
                ),
                ReviewEntity(
                    id = "seed_sc_2",
                    mediaId = "stranger_chronicles",
                    userEmail = "sarah.connor@streamflix.tv",
                    userName = "Sarah Connor",
                    userAvatarUrl = "avatar_headphones",
                    rating = 5,
                    reviewText = "Episode 4 was absolutely thrilling! The upside-down lore is expanding in the best way possible.",
                    timestamp = System.currentTimeMillis() - 86400000L * 5,
                    isVerifiedUser = true
                ),
                ReviewEntity(
                    id = "seed_sg_1",
                    mediaId = "squid_gambit",
                    userEmail = "david.k@streamflix.tv",
                    userName = "David Kim",
                    userAvatarUrl = "avatar_glasses",
                    rating = 5,
                    reviewText = "Intense psychological drama. The set designs and suspenseful elimination games keep you on the edge of your seat from start to finish.",
                    timestamp = System.currentTimeMillis() - 86400000L * 1,
                    isVerifiedUser = true
                ),
                ReviewEntity(
                    id = "seed_sg_2",
                    mediaId = "squid_gambit",
                    userEmail = "elena.m@streamflix.tv",
                    userName = "Elena Morales",
                    userAvatarUrl = "avatar_classic",
                    rating = 4,
                    reviewText = "Binge-watched the entire season in a single weekend. Visually stunning and thought-provoking social commentary.",
                    timestamp = System.currentTimeMillis() - 86400000L * 3,
                    isVerifiedUser = true
                ),
                ReviewEntity(
                    id = "seed_cr_1",
                    mediaId = "the_crown_legacy",
                    userEmail = "james.w@streamflix.tv",
                    userName = "James Wright",
                    userAvatarUrl = "avatar_star",
                    rating = 5,
                    reviewText = "Superb acting and royal costume design. The historical drama is portrayed with absolute nuance and elegance.",
                    timestamp = System.currentTimeMillis() - 86400000L * 4,
                    isVerifiedUser = true
                ),
                ReviewEntity(
                    id = "seed_ex_1",
                    mediaId = "extraction_zero",
                    userEmail = "mike.r@streamflix.tv",
                    userName = "Mike Ross",
                    userAvatarUrl = "avatar_cap",
                    rating = 5,
                    reviewText = "The one-shot 20-minute extraction sequence is one of the best action set pieces in modern cinema. Pure adrenaline!",
                    timestamp = System.currentTimeMillis() - 86400000L * 2,
                    isVerifiedUser = true
                ),
                ReviewEntity(
                    id = "seed_cb_1",
                    mediaId = "cyber_city_2099",
                    userEmail = "n4062226@gmail.com",
                    userName = "Alex Turner (Admin)",
                    userAvatarUrl = "avatar_crown",
                    rating = 5,
                    reviewText = "Cyberpunk neon visuals look spectacular in 4K HDR. Deep existential plot line and top-tier soundtrack.",
                    timestamp = System.currentTimeMillis() - 86400000L * 1,
                    isVerifiedUser = true
                )
            )
            reviewDao.insertReviews(defaultReviews)
        }
    }
}
