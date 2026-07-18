package com.example.recipe_book.domain.usecase.auth

import com.example.recipe_book.domain.repository.AuthRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke() {
        authRepository.logout()
        authRepository.setRememberMe(false)
    }
}