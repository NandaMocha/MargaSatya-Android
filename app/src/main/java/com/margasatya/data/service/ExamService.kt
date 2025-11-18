package com.margasatya.data.service

import com.margasatya.domain.model.Exam
import com.margasatya.domain.model.ExamDraft
import com.margasatya.domain.model.ExamParticipant
import com.margasatya.domain.model.ExamQuestion

interface ExamService {
    suspend fun getExamByCode(code: String): Exam?
    suspend fun createExam(draft: ExamDraft, teacherId: String): Exam
    suspend fun updateExam(exam: Exam)
    suspend fun listExams(teacherId: String): List<Exam>

    suspend fun listQuestions(examId: String): List<ExamQuestion>
    suspend fun saveQuestions(examId: String, questions: List<ExamQuestion>)

    suspend fun listParticipants(examId: String): List<ExamParticipant>
    suspend fun saveParticipants(examId: String, participants: List<ExamParticipant>)
}
