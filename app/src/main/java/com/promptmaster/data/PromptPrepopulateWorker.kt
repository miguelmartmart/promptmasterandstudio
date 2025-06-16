package com.promptmaster.data

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import java.io.InputStreamReader
import com.promptmaster.ChildWorkerFactory

@HiltWorker
class PromptPrepopulateWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val promptDao: PromptDao,
    private val gson: Gson,
    private val promptRepository: PromptRepository // Inject PromptRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            applicationContext.assets.open("initial_prompts.json").use { inputStream ->
                val reader = InputStreamReader(inputStream)
                val promptListType = object : TypeToken<List<Prompt>>() {}.type
                val prompts: List<Prompt> = gson.fromJson(reader, promptListType)
                promptDao.insertAll(prompts)
                android.util.Log.d("PromptPrepopulateWorker", "Initial prompts loaded successfully: ${prompts.size} prompts")
            }
            promptRepository.setDatabaseReady() // Signal that the database is ready
            Result.success()
        } catch (e: Exception) {
            android.util.Log.e("PromptPrepopulateWorker", "Error loading initial prompts: ${e.message}", e)
            Result.failure()
        }
    }

    @AssistedFactory
    interface Factory : ChildWorkerFactory {
        override fun create(appContext: Context, workerParameters: WorkerParameters): PromptPrepopulateWorker
    }
}

