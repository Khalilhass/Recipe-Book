package com.example.recipe_book.domain.usecase.recipe

import com.example.recipe_book.domain.repository.RecipeRepository
import javax.inject.Inject

class GenerateRecipeIdUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository
) {
    operator fun invoke(): String = recipeRepository.generateRecipeId()
}
