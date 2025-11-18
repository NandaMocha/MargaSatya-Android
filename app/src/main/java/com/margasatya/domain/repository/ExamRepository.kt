package com.margasatya.domain.repository

import com.margasatya.core.util.Resource
import com.margasatya.domain.model.Exam
import com.margasatya.domain.model.ExamDraft
import com.margasatya.domain.model.ExamParticipant
import com.margasatya.domain.model.ExamQuestion
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface following Dependency Inversion Principle
 * Domain layer doesn't know about implementation details
 */
interface ExamRepository {
    suspend fun getExamByCode(code: String): Resource<Exam>
    suspend fun getExamById(examId: String): Resource<Exam>
    suspend fun createExam(draft: ExamDraft): Resource<Exam>
    suspend fun updateExam(exam: Exam): Resource<Unit>
    suspend fun getExamsByTeacher(teacherId: String): Flow<Resource<List<Exam>>>

    suspend fun getQuestions(examId: String): Resource<List<ExamQuestion>>
    suspend fun saveQuestions(examId: String, questions: List<ExamQuestion>): Resource<Unit>

    suspend fun getParticipants(examId: String): Resource<List<ExamParticipant>>
    suspend fun saveParticipants(examId: String, participants: List<ExamParticipant>): Resource<Unit>
    suspend fun isStudentAllowed(nis: String, examId: String): Resource<Boolean>
}
