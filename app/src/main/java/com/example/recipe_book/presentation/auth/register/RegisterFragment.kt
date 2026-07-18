package com.example.recipe_book.presentation.auth.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.recipe_book.R
import com.example.recipe_book.databinding.FragmentRegisterBinding
import com.example.recipe_book.presentation.common.UiState
import com.example.recipe_book.util.Validators
import com.example.recipe_book.util.gone
import com.example.recipe_book.util.setErrorOrNull
import com.example.recipe_book.util.textOrEmpty
import com.example.recipe_book.util.toast
import com.example.recipe_book.util.visible
import com.google.android.material.snackbar.Snackbar
import com.example.recipe_book.util.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RegisterViewModel by viewModels()

    private val pickPhotoLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                viewModel.onImagePicked(uri)
                binding.imageProfile.setImageURI(uri)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Glide.with(this)
            .load(Constants.DEFAULT_USER_IMAGE)
            .fitCenter()
            .into(binding.imageProfile)

        binding.frameProfileImage.setOnClickListener {
            pickPhotoLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }

        binding.buttonRegister.setOnClickListener { attemptRegister() }
        binding.textGoToLogin.setOnClickListener { findNavController().popBackStack() }

        observeUiState()
    }

    private fun attemptRegister() {
        val name = binding.editName.textOrEmpty()
        val email = binding.editEmail.textOrEmpty()
        val password = binding.editPassword.textOrEmpty()
        val country = if (binding.spinnerCountry.selectedItemPosition == 0) "" else binding.spinnerCountry.selectedItem.toString()

        binding.layoutName.setErrorOrNull(
            if (!Validators.isValidName(name)) "Name must be 2–50 characters" else null
        )
        binding.layoutEmail.setErrorOrNull(
            if (email.isBlank()) "Email is required" 
            else if (!Validators.isValidEmail(email)) "Enter a valid email address" 
            else null
        )
        binding.layoutPassword.setErrorOrNull(
            if (!Validators.isValidPassword(password)) "Password must be at least 6 characters" else null
        )
        
        if (country.isBlank()) {
            toast("Please select a country")
            return
        }

        val isValid = Validators.isValidName(name) &&
            Validators.isValidEmail(email) &&
            Validators.isValidPassword(password) &&
            country.isNotBlank()
        if (!isValid) return

        toast("Creating account...")
        viewModel.register(name, email, password, country)
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state -> render(state) }
            }
        }
    }

    private fun render(state: UiState<*>?) {
        when (state) {
            null -> setLoading(false)
            is UiState.Loading -> setLoading(true)
            is UiState.Success<*> -> {
                setLoading(false)
                viewModel.consumeState()
                toast(getString(R.string.account_created))
                findNavController().popBackStack() // per spec: Register does NOT auto-login
            }
            is UiState.Error -> {
                setLoading(false)
                viewModel.consumeState()
                Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
            }
            is UiState.Empty -> setLoading(false)
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.buttonRegister.isEnabled = !isLoading
        binding.editName.isEnabled = !isLoading
        binding.editEmail.isEnabled = !isLoading
        binding.editPassword.isEnabled = !isLoading
        binding.spinnerCountry.isEnabled = !isLoading
        if (isLoading) binding.progressRegister.visible() else binding.progressRegister.gone()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
