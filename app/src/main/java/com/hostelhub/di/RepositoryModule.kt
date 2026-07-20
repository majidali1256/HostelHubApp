package com.hostelhub.di

import com.hostelhub.data.api.*
import com.hostelhub.data.local.TokenManager
import com.hostelhub.data.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    
    @Provides
    @Singleton
    fun provideAuthRepository(
        authApi: AuthApi,
        tokenManager: TokenManager
    ): AuthRepository {
        return AuthRepository(authApi, tokenManager)
    }
    
    @Provides
    @Singleton
    fun provideHostelRepository(hostelApi: HostelApi): HostelRepository {
        return HostelRepository(hostelApi)
    }
    
    @Provides
    @Singleton
    fun provideBookingRepository(bookingApi: BookingApi): BookingRepository {
        return BookingRepository(bookingApi)
    }
    
    @Provides
    @Singleton
    fun provideUserRepository(userApi: UserApi): UserRepository {
        return UserRepository(userApi)
    }

    @Provides
    @Singleton
    fun provideChatRepository(chatApi: ChatApi): ChatRepository {
        return ChatRepository(chatApi)
    }

    @Provides
    @Singleton
    fun provideAppointmentRepository(appointmentApi: AppointmentApi): AppointmentRepository {
        return AppointmentRepository(appointmentApi)
    }

    @Provides
    @Singleton
    fun provideAgreementRepository(agreementApi: AgreementApi): AgreementRepository {
        return AgreementRepository(agreementApi)
    }

    @Provides
    @Singleton
    fun provideRoommateRepository(roommateApi: RoommateApi): RoommateRepository {
        return RoommateRepository(roommateApi)
    }

    @Provides
    @Singleton
    fun provideNotificationRepository(notificationApi: NotificationApi): NotificationRepository {
        return NotificationRepository(notificationApi)
    }

    @Provides
    @Singleton
    fun provideFraudRepository(fraudApi: FraudApi): FraudRepository {
        return FraudRepository(fraudApi)
    }

    @Provides
    @Singleton
    fun provideReviewRepository(reviewApi: ReviewApi): ReviewRepository {
        return ReviewRepository(reviewApi)
    }
}
