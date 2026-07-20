package com.hostelhub.data.model

import com.google.gson.annotations.SerializedName

/**
 * Data models for Module 2: Admin & Moderation Platform
 */

data class AdminStats(
    @SerializedName(value = "users", alternate = ["totalUsers"])
    val users: Int = 0,
    @SerializedName(value = "hostels", alternate = ["activeHostels"])
    val hostels: Int = 0,
    @SerializedName(value = "bookings", alternate = ["totalBookings"])
    val bookings: Int = 0,
    @SerializedName(value = "pendingReports", alternate = ["pendingVerifications"])
    val pendingReports: Int = 0,
    @SerializedName(value = "totalRevenue", alternate = ["revenue"])
    val totalRevenue: Double = 0.0
)

data class UserManagementItem(
    @SerializedName(value = "id", alternate = ["_id"])
    val id: String = "",
    val email: String = "",
    val firstName: String? = null,
    val lastName: String? = null,
    val username: String? = null,
    val role: String = "customer",
    val isVerified: Boolean = false,
    val verificationStatus: String? = null,
    val isBanned: Boolean = false,
    val createdAt: String? = null
) {
    val displayName: String
        get() = when {
            !firstName.isNullOrBlank() || !lastName.isNullOrBlank() -> listOfNotNull(firstName, lastName).joinToString(" ")
            !username.isNullOrBlank() -> username
            else -> email
        }
}

data class UserManagementResponse(
    val users: List<UserManagementItem> = emptyList(),
    val pagination: PaginationMeta? = null
)

data class PaginationMeta(
    val total: Int = 0,
    val page: Int = 1,
    val pages: Int = 1
)

data class UserActionRequest(
    val action: String? = null,
    val reason: String? = null,
    val verificationStatus: String? = null,
    val isBanned: Boolean? = null,
    val status: String? = null
)

data class PendingHostel(
    @SerializedName(value = "id", alternate = ["_id"])
    val id: String = "",
    val name: String = "",
    val location: String = "",
    @SerializedName(value = "price", alternate = ["rent", "monthlyRent"])
    val price: Double = 0.0,
    val status: String = "pending",
    val riskScore: Int = 0,
    val description: String? = null,
    val images: List<String> = emptyList(),
    val ownerId: OwnerInfo? = null,
    val createdAt: String? = null
)

data class OwnerInfo(
    @SerializedName(value = "id", alternate = ["_id"])
    val id: String = "",
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null
) {
    val fullName: String
        get() = listOfNotNull(firstName, lastName).joinToString(" ").ifBlank { email ?: "Unknown Owner" }
}

data class HostelModerateRequest(
    val status: String,
    val adminNotes: String? = null
)
