package com.digifello.tuitionmanager.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Wallet
import androidx.compose.ui.graphics.vector.ImageVector

// Defines the 5 bottom nav tabs, in order. Question Generator is a
// first-class tab per product decision — not tucked inside a menu.
// Students is intentionally NOT here — it lives inside More.

data class BottomNavItem(
    val route: String,
    val label: String,
    val filledIcon: ImageVector,
    val outlinedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(
        route = Screen.Dashboard.route,
        label = "Home",
        filledIcon = Icons.Filled.Home,
        outlinedIcon = Icons.Outlined.Home
    ),
    BottomNavItem(
        route = Screen.Today.route,
        label = "Routine",
        filledIcon = Icons.Filled.CalendarToday,
        outlinedIcon = Icons.Outlined.CalendarToday
    ),
    BottomNavItem(
        route = Screen.QuestionGeneratorHome.route,
        label = "Questions",
        filledIcon = Icons.Filled.AutoAwesome,
        outlinedIcon = Icons.Outlined.AutoAwesome
    ),
    BottomNavItem(
        route = Screen.Finance.route,
        label = "Finance",
        filledIcon = Icons.Filled.Wallet,
        outlinedIcon = Icons.Outlined.Wallet
    ),
    BottomNavItem(
        route = Screen.More.route,
        label = "More",
        filledIcon = Icons.Filled.MoreHoriz,
        outlinedIcon = Icons.Outlined.MoreHoriz
    )
)