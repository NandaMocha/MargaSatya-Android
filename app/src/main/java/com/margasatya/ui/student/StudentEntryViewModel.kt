package com.margasatya.ui.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.margasatya.data.service.ExamService
import com.margasatya.data.service.ExamSessionService
import com.margasatya.data.service.StudentAccessService
import com.margasatya.domain.enums.ExamSessionStatus
import com.margasatya.domain.enums.ExamType
import com.margasatya.domain.model.Exam
import com.margasatya.domain.model.ExamSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StudentEntryUiState(
    val nis: String = "",
    val examCode: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val exam: Exam? = null,
    val session: ExamSession? = null,
    val navigateToExam: Boolean = false
)

@HiltViewModel
class StudentEntryViewModel @Inject constructor(
    private val examService: ExamService,
    private val studentAccessService: StudentAccessService,
    private val examSessionService: ExamSessionService
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentEntryUiState())
    val uiState: StateFlow<StudentEntryUiState> = _uiState.asStateFlow()

    fun updateNis(nis: String) {
        _uiState.value = _uiState.value.copy(nis = nis, error = null)
    }

    fun updateExamCode(code: String) {
        _uiState.value = _uiState.value.copy(examCode = code, error = null)
    }

    fun startExam() {
        viewModelScope.launch {
            val nis = _uiState.value.nis.trim()
            val examCode = _uiState.value.examCode.trim()

            // Validate input
            if (nis.isEmpty()) {
                _uiState.value = _uiState.value.copy(error = "Nomor Induk Siswa wajib diisi.")
                return@launch
            }

            if (examCode.isEmpty()) {
                _uiState.value = _uiState.value.copy(error = "Kode ujian wajib diisi.")
                return@launch
            }

            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                // Get exam by code
                val exam = examService.getExamByCode(examCode)
                if (exam == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Kode ujian tidak ditemukan."
                    )
                    return@launch
                }

                // Check if student is allowed
                val isAllowed = studentAccessService.isStudentAllowed(nis, exam.id)
                if (!isAllowed) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Nomor Induk tidak terdaftar untuk ujian ini."
                    )
                    return@launch
                }

                // Check exam time
                val now = Timestamp.now()
                if (exam.startTime != null && now.seconds < exam.startTime.seconds) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Ujian belum dimulai."
                    )
                    return@launch
                }

                if (exam.endTime != null && now.seconds > exam.endTime.seconds) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Ujian sudah berakhir."
                    )
                    return@launch
                }

                // Get student
                val student = studentAccessService.getStudentByNis(nis)
                if (student == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Siswa tidak ditemukan."
                    )
                    return@launch
                }

                // Create or resume session
                val session = examSessionService.createOrResumeSession(exam, student)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    exam = exam,
                    session = session,
                    navigateToExam = true
                )
            } catch (e: IllegalStateException) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Kamu sudah menyelesaikan ujian ini."
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Terjadi kesalahan. Silakan coba lagi."
                )
            }
        }
    }

    fun clearNavigation() {
        _uiState.value = _uiState.value.copy(navigateToExam = false)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
