package com.margasatya.data.repository

import com.margasatya.core.util.Resource
import com.margasatya.data.service.ExamService
import com.margasatya.domain.model.Exam
import com.margasatya.domain.model.ExamDraft
import com.margasatya.domain.model.ExamParticipant
import com.margasatya.domain.model.ExamQuestion
import com.margasatya.domain.repository.ExamRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Repository implementation following Clean Architecture
 * Converts data layer to domain layer with proper error handling
 */
class ExamRepositoryImpl @Inject constructor(
    private val examService: ExamService
) : ExamRepository {

    override suspend fun getExamByCode(code: String): Resource<Exam> {
        return try {
            val exam = examService.getExamByCode(code)
            if (exam != null) {
                Resource.Success(exam)
            } else {
                Resource.Error(
                    NoSuchElementException("Exam not found"),
                    "Ujian dengan kode $code tidak ditemukan"
                )
            }
        } catch (e: Exception) {
            Resource.Error(e, "Gagal mengambil data ujian")
        }
    }

    override suspend fun getExamById(examId: String): Resource<Exam> {
        return try {
            // Implementation would get exam by ID from service
            // For now, returning error as service doesn't have this method
            Resource.Error(
                NotImplementedError("Not implemented"),
                "Fitur ini belum diimplementasikan"
            )
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }

    override suspend fun createExam(draft: ExamDraft): Resource<Exam> {
        return try {
            val exam = examService.createExam(draft, draft.teacherId)
            Resource.Success(exam)
        } catch (e: Exception) {
            Resource.Error(e, "Gagal membuat ujian")
        }
    }

    override suspend fun updateExam(exam: Exam): Resource<Unit> {
        return try {
            examService.updateExam(exam)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e, "Gagal mengupdate ujian")
        }
    }

    override suspend fun getExamsByTeacher(teacherId: String): Flow<Resource<List<Exam>>> = flow {
        emit(Resource.Loading)
        try {
            val exams = examService.listExams(teacherId)
            emit(Resource.Success(exams))
        } catch (e: Exception) {
            emit(Resource.Error(e, "Gagal mengambil daftar ujian"))
        }
    }

    override suspend fun getQuestions(examId: String): Resource<List<ExamQuestion>> {
        return try {
            val questions = examService.listQuestions(examId)
            Resource.Success(questions)
        } catch (e: Exception) {
            Resource.Error(e, "Gagal mengambil soal ujian")
        }
    }

    override suspend fun saveQuestions(examId: String, questions: List<ExamQuestion>): Resource<Unit> {
        return try {
            examService.saveQuestions(examId, questions)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e, "Gagal menyimpan soal")
        }
    }

    override suspend fun getParticipants(examId: String): Resource<List<ExamParticipant>> {
        return try {
            val participants = examService.listParticipants(examId)
            Resource.Success(participants)
        } catch (e: Exception) {
            Resource.Error(e, "Gagal mengambil daftar peserta")
        }
    }

    override suspend fun saveParticipants(
        examId: String,
        participants: List<ExamParticipant>
    ): Resource<Unit> {
        return try {
            examService.saveParticipants(examId, participants)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e, "Gagal menyimpan peserta")
        }
    }

    override suspend fun isStudentAllowed(nis: String, examId: String): Resource<Boolean> {
        return try {
            // Need to get this from service - for now using dummy
            val participants = examService.listParticipants(examId)
            val isAllowed = participants.any { it.nis == nis && it.allowed }
            Resource.Success(isAllowed)
        } catch (e: Exception) {
            Resource.Error(e, "Gagal memeriksa akses siswa")
        }
    }
}
