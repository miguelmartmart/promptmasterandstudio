package com.promptmaster.data

import android.content.Context
import android.os.Environment
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow // Explicitly import asSharedFlow
import androidx.lifecycle.LiveData // Import LiveData
import androidx.lifecycle.MutableLiveData // Import MutableLiveData
import androidx.lifecycle.asLiveData // Import asLiveData
import androidx.lifecycle.liveData // Import liveData builder
import kotlinx.coroutines.flow.onEach // Import onEach
import kotlinx.coroutines.flow.onStart // Import onStart
import kotlinx.coroutines.flow.combine // Import combine
import kotlinx.coroutines.flow.distinctUntilChanged // Import distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest // Import flatMapLatest
import kotlinx.coroutines.Dispatchers // Import Dispatchers
import kotlinx.coroutines.withContext // Import withContext
import java.io.File
import java.io.IOException
import android.app.Application // Import Application
import javax.inject.Inject // Import Inject

class PromptRepository @Inject constructor(
    private val promptDao: PromptDao,
    private val application: Application // Accept Application in constructor
) {

    val pageSize = 20 // Define your page size

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _databaseReadyEvent = MutableSharedFlow<Unit>(replay = 1) // Replay 1 to ensure late collectors get the last event
    val databaseReadyEvent: SharedFlow<Unit> = _databaseReadyEvent.asSharedFlow()
    val databaseReadyLiveData: LiveData<Unit> = databaseReadyEvent.asLiveData() // Expose as LiveData

    // LiveData to hold all prompts, updated when database is ready
    val allPromptsLiveData: LiveData<List<Prompt>> = liveData {
        android.util.Log.d("PromptMasterDebug", "PromptRepository: allPromptsLiveData init. Waiting for databaseReadyEvent.")
        // Wait for the database to be ready
        databaseReadyEvent.collect {
            android.util.Log.d("PromptMasterDebug", "PromptRepository: databaseReadyEvent received. Fetching all prompts.")
            // Once ready, start emitting prompts from the DAO
            promptDao.getAllPrompts().collect { prompts ->
                emit(prompts)
            }
        }
    }

    suspend fun setDatabaseReady() {
        android.util.Log.d("PromptMasterDebug", "PromptRepository: setDatabaseReady called, emitting databaseReadyEvent.")
        _databaseReadyEvent.emit(Unit)
    }

    val favoritePrompts: Flow<List<Prompt>> = promptDao.getFavoritePrompts()

    suspend fun insert(prompt: Prompt): Int { // Keep original insert for other uses if needed
        android.util.Log.d("PromptMasterDebug", "PromptRepository: insert prompt: ${prompt.title}")
        val newPromptId = promptDao.insert(prompt)
        return newPromptId.toInt()
    }

    suspend fun insertOrUpdatePrompt(prompt: Prompt) {
        val existingPrompt = promptDao.getPromptByContent(prompt.title, prompt.description)
        if (existingPrompt != null) {
            // Prompt with same title and description exists, update it
            android.util.Log.d("PromptMasterDebug", "PromptRepository: Updating existing prompt: ${existingPrompt.id} - ${existingPrompt.title}")
            // Preserve original ID, isFavorite, and lastUsed if they are not meant to be overwritten by import
            val updatedPrompt = existingPrompt.copy(
                category = prompt.category,
                subcategory = prompt.subcategory,
                tags = prompt.tags,
                recommendedModel = prompt.recommendedModel,
                description = prompt.description,
                // Keep existing isFavorite and lastUsed unless explicitly changing them
                isFavorite = existingPrompt.isFavorite,
                lastUsed = existingPrompt.lastUsed
            )
            promptDao.update(updatedPrompt)
        } else {
            // Prompt does not exist, insert new one
            android.util.Log.d("PromptMasterDebug", "PromptRepository: Inserting new prompt: ${prompt.title}")
            promptDao.insert(prompt)
        }
    }

    suspend fun update(prompt: Prompt) {
        android.util.Log.d("PromptMasterDebug", "PromptRepository: update prompt: ${prompt.id} - ${prompt.title}, isFavorite: ${prompt.isFavorite}")
        promptDao.update(prompt)
    }

    suspend fun delete(prompt: Prompt) {
        android.util.Log.d("PromptMasterDebug", "PromptRepository: delete prompt: ${prompt.id} - ${prompt.title}")
        promptDao.delete(prompt)
    }

    suspend fun deleteAllPrompts() {
        android.util.Log.d("PromptMasterDebug", "PromptRepository: deleteAllPrompts")
        promptDao.deleteAllPrompts()
    }

    fun getPrompt(id: Int): Flow<Prompt> {
        android.util.Log.d("PromptMasterDebug", "PromptRepository: getPrompt for ID: $id")
        return promptDao.getPrompt(id)
    }

    suspend fun getFilteredAndSortedPrompts(
        searchQuery: String?,
        category: String?,
        subcategory: String?,
        showFavoritesOnly: Boolean,
        limit: Int,
        offset: Int
    ): List<Prompt> {
        android.util.Log.d("PromptMasterDebug", "PromptRepository: getFilteredAndSortedPrompts - Query: '$searchQuery', Cat: '$category', Subcat: '$subcategory', FavOnly: $showFavoritesOnly, Limit: $limit, Offset: $offset")
        return promptDao.getPaginatedFilteredAndSortedPromptsList(
            searchQuery,
            category,
            subcategory,
            showFavoritesOnly,
            limit,
            offset
        )
    }

    suspend fun getAllFilteredAndSortedPromptsList(
        searchQuery: String?,
        category: String?,
        subcategory: String?,
        showFavoritesOnly: Boolean
    ): List<Prompt> {
        android.util.Log.d("PromptMasterDebug", "PromptRepository: getAllFilteredAndSortedPromptsList - Query: '$searchQuery', Cat: '$category', Subcat: '$subcategory', FavOnly: $showFavoritesOnly")
        return promptDao.getAllFilteredAndSortedPromptsList(searchQuery, category, subcategory, showFavoritesOnly)
    }

    fun getAllCategories(): Flow<List<String>> {
        android.util.Log.d("PromptMasterDebug", "PromptRepository: getAllCategories")
        return promptDao.getAllCategories()
    }

    // This function is no longer directly used by HomeViewModel, but kept for other potential uses
    fun getPromptsFiltered(category: String?): Flow<List<Prompt>> {
        android.util.Log.d("PromptMasterDebug", "PromptRepository: getPromptsFiltered for category: $category")
        return promptDao.getPromptsFiltered(category)
    }

    fun getAllSubcategories(category: String?): Flow<List<String>> {
        android.util.Log.d("PromptMasterDebug", "PromptRepository: getAllSubcategories for category: $category")
        return promptDao.getAllSubcategories(category)
    }

    suspend fun updateLastUsed(id: Int, timestamp: Long) {
        android.util.Log.d("PromptMasterDebug", "PromptRepository: updateLastUsed for ID: $id, Timestamp: $timestamp")
        promptDao.updateLastUsed(id, timestamp)
    }

    suspend fun exportPromptsToJson(): String {
        val prompts = promptDao.getAllPromptsList()
        return Gson().toJson(prompts)
    }

    suspend fun writeJsonToFile(context: Context, jsonString: String, filename: String): Boolean {
        return try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, filename)
            file.writeText(jsonString)
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }
}
