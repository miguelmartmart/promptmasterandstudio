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
import android.content.ContextWrapper
import java.util.Locale
import androidx.preference.PreferenceManager
import com.promptmaster.utils.setLocale // Import the setLocale extension function
import com.google.android.gms.ads.MobileAds // Import MobileAds
import javax.inject.Inject

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

    override fun attachBaseContext(base: Context?) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(base!!)
        val language = preferences.getString("appLanguage", Locale.getDefault().language) ?: "en"
        val context = base.setLocale(language)
        super.attachBaseContext(context)
    }

    override fun onCreate() {
        super.onCreate()
        // Initialize the Mobile Ads SDK.
        MobileAds.initialize(this) {}
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
