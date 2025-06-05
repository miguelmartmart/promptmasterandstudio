package com.promptmaster.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.CopyAll

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource // Import stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.promptmaster.R // Import R
import com.promptmaster.data.Prompt
import com.promptmaster.ui.PromptViewModel
import com.promptmaster.ui.PromptViewModelFactory // Assuming the factory is in ui package
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import android.content.ClipData
import android.content.ClipboardManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExposedDropdownMenuBox

import androidx.compose.material3.DropdownMenuItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: PromptViewModel = viewModel(factory = PromptViewModelFactory(TODO("Provide PromptRepository"))), // TODO: Provide PromptRepository
    onPromptClick: (Int) -> Unit,
    onAddPromptClick: () -> Unit,
) {
    val prompts by viewModel.filteredPrompts.collectAsState() // Use filteredPrompts
    val searchQuery by viewModel.searchQuery.collectAsState()
    var expanded by remember { mutableStateOf(false) } // State for dropdown expansion
    val searchHistory = remember { mutableStateListOf<String>() } // State for search history
    // TODO: Collect state for filter selections (category, model, tags)

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.app_name)) }) // Use stringResource
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddPromptClick) {
                Icon(Icons.Filled.Add, stringResource(R.string.add_new_prompt)) // Use stringResource
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp)) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        viewModel.setSearchQuery(it)
                        if (it.isNotBlank()) {
                            searchHistory.remove(it) // Remove if already exists to maintain uniqueness and order
                            searchHistory.add(0, it) // Add to the beginning
                            if (searchHistory.size > 10) {
                                searchHistory.removeAt(searchHistory.size - 1) // Keep only the last 10
                            }
                        }
                    }, // Update ViewModel state
                    label = { Text(stringResource(R.string.search_prompts_label)) }, // TODO: Add search_prompts_label string resource
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .menuAnchor(), // Anchor the dropdown to the TextField
                    readOnly = false // Allow typing
                )
                // TODO: Add filter UI elements (Dropdowns or similar) and update ViewModel state

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    searchHistory.forEach { historyItem ->
                        DropdownMenuItem(
                            text = { Text(historyItem) },
                            onClick = {
                                viewModel.setSearchQuery(historyItem)
                                expanded = false
                            }
                        )
                    }
                }
            }

            Text(stringResource(R.string.prompt_list_title)) // TODO: Add prompt_list_title string resource
            LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
                items(prompts) { prompt ->
                    PromptItem(
                        prompt = prompt,
                        onPromptClick = onPromptClick,
                        onDeleteClick = { viewModel.delete(it) },
                        onFavoriteClick = { viewModel.update(it.copy(isFavorite = !it.isFavorite)) },
                        onDuplicateClick = { viewModel.duplicatePrompt(it) } // Add duplicate action
                    )
                }
            }
        }
    }
}

@Composable
fun PromptItem(
    prompt: Prompt,
    onPromptClick: (Int) -> Unit,
    onDeleteClick: (Prompt) -> Unit,
    onFavoriteClick: (Prompt) -> Unit,
    onDuplicateClick: (Prompt) -> Unit // Add duplicate action parameter
) {
    Card(
        modifier = Modifier
            .padding(vertical = 4.dp)
            .fillMaxWidth()
            .clickable { onPromptClick(prompt.id) }
            .height(150.dp) // Set a fixed height for the card
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = prompt.title, style = MaterialTheme.typography.titleMedium, maxLines = 1)
            Text(text = prompt.category, style = MaterialTheme.typography.bodySmall, maxLines = 1)
            Text(text = prompt.description, maxLines = 2) // Limit description to two lines
            // TODO: Display other prompt details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onFavoriteClick(prompt) }) {
                    Icon(
                        imageVector = if (prompt.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = if (prompt.isFavorite) stringResource(R.string.remove_from_favorites_content_description) else stringResource(R.string.add_to_favorites_content_description) // Use stringResource
                    )
                }
                val context = LocalContext.current
                IconButton(onClick = {
                    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clipData = ClipData.newPlainText("Prompt Description", prompt.description)
                    clipboardManager.setPrimaryClip(clipData)
                    // TODO: Show a toast message indicating the text was copied
                }) {
                    Icon(Icons.Filled.ContentCopy, stringResource(R.string.copy_prompt_description_content_description)) // TODO: Add string resource
                }
                IconButton(onClick = { onDuplicateClick(prompt) }) { // Add duplicate icon button
                    Icon(Icons.Filled.CopyAll, stringResource(R.string.duplicate_prompt_content_description)) // Use stringResource
                }
                IconButton(onClick = { onDeleteClick(prompt) }) {
                    Icon(Icons.Filled.Delete, stringResource(R.string.delete_prompt_content_description)) // Use stringResource
                }
            }
        }
    }
}

// TODO: Add Preview
