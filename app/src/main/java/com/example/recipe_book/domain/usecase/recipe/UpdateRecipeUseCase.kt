package com.example.recipe_book.domain.usecase.recipe


import com.example.recipe_book.domain.model.AppError
import com.example.recipe_book.domain.model.AppException
import com.example.recipe_book.domain.model.Recipe
import com.example.recipe_book.domain.repository.AuthRepository
import com.example.recipe_book.domain.repository.RecipeRepository
import javax.inject.Inject

class UpdateRecipeUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(recipe: Recipe): Result<Unit> {
        AddRecipeUseCase.validate(recipe)?.let { return Result.failure(it) }

        // Client-side check only for a fast, friendly error — the real
        // enforcement is the Firestore Security Rule (blueprint Section 4.6)
        // requiring resource.data.authorId == request.auth.uid, which a
        // malicious client can't bypass even by skipping this check.
        if (recipe.authorId != authRepository.getCurrentUserId()) {
            return Result.failure(AppException(AppError.AuthError("You can only edit your own recipes")))
        }

        return recipeRepository.updateRecipe(recipe)
    }
}
