package com.example.recipe_book.data.repository

import com.example.recipe_book.data.local.SessionPreferences
import com.example.recipe_book.data.mapper.toDomain
import com.example.recipe_book.data.remote.FirebaseAuthDataSource
import com.example.recipe_book.data.remote.dto.UserDto
import com.example.recipe_book.data.remote.toAppError
import com.example.recipe_book.domain.model.AppException
import com.example.recipe_book.domain.model.User
import com.example.recipe_book.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authDataSource: FirebaseAuthDataSource,
    private val sessionPreferences: SessionPreferences
) : AuthRepository {

    override suspend fun register(
        name: String,
        email: String,
        password: String,
        photoUrl: String,
        country: String
    ): Result<String> = runCatching {
        val userDto = UserDto(name = name, email = email, photoUrl = photoUrl, country = country)
        authDataSource.register(email, password, userDto)
    }.recoverCatching { throwable ->
        throw AppException(throwable.toAppError())
    }

    override suspend fun updateProfilePhoto(uid: String, photoUrl: String): Result<Unit> = runCatching {
        authDataSource.updateProfilePhoto(uid, photoUrl)
    }.recoverCatching { throwable ->
        throw AppException(throwable.toAppError())
    }

    override suspend fun login(email: String, password: String): Result<User> = runCatching {
        val uid = authDataSource.login(email, password)
        val userDto = authDataSource.getUserProfile(uid)
        userDto.toDomain(uid)
    }.recoverCatching { throwable ->
        throw AppException(throwable.toAppError())
    }

    override suspend fun logout() {
        authDataSource.logout()
    }

    override suspend fun isRememberMeEnabled(): Boolean =
        sessionPreferences.isRememberMeEnabled()

    override suspend fun setRememberMe(enabled: Boolean) {
        sessionPreferences.setRememberMe(enabled)
    }

    override fun getCurrentUserId(): String? = authDataSource.getCurrentUserId()

    override suspend fun getCurrentUserProfile(): Result<User> = runCatching {
        val uid = authDataSource.getCurrentUserId()
            ?: throw NoSuchElementException("No authenticated user")
        authDataSource.getUserProfile(uid).toDomain(uid)
    }.recoverCatching { throwable ->
        throw AppException(throwable.toAppError())
    }
}