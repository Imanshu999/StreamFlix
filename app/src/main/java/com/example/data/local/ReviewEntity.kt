package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reviews")
data class ReviewEntity(
    @PrimaryKey val id: String,
    val mediaId: String,
    val userEmail: String,
    val userName: String,
    val userAvatarUrl: String,
    val rating: Int, // 1 to 5 stars
    val reviewText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isVerifiedUser: Boolean = true
)
