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
import androidx.compose.material3.Switch
import androidx.compose.material3.ListItem
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext // Import LocalContext
import androidx.compose.foundation.isSystemInDarkTheme
import java.util.Locale
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
import java.io.OutputStreamWriter
import androidx.compose.foundation.rememberScrollState // Import rememberScrollState
import androidx.compose.foundation.verticalScroll // Import verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: PromptViewModel, // Accept ViewModel
    onThemeChange: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope() // Create a coroutine scope
    val sharedPreferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context) // Use default shared preferences
    // Get current language from preferences, fallback to system or English
    val currentLanguage = sharedPreferences.getString("idioma_establecido", Locale.getDefault().language) ?: "en"

    var showResetConfirmationDialog by remember { mutableStateOf(false) } // State for dialog visibility
    var showImportConfirmationDialog by remember { mutableStateOf(false) } // State for import confirmation dialog

    // Launcher for importing prompts (Open Document)
    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch {
                viewModel.importPrompts(context, it)
            }
        }
    }
    var showDeleteAllPromptsConfirmationDialog by remember { mutableStateOf(false) } // State for delete all prompts dialog visibility

    // Launcher for exporting prompts (Create Document)
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json") // MIME type for JSON
    ) { uri: Uri? ->
        uri?.let { outputUri ->
            coroutineScope.launch {
                val jsonString = viewModel.exportPrompts(context) // Get JSON string from ViewModel
                if (jsonString != null) {
                    try {
                        context.contentResolver.openOutputStream(outputUri)?.use { outputStream ->
                            OutputStreamWriter(outputStream).use { writer ->
                                writer.write(jsonString)
                            }
                        }
                        Toast.makeText(context, "Prompts exported successfully!", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error exporting prompts: ${e.message}", Toast.LENGTH_LONG).show()
                        e.printStackTrace()
                    }
                } else {
                    Toast.makeText(context, "No prompts data to export.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.menu_settings)) }) // Use stringResource
        }
    ) { paddingValues ->
        val scrollState = rememberScrollState() // Create a scroll state

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(scrollState) // Apply vertical scroll modifier
        ) {
            // General Settings Group
            Text(
                text = stringResource(R.string.settings_group_general), // "Generales"
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            HorizontalDivider(color = Color.LightGray, thickness = 1.dp, modifier = Modifier.padding(bottom = 16.dp))

            // Dark Mode Setting
            val initialDarkMode = sharedPreferences.getBoolean("darkModeEnabled", isSystemInDarkTheme())
            var isDarkModeEnabled by remember {
                mutableStateOf(initialDarkMode)
            }

            ListItem(
                headlineContent = { Text(stringResource(R.string.dark_mode_setting)) },
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

            // Share Prompt Option (New) - Moved to General
            Button(
                onClick = {
                    coroutineScope.launch {
                        val allPrompts = viewModel.getAllPromptsForSharing()
                        val shareText = allPrompts.joinToString(separator = "\n\n---\n\n") { prompt ->
                            "Título: ${prompt.title}\nDescripción: ${prompt.description}\nCategoría: ${prompt.category ?: "N/A"}\nSubcategoría: ${prompt.subcategory ?: "N/A"}\nModelo Recomendado: ${prompt.recommendedModel ?: "N/A"}\nEtiquetas: ${prompt.tags ?: "N/A"}"
                        }
                        val shareTitle = context.getString(R.string.share_all_prompts_title)
                        viewModel.shareAllPrompts(context, shareTitle, shareText)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.share_all_prompts_button))
            }

            Spacer(modifier = Modifier.height(32.dp)) // Increased space before next group


            // Advanced Settings Group
            Text(
                text = stringResource(R.string.settings_group_advanced), // "Avanzados"
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            HorizontalDivider(color = Color.LightGray, thickness = 1.dp, modifier = Modifier.padding(bottom = 16.dp))

            // Language Selection (Commented out to hide from UI, but functionality remains)
            /*
            val languages = listOf("en", "es", "fr", "de", "it", "pt", "ca", "eu", "gl", "es-rES") // Added new supported languages
            var expanded by remember { mutableStateOf(false) }
            var selectedLanguage by remember { mutableStateOf(currentLanguage) }

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth() // Make dropdown fill width
            ) {
                TextField(
                    value = getLanguageDisplayName(selectedLanguage), // Display full language name
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.language_setting)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth() // Make TextField fill width
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth() // Make dropdown menu fill width
                ) {
                    languages.forEach { language ->
                        DropdownMenuItem(
                            text = { Text(getLanguageDisplayName(language)) }, // Display full language name
                            onClick = {
                                selectedLanguage = language
                                expanded = false
                                sharedPreferences.edit().putString("idioma_establecido", language).apply()
                                // aplicarIdioma(context, language) // Apply the new language - This function is commented out in the file
                                // No need to restart activity, AppCompatDelegate.setApplicationLocales() handles it.
                            },
                            modifier = Modifier.fillMaxWidth() // Make dropdown item fill width
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            */

            // Export Prompts Option
            Button(
                onClick = {
                    // Launch the document creation intent with a suggested filename
                    createDocumentLauncher.launch("prompts_backup_${System.currentTimeMillis()}.json")
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

            Spacer(modifier = Modifier.height(16.dp))

            // Delete All Prompts Option
            Button(
                onClick = { showDeleteAllPromptsConfirmationDialog = true }, // Show confirmation dialog on click
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red) // Make button red
            ) {
                Text(stringResource(R.string.delete_all_prompts_button))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Reset Application Option (in red)
            Button(
                onClick = { showResetConfirmationDialog = true }, // Show dialog on click
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red) // Make button red
            ) {
                Text(stringResource(R.string.reset_application_button))
            }
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
                    remove("idioma_establecido") // Reset language to default
                    remove("darkModeEnabled") // Reset dark mode to default
                    apply()
                }
                // Reapply system language or default English (language logic commented out)
                /*
                val idiomaSistema = Locale.getDefault().language
                val idiomasDisponibles = listOf("es", "en", "fr", "de", "it", "pt") // Added pt
                val idiomaFinal = if (idiomasDisponibles.contains(idiomaSistema)) {
                    idiomaSistema
                } else {
                    "en"
                }
                aplicarIdioma(context, idiomaFinal)
                */
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
                openDocumentLauncher.launch(arrayOf("application/json")) // Launch file picker for JSON files
                showImportConfirmationDialog = false // Hide dialog after confirming
            },
        onCancel = { showImportConfirmationDialog = false } // Hide dialog on cancel
    )
    }

    // Delete All Prompts Confirmation Dialog
    if (showDeleteAllPromptsConfirmationDialog) {
        ConfirmationDialog(
            showDialog = showDeleteAllPromptsConfirmationDialog,
            title = stringResource(id = R.string.delete_all_prompts_dialog_title), // TODO: Add string resource
            text = stringResource(id = R.string.delete_all_prompts_dialog_message), // TODO: Add string resource
            confirmButtonText = stringResource(id = R.string.dialog_confirm),
            cancelButtonText = stringResource(id = R.string.dialog_cancel),
            onConfirm = {
                coroutineScope.launch {
                    viewModel.deleteAllPrompts() // Call the delete all prompts function
                    Toast.makeText(context, "All prompts deleted!", Toast.LENGTH_SHORT).show() // TODO: Add string resource
                }
                showDeleteAllPromptsConfirmationDialog = false // Hide dialog after confirming
            },
            onCancel = { showDeleteAllPromptsConfirmationDialog = false } // Hide dialog on cancel
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
        "ca" -> stringResource(R.string.language_catalan)
        "eu" -> stringResource(R.string.language_basque)
        "gl" -> stringResource(R.string.language_galician)
        "es-rES" -> stringResource(R.string.language_spanish_spain)
        else -> languageCode // Fallback to code if name not found
    }
}

// TODO: Add Preview
