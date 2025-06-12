package com.promptmaster.ui


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.promptmaster.data.PromptViewModelFactory



class CombinedViewModelFactory(
    private val promptViewModelFactory: PromptViewModelFactory,
    private val favoritesViewModelFactory: FavoritesViewModelFactory
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PromptViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return promptViewModelFactory.create(modelClass) as T
        } else if (modelClass.isAssignableFrom(FavoritesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return favoritesViewModelFactory.create(modelClass) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
    }
}
