package com.rodriguesacai.gadm.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// GADM V24: identidade clara alinhada ao Cliente Rodrigues.
// Os nomes antigos permanecem para compatibilidade com telas legadas.
internal val GadmNavy = Color(0xFF351244)
internal val GadmSurface = Color(0xFFF7F8FA)
internal val GadmSurfaceStrong = Color(0xFFFFFFFF)
internal val GadmBlue = Color(0xFF4B0082)
internal val GadmLime = Color(0xFF72B51B)
internal val GadmWhite = Color(0xFFFFFFFF)
internal val GadmMuted = Color(0xFF77808D)
internal val GadmSuccess = Color(0xFF5C9716)
internal val GadmYellow = Color(0xFFE59B18)
internal val GadmDanger = Color(0xFFD94949)
internal val GadmBorder = Color(0xFFE7E8EC)
internal val GadmSoftBlue = Color(0xFFF4EDF8)
internal val GadmSoftLime = Color(0xFFF0F8E4)
internal val GadmSoftOrange = Color(0xFFFFF3E5)
internal val GadmSoftDanger = Color(0xFFFFECEC)

@Composable
fun GadmMobileTheme(content: @Composable () -> Unit) {
    val scheme = lightColorScheme(
        primary = GadmBlue,
        onPrimary = GadmWhite,
        secondary = GadmLime,
        onSecondary = GadmNavy,
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
