package com.hostelhub.data.model

import com.google.gson.annotations.SerializedName

/**
 * Booking model matching the backend Booking schema
 */
data class Booking(
    @SerializedName(value = "id", alternate = ["_id"])
    val id: String = "",
    val hostelId: BookingHostelInfo? = null,
    val customerId: BookingUserInfo? = null,
    val checkIn: String = "",
    val checkOut: String = "",
    val status: BookingStatus = BookingStatus.PENDING,
    val totalPrice: Double = 0.0,
    val numberOfGuests: Int = 1,
    val specialRequests: String? = null,
    val paymentStatus: PaymentStatus = PaymentStatus.PENDING,
    val paymentMethod: String? = null,
    val paymentReceipt: PaymentReceipt? = null,
    val transactionId: String? = null,
    val createdAt: String? = null,
    val confirmedAt: String? = null,
    val cancelledAt: String? = null,
    val cancelReason: String? = null
)

data class BookingHostelInfo(
    @SerializedName(value = "id", alternate = ["_id"])
    val id: String = "",
    val name: String = "",
    val location: String = "",
    val price: Double = 0.0,
    val images: List<String>? = null
)

data class BookingUserInfo(
    @SerializedName(value = "id", alternate = ["_id"])
    val id: String = "",
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val contactNumber: String? = null,
    val profilePicture: String? = null
)

data class PaymentReceipt(
    val image: String? = null,
    val uploadedAt: String? = null,
    val verified: Boolean = false,
    val verifiedBy: String? = null,
    val verifiedAt: String? = null,
    val rejectionReason: String? = null
)

enum class BookingStatus {
    @SerializedName("pending") PENDING,
    @SerializedName("confirmed") CONFIRMED,
    @SerializedName("cancelled") CANCELLED,
    @SerializedName("completed") COMPLETED,
    @SerializedName("rejected") REJECTED
}

enum class PaymentStatus {
    @SerializedName("pending") PENDING,
    @SerializedName("submitted") SUBMITTED,
    @SerializedName("verified") VERIFIED,
    @SerializedName("rejected") REJECTED
}

// Payment Methods supported in Pakistan
object PaymentMethod {
    const val BANK_TRANSFER = "bank_transfer"
    const val JAZZCASH = "jazzcash"
    const val EASYPAISA = "easypaisa"
    const val OTHER = "other"

    val all = listOf(BANK_TRANSFER, JAZZCASH, EASYPAISA, OTHER)
}

// Pakistani Banks
object PakistaniBanks {
    val banks = listOf(
        "HBL" to "Habib Bank Limited",
        "UBL" to "United Bank Limited",
        "Meezan" to "Meezan Bank",
        "Allied" to "Allied Bank",
        "MCB" to "MCB Bank",
        "Askari" to "Askari Bank",
        "Faysal" to "Faysal Bank",
        "Bank Alfalah" to "Bank Alfalah",
        "Standard Chartered" to "Standard Chartered",
        "JS Bank" to "JS Bank"
    )
}
