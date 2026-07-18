package com.example.recipe_book.presentation.addeditrecipe

import android.net.Uri
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
import com.example.recipe_book.databinding.FragmentAddEditRecipeBinding
import com.example.recipe_book.domain.model.Recipe
import com.example.recipe_book.presentation.common.UiState
import com.example.recipe_book.util.*
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddEditRecipeFragment : Fragment() {

    private var _binding: FragmentAddEditRecipeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddEditRecipeViewModel by viewModels()

    private val pickPhotoLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                viewModel.onImagePicked(uri)
                showImagePreview(uri)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEditRecipeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.title = getString(
            if (viewModel.isEditMode) R.string.edit_recipe_title else R.string.add_recipe_title
        )
        binding.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }

        binding.frameImage.setOnClickListener {
            pickPhotoLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }

        binding.buttonSave.setOnClickListener { attemptSave() }

        observeLoadState()
        observeSubmitState()
        observePickedImage()
    }

    private fun observeLoadState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loadState.collect { state ->
                    when (state) {
                        is UiState.Success -> prefillForm(state.data)
                        is UiState.Error -> {
                            Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                            findNavController().popBackStack()
                        }
                        else -> Unit
                    }
                }
            }
        }
    }

    private fun prefillForm(recipe: Recipe) {
        binding.editTitle.setText(recipe.title)
        binding.editIngredients.setText(recipe.ingredients.joinToString(", "))
        binding.editSteps.setText(recipe.steps.joinToString(", "))
        binding.editCategory.setText(recipe.category)
        binding.editVideoUrl.setText(recipe.videoUrl.orEmpty())

        val displayUrl = recipe.imageUrl.ifBlank { Constants.DEFAULT_RECIPE_IMAGE }
        if (displayUrl.isNotBlank() && viewModel.pickedImageUri.value == null) {
            binding.textTapToAddImage.gone()
            Glide.with(this).load(displayUrl).centerCrop().into(binding.imageRecipePreview)
        }
    }

    private fun observePickedImage() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.pickedImageUri.collect { uri ->
                    if (uri != null) showImagePreview(uri)
                }
            }
        }
    }

    private fun showImagePreview(uri: Uri) {
        binding.textTapToAddImage.gone()
        Glide.with(this).load(uri).centerCrop().into(binding.imageRecipePreview)
    }

    private fun attemptSave() {
        val title = binding.editTitle.textOrEmpty()
        val ingredientsText = binding.editIngredients.textOrEmpty()
        val stepsText = binding.editSteps.textOrEmpty()
        val category = binding.editCategory.textOrEmpty()
        val videoUrl = binding.editVideoUrl.textOrEmpty()

        binding.layoutTitle.setErrorOrNull(
            if (!Validators.isValidRecipeTitle(title)) "Title must be 3–100 characters" else null
        )
        binding.layoutIngredients.setErrorOrNull(
            if (Validators.splitCommaSeparated(ingredientsText).isEmpty()) "Add at least one ingredient" else null
        )
        binding.layoutSteps.setErrorOrNull(
            if (Validators.splitCommaSeparated(stepsText).isEmpty()) "Add at least one step" else null
        )
        binding.layoutCategory.setErrorOrNull(
            if (category.isBlank()) "Category is required" else null
        )
        binding.layoutVideoUrl.setErrorOrNull(
            if (!Validators.isValidYoutubeUrl(videoUrl)) "Enter a valid YouTube URL, or leave it empty" else null
        )

        val isValid = Validators.isValidRecipeTitle(title) &&
            Validators.splitCommaSeparated(ingredientsText).isNotEmpty() &&
            Validators.splitCommaSeparated(stepsText).isNotEmpty() &&
            category.isNotBlank() &&
            Validators.isValidYoutubeUrl(videoUrl)
        if (!isValid) return

        toast("Saving recipe...")
        viewModel.submit(title, ingredientsText, stepsText, category, videoUrl)
    }

    private fun observeSubmitState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.submitState.collect { state -> renderSubmitState(state) }
            }
        }
    }

    private fun renderSubmitState(state: UiState<Unit>?) {
        when (state) {
            null -> setSaving(false)
            is UiState.Loading -> setSaving(true)
            is UiState.Success -> {
                setSaving(false)
                viewModel.consumeSubmitState()
                toast(getString(R.string.recipe_saved))
                findNavController().popBackStack()
            }
            is UiState.Error -> {
                setSaving(false)
                viewModel.consumeSubmitState()
                Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
            }
            is UiState.Empty -> setSaving(false)
        }
    }

    private fun setSaving(isSaving: Boolean) {
        binding.buttonSave.isEnabled = !isSaving
        if (isSaving) binding.progressSave.visible() else binding.progressSave.gone()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
