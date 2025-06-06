package com.promptmaster.ui.editprompt

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.promptmaster.R
import com.promptmaster.data.Prompt
import com.promptmaster.ui.PromptViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPromptScreen(
    navController: NavController,
    promptId: Int? = null, // Null for adding, ID for editing
    viewModel: PromptViewModel // Accept ViewModel as parameter
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") } // Comma separated for now
    var recommendedModel by remember { mutableStateOf("") }
    var customizableFields by remember { mutableStateOf("") } // JSON string or similar for now
    var imagePath by remember { mutableStateOf<String?>(null) }
    var videoPath by remember { mutableStateOf<String?>(null) }
    var isFavorite by remember { mutableStateOf(false) }

    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Fetch prompt data if editing
    LaunchedEffect(promptId) {
        if (promptId != null && promptId != 0) {
            viewModel.getPrompt(promptId).collect { prompt ->
                if (prompt != null) {
                    title = prompt.title
                    description = prompt.description
                    category = prompt.category
                    tags = prompt.tags.joinToString(",") // Convert list to comma separated string
                    recommendedModel = prompt.recommendedModel
                    // TODO: Convert map to string for customizableFields
                    imagePath = prompt.imagePath
                    videoPath = prompt.videoPath
                    isFavorite = prompt.isFavorite
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(if (promptId == null || promptId == 0) R.string.add_new_prompt else R.string.edit_prompt)) }, // Use stringResource
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back_button_content_description)) // TODO: Add back_button_content_description string resource
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.title_label)) }, // Use stringResource
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    if (title.isNotEmpty()) {
                        IconButton(onClick = { title = "" }) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = stringResource(R.string.clear_text_button_description) // TODO: Add string resource
                            )
                        }
                    }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(stringResource(R.string.description_label)) }, // Use stringResource
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    if (description.isNotEmpty()) {
                        IconButton(onClick = { description = "" }) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = stringResource(R.string.clear_text_button_description) // TODO: Add string resource
                            )
                        }
                    }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text(stringResource(R.string.category_label)) }, // Use stringResource
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    if (category.isNotEmpty()) {
                        IconButton(onClick = { category = "" }) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = stringResource(R.string.clear_text_button_description) // TODO: Add string resource
                            )
                        }
                    }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = tags,
                onValueChange = { tags = it },
                label = { Text(stringResource(R.string.tags_label)) }, // Use stringResource
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    if (tags.isNotEmpty()) {
                        IconButton(onClick = { tags = "" }) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = stringResource(R.string.clear_text_button_description) // TODO: Add string resource
                            )
                        }
                    }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = recommendedModel,
                onValueChange = { recommendedModel = it },
                label = { Text(stringResource(R.string.recommended_model_label)) }, // Use stringResource
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    if (recommendedModel.isNotEmpty()) {
                        IconButton(onClick = { recommendedModel = "" }) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = stringResource(R.string.clear_text_button_description) // TODO: Add string resource
                            )
                        }
                    }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = customizableFields,
                onValueChange = { customizableFields = it },
                label = { Text(stringResource(R.string.customizable_fields_label)) }, // Use stringResource
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    if (customizableFields.isNotEmpty()) {
                        IconButton(onClick = { customizableFields = "" }) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = stringResource(R.string.clear_text_button_description) // TODO: Add string resource
                            )
                        }
                    }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            // TODO: Add fields for image and video paths
            // TODO: Add checkbox for isFavorite

            Spacer(modifier = Modifier.weight(1f)) // Push button to bottom

            Button(
                onClick = {
                    if (title.isBlank() || description.isBlank() || category.isBlank()) {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Title, Description, and Category cannot be empty.", // TODO: Add string resource
                                duration = SnackbarDuration.Short
                            )
                        }
                        return@Button
                    }

                    val prompt = Prompt(
                        id = promptId ?: 0, // Use 0 for new prompt, actual ID for editing
                        title = title,
                        description = description,
                        category = category,
                        tags = tags.split(",").map { it.trim() }, // Convert comma separated string to list
                        recommendedModel = recommendedModel,
                        customizableFields = emptyMap(), // TODO: Convert string to map
                        imagePath = imagePath,
                        videoPath = videoPath,
                        isFavorite = isFavorite
                    )
                    if (promptId == null || promptId == 0) {
                        viewModel.insert(prompt)
                    } else {
                        viewModel.update(prompt)
                    }
                    navController.popBackStack() // Go back after saving
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(if (promptId == null || promptId == 0) R.string.save_prompt_button else R.string.update_prompt_button)) // Use stringResource
            }
        }
    }
}

// TODO: Add Preview
