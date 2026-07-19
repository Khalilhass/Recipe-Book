package com.example.recipe_book.presentation.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recipe_book.domain.model.AppException
import com.example.recipe_book.domain.model.User
import com.example.recipe_book.domain.usecase.auth.LoginUseCase
import com.example.recipe_book.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<User>?>(null)
    val uiState: StateFlow<UiState<User>?> = _uiState.asStateFlow()

    fun login(email: String, password: String, rememberMe: Boolean) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = loginUseCase(email, password, rememberMe)
            _uiState.value = result.fold(
                onSuccess = { user -> UiState.Success(user) },
                onFailure = { throwable ->
                    val message = (throwable as? AppException)?.error?.message
                        ?: "Something went wrong. Please try again."
                    UiState.Error(message)
                }
            )
        }
    }

    /** Called after the Fragment has consumed a terminal state, so rotation
     *  doesn't replay a stale Success/Error and re-navigate or re-show a Snackbar. */
    fun consumeState() {
        _uiState.value = null
    }
}