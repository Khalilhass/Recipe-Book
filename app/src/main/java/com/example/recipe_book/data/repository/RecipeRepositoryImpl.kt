package com.example.recipe_book.data.repository

import com.example.recipe_book.data.mapper.toDomain
import com.example.recipe_book.data.mapper.toDto
import com.example.recipe_book.data.remote.FirestoreRecipeDataSource
import com.example.recipe_book.data.remote.toAppError
import com.example.recipe_book.domain.model.AppException
import com.example.recipe_book.domain.model.Recipe
import com.example.recipe_book.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecipeRepositoryImpl @Inject constructor(
    private val recipeDataSource: FirestoreRecipeDataSource
) : RecipeRepository {

    override fun getAllRecipes(): Flow<Result<List<Recipe>>> =
        recipeDataSource.observeAllRecipes()
            .map { list -> Result.success(list.map { (id, dto) -> dto.toDomain(id) }) }
            .catch { throwable -> emit(Result.failure(AppException(throwable.toAppError()))) }

    override fun getRecipesByCategory(category: String): Flow<Result<List<Recipe>>> =
        recipeDataSource.observeRecipesByCategory(category)
            .map { list -> Result.success(list.map { (id, dto) -> dto.toDomain(id) }) }
            .catch { throwable -> emit(Result.failure(AppException(throwable.toAppError()))) }

    override suspend fun getRecipeById(recipeId: String): Result<Recipe> = runCatching {
        val (id, dto) = recipeDataSource.getRecipeById(recipeId)
            ?: throw NoSuchElementException("Recipe not found")
        dto.toDomain(id)
    }.recoverCatching { throwable -> throw AppException(throwable.toAppError()) }

    override fun generateRecipeId(): String = recipeDataSource.generateRecipeId()

    override suspend fun addRecipe(recipe: Recipe): Result<Unit> = runCatching {
        recipeDataSource.addRecipe(recipe.id, recipe.toDto())
    }.recoverCatching { throwable -> throw AppException(throwable.toAppError()) }

    override suspend fun updateRecipe(recipe: Recipe): Result<Unit> = runCatching {
        recipeDataSource.updateRecipe(recipe.id, recipe.toDto())
    }.recoverCatching { throwable -> throw AppException(throwable.toAppError()) }

    override suspend fun deleteRecipe(recipeId: String): Result<Unit> = runCatching {
        recipeDataSource.deleteRecipe(recipeId)
    }.recoverCatching { throwable -> throw AppException(throwable.toAppError()) }

    override fun getUserRecipes(userId: String): Flow<Result<List<Recipe>>> =
        recipeDataSource.observeRecipesByAuthor(userId)
            .map { list -> Result.success(list.map { (id, dto) -> dto.toDomain(id) }) }
            .catch { throwable -> emit(Result.failure(AppException(throwable.toAppError()))) }
}
