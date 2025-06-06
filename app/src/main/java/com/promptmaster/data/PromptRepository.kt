package com.promptmaster.data

import kotlinx.coroutines.flow.Flow

class PromptRepository(private val promptDao: PromptDao) {

    val allPrompts: Flow<List<Prompt>> = promptDao.getAllPrompts()
    val favoritePrompts: Flow<List<Prompt>> = promptDao.getFavoritePrompts()

    suspend fun insert(prompt: Prompt): Long { // Explicitly declare return type as Long
        return promptDao.insert(prompt) // Return the result of the DAO insert
    }

    suspend fun update(prompt: Prompt) {
        promptDao.update(prompt)
    }

    suspend fun delete(prompt: Prompt) {
        promptDao.delete(prompt)
    }

    fun getPrompt(id: Int): Flow<Prompt> {
        return promptDao.getPrompt(id)
    }

    // TODO: Add methods for searching and filtering

    fun searchPrompts(
        searchQuery: String?,
        category: String?,
        recommendedModel: String?,
        tag: String?,
        showFavoritesOnly: Boolean // Add showFavoritesOnly parameter
    ): Flow<List<Prompt>> {
        // Pass all parameters, including showFavoritesOnly, to the DAO
        return promptDao.getFilteredAndSortedPrompts(searchQuery, category, showFavoritesOnly)
    }

    fun getAllCategories(): Flow<List<String>> = promptDao.getAllCategories()

    fun getPromptsFiltered(category: String?): Flow<List<Prompt>> =
        promptDao.getPromptsFiltered(category)

    suspend fun updateLastUsed(id: Int, timestamp: Long) {
        promptDao.updateLastUsed(id, timestamp)
    }

    fun getFilteredAndSortedPrompts(
        searchQuery: String?,
        category: String?,
        showFavoritesOnly: Boolean
    ): Flow<List<Prompt>> {
        return promptDao.getFilteredAndSortedPrompts(searchQuery, category, showFavoritesOnly)
    }
}
