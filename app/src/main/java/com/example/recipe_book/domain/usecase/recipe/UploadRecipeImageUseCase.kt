package com.example.recipe_book.domain.usecase.recipe

import android.net.Uri
import com.example.recipe_book.domain.repository.ImageRepository
import javax.inject.Inject

class UploadRecipeImageUseCase @Inject constructor(
    private val imageRepository: ImageRepository
) {
    suspend operator fun invoke(uri: Uri, recipeId: String): Result<String> =
        imageRepository.uploadRecipeImage(uri, recipeId)
}
