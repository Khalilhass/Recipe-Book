package com.example.recipe_book.data.remote

import com.example.recipe_book.domain.model.AppError
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException


fun Throwable.toAppError(): AppError = when (this) {
    is FirebaseNetworkException,
    is UnknownHostException,
    is ConnectException,
    is SocketTimeoutException ->
        AppError.NetworkError()

    is IOException -> {
        val messageText = message?.trim().orEmpty()
        if (messageText.contains("network", ignoreCase = true) ||
            messageText.contains("timeout", ignoreCase = true) ||
            messageText.contains("timed out", ignoreCase = true) ||
            messageText.contains("connection", ignoreCase = true)
        ) {
            AppError.NetworkError()
        } else {
            AppError.UnknownError(messageText.ifBlank { "Image upload failed" })
        }
    }

    is FirebaseAuthUserCollisionException ->
        AppError.AuthError("An account with this email already exists")

    is FirebaseAuthWeakPasswordException ->
        AppError.ValidationError("Password is too weak — use at least 6 characters")

    is FirebaseAuthInvalidCredentialsException, is FirebaseAuthInvalidUserException ->
        AppError.AuthError("Invalid email or password")

    is NoSuchElementException ->
        AppError.NotFoundError(message ?: "Not found")

    is IllegalArgumentException ->
        AppError.ValidationError(message ?: "That image couldn't be used — try a different one")

    else ->
        AppError.UnknownError(message ?: "Something went wrong")
}
