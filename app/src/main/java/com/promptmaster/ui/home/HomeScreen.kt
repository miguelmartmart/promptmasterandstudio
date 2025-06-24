package com.promptmaster.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalConfiguration // Import LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.hilt.navigation.compose.hiltViewModel // Import hiltViewModel
import com.promptmaster.ui.PromptViewModel // Import PromptViewModel
import com.promptmaster.ui.components.PromptListScreen // Import the new Composable
// Removed ScreenMetrics import

@Composable
fun HomeScreen(
    onPromptClick: (Int) -> Unit,
    horizontalPadding: Dp // Accept horizontalPadding as a parameter
) {
    val viewModel: PromptViewModel = hiltViewModel()

    Column(
        modifier = Modifier
            .fillMaxSize()
            // Removed horizontal padding here, as PromptListScreen handles it internally
    ) {
        // Use the new PromptListScreen Composable
        PromptListScreen(
            viewModel = viewModel,
            onPromptClick = onPromptClick,
            onCopyDescriptionClick = { promptId, description ->
                viewModel.onPromptDescriptionCopied(promptId, description)
            },
            horizontalPadding = horizontalPadding // Pass horizontalPadding
        )
    }
}
