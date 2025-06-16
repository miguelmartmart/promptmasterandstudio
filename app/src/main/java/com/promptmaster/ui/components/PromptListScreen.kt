package com.promptmaster.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.promptmaster.ui.PromptViewModel
import com.promptmaster.ui.PromptOperationsViewModel // New import
import androidx.compose.ui.res.stringResource // Import stringResource
import com.promptmaster.R // Import R
import androidx.compose.foundation.layout.wrapContentSize // Import wrapContentSize
import androidx.compose.ui.Alignment // Import Alignment
import kotlinx.coroutines.launch // Import launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromptListScreen(
    viewModel: PromptOperationsViewModel, // Change to PromptOperationsViewModel
    onPromptClick: (Int) -> Unit,
    onCopyDescriptionClick: (Int, String) -> Unit // Modify to accept prompt ID and description
) {
    val coroutineScope = rememberCoroutineScope() // Define coroutine scope once at the top

    // Always collect prompts from the main prompts flow, filtering is handled in the ViewModel/Repository
    val prompts by viewModel.prompts.collectAsState()
    android.util.Log.d("PromptMasterDebug", "PromptListScreen: Prompts collected: ${prompts.size}")

    val isLoading by viewModel.isLoading.collectAsState()
    val categories by viewModel.getAllCategories().collectAsState(initial = emptyList())

    var expanded by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }
    val searchQuery by viewModel.searchQuery.collectAsState() // Collect search query state
    val selectedCategory by viewModel.selectedCategory.collectAsState() // Collect selected category state from ViewModel
    val selectedSubcategory by viewModel.selectedSubcategory.collectAsState() // Collect selected subcategory state

    Column(modifier = Modifier.padding(16.dp)) {
        // General Search Field
        TextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            label = { Text(stringResource(R.string.search_prompts_label)) }, // Use stringResource
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                        Icon(
                            imageVector = Icons.Filled.Clear,
                            contentDescription = stringResource(R.string.clear_search_button_description) // Add a string resource for accessibility
                        )
                    }
                }
            }
        )

        // Category Filter
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            TextField(
                value = selectedCategory ?: "", // Use selectedCategory from ViewModel, handle null
                onValueChange = {
                    // Update searchText for filtering dropdown items, but don't update ViewModel's selectedCategory here
                    searchText = it
                },
                label = { Text(stringResource(R.string.category_label)) }, // Use stringResource
                trailingIcon = {
                    // Show clear icon if a category is selected
                    if (selectedCategory != null) {
                        IconButton(onClick = {
                            viewModel.setSelectedCategory(null) // Clear selected category
                            searchText = "" // Clear searchText as well
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = stringResource(R.string.clear_category_button_description)
                            )
                        }
                    } else {
                        // Otherwise, show the default dropdown icon
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    }
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                // Add "Select All" option
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.all_categories_label)) }, // Use stringResource
                    onClick = {
                        expanded = false
                        viewModel.setSelectedCategory(null) // Set category filter to null in ViewModel
                        searchText = "" // Clear searchText when "Select All" is selected
                    }
                )
                categories.filter {
                    it.contains(searchText, ignoreCase = true)
                }.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category) },
                        onClick = {
                            expanded = false
                            viewModel.setSelectedCategory(category) // Apply filter when item is selected
                            searchText = category // Update searchText to show selected category
                        }
                    )
                }
            }
        }

        // Subcategory Filter (appears when a category is selected)
        val subcategories by viewModel.getAllSubcategories(selectedCategory).collectAsState(initial = emptyList()) // Get subcategories for selected category

        if (selectedCategory != null) { // Only show subcategory filter if a category is selected
            var subcategoryExpanded by remember { mutableStateOf(false) }
            var subcategorySearchText by remember { mutableStateOf("") }

            ExposedDropdownMenuBox(
                expanded = subcategoryExpanded,
                onExpandedChange = { subcategoryExpanded = !subcategoryExpanded }
            ) {
                TextField(
                    value = if (selectedSubcategory == null && selectedCategory != null) stringResource(R.string.all_subcategories_label) else selectedSubcategory ?: "",
                    onValueChange = {
                        // Update subcategorySearchText for filtering dropdown items
                        subcategorySearchText = it
                    },
                    label = { Text(stringResource(R.string.subcategory_label)) },
                    trailingIcon = {
                        // Show clear icon if a subcategory is selected
                        if (selectedSubcategory != null) {
                            IconButton(onClick = {
                                viewModel.setSelectedSubcategory(null) // Clear selected subcategory
                                subcategorySearchText = "" // Clear subcategorySearchText as well
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.Clear,
                                    contentDescription = stringResource(R.string.clear_subcategory_button_description)
                                )
                            }
                        } else {
                            // Otherwise, show the default dropdown icon
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = subcategoryExpanded)
                        }
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = subcategoryExpanded,
                    onDismissRequest = { subcategoryExpanded = false }
                ) {
                    // Add "Select All" option
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.all_subcategories_label)) },
                        onClick = {
                            subcategoryExpanded = false
                            viewModel.setSelectedSubcategory(null) // Set subcategory filter to null in ViewModel
                            subcategorySearchText = "" // Clear subcategorySearchText when "All Subcategories" is selected
                        }
                    )
                    subcategories.filter {
                        it?.contains(subcategorySearchText, ignoreCase = true) == true
                    }.forEach { subcategory ->
                        DropdownMenuItem(
                            text = { Text(subcategory as String) },
                            onClick = {
                                subcategoryExpanded = false
                                viewModel.setSelectedSubcategory(subcategory) // Apply filter when item is selected
                                subcategorySearchText = subcategory // Update subcategorySearchText to show selected subcategory
                            }
                        )
                    }
                }
            }
        }

        val listState = rememberLazyListState()

        LazyColumn(
            state = listState,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            items(
                items = prompts,
                key = { prompt -> prompt.id } // Provide a unique key for each item
            ) { prompt ->
                PromptItem( // Assuming PromptItem Composable exists
                    prompt = prompt,
                    onPromptClick = {
                        android.util.Log.d("PromptMasterDebug", "PromptListScreen: onPromptClick for ID: ${prompt.id}")
                        coroutineScope.launch {
                            viewModel.markPromptAsUsed(prompt.id)
                            onPromptClick(prompt.id)
                        }
                    },
                    onDeleteClick = {
                        android.util.Log.d("PromptMasterDebug", "PromptListScreen: onDeleteClick for ID: ${prompt.id}")
                        coroutineScope.launch {
                            viewModel.delete(prompt)
                        }
                    }, // Assuming delete exists in ViewModel
                    onFavoriteClick = {
                        android.util.Log.d("PromptMasterDebug", "PromptListScreen: onFavoriteClick for ID: ${prompt.id}, current favorite: ${prompt.isFavorite}")
                        coroutineScope.launch {
                            viewModel.toggleFavorite(prompt)
                        }
                    },
                    onDuplicateClick = {
                        android.util.Log.d("PromptMasterDebug", "PromptListScreen: onDuplicateClick for ID: ${prompt.id}")
                        coroutineScope.launch {
                            viewModel.duplicatePrompt(prompt)
                        }
                    }, // Assuming duplicatePrompt exists in ViewModel
                    onCopyDescriptionClick = { description -> // Modify lambda to receive description
                        android.util.Log.d("PromptMasterDebug", "PromptListScreen: onCopyDescriptionClick for ID: ${prompt.id}")
                        onCopyDescriptionClick(prompt.id, description) // Call the lambda with prompt ID and description
                    }
                )
            }

            // Loading indicator at the end of the list
            if (isLoading) {
                item {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .wrapContentSize(Alignment.Center) // Center the indicator
                    )
                }
            }
        }

        // Load more data when the user scrolls to the end
        LaunchedEffect(listState) {
            snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
                .collect { lastVisibleItemIndex ->
                    // Only load next page if there are prompts and we are at the end of the list
                    if (prompts.isNotEmpty() && lastVisibleItemIndex != null && lastVisibleItemIndex >= prompts.size - 1 && !isLoading) {
                        viewModel.loadNextPage()
                    }
                }
        }
    }
}
