package com.promptmaster.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.promptmaster.data.Prompt
import com.promptmaster.data.PromptRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PromptViewModel(private val repository: PromptRepository) : ViewModel() {

    // State for search query and filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

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


    @OptIn(ExperimentalCoroutinesApi::class)
    val filteredPrompts: StateFlow<List<Prompt>> = combine(
        _searchQuery,
        _selectedCategory,
        _selectedModel,
        _selectedTag
    ) { query, category, model, tag ->
        android.util.Log.d("PromptViewModel", "Combining filters: query=$query, category=$category, model=$model, tag=$tag")
        repository.searchPrompts(query, category, model, tag)
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


    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun setSelectedModel(model: String?) {
        _selectedModel.value = model
    }

    fun setSelectedTag(tag: String?) {
        _selectedTag.value = tag
    }


    fun insert(prompt: Prompt) = viewModelScope.launch {
        repository.insert(prompt)
    }

    fun update(prompt: Prompt) = viewModelScope.launch {
        repository.update(prompt)
    }

    fun delete(prompt: Prompt) = viewModelScope.launch {
        repository.delete(prompt)
    }

    fun getPrompt(id: Int) = repository.getPrompt(id)

    fun duplicatePrompt(prompt: Prompt) = viewModelScope.launch {
        val newPrompt = prompt.copy(id = 0) // Create a copy with default ID
        repository.insert(newPrompt) // Insert the new prompt
    }
}

class PromptViewModelFactory(private val repository: PromptRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PromptViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PromptViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
