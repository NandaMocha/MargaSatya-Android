package com.margasatya.domain.model

import com.google.firebase.Timestamp

data class AppConfig(
    val id: String = "default",
    val minAppVersion: String = "1.0.0",
    val isMaintenance: Boolean = false,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "minAppVersion" to minAppVersion,
            "isMaintenance" to isMaintenance,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt
        )
    }

    companion object {
        fun fromMap(id: String, data: Map<String, Any>): AppConfig {
            return AppConfig(
                id = id,
                minAppVersion = data["minAppVersion"] as? String ?: "1.0.0",
                isMaintenance = data["isMaintenance"] as? Boolean ?: false,
                createdAt = data["createdAt"] as? Timestamp,
                updatedAt = data["updatedAt"] as? Timestamp
            )
        }
    }
}
