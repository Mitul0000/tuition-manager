package com.digifello.tutordesk.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Marigold,
    onPrimary = Ink_Navy,
    background = ChalkWhite,
    onBackground = Ink_Navy,
    surface = CardWhite,
    onSurface = Ink_Navy,
    secondary = SlateBlue,
    error = UnpaidCrimson
)

private val DarkColors = darkColorScheme(
    primary = Marigold,
    onPrimary = Ink_Navy,
    background = ChalkWhiteDark,
    onBackground = Ink_NavyDark,
    surface = CardWhiteDark,
    onSurface = Ink_NavyDark,
    secondary = SlateBlueDark,
    error = UnpaidCrimson
)

@Composable
fun TutorDeskTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}