package com.margasatya.data.repository

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.margasatya.data.service.ExamSessionService
import com.margasatya.domain.enums.ExamSessionStatus
import com.margasatya.domain.model.Exam
import com.margasatya.domain.model.ExamSession
import com.margasatya.domain.model.Student
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirestoreExamSessionService @Inject constructor(
    private val firestore: FirebaseFirestore
) : ExamSessionService {

    companion object {
        private const val TAG = "FirestoreSessionService"
        private const val COLLECTION_SESSIONS = "examSessions"
    }

    override suspend fun createOrResumeSession(exam: Exam, student: Student): ExamSession {
        return try {
            // Check if session exists
            val existingSession = getSession(exam.id, student.id)

            if (existingSession != null) {
                // Check if already submitted
                if (existingSession.status == ExamSessionStatus.SUBMITTED) {
                    throw IllegalStateException("Exam already submitted")
                }

                // Resume session
                if (existingSession.status == ExamSessionStatus.NOT_STARTED) {
                    // Update to IN_PROGRESS
                    updateSessionStatus(existingSession.id, ExamSessionStatus.IN_PROGRESS)
                    existingSession.copy(status = ExamSessionStatus.IN_PROGRESS)
                } else {
                    existingSession
                }
            } else {
                // Create new session
                val now = Timestamp.now()
                val sessionData = mapOf(
                    "examId" to exam.id,
                    "studentId" to student.id,
                    "nis" to student.nis,
                    "status" to ExamSessionStatus.IN_PROGRESS.name,
                    "startedAt" to now,
                    "lastActivityAt" to now,
                    "type" to exam.type.name,
                    "deviceInfo" to android.os.Build.MODEL
                )

                val docRef = firestore.collection(COLLECTION_SESSIONS)
                    .add(sessionData)
                    .await()

                ExamSession.fromMap(docRef.id, sessionData)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating or resuming session", e)
            throw e
        }
    }

    override suspend fun updateSessionStatus(sessionId: String, status: ExamSessionStatus) {
        try {
            val now = Timestamp.now()
            val updateData = mutableMapOf<String, Any>(
                "status" to status.name,
                "lastActivityAt" to now
            )

            if (status == ExamSessionStatus.SUBMITTED) {
                updateData["submittedAt"] = now
            }

            firestore.collection(COLLECTION_SESSIONS)
                .document(sessionId)
                .update(updateData)
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating session status", e)
            throw e
        }
    }

    override suspend fun getSession(examId: String, studentId: String): ExamSession? {
        return try {
            val querySnapshot = firestore.collection(COLLECTION_SESSIONS)
                .whereEqualTo("examId", examId)
                .whereEqualTo("studentId", studentId)
                .limit(1)
                .get()
                .await()

            if (querySnapshot.documents.isNotEmpty()) {
                val doc = querySnapshot.documents[0]
                ExamSession.fromMap(doc.id, doc.data as Map<String, Any>)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting session", e)
            null
        }
    }

    override suspend fun listSessions(examId: String): List<ExamSession> {
        return try {
            val querySnapshot = firestore.collection(COLLECTION_SESSIONS)
                .whereEqualTo("examId", examId)
                .orderBy("startedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            querySnapshot.documents.mapNotNull { doc ->
                try {
                    ExamSession.fromMap(doc.id, doc.data as Map<String, Any>)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing session document", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error listing sessions", e)
            emptyList()
        }
    }
}
