package com.promptmaster.data

import kotlinx.coroutines.flow.Flow

class PromptRepository(private val promptDao: PromptDao) {

    val allPrompts: Flow<List<Prompt>> = promptDao.getAllPrompts()
    val favoritePrompts: Flow<List<Prompt>> = promptDao.getFavoritePrompts()

    suspend fun insert(prompt: Prompt) {
        promptDao.insert(prompt)
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
        tag: String?
    ): Flow<List<Prompt>> {
        return promptDao.searchPrompts(searchQuery, category, recommendedModel, tag)
    }
}
