package com.digifello.tuitionmanager.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Marigold,
    onPrimary = InkNavy,
    primaryContainer = SkyTint,
    onPrimaryContainer = InkNavy,

    secondary = SlateBlue,
    onSecondary = CardWhite,
    secondaryContainer = SkyTint,
    onSecondaryContainer = InkNavy,

    tertiary = PaidGreen,
    onTertiary = CardWhite,

    error = UnpaidCrimson,
    onError = CardWhite,
    errorContainer = UnpaidCrimsonBg,
    onErrorContainer = UnpaidCrimson,

    background = ChalkWhite,
    onBackground = InkNavy,

    surface = CardWhite,
    onSurface = InkNavy,
    surfaceVariant = SkyTint,
    onSurfaceVariant = Ink60,

    outline = Hairline,
    outlineVariant = Hairline
)

private val DarkColorScheme = darkColorScheme(
    primary = Marigold,
    onPrimary = InkNavy,
    primaryContainer = DarkSurfaceRaised,
    onPrimaryContainer = DarkTextPrimary,

    secondary = SlateBlue,
    onSecondary = DarkTextPrimary,
    secondaryContainer = DarkSurfaceRaised,
    onSecondaryContainer = DarkTextPrimary,

    tertiary = PaidGreen,
    onTertiary = DarkBackground,

    error = UnpaidCrimson,
    onError = DarkTextPrimary,
    errorContainer = UnpaidCrimsonBg,
    onErrorContainer = UnpaidCrimson,

    background = DarkBackground,
    onBackground = DarkTextPrimary,

    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceRaised,
    onSurfaceVariant = DarkTextSecondary,

    outline = DarkHairline,
    outlineVariant = DarkHairline
)

@Composable
fun TuitionManagerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color deliberately OFF: this app has its own brand palette
    // (Ink Navy / Marigold / Slate Blue) and should never fall back to
    // the user's wallpaper-derived Material You colors.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}