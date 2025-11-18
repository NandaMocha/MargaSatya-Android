package com.margasatya.domain.usecase.student

import com.google.firebase.Timestamp
import com.margasatya.core.util.Resource
import com.margasatya.core.util.Validator
import com.margasatya.domain.model.Exam
import com.margasatya.domain.model.ExamSession
import com.margasatya.domain.repository.ExamRepository
import com.margasatya.domain.repository.ExamSessionRepository
import com.margasatya.domain.repository.StudentRepository
import com.margasatya.domain.usecase.BaseUseCase
import javax.inject.Inject

/**
 * Use Case for starting an exam
 * Encapsulates all business logic for exam validation and session creation
 */
class StartExamUseCase @Inject constructor(
    private val examRepository: ExamRepository,
    private val studentRepository: StudentRepository,
    private val examSessionRepository: ExamSessionRepository
) : BaseUseCase<StartExamUseCase.Params, ExamSession>() {

    data class Params(
        val nis: String,
        val examCode: String
    )

    override suspend fun execute(params: Params): Resource<ExamSession> {
        // Validate NIS
        val nisValidation = Validator.validateNis(params.nis)
        if (!nisValidation.isValid) {
            return Resource.Error(
                IllegalArgumentException(nisValidation.errorMessage)
            )
        }

        // Validate exam code
        val codeValidation = Validator.validateExamCode(params.examCode)
        if (!codeValidation.isValid) {
            return Resource.Error(
                IllegalArgumentException(codeValidation.errorMessage)
            )
        }

        // Get exam by code
        val examResult = examRepository.getExamByCode(params.examCode)
        if (examResult is Resource.Error) {
            return Resource.Error(
                IllegalStateException("Kode ujian tidak ditemukan."),
                "Kode ujian tidak ditemukan."
            )
        }

        val exam = (examResult as Resource.Success).data

        // Check if student is allowed
        val allowedResult = examRepository.isStudentAllowed(params.nis, exam.id)
        if (allowedResult is Resource.Error || (allowedResult as Resource.Success).data == false) {
            return Resource.Error(
                IllegalStateException("Nomor Induk tidak terdaftar untuk ujian ini."),
                "Nomor Induk tidak terdaftar untuk ujian ini."
            )
        }

        // Validate exam time
        val timeValidation = validateExamTime(exam)
        if (timeValidation is Resource.Error) {
            return timeValidation
        }

        // Get student
        val studentResult = studentRepository.getStudentByNis(params.nis)
        if (studentResult is Resource.Error) {
            return Resource.Error(
                IllegalStateException("Siswa tidak ditemukan."),
                "Siswa tidak ditemukan."
            )
        }

        val student = (studentResult as Resource.Success).data

        // Create or resume session
        return examSessionRepository.createOrResumeSession(exam, student)
    }

    private fun validateExamTime(exam: Exam): Resource<Unit> {
        val now = Timestamp.now()

        if (exam.startTime != null && now.seconds < exam.startTime.seconds) {
            return Resource.Error(
                IllegalStateException("Ujian belum dimulai."),
                "Ujian belum dimulai."
            )
        }

        if (exam.endTime != null && now.seconds > exam.endTime.seconds) {
            return Resource.Error(
                IllegalStateException("Ujian sudah berakhir."),
                "Ujian sudah berakhir."
            )
        }

        return Resource.Success(Unit)
    }
}
