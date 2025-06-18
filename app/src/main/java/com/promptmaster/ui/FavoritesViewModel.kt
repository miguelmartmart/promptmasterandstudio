package com.promptmaster.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.promptmaster.data.Prompt
import com.promptmaster.data.PromptBackupManager
import com.promptmaster.data.PromptDataFetcher
import com.promptmaster.data.PromptRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.*
import dagger.hilt.android.lifecycle.HiltViewModel // Import HiltViewModel
import javax.inject.Inject // Import Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val repository: PromptRepository,
    application: Application,
    private val promptBackupManager: PromptBackupManager
) : AndroidViewModel(application), PromptOperationsViewModel {

    // Filters for favorites tab (fixed)
    private val _searchQuery = MutableStateFlow("") // Still allow search within favorites
    override val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    override val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _selectedSubcategory = MutableStateFlow<String?>(null)
    override val selectedSubcategory: StateFlow<String?> = _selectedSubcategory.asStateFlow()

    private val _showFavoritesOnly = MutableStateFlow(true) // Always true for favorites tab
    val showFavoritesOnly: StateFlow<Boolean> = _showFavoritesOnly.asStateFlow() // This one does not need override as it's not in the interface

    // Instantiate PromptDataFetcher for favorites
    private val promptDataFetcher = PromptDataFetcher(
        repository = repository,
        searchQuery = _searchQuery,
        selectedCategory = _selectedCategory,
        selectedSubcategory = _selectedSubcategory,
        showFavoritesOnly = _showFavoritesOnly, // This will always be true
        viewModelScope = viewModelScope,
        databaseReadyEvent = repository.databaseReadyEvent // Pass the database ready event
    )

    override val isLoading: StateFlow<Boolean> = promptDataFetcher.isLoading
    override val prompts: StateFlow<List<Prompt>> = promptDataFetcher.prompts

    override fun setSearchQuery(query: String) {
        _searchQuery.value = query
        refresh() // Refresh prompts when search query changes
    }

    override fun setSelectedCategory(category: String?) {
        // Only reset subcategory if the category is actually changing or being cleared
        if (_selectedCategory.value != category) {
            _selectedCategory.value = category
            _selectedSubcategory.value = null // Reset subcategory when category changes
            refresh() // Refresh prompts when category changes
        }
    }

    override fun setSelectedSubcategory(subcategory: String?) {
        _selectedSubcategory.value = subcategory
        refresh() // Refresh prompts when subcategory changes
    }

    override fun loadNextPage() {
        promptDataFetcher.loadNextPage()
    }

    override suspend fun update(prompt: Prompt) {
        repository.update(prompt)
        refresh() // Refresh prompts after update (e.g., favorite status change)
    }

    override suspend fun delete(prompt: Prompt) {
        repository.delete(prompt)
        refresh() // Refresh prompts after deletion
    }

    override suspend fun insert(prompt: Prompt) {
        repository.insert(prompt)
        refresh() // Refresh prompts after insert
    }

    override suspend fun duplicatePrompt(prompt: Prompt) {
        repository.insert(prompt.copy(id = 0))
        refresh() // Refresh prompts after duplication
    }

    override suspend fun markPromptAsUsed(id: Int) {
        promptDataFetcher.markPromptAsUsed(id)
    }

    override fun getPrompt(id: Int): Flow<Prompt> {
        return repository.getPrompt(id)
    }

    override fun getAllCategories(): Flow<List<String>> = promptDataFetcher.getAllCategories()
    override fun getAllSubcategories(category: String?): Flow<List<String>> = promptDataFetcher.getAllSubcategories(category)

    override fun toggleFavorite(prompt: Prompt) {
        viewModelScope.launch {
            val updatedPrompt = prompt.copy(isFavorite = !prompt.isFavorite)
            repository.update(updatedPrompt)
            refresh() // Refresh prompts after favorite status change
        }
    }

    fun refresh() {
        promptDataFetcher.refreshPrompts(forceReload = true, showFavoritesOnly = true) // Explicitly pass showFavoritesOnly for Favorites
    }
}
