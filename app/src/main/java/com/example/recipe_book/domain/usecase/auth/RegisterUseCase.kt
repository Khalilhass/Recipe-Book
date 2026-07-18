package com.example.recipe_book.domain.usecase.auth

import com.example.recipe_book.domain.model.AppError
import com.example.recipe_book.domain.model.AppException
import com.example.recipe_book.domain.repository.AuthRepository
import com.example.recipe_book.util.Validators
import javax.inject.Inject


class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        name: String,
        email: String,
        password: String,
        photoUrl: String,
        country: String
    ): Result<String> {
        if (!Validators.isValidName(name)) {
            return Result.failure(AppException(AppError.ValidationError("Name must be 2–50 characters")))
        }
        if (!Validators.isValidEmail(email)) {
            return Result.failure(AppException(AppError.ValidationError("Enter a valid email address")))
        }
        if (!Validators.isValidPassword(password)) {
            return Result.failure(AppException(AppError.ValidationError("Password must be at least 6 characters")))
        }
        if (country.isBlank()) {
            return Result.failure(AppException(AppError.ValidationError("Please select a country")))
        }
        return authRepository.register(name.trim(), email.trim(), password, photoUrl, country)
    }
}