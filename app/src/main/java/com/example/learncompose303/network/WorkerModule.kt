package com.example.learncompose303.network

import androidx.hilt.work.WorkerAssistedFactory
import androidx.work.ListenableWorker
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface WorkerModule {
    @Binds
    fun bindSyncInvoiceWorkerFactory(factory: SyncInvoiceWorker.Factory): WorkerAssistedFactory<out ListenableWorker>

    @Binds
    fun bindSyncProductWorkerFactory(factory: SyncProductWorker.Factory): WorkerAssistedFactory<out ListenableWorker>

    @Binds
    fun bindSyncInvoiceLineWorkerFactory(factory: SyncInvoiceLineWorker.Factory): WorkerAssistedFactory<out ListenableWorker>
}
