package com.margasatya.data.repository

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.margasatya.data.service.AdminStatisticsService
import com.margasatya.domain.enums.UserRole
import com.margasatya.domain.model.AdminSummary
import com.margasatya.domain.model.Exam
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import javax.inject.Inject

class FirestoreAdminStatisticsService @Inject constructor(
    private val firestore: FirebaseFirestore
) : AdminStatisticsService {

    companion object {
        private const val TAG = "FirestoreAdminStats"
        private const val COLLECTION_USERS = "users"
        private const val COLLECTION_STUDENTS = "students"
        private const val COLLECTION_EXAMS = "exams"
        private const val COLLECTION_SESSIONS = "examSessions"
    }

    override suspend fun getSummary(): AdminSummary {
        return try {
            // Count teachers
            val teachersSnapshot = firestore.collection(COLLECTION_USERS)
                .whereEqualTo("role", UserRole.GURU.name)
                .get()
                .await()
            val totalTeachers = teachersSnapshot.size()

            // Count students
            val studentsSnapshot = firestore.collection(COLLECTION_STUDENTS)
                .whereEqualTo("isActive", true)
                .get()
                .await()
            val totalStudents = studentsSnapshot.size()

            // Count exams
            val examsSnapshot = firestore.collection(COLLECTION_EXAMS)
                .whereEqualTo("isActive", true)
                .get()
                .await()
            val totalExams = examsSnapshot.size()

            // Count running and finished exams
            val now = Timestamp.now()
            var runningExams = 0
            var finishedExams = 0

            examsSnapshot.documents.forEach { doc ->
                try {
                    val exam = Exam.fromMap(doc.id, doc.data as Map<String, Any>)
                    val startTime = exam.startTime
                    val endTime = exam.endTime

                    if (startTime != null && endTime != null) {
                        when {
                            now.seconds in startTime.seconds..endTime.seconds -> runningExams++
                            now.seconds > endTime.seconds -> finishedExams++
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing exam for statistics", e)
                }
            }

            // Count sessions today
            val startOfDay = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startOfDayTimestamp = Timestamp(startOfDay.time)

            val sessionsSnapshot = firestore.collection(COLLECTION_SESSIONS)
                .whereGreaterThanOrEqualTo("startedAt", startOfDayTimestamp)
                .get()
                .await()
            val sessionsToday = sessionsSnapshot.size()

            AdminSummary(
                totalTeachers = totalTeachers,
                totalStudents = totalStudents,
                totalExams = totalExams,
                runningExams = runningExams,
                finishedExams = finishedExams,
                sessionsToday = sessionsToday
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting admin summary", e)
            AdminSummary()
        }
    }

    override suspend fun getRecentExams(limit: Int): List<Exam> {
        return try {
            val querySnapshot = firestore.collection(COLLECTION_EXAMS)
                .whereEqualTo("isActive", true)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit.toLong())
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
            Log.e(TAG, "Error getting recent exams", e)
            emptyList()
        }
    }
}
