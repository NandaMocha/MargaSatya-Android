package com.margasatya.domain.model

data class AdminSummary(
    val totalTeachers: Int = 0,
    val totalStudents: Int = 0,
    val totalExams: Int = 0,
    val runningExams: Int = 0,
    val finishedExams: Int = 0,
    val sessionsToday: Int = 0
)
