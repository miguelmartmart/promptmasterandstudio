package com.promptmaster.utils

import android.content.SharedPreferences
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext

@Composable
fun rememberBooleanPreference(
    key: String,
    defaultValue: Boolean,
    preferences: SharedPreferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(LocalContext.current)
): State<Boolean> {
    val context = LocalContext.current
    val state = remember { mutableStateOf(preferences.getBoolean(key, defaultValue)) }

    DisposableEffect(preferences, key) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, changedKey ->
            if (changedKey == key) {
                state.value = sharedPreferences.getBoolean(key, defaultValue)
            }
        }
        preferences.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            preferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    return state
}

fun SharedPreferences.Editor.putStringList(key: String, list: List<String>) {
    putStringSet(key, list.toSet())
}

fun SharedPreferences.getStringList(key: String, defaultValue: List<String> = emptyList()): List<String> {
    return getStringSet(key, defaultValue.toSet())?.toList() ?: defaultValue
}
