package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.UserProfile
import com.example.data.repository.AuthVerificationResult
import com.example.ui.components.ALL_AVATAR_STYLES
import com.example.ui.components.AvatarStyle
import com.example.ui.components.MinimalVectorAvatar
import com.example.ui.theme.*

@Composable
fun ProfileScreen(
    user: UserProfile,
    deviceId: String,
    deviceIp: String,
    isBanned: Boolean,
    onSignIn: (email: String, password: String) -> AuthVerificationResult,
    onSignOutGuest: () -> Unit,
    onSelectAvatar: (String) -> Unit,
    onOpenAdminPortal: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showGoogleAuthDialog by remember { mutableStateOf(false) }
    var inputEmail by remember { mutableStateOf("") }
    var inputPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var authErrorMessage by remember { mutableStateOf<String?>(null) }

    val isStrictAdmin = !user.isGuest && user.email.equals("n4062226@gmail.com", ignoreCase = true)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NetflixBlack)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("profile_screen_root")
    ) {
        // Header
        Text(
            text = "Profile & Account",
            color = NetflixWhite,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Profile Avatar & Name Card
        Surface(
            color = NetflixCardSurface,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Minimalist vector avatar with clean white circular background and dark silhouette
                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(
                            width = 2.5.dp,
                            color = if (isStrictAdmin) AccentGlow else if (user.isGuest) NetflixCardBorder else NetflixRed,
                            shape = CircleShape
                        )
                        .padding(2.dp)
                        .testTag("current_profile_avatar"),
                    contentAlignment = Alignment.Center
                ) {
                    MinimalVectorAvatar(
                        avatarId = user.avatarUrl,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = user.displayName,
                    color = NetflixWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = user.email,
                    color = NetflixLightGrey,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = if (user.isGuest) Color.White.copy(alpha = 0.1f) else AccentBlue.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = user.subscriptionTier,
                            color = if (user.isGuest) NetflixLightGrey else AccentGlow,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    if (isStrictAdmin) {
                        Surface(
                            color = NetflixRed.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = "Admin Verified",
                                    tint = NetflixRed,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Administrator",
                                    color = NetflixRed,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Super Administrator Badge & Admin Panel Card - STRICT CONDITIONAL RENDERING
        // ONLY renders if the authenticated Firebase Auth user email matches exactly with "n4062226@gmail.com"
        // Completely hidden from the DOM/composition for all guests and non-matching accounts.
        if (isStrictAdmin) {
            Spacer(modifier = Modifier.height(14.dp))
            Surface(
                color = AccentBlue.copy(alpha = 0.12f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, AccentBlue.copy(alpha = 0.45f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("super_admin_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(AccentBlue.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = "Super Administrator Badge",
                                tint = AccentGlow,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Super Administrator",
                                    color = NetflixWhite,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Surface(
                                    color = NetflixRed,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "ROOT ACCESS",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Google Account: n4062226@gmail.com",
                                color = AccentGlow,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Administrative authority active: catalog CMS editing, dynamic multi-season/episode publishing, and client telemetry security moderation.",
                        color = NetflixLightGrey,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onOpenAdminPortal,
                        colors = ButtonDefaults.buttonColors(containerColor = NetflixRed),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("admin_portal_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Admin Panel (CMS & Moderation) • Authenticated",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 1. Choose Profile Avatar (Clean modern minimal vector avatars similar to Instagram placeholder)
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Choose Profile Avatar",
            color = NetflixWhite,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Modern vector silhouettes on solid light circular canvas",
            color = NetflixLightGrey,
            fontSize = 11.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
            items(ALL_AVATAR_STYLES, key = { it.id }) { style ->
                val isSelected = user.avatarUrl == style.id || (user.avatarUrl.startsWith("http") && style == AvatarStyle.CLASSIC)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onSelectAvatar(style.id) }
                        .testTag("avatar_option_${style.id}")
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) NetflixRed else NetflixCardBorder,
                                shape = CircleShape
                            )
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        MinimalVectorAvatar(
                            avatarId = style.id,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = style.label,
                        color = if (isSelected) NetflixWhite else NetflixLightGrey,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        // Authentication & Cloud Sync Section
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Authentication & Cloud Sync",
            color = NetflixWhite,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            color = NetflixCardSurface,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (user.isGuest) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CloudOff,
                            contentDescription = null,
                            tint = NetflixLightGrey,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Guest Mode Active",
                                color = NetflixWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Sign in with Google to sync watch history, bookmarks & resume points across all devices.",
                                color = NetflixLightGrey,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            inputEmail = ""
                            inputPassword = ""
                            authErrorMessage = null
                            isPasswordVisible = false
                            showGoogleAuthDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NetflixRed),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sign_in_google_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sign in with Google / Firebase", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CloudDone,
                            contentDescription = null,
                            tint = AccentGlow,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Cloud Synchronization Active",
                                color = NetflixWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Watch history and bookmarks are auto-saved to Firebase Firestore.",
                                color = AccentGlow,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedButton(
                        onClick = onSignOutGuest,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NetflixLightGrey),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("switch_to_guest_button")
                    ) {
                        Text("Switch to Guest Mode")
                    }
                }
            }
        }

        // Device Telemetry & Security Diagnostics Card
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Device & Telemetry Diagnostics",
            color = NetflixWhite,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            color = NetflixCardSurface,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Device ID", color = NetflixLightGrey, fontSize = 12.sp)
                    Text(text = deviceId, color = NetflixWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "IP Address", color = NetflixLightGrey, fontSize = 12.sp)
                    Text(text = deviceIp, color = NetflixWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Security Ban Status", color = NetflixLightGrey, fontSize = 12.sp)
                    Text(
                        text = if (isBanned) "BANNED (Blocked)" else "CLEARED (Active)",
                        color = if (isBanned) DangerRed else BadgeGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(60.dp))
    }

    // Google / Firebase Secure Sign-In Dialog
    if (showGoogleAuthDialog) {
        val isAdminEmailDetected = inputEmail.trim().equals("n4062226@gmail.com", ignoreCase = true)

        AlertDialog(
            onDismissRequest = {
                showGoogleAuthDialog = false
                authErrorMessage = null
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = NetflixRed
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Firebase Account Sign-In", color = NetflixWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Sign in to synchronize watch history, bookmarks, and account credentials across your devices.",
                        color = NetflixLightGrey,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    // Error banner
                    if (authErrorMessage != null) {
                        Surface(
                            color = DangerRed.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DangerRed.copy(alpha = 0.6f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = DangerRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = authErrorMessage ?: "",
                                    color = DangerRed,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Email Input (Blank / Empty by default)
                    OutlinedTextField(
                        value = inputEmail,
                        onValueChange = {
                            inputEmail = it
                            if (authErrorMessage != null) authErrorMessage = null
                        },
                        label = { Text("Email Address", color = NetflixLightGrey) },
                        placeholder = { Text("e.g. yourname@example.com", color = NetflixLightGrey.copy(alpha = 0.5f)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = NetflixWhite,
                            unfocusedTextColor = NetflixWhite,
                            focusedContainerColor = NetflixBlack,
                            unfocusedContainerColor = NetflixBlack,
                            focusedBorderColor = NetflixRed,
                            unfocusedBorderColor = NetflixCardBorder
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("google_email_input")
                    )

                    // Super Admin Detected Security Notice
                    if (isAdminEmailDetected) {
                        Surface(
                            color = Color(0xFFFFD700).copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Super Administrator account identified. Mandatory security password required.",
                                    color = Color(0xFFFFD700),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // Secure Password Field (Mandatory)
                    OutlinedTextField(
                        value = inputPassword,
                        onValueChange = {
                            inputPassword = it
                            if (authErrorMessage != null) authErrorMessage = null
                        },
                        label = {
                            Text(
                                text = if (isAdminEmailDetected) "Admin Master Password *" else "Password *",
                                color = NetflixLightGrey
                            )
                        },
                        placeholder = {
                            Text(
                                text = if (isAdminEmailDetected) "Enter admin password" else "Enter password (min 6 chars)",
                                color = NetflixLightGrey.copy(alpha = 0.5f)
                            )
                        },
                        singleLine = true,
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                                    tint = NetflixLightGrey
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = NetflixWhite,
                            unfocusedTextColor = NetflixWhite,
                            focusedContainerColor = NetflixBlack,
                            unfocusedContainerColor = NetflixBlack,
                            focusedBorderColor = NetflixRed,
                            unfocusedBorderColor = NetflixCardBorder
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_password_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmedEmail = inputEmail.trim()
                        if (trimmedEmail.isEmpty()) {
                            authErrorMessage = "Email address cannot be empty."
                            return@Button
                        }
                        if (inputPassword.isEmpty()) {
                            authErrorMessage = if (isAdminEmailDetected) {
                                "Super Administrator password is required."
                            } else {
                                "Password is required."
                            }
                            return@Button
                        }

                        val result = onSignIn(trimmedEmail, inputPassword)
                        if (result.isSuccess) {
                            showGoogleAuthDialog = false
                            authErrorMessage = null
                            inputEmail = ""
                            inputPassword = ""
                        } else {
                            authErrorMessage = result.errorMessage ?: "Authentication failed."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NetflixRed),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("google_confirm_button")
                ) {
                    Text("Authenticate & Sync", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showGoogleAuthDialog = false
                        authErrorMessage = null
                    }
                ) {
                    Text("Cancel", color = NetflixLightGrey)
                }
            },
            containerColor = NetflixCardSurface,
            shape = RoundedCornerShape(12.dp)
        )
    }
}
