package com.example.recipe_book.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recipe_book.domain.model.AppException
import com.example.recipe_book.domain.model.Recipe
import com.example.recipe_book.domain.repository.AuthRepository
import com.example.recipe_book.domain.usecase.auth.LogoutUseCase
import com.example.recipe_book.domain.usecase.recipe.DeleteRecipeUseCase
import com.example.recipe_book.domain.usecase.recipe.GetRecipesByCategoryUseCase
import com.example.recipe_book.presentation.common.UiState
import com.example.recipe_book.util.Validators
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val ALL_TAB = "All"

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getRecipesByCategoryUseCase: GetRecipesByCategoryUseCase,
    private val deleteRecipeUseCase: DeleteRecipeUseCase,
    private val authRepository: AuthRepository,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    /** Read once per Fragment lifecycle by RecipeAdapter to toggle owner-only
     *  edit/delete icons — a lambda rather than a stored val so it always
     *  reflects the live Firebase session rather than a stale snapshot. */
    val currentUserId: () -> String? = { authRepository.getCurrentUserId() }

    private val _deleteState = MutableStateFlow<UiState<Unit>?>(null)
    val deleteState: StateFlow<UiState<Unit>?> = _deleteState.asStateFlow()

    private val _logoutState = MutableStateFlow<UiState<Unit>?>(null)
    val logoutState: StateFlow<UiState<Unit>?> = _logoutState.asStateFlow()

    private val _selectedCategory = MutableStateFlow(ALL_TAB)
    val selectedCategory: StateFlow<String> = _selectedCategory

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    /**
     * Tabs are derived from the full recipe set, independent of which tab is
     * currently selected — otherwise switching to "Desserts" would make every
     * other category tab disappear. See blueprint Section 8.3: categories are
     * free-text, so we normalize + de-duplicate here (Section 1.3 edge case).
     */
    private val allRecipesResult = getRecipesByCategoryUseCase(ALL_TAB)

    val categories: StateFlow<List<String>> = allRecipesResult
        .map { result ->
            val recipes = result.getOrNull().orEmpty()
            val distinctCategories = recipes
                .map { Validators.normalizeCategory(it.category) }
                .filter { it.isNotBlank() }
                .distinct()
                .sorted()
            listOf(ALL_TAB) + distinctCategories
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf(ALL_TAB))

    /**
     * Category filtering happens server-side via Firestore's whereEqualTo
     * query (GetRecipesByCategoryUseCase) and only re-subscribes when the
     * selected TAB changes — not on every keystroke. Search-by-title is a
     * lightweight client-side substring filter layered on top of whatever
     * the category query last emitted, which is acceptable at this
     * project's scale per blueprint Section 4.7's documented trade-off, and
     * pulled forward from Phase 4 since it's cheap once the feed already
     * exists.
     */
    private val categoryRecipesFlow = _selectedCategory
        .flatMapLatest { category -> getRecipesByCategoryUseCase(category) }

    private val debouncedQueryFlow = _searchQuery
        .map { it.trim() }
        .debounce(300)

    val uiState: StateFlow<UiState<List<Recipe>>> =
        combine(categoryRecipesFlow, debouncedQueryFlow) { result, query ->
            result.fold(
                onSuccess = { recipes ->
                    val filtered = if (query.isBlank()) {
                        recipes
                    } else {
                        recipes.filter { it.title.contains(query, ignoreCase = true) }
                    }
                    if (filtered.isEmpty()) UiState.Empty else UiState.Success(filtered)
                },
                onFailure = { throwable ->
                    val message = (throwable as? AppException)?.error?.message
                        ?: "Something went wrong. Please try again."
                    UiState.Error(message)
                }
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun search(query: String) {
        _searchQuery.value = query
    }

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
