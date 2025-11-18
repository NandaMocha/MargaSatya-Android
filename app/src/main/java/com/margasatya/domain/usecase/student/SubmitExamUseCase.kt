package com.margasatya.domain.usecase.student

import com.margasatya.core.util.Resource
import com.margasatya.domain.enums.ExamSessionStatus
import com.margasatya.domain.model.EncryptedAnswer
import com.margasatya.domain.repository.AnswerRepository
import com.margasatya.domain.repository.ExamSessionRepository
import com.margasatya.domain.usecase.BaseUseCase
import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * Use Case for submitting exam with retry logic
 */
class SubmitExamUseCase @Inject constructor(
    private val answerRepository: AnswerRepository,
    private val examSessionRepository: ExamSessionRepository
) : BaseUseCase<SubmitExamUseCase.Params, SubmitExamUseCase.Result>() {

    data class Params(
        val sessionId: String,
        val answers: List<EncryptedAnswer>,
        val maxRetries: Int = 3
    )

    sealed class Result {
        object Success : Result()
        object Pending : Result()
    }

    override suspend fun execute(params: Params): Resource<Result> {
        var retryCount = 0
        var lastException: Exception? = null

        // Try to submit with retry
        while (retryCount < params.maxRetries) {
            try {
                // Save answers
                val saveResult = answerRepository.saveAnswersBatch(params.sessionId, params.answers)
                if (saveResult is Resource.Error) {
                    lastException = saveResult.exception as? Exception
                    retryCount++
                    if (retryCount < params.maxRetries) {
                        delay(1000L * retryCount) // Exponential backoff
                    }
                    continue
                }

                // Update session status
                val statusResult = examSessionRepository.updateSessionStatus(
                    params.sessionId,
                    ExamSessionStatus.SUBMITTED
                )

                if (statusResult is Resource.Error) {
                    lastException = statusResult.exception as? Exception
                    retryCount++
                    if (retryCount < params.maxRetries) {
                        delay(1000L * retryCount)
                    }
                    continue
                }

                // Success
                return Resource.Success(Result.Success)
            } catch (e: Exception) {
                lastException = e
                retryCount++
                if (retryCount < params.maxRetries) {
                    delay(1000L * retryCount)
                }
            }
        }

        // All retries failed, set to pending
        try {
            examSessionRepository.updateSessionStatus(
                params.sessionId,
                ExamSessionStatus.SUBMISSION_PENDING
            )
            return Resource.Success(Result.Pending)
        } catch (e: Exception) {
            return Resource.Error(
                lastException ?: e,
                "Gagal mengirim jawaban. Silakan coba lagi."
            )
        }
    }
}
