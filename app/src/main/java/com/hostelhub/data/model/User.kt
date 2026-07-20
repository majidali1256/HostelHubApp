package com.hostelhub.data.model

import com.google.gson.annotations.SerializedName

/**
 * User model matching the backend User schema
 */
data class User(
    @SerializedName(value = "id", alternate = ["_id"])
    val id: String = "",
    val username: String = "",
    val email: String = "",
    val role: UserRole = UserRole.CUSTOMER,
    val firstName: String? = null,
    val lastName: String? = null,
    val contactNumber: String? = null,
    val profilePicture: String? = null,
    val trustScore: Int? = null,
    val isVerified: Boolean = false,
    val emailVerified: Boolean = false,
    val verificationStatus: String? = null,
    val idDocument: String? = null,
    val documentName: String? = null,
    val rejectionReason: String? = null,
    val verificationDate: String? = null,
    val verificationDocuments: List<String>? = null,
    val stayHistory: List<String>? = null,
    val bankDetails: BankDetails? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

enum class UserRole {
    @SerializedName("owner") OWNER,
    @SerializedName("customer") CUSTOMER,
    @SerializedName("pending") PENDING,
    @SerializedName("admin") ADMIN
}

data class BankDetails(
    val bankName: String? = null,
    val accountTitle: String? = null,
    val accountNumber: String? = null,
    val iban: String? = null,
    val jazzCashNumber: String? = null,
    val easyPaisaNumber: String? = null,
    val verified: Boolean = false
)
