package com.example.recipe_book.domain.repository


import com.example.recipe_book.domain.model.Recipe
import kotlinx.coroutines.flow.Flow

interface RecipeRepository {

    /** Real-time stream of every recipe, newest first. */
    fun getAllRecipes(): Flow<Result<List<Recipe>>>

    /** Real-time stream of recipes in a single category, newest first. */
    fun getRecipesByCategory(category: String): Flow<Result<List<Recipe>>>

    /** One-shot fetch, used by the Edit screen to pre-fill the form. */
    suspend fun getRecipeById(recipeId: String): Result<Recipe>

    /**
     * Generates a Firestore document ID locally (no network call) so the
     * same ID can be used as the Cloudinary public_id BEFORE the document
     * is written — this is what lets the image upload complete first and
     * the Firestore write happen second, per blueprint Section 10.2's
     * ordering requirement.
     */
    fun generateRecipeId(): String

    suspend fun addRecipe(recipe: Recipe): Result<Unit>

    suspend fun updateRecipe(recipe: Recipe): Result<Unit>

    suspend fun deleteRecipe(recipeId: String): Result<Unit>

    /** Real-time stream of recipes created by a specific user. */
    fun getUserRecipes(userId: String): Flow<Result<List<Recipe>>>
}