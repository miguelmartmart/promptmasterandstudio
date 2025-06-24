package com.promptmaster.data

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject // Import Inject

class PromptDataOperations @Inject constructor(
    private val repository: PromptRepository,
    private val promptBackupManager: PromptBackupManager
) {

    suspend fun insert(prompt: Prompt): Int {
        android.util.Log.d("PromptMasterDebug", "PromptDataOperations: insert prompt: ${prompt.title}")
        val newPromptId = repository.insert(prompt)
        repository.updateLastUsed(newPromptId, System.currentTimeMillis()) // newPromptId is already Int
        promptBackupManager.createBackup() // Create backup after insert
        return newPromptId
    }

    suspend fun update(prompt: Prompt) {
        android.util.Log.d("PromptMasterDebug", "PromptDataOperations: update prompt: ${prompt.id} - ${prompt.title}, isFavorite: ${prompt.isFavorite}")
        repository.update(prompt)
        repository.updateLastUsed(prompt.id, System.currentTimeMillis()) // Update lastUsed timestamp
        promptBackupManager.createBackup() // Create backup after update
    }

    suspend fun delete(prompt: Prompt) {
        android.util.Log.d("PromptMasterDebug", "PromptDataOperations: delete prompt: ${prompt.id} - ${prompt.title}")
        repository.delete(prompt)
        promptBackupManager.createBackup() // Create backup after delete
    }

    fun getPrompt(id: Int): Flow<Prompt> {
        android.util.Log.d("PromptMasterDebug", "PromptDataOperations: getPrompt for ID: $id")
        return repository.getPrompt(id)
    }

    suspend fun duplicatePrompt(prompt: Prompt) {
        android.util.Log.d("PromptMasterDebug", "PromptDataOperations: duplicatePrompt: ${prompt.id} - ${prompt.title}")
        val newPrompt = prompt.copy(id = 0) // Create a copy with default ID
        val newPromptId = repository.insert(newPrompt) // Insert the new prompt
        repository.updateLastUsed(newPromptId, System.currentTimeMillis()) // newPromptId is already Int
    }

    suspend fun deleteAllPrompts() {
        android.util.Log.d("PromptMasterDebug", "PromptDataOperations: deleteAllPrompts")
        repository.deleteAllPrompts()
        promptBackupManager.createBackup() // Create backup after deleting all prompts
    }
}
