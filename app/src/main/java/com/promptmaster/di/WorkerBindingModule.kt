package com.promptmaster.di

import androidx.work.WorkerFactory
import com.promptmaster.ChildWorkerFactory
import com.promptmaster.PromptMasterHiltWorkerFactory
import com.promptmaster.data.PromptPrepopulateWorker
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.ClassKey

@InstallIn(SingletonComponent::class)
@Module
abstract class WorkerBindingModule {
    @Binds
    abstract fun bindWorkerFactory(
        factory: PromptMasterHiltWorkerFactory
    ): WorkerFactory

    @Binds
    @IntoMap
    @ClassKey(PromptPrepopulateWorker::class)
    abstract fun bindPromptPrepopulateWorkerFactory(factory: PromptPrepopulateWorker.Factory): ChildWorkerFactory
}
