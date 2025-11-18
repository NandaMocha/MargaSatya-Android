package com.margasatya.domain.model

import com.google.firebase.Timestamp
import com.margasatya.domain.enums.QuestionType

data class ExamQuestion(
    val id: String = "",
    val order: Int = 0,
    val type: QuestionType = QuestionType.MULTIPLE_CHOICE,
    val questionText: String = "",
    val options: List<String> = emptyList(),
    val correctOptionIndex: Int? = null,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "order" to order,
            "type" to type.name,
            "questionText" to questionText,
            "options" to options,
            "correctOptionIndex" to correctOptionIndex,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt
        )
    }

    companion object {
        fun fromMap(id: String, data: Map<String, Any>): ExamQuestion {
            return ExamQuestion(
                id = id,
                order = (data["order"] as? Long)?.toInt() ?: 0,
                type = try {
                    QuestionType.valueOf(data["type"] as? String ?: "MULTIPLE_CHOICE")
                } catch (e: Exception) {
                    QuestionType.MULTIPLE_CHOICE
                },
                questionText = data["questionText"] as? String ?: "",
                options = (data["options"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                correctOptionIndex = (data["correctOptionIndex"] as? Long)?.toInt(),
                createdAt = data["createdAt"] as? Timestamp,
                updatedAt = data["updatedAt"] as? Timestamp
            )
        }
    }
}
