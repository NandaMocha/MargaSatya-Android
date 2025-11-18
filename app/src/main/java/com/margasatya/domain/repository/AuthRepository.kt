package com.margasatya.domain.repository

import com.margasatya.core.util.Resource
import com.margasatya.domain.model.User

interface AuthRepository {
    suspend fun login(email: String, password: String): Resource<User>
    suspend fun register(name: String, email: String, password: String): Resource<User>
    suspend fun logout(): Resource<Unit>
    suspend fun getCurrentUser(): Resource<User?>
}
