package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val StreamFlixDarkColorScheme = darkColorScheme(
    primary = NetflixRed,
    onPrimary = Color.White,
    primaryContainer = NetflixDarkRed,
    onPrimaryContainer = Color.White,
    secondary = AccentBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF1E293B),
    onSecondaryContainer = AccentGlow,
    tertiary = AccentCyan,
    onTertiary = Color.Black,
    background = NetflixBlack,
    onBackground = NetflixWhite,
    surface = NetflixDarkGrey,
    onSurface = NetflixWhite,
    surfaceVariant = NetflixCardSurface,
    onSurfaceVariant = NetflixLightGrey,
    outline = NetflixCardBorder,
    error = DangerRed,
    onError = Color.White
)

@Composable
fun StreamFlixTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) = MyApplicationTheme(darkTheme = true, content = content)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // StreamFlix uses an uncompromising premium dark cinematic theme by design
    val colorScheme = StreamFlixDarkColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
