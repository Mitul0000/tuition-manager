package com.digifello.tutordesk.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.digifello.tutordesk.ui.Screens.questionGenerator.QuestionGeneratorScreen
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.digifello.tutordesk.ui.Screens.Home.MainScaffold
import com.digifello.tutordesk.ui.Screens.addBatch.AddBatchScreen
import com.digifello.tutordesk.ui.Screens.auth.SignupScreen.SignupScreen
import com.digifello.tutordesk.ui.Screens.auth.forgotPassword.ForgotPasswordScreen
import com.digifello.tutordesk.ui.Screens.auth.loginScreen.LoginScreen
import com.digifello.tutordesk.ui.Screens.batchDetails.BatchDetailsScreen
import com.digifello.tutordesk.ui.Screens.finance.FinanceScreen
import com.digifello.tutordesk.ui.Screens.navigation.Routes
import com.digifello.tutordesk.ui.Screens.splash.AnimatedOnboardingScreen
import com.digifello.tutordesk.ui.Screens.splash.OnboardingPreferences
import com.digifello.tutordesk.ui.Screens.splash.Splashscreen
import com.digifello.tutordesk.ui.Screens.more.HelpScreen
import com.digifello.tutordesk.ui.Screens.more.AboutScreen
import com.digifello.tutordesk.ui.Screens.more.PrivacyScreen
import com.digifello.tutordesk.ui.Screens.studentSearch.StudentSearchScreen
import com.digifello.tutordesk.ui.Screens.auth.verify.EmailVerificationScreen
import com.digifello.tutordesk.ui.Screens.savedPapers.SavedPapersScreen


@Composable
fun TutorDeskNavGraph(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val onboardingPrefs = remember { OnboardingPreferences(context) }

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
        enterTransition = { androidx.compose.animation.EnterTransition.None },
        exitTransition = { androidx.compose.animation.ExitTransition.None },
        popEnterTransition = { androidx.compose.animation.EnterTransition.None },
        popExitTransition = { androidx.compose.animation.ExitTransition.None }
    ) {
        composable(Routes.SPLASH) {
            Splashscreen(
                onNavigationToOnboarding = {
                    navController.navigate(Routes.ONBOARDING) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onNavigationToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onNavigationToEmailVerification = {
                    navController.navigate(Routes.EMAIL_VERIFICATION) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onNavigationToMain = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.ONBOARDING) {
            AnimatedOnboardingScreen(
                onFinished = {
                    onboardingPrefs.hasSeenOnboarding = true
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToSignup = { navController.navigate(Routes.SIGNUP) },
                onNavigateToForgotPassword = { navController.navigate(Routes.FORGOT_PASSWORD) }
            )
        }

        composable(Routes.SIGNUP) {
            SignupScreen(
                onSuccess = {
                    navController.navigate(Routes.EMAIL_VERIFICATION) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        composable(Routes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        composable(Routes.EMAIL_VERIFICATION) {
            EmailVerificationScreen(
                onVerified = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.EMAIL_VERIFICATION) { inclusive = true }
                    }
                },
                onGoBack = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.EMAIL_VERIFICATION) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.MAIN) {
            MainScaffold(
                onAddBatchClick = { navController.navigate(Routes.ADD_BATCH) },
                onBatchClick = { batch ->
                    navController.navigate("${Routes.BATCH_DETAILS}/${batch.id}")
                },
                onSearchStudentClick = { navController.navigate(Routes.STUDENT_SEARCH) },
                onChangePasswordClick = { navController.navigate(Routes.FORGOT_PASSWORD) },
                onHelpClick = { navController.navigate(Routes.HELP) },
                onAboutClick = { navController.navigate(Routes.ABOUT) },
                onPrivacyClick = { navController.navigate(Routes.PRIVACY) },
                onSavedPapersClick = { navController.navigate(Routes.SAVED_PAPERS) },
                onLoggedOut = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) // clear back stack so back button can't return to the app
                    }
                },
                onBackClickGenerator = {navController.navigate(Routes.MAIN)}
            )
        }

        composable (Routes.FINANCE){
            FinanceScreen(
                onBatchClick = {batch ->
                    navController.navigate("${Routes.BATCH_DETAILS}/${batch.id}")
                }
            )
        }

        composable(
            route = "${Routes.BATCH_DETAILS}/{batchId}",
            arguments = listOf(navArgument("batchId") { type = NavType.StringType })
        ) { backStackEntry ->
            val batchId = backStackEntry.arguments?.getString("batchId") ?: ""
            BatchDetailsScreen(
                batchId = batchId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ADD_BATCH) {
            AddBatchScreen(
                onBatchCreated = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.STUDENT_SEARCH) {
            StudentSearchScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.HELP) {
            HelpScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.ABOUT) {
            AboutScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.PRIVACY) {
            PrivacyScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.QUESTION_GENERATOR) {
            QuestionGeneratorScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SAVED_PAPERS) {
            SavedPapersScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }

}