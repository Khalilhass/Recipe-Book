package com.example.recipe_book.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recipe_book.domain.model.AppException
import com.example.recipe_book.domain.model.Recipe
import com.example.recipe_book.domain.model.User
import com.example.recipe_book.domain.repository.AuthRepository
import com.example.recipe_book.domain.usecase.auth.GetCurrentUserUseCase
import com.example.recipe_book.domain.usecase.auth.LogoutUseCase
import com.example.recipe_book.domain.usecase.recipe.DeleteRecipeUseCase
import com.example.recipe_book.domain.usecase.recipe.GetUserRecipesUseCase
import com.example.recipe_book.domain.usecase.auth.UpdateProfilePhotoUseCase
import com.example.recipe_book.domain.usecase.auth.UploadProfileImageUseCase
import com.example.recipe_book.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getUserRecipesUseCase: GetUserRecipesUseCase,
    private val deleteRecipeUseCase: DeleteRecipeUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val uploadProfileImageUseCase: UploadProfileImageUseCase,
    private val updateProfilePhotoUseCase: UpdateProfilePhotoUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _userState = MutableStateFlow<UiState<User>>(UiState.Loading)
    val userState: StateFlow<UiState<User>> = _userState.asStateFlow()

    private val _updatePhotoState = MutableStateFlow<UiState<String>?>(null)
    val updatePhotoState: StateFlow<UiState<String>?> = _updatePhotoState.asStateFlow()

    private val _deleteState = MutableStateFlow<UiState<Unit>?>(null)
    val deleteState: StateFlow<UiState<Unit>?> = _deleteState.asStateFlow()

    private val _logoutState = MutableStateFlow<UiState<Unit>?>(null)
    val logoutState: StateFlow<UiState<Unit>?> = _logoutState.asStateFlow()

    val currentUserId: () -> String? = { authRepository.getCurrentUserId() }

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            _userState.value = UiState.Loading
            getCurrentUserUseCase().fold(
                onSuccess = { user -> _userState.value = UiState.Success(user) },
                onFailure = { throwable ->
                    val message = (throwable as? AppException)?.error?.message
                        ?: "Couldn't load profile. Please try again."
                    _userState.value = UiState.Error(message)
                }
            )
        }
    }

    fun updateProfilePhoto(uri: android.net.Uri) {
        val user = (userState.value as? UiState.Success)?.data ?: return
        viewModelScope.launch {
            _updatePhotoState.value = UiState.Loading
            val uploadResult = uploadProfileImageUseCase(uri, user.id)
            
            uploadResult.fold(
                onSuccess = { newUrl ->
                    val updateResult = updateProfilePhotoUseCase(user.id, newUrl)
                    updateResult.fold(
                        onSuccess = {
                            _updatePhotoState.value = UiState.Success(newUrl)
                            // Update local state immediately to trigger UI refresh
                            val updatedUser = user.copy(photoUrl = newUrl)
                            _userState.value = UiState.Success(updatedUser)
                        },
                        onFailure = { throwable ->
                            _updatePhotoState.value = UiState.Error(
                                (throwable as? AppException)?.error?.message ?: "Failed to update profile"
                            )
                        }
                    )
                },
                onFailure = { throwable ->
                    _updatePhotoState.value = UiState.Error(
                        (throwable as? AppException)?.error?.message ?: "Image upload failed"
                    )
                }
            )
        }
    }

    fun consumeUpdatePhotoState() {
        _updatePhotoState.value = null
    }

    val recipesState: StateFlow<UiState<List<Recipe>>> = _userState
        .flatMapLatest { state ->
            if (state is UiState.Success) {
                getUserRecipesUseCase(state.data.id)
            } else {
                kotlinx.coroutines.flow.flowOf(Result.success(emptyList()))
            }
        }
        .map { result ->
            result.fold(
                onSuccess = { recipes ->
                    if (recipes.isEmpty()) UiState.Empty else UiState.Success(recipes)
                },
                onFailure = { throwable ->
                    val message = (throwable as? AppException)?.error?.message
                        ?: "Couldn't load recipes. Please try again."
                    UiState.Error(message)
                }
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    fun deleteRecipe(recipe: Recipe) {
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

    fun logout() {
        viewModelScope.launch {
            _logoutState.value = UiState.Loading
            runCatching { logoutUseCase() }
                .onSuccess {
                    _logoutState.value = UiState.Success(Unit)
                }.onFailure { throwable ->
                    val message = (throwable as? AppException)?.error?.message
                        ?: "Couldn't log out. Please try again."
                    _logoutState.value = UiState.Error(message)
                }
        }
    }

    fun consumeLogoutState() {
        _logoutState.value = null
    }
}
