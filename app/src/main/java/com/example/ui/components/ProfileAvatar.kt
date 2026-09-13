package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class AvatarStyle(val id: String, val label: String) {
    CLASSIC("avatar_classic", "Minimal Silhouette"),
    HEADPHONES("avatar_headphones", "Audio Enthusiast"),
    GLASSES("avatar_glasses", "Sleek Glasses"),
    STAR("avatar_star", "VIP Star"),
    CAP("avatar_cap", "Urban Cap"),
    CROWN("avatar_crown", "Stream Elite")
}

val ALL_AVATAR_STYLES = listOf(
    AvatarStyle.CLASSIC,
    AvatarStyle.HEADPHONES,
    AvatarStyle.GLASSES,
    AvatarStyle.STAR,
    AvatarStyle.CAP,
    AvatarStyle.CROWN
)

/**
 * Clean, modern, minimal vector avatar inspired by Instagram's default new account placeholder:
 * A solid light/white circular background featuring a sleek black/dark-grey vector silhouette
 * of a user/character.
 */
@Composable
fun MinimalVectorAvatar(
    avatarId: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    silhouetteColor: Color = Color(0xFF1E293B) // Sleek slate/dark-grey silhouette
) {
    val style = ALL_AVATAR_STYLES.find { it.id == avatarId } ?: AvatarStyle.CLASSIC

    Canvas(
        modifier = modifier
            .clip(CircleShape)
            .background(backgroundColor)
    ) {
        val w = size.width
        val h = size.height

        // Outer circular clip for seamless torso edges
        val clipCirclePath = Path().apply {
            addOval(Rect(0f, 0f, w, h))
        }

        clipPath(clipCirclePath) {
            val headRadius = w * 0.20f
            val headCenterY = h * 0.36f
            val headCenterX = w * 0.50f

            // 1. Sleek Torso / Shoulders (Instagram style smooth curve)
            val torsoPath = Path().apply {
                moveTo(w * 0.08f, h * 1.05f)
                cubicTo(
                    w * 0.12f, h * 0.70f,
                    w * 0.28f, h * 0.56f,
                    headCenterX, h * 0.56f
                )
                cubicTo(
                    w * 0.72f, h * 0.56f,
                    w * 0.88f, h * 0.70f,
                    w * 0.92f, h * 1.05f
                )
                close()
            }
            drawPath(torsoPath, color = silhouetteColor)

            // 2. Head Silhouette
            drawCircle(
                color = silhouetteColor,
                radius = headRadius,
                center = Offset(headCenterX, headCenterY)
            )

            // 3. Variant-specific minimal vector details
            when (style) {
                AvatarStyle.CLASSIC -> {
                    // Pure Instagram-style clean minimalist silhouette
                }
                AvatarStyle.HEADPHONES -> {
                    // Studio Headphones Arc
                    val arcPadding = headRadius * 1.25f
                    drawArc(
                        color = silhouetteColor,
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(headCenterX - arcPadding, headCenterY - arcPadding),
                        size = Size(arcPadding * 2f, arcPadding * 2f),
                        style = Stroke(width = w * 0.055f, cap = StrokeCap.Round)
                    )
                    // Left Earcup
                    drawRoundRect(
                        color = silhouetteColor,
                        topLeft = Offset(headCenterX - arcPadding - w * 0.02f, headCenterY - headRadius * 0.45f),
                        size = Size(w * 0.08f, headRadius * 0.95f),
                        cornerRadius = CornerRadius(w * 0.04f, w * 0.04f)
                    )
                    // Right Earcup
                    drawRoundRect(
                        color = silhouetteColor,
                        topLeft = Offset(headCenterX + arcPadding - w * 0.06f, headCenterY - headRadius * 0.45f),
                        size = Size(w * 0.08f, headRadius * 0.95f),
                        cornerRadius = CornerRadius(w * 0.04f, w * 0.04f)
                    )
                }
                AvatarStyle.GLASSES -> {
                    // Minimalist modern shades / glasses in clean negative space
                    val glassesY = headCenterY - headRadius * 0.08f
                    val lensW = headRadius * 0.62f
                    val lensH = headRadius * 0.32f
                    // Left lens
                    drawRoundRect(
                        color = Color.White,
                        topLeft = Offset(headCenterX - headRadius * 0.74f, glassesY),
                        size = Size(lensW, lensH),
                        cornerRadius = CornerRadius(lensH * 0.35f, lensH * 0.35f)
                    )
                    // Right lens
                    drawRoundRect(
                        color = Color.White,
                        topLeft = Offset(headCenterX + headRadius * 0.12f, glassesY),
                        size = Size(lensW, lensH),
                        cornerRadius = CornerRadius(lensH * 0.35f, lensH * 0.35f)
                    )
                    // Connecting bridge
                    drawLine(
                        color = Color.White,
                        start = Offset(headCenterX - headRadius * 0.12f, glassesY + lensH * 0.35f),
                        end = Offset(headCenterX + headRadius * 0.12f, glassesY + lensH * 0.35f),
                        strokeWidth = w * 0.025f
                    )
                }
                AvatarStyle.STAR -> {
                    // VIP Star Emblem on chest in crisp clean white
                    val starCenter = Offset(headCenterX, h * 0.74f)
                    val starSize = w * 0.09f
                    drawCircle(
                        color = Color.White,
                        radius = starSize,
                        center = starCenter
                    )
                    drawCircle(
                        color = silhouetteColor,
                        radius = starSize * 0.5f,
                        center = starCenter
                    )
                }
                AvatarStyle.CAP -> {
                    // Sleek baseball cap visor
                    val visorPath = Path().apply {
                        moveTo(headCenterX - headRadius * 0.95f, headCenterY - headRadius * 0.3f)
                        cubicTo(
                            headCenterX - headRadius * 0.2f, headCenterY - headRadius * 1.1f,
                            headCenterX + headRadius * 0.8f, headCenterY - headRadius * 0.8f,
                            headCenterX + headRadius * 1.35f, headCenterY - headRadius * 0.05f
                        )
                        cubicTo(
                            headCenterX + headRadius * 0.8f, headCenterY - headRadius * 0.25f,
                            headCenterX, headCenterY - headRadius * 0.2f,
                            headCenterX - headRadius * 0.95f, headCenterY - headRadius * 0.3f
                        )
                        close()
                    }
                    drawPath(visorPath, color = Color.White)
                }
                AvatarStyle.CROWN -> {
                    // Minimalist 3-point crown atop head
                    val crownBottom = headCenterY - headRadius * 0.85f
                    val crownW = headRadius * 1.1f
                    val crownH = headRadius * 0.5f
                    val crownPath = Path().apply {
                        moveTo(headCenterX - crownW * 0.5f, crownBottom)
                        lineTo(headCenterX - crownW * 0.5f, crownBottom - crownH)
                        lineTo(headCenterX - crownW * 0.2f, crownBottom - crownH * 0.5f)
                        lineTo(headCenterX, crownBottom - crownH * 1.1f)
                        lineTo(headCenterX + crownW * 0.2f, crownBottom - crownH * 0.5f)
                        lineTo(headCenterX + crownW * 0.5f, crownBottom - crownH)
                        lineTo(headCenterX + crownW * 0.5f, crownBottom)
                        close()
                    }
                    drawPath(crownPath, color = silhouetteColor)
                }
            }
        }
    }
}

/**
 * Universal App User Avatar that renders clean Instagram-style minimal vector avatar.
 */
@Composable
fun AppUserAvatar(
    avatarUrl: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    val matchingStyle = ALL_AVATAR_STYLES.find { it.id == avatarUrl }
    val avatarId = matchingStyle?.id ?: AvatarStyle.CLASSIC.id

    MinimalVectorAvatar(
        avatarId = avatarId,
        modifier = modifier
    )
}
