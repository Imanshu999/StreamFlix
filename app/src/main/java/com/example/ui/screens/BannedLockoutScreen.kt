package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun BannedLockoutScreen(
    deviceId: String,
    ipAddress: String,
    onEmergencyOverride: (pin: String) -> Boolean,
    modifier: Modifier = Modifier
) {
    var showOverrideDialog by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NetflixBlack)
            .padding(24.dp)
            .testTag("banned_lockout_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Shield & Lock Icon
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(DangerRed.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Access Blocked",
                    tint = DangerRed,
                    modifier = Modifier.size(46.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "ACCESS RESTRICTED",
                color = DangerRed,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "This device or network IP has been blacklisted due to automated security policy enforcement.",
                color = NetflixWhite,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Incident Details Card
            Surface(
                color = NetflixCardSurface,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Device ID:", color = NetflixLightGrey, fontSize = 12.sp)
                        Text(deviceId, color = NetflixWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Blocked IP:", color = NetflixLightGrey, fontSize = 12.sp)
                        Text(ipAddress, color = NetflixWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Incident Code:", color = NetflixLightGrey, fontSize = 12.sp)
                        Text("SEC-ERR-4039", color = DangerRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Emergency Admin Override button
            OutlinedButton(
                onClick = { showOverrideDialog = true },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentGlow),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("emergency_unban_button")
            ) {
                Icon(imageVector = Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Admin Emergency Override (PIN)")
            }
        }
    }

    // Emergency Override Dialog
    if (showOverrideDialog) {
        AlertDialog(
            onDismissRequest = {
                showOverrideDialog = false
                pinInput = ""
                errorMsg = null
            },
            title = { Text("Emergency Admin Unban", color = NetflixWhite) },
            text = {
                Column {
                    Text(
                        text = "Enter master admin PIN (admin123) to instantly remove restrictions and restore client access.",
                        color = NetflixLightGrey,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = {
                            pinInput = it
                            errorMsg = null
                        },
                        placeholder = { Text("admin123", color = NetflixLightGrey) },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = NetflixWhite,
                            unfocusedTextColor = NetflixWhite,
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = NetflixCardBorder
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("emergency_pin_input")
                    )

                    errorMsg?.let {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(it, color = DangerRed, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (onEmergencyOverride(pinInput)) {
                            showOverrideDialog = false
                        } else {
                            errorMsg = "Incorrect admin PIN"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                    modifier = Modifier.testTag("emergency_pin_confirm")
                ) {
                    Text("Lift Ban")
                }
            },
            dismissButton = {
                TextButton(onClick = { showOverrideDialog = false }) {
                    Text("Cancel", color = NetflixLightGrey)
                }
            },
            containerColor = NetflixCardSurface
        )
    }
}
