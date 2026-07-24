package com.digifello.tuitionmanager.ui.theme

import androidx.compose.ui.graphics.Color

// ---------------------------------------------------------------------
// TutorDesk palette — "Teacher's Ledger"
// Deep ink navy + warm marigold, built for a professional-but-colorful,
// data-dense app. Every color below is used deliberately somewhere —
// none of these are unused Material scaffolding leftovers.
// ---------------------------------------------------------------------

// Brand
val InkNavy = Color(0xFF15213B)          // primary text, nav bar, headers
val InkNavyLight = Color(0xFF25365C)     // hover/pressed states on navy surfaces
val Marigold = Color(0xFFF0A63A)         // primary CTA, active tab, FAB
val MarigoldDeep = Color(0xFFD98A1E)     // pressed marigold
val SlateBlue = Color(0xFF5A7FA6)        // secondary accent, links, icons
val SkyTint = Color(0xFFEAF2FB)          // soft accent backgrounds (chips, info banners)

// Status — payment states (used consistently everywhere: badges, edges, dots)
val PaidGreen = Color(0xFF2F9E5B)
val PaidGreenBg = Color(0xFFE4F6EB)
val PartialAmber = Color(0xFFDB9A1F)
val PartialAmberBg = Color(0xFFFCF1DA)
val UnpaidCrimson = Color(0xFFC8434F)
val UnpaidCrimsonBg = Color(0xFFFBE7E9)

// Weekday dot strip accents (7 distinct, muted-but-colorful, cycle Mon-Sun)
val DotMon = Color(0xFFE07A5F)
val DotTue = Color(0xFFF2B134)
val DotWed = Color(0xFF6DAA6E)
val DotThu = Color(0xFF4C8DA6)
val DotFri = Color(0xFF7D6BAD)
val DotSat = Color(0xFFD46A9F)
val DotSun = Color(0xFF8E8E8E)

// Neutrals
val ChalkWhite = Color(0xFFFAF9F6)       // app background
val CardWhite = Color(0xFFFFFFFF)        // card surfaces
val Ink60 = Color(0xFF5B6474)            // secondary text
val Ink40 = Color(0xFF8A93A3)            // tertiary text / hints
val Hairline = Color(0xFFE6E5E1)         // borders/dividers
val ScrimNavy = Color(0xCC15213B)        // bottom sheet scrim

// Dark theme neutrals
val DarkBackground = Color(0xFF0E1626)
val DarkSurface = Color(0xFF17233B)
val DarkSurfaceRaised = Color(0xFF1F2E4C)
val DarkHairline = Color(0xFF2C3B5C)
val DarkTextPrimary = Color(0xFFF3F1EA)
val DarkTextSecondary = Color(0xFFAFB8C8)

// Feedback
val ErrorRed = UnpaidCrimson
val InfoBlue = SlateBlue