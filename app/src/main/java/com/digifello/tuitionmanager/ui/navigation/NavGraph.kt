package com.digifello.tuitionmanager.ui.navigation

import androidx.compose.runtime.Composable
import com.digifello.tuitionmanager.ui.addeditbatch.AddEditBatchScreen
import com.digifello.tuitionmanager.ui.questionbank.QuestionBankScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.digifello.tuitionmanager.ui.batchdetail.BatchDetailScreen
import com.digifello.tuitionmanager.ui.batchdetail.BatchDetailViewModel
import com.digifello.tuitionmanager.ui.common.BatchDetailViewModelFactory
import com.digifello.tuitionmanager.ui.dashboard.DashboardScreen
import com.digifello.tuitionmanager.ui.finance.FinanceScreen
import com.digifello.tuitionmanager.ui.students.AllStudentsScreen
import com.digifello.tuitionmanager.ui.today.TodayScreen
import com.digifello.tuitionmanager.ui.common.AddEditBatchViewModelFactory
@Composable
fun NavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Dashboard.route) {

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onBatchClick = { batchId ->
                    navController.navigate(Screen.BatchDetail.createRoute(batchId))
                },
                onAddBatchClick = {
                    navController.navigate(Screen.AddEditBatch.createRoute())
                }
            )
        }

        composable(Screen.Today.route) {
            TodayScreen(
                onBatchClick = { batchId ->
                    navController.navigate(Screen.BatchDetail.createRoute(batchId))
                }
            )
        }

        composable(Screen.AllStudents.route) {
            AllStudentsScreen()
        }

        composable(Screen.Finance.route) {
            FinanceScreen(
                onBatchClick = { batchId ->
                    navController.navigate(Screen.BatchDetail.createRoute(batchId))
                }
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
            val viewModel: AddEditBatchViewModel = viewModel(
                factory = AddEditBatchViewModelFactory(batchId)
            )
            AddEditBatchScreen(
                viewModel = viewModel,
                onSaved = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.QuestionBank.route) {
            QuestionBankScreen()
        }

        composable(
            route = Screen.BatchDetail.route,
            arguments = listOf(navArgument("batchId") { type = NavType.StringType })
        ) { backStackEntry ->
            val batchId = backStackEntry.arguments?.getString("batchId") ?: return@composable
            val viewModel: BatchDetailViewModel = viewModel(
                factory = BatchDetailViewModelFactory(batchId)
            )
            BatchDetailScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onEditClick = { navController.navigate(Screen.AddEditBatch.createRoute(batchId)) },
                onDeleted = { navController.popBackStack() }
            )
        }

        // AddEditBatch and QuestionBank routes will be added once those screens are built
    }
}