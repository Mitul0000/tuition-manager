package com.digifello.tuitionmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.digifello.tuitionmanager.ui.navigation.RootNavGraph
import com.digifello.tuitionmanager.ui.theme.TuitionManagerTheme

// Entry point only. No auth logic and no maintenance jobs happen here
// directly anymore — SplashScreen owns the "is there a session?" check,
// and the monthly-payment maintenance job runs once we know the user
// IS authenticated (see SplashScreen -> onAuthenticated).

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TuitionManagerTheme {
                TutorDeskApp()
            }
        }
    }
}

@Composable
fun TutorDeskApp() {
    val navController = rememberNavController()
    RootNavGraph(navController = navController)
}