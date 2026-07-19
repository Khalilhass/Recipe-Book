package com.example.recipe_book.domain.usecase.recipe

import com.example.recipe_book.domain.model.Recipe
import com.example.recipe_book.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserRecipesUseCase @Inject constructor(
    private val repository: RecipeRepository
) {
    operator fun invoke(userId: String): Flow<Result<List<Recipe>>> {
        return repository.getUserRecipes(userId)
    }
}
