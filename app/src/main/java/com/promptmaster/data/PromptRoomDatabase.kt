package com.promptmaster.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.InputStreamReader
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import android.util.Log // Import Log

private val APPLICATION_SCOPE = CoroutineScope(Dispatchers.IO)

@Database(entities = [Prompt::class], version = 6, exportSchema = false)
@TypeConverters(Converters::class)
abstract class PromptRoomDatabase : RoomDatabase() {

    abstract fun promptDao(): PromptDao

    companion object {
        @Volatile
        private var INSTANCE: PromptRoomDatabase? = null

        fun getDatabase(
            context: Context,
            promptBackupManager: PromptBackupManager, // New parameter for injected PromptBackupManager
            onDatabasePopulated: () -> Unit
        ): PromptRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PromptRoomDatabase::class.java,
                    "prompt_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(PromptDatabaseCallback(context.applicationContext, promptBackupManager, onDatabasePopulated)) // Pass application context, promptBackupManager, and callback
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class PromptDatabaseCallback(
        private val applicationContext: Context, // Accept application context
        private val promptBackupManager: PromptBackupManager, // Injected PromptBackupManager
        private val onDatabasePopulated: () -> Unit // Callback to signal database population
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            android.util.Log.d("PromptDatabaseCallback", "Database onCreate called")
            APPLICATION_SCOPE.launch {
                // Access the DAO from the already built database instance
                // INSTANCE is guaranteed to be non-null here because onCreate is called after build()
                val promptDao = INSTANCE!!.promptDao()
                populateDatabase(promptDao)
            }
        }

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            android.util.Log.d("PromptDatabaseCallback", "Database onOpen called")
            APPLICATION_SCOPE.launch {
                promptBackupManager.restoreBackup() // Restore backup on open using injected manager
                onDatabasePopulated() // Invoke the callback after restoring backup
            }
        }

        suspend fun populateDatabase(promptDao: PromptDao) {
            try {
                val inputStream: InputStream = applicationContext.assets.open("initial_prompts.json")
                val reader = InputStreamReader(inputStream)
                val promptListType = object : TypeToken<List<Prompt>>() {}.type
                val prompts: List<Prompt> = Gson().fromJson(reader, promptListType)

                prompts.forEach { prompt ->
                    promptDao.insert(prompt)
                }
                Log.d("PromptDatabaseCallback", "Prompts preloaded successfully")
                onDatabasePopulated() // Invoke the callback
            } catch (e: Exception) {
                Log.e("PromptDatabaseCallback", "Error preloading prompts", e)
            }
        }
    }
}
