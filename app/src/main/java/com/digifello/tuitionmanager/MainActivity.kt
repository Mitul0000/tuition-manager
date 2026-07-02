package com.digifello.tuitionmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.digifello.tuitionmanager.data.repository.AppMaintenance
import com.digifello.tuitionmanager.ui.navigation.NavGraph
import com.digifello.tuitionmanager.ui.navigation.Screen
import com.digifello.tuitionmanager.ui.theme.TuitionManagerTheme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            auth.signInAnonymously().addOnCompleteListener {
                lifecycleScope.launch {
                    AppMaintenance().runMonthlyPaymentMaintenance()
                }
            }
        } else {
            lifecycleScope.launch {
                AppMaintenance().runMonthlyPaymentMaintenance()
            }
        }

        setContent {
            TuitionManagerTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { AppBottomNavBar(navController) }
    ) { innerPadding ->
        androidx.compose.foundation.layout.Box(modifier = androidx.compose.ui.Modifier.padding(innerPadding)) {
            NavGraph(navController = navController)
        }
    }
}

@Composable
fun AppBottomNavBar(navController: NavHostController) {
    val items = listOf(
        Triple(Screen.Dashboard, "Home", Icons.Filled.Home),
        Triple(Screen.Today, "Today", Icons.Filled.DateRange),
        Triple(Screen.AllStudents, "Students", Icons.Filled.Person),
        Triple(Screen.Finance, "Finance", Icons.Filled.AccountBalanceWallet)
    )

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    NavigationBar {
        items.forEach { (screen, label, icon) ->
            NavigationBarItem(
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(Screen.Dashboard.route)
                        launchSingleTop = true
                    }
                },
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label) },
                alwaysShowLabel = false
            )
        }
        // Question Bank — distinct lock icon + "Soon" label, clearly different from Finance
        NavigationBarItem(
            selected = false,
            onClick = { /* no-op — feature not built yet */ },
            icon = { Icon(Icons.Filled.Lock, contentDescription = "Question Bank — coming soon") },
            label = { Text("Soon") },
            alwaysShowLabel = true,
            enabled = false
        )
    }
}