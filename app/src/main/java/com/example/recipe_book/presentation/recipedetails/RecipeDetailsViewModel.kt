package com.example.recipe_book.presentation.recipedetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recipe_book.domain.model.AppException
import com.example.recipe_book.domain.model.Recipe
import com.example.recipe_book.domain.repository.AuthRepository
import com.example.recipe_book.domain.usecase.recipe.DeleteRecipeUseCase
import com.example.recipe_book.domain.usecase.recipe.GetRecipeByIdUseCase
import com.example.recipe_book.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipeDetailsViewModel @Inject constructor(
    private val getRecipeByIdUseCase: GetRecipeByIdUseCase,
    private val deleteRecipeUseCase: DeleteRecipeUseCase,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val recipeId: String = checkNotNull(savedStateHandle["recipeId"])

    private val _uiState = MutableStateFlow<UiState<Recipe>>(UiState.Loading)
    val uiState: StateFlow<UiState<Recipe>> = _uiState.asStateFlow()

    private val _deleteState = MutableStateFlow<UiState<Unit>?>(null)
    val deleteState: StateFlow<UiState<Unit>?> = _deleteState.asStateFlow()

    val currentUserId: String? = authRepository.getCurrentUserId()

    init {
        loadRecipe()
    }

    fun loadRecipe() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            getRecipeByIdUseCase(recipeId).fold(
                onSuccess = { recipe -> _uiState.value = UiState.Success(recipe) },
                onFailure = { throwable ->
                    val message = (throwable as? AppException)?.error?.message
                        ?: "Couldn't load recipe details. Please try again."
                    _uiState.value = UiState.Error(message)
                }
            )
        }
    }

    fun deleteRecipe() {
        val recipe = (uiState.value as? UiState.Success)?.data ?: return
        viewModelScope.launch {
            _deleteState.value = UiState.Loading
            val result = deleteRecipeUseCase(recipe.id, recipe.authorId)
            _deleteState.value = result.fold(
                onSuccess = { UiState.Success(Unit) },
                onFailure = { throwable ->
                    val message = (throwable as? AppException)?.error?.message
                        ?: "Couldn't delete this recipe. Please try again."
                    UiState.Error(message)
                }
            )
        }
    }

    fun consumeDeleteState() {
        _deleteState.value = null
    }
}
