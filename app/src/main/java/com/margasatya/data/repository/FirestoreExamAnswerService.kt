package com.margasatya.data.repository

import android.util.Base64
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.margasatya.data.service.ExamAnswerService
import com.margasatya.domain.model.EncryptedAnswer
import com.margasatya.domain.model.EncryptionMetadata
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirestoreExamAnswerService @Inject constructor(
    private val firestore: FirebaseFirestore
) : ExamAnswerService {

    companion object {
        private const val TAG = "FirestoreAnswerService"
        private const val COLLECTION_SESSIONS = "examSessions"
        private const val SUBCOLLECTION_ANSWERS = "answers"
    }

    override suspend fun saveAnswer(sessionId: String, answer: EncryptedAnswer) {
        try {
            val now = Timestamp.now()
            val answerData = mapOf(
                "questionId" to answer.questionId,
                "encryptedPayload" to Base64.encodeToString(answer.cipherText, Base64.NO_WRAP),
                "encryptionMetadata" to mapOf(
                    "iv" to Base64.encodeToString(answer.iv, Base64.NO_WRAP),
                    "algorithm" to answer.algorithm,
                    "keyVersion" to answer.keyVersion
                ),
                "createdAt" to now,
                "updatedAt" to now
            )

            // Use questionId as document ID for idempotency
            firestore.collection(COLLECTION_SESSIONS)
                .document(sessionId)
                .collection(SUBCOLLECTION_ANSWERS)
                .document(answer.questionId)
                .set(answerData)
                .await()

            // Update session last activity
            firestore.collection(COLLECTION_SESSIONS)
                .document(sessionId)
                .update("lastActivityAt", now)
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving answer", e)
            throw e
        }
    }

    override suspend fun saveAnswersBatch(sessionId: String, answers: List<EncryptedAnswer>) {
        try {
            val batch = firestore.batch()
            val now = Timestamp.now()
            val answersRef = firestore.collection(COLLECTION_SESSIONS)
                .document(sessionId)
                .collection(SUBCOLLECTION_ANSWERS)

            answers.forEach { answer ->
                val answerData = mapOf(
                    "questionId" to answer.questionId,
                    "encryptedPayload" to Base64.encodeToString(answer.cipherText, Base64.NO_WRAP),
                    "encryptionMetadata" to mapOf(
                        "iv" to Base64.encodeToString(answer.iv, Base64.NO_WRAP),
                        "algorithm" to answer.algorithm,
                        "keyVersion" to answer.keyVersion
                    ),
                    "createdAt" to now,
                    "updatedAt" to now
                )

                val docRef = answersRef.document(answer.questionId)
                batch.set(docRef, answerData)
            }

            // Update session last activity
            val sessionRef = firestore.collection(COLLECTION_SESSIONS)
                .document(sessionId)
            batch.update(sessionRef, "lastActivityAt", now)

            batch.commit().await()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving answers batch", e)
            throw e
        }
    }

    override suspend fun listAnswers(sessionId: String): List<EncryptedAnswer> {
        return try {
            val querySnapshot = firestore.collection(COLLECTION_SESSIONS)
                .document(sessionId)
                .collection(SUBCOLLECTION_ANSWERS)
                .get()
                .await()

            querySnapshot.documents.mapNotNull { doc ->
                try {
                    val data = doc.data ?: return@mapNotNull null
                    val questionId = data["questionId"] as? String ?: return@mapNotNull null
                    val encryptedPayload = data["encryptedPayload"] as? String ?: return@mapNotNull null
                    val metadata = data["encryptionMetadata"] as? Map<String, Any> ?: return@mapNotNull null

                    val iv = metadata["iv"] as? String ?: return@mapNotNull null
                    val algorithm = metadata["algorithm"] as? String ?: "AES/GCM/NoPadding"
                    val keyVersion = (metadata["keyVersion"] as? Long)?.toInt() ?: 1

                    EncryptedAnswer(
                        questionId = questionId,
                        cipherText = Base64.decode(encryptedPayload, Base64.NO_WRAP),
                        iv = Base64.decode(iv, Base64.NO_WRAP),
                        algorithm = algorithm,
                        keyVersion = keyVersion
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing answer document", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error listing answers", e)
            emptyList()
        }
    }
}
