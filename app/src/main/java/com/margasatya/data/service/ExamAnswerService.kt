package com.margasatya.data.service

import com.margasatya.domain.model.EncryptedAnswer

interface ExamAnswerService {
    suspend fun saveAnswer(
        sessionId: String,
        answer: EncryptedAnswer
    )

    suspend fun saveAnswersBatch(
        sessionId: String,
        answers: List<EncryptedAnswer>
    )

    suspend fun listAnswers(
        sessionId: String
    ): List<EncryptedAnswer>
}
