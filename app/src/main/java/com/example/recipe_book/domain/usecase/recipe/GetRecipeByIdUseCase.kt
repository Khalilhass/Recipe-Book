package com.example.recipe_book.domain.usecase.recipe

import com.example.recipe_book.domain.model.Recipe
import com.example.recipe_book.domain.repository.RecipeRepository
import javax.inject.Inject

class GetRecipeByIdUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository
) {
    suspend operator fun invoke(recipeId: String): Result<Recipe> =
        recipeRepository.getRecipeById(recipeId)
}
