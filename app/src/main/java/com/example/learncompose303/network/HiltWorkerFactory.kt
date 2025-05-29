package com.example.learncompose303.network

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.hilt.work.WorkerAssistedFactory
import androidx.work.ListenableWorker
import androidx.work.Worker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.example.learncompose303.data.data_source.InvoiceDao
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import javax.inject.Qualifier
import kotlin.reflect.KClass
import dagger.MapKey
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap

import dagger.assisted.AssistedFactory
import kotlin.jvm.JvmSuppressWildcards

@Module
@InstallIn(SingletonComponent::class)
interface WorkerModule {
    @Binds
    fun bindSyncInvoiceWorkerFactory(factory: SyncInvoiceWorker.Factory): WorkerAssistedFactory<out ListenableWorker>
}

@Singleton
class HiltWorkerFactory @Inject constructor(
    private val workerFactories: Map<String, @JvmSuppressWildcards Provider<WorkerAssistedFactory<*>>>
) : WorkerFactory() {
    @SuppressLint("RestrictedApi")
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        val factoryProvider = workerFactories[workerClassName]
            ?: throw IllegalArgumentException("Unknown worker class name: $workerClassName")

        return factoryProvider.get().create(appContext, workerParameters)
    }
}
