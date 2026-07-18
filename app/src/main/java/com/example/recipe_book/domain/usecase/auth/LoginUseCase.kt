package com.example.recipe_book.domain.usecase.auth


import com.example.recipe_book.domain.model.AppError
import com.example.recipe_book.domain.model.AppException
import com.example.recipe_book.domain.model.User
import com.example.recipe_book.domain.repository.AuthRepository
import com.example.recipe_book.util.Validators
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        rememberMe: Boolean
    ): Result<User> {
        if (!Validators.isValidEmail(email)) {
            return Result.failure(AppException(AppError.ValidationError("Enter a valid email address")))
        }
        if (password.isBlank()) {
            return Result.failure(AppException(AppError.ValidationError("Enter your password")))
        }

        val result = authRepository.login(email.trim(), password)
        // Persist the "remember me" choice regardless of outcome timing nuance:
        // only persist on success, so a failed login attempt doesn't silently flip a stale session flag.
        if (result.isSuccess) {
            authRepository.setRememberMe(rememberMe)
        }
        return result
    }
}
