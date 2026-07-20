package com.hostelhub.data.model

import com.google.gson.annotations.SerializedName

/**
 * Appointment model for scheduling hostel viewings
 */
data class Appointment(
    @SerializedName(value = "id", alternate = ["_id"])
    val id: String = "",
    val hostelId: String = "",
    val customerId: String = "",
    val ownerId: String = "",
    val hostel: AppointmentHostelInfo? = null,
    val customer: AppointmentUserInfo? = null,
    val owner: AppointmentUserInfo? = null,
    val scheduledTime: String = "",
    val duration: Int = 60, // minutes (15-180)
    val type: AppointmentType = AppointmentType.VIEWING,
    val status: AppointmentStatus = AppointmentStatus.PENDING,
    val notes: String? = null,
    val location: String? = null,
    val meetingLink: String? = null, // For virtual meetings
    val createdAt: String? = null,
    val confirmedAt: String? = null,
    val cancelledAt: String? = null,
    val cancelReason: String? = null
)

data class AppointmentHostelInfo(
    @SerializedName(value = "id", alternate = ["_id"])
    val id: String = "",
    val name: String = "",
    val location: String = "",
    val images: List<String>? = null
)

data class AppointmentUserInfo(
    @SerializedName(value = "id", alternate = ["_id"])
    val id: String = "",
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val contactNumber: String? = null,
    val profilePicture: String? = null
) {
    val displayName: String
        get() = when {
            !firstName.isNullOrBlank() && !lastName.isNullOrBlank() -> "$firstName $lastName"
            !firstName.isNullOrBlank() -> firstName
            else -> "User"
        }
}

enum class AppointmentType {
    @SerializedName("viewing") VIEWING,
    @SerializedName("consultation") CONSULTATION,
    @SerializedName("other") OTHER
}

enum class AppointmentStatus {
    @SerializedName("pending") PENDING,
    @SerializedName("confirmed") CONFIRMED,
    @SerializedName("cancelled") CANCELLED,
    @SerializedName("completed") COMPLETED
}

// Appointment duration options in minutes
object AppointmentDuration {
    val options = listOf(15, 30, 45, 60, 90, 120, 180)

    fun formatDuration(minutes: Int): String {
        return when {
            minutes < 60 -> "$minutes min"
            minutes == 60 -> "1 hour"
            minutes % 60 == 0 -> "${minutes / 60} hours"
            else -> "${minutes / 60}h ${minutes % 60}m"
        }
    }
}

