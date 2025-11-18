package com.margasatya.domain.repository

import com.margasatya.core.util.Resource
import com.margasatya.domain.model.EncryptedAnswer

interface AnswerRepository {
    suspend fun saveAnswer(sessionId: String, answer: EncryptedAnswer): Resource<Unit>
    suspend fun saveAnswersBatch(sessionId: String, answers: List<EncryptedAnswer>): Resource<Unit>
    suspend fun getAnswers(sessionId: String): Resource<List<EncryptedAnswer>>
}
