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

import com.promptmaster.R
import com.promptmaster.data.Prompt

import com.promptmaster.ui.PromptViewModel
import com.promptmaster.ui.PromptOperationsViewModel // New import
import androidx.activity.compose.BackHandler // Import BackHandler
import com.promptmaster.ui.components.ConfirmationDialog // Import ConfirmationDialog
import kotlinx.coroutines.launch // Import launch
import androidx.compose.runtime.saveable.rememberSaveable // Import rememberSaveable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPromptScreen(
    promptId: Int,
    promptViewModel: PromptOperationsViewModel, // Accept generic interface
    onBack: () -> Unit
) {
    // Log the received promptId
    android.util.Log.d("EditPromptScreen", "Received promptId: $promptId")

    val prompt by promptViewModel.getPrompt(promptId)
        .collectAsState(initial = null) // Use getPrompt

    // Log the collected prompt object
    LaunchedEffect(prompt) {
        android.util.Log.d("EditPromptScreen", "Collected prompt: $prompt")
    }

    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("") }
    var subcategory by rememberSaveable { mutableStateOf("") }
    var tags by rememberSaveable { mutableStateOf("") }
    var recommendedModel by rememberSaveable { mutableStateOf("") }
    var customizableFields by rememberSaveable { mutableStateOf("") }
    var imagePath by rememberSaveable { mutableStateOf("") }
    var videoPath by rememberSaveable { mutableStateOf("") }
    var isFavorite by rememberSaveable { mutableStateOf(false) }

    var showDiscardConfirmationDialog by remember { mutableStateOf(false) }

    LaunchedEffect(prompt) {
        prompt?.let {
            title = it.title
            description = it.description ?: "" // Handle null description
            category = it.category
            subcategory = it.subcategory ?: ""
            tags = it.tags.joinToString(",")
            customizableFields = it.customizableFields?.toString() ?: ""
            recommendedModel = it.recommendedModel ?: ""
            imagePath = it.imagePath ?: ""
            videoPath = it.videoPath ?: ""
            isFavorite = it.isFavorite
        }
    }

    val hasUnsavedChanges = remember(
        prompt,
        title,
        description,
        category,
        subcategory,
        tags,
        recommendedModel,
        customizableFields,
        imagePath,
        videoPath,
        isFavorite
    ) {
        prompt?.let {
            it.title != title ||
                    it.description != description ||
                    it.category != category ||
                    it.subcategory != subcategory ||
                    it.tags.joinToString(",") != tags ||
                    it.recommendedModel != recommendedModel ||
                    it.customizableFields?.toString() != customizableFields ||
                    it.imagePath != imagePath ||
                    it.videoPath != videoPath ||
                    it.isFavorite != isFavorite
        }
            ?: (title.isNotEmpty() || description.isNotEmpty() || category.isNotEmpty() || subcategory.isNotEmpty() || tags.isNotEmpty() || recommendedModel.isNotEmpty() || customizableFields.isNotEmpty() || imagePath.isNotEmpty() || videoPath.isNotEmpty() || isFavorite) // Consider changes for new prompts
    }

    BackHandler(enabled = hasUnsavedChanges) {
        showDiscardConfirmationDialog = true
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.edit_prompt)) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (hasUnsavedChanges) {
                            showDiscardConfirmationDialog = true
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_button_content_description)
                        )
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
                onValueChange = { title = it }, // Simplified onValueChange
                label = { Text(stringResource(R.string.title_label)) },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    if (title.isNotEmpty()) {
                        IconButton(onClick = { title = "" }) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = stringResource(R.string.clear_text_button_description)
                            )
                        }
                    }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it }, // Simplified onValueChange
                label = { Text(stringResource(R.string.description_label)) },
                modifier = Modifier
                    .fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = category,
                onValueChange = { category = it }, // Simplified onValueChange
                label = { Text(stringResource(R.string.category_label)) },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    if (category.isNotEmpty()) {
                        IconButton(onClick = { category = "" }) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = stringResource(R.string.clear_text_button_description)
                            )
                        }
                    }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = subcategory,
                onValueChange = { subcategory = it }, // Simplified onValueChange
                label = { Text(stringResource(R.string.subcategory_label)) },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    if (subcategory.isNotEmpty()) {
                        IconButton(onClick = { subcategory = "" }) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = stringResource(R.string.clear_text_button_description)
                            )
                        }
                    }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = tags,
                onValueChange = { tags = it }, // Simplified onValueChange
                label = { Text(stringResource(R.string.tags_label)) },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    if (tags.isNotEmpty()) {
                        IconButton(onClick = { tags = "" }) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = stringResource(R.string.clear_text_button_description)
                            )
                        }
                    }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = recommendedModel,
                onValueChange = { recommendedModel = it }, // Simplified onValueChange
                label = { Text(stringResource(R.string.recommended_model_label)) },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    if (recommendedModel.isNotEmpty()) {
                        IconButton(onClick = { recommendedModel = "" }) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = stringResource(R.string.clear_text_button_description)
                            )
                        }
                    }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = customizableFields,
                onValueChange = { customizableFields = it }, // Simplified onValueChange
                label = { Text(stringResource(R.string.customizable_fields_label)) },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    if (customizableFields.isNotEmpty()) {
                        IconButton(onClick = { customizableFields = "" }) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = stringResource(R.string.clear_text_button_description)
                            )
                        }
                    }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            // TODO: Add fields for image and video paths
            // TODO: Add checkbox for isFavorite

            Spacer(modifier = Modifier.height(16.dp))


            val emptyFieldsMessage = stringResource(R.string.empty_fields_error)
            Button(
                onClick = {
                    if (title.isBlank() || description.isBlank() || category.isBlank()) {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                message = emptyFieldsMessage,
                                duration = SnackbarDuration.Short
                            )
                        }
                        return@Button
                    }

                    val promptToSave = Prompt(
                        id = promptId,
                        title = title,
                        description = description,
                        category = category,
                        subcategory = subcategory.ifEmpty { null },
                        tags = tags.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                        recommendedModel = recommendedModel.ifEmpty { null },
                        customizableFields = emptyMap(), // Pass emptyMap()
                        imagePath = imagePath.ifEmpty { null },
                        videoPath = videoPath.ifEmpty { null },
                        isFavorite = isFavorite
                    )
                    coroutineScope.launch {
                        if (promptId == 0) {
                            promptViewModel.insert(promptToSave)
                        } else {
                            promptViewModel.update(promptToSave)
                        }
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = hasUnsavedChanges
            ) {
                Text(stringResource(if (promptId == 0) R.string.save_prompt_button else R.string.update_prompt_button))
            }
        }
        // Confirmation Dialog
        if (showDiscardConfirmationDialog) {
            ConfirmationDialog(
                showDialog = showDiscardConfirmationDialog,
                title = stringResource(id = R.string.edit_prompt_dialog_title),
                text = stringResource(id = R.string.edit_prompt_dialog_message),
                confirmButtonText = stringResource(id = R.string.dialog_confirm),
                cancelButtonText = stringResource(id = R.string.dialog_cancel),
                onConfirm = {
                    showDiscardConfirmationDialog = false
                    onBack()
                },
                onCancel = { showDiscardConfirmationDialog = false }
            )
        }
    }
}

// TODO: Add Preview
