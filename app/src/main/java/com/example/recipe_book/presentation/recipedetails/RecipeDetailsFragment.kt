package com.example.recipe_book.presentation.recipedetails

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.recipe_book.R
import com.example.recipe_book.databinding.FragmentRecipeDetailsBinding
import com.example.recipe_book.domain.model.Recipe
import com.example.recipe_book.presentation.common.UiState
import com.example.recipe_book.util.Validators
import com.example.recipe_book.util.gone
import com.example.recipe_book.util.toast
import com.example.recipe_book.util.visible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RecipeDetailsFragment : Fragment() {

    private var _binding: FragmentRecipeDetailsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RecipeDetailsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecipeDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        observeUiState()
        observeDeleteState()
    }

    private fun setupToolbar() {
        binding.toolbar.inflateMenu(R.menu.recipe_details_menu)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_edit -> {
                    val recipe = (viewModel.uiState.value as? UiState.Success)?.data
                    if (recipe != null) {
                        findNavController().navigate(
                            RecipeDetailsFragmentDirections.actionRecipeDetailsFragmentToAddEditRecipeFragment(recipe.id)
                        )
                    }
                    true
                }
                R.id.action_delete -> {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.delete_recipe_confirm_title)
                        .setMessage(R.string.delete_recipe_confirm_message)
                        .setPositiveButton(R.string.delete) { _, _ -> 
                            toast("Deleting recipe...")
                            viewModel.deleteRecipe() 
                        }
                        .setNegativeButton(R.string.cancel, null)
                        .show()
                    true
                }
                else -> false
            }
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.progressLoading.gone()
                    when (state) {
                        is UiState.Loading -> binding.progressLoading.visible()
                        is UiState.Success -> renderRecipe(state.data)
                        is UiState.Error -> {
                            toast(state.message)
                            findNavController().popBackStack()
                        }
                        else -> Unit
                    }
                }
            }
        }
    }

    private fun renderRecipe(recipe: Recipe) {
        binding.textTitle.text = recipe.title
        binding.chipCategory.text = recipe.category
        binding.textAuthor.text = getString(R.string.author_format, recipe.authorName)
        binding.textIngredients.text = recipe.ingredients.joinToString("\n") { "• $it" }
        binding.textSteps.text = recipe.steps.mapIndexed { index, step -> "${index + 1}. $step" }.joinToString("\n")

        Glide.with(this)
            .load(recipe.imageUrl.ifBlank { com.example.recipe_book.util.Constants.DEFAULT_RECIPE_IMAGE })
            .placeholder(R.drawable.auth_logo)
            .error(R.drawable.auth_logo)
            .into(binding.imageRecipe)

        if (recipe.videoUrl.isNullOrBlank() || !Validators.isValidYoutubeUrl(recipe.videoUrl)) {
            binding.buttonWatchVideo.gone()
        } else {
            binding.buttonWatchVideo.visible()
            binding.buttonWatchVideo.setOnClickListener {
                openYoutube(recipe.videoUrl)
            }
        }

        // Only show edit/delete if current user is the author
        val isOwner = viewModel.currentUserId == recipe.authorId
        binding.toolbar.menu.findItem(R.id.action_edit)?.isVisible = isOwner
        binding.toolbar.menu.findItem(R.id.action_delete)?.isVisible = isOwner
    }

    private fun openYoutube(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            startActivity(intent)
        } catch (_: Exception) {
            toast(getString(R.string.unable_to_open_video))
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
                            findNavController().popBackStack()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
