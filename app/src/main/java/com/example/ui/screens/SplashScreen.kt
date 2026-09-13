package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    var startAnimation by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.88f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "brand_scale"
    )

    val contentAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "content_alpha"
    )

    val glowAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 0.55f else 0.05f,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "glow_alpha"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(1100)
        onSplashFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NetflixBlack)
            .clickable { onSplashFinished() }
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        // Subtle ambient radial glow behind brand typography
        Box(
            modifier = Modifier
                .size(280.dp)
                .scale(scale * 1.2f)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            NetflixRed.copy(alpha = glowAlpha * 0.4f),
                            Color(0xFFFFD700).copy(alpha = glowAlpha * 0.2f),
                            Color.Transparent
                        )
                    )
                )
                .blur(40.dp)
        )

        // StreamFlix Clean Brand Typography (No 'N' logo box)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .scale(scale)
                .alpha(contentAlpha)
        ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(
                            color = Color(0xFFFFD700),
                            fontWeight = FontWeight.Black
                        )
                    ) {
                        append("Stream")
                    }
                    withStyle(
                        style = SpanStyle(
                            color = NetflixRed,
                            fontWeight = FontWeight.Black
                        )
                    ) {
                        append("Flix")
                    }
                },
                fontSize = 38.sp,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "STREAM THE BEST IN CINEMA & TV",
                color = NetflixLightGrey,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 2.sp
            )
        }

        // Minimalist bottom progress indicator
        CircularProgressIndicator(
            color = NetflixRed,
            strokeWidth = 2.dp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 54.dp)
                .size(22.dp)
                .alpha(contentAlpha)
        )
    }
}

