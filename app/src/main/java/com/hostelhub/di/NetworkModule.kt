package com.hostelhub.di

import com.hostelhub.BuildConfig
import com.hostelhub.data.api.*
import com.hostelhub.data.local.TokenManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideAuthInterceptor(tokenManager: TokenManager): Interceptor {
        return Interceptor { chain ->
            val token = runBlocking { tokenManager.accessToken.first() }
            val request = chain.request().newBuilder().apply {
                token?.let {
                    addHeader("Authorization", "Bearer $it")
                }
            }.build()
            chain.proceed(request)
        }
    }
    
    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: Interceptor): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }
        
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }
    
    @Provides
    @Singleton
    fun provideHostelApi(retrofit: Retrofit): HostelApi {
        return retrofit.create(HostelApi::class.java)
    }
    
    @Provides
    @Singleton
    fun provideUserApi(retrofit: Retrofit): UserApi {
        return retrofit.create(UserApi::class.java)
    }
    
    @Provides
    @Singleton
    fun provideBookingApi(retrofit: Retrofit): BookingApi {
        return retrofit.create(BookingApi::class.java)
    }
    
    @Provides
    @Singleton
    fun provideChatApi(retrofit: Retrofit): ChatApi {
        return retrofit.create(ChatApi::class.java)
    }
    
    @Provides
    @Singleton
    fun provideNotificationApi(retrofit: Retrofit): NotificationApi {
        return retrofit.create(NotificationApi::class.java)
    }

    @Provides
    @Singleton
    fun provideAppointmentApi(retrofit: Retrofit): AppointmentApi {
        return retrofit.create(AppointmentApi::class.java)
    }

    @Provides
    @Singleton
    fun provideAgreementApi(retrofit: Retrofit): AgreementApi {
        return retrofit.create(AgreementApi::class.java)
    }

    @Provides
    @Singleton
    fun provideRoommateApi(retrofit: Retrofit): RoommateApi {
        return retrofit.create(RoommateApi::class.java)
    }

    @Provides
    @Singleton
    fun provideFraudApi(retrofit: Retrofit): FraudApi {
        return retrofit.create(FraudApi::class.java)
    }

    @Provides
    @Singleton
    fun provideAdminApi(retrofit: Retrofit): AdminApi {
        return retrofit.create(AdminApi::class.java)
    }

    @Provides
    @Singleton
    fun provideReviewApi(retrofit: Retrofit): ReviewApi {
        return retrofit.create(ReviewApi::class.java)
    }
}
