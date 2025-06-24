package com.promptmaster.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.util.DisplayMetrics
import java.util.Locale
import android.util.Log

/*
// This function applies the selected language globally to the application by updating the resources configuration.
fun aplicarIdioma(context: Context, codigoIdioma: String) {
    Log.d("ContextUtils", "Attempting to apply language: $codigoIdioma to context: $context")
    val locale = Locale(codigoIdioma)
    Locale.setDefault(locale)

    val resources = context.resources
    val configuration = resources.configuration
    val displayMetrics = resources.displayMetrics

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
        configuration.setLocale(locale)
    } else {
        @Suppress("DEPRECATION")
        configuration.locale = locale
    }

    @Suppress("DEPRECATION")
    resources.updateConfiguration(configuration, displayMetrics)
    Log.d("ContextUtils", "Language applied to context resources. Current locale: ${resources.configuration.locale.language}")
}
*/
