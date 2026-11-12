package com.ipos.network

import com.google.firebase.firestore.FirebaseFirestore
import com.ipos.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL) // Set via BuildConfig — debug/release values in build.gradle.kts
            .addConverterFactory(GsonConverterFactory.create())
            .client(
                OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)  // short enough for WorkManager window
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(false)        // BaseSyncWorker handles retries
                    .build()
            )
            .build()
    }

    @Provides
    @Singleton
    fun provideUserApiService(retrofit: Retrofit): UserApiService {
        return retrofit.create(UserApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideBusinessApiService(retrofit: Retrofit): BusinessApiService {
        return retrofit.create(BusinessApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideInvoiceApiService(retrofit: Retrofit): InvoiceApiService {
        return retrofit.create(InvoiceApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideProductApiService(retrofit: Retrofit): ProductApiService {
        return retrofit.create(ProductApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideFiscalizationApi(retrofit: Retrofit): FiscalizationApi {
        return retrofit.create(FiscalizationApi::class.java)
    }

    @Provides
    @Singleton
    fun provideInvoiceLineApiService(retrofit: Retrofit): InvoiceLineApiService {
        return retrofit.create(InvoiceLineApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideDeviceManager(@dagger.hilt.android.qualifiers.ApplicationContext context: android.content.Context): com.ipos.app.core.DeviceManager {
        return com.ipos.app.core.DeviceManager(context)
    }

    @Provides
    @Singleton
    fun provideConnectivityHelper(@dagger.hilt.android.qualifiers.ApplicationContext context: android.content.Context): ConnectivityHelper {
        return ConnectivityHelper(context)
    }

    @Provides
    @Singleton
    fun provideSyncApiService(retrofit: Retrofit): SyncApiService {
        return retrofit.create(SyncApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }
}
