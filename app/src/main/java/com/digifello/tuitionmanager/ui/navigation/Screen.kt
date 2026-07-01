package com.digifello.tuitionmanager.ui.navigation

/**
 * Every navigable destination in the app, defined in one place.
 * Using a sealed class (instead of raw strings scattered everywhere)
 * means typos in route names get caught by the compiler.
 */
sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Today : Screen("today")
    object AllStudents : Screen("students")
    object Finance : Screen("finance")
    object QuestionBank : Screen("question_bank")   // disabled/"coming soon" tab

    object BatchDetail : Screen("batch/{batchId}") {
        fun createRoute(batchId: String) = "batch/$batchId"
    }

    object AddEditBatch : Screen("batch/edit?batchId={batchId}") {
        // batchId is optional — omit it entirely for "add new batch" mode
        fun createRoute(batchId: String? = null) =
            if (batchId != null) "batch/edit?batchId=$batchId" else "batch/edit"
    }
}