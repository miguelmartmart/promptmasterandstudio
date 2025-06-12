package com.promptmaster.ui

import com.promptmaster.data.Prompt
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface PromptOperationsViewModel {
    // Operations
    suspend fun insert(prompt: Prompt)
    suspend fun update(prompt: Prompt)
    suspend fun delete(prompt: Prompt)
    suspend fun duplicatePrompt(prompt: Prompt)
    suspend fun markPromptAsUsed(id: Int)
    fun toggleFavorite(prompt: Prompt)

    // State and Data Flows
    fun getPrompt(id: Int): Flow<Prompt>
    val prompts: StateFlow<List<Prompt>>
    val isLoading: StateFlow<Boolean>
    val searchQuery: StateFlow<String>
    val selectedCategory: StateFlow<String?>
    val selectedSubcategory: StateFlow<String?>

    // Data fetching/filtering functions
    fun getAllCategories(): Flow<List<String>>
    fun getAllSubcategories(category: String?): Flow<List<String>>
    fun setSearchQuery(query: String)
    fun setSelectedCategory(category: String?)
    fun setSelectedSubcategory(subcategory: String?)
    fun loadNextPage()
}
