package com.example.recipe_book.domain.usecase.recipe

import com.example.recipe_book.domain.model.Recipe
import com.example.recipe_book.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class GetRecipesByCategoryUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository
) {
    operator fun invoke(category: String): Flow<Result<List<Recipe>>> {
        return if (category.equals("All", ignoreCase = true)) {
            recipeRepository.getAllRecipes()
        } else {
            recipeRepository.getRecipesByCategory(category)
        }
    }
}