package com.rodriguesacai.gadm.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Ink = Color(0xFF151515)
val Paper = Color(0xFFF7F5F2)
val Accent = Color(0xFFCC3B2E)
val Success = Color(0xFF2E7D5B)
val Warning = Color(0xFFC97817)
val OutlineSoft = Color(0xFFE0DDD8)

private val GadmColors = darkColorScheme(
    primary = Accent,
    onPrimary = Color.White,
    secondary = Color(0xFFE9E1D8),
    onSecondary = Ink,
    background = Paper,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = Color(0xFFF0ECE7),
    onSurfaceVariant = Color(0xFF665F59),
    outline = OutlineSoft,
    error = Color(0xFFB3261E)
)

@Composable
fun GadmTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = GadmColors, typography = GadmTypography, content = content)
}
