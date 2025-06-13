package com.promptmaster

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.promptmaster.data.PromptPrepopulateWorker
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class PromptMasterApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        val prepopulateRequest = OneTimeWorkRequest.Builder(PromptPrepopulateWorker::class.java)
            .build()

        WorkManager.getInstance(this).enqueueUniqueWork(
            "PromptPrepopulateWork",
            ExistingWorkPolicy.KEEP,
            prepopulateRequest
        )
    }
}
