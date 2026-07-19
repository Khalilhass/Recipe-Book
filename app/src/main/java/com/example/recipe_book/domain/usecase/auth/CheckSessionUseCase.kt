package com.example.recipe_book.domain.usecase.auth

import com.example.recipe_book.domain.repository.AuthRepository
import javax.inject.Inject

class CheckSessionUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Boolean {
        val hasFirebaseSession = authRepository.getCurrentUserId() != null
        val rememberMe = authRepository.isRememberMeEnabled()

        return if (hasFirebaseSession && rememberMe) {
            true
        } else {
            // Either there's no session, or the user didn't ask to be
            // remembered — actively clear any lingering Firebase session
            // so the app doesn't silently auto-login against the user's wish.
            if (hasFirebaseSession) authRepository.logout()
            false
        }
    }
}