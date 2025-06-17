package com.promptmaster.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.promptmaster.data.Prompt
import com.promptmaster.data.PromptBackupManager
import com.promptmaster.data.PromptRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

class PromptUtils(
    private val context: Context,
    private val repository: PromptRepository,
    private val promptBackupManager: PromptBackupManager
) {

    private val sharedPreferences: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(context)
    }

    // Channel for one-time events like status messages
    private val _statusChannel = Channel<String>(Channel.BUFFERED)
    val status = _statusChannel.receiveAsFlow()

    fun loadSearchHistory(): List<String> {
        return sharedPreferences.getStringList("search_history")
    }

    fun saveSearchHistory(history: List<String>) {
        sharedPreferences.edit().apply {
            putStringList("search_history", history)
            apply()
        }
    }

    // Function to copy text to clipboard
    fun copyToClipboard(text: String) {
        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clipData = android.content.ClipData.newPlainText("Prompt Description", text)
        clipboardManager.setPrimaryClip(clipData)
    }

    // Function to reset application data to initial state
    suspend fun resetApplicationData() {
        repository.deleteAllPrompts()

        // Delete the backup file
        val backupFile = promptBackupManager.getBackupFile()
        if (backupFile != null && backupFile.exists()) {
            try {
                backupFile.delete()
                android.util.Log.d("PromptUtils", "Backup file deleted during reset")
            } catch (e: Exception) {
                android.util.Log.e("PromptUtils", "Error deleting backup file during reset", e)
            }
        }

        try {
            val inputStream: InputStream = context.assets.open("initial_prompts.json")
            val reader = InputStreamReader(inputStream)
            val promptListType = object : TypeToken<List<Prompt>>() {}.type
            val prompts: List<Prompt> = Gson().fromJson(reader, promptListType)

            prompts.forEach { prompt ->
                repository.insert(prompt)
            }
            _statusChannel.send("Application data reset to initial prompts successfully")
        } catch (e: IOException) {
            e.printStackTrace()
            _statusChannel.send("Error reading initial_prompts.json: ${e.message}")
        } catch (e: Exception) {
            e.printStackTrace()
            _statusChannel.send("Error resetting application data: ${e.message}")
        }
    }

    suspend fun importPrompts(uri: android.net.Uri) {
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val reader = InputStreamReader(inputStream)
                val promptListType = object : TypeToken<List<Prompt>>() {}.type
                val prompts: List<Prompt> = Gson().fromJson(reader, promptListType)

                prompts.forEach { prompt ->
                    repository.insert(prompt)
                }
                _statusChannel.send("Prompts imported successfully")
            }
        } catch (e: IOException) {
            e.printStackTrace()
            _statusChannel.send("Error reading prompts file: ${e.message}")
        } catch (e: Exception) {
            e.printStackTrace()
            _statusChannel.send("Error importing prompts: ${e.message}")
        }
    }

    suspend fun exportPrompts(): String? {
        return try {
            val json = repository.exportPromptsToJson() // Corrected function call
            _statusChannel.send("Prompts data prepared for export.")
            json
        } catch (e: Exception) {
            e.printStackTrace()
            _statusChannel.send("Error preparing prompts for export: ${e.message}")
            null
        }
    }
}
