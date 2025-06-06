package com.promptmaster.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.promptmaster.ui.PromptViewModel
import com.promptmaster.ui.PromptViewModelFactory
import com.promptmaster.ui.components.PromptListScreen // Import the new Composable

@Composable
fun HomeScreen(
    viewModelFactory: PromptViewModelFactory,
    onPromptClick: (Int) -> Unit
) {
    val viewModel: PromptViewModel = viewModel(factory = viewModelFactory)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Use the new PromptListScreen Composable
        PromptListScreen(
            viewModel = viewModel,
            showFavoritesOnly = false, // Show all prompts on the home screen
            onPromptClick = onPromptClick,
            onCopyDescriptionClick = { description -> viewModel.copyTextToClipboard(description) } // Use the obtained ViewModel
        )
    }
}
