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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onThemeChange: () -> Unit) { // Add the callback parameter
    val context = LocalContext.current
    val sharedPreferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context) // Use default shared preferences
    // Fix for Deprecated Locale
    val currentLanguage = if (context.resources.configuration.locales.isEmpty) {
        Locale.getDefault().language
    } else {
        context.resources.configuration.locales[0].language
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
            val languages = listOf("en", "es", "fr", "de", "pt") // Use language codes
            var expanded by remember { mutableStateOf(false) }
            var selectedLanguage by remember { mutableStateOf(sharedPreferences.getString("appLanguage", currentLanguage) ?: currentLanguage) }

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                TextField(
                    value = selectedLanguage,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Language") }, // TODO: Add string resource for "Language"
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    languages.forEach { language ->
                        DropdownMenuItem(
                            text = { Text(language) }, // TODO: Use full language names from string resources
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

            // TODO: Add theme selection, text size, backup/restore, export options
        }
    }
}

// TODO: Add Preview
