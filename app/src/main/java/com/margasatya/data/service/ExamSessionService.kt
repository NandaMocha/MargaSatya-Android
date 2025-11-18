package com.margasatya.data.service

import com.margasatya.domain.enums.ExamSessionStatus
import com.margasatya.domain.model.Exam
import com.margasatya.domain.model.ExamSession
import com.margasatya.domain.model.Student

interface ExamSessionService {
    suspend fun createOrResumeSession(
        exam: Exam,
        student: Student
    ): ExamSession

    suspend fun updateSessionStatus(
        sessionId: String,
        status: ExamSessionStatus
    )

    suspend fun getSession(
        examId: String,
        studentId: String
    ): ExamSession?

    suspend fun listSessions(examId: String): List<ExamSession>
}
