package com.margasatya.data.repository

import com.margasatya.core.util.Resource
import com.margasatya.data.service.ExamAnswerService
import com.margasatya.domain.model.EncryptedAnswer
import com.margasatya.domain.repository.AnswerRepository
import javax.inject.Inject

class AnswerRepositoryImpl @Inject constructor(
    private val examAnswerService: ExamAnswerService
) : AnswerRepository {

    override suspend fun saveAnswer(sessionId: String, answer: EncryptedAnswer): Resource<Unit> {
        return try {
            examAnswerService.saveAnswer(sessionId, answer)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e, "Gagal menyimpan jawaban")
        }
    }

    override suspend fun saveAnswersBatch(
        sessionId: String,
        answers: List<EncryptedAnswer>
    ): Resource<Unit> {
        return try {
            examAnswerService.saveAnswersBatch(sessionId, answers)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e, "Gagal menyimpan jawaban")
        }
    }

    override suspend fun getAnswers(sessionId: String): Resource<List<EncryptedAnswer>> {
        return try {
            val answers = examAnswerService.listAnswers(sessionId)
            Resource.Success(answers)
        } catch (e: Exception) {
            Resource.Error(e, "Gagal mengambil jawaban")
        }
    }
}
