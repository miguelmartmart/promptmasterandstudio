package com.promptmaster.utils

import android.content.Context
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
