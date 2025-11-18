package com.margasatya.core.encryption

import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.margasatya.domain.model.EncryptedAnswer
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

interface EncryptionService {
    fun encryptAnswer(
        plainText: String,
        questionId: String,
        sessionId: String
    ): EncryptedAnswer

    fun decryptAnswer(answer: EncryptedAnswer): String
}

class EncryptionServiceImpl(
    private val context: Context
) : EncryptionService {

    companion object {
        private const val TAG = "EncryptionService"
        private const val ALGORITHM = "AES/GCM/NoPadding"
        private const val KEY_SIZE = 256
        private const val IV_SIZE = 12 // GCM standard IV size
        private const val TAG_LENGTH = 128
        private const val PREFS_NAME = "exam_encryption_prefs"
        private const val KEY_ALIAS = "exam_answer_key"
        private const val KEY_VERSION = 1
    }

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val encryptedPrefs = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private fun getOrCreateKey(): SecretKey {
        // Try to get existing key
        val keyString = encryptedPrefs.getString(KEY_ALIAS, null)

        return if (keyString != null) {
            try {
                val keyBytes = Base64.decode(keyString, Base64.NO_WRAP)
                SecretKeySpec(keyBytes, "AES")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to decode existing key, creating new one", e)
                createAndStoreKey()
            }
        } else {
            createAndStoreKey()
        }
    }

    private fun createAndStoreKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(KEY_SIZE)
        val key = keyGenerator.generateKey()

        // Store key
        val keyString = Base64.encodeToString(key.encoded, Base64.NO_WRAP)
        encryptedPrefs.edit().putString(KEY_ALIAS, keyString).apply()

        Log.d(TAG, "New encryption key created and stored")
        return key
    }

    override fun encryptAnswer(
        plainText: String,
        questionId: String,
        sessionId: String
    ): EncryptedAnswer {
        try {
            val key = getOrCreateKey()

            // Generate random IV
            val iv = ByteArray(IV_SIZE)
            SecureRandom().nextBytes(iv)

            // Initialize cipher
            val cipher = Cipher.getInstance(ALGORITHM)
            val gcmSpec = GCMParameterSpec(TAG_LENGTH, iv)
            cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec)

            // Add associated data (questionId and sessionId) for additional security
            val associatedData = "$questionId:$sessionId".toByteArray()
            cipher.updateAAD(associatedData)

            // Encrypt
            val cipherText = cipher.doFinal(plainText.toByteArray())

            return EncryptedAnswer(
                questionId = questionId,
                cipherText = cipherText,
                iv = iv,
                algorithm = ALGORITHM,
                keyVersion = KEY_VERSION
            )
        } catch (e: Exception) {
            Log.e(TAG, "Encryption failed", e)
            throw EncryptionException("Failed to encrypt answer", e)
        }
    }

    override fun decryptAnswer(answer: EncryptedAnswer): String {
        try {
            val key = getOrCreateKey()

            // Initialize cipher
            val cipher = Cipher.getInstance(answer.algorithm)
            val gcmSpec = GCMParameterSpec(TAG_LENGTH, answer.iv)
            cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec)

            // Add the same associated data used during encryption
            val associatedData = answer.questionId.toByteArray()
            cipher.updateAAD(associatedData)

            // Decrypt
            val plainTextBytes = cipher.doFinal(answer.cipherText)

            return String(plainTextBytes)
        } catch (e: Exception) {
            Log.e(TAG, "Decryption failed", e)
            throw EncryptionException("Failed to decrypt answer", e)
        }
    }
}

class EncryptionException(message: String, cause: Throwable? = null) : Exception(message, cause)
