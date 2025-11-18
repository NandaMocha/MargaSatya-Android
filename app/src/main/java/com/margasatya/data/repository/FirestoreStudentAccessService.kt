package com.margasatya.data.repository

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.margasatya.data.service.StudentAccessService
import com.margasatya.domain.model.Student
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirestoreStudentAccessService @Inject constructor(
    private val firestore: FirebaseFirestore
) : StudentAccessService {

    companion object {
        private const val TAG = "FirestoreStudentService"
        private const val COLLECTION_STUDENTS = "students"
        private const val COLLECTION_EXAMS = "exams"
        private const val SUBCOLLECTION_PARTICIPANTS = "participants"
    }

    override suspend fun getStudentByNis(nis: String, teacherId: String?): Student? {
        return try {
            var query = firestore.collection(COLLECTION_STUDENTS)
                .whereEqualTo("nis", nis)
                .whereEqualTo("isActive", true)

            if (teacherId != null) {
                query = query.whereEqualTo("teacherId", teacherId)
            }

            val querySnapshot = query.limit(1).get().await()

            if (querySnapshot.documents.isNotEmpty()) {
                val doc = querySnapshot.documents[0]
                Student.fromMap(doc.id, doc.data as Map<String, Any>)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting student by NIS", e)
            null
        }
    }

    override suspend fun createStudent(student: Student): Student {
        return try {
            val now = Timestamp.now()
            val studentData = student.toMap().toMutableMap()
            studentData["createdAt"] = now
            studentData["updatedAt"] = now
            studentData["isActive"] = true

            val docRef = firestore.collection(COLLECTION_STUDENTS)
                .add(studentData)
                .await()

            student.copy(
                id = docRef.id,
                createdAt = now,
                updatedAt = now
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error creating student", e)
            throw e
        }
    }

    override suspend fun updateStudent(student: Student) {
        try {
            val now = Timestamp.now()
            val updateData = student.toMap().toMutableMap()
            updateData["updatedAt"] = now

            firestore.collection(COLLECTION_STUDENTS)
                .document(student.id)
                .set(updateData)
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating student", e)
            throw e
        }
    }

    override suspend fun listStudents(teacherId: String): List<Student> {
        return try {
            val querySnapshot = firestore.collection(COLLECTION_STUDENTS)
                .whereEqualTo("teacherId", teacherId)
                .whereEqualTo("isActive", true)
                .orderBy("name")
                .get()
                .await()

            querySnapshot.documents.mapNotNull { doc ->
                try {
                    Student.fromMap(doc.id, doc.data as Map<String, Any>)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing student document", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error listing students", e)
            emptyList()
        }
    }

    override suspend fun isStudentAllowed(nis: String, examId: String): Boolean {
        return try {
            val querySnapshot = firestore.collection(COLLECTION_EXAMS)
                .document(examId)
                .collection(SUBCOLLECTION_PARTICIPANTS)
                .whereEqualTo("nis", nis)
                .whereEqualTo("allowed", true)
                .limit(1)
                .get()
                .await()

            querySnapshot.documents.isNotEmpty()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking student access", e)
            false
        }
    }
}
