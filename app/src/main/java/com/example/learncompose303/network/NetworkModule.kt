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
        //     "DefaultConnection_RABCHITSERVER": "Server=RABCHITSERVER;Database=iPosDb;User Id=Aximos;Password=$(db_password);Integrated Security=True;MultipleActiveResultSets=True;Trusted_Connection=True;TrustServerCertificate=True"

        return Retrofit.Builder()
//            .baseUrl("http://192.168.222.98:5000/") // Wi-Fi IP (old)
            .baseUrl("http://192.168.43.217:5000/") // Wi-Fi IP (old)
//            .baseUrl("http://192.168.223.126:5000/") // Wi-Fi IP (current)
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
