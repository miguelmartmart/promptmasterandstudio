package com.promptmaster.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.promptmaster.data.Prompt
import com.promptmaster.data.PromptRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import android.content.Context
import androidx.preference.PreferenceManager
import com.promptmaster.utils.getStringList
import com.promptmaster.utils.putStringList
import android.app.Application // Import Application
import androidx.lifecycle.AndroidViewModel // Import AndroidViewModel
import android.content.SharedPreferences // Import SharedPreferences


class PromptViewModel(
    private val repository: PromptRepository,
    application: Application // Accept Application in constructor
) : AndroidViewModel(application) { // Extend AndroidViewModel to get application context

    // State for search query and filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // State for search history
    private val _searchHistory = MutableStateFlow<List<String>>(emptyList())
    val searchHistory: StateFlow<List<String>> = _searchHistory.asStateFlow()

    private val sharedPreferences: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(application)
    }

    init {
        loadSearchHistory()
    }

    private fun loadSearchHistory() {
        _searchHistory.value = sharedPreferences.getStringList("search_history")
    }

    private fun saveSearchHistory(history: List<String>) {
        sharedPreferences.edit().apply {
            putStringList("search_history", history)
        }

    }

    val favoritePrompts: StateFlow<List<Prompt>> = repository.favoritePrompts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // TODO: Add MutableStateFlows for filter selections (category, model, tags)
    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _selectedModel = MutableStateFlow<String?>(null)
    val selectedModel: StateFlow<String?> = _selectedModel.asStateFlow()

    private val _selectedTag = MutableStateFlow<String?>(null)
    val selectedTag: StateFlow<String?> = _selectedTag.asStateFlow()

    private val _showFavoritesOnly = MutableStateFlow(false)
    val showFavoritesOnly: StateFlow<Boolean> = _showFavoritesOnly.asStateFlow()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setShowFavoritesOnly(show: Boolean) {
        _showFavoritesOnly.value = show
    }

    fun setSelectedCategory(category: String?) {
        _selectedCategory.value = category
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val filteredPrompts: StateFlow<List<Prompt>> = combine(
        _searchQuery,
        _selectedCategory,
        _selectedModel,
        _selectedTag,
        _showFavoritesOnly // Include showFavoritesOnly in the combine
    ) { query, category, model, tag, showFavorites ->
        android.util.Log.d("PromptViewModel", "Combining filters: query=$query, category=$category, model=$model, tag=$tag, showFavorites=$showFavorites")
        repository.searchPrompts(query, category, model, tag, showFavorites) // Pass showFavorites to repository
    }
        .flatMapLatest { it } // Flatten the Flow of Flow
        .onEach { prompts ->
            android.util.Log.d("PromptViewModel", "Filtered prompts emitted: ${prompts.size}")
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Function to mark a prompt as used
    fun markPromptAsUsed(id: Int) {
        viewModelScope.launch {
            repository.updateLastUsed(id, System.currentTimeMillis())
        }
    }

    // Expose getAllCategories from the repository
    fun getAllCategories(): Flow<List<String>> = repository.getAllCategories()
        .onEach { categories ->
            android.util.Log.d("PromptViewModel", "Categories emitted: ${categories.size}")
            categories.forEach { category ->
                android.util.Log.d("PromptViewModel", "Category: $category")
            }
        }


    fun insert(prompt: Prompt) = viewModelScope.launch {
        val newPromptId = repository.insert(prompt)
        repository.updateLastUsed(Integer.parseInt(newPromptId.toString()), System.currentTimeMillis()) // Explicitly cast Long to Int
    }

    fun update(prompt: Prompt) = viewModelScope.launch {
        repository.update(prompt)
        repository.updateLastUsed(prompt.id, System.currentTimeMillis())
    }

    fun delete(prompt: Prompt) = viewModelScope.launch {
        repository.delete(prompt)
    }

    fun getPrompt(id: Int) = repository.getPrompt(id)

    fun duplicatePrompt(prompt: Prompt) = viewModelScope.launch {
        val newPrompt = prompt.copy(id = 0) // Create a copy with default ID
        val newPromptId = repository.insert(newPrompt) // Insert the new prompt
        repository.updateLastUsed(Integer.parseInt(newPromptId.toString()), System.currentTimeMillis()) // Explicitly cast Long to Int
    }

    // Function to copy text to clipboard
    fun copyTextToClipboard(text: String) {
        val clipboardManager = getApplication<Application>().getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clipData = android.content.ClipData.newPlainText("Prompt Description", text)
        clipboardManager.setPrimaryClip(clipData)
        // TODO: Find the prompt with this description and update its lastUsed timestamp
    }
}

class PromptViewModelFactory(
    private val repository: PromptRepository,
    private val application: Application // Accept Application in constructor
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PromptViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PromptViewModel(repository, application) as T // Pass application to ViewModel
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
