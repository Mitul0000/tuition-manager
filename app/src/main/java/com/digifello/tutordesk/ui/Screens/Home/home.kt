package com.digifello.tutordesk.ui.Screens.Home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digifello.tutordesk.R
import com.digifello.tutordesk.ui.Screens.Batchlistcontent.BatchListContent
import com.digifello.tutordesk.ui.Screens.questionGenerator.QuestionGeneratorScreen
import com.digifello.tutordesk.ui.theme.CardWhite
import com.digifello.tutordesk.ui.theme.CardWhiteDark
import com.digifello.tutordesk.ui.theme.ChalkWhite
import com.digifello.tutordesk.ui.theme.ChalkWhiteDark
import com.digifello.tutordesk.ui.theme.Ink_Navy
import com.digifello.tutordesk.ui.theme.Ink_NavyDark
import com.digifello.tutordesk.ui.theme.Marigold
import com.digifello.tutordesk.ui.theme.SlateBlue
import com.digifello.tutordesk.ui.theme.SlateBlueDark

data class BottomNavItem(
    val label: String,
    val topBarTitle: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val bottomNavItems = listOf(
    BottomNavItem("Home", "Tutor's Desk", Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem("Routine", "Routine", Icons.Filled.CalendarToday, Icons.Outlined.CalendarToday),
    BottomNavItem("Generator", "Question Generator", Icons.Filled.Quiz, Icons.Outlined.Quiz),
    BottomNavItem("Finance", "Finance", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet),
    BottomNavItem("More", "More", Icons.Filled.MoreHoriz, Icons.Outlined.MoreHoriz)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    onAddBatchClick: () -> Unit = {},
    onBatchClick: (com.digifello.tutordesk.data.model.Batch) -> Unit = {},
    onSearchStudentClick: () -> Unit = {},
    onChangePasswordClick: () -> Unit = {},
    onHelpClick: () -> Unit = {},
    onAboutClick: () -> Unit = {},
    onPrivacyClick: () -> Unit = {},
    onSavedPapersClick: () -> Unit = {},
    onLoggedOut: () -> Unit = {},
    onBackClickGenerator:() ->Unit = {}
) {

    var selectedTab by rememberSaveable { mutableStateOf(0) }

    // Any tab other than Home (0) should return to Home on system back,
    // instead of popping the NavHost back stack and exiting past MAIN.
    BackHandler(enabled = selectedTab != 0) {
        selectedTab = 0
    }

    val isDark = isSystemInDarkTheme()
    val backgroundColor = if (isDark) ChalkWhiteDark else ChalkWhite
    val surfaceColor = if (isDark) CardWhiteDark else CardWhite
    val textColor = if (isDark) Ink_NavyDark else Ink_Navy
    val secondaryColor = if (isDark) SlateBlueDark else SlateBlue

    Scaffold(
        containerColor = backgroundColor,

        topBar = {
            TopAppBar(

                title = {
                    Text(
                        text = bottomNavItems[selectedTab].topBarTitle,
                        style = MaterialTheme.typography.titleLarge, // serif
                        color = textColor,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor,
                    titleContentColor = textColor
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = surfaceColor,
                tonalElevation = 4.dp
            ) {
                bottomNavItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == index) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label
                            )
                        },
                        label = {
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Marigold,
                            selectedTextColor = textColor,
                            unselectedIconColor = secondaryColor,
                            unselectedTextColor = secondaryColor,
                            indicatorColor = Marigold.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = onAddBatchClick,
                    containerColor = Marigold,
                    contentColor = Ink_Navy,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Batch"
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when (selectedTab) {
                0 -> BatchListContent(onBatchClick = onBatchClick)
                1 -> com.digifello.tutordesk.ui.Screens.routine.RoutineScreen()
                2-> QuestionGeneratorScreen(onBack = onBackClickGenerator)
                3 -> com.digifello.tutordesk.ui.Screens.finance.FinanceScreen(
                    onBatchClick = onBatchClick
                )
                4 -> com.digifello.tutordesk.ui.Screens.more.MoreScreen(
                    onSearchStudentClick = onSearchStudentClick,
                    onChangePasswordClick = onChangePasswordClick,
                    onHelpClick = onHelpClick,
                    onAboutClick = onAboutClick,
                    onPrivacyClick = onPrivacyClick,
                    onSavedPapersClick = onSavedPapersClick,
                    onLoggedOut = onLoggedOut
                )
                else -> Text(
                    text = "${bottomNavItems[selectedTab].label} coming soon",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = textColor
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun MainScaffoldPreview() {
    MainScaffold()
}