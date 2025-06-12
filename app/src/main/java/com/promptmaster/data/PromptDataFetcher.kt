package com.promptmaster.data

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PromptDataFetcher(
    private val repository: PromptRepository,
    private val searchQuery: StateFlow<String>,
    private val selectedCategory: StateFlow<String?>,
    private val selectedSubcategory: StateFlow<String?>,
    private val showFavoritesOnly: StateFlow<Boolean>,
    private val viewModelScope: kotlinx.coroutines.CoroutineScope, // Pass the ViewModel's scope
    private val databaseReadyEvent: SharedFlow<Unit> // Keep this parameter
) {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _prompts = MutableStateFlow<List<Prompt>>(emptyList())
    val prompts: StateFlow<List<Prompt>> = _prompts.asStateFlow()

    private val _currentPage = MutableStateFlow(0)
    private val _lastFetchedSize = MutableStateFlow(0)

    // New refresh trigger
    private val _refreshTrigger = MutableStateFlow(0) // Use MutableStateFlow<Int>
    val refreshTrigger: StateFlow<Int> = _refreshTrigger.asStateFlow() // Expose as StateFlow

    init {
        viewModelScope.launch {
            // Emit initial value to trigger first fetch
            // No need to emit explicitly here, as StateFlow has an initial value

            combine(
                searchQuery,
                selectedCategory,
                selectedSubcategory,
                showFavoritesOnly,
                _currentPage,
                databaseReadyEvent,
                _refreshTrigger // Include refresh trigger in combine
            ) { args -> // Use a single 'args' parameter for combined values
                val query = args[0] as String?
                val category = args[1] as String?
                val subcategory = args[2] as String?
                val favoritesOnly = args[3] as Boolean
                val page = args[4] as Int
                // args[5] is Unit from databaseReadyEvent, args[6] is Int from _refreshTrigger

                android.util.Log.d("PromptMasterDebug", "PromptDataFetcher: combine triggered, fetching prompts...")
                _isLoading.value = true
                try {
                    val fetchedPrompts = repository.getFilteredAndSortedPrompts(
                        searchQuery = query,
                        category = category,
                        subcategory = subcategory,
                        showFavoritesOnly = favoritesOnly,
                        limit = repository.pageSize,
                        offset = page * repository.pageSize
                    )

                    _lastFetchedSize.value = fetchedPrompts.size

                    if (page == 0) {
                        _prompts.value = fetchedPrompts.toList() // .toList() ensures a new list instance
                    } else {
                        _prompts.update { currentList ->
                            currentList + fetchedPrompts
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("PromptDataFetcher", "Error fetching prompts: ${e.message}", e)
                    if (page == 0) {
                        _prompts.value = emptyList()
                    }
                } finally {
                    _isLoading.value = false
                }
            }.collect()
        }
    }

    fun loadNextPage() {
        if (!_isLoading.value && _lastFetchedSize.value == repository.pageSize) {
            _currentPage.update { it + 1 }
        }
    }

    fun refreshPrompts(forceReload: Boolean = false, showFavoritesOnly: Boolean) {
        viewModelScope.launch {
            android.util.Log.d("PromptMasterDebug", "PromptDataFetcher: refreshPrompts: Refreshing data with forceReload = $forceReload, showFavoritesOnly = $showFavoritesOnly")
            _currentPage.value = 0 // Reset page to 0 for a refresh
            _refreshTrigger.value++ // Increment to trigger a new fetch
        }
    }

    // Function to mark a prompt as used
    suspend fun markPromptAsUsed(id: Int) { // Make it a suspend function
        repository.updateLastUsed(id, System.currentTimeMillis())
        android.util.Log.d(
            "PromptDataFetcher",
            "markPromptAsUsed: Updated lastUsed for prompt ID $id"
        )
        // No need to call loadNextPage here, the ViewModel will call refreshPrompts
    }

    // Expose getAllCategories from the repository
    fun getAllCategories(): Flow<List<String>> = repository.getAllCategories()
        .onEach { categories ->
            android.util.Log.d("PromptDataFetcher", "Categories emitted: ${categories.size}")
            categories.forEach { category ->
                android.util.Log.d("PromptDataFetcher", "Category: $category")
            }
        }

    // Expose getAllSubcategories from the repository
    fun getAllSubcategories(category: String?): Flow<List<String>> =
        repository.getAllSubcategories(category)
            .onEach { subcategories ->
                android.util.Log.d(
                    "PromptDataFetcher",
                    "Subcategories emitted: ${subcategories.size}"
                )
                subcategories.forEach { subcategory ->
                    android.util.Log.d("PromptDataFetcher", "Subcategory: $subcategory")
                }
            }
}
