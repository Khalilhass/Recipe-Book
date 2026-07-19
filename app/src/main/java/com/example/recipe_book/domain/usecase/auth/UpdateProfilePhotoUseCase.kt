package com.example.recipe_book.domain.usecase.auth

import com.example.recipe_book.domain.repository.AuthRepository
import javax.inject.Inject

class UpdateProfilePhotoUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(uid: String, photoUrl: String): Result<Unit> =
        authRepository.updateProfilePhoto(uid, photoUrl)
}