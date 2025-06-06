package com.promptmaster.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.promptmaster.Application
import com.promptmaster.ui.PromptViewModel

class PromptViewModelFactory(
    private val repository: PromptRepository,
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PromptViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PromptViewModel(repository, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

