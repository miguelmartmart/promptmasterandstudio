package com.promptmaster.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

class PromptBackupManager(private val promptDao: PromptDao, private val context: Context) {

    private val backupFileName = "prompts_backup.json"

    internal fun getBackupFile(): File? {
        val externalFilesDir = context.getExternalFilesDir(null)
        return if (externalFilesDir != null) {
            File(externalFilesDir, backupFileName)
        } else {
            null
        }
    }

    suspend fun createBackup() = withContext(Dispatchers.IO) {
        val prompts = promptDao.getAllPromptsList()
        val json = Gson().toJson(prompts)
        val backupFile = getBackupFile()
        if (backupFile != null) {
            try {
                backupFile.writeText(json)
                // TODO: Add logging or status update
            } catch (e: IOException) {
                e.printStackTrace()
                // TODO: Handle error
            }
        } else {
            // TODO: Handle case where external files directory is not available
        }
    }

    suspend fun restoreBackup(): Boolean = withContext(Dispatchers.IO) {
        val backupFile = getBackupFile()
        if (backupFile != null && backupFile.exists()) {
            try {
                val json = backupFile.readText()
                val promptListType = object : TypeToken<List<Prompt>>() {}.type
                val prompts: List<Prompt> = Gson().fromJson(json, promptListType)

                prompts.forEach { prompt ->
                    // Check if a prompt with the same title and description already exists
                    val existingPromptCount = promptDao.getPromptCountByContent(prompt.title, prompt.description)
                    if (existingPromptCount == 0) {
                        // If no duplicate exists, insert the prompt
                        promptDao.insert(prompt)
                    } else {
                        // TODO: Handle duplicate (e.g., log a message)
                    }
                }
                // TODO: Add logging or status update
                true
            } catch (e: IOException) {
                e.printStackTrace()
                // TODO: Handle error
                false
            } catch (e: Exception) {
                e.printStackTrace()
                // TODO: Handle error
                false
            }
        } else {
            // TODO: Handle case where backup file does not exist
            false
        }
    }
}
