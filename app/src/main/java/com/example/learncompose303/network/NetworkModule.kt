package com.example.learncompose303.network

import com.google.firebase.firestore.FirebaseFirestore
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
            .baseUrl("http://10.51.10.104:5000/") // Backend via USB tethering (Ethernet 5)
//            .baseUrl("http://192.168.222.61:5000/") // Wi-Fi IP (AP isolation blocks this)
//            .baseUrl("http://10.4.60.28:5000/") // Old Ethernet IP
//            .baseUrl("http://192.168.222.36:5000/") // Old Wi-Fi IP
//            .baseUrl("http://192.168.43.35:5000/") // Old hotspot IP
//            .baseUrl("http://192.168.100.5:5000/") // Old network IP
//            .baseUrl("http://10.0.2.2:5000/") // Use 10.0.2.2 for localhost from Android emulator
            .addConverterFactory(GsonConverterFactory.create())
            .client(
                OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
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
    fun provideDeviceManager(@dagger.hilt.android.qualifiers.ApplicationContext context: android.content.Context): com.example.learncompose303.app.core.DeviceManager {
        return com.example.learncompose303.app.core.DeviceManager(context)
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
