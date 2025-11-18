package com.margasatya.domain.model

import com.google.firebase.Timestamp

data class ExamParticipant(
    val id: String = "",
    val studentId: String = "",
    val nis: String = "",
    val allowed: Boolean = true,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "studentId" to studentId,
            "nis" to nis,
            "allowed" to allowed,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt
        )
    }

    companion object {
        fun fromMap(id: String, data: Map<String, Any>): ExamParticipant {
            return ExamParticipant(
                id = id,
                studentId = data["studentId"] as? String ?: "",
                nis = data["nis"] as? String ?: "",
                allowed = data["allowed"] as? Boolean ?: true,
                createdAt = data["createdAt"] as? Timestamp,
                updatedAt = data["updatedAt"] as? Timestamp
            )
        }
    }
}
