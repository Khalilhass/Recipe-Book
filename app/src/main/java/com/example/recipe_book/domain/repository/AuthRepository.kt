package com.example.recipe_book.domain.repository

import com.example.recipe_book.domain.model.User

interface AuthRepository {

    suspend fun register(
        name: String,
        email: String,
        password: String,
        photoUrl: String,
        country: String
    ): Result<String>

    suspend fun updateProfilePhoto(uid: String, photoUrl: String): Result<Unit>

    suspend fun login(email: String, password: String): Result<User>

    suspend fun logout()

    /** Reads whatever session state DataStore currently holds, without hitting the network. */
    suspend fun isRememberMeEnabled(): Boolean

    suspend fun setRememberMe(enabled: Boolean)

    /** Null if nobody is currently authenticated with Firebase Auth. */
    fun getCurrentUserId(): String?

    suspend fun getCurrentUserProfile(): Result<User>
}

/** Convenience alias used by repository implementations when mapping exceptions. */
typealias AuthResult<T> = Result<T>