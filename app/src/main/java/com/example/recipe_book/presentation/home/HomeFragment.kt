package com.example.recipe_book.presentation.home

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.recipe_book.R
import com.example.recipe_book.databinding.FragmentHomeBinding
import com.example.recipe_book.domain.model.Recipe
import com.example.recipe_book.presentation.common.UiState
import com.example.recipe_book.util.gone
import com.example.recipe_book.util.toast
import com.example.recipe_book.util.visible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var recipeAdapter: RecipeAdapter

    /** Guards against TabLayout.addTab() callbacks re-triggering selectCategory()
     *  while we're programmatically rebuilding the tabs from a new categories list. */
    private var isRebuildingTabs = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()
        setupSearch()
        setupTabs()
        setupFab()
        setupRetry()

        observeCategories()
        observeUiState()
        observeDeleteState()
        observeLogoutState()
    }

    private fun setupToolbar() {
        binding.toolbar.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.action_profile -> {
                    findNavController().navigate(
                        HomeFragmentDirections.actionHomeFragmentToProfileFragment()
                    )
                    true
                }
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
            onItemClick = { recipe -> onRecipeClicked(recipe) },
            onEditClick = { recipe -> onEditClicked(recipe) },
            onDeleteClick = { recipe -> onDeleteClicked(recipe) }
        )
        binding.recyclerRecipes.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = recipeAdapter
        }
    }

    private fun onRecipeClicked(recipe: Recipe) {
        findNavController().navigate(
            HomeFragmentDirections.actionHomeFragmentToRecipeDetailsFragment(recipe.id)
        )
    }

    private fun onEditClicked(recipe: Recipe) {
        findNavController().navigate(
            HomeFragmentDirections.actionHomeFragmentToAddEditRecipeFragment(recipe.id)
        )
    }

    private fun onDeleteClicked(recipe: Recipe) {
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

    private fun setupSearch() {
        binding.editSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                viewModel.search(s?.toString().orEmpty())
            }
        })
    }

    private fun setupTabs() {
        binding.tabLayoutCategories.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                if (isRebuildingTabs) return
                val category = tab.text?.toString() ?: return
                binding.toolbar.title = if (category == "All") getString(R.string.app_name) else category
                viewModel.selectCategory(category)
            }
            override fun onTabUnselected(tab: TabLayout.Tab) = Unit
            override fun onTabReselected(tab: TabLayout.Tab) = Unit
        })
    }

    private fun setupFab() {
        binding.fabAddRecipe.setOnClickListener {
            findNavController().navigate(
                HomeFragmentDirections.actionHomeFragmentToAddEditRecipeFragment(null)
            )
        }
    }

    private fun setupRetry() {
        binding.buttonRetry.setOnClickListener {
            // Re-selecting the current category re-subscribes the Firestore listener.
            viewModel.selectCategory(viewModel.selectedCategory.value)
        }
    }

    private fun observeCategories() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.categories.collect { categories -> rebuildTabs(categories) }
            }
        }
    }

    private fun rebuildTabs(categories: List<String>) {
        isRebuildingTabs = true
        val currentlySelected = viewModel.selectedCategory.value
        binding.tabLayoutCategories.removeAllTabs()
        categories.forEach { category ->
            val tab = binding.tabLayoutCategories.newTab().setText(category)
            binding.tabLayoutCategories.addTab(tab, category == currentlySelected)
        }
        isRebuildingTabs = false
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state -> render(state) }
            }
        }
    }

    private fun render(state: UiState<List<Recipe>>) {
        binding.progressLoading.gone()
        binding.layoutEmpty.gone()
        binding.layoutError.gone()
        binding.recyclerRecipes.gone()

        when (state) {
            is UiState.Loading -> binding.progressLoading.visible()
            is UiState.Success -> {
                binding.recyclerRecipes.visible()
                recipeAdapter.submitList(state.data)
            }
            is UiState.Empty -> binding.layoutEmpty.visible()
            is UiState.Error -> {
                binding.layoutError.visible()
                binding.textErrorMessage.text = state.message
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
                            // The Firestore snapshot listener updates the feed automatically —
                            // no manual list refresh needed here (blueprint Section 5.3).
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
                                HomeFragmentDirections.actionHomeFragmentToLoginFragment()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
