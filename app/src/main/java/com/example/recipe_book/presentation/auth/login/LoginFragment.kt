package com.example.recipe_book.presentation.auth.login
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.recipe_book.databinding.FragmentLoginBinding
import com.example.recipe_book.presentation.common.UiState
import com.example.recipe_book.util.Validators
import com.example.recipe_book.util.setErrorOrNull
import com.example.recipe_book.util.textOrEmpty
import com.example.recipe_book.util.toast
import com.example.recipe_book.util.visible
import com.example.recipe_book.util.gone
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonLogin.setOnClickListener { attemptLogin() }
        binding.textGoToRegister.setOnClickListener {
            findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToRegisterFragment())
        }

        observeUiState()
    }

    private fun attemptLogin() {
        val email = binding.editEmail.textOrEmpty()
        val password = binding.editPassword.textOrEmpty()

        binding.layoutEmail.setErrorOrNull(
            if (email.isBlank()) "Email is required" 
            else if (!Validators.isValidEmail(email)) "Enter a valid email address" 
            else null
        )
        binding.layoutPassword.setErrorOrNull(
            if (password.isBlank()) "Password is required" else null
        )
        if (!Validators.isValidEmail(email) || password.isBlank()) return

        toast("Logging in...")
        viewModel.login(email, password, binding.checkboxRememberMe.isChecked)
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
                toast("Logged in successfully")
                findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToHomeFragment())
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
        binding.buttonLogin.isEnabled = !isLoading
        binding.editEmail.isEnabled = !isLoading
        binding.editPassword.isEnabled = !isLoading
        binding.checkboxRememberMe.isEnabled = !isLoading
        if (isLoading) binding.progressLogin.visible() else binding.progressLogin.gone()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}