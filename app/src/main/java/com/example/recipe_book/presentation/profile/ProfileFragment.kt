package com.example.recipe_book.presentation.profile

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
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.signature.ObjectKey
import com.example.recipe_book.R
import com.example.recipe_book.databinding.FragmentProfileBinding
import com.example.recipe_book.domain.model.Recipe
import com.example.recipe_book.domain.model.User
import com.example.recipe_book.presentation.common.UiState
import com.example.recipe_book.presentation.home.RecipeAdapter
import com.example.recipe_book.util.gone
import com.example.recipe_book.util.toast
import com.example.recipe_book.util.visible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()
    private lateinit var recipeAdapter: RecipeAdapter

    private val pickPhotoLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                toast("Updating profile picture...")
                viewModel.updateProfilePhoto(uri)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()
        setupEmptyState()
        setupPhotoUpdate()
        setupRetry()

        observeUserState()
        observeRecipesState()
        observeDeleteState()
        observeLogoutState()
        observeUpdatePhotoState()
    }

    private fun setupPhotoUpdate() {
        binding.frameProfileImage.setOnClickListener {
            pickPhotoLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_logout -> {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.logout_confirm_title)
                        .setMessage(R.string.logout_confirm_message)
                        .setPositiveButton(R.string.logout) { _, _ -> 
                            toast("Logging out...")
                            viewModel.logout() 
                        }
                        .setNegativeButton(R.string.cancel, null)
                        .show()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupRecyclerView() {
        recipeAdapter = RecipeAdapter(
            currentUserId = viewModel.currentUserId,
            onItemClick = { recipe ->
                findNavController().navigate(
                    ProfileFragmentDirections.actionProfileFragmentToRecipeDetailsFragment(recipe.id)
                )
            },
            onEditClick = { recipe ->
                findNavController().navigate(
                    ProfileFragmentDirections.actionProfileFragmentToAddEditRecipeFragment(recipe.id)
                )
            },
            onDeleteClick = { recipe ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.delete_recipe_confirm_title)
                    .setMessage(R.string.delete_recipe_confirm_message)
                    .setPositiveButton(R.string.delete) { _, _ -> 
                        toast("Deleting recipe...")
                        viewModel.deleteRecipe(recipe) 
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
            }
        )
        binding.recyclerUserRecipes.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = recipeAdapter
        }
    }

    private fun setupEmptyState() {
        binding.buttonAddRecipe.setOnClickListener {
            findNavController().navigate(
                ProfileFragmentDirections.actionProfileFragmentToAddEditRecipeFragment(null)
            )
        }
    }

    private fun setupRetry() {
        binding.buttonRetry.setOnClickListener {
            viewModel.loadUserProfile()
        }
    }

    private fun observeUserState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.userState.collect { state ->
                    when (state) {
                        is UiState.Loading -> { /* Handled by recipesState mostly */ }
                        is UiState.Success -> renderUser(state.data)
                        is UiState.Error -> {
                            binding.layoutError.visible()
                            binding.textErrorMessage.text = state.message
                        }
                        else -> Unit
                    }
                }
            }
        }
    }

    private fun renderUser(user: User) {
        binding.textName.text = user.name
        binding.textEmail.text = user.email
        binding.textCountry.text = getString(R.string.country_format, user.country)

        val displayUrl = user.photoUrl.ifBlank { com.example.recipe_book.util.Constants.DEFAULT_USER_IMAGE }
        Glide.with(this)
            .load(displayUrl)
            .placeholder(R.drawable.auth_logo)
            .error(R.drawable.auth_logo)
            .circleCrop()
            .into(binding.imageProfile)
    }

    private fun observeRecipesState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.recipesState.collect { state ->
                    binding.progressLoading.gone()
                    binding.layoutEmpty.gone()
                    binding.layoutError.gone()
                    binding.recyclerUserRecipes.gone()

                    when (state) {
                        is UiState.Loading -> binding.progressLoading.visible()
                        is UiState.Success -> {
                            binding.recyclerUserRecipes.visible()
                            recipeAdapter.submitList(state.data)
                        }
                        is UiState.Empty -> binding.layoutEmpty.visible()
                        is UiState.Error -> {
                            binding.layoutError.visible()
                            binding.textErrorMessage.text = state.message
                        }
                    }
                }
            }
        }
    }

    private fun observeDeleteState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.deleteState.collect { state ->
                    when (state) {
                        is UiState.Success -> {
                            viewModel.consumeDeleteState()
                            toast(getString(R.string.recipe_deleted))
                        }
                        is UiState.Error -> {
                            viewModel.consumeDeleteState()
                            Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                        }
                        else -> Unit
                    }
                }
            }
        }
    }

    private fun observeLogoutState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.logoutState.collect { state ->
                    when (state) {
                        is UiState.Success -> {
                            viewModel.consumeLogoutState()
                            toast(getString(R.string.logout_success))
                            findNavController().navigate(
                                ProfileFragmentDirections.actionProfileFragmentToLoginFragment()
                            )
                        }
                        is UiState.Error -> {
                            viewModel.consumeLogoutState()
                            Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                        }
                        else -> Unit
                    }
                }
            }
        }
    }

    private fun observeUpdatePhotoState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.updatePhotoState.collect { state ->
                    when (state) {
                        is UiState.Success -> {
                            viewModel.consumeUpdatePhotoState()
                            toast("Profile picture updated successfully")
                        }
                        is UiState.Error -> {
                            viewModel.consumeUpdatePhotoState()
                            Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                        }
                        else -> Unit
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
