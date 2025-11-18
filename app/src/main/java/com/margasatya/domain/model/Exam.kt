package com.margasatya.domain.model

import com.google.firebase.Timestamp
import com.margasatya.domain.enums.ExamType

data class Exam(
    val id: String = "",
    val teacherId: String = "",
    val title: String = "",
    val description: String = "",
    val examCode: String = "",
    val type: ExamType = ExamType.IN_APP,
    val formUrl: String? = null,
    val startTime: Timestamp? = null,
    val endTime: Timestamp? = null,
    val durationMinutes: Int? = null,
    val isActive: Boolean = true,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "teacherId" to teacherId,
            "title" to title,
            "description" to description,
            "examCode" to examCode,
            "type" to type.name,
            "formUrl" to formUrl,
            "startTime" to startTime,
            "endTime" to endTime,
            "durationMinutes" to durationMinutes,
            "isActive" to isActive,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt
        )
    }

    companion object {
        fun fromMap(id: String, data: Map<String, Any>): Exam {
            return Exam(
                id = id,
                teacherId = data["teacherId"] as? String ?: "",
                title = data["title"] as? String ?: "",
                description = data["description"] as? String ?: "",
                examCode = data["examCode"] as? String ?: "",
                type = try {
                    ExamType.valueOf(data["type"] as? String ?: "IN_APP")
                } catch (e: Exception) {
                    ExamType.IN_APP
                },
                formUrl = data["formUrl"] as? String,
                startTime = data["startTime"] as? Timestamp,
                endTime = data["endTime"] as? Timestamp,
                durationMinutes = (data["durationMinutes"] as? Long)?.toInt(),
                isActive = data["isActive"] as? Boolean ?: true,
                createdAt = data["createdAt"] as? Timestamp,
                updatedAt = data["updatedAt"] as? Timestamp
            )
        }
    }
}

data class ExamDraft(
    val teacherId: String,
    val title: String,
    val description: String,
    val examCode: String,
    val type: ExamType,
    val formUrl: String? = null,
    val startTime: Timestamp? = null,
    val endTime: Timestamp? = null,
    val durationMinutes: Int? = null
)
