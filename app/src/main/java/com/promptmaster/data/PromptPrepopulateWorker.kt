package com.promptmaster.data

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.InputStreamReader

@HiltWorker
class PromptPrepopulateWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val promptDao: PromptDao,
    private val gson: Gson
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
            Result.success()
        } catch (e: Exception) {
            android.util.Log.e("PromptPrepopulateWorker", "Error loading initial prompts: ${e.message}", e)
            Result.failure()
        }
    }
}
