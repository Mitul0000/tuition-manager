package com.digifello.tuitionmanager.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ---------------------------------------------------------------------
// TutorDesk type system — three distinct voices instead of one default:
//   Serif   -> screen titles / headers ("ledger" personality)
//   Sans    -> all UI body copy, labels, buttons
//   Mono    -> every number: fees, times, counts, marks (scannable, distinct)
// No custom font files needed — uses system font families so the build
// stays simple, but the *pairing* is what makes it feel intentional.
// ---------------------------------------------------------------------

val DisplaySerif = FontFamily.Serif
val UiSans = FontFamily.SansSerif
val NumberMono = FontFamily.Monospace

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = DisplaySerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 34.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.25).sp
    ),
    headlineLarge = TextStyle(
        fontFamily = DisplaySerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = DisplaySerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = DisplaySerif,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = UiSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.1.sp
    ),
    titleSmall = TextStyle(
        fontFamily = UiSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = UiSans,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.2.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = UiSans,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.15.sp
    ),
    bodySmall = TextStyle(
        fontFamily = UiSans,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.2.sp
    ),
    labelLarge = TextStyle(
        fontFamily = UiSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.3.sp
    ),
    labelMedium = TextStyle(
        fontFamily = UiSans,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.3.sp
    ),
    labelSmall = TextStyle(
        fontFamily = UiSans,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.4.sp
    )
)

// Dedicated style for numeric/ledger content (fees, times, marks, counts).
// Not part of the Material Typography scale on purpose — call directly where used,
// e.g. Text("₹4,500", style = NumberStyle.amount)
object NumberStyle {
    val amount = TextStyle(
        fontFamily = NumberMono,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        letterSpacing = 0.sp
    )
    val amountLarge = TextStyle(
        fontFamily = NumberMono,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        letterSpacing = 0.sp
    )
    val meta = TextStyle(
        fontFamily = NumberMono,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        letterSpacing = 0.sp
    )
}