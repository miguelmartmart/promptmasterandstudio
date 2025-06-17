package com.promptmaster.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.promptmaster.data.Prompt
import com.promptmaster.data.PromptDao
import com.promptmaster.data.PromptRepository
import com.promptmaster.data.PromptRoomDatabase
import com.promptmaster.data.PromptBackupManager
import com.promptmaster.data.PromptDataOperations
import com.promptmaster.utils.PromptUtils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import java.io.InputStreamReader
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun providePromptRoomDatabase(
        @ApplicationContext context: Context
    ): PromptRoomDatabase {
        return Room.databaseBuilder(
            context,
            PromptRoomDatabase::class.java,
            "prompt_database"
        ).build()
    }

    @Singleton
    @Provides
    fun providePromptDao(database: PromptRoomDatabase): PromptDao {
        return database.promptDao()
    }

    @Singleton
    @Provides
    fun providePromptRepository(
        promptDao: PromptDao,
        application: Application
    ): PromptRepository {
        return PromptRepository(promptDao, application)
    }

    @Singleton
    @Provides
    fun providePromptBackupManager(
        promptDao: PromptDao,
        @ApplicationContext context: Context
    ): PromptBackupManager {
        return PromptBackupManager(promptDao, context)
    }

    @Singleton
    @Provides
    fun providePromptUtils(
        @ApplicationContext context: Context,
        repository: PromptRepository,
        promptBackupManager: PromptBackupManager
    ): PromptUtils {
        return PromptUtils(context, repository, promptBackupManager)
    }

    @Singleton
    @Provides
    fun providePromptDataOperations(
        repository: PromptRepository,
        promptBackupManager: PromptBackupManager
    ): PromptDataOperations {
        return PromptDataOperations(repository, promptBackupManager)
    }

    @Singleton
    @Provides
    fun provideGson(): Gson {
        return Gson()
    }

    @Singleton
    @Provides
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .build()
    }
}
