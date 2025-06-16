package com.promptmaster

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import dagger.Reusable
import javax.inject.Inject
import javax.inject.Provider

@Reusable
class PromptMasterHiltWorkerFactory @Inject constructor(
    private val workerFactories: Map<Class<out ListenableWorker>, @JvmSuppressWildcards Provider<ChildWorkerFactory>>
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker { // Changed return type to non-nullable
        android.util.Log.d("PromptMasterHiltWorkerFactory", "createWorker called for: $workerClassName")
        val foundEntry = workerFactories.entries.find {
            Class.forName(workerClassName).isAssignableFrom(it.key)
        }
        val factoryProvider = foundEntry?.value
            ?: run {
                android.util.Log.e("PromptMasterHiltWorkerFactory", "Unknown worker class name: $workerClassName")
                throw IllegalArgumentException("unknown worker class name: $workerClassName")
            }
        android.util.Log.d("PromptMasterHiltWorkerFactory", "Found factory for: ${foundEntry.key.simpleName}")
        return factoryProvider.get().create(appContext, workerParameters)
    }
}

interface ChildWorkerFactory {
    fun create(appContext: Context, workerParameters: WorkerParameters): ListenableWorker
}
