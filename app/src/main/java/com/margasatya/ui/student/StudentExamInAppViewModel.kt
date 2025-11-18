package com.margasatya.ui.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.margasatya.core.encryption.EncryptionService
import com.margasatya.core.network.NetworkMonitor
import com.margasatya.data.service.ExamAnswerService
import com.margasatya.data.service.ExamService
import com.margasatya.data.service.ExamSessionService
import com.margasatya.domain.enums.ExamSessionStatus
import com.margasatya.domain.enums.QuestionType
import com.margasatya.domain.model.EncryptedAnswer
import com.margasatya.domain.model.ExamQuestion
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuestionAnswerState(
    val questionId: String,
    val answer: String = "",
    val isAnswered: Boolean = false
)

data class ExamInAppUiState(
    val questions: List<ExamQuestion> = emptyList(),
    val answers: Map<String, QuestionAnswerState> = emptyMap(),
    val currentQuestionIndex: Int = 0,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val showSubmitDialog: Boolean = false,
    val isSubmitted: Boolean = false,
    val isSubmissionPending: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class StudentExamInAppViewModel @Inject constructor(
    private val examService: ExamService,
    private val examSessionService: ExamSessionService,
    private val examAnswerService: ExamAnswerService,
    private val encryptionService: EncryptionService,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExamInAppUiState())
    val uiState: StateFlow<ExamInAppUiState> = _uiState.asStateFlow()

    private var sessionId: String = ""
    private var examId: String = ""

    fun loadExam(examId: String, sessionId: String) {
        this.examId = examId
        this.sessionId = sessionId

        viewModelScope.launch {
            try {
                // Load questions
                val questions = examService.listQuestions(examId)

                // Load existing answers
                val existingAnswers = examAnswerService.listAnswers(sessionId)

                // Initialize answer states
                val answerStates = questions.associate { question ->
                    val existingAnswer = existingAnswers.find { it.questionId == question.id }
                    val decryptedAnswer = existingAnswer?.let {
                        try {
                            encryptionService.decryptAnswer(it)
                        } catch (e: Exception) {
                            ""
                        }
                    } ?: ""

                    question.id to QuestionAnswerState(
                        questionId = question.id,
                        answer = decryptedAnswer,
                        isAnswered = decryptedAnswer.isNotEmpty()
                    )
                }

                _uiState.value = _uiState.value.copy(
                    questions = questions,
                    answers = answerStates,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun updateAnswer(questionId: String, answer: String) {
        val current = _uiState.value.answers[questionId] ?: return
        val updated = current.copy(
            answer = answer,
            isAnswered = answer.isNotEmpty()
        )

        _uiState.value = _uiState.value.copy(
            answers = _uiState.value.answers + (questionId to updated)
        )

        // Auto-save answer
        saveCurrentAnswer()
    }

    private fun saveCurrentAnswer() {
        viewModelScope.launch {
            try {
                val currentQuestion = _uiState.value.questions.getOrNull(_uiState.value.currentQuestionIndex)
                    ?: return@launch

                val answerState = _uiState.value.answers[currentQuestion.id] ?: return@launch

                if (answerState.answer.isEmpty()) return@launch

                _uiState.value = _uiState.value.copy(isSaving = true)

                // Encrypt answer
                val encryptedAnswer = encryptionService.encryptAnswer(
                    plainText = answerState.answer,
                    questionId = currentQuestion.id,
                    sessionId = sessionId
                )

                // Save to Firestore
                examAnswerService.saveAnswer(sessionId, encryptedAnswer)

                _uiState.value = _uiState.value.copy(isSaving = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSaving = false)
            }
        }
    }

    fun nextQuestion() {
        val currentIndex = _uiState.value.currentQuestionIndex
        if (currentIndex < _uiState.value.questions.size - 1) {
            saveCurrentAnswer()
            _uiState.value = _uiState.value.copy(currentQuestionIndex = currentIndex + 1)
        }
    }

    fun previousQuestion() {
        val currentIndex = _uiState.value.currentQuestionIndex
        if (currentIndex > 0) {
            saveCurrentAnswer()
            _uiState.value = _uiState.value.copy(currentQuestionIndex = currentIndex - 1)
        }
    }

    fun goToQuestion(index: Int) {
        if (index in _uiState.value.questions.indices) {
            saveCurrentAnswer()
            _uiState.value = _uiState.value.copy(currentQuestionIndex = index)
        }
    }

    fun showSubmitDialog() {
        _uiState.value = _uiState.value.copy(showSubmitDialog = true)
    }

    fun hideSubmitDialog() {
        _uiState.value = _uiState.value.copy(showSubmitDialog = false)
    }

    fun submitExam() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, showSubmitDialog = false)

                // Save all remaining answers
                val allAnswers = _uiState.value.answers.values
                    .filter { it.answer.isNotEmpty() }
                    .map { answerState ->
                        encryptionService.encryptAnswer(
                            plainText = answerState.answer,
                            questionId = answerState.questionId,
                            sessionId = sessionId
                        )
                    }

                // Try to submit with retry
                var retryCount = 0
                var submitted = false

                while (retryCount < 3 && !submitted) {
                    try {
                        examAnswerService.saveAnswersBatch(sessionId, allAnswers)
                        examSessionService.updateSessionStatus(sessionId, ExamSessionStatus.SUBMITTED)
                        submitted = true
                    } catch (e: Exception) {
                        retryCount++
                        if (retryCount < 3) {
                            delay(1000)
                        }
                    }
                }

                if (submitted) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSubmitted = true
                    )
                } else {
                    // Set to submission pending
                    examSessionService.updateSessionStatus(sessionId, ExamSessionStatus.SUBMISSION_PENDING)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSubmissionPending = true
                    )
                }
            } catch (e: Exception) {
                // Network error - go to pending state
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSubmissionPending = true
                )
            }
        }
    }

    fun getCurrentQuestion(): ExamQuestion? {
        return _uiState.value.questions.getOrNull(_uiState.value.currentQuestionIndex)
    }

    fun getCurrentAnswer(): String {
        val currentQuestion = getCurrentQuestion() ?: return ""
        return _uiState.value.answers[currentQuestion.id]?.answer ?: ""
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
