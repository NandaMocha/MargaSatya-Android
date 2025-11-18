package com.margasatya.domain.model

import com.google.firebase.Timestamp
import com.margasatya.domain.enums.UserRole

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val role: UserRole = UserRole.SISWA,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "name" to name,
            "email" to email,
            "role" to role.name,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt
        )
    }

    companion object {
        fun fromMap(id: String, data: Map<String, Any>): User {
            return User(
                id = id,
                name = data["name"] as? String ?: "",
                email = data["email"] as? String ?: "",
                role = try {
                    UserRole.valueOf(data["role"] as? String ?: "SISWA")
                } catch (e: Exception) {
                    UserRole.SISWA
                },
                createdAt = data["createdAt"] as? Timestamp,
                updatedAt = data["updatedAt"] as? Timestamp
            )
        }
    }
}
