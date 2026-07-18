package com.example.recipe_book.domain.usecase.recipe

import com.example.recipe_book.domain.model.AppError
import com.example.recipe_book.domain.model.AppException
import com.example.recipe_book.domain.model.Recipe
import com.example.recipe_book.domain.repository.RecipeRepository
import com.example.recipe_book.util.Validators
import javax.inject.Inject

class AddRecipeUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository
) {
    suspend operator fun invoke(recipe: Recipe): Result<Unit> {
        validate(recipe)?.let { return Result.failure(it) }
        return recipeRepository.addRecipe(recipe)
    }

    companion object {
        /** Shared by AddRecipeUseCase and UpdateRecipeUseCase — same field rules either way. */
        fun validate(recipe: Recipe): AppException? {
            if (!Validators.isValidRecipeTitle(recipe.title)) {
                return AppException(AppError.ValidationError("Title must be 3–100 characters"))
            }
            if (recipe.ingredients.isEmpty()) {
                return AppException(AppError.ValidationError("Add at least one ingredient"))
            }
            if (recipe.steps.isEmpty()) {
                return AppException(AppError.ValidationError("Add at least one step"))
            }
            if (recipe.category.isBlank()) {
                return AppException(AppError.ValidationError("Category is required"))
            }
            if (!Validators.isValidYoutubeUrl(recipe.videoUrl.orEmpty())) {
                return AppException(AppError.ValidationError("Enter a valid YouTube URL, or leave it empty"))
            }
            return null
        }
    }
}
