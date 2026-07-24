package com.digifello.tuitionmanager.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.digifello.tuitionmanager.ui.auth.forgotpassword.ForgotPasswordScreen
import com.digifello.tuitionmanager.ui.auth.login.LoginScreen
import com.digifello.tuitionmanager.ui.auth.otp.OtpScreen
import com.digifello.tuitionmanager.ui.auth.signup.SignupScreen
import com.digifello.tuitionmanager.ui.splash.SplashScreen

// Root graph: decides between the pre-auth flow and the main 5-tab app.
// SplashScreen is the actual start destination — it checks the Firebase
// session and navigates onward before anything else renders.

@Composable
fun RootNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Splash.route) {

        composable(Screen.Splash.route) {
            SplashScreen(
                onAuthenticated = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onUnauthenticated = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onSignupClick = { navController.navigate(Screen.Signup.route) },
                onForgotPasswordClick = { navController.navigate(Screen.ForgotPassword.route) }
            )
        }

        composable(Screen.Signup.route) {
            SignupScreen(
                onSignupSubmitted = { email ->
                    navController.navigate(Screen.Otp.createRoute(email, purpose = "signup"))
                },
                onBackToLogin = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Otp.route,
            arguments = listOf(
                navArgument("email") { type = NavType.StringType; defaultValue = "" },
                navArgument("purpose") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email").orEmpty()
            val purpose = backStackEntry.arguments?.getString("purpose").orEmpty()
            OtpScreen(
                email = email,
                purpose = purpose,
                onVerified = {
                    if (purpose == "signup") {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    } else {
                        navController.popBackStack(Screen.Login.route, inclusive = false)
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onResetEmailSent = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() }
            )
        }

        // Everything past auth lives inside MainScaffold, which owns the
        // bottom nav bar and its own nested NavHost (see MainNavGraph.kt).
        composable(Screen.Dashboard.route) {
            MainScaffold(navController = navController)
        }
    }
}