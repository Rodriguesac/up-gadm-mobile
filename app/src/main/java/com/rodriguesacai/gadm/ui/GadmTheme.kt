package com.rodriguesacai.gadm.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Paleta clara do GADM Mobile. Os nomes antigos são mantidos para compatibilidade.
internal val GadmNavy = Color(0xFF13213D)
internal val GadmSurface = Color(0xFFF7F9FC)
internal val GadmSurfaceStrong = Color(0xFFFFFFFF)
internal val GadmBlue = Color(0xFF2A67F8)
internal val GadmLime = Color(0xFF71C900)
internal val GadmWhite = Color(0xFFFFFFFF)
internal val GadmMuted = Color(0xFF68738A)
internal val GadmSuccess = Color(0xFF1FA65B)
internal val GadmYellow = Color(0xFFFFAD1F)
internal val GadmDanger = Color(0xFFE84C4F)
internal val GadmBorder = Color(0xFFE5EAF2)
internal val GadmSoftBlue = Color(0xFFEAF1FF)
internal val GadmSoftLime = Color(0xFFEAF8D9)
internal val GadmSoftOrange = Color(0xFFFFF1DD)
internal val GadmSoftDanger = Color(0xFFFFE9EA)

@Composable
fun GadmMobileTheme(content: @Composable () -> Unit) {
    val scheme = lightColorScheme(
        primary = GadmLime,
        onPrimary = GadmNavy,
        secondary = GadmBlue,
        onSecondary = GadmWhite,
        background = GadmSurface,
        onBackground = GadmNavy,
        surface = GadmWhite,
        onSurface = GadmNavy,
        error = GadmDanger,
        onError = GadmWhite,
        outline = GadmBorder
    )
    MaterialTheme(colorScheme = scheme, content = content)
}
