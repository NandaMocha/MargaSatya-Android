package com.margasatya.ui.navigation

sealed class Screen(val route: String) {
    // Role Selection
    object RoleSelection : Screen("role_selection")

    // Student
    object StudentEntry : Screen("student_entry")
    object StudentExamWebView : Screen("student_exam_webview/{examId}/{sessionId}") {
        fun createRoute(examId: String, sessionId: String) = "student_exam_webview/$examId/$sessionId"
    }
    object StudentExamInApp : Screen("student_exam_inapp/{examId}/{sessionId}") {
        fun createRoute(examId: String, sessionId: String) = "student_exam_inapp/$examId/$sessionId"
    }
    object StudentExamSubmissionPending : Screen("student_exam_pending/{sessionId}") {
        fun createRoute(sessionId: String) = "student_exam_pending/$sessionId"
    }

    // Teacher
    object TeacherLogin : Screen("teacher_login")
    object TeacherRegister : Screen("teacher_register")
    object TeacherHome : Screen("teacher_home")
    object TeacherStudentList : Screen("teacher_student_list")
    object TeacherStudentForm : Screen("teacher_student_form/{studentId}") {
        fun createRoute(studentId: String = "new") = "teacher_student_form/$studentId"
    }
    object TeacherExamList : Screen("teacher_exam_list")
    object TeacherExamForm : Screen("teacher_exam_form/{examId}") {
        fun createRoute(examId: String = "new") = "teacher_exam_form/$examId"
    }

    // Admin
    object AdminLogin : Screen("admin_login")
    object AdminDashboard : Screen("admin_dashboard")
}
