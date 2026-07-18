package com.example.recipe_book.presentation.addeditrecipe

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recipe_book.domain.model.AppException
import com.example.recipe_book.domain.model.Recipe
import com.example.recipe_book.domain.usecase.auth.GetCurrentUserUseCase
import com.example.recipe_book.domain.usecase.recipe.AddRecipeUseCase
import com.example.recipe_book.domain.usecase.recipe.GenerateRecipeIdUseCase
import com.example.recipe_book.domain.usecase.recipe.GetRecipeByIdUseCase
import com.example.recipe_book.domain.usecase.recipe.UpdateRecipeUseCase
import com.example.recipe_book.domain.usecase.recipe.UploadRecipeImageUseCase
import com.example.recipe_book.presentation.common.UiState
import com.example.recipe_book.util.Constants
import com.example.recipe_book.util.Validators
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditRecipeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getRecipeByIdUseCase: GetRecipeByIdUseCase,
    private val generateRecipeIdUseCase: GenerateRecipeIdUseCase,
    private val uploadRecipeImageUseCase: UploadRecipeImageUseCase,
    private val addRecipeUseCase: AddRecipeUseCase,
    private val updateRecipeUseCase: UpdateRecipeUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    /** Null recipeId = Add mode; non-null = Edit mode (Safe Args nav argument, matches nav_graph.xml). */
    private val recipeId: String? = savedStateHandle["recipeId"]
    val isEditMode: Boolean = recipeId != null

    private var loadedRecipe: Recipe? = null

    private val _pickedImageUri = MutableStateFlow<Uri?>(null)
    val pickedImageUri: StateFlow<Uri?> = _pickedImageUri.asStateFlow()

    fun onImagePicked(uri: Uri) {
        _pickedImageUri.value = uri
    }

    private val _loadState = MutableStateFlow<UiState<Recipe>?>(if (isEditMode) UiState.Loading else null)
    val loadState: StateFlow<UiState<Recipe>?> = _loadState.asStateFlow()

    private val _submitState = MutableStateFlow<UiState<Unit>?>(null)
    val submitState: StateFlow<UiState<Unit>?> = _submitState.asStateFlow()

    init {
        if (isEditMode) loadExistingRecipe()
    }

    private fun loadExistingRecipe() {
        viewModelScope.launch {
            val result = getRecipeByIdUseCase(recipeId!!)
            _loadState.value = result.fold(
                onSuccess = { recipe ->
                    loadedRecipe = recipe
                    UiState.Success(recipe)
                },
                onFailure = { throwable ->
                    UiState.Error((throwable as? AppException)?.error?.message ?: "Couldn't load this recipe")
                }
            )
        }
    }

    fun submit(
        title: String,
        ingredientsText: String,
        stepsText: String,
        category: String,
        videoUrl: String
    ) {
        viewModelScope.launch {
            _submitState.value = UiState.Loading

            val currentId = recipeId ?: generateRecipeIdUseCase()

            var imageUrl = if (isEditMode) {
                loadedRecipe?.imageUrl?.ifBlank { Constants.DEFAULT_RECIPE_IMAGE } ?: Constants.DEFAULT_RECIPE_IMAGE
            } else {
                Constants.DEFAULT_RECIPE_IMAGE
            }
            val pickedUri = _pickedImageUri.value
            if (pickedUri != null) {
                val uploadResult = uploadRecipeImageUseCase(pickedUri, currentId)
                val newUrl = uploadResult.getOrElse { throwable ->
                    _submitState.value = UiState.Error(
                        (throwable as? AppException)?.error?.message ?: "Image upload failed"
                    )
                    return@launch
                }
                imageUrl = newUrl
            }

            val authorId: String
            val authorName: String
            if (loadedRecipe != null) {
                authorId = loadedRecipe!!.authorId
                authorName = loadedRecipe!!.authorName
            } else {
                val userResult = getCurrentUserUseCase()
                val user = userResult.getOrElse { throwable ->
                    _submitState.value = UiState.Error(
                        (throwable as? AppException)?.error?.message ?: "Couldn't verify your account"
                    )
                    return@launch
                }
                authorId = user.id
                authorName = user.name
            }

            val recipe = Recipe(
                id = currentId,
                title = title.trim(),
                ingredients = Validators.splitCommaSeparated(ingredientsText),
                steps = Validators.splitCommaSeparated(stepsText),
                category = Validators.normalizeCategory(category),
                imageUrl = imageUrl,
                videoUrl = videoUrl.trim().ifBlank { null },
                authorId = authorId,
                authorName = authorName,
                createdAt = loadedRecipe?.createdAt ?: System.currentTimeMillis()
            )

            val result = if (isEditMode) updateRecipeUseCase(recipe) else addRecipeUseCase(recipe)
            _submitState.value = result.fold(
                onSuccess = { UiState.Success(Unit) },
                onFailure = { throwable ->
                    UiState.Error((throwable as? AppException)?.error?.message ?: "Couldn't save the recipe")
                }
            )
        }
    }

    fun consumeSubmitState() {
        _submitState.value = null
    }
}
