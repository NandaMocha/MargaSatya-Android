package com.margasatya.data.repository

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.margasatya.data.service.ExamService
import com.margasatya.domain.model.Exam
import com.margasatya.domain.model.ExamDraft
import com.margasatya.domain.model.ExamParticipant
import com.margasatya.domain.model.ExamQuestion
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirestoreExamService @Inject constructor(
    private val firestore: FirebaseFirestore
) : ExamService {

    companion object {
        private const val TAG = "FirestoreExamService"
        private const val COLLECTION_EXAMS = "exams"
        private const val SUBCOLLECTION_QUESTIONS = "questions"
        private const val SUBCOLLECTION_PARTICIPANTS = "participants"
    }

    override suspend fun getExamByCode(code: String): Exam? {
        return try {
            val querySnapshot = firestore.collection(COLLECTION_EXAMS)
                .whereEqualTo("examCode", code)
                .whereEqualTo("isActive", true)
                .limit(1)
                .get()
                .await()

            if (querySnapshot.documents.isNotEmpty()) {
                val doc = querySnapshot.documents[0]
                Exam.fromMap(doc.id, doc.data as Map<String, Any>)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting exam by code", e)
            null
        }
    }

    override suspend fun createExam(draft: ExamDraft, teacherId: String): Exam {
        return try {
            val now = Timestamp.now()
            val examData = mapOf(
                "teacherId" to teacherId,
                "title" to draft.title,
                "description" to draft.description,
                "examCode" to draft.examCode,
                "type" to draft.type.name,
                "formUrl" to draft.formUrl,
                "startTime" to draft.startTime,
                "endTime" to draft.endTime,
                "durationMinutes" to draft.durationMinutes,
                "isActive" to true,
                "createdAt" to now,
                "updatedAt" to now
            )

            val docRef = firestore.collection(COLLECTION_EXAMS)
                .add(examData)
                .await()

            Exam.fromMap(docRef.id, examData)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating exam", e)
            throw e
        }
    }

    override suspend fun updateExam(exam: Exam) {
        try {
            val now = Timestamp.now()
            val updateData = exam.toMap().toMutableMap()
            updateData["updatedAt"] = now

            firestore.collection(COLLECTION_EXAMS)
                .document(exam.id)
                .set(updateData)
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating exam", e)
            throw e
        }
    }

    override suspend fun listExams(teacherId: String): List<Exam> {
        return try {
            val querySnapshot = firestore.collection(COLLECTION_EXAMS)
                .whereEqualTo("teacherId", teacherId)
                .whereEqualTo("isActive", true)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            querySnapshot.documents.mapNotNull { doc ->
                try {
                    Exam.fromMap(doc.id, doc.data as Map<String, Any>)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing exam document", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error listing exams", e)
            emptyList()
        }
    }

    override suspend fun listQuestions(examId: String): List<ExamQuestion> {
        return try {
            val querySnapshot = firestore.collection(COLLECTION_EXAMS)
                .document(examId)
                .collection(SUBCOLLECTION_QUESTIONS)
                .orderBy("order")
                .get()
                .await()

            querySnapshot.documents.mapNotNull { doc ->
                try {
                    ExamQuestion.fromMap(doc.id, doc.data as Map<String, Any>)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing question document", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error listing questions", e)
            emptyList()
        }
    }

    override suspend fun saveQuestions(examId: String, questions: List<ExamQuestion>) {
        try {
            val batch = firestore.batch()
            val questionsRef = firestore.collection(COLLECTION_EXAMS)
                .document(examId)
                .collection(SUBCOLLECTION_QUESTIONS)

            // Delete existing questions
            val existingDocs = questionsRef.get().await()
            existingDocs.documents.forEach { doc ->
                batch.delete(doc.reference)
            }

            // Add new questions
            questions.forEach { question ->
                val docRef = if (question.id.isNotEmpty()) {
                    questionsRef.document(question.id)
                } else {
                    questionsRef.document()
                }
                batch.set(docRef, question.toMap())
            }

            batch.commit().await()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving questions", e)
            throw e
        }
    }

    override suspend fun listParticipants(examId: String): List<ExamParticipant> {
        return try {
            val querySnapshot = firestore.collection(COLLECTION_EXAMS)
                .document(examId)
                .collection(SUBCOLLECTION_PARTICIPANTS)
                .get()
                .await()

            querySnapshot.documents.mapNotNull { doc ->
                try {
                    ExamParticipant.fromMap(doc.id, doc.data as Map<String, Any>)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing participant document", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error listing participants", e)
            emptyList()
        }
    }

    override suspend fun saveParticipants(examId: String, participants: List<ExamParticipant>) {
        try {
            val batch = firestore.batch()
            val participantsRef = firestore.collection(COLLECTION_EXAMS)
                .document(examId)
                .collection(SUBCOLLECTION_PARTICIPANTS)

            // Delete existing participants
            val existingDocs = participantsRef.get().await()
            existingDocs.documents.forEach { doc ->
                batch.delete(doc.reference)
            }

            // Add new participants
            participants.forEach { participant ->
                val docRef = if (participant.id.isNotEmpty()) {
                    participantsRef.document(participant.id)
                } else {
                    participantsRef.document()
                }
                batch.set(docRef, participant.toMap())
            }

            batch.commit().await()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving participants", e)
            throw e
        }
    }
}
