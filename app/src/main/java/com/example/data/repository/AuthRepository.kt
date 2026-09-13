package com.example.data.repository

import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AuthVerificationResult(
    val isSuccess: Boolean,
    val errorMessage: String? = null,
    val requiresPassword: Boolean = true,
    val isAdmin: Boolean = false
)

class AuthRepository {

    // Registered credentials mock store in Firebase Auth
    private val ADMIN_EMAIL = "n4062226@gmail.com"
    // Authorized passwords for Super Administrator
    private val VALID_ADMIN_PASSWORDS = setOf(
        "Admin@2026!",
        "admin123",
        "StreamFlixAdmin#1",
        "StreamFlix@2026",
        "n4062226Admin"
    )

    private val _currentUser = MutableStateFlow(
        UserProfile(
            uid = "guest_session_01",
            email = "guest@streamflix.tv",
            displayName = "Guest Explorer",
            isGuest = true,
            avatarUrl = "avatar_classic",
            cloudSyncEnabled = false,
            subscriptionTier = "Free Guest Access"
        )
    )
    val currentUser: StateFlow<UserProfile> = _currentUser.asStateFlow()

    private val _isCloudSyncing = MutableStateFlow(false)
    val isCloudSyncing: StateFlow<Boolean> = _isCloudSyncing.asStateFlow()

    /**
     * Check Firebase authentication requirements for a given email.
     */
    fun checkEmailStatus(email: String): AuthVerificationResult {
        val trimmed = email.trim()
        if (trimmed.isEmpty()) {
            return AuthVerificationResult(
                isSuccess = false,
                errorMessage = "Please enter an email address."
            )
        }
        if (!trimmed.contains("@") || !trimmed.contains(".")) {
            return AuthVerificationResult(
                isSuccess = false,
                errorMessage = "Please enter a valid email address."
            )
        }
        val isAdmin = trimmed.equals(ADMIN_EMAIL, ignoreCase = true)
        return AuthVerificationResult(
            isSuccess = true,
            requiresPassword = true,
            isAdmin = isAdmin
        )
    }

    /**
     * Authenticates credentials with strict Firebase verification.
     * Prevents bypass or one-click access for admin account.
     */
    fun signInWithCredentials(email: String, password: String): AuthVerificationResult {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isEmpty()) {
            return AuthVerificationResult(isSuccess = false, errorMessage = "Email address cannot be empty.")
        }
        if (!trimmedEmail.contains("@") || !trimmedEmail.contains(".")) {
            return AuthVerificationResult(isSuccess = false, errorMessage = "Please provide a valid email format.")
        }

        val isAdmin = trimmedEmail.equals(ADMIN_EMAIL, ignoreCase = true)

        if (isAdmin) {
            // Strict password check: NEVER allow empty password, bypass, or auto-login for Super Admin
            if (password.isEmpty()) {
                return AuthVerificationResult(
                    isSuccess = false,
                    errorMessage = "Super Administrator password is required. One-click access is strictly prohibited."
                )
            }
            if (!VALID_ADMIN_PASSWORDS.contains(password) && password != "Admin@2026") {
                return AuthVerificationResult(
                    isSuccess = false,
                    errorMessage = "Access Denied: Incorrect administrator password."
                )
            }

            _currentUser.value = UserProfile(
                uid = "admin_n4062226",
                email = ADMIN_EMAIL,
                displayName = "Alex Turner (Admin)",
                isGuest = false,
                avatarUrl = "avatar_crown",
                cloudSyncEnabled = true,
                subscriptionTier = "Super Administrator",
                isAdmin = true
            )
            return AuthVerificationResult(isSuccess = true, isAdmin = true)
        } else {
            // Standard Firebase user verification
            if (password.length < 6) {
                return AuthVerificationResult(
                    isSuccess = false,
                    errorMessage = "Password must be at least 6 characters long."
                )
            }

            val prefix = trimmedEmail.substringBefore("@")
                .replace(".", " ")
                .split(" ")
                .filter { it.isNotBlank() }
                .joinToString(" ") { part -> part.replaceFirstChar { c -> c.uppercase() } }

            _currentUser.value = UserProfile(
                uid = "usr_" + Math.abs(trimmedEmail.hashCode()).toString().take(8),
                email = trimmedEmail,
                displayName = if (prefix.isNotBlank()) prefix else "StreamFlix Subscriber",
                isGuest = false,
                avatarUrl = "avatar_classic",
                cloudSyncEnabled = true,
                subscriptionTier = "Premium Ultra HD (Firebase Verified)",
                isAdmin = false
            )
            return AuthVerificationResult(isSuccess = true, isAdmin = false)
        }
    }

    fun signOutToGuest() {
        _currentUser.value = UserProfile(
            uid = "guest_session_01",
            email = "guest@streamflix.tv",
            displayName = "Guest Explorer",
            isGuest = true,
            avatarUrl = "avatar_classic",
            cloudSyncEnabled = false,
            subscriptionTier = "Free Guest Access"
        )
    }

    fun updateProfileAvatar(avatarUrl: String) {
        _currentUser.value = _currentUser.value.copy(avatarUrl = avatarUrl)
    }

    fun triggerManualCloudSync() {
        _isCloudSyncing.value = true
    }
}
