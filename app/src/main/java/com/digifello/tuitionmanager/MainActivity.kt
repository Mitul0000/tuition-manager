package com.digifello.tuitionmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.digifello.tuitionmanager.ui.navigation.NavGraph
import com.digifello.tuitionmanager.ui.navigation.Screen
import com.digifello.tuitionmanager.ui.theme.TuitionManagerTheme
import com.google.firebase.auth.FirebaseAuth
import androidx.lifecycle.lifecycleScope
import com.digifello.tuitionmanager.data.repository.AppMaintenance
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            auth.signInAnonymously().addOnCompleteListener {
                // Run maintenance only after auth is confirmed ready,
                // since Firestore rules require request.auth != null
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
            NavGraph()
        }
    }
}

@Composable
fun AppBottomNavBar(navController: androidx.navigation.NavHostController) {
    val items = listOf(
        Triple(Screen.Dashboard, "Dashboard", Icons.Filled.Home),
        Triple(Screen.Today, "Today", Icons.Filled.DateRange),
        Triple(Screen.AllStudents, "Students", Icons.Filled.Person),
        Triple(Screen.Finance, "Finance", Icons.Filled.Star)
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
                label = { Text(label) }
            )
        }
        // Question Bank — disabled/"coming soon" tab, not yet functional
        NavigationBarItem(
            selected = false,
            onClick = { /* no-op for now */ },
            icon = { Icon(Icons.Filled.Star, contentDescription = "Question Bank") },
            label = { Text("Questions") },
            enabled = false
        )
    }
}