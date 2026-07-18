package com.example.recipe_book.presentation.auth.register

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recipe_book.domain.model.AppException
import com.example.recipe_book.domain.usecase.auth.RegisterUseCase
import com.example.recipe_book.domain.usecase.auth.UpdateProfilePhotoUseCase
import com.example.recipe_book.domain.usecase.auth.UploadProfileImageUseCase
import com.example.recipe_book.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase,
    private val uploadProfileImageUseCase: UploadProfileImageUseCase,
    private val updateProfilePhotoUseCase: UpdateProfilePhotoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<Unit>?>(null)
    val uiState: StateFlow<UiState<Unit>?> = _uiState.asStateFlow()

    private val _pickedImageUri = MutableStateFlow<Uri?>(null)
    val pickedImageUri: StateFlow<Uri?> = _pickedImageUri.asStateFlow()

    fun onImagePicked(uri: Uri) {
        _pickedImageUri.value = uri
    }

    /**
     * photoUrl is empty for now — profile photo upload is wired up in Phase 3
     * once the shared Cloudinary upload workflow (blueprint Section 10.2) is
     * built for recipe images and reused here. Registering with no photo is
     * an explicitly supported edge case (blueprint Section 1.3): the UI falls
     * back to a default placeholder avatar rather than blocking signup.
     */
    fun register(name: String, email: String, password: String, country: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            
            // 1. Register user
            val registerResult = registerUseCase(
                name = name,
                email = email,
                password = password,
                photoUrl = "", // initially empty
                country = country
            )

            registerResult.fold(
                onSuccess = { uid ->
                    // 2. If an image was picked, upload it
                    val uri = _pickedImageUri.value
                    if (uri != null) {
                        val uploadResult = uploadProfileImageUseCase(uri, uid)
                        uploadResult.onSuccess { photoUrl ->
                            updateProfilePhotoUseCase(uid, photoUrl)
                        }
                    }
                    _uiState.value = UiState.Success(Unit)
                },
                onFailure = { throwable ->
                    val message = (throwable as? AppException)?.error?.message
                        ?: "Something went wrong. Please try again."
                    _uiState.value = UiState.Error(message)
                }
            )
        }
    }

    fun consumeState() {
        _uiState.value = null
    }
}
