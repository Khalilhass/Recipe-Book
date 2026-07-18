package com.example.recipe_book.domain.usecase.recipe

import com.example.recipe_book.domain.model.AppError
import com.example.recipe_book.domain.model.AppException
import com.example.recipe_book.domain.repository.AuthRepository
import com.example.recipe_book.domain.repository.RecipeRepository
import javax.inject.Inject

class DeleteRecipeUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(recipeId: String, authorId: String): Result<Unit> {
        if (authorId != authRepository.getCurrentUserId()) {
            return Result.failure(AppException(AppError.AuthError("You can only delete your own recipes")))
        }
        return recipeRepository.deleteRecipe(recipeId)
    }
}
