package com.hostelhub.data.model

import com.google.gson.annotations.SerializedName

/**
 * Digital Agreement model for rental contracts
 */
data class Agreement(
    @SerializedName(value = "id", alternate = ["_id"])
    val id: String = "",
    val bookingId: String = "",
    val hostelId: String = "",
    val landlordId: String = "",
    val tenantId: String = "",
    val hostel: AgreementHostelInfo? = null,
    val landlord: AgreementUserInfo? = null,
    val tenant: AgreementUserInfo? = null,
    val type: AgreementType = AgreementType.RENTAL,
    val title: String = "",
    val content: String = "", // Full agreement text
    val terms: List<AgreementTerm> = emptyList(),
    val duration: AgreementDuration = AgreementDuration(),
    val rentAmount: Double = 0.0,
    val deposit: Double = 0.0,
    val status: AgreementStatus = AgreementStatus.DRAFT,
    val signatures: List<SignatureRecord> = emptyList(),
    val metadata: AgreementMetadata? = null,
    val createdAt: String? = null,
    val signedAt: String? = null,
    val expiresAt: String? = null
)

data class AgreementHostelInfo(
    @SerializedName(value = "id", alternate = ["_id"])
    val id: String = "",
    val name: String = "",
    val location: String = ""
)

data class AgreementUserInfo(
    @SerializedName(value = "id", alternate = ["_id"])
    val id: String = "",
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val contactNumber: String? = null
) {
    val displayName: String
        get() = when {
            !firstName.isNullOrBlank() && !lastName.isNullOrBlank() -> "$firstName $lastName"
            !firstName.isNullOrBlank() -> firstName
            else -> "User"
        }
}

data class AgreementTerm(
    val title: String = "",
    val content: String = "",
    val required: Boolean = true
)

data class AgreementDuration(
    val startDate: String = "",
    val endDate: String = ""
)

data class SignatureRecord(
    val userId: String = "",
    val role: String = "", // "landlord", "tenant"
    val signatureId: String = "",
    val signatureUrl: String? = null,
    val signedAt: String = ""
)

data class AgreementMetadata(
    val utilities: List<String>? = null,
    val rules: List<String>? = null,
    val paymentMethod: String? = null,
    val lateFeePolicy: String? = null
)

enum class AgreementType {
    @SerializedName("lease") LEASE,
    @SerializedName("rental") RENTAL,
    @SerializedName("sublease") SUBLEASE,
    @SerializedName("rules") RULES
}

enum class AgreementStatus {
    @SerializedName("draft") DRAFT,
    @SerializedName("pending") PENDING,
    @SerializedName("signed") SIGNED,
    @SerializedName("active") ACTIVE,
    @SerializedName("expired") EXPIRED,
    @SerializedName("terminated") TERMINATED
}

// Common utilities in Pakistani hostels
object HostelUtilities {
    val common = listOf(
        "Electricity",
        "Water",
        "Gas",
        "Internet/WiFi",
        "Cable TV",
        "Generator Backup"
    )
}

// Common hostel rules
object HostelRules {
    val common = listOf(
        "No smoking in rooms",
        "No loud music after 10 PM",
        "Guests must be registered",
        "Keep common areas clean",
        "Pay rent by 5th of each month",
        "One month notice before vacating"
    )
}

