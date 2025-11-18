package com.margasatya.data.repository

import com.margasatya.core.util.Resource
import com.margasatya.data.service.ExamSessionService
import com.margasatya.domain.enums.ExamSessionStatus
import com.margasatya.domain.model.Exam
import com.margasatya.domain.model.ExamSession
import com.margasatya.domain.model.Student
import com.margasatya.domain.repository.ExamSessionRepository
import javax.inject.Inject

class ExamSessionRepositoryImpl @Inject constructor(
    private val examSessionService: ExamSessionService
) : ExamSessionRepository {

    override suspend fun createOrResumeSession(exam: Exam, student: Student): Resource<ExamSession> {
        return try {
            val session = examSessionService.createOrResumeSession(exam, student)
            Resource.Success(session)
        } catch (e: IllegalStateException) {
            // Session already submitted
            Resource.Error(e, e.message ?: "Session already completed")
        } catch (e: Exception) {
            Resource.Error(e, "Gagal membuat sesi ujian")
        }
    }

    override suspend fun getSession(examId: String, studentId: String): Resource<ExamSession> {
        return try {
            val session = examSessionService.getSession(examId, studentId)
            if (session != null) {
                Resource.Success(session)
            } else {
                Resource.Error(
                    NoSuchElementException("Session not found"),
                    "Sesi ujian tidak ditemukan"
                )
            }
        } catch (e: Exception) {
            Resource.Error(e, "Gagal mengambil sesi ujian")
        }
    }

    override suspend fun updateSessionStatus(
        sessionId: String,
        status: ExamSessionStatus
    ): Resource<Unit> {
        return try {
            examSessionService.updateSessionStatus(sessionId, status)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e, "Gagal mengupdate status sesi")
        }
    }

    override suspend fun getSessionsByExam(examId: String): Resource<List<ExamSession>> {
        return try {
            val sessions = examSessionService.listSessions(examId)
            Resource.Success(sessions)
        } catch (e: Exception) {
            Resource.Error(e, "Gagal mengambil daftar sesi")
        }
    }
}
