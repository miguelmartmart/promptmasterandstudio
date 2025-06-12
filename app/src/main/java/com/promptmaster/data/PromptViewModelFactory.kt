package com.promptmaster.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import android.app.Application
import com.promptmaster.ui.PromptViewModel

class PromptViewModelFactory(
    private val repository: PromptRepository,
    private val application: Application, // Accept Application in constructor
    private val promptBackupManager: PromptBackupManager // Add PromptBackupManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PromptViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PromptViewModel(repository, application, promptBackupManager) as T // Pass application and PromptBackupManager to ViewModel
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
