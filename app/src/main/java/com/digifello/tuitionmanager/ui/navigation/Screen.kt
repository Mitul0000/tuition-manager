package com.digifello.tuitionmanager.ui.navigation

/**
 * Every navigable destination in the app, defined in one place.
 * Using a sealed class (instead of raw strings scattered everywhere)
 * means typos in route names get caught by the compiler.
 */
sealed class Screen(val route: String) {

    // ---- Pre-auth ----
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Signup : Screen("signup")
    object Otp : Screen("otp?email={email}&purpose={purpose}") {
        // purpose: "signup" or "reset" — determines what happens after verification
        fun createRoute(email: String, purpose: String) = "otp?email=$email&purpose=$purpose"
    }
    object ForgotPassword : Screen("forgot_password")

    // ---- Bottom nav tabs (5) ----
    object Dashboard : Screen("dashboard")             // Home
    object Today : Screen("today")                      // Routine
    object QuestionGeneratorHome : Screen("qgen_home")   // Question Generator (wizard entry)
    object Finance : Screen("finance")
    object More : Screen("more")

    // ---- Home flow ----
    object BatchDetail : Screen("batch/{batchId}") {
        fun createRoute(batchId: String) = "batch/$batchId"
    }
    object AddEditBatch : Screen("batch/edit?batchId={batchId}") {
        // batchId is optional — omit it entirely for "add new batch" mode
        fun createRoute(batchId: String? = null) =
            if (batchId != null) "batch/edit?batchId=$batchId" else "batch/edit"
    }

    // ---- Question Generator wizard (steps 1-5 + saved list) ----
    object QGenUpload : Screen("qgen/upload")
    object QGenRequirements : Screen("qgen/requirements")
    object QGenReview : Screen("qgen/review")
    object QGenPaperSetup : Screen("qgen/paper_setup")
    object QGenPreview : Screen("qgen/preview")
    object MyPapers : Screen("qgen/my_papers")

    // ---- More hub ----
    object AllStudents : Screen("students")
    object StudentDetail : Screen("students/{studentId}") {
        fun createRoute(studentId: String) = "students/$studentId"
    }
    object Profile : Screen("profile")
    object Settings : Screen("settings")
}