package com.promptmaster.ui


import androidx.lifecycle.viewModelScope
import com.promptmaster.data.Prompt
import com.promptmaster.data.PromptRepository
import com.promptmaster.data.PromptBackupManager // Import PromptBackupManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import android.content.Context
import android.app.Application // Import Application
import androidx.lifecycle.AndroidViewModel // Import AndroidViewModel
import androidx.lifecycle.LiveData // Import LiveData
import androidx.lifecycle.asLiveData // Import asLiveData
import com.promptmaster.utils.PromptUtils // Import PromptUtils
import com.promptmaster.data.PromptDataOperations // Import PromptDataOperations
import com.promptmaster.data.PromptDataFetcher // Import PromptDataFetcher
import dagger.hilt.android.lifecycle.HiltViewModel // Import HiltViewModel
import javax.inject.Inject // Import Inject

@HiltViewModel
class PromptViewModel @Inject constructor(
    private val repository: PromptRepository,
    application: Application, // Accept Application in constructor
    private val promptBackupManager: PromptBackupManager, // Add PromptBackupManager
    private val promptUtils: PromptUtils, // Inject PromptUtils
    private val promptDataOperations: PromptDataOperations // Inject PromptDataOperations
) : AndroidViewModel(application), PromptOperationsViewModel { // Extend AndroidViewModel to get application context and implement interface

    // State for search query and filters
    private val _searchQuery = MutableStateFlow("")
    override val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // State for search history
    private val _searchHistory =
        MutableStateFlow<List<String>>(promptUtils.loadSearchHistory()) // Load history using PromptUtils
    val searchHistory: StateFlow<List<String>> = _searchHistory.asStateFlow()

    // Channel for one-time events like status messages
    val status = promptUtils.status // Expose status from PromptUtils

    // TODO: Add MutableStateFlows for filter selections (category, model, tags)
    private val _selectedCategory = MutableStateFlow<String?>(null)
    override val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _selectedSubcategory = MutableStateFlow<String?>(null) // Add subcategory state
    override val selectedSubcategory: StateFlow<String?> = _selectedSubcategory.asStateFlow()

    private val _selectedModel = MutableStateFlow<String?>(null)
    val selectedModel: StateFlow<String?> = _selectedModel.asStateFlow()

    private val _selectedTag = MutableStateFlow<String?>(null)
    val selectedTag: StateFlow<String?> = _selectedTag.asStateFlow()

    // Instantiate PromptDataFetcher
    private val promptDataFetcher = PromptDataFetcher(
        repository = repository,
        searchQuery = _searchQuery,
        selectedCategory = _selectedCategory,
        selectedSubcategory = _selectedSubcategory,
        showFavoritesOnly = MutableStateFlow(false), // Always false for Home tab
        viewModelScope = viewModelScope, // Pass the ViewModel's scope
        databaseReadyEvent = repository.databaseReadyEvent // Pass the database ready event
    )

    // Expose data and loading state from PromptDataFetcher
    override val isLoading: StateFlow<Boolean> = promptDataFetcher.isLoading
    override val prompts: StateFlow<List<Prompt>> = promptDataFetcher.prompts // Re-add for interface implementation
    val promptsLiveData: LiveData<List<Prompt>> = promptDataFetcher.prompts.asLiveData() // Expose as LiveData for Java

    override fun setSearchQuery(query: String) {
        _searchQuery.value = query
        // Save search query to history
        var currentHistory = _searchHistory.value.toMutableList()
        if (currentHistory.contains(query)) {
            currentHistory.remove(query)
        }
        currentHistory.add(0, query)
        if (currentHistory.size > 10) { // Limit history size
            currentHistory = currentHistory.take(10).toMutableList()
        }
        _searchHistory.value = currentHistory
        promptUtils.saveSearchHistory(currentHistory) // Save updated history
    }

    override fun setSelectedCategory(category: String?) {
        _selectedCategory.value = category
        // Reset subcategory when category changes
        _selectedSubcategory.value = null
    }

    override fun setSelectedSubcategory(subcategory: String?) {
        _selectedSubcategory.value = subcategory
    }

    override fun loadNextPage() {
        promptDataFetcher.loadNextPage()
    }

    override suspend fun markPromptAsUsed(id: Int) {
        android.util.Log.d("PromptMasterDebug", "PromptViewModel: markPromptAsUsed for ID: $id")
        promptDataFetcher.markPromptAsUsed(id)
    }

    override fun getAllCategories(): Flow<List<String>> = promptDataFetcher.getAllCategories()

    override fun getAllSubcategories(category: String?): Flow<List<String>> =
        promptDataFetcher.getAllSubcategories(category)

    override suspend fun insert(prompt: Prompt) {
        android.util.Log.d("PromptMasterDebug", "PromptViewModel: insert prompt: ${prompt.title}")
        promptDataOperations.insert(prompt)
    }

    override suspend fun update(prompt: Prompt) {
        android.util.Log.d("PromptMasterDebug", "PromptViewModel: update prompt: ${prompt.id} - ${prompt.title}")
        promptDataOperations.update(prompt)
        refresh() // Refresh prompts after update (e.g., favorite status change)
    }

    override suspend fun delete(prompt: Prompt) {
        android.util.Log.d("PromptMasterDebug", "PromptViewModel: delete prompt: ${prompt.id} - ${prompt.title}")
        promptDataOperations.delete(prompt)
        refresh() // Refresh prompts after deletion
    }

    override fun getPrompt(id: Int) = promptDataOperations.getPrompt(id)

    override suspend fun duplicatePrompt(prompt: Prompt) {
        android.util.Log.d("PromptMasterDebug", "PromptViewModel: duplicatePrompt: ${prompt.id} - ${prompt.title}")
        promptDataOperations.duplicatePrompt(prompt)
        refresh() // Refresh prompts after duplication
    }

    override fun toggleFavorite(prompt: Prompt) {
        viewModelScope.launch {
            android.util.Log.d("PromptMasterDebug", "PromptViewModel: toggleFavorite for ID: ${prompt.id}, current favorite: ${prompt.isFavorite}")
            val updatedPrompt = prompt.copy(isFavorite = !prompt.isFavorite)
            promptDataOperations.update(updatedPrompt)
            refresh() // Refresh prompts after favorite status change
        }
    }

    fun onPromptDescriptionCopied(promptId: Int, description: String) {
        viewModelScope.launch {
            android.util.Log.d("PromptMasterDebug", "PromptViewModel: onPromptDescriptionCopied for ID: $promptId")
            promptDataFetcher.markPromptAsUsed(promptId)
            promptUtils.copyToClipboard(description)
            refresh() // Refresh prompts after copying description (to reorder by lastUsed)
        }
    }

    suspend fun resetApplicationData() {
        android.util.Log.d("PromptMasterDebug", "PromptViewModel: resetApplicationData")
        promptUtils.resetApplicationData()
        promptDataFetcher.refreshPrompts(forceReload = true, showFavoritesOnly = false) // Force reload after reset, explicitly pass showFavoritesOnly
    }

    suspend fun importPrompts(context: Context, uri: android.net.Uri) {
        android.util.Log.d("PromptMasterDebug", "PromptViewModel: importPrompts")
        promptUtils.importPrompts(uri)
        promptDataFetcher.refreshPrompts(forceReload = true, showFavoritesOnly = false) // Force reload after import, explicitly pass showFavoritesOnly
    }

    suspend fun exportPrompts(context: Context): String? { // Change return type to String?
        android.util.Log.d("PromptMasterDebug", "PromptViewModel: exportPrompts")
        return promptUtils.exportPrompts()
    }

    fun refresh() {
        android.util.Log.d("PromptMasterDebug", "PromptViewModel: refresh() called")
        promptDataFetcher.refreshPrompts(forceReload = true, showFavoritesOnly = false) // Explicitly pass showFavoritesOnly for Home
    }
}
