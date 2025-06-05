package com.promptmaster.utils

import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import java.util.Locale

fun Context.setLocale(language: String): ContextWrapper {
    val locale = Locale(language)
    Locale.setDefault(locale)

    val configuration = resources.configuration
    configuration.setLocale(locale)

    val context = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        createConfigurationContext(configuration)
    } else {
        @Suppress("DEPRECATION")
        resources.updateConfiguration(configuration, resources.displayMetrics)
        this
    }

    return ContextWrapper(context)
}
