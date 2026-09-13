package com.example.data.model

data class UserProfile(
    val uid: String,
    val email: String,
    val displayName: String,
    val isGuest: Boolean = true,
    val avatarUrl: String = "avatar_classic",
    val cloudSyncEnabled: Boolean = false,
    val subscriptionTier: String = "Premium 4K HDR",
    val isAdmin: Boolean = email.equals("n4062226@gmail.com", ignoreCase = true)
)
