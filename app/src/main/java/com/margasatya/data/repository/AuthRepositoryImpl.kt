package com.margasatya.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.margasatya.core.util.Resource
import com.margasatya.domain.enums.UserRole
import com.margasatya.domain.model.User
import com.margasatya.domain.repository.AuthRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override suspend fun login(email: String, password: String): Resource<User> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val userId = result.user?.uid
                ?: return Resource.Error(
                    IllegalStateException("User ID is null"),
                    "Gagal mendapatkan informasi user"
                )

            // Get user data from Firestore
            val userDoc = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            if (userDoc.exists()) {
                val user = User.fromMap(userId, userDoc.data as Map<String, Any>)
                Resource.Success(user)
            } else {
                Resource.Error(
                    NoSuchElementException("User data not found"),
                    "Data user tidak ditemukan"
                )
            }
        } catch (e: Exception) {
            Resource.Error(e, "Login gagal. Periksa email dan password Anda.")
        }
    }

    override suspend fun register(name: String, email: String, password: String): Resource<User> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val userId = result.user?.uid
                ?: return Resource.Error(
                    IllegalStateException("User ID is null"),
                    "Gagal membuat akun"
                )

            val now = Timestamp.now()
            val userData = mapOf(
                "id" to userId,
                "name" to name,
                "email" to email,
                "role" to UserRole.GURU.name,
                "createdAt" to now,
                "updatedAt" to now
            )

            firestore.collection("users")
                .document(userId)
                .set(userData)
                .await()

            val user = User.fromMap(userId, userData)
            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error(e, "Registrasi gagal. Silakan coba lagi.")
        }
    }

    override suspend fun logout(): Resource<Unit> {
        return try {
            firebaseAuth.signOut()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e, "Gagal logout")
        }
    }

    override suspend fun getCurrentUser(): Resource<User?> {
        return try {
            val currentUser = firebaseAuth.currentUser
            if (currentUser != null) {
                val userDoc = firestore.collection("users")
                    .document(currentUser.uid)
                    .get()
                    .await()

                if (userDoc.exists()) {
                    val user = User.fromMap(currentUser.uid, userDoc.data as Map<String, Any>)
                    Resource.Success(user)
                } else {
                    Resource.Success(null)
                }
            } else {
                Resource.Success(null)
            }
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }
}
