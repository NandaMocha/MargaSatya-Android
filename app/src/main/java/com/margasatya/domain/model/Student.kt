package com.margasatya.domain.model

import com.google.firebase.Timestamp

data class Student(
    val id: String = "",
    val teacherId: String = "",
    val nis: String = "",
    val name: String = "",
    val className: String? = null,
    val isActive: Boolean = true,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "teacherId" to teacherId,
            "nis" to nis,
            "name" to name,
            "className" to className,
            "isActive" to isActive,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt
        )
    }

    companion object {
        fun fromMap(id: String, data: Map<String, Any>): Student {
            return Student(
                id = id,
                teacherId = data["teacherId"] as? String ?: "",
                nis = data["nis"] as? String ?: "",
                name = data["name"] as? String ?: "",
                className = data["className"] as? String,
                isActive = data["isActive"] as? Boolean ?: true,
                createdAt = data["createdAt"] as? Timestamp,
                updatedAt = data["updatedAt"] as? Timestamp
            )
        }
    }
}
