package com.margasatya.domain.model

import com.google.firebase.Timestamp
import com.margasatya.domain.enums.ExamSessionStatus
import com.margasatya.domain.enums.ExamType

data class ExamSession(
    val id: String = "",
    val examId: String = "",
    val studentId: String = "",
    val nis: String = "",
    val status: ExamSessionStatus = ExamSessionStatus.NOT_STARTED,
    val startedAt: Timestamp? = null,
    val submittedAt: Timestamp? = null,
    val lastActivityAt: Timestamp? = null,
    val type: ExamType = ExamType.IN_APP,
    val deviceInfo: String? = null
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "examId" to examId,
            "studentId" to studentId,
            "nis" to nis,
            "status" to status.name,
            "startedAt" to startedAt,
            "submittedAt" to submittedAt,
            "lastActivityAt" to lastActivityAt,
            "type" to type.name,
            "deviceInfo" to deviceInfo
        )
    }

    companion object {
        fun fromMap(id: String, data: Map<String, Any>): ExamSession {
            return ExamSession(
                id = id,
                examId = data["examId"] as? String ?: "",
                studentId = data["studentId"] as? String ?: "",
                nis = data["nis"] as? String ?: "",
                status = try {
                    ExamSessionStatus.valueOf(data["status"] as? String ?: "NOT_STARTED")
                } catch (e: Exception) {
                    ExamSessionStatus.NOT_STARTED
                },
                startedAt = data["startedAt"] as? Timestamp,
                submittedAt = data["submittedAt"] as? Timestamp,
                lastActivityAt = data["lastActivityAt"] as? Timestamp,
                type = try {
                    ExamType.valueOf(data["type"] as? String ?: "IN_APP")
                } catch (e: Exception) {
                    ExamType.IN_APP
                },
                deviceInfo = data["deviceInfo"] as? String
            )
        }
    }
}
