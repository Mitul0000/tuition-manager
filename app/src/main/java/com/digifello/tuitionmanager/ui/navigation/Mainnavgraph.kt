package com.digifello.tuitionmanager.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.digifello.tuitionmanager.ui.addeditbatch.AddEditBatchScreen
import com.digifello.tuitionmanager.ui.addeditbatch.AddEditBatchViewModel
import com.digifello.tuitionmanager.ui.batchdetails.BatchDetailScreen
import com.digifello.tuitionmanager.ui.batchdetails.BatchDetailViewModel
import com.digifello.tuitionmanager.ui.common.AddEditBatchViewModelFactory
import com.digifello.tuitionmanager.ui.common.BatchDetailViewModelFactory
import com.digifello.tuitionmanager.ui.dashboard.DashboardScreen
import com.digifello.tuitionmanager.ui.finance.FinanceScreen
import com.digifello.tuitionmanager.ui.more.MoreScreen
import com.digifello.tuitionmanager.ui.profile.ProfileScreen
import com.digifello.tuitionmanager.ui.questiongenerator.landing.QuestionGeneratorLandingScreen
import com.digifello.tuitionmanager.ui.questiongenerator.mypapers.MyPapersScreen
import com.digifello.tuitionmanager.ui.questiongenerator.papersetup.PaperSetupScreen
import com.digifello.tuitionmanager.ui.questiongenerator.preview.PreviewScreen
import com.digifello.tuitionmanager.ui.questiongenerator.requirements.RequirementsScreen
import com.digifello.tuitionmanager.ui.questiongenerator.review.ReviewScreen
import com.digifello.tuitionmanager.ui.questiongenerator.upload.UploadScreen
import com.digifello.tuitionmanager.ui.students.AllStudentsScreen
import com.digifello.tuitionmanager.ui.students.studentdetail.StudentDetailScreen
import com.digifello.tuitionmanager.ui.today.TodayScreen

// Nested graph living inside MainScaffold's Scaffold content slot.
// Holds the 5 tab roots plus every screen pushed on top of them
// (batch detail/edit, the QGen wizard steps, students, profile).

@Composable
fun MainNavGraph(navController: NavHostController, innerPadding: PaddingValues) {

    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = Modifier.padding(innerPadding)
    ) {

        // ---- Home ----
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onBatchClick = { batchId -> navController.navigate(Screen.BatchDetail.createRoute(batchId)) },
                onAddBatchClick = { navController.navigate(Screen.AddEditBatch.createRoute()) }
            )
        }

        composable(
            route = Screen.AddEditBatch.route,
            arguments = listOf(navArgument("batchId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val batchId = backStackEntry.arguments?.getString("batchId")
            val viewModel: AddEditBatchViewModel = viewModel(factory = AddEditBatchViewModelFactory(batchId))
            AddEditBatchScreen(
                viewModel = viewModel,
                onSaved = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.BatchDetail.route,
            arguments = listOf(navArgument("batchId") { type = NavType.StringType })
        ) { backStackEntry ->
            val batchId = backStackEntry.arguments?.getString("batchId") ?: return@composable
            val viewModel: BatchDetailViewModel = viewModel(factory = BatchDetailViewModelFactory(batchId))
            BatchDetailScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onEditClick = { navController.navigate(Screen.AddEditBatch.createRoute(batchId)) },
                onDeleted = { navController.popBackStack() }
            )
        }

        // ---- Routine ----
        composable(Screen.Today.route) {
            TodayScreen(
                onBatchClick = { batchId -> navController.navigate(Screen.BatchDetail.createRoute(batchId)) }
            )
        }

        // ---- Question Generator (wizard) ----
        composable(Screen.QuestionGeneratorHome.route) {
            QuestionGeneratorLandingScreen(
                onStartNew = { navController.navigate(Screen.QGenUpload.route) },
                onOpenMyPapers = { navController.navigate(Screen.MyPapers.route) }
            )
        }
        composable(Screen.QGenUpload.route) {
            UploadScreen(onContinue = { navController.navigate(Screen.QGenRequirements.route) })
        }
        composable(Screen.QGenRequirements.route) {
            RequirementsScreen(onGenerated = { navController.navigate(Screen.QGenReview.route) })
        }
        composable(Screen.QGenReview.route) {
            ReviewScreen(onContinue = { navController.navigate(Screen.QGenPaperSetup.route) })
        }
        composable(Screen.QGenPaperSetup.route) {
            PaperSetupScreen(onPreview = { navController.navigate(Screen.QGenPreview.route) })
        }
        composable(Screen.QGenPreview.route) {
            PreviewScreen(
                onDone = {
                    navController.popBackStack(Screen.QuestionGeneratorHome.route, inclusive = false)
                }
            )
        }
        composable(Screen.MyPapers.route) {
            MyPapersScreen(onBackClick = { navController.popBackStack() })
        }

        // ---- Finance ----
        composable(Screen.Finance.route) {
            FinanceScreen(
                onBatchClick = { batchId -> navController.navigate(Screen.BatchDetail.createRoute(batchId)) }
            )
        }

        // ---- More hub ----
        composable(Screen.More.route) {
            MoreScreen(
                onStudentsClick = { navController.navigate(Screen.AllStudents.route) },
                onProfileClick = { navController.navigate(Screen.Profile.route) },
                onSettingsClick = { navController.navigate(Screen.Settings.route) },
                onLoggedOut = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.AllStudents.route) {
            AllStudentsScreen(
                onStudentClick = { studentId -> navController.navigate(Screen.StudentDetail.createRoute(studentId)) },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.StudentDetail.route,
            arguments = listOf(navArgument("studentId") { type = NavType.StringType })
        ) { backStackEntry ->
            val studentId = backStackEntry.arguments?.getString("studentId") ?: return@composable
            StudentDetailScreen(
                studentId = studentId,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(onBackClick = { navController.popBackStack() })
        }
    }
}