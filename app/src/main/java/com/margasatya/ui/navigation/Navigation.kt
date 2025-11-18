package com.margasatya.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.margasatya.ui.admin.AdminDashboardScreen
import com.margasatya.ui.admin.AdminLoginScreen
import com.margasatya.ui.role.RoleSelectionScreen
import com.margasatya.ui.student.StudentEntryScreen
import com.margasatya.ui.student.StudentExamInAppScreen
import com.margasatya.ui.student.StudentExamSubmissionPendingScreen
import com.margasatya.ui.student.StudentExamWebViewScreen
import com.margasatya.ui.teacher.*

@Composable
fun MargaSatyaNavigation(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Screen.RoleSelection.route
    ) {
        // Role Selection
        composable(Screen.RoleSelection.route) {
            RoleSelectionScreen(navController = navController)
        }

        // Student Routes
        composable(Screen.StudentEntry.route) {
            StudentEntryScreen(navController = navController)
        }

        composable(
            route = Screen.StudentExamWebView.route,
            arguments = listOf(
                navArgument("examId") { type = NavType.StringType },
                navArgument("sessionId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val examId = backStackEntry.arguments?.getString("examId") ?: ""
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            StudentExamWebViewScreen(
                navController = navController,
                examId = examId,
                sessionId = sessionId
            )
        }

        composable(
            route = Screen.StudentExamInApp.route,
            arguments = listOf(
                navArgument("examId") { type = NavType.StringType },
                navArgument("sessionId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val examId = backStackEntry.arguments?.getString("examId") ?: ""
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            StudentExamInAppScreen(
                navController = navController,
                examId = examId,
                sessionId = sessionId
            )
        }

        composable(
            route = Screen.StudentExamSubmissionPending.route,
            arguments = listOf(
                navArgument("sessionId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            StudentExamSubmissionPendingScreen(
                navController = navController,
                sessionId = sessionId
            )
        }

        // Teacher Routes
        composable(Screen.TeacherLogin.route) {
            TeacherLoginScreen(navController = navController)
        }

        composable(Screen.TeacherRegister.route) {
            TeacherRegisterScreen(navController = navController)
        }

        composable(Screen.TeacherHome.route) {
            TeacherHomeScreen(navController = navController)
        }

        composable(Screen.TeacherStudentList.route) {
            TeacherStudentListScreen(navController = navController)
        }

        composable(
            route = Screen.TeacherStudentForm.route,
            arguments = listOf(
                navArgument("studentId") {
                    type = NavType.StringType
                    defaultValue = "new"
                }
            )
        ) { backStackEntry ->
            val studentId = backStackEntry.arguments?.getString("studentId") ?: "new"
            TeacherStudentFormScreen(
                navController = navController,
                studentId = studentId
            )
        }

        composable(Screen.TeacherExamList.route) {
            TeacherExamListScreen(navController = navController)
        }

        composable(
            route = Screen.TeacherExamForm.route,
            arguments = listOf(
                navArgument("examId") {
                    type = NavType.StringType
                    defaultValue = "new"
                }
            )
        ) { backStackEntry ->
            val examId = backStackEntry.arguments?.getString("examId") ?: "new"
            TeacherExamFormScreen(
                navController = navController,
                examId = examId
            )
        }

        // Admin Routes
        composable(Screen.AdminLogin.route) {
            AdminLoginScreen(navController = navController)
        }

        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(navController = navController)
        }
    }
}
