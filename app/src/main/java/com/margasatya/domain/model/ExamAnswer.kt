package com.margasatya.domain.model

import com.google.firebase.Timestamp

data class ExamAnswer(
    val id: String = "",
    val questionId: String = "",
    val encryptedPayload: String = "",
    val encryptionMetadata: EncryptionMetadata? = null,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "questionId" to questionId,
            "encryptedPayload" to encryptedPayload,
            "encryptionMetadata" to encryptionMetadata?.toMap(),
            "createdAt" to createdAt,
            "updatedAt" to updatedAt
        )
    }

    companion object {
        fun fromMap(id: String, data: Map<String, Any>): ExamAnswer {
            return ExamAnswer(
                id = id,
                questionId = data["questionId"] as? String ?: "",
                encryptedPayload = data["encryptedPayload"] as? String ?: "",
                encryptionMetadata = (data["encryptionMetadata"] as? Map<String, Any>)?.let {
                    EncryptionMetadata.fromMap(it)
                },
                createdAt = data["createdAt"] as? Timestamp,
                updatedAt = data["updatedAt"] as? Timestamp
            )
        }
    }
}

data class EncryptionMetadata(
    val iv: String = "",
    val algorithm: String = "AES/GCM/NoPadding",
    val keyVersion: Int = 1
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "iv" to iv,
            "algorithm" to algorithm,
            "keyVersion" to keyVersion
        )
    }

    companion object {
        fun fromMap(data: Map<String, Any>): EncryptionMetadata {
            return EncryptionMetadata(
                iv = data["iv"] as? String ?: "",
                algorithm = data["algorithm"] as? String ?: "AES/GCM/NoPadding",
                keyVersion = (data["keyVersion"] as? Long)?.toInt() ?: 1
            )
        }
    }
}

data class EncryptedAnswer(
    val questionId: String,
    val cipherText: ByteArray,
    val iv: ByteArray,
    val algorithm: String = "AES/GCM/NoPadding",
    val keyVersion: Int = 1
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EncryptedAnswer

        if (questionId != other.questionId) return false
        if (!cipherText.contentEquals(other.cipherText)) return false
        if (!iv.contentEquals(other.iv)) return false
        if (algorithm != other.algorithm) return false
        if (keyVersion != other.keyVersion) return false

        return true
    }

    override fun hashCode(): Int {
        var result = questionId.hashCode()
        result = 31 * result + cipherText.contentHashCode()
        result = 31 * result + iv.contentHashCode()
        result = 31 * result + algorithm.hashCode()
        result = 31 * result + keyVersion
        return result
    }
}
