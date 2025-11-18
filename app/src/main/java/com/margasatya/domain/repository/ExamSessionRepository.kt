package com.margasatya.domain.repository

import com.margasatya.core.util.Resource
import com.margasatya.domain.enums.ExamSessionStatus
import com.margasatya.domain.model.Exam
import com.margasatya.domain.model.ExamSession
import com.margasatya.domain.model.Student

interface ExamSessionRepository {
    suspend fun createOrResumeSession(exam: Exam, student: Student): Resource<ExamSession>
    suspend fun getSession(examId: String, studentId: String): Resource<ExamSession>
    suspend fun updateSessionStatus(sessionId: String, status: ExamSessionStatus): Resource<Unit>
    suspend fun getSessionsByExam(examId: String): Resource<List<ExamSession>>
}
