package com.example.recipe_book.domain.model

sealed class AppError(val message: String) {
    data class NetworkError(val reason: String = "No internet connection") : AppError(reason)
    data class AuthError(val reason: String) : AppError(reason)
    data class NotFoundError(val reason: String = "Not found") : AppError(reason)
    data class ValidationError(val reason: String) : AppError(reason)
    data class UnknownError(val reason: String = "Something went wrong") : AppError(reason)
}

/**
 * Wraps an AppError as a Throwable so it can travel inside a Kotlin
 * to get back the typed AppError instead of pattern-matching on exception
 * class names.
 */
class AppException(val error: AppError) : Exception(error.message)