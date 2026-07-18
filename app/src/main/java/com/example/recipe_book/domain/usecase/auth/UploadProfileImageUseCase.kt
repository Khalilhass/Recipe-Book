package com.example.recipe_book.domain.usecase.auth

import android.net.Uri
import com.example.recipe_book.domain.repository.ImageRepository
import javax.inject.Inject

class UploadProfileImageUseCase @Inject constructor(
    private val imageRepository: ImageRepository
) {
    suspend operator fun invoke(uri: Uri, uid: String): Result<String> =
        imageRepository.uploadProfileImage(uri, uid)
}