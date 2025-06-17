package com.promptmaster.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.promptmaster.R // Import R
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.TextField
import androidx.compose.material3.Switch
import androidx.compose.material3.ListItem
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext // Import LocalContext
import androidx.compose.foundation.isSystemInDarkTheme
import java.util.Locale
import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import kotlinx.coroutines.launch // Import launch
import androidx.compose.runtime.rememberCoroutineScope // Import rememberCoroutineScope
import com.promptmaster.ui.PromptViewModel
import com.promptmaster.ui.components.ConfirmationDialog // Import ConfirmationDialog
import android.widget.Toast // Import Toast
import androidx.compose.runtime.LaunchedEffect // Import LaunchedEffect
import androidx.activity.compose.rememberLauncherForActivityResult // Import rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts // Import ActivityResultContracts
import android.net.Uri // Import Uri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: PromptViewModel, // Accept ViewModel
    onThemeChange: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope() // Create a coroutine scope
    val sharedPreferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context) // Use default shared preferences
    // Fix for Deprecated Locale
    val currentLanguage = if (context.resources.configuration.locales.isEmpty) {
        Locale.getDefault().language
    } else {
        context.resources.configuration.locales[0].language
    }

    var showResetConfirmationDialog by remember { mutableStateOf(false) } // State for dialog visibility
    var showImportConfirmationDialog by remember { mutableStateOf(false) } // State for import confirmation dialog

    // Import Prompts Option
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch {
                // TODO: Call ViewModel function to import prompts
                viewModel.importPrompts(context, it)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.menu_settings)) }) // Use stringResource
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // Dark Mode Setting
            val initialDarkMode = sharedPreferences.getBoolean("darkModeEnabled", isSystemInDarkTheme())
            var isDarkModeEnabled by remember {
                mutableStateOf(initialDarkMode)
            }

            ListItem(
                headlineContent = { Text(stringResource(R.string.dark_mode_setting)) }, // TODO: Add string resource
                trailingContent = {
                    Switch(
                        checked = isDarkModeEnabled,
                        onCheckedChange = {
                            isDarkModeEnabled = it
                            sharedPreferences.edit().putBoolean("darkModeEnabled", it).apply()
                            onThemeChange() // Call the callback to trigger theme change
                        }
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Language Selection
            val languages = listOf("en", "es", "fr", "de", "pt", "it") // Add "it" for Italian
            var expanded by remember { mutableStateOf(false) }
            var selectedLanguage by remember { mutableStateOf(sharedPreferences.getString("appLanguage", currentLanguage) ?: currentLanguage) }

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                TextField(
                    value = getLanguageDisplayName(selectedLanguage), // Display full language name
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.language_setting)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    languages.forEach { language ->
                        DropdownMenuItem(
                            text = { Text(getLanguageDisplayName(language)) }, // Display full language name
                            onClick = {
                                selectedLanguage = language
                                expanded = false
                                sharedPreferences.edit().putString("appLanguage", language).apply()
                                // Recreate the activity to apply the language change
                                (context as? Activity)?.recreate()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Reset Application Option
            Button(
                onClick = { showResetConfirmationDialog = true }, // Show dialog on click
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.reset_application_button)) // Use string resource
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Export Prompts Option
            Button(
                onClick = {
                    coroutineScope.launch {
                        val fileUri = viewModel.exportPrompts(context)
                        fileUri?.let { uri ->
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/json"
                                putExtra(Intent.EXTRA_SUBJECT, "PromptMaster Prompts Backup")
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Prompts Backup"))
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.export_prompts_button))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Import Prompts Option
            Button(
                onClick = { showImportConfirmationDialog = true }, // Show confirmation dialog on click
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.import_prompts_button))
            }

            // TODO: Add theme selection, text size, backup/restore options
        }
    }

    // Reset Confirmation Dialog
    ConfirmationDialog(
        showDialog = showResetConfirmationDialog,
        title = stringResource(id = R.string.reset_app_dialog_title),
        text = stringResource(id = R.string.reset_app_dialog_message),
        confirmButtonText = stringResource(id = R.string.dialog_confirm),
        cancelButtonText = stringResource(id = R.string.dialog_cancel),
        onConfirm = {
            coroutineScope.launch {
                viewModel.resetApplicationData() // Call the reset function
                // Reset SharedPreferences
                sharedPreferences.edit().apply {
                    remove("search_history") // Clear search history
                    remove("appLanguage") // Reset language to default
                    remove("darkModeEnabled") // Reset dark mode to default
                    apply()
                }
                // Recreate activity to apply language and theme changes
                (context as? Activity)?.recreate()
            }
            showResetConfirmationDialog = false // Hide dialog after confirming
        },
        onCancel = { showResetConfirmationDialog = false } // Hide dialog on cancel
    )

    // Import Confirmation Dialog
    if (showImportConfirmationDialog) {
        ConfirmationDialog(
            showDialog = showImportConfirmationDialog,
            title = stringResource(id = R.string.import_prompts_dialog_title),
            text = stringResource(id = R.string.import_prompts_dialog_message),
            confirmButtonText = stringResource(id = R.string.dialog_confirm),
            cancelButtonText = stringResource(id = R.string.dialog_cancel),
            onConfirm = {
                importLauncher.launch("application/json") // Launch file picker for JSON files
                showImportConfirmationDialog = false // Hide dialog after confirming
            },
            onCancel = { showImportConfirmationDialog = false } // Hide dialog on cancel
        )
    }

    // Observe status and show Toast
    LaunchedEffect(viewModel.status) {
        viewModel.status.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
private fun getLanguageDisplayName(languageCode: String): String {
    return when (languageCode) {
        "en" -> stringResource(R.string.language_english)
        "es" -> stringResource(R.string.language_spanish)
        "fr" -> stringResource(R.string.language_french)
        "de" -> stringResource(R.string.language_german)
        "pt" -> stringResource(R.string.language_portuguese)
        "it" -> stringResource(R.string.language_italian)
        else -> languageCode // Fallback to code if name not found
    }
}

// TODO: Add Preview
