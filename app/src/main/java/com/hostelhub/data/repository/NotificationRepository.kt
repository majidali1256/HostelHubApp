package com.hostelhub.data.repository

import android.util.Log
import com.hostelhub.BuildConfig
import com.hostelhub.data.api.*
import com.hostelhub.data.model.*
import com.hostelhub.utils.DateUtils
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "NotificationRepository"

@Singleton
class NotificationRepository @Inject constructor(
    private val notificationApi: NotificationApi
) {
    private val demoNotifications = mutableListOf<Notification>()

    init {
        if (BuildConfig.DEMO_MODE) {
            initDemoData()
        }
    }

    private fun initDemoData() {
        demoNotifications.addAll(listOf(
            Notification(
                id = "notif_1",
                userId = "demo_user",
                type = "booking",
                title = "Booking Confirmed!",
                message = "Your booking at Lahore Student Hostel has been confirmed.",
                data = NotificationData(bookingId = "demo_booking_1", hostelId = "demo_hostel_1"),
                read = false,
                priority = "high",
                createdAt = "2024-12-15T10:00:00Z"
            ),
            Notification(
                id = "notif_2",
                userId = "demo_user",
                type = "message",
                title = "New Message",
                message = "Ahmed Khan sent you a message about your booking.",
                data = NotificationData(conversationId = "demo_convo_1"),
                read = false,
                priority = "medium",
                createdAt = "2024-12-15T09:30:00Z"
            ),
            Notification(
                id = "notif_3",
                userId = "demo_user",
                type = "system",
                title = "Welcome to Hostel Hub!",
                message = "Start exploring hostels in your area and find your perfect stay.",
                read = true,
                readAt = "2024-12-14T15:00:00Z",
                priority = "low",
                createdAt = "2024-12-14T12:00:00Z"
            ),
            Notification(
                id = "notif_4",
                userId = "demo_user",
                type = "payment",
                title = "Payment Verified",
                message = "Your payment of PKR 45,000 has been verified by the owner.",
                data = NotificationData(bookingId = "demo_booking_1", amount = 45000.0),
                read = true,
                priority = "high",
                createdAt = "2024-12-14T10:00:00Z"
            )
        ))
    }

    suspend fun getNotifications(): Result<List<Notification>> {
        if (BuildConfig.DEMO_MODE) {
            delay(500)
            return Result.success(demoNotifications.sortedByDescending { it.createdAt })
        }

        return try {
            val response = notificationApi.getNotifications()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.notifications)
            } else {
                Result.failure(Exception("Failed to fetch notifications"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching notifications", e)
            Result.failure(Exception("Failed to fetch notifications: ${e.message}"))
        }
    }

    suspend fun getUnreadCount(): Result<Int> {
        if (BuildConfig.DEMO_MODE) {
            delay(200)
            val count = demoNotifications.count { !it.read }
            return Result.success(count)
        }

        return try {
            val response = notificationApi.getUnreadCount()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.count)
            } else {
                Result.failure(Exception("Failed to fetch unread count"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markAsRead(notificationId: String): Result<Notification> {
        if (BuildConfig.DEMO_MODE) {
            delay(200)
            val index = demoNotifications.indexOfFirst { it.id == notificationId }
            if (index >= 0) {
                val updated = demoNotifications[index].copy(
                    read = true,
                    readAt = DateUtils.getCurrentTimestamp()
                )
                demoNotifications[index] = updated
                return Result.success(updated)
            }
            return Result.failure(Exception("Notification not found"))
        }

        return try {
            val response = notificationApi.markAsRead(notificationId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to mark as read"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markAllAsRead(): Result<String> {
        if (BuildConfig.DEMO_MODE) {
            delay(300)
            val now = DateUtils.getCurrentTimestamp()
            demoNotifications.forEachIndexed { index, notification ->
                if (!notification.read) {
                    demoNotifications[index] = notification.copy(read = true, readAt = now)
                }
            }
            return Result.success("All notifications marked as read")
        }

        return try {
            val response = notificationApi.markAllAsRead()
            if (response.isSuccessful) {
                Result.success(response.body()?.message ?: "All marked as read")
            } else {
                Result.failure(Exception("Failed to mark all as read"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteNotification(notificationId: String): Result<String> {
        if (BuildConfig.DEMO_MODE) {
            delay(200)
            demoNotifications.removeIf { it.id == notificationId }
            return Result.success("Notification deleted")
        }

        return try {
            val response = notificationApi.deleteNotification(notificationId)
            if (response.isSuccessful) {
                Result.success(response.body()?.message ?: "Notification deleted")
            } else {
                Result.failure(Exception("Failed to delete notification"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getNotificationPreferences(): Result<NotificationPreferences> {
        if (BuildConfig.DEMO_MODE) {
            delay(300)
            return Result.success(NotificationPreferences())
        }

        return try {
            val response = notificationApi.getNotificationPreferences()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch preferences"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateNotificationPreferences(preferences: NotificationPreferences): Result<NotificationPreferences> {
        if (BuildConfig.DEMO_MODE) {
            delay(500)
            return Result.success(preferences)
        }

        return try {
            val response = notificationApi.updateNotificationPreferences(preferences)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to update preferences"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registerFcmToken(token: String, deviceId: String): Result<String> {
        if (BuildConfig.DEMO_MODE) {
            delay(200)
            return Result.success("Token registered (Demo)")
        }

        return try {
            val response = notificationApi.registerFcmToken(FcmTokenRequest(token, deviceId))
            if (response.isSuccessful) {
                Result.success(response.body()?.message ?: "Token registered")
            } else {
                Result.failure(Exception("Failed to register token"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

