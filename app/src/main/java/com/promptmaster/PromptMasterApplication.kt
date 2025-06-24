package com.promptmaster

import android.app.Application
import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.promptmaster.data.PromptPrepopulateWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.Locale
import androidx.preference.PreferenceManager
import com.google.android.gms.ads.MobileAds // Import MobileAds
import javax.inject.Inject
// import androidx.appcompat.app.AppCompatDelegate // Commented out for language change
// import androidx.core.os.LocaleListCompat // Commented out for language change
// import android.util.Log // Commented out for language change

@HiltAndroidApp
class PromptMasterApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() {
            android.util.Log.d("PromptMasterApplication", "Providing WorkManager configuration with HiltWorkerFactory.")
            return Configuration.Builder()
                .setWorkerFactory(workerFactory)
                .build()
        }

    override fun onCreate() {
        super.onCreate()

        // Initialize the Mobile Ads SDK.
        MobileAds.initialize(this) {}

        // Language change logic commented out as per user request.
        /*
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val idiomaGuardado = prefs.getString("idioma_establecido", null)

        if (idiomaGuardado == null) {
            val idiomaSistema = Locale.getDefault().language
            val idiomasDisponibles = listOf("es", "en", "fr", "de", "it", "pt") // los que tú soportes

            val idiomaFinal = if (idiomasDisponibles.contains(idiomaSistema)) {
                idiomaSistema
            } else {
                "en"
            }

            // Guardamos y aplicamos por primera vez
            prefs.edit().putString("idioma_establecido", idiomaFinal).apply()
            val localeList = LocaleListCompat.forLanguageTags(idiomaFinal)
            AppCompatDelegate.setApplicationLocales(localeList)
            Log.d("PromptMasterApplication", "Initial language set to: $idiomaFinal")
        } else {
            // Aplicar el idioma guardado
            val localeList = LocaleListCompat.forLanguageTags(idiomaGuardado)
            AppCompatDelegate.setApplicationLocales(localeList)
            Log.d("PromptMasterApplication", "Saved language applied: $idiomaGuardado")
        }
        */

        android.util.Log.d("PromptMasterApplication", "Application onCreate called. Enqueuing PromptPrepopulateWorker.")
        val prepopulateRequest = OneTimeWorkRequest.Builder(PromptPrepopulateWorker::class.java)
            .build()

        WorkManager.getInstance(this).enqueueUniqueWork(
            "PromptPrepopulateWork",
            ExistingWorkPolicy.KEEP,
            prepopulateRequest
        )
    }

}
