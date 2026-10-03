package com.krishiai.app.domain.repository

import com.krishiai.app.data.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun getCurrentUser(): Flow<User?>
    suspend fun login(email: String, password: String): Result<User>
    suspend fun signup(user: User, password: String): Result<User>
    suspend fun logout(): Result<Unit>
    suspend fun resetPassword(email: String): Result<Unit>
    suspend fun uploadProfilePhoto(bytes: ByteArray): Result<String>
    suspend fun getUserProfile(uid: String): Result<User>
}
