package com.hostelhub.data.api

import com.hostelhub.data.model.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Notifications API endpoints
 */
interface NotificationApi {

    @GET("notifications")
    suspend fun getNotifications(): Response<NotificationsResponse>

    @GET("notifications/unread")
    suspend fun getUnreadCount(): Response<UnreadCountResponse>

    @PATCH("notifications/{id}/read")
    suspend fun markAsRead(@Path("id") notificationId: String): Response<Notification>

    @PATCH("notifications/read-all")
    suspend fun markAllAsRead(): Response<MessageResponse>

    @DELETE("notifications/{id}")
    suspend fun deleteNotification(@Path("id") notificationId: String): Response<MessageResponse>

    @GET("notifications/preferences")
    suspend fun getNotificationPreferences(): Response<NotificationPreferences>

    @PATCH("notifications/preferences")
    suspend fun updateNotificationPreferences(
        @Body preferences: NotificationPreferences
    ): Response<NotificationPreferences>

    @POST("notifications/fcm-token")
    suspend fun registerFcmToken(@Body request: FcmTokenRequest): Response<MessageResponse>
}

data class NotificationsResponse(
    val notifications: List<Notification>,
    val total: Int = 0,
    val unreadCount: Int = 0
)

data class UnreadCountResponse(
    val count: Int
)

data class NotificationPreferences(
    val bookingUpdates: Boolean = true,
    val messageNotifications: Boolean = true,
    val reviewNotifications: Boolean = true,
    val appointmentReminders: Boolean = true,
    val systemAnnouncements: Boolean = true,
    val paymentUpdates: Boolean = true,
    val emailNotifications: Boolean = true,
    val pushNotifications: Boolean = true
)

data class FcmTokenRequest(
    val token: String,
    val deviceId: String
)

