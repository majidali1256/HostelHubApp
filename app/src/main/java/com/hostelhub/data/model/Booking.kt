package com.hostelhub.data.model

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type

class FlexibleBookingHostelInfoDeserializer : JsonDeserializer<BookingHostelInfo?> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): BookingHostelInfo? {
        if (json == null || json.isJsonNull) return null
        if (json.isJsonObject) {
            val obj = json.asJsonObject
            val id = when {
                obj.has("_id") && !obj.get("_id").isJsonNull -> obj.get("_id").asString
                obj.has("id") && !obj.get("id").isJsonNull -> obj.get("id").asString
                else -> ""
            }
            val name = if (obj.has("name") && !obj.get("name").isJsonNull) obj.get("name").asString else ""
            val location = if (obj.has("location") && !obj.get("location").isJsonNull) obj.get("location").asString else ""
            val price = if (obj.has("price") && !obj.get("price").isJsonNull) obj.get("price").asDouble else 0.0
            return BookingHostelInfo(id = id, name = name, location = location, price = price)
        }
        if (json.isJsonPrimitive) {
            return BookingHostelInfo(id = json.asString)
        }
        return null
    }
}

class FlexibleBookingUserInfoDeserializer : JsonDeserializer<BookingUserInfo?> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): BookingUserInfo? {
        if (json == null || json.isJsonNull) return null
        if (json.isJsonObject) {
            val obj = json.asJsonObject
            val id = when {
                obj.has("_id") && !obj.get("_id").isJsonNull -> obj.get("_id").asString
                obj.has("id") && !obj.get("id").isJsonNull -> obj.get("id").asString
                else -> ""
            }
            val firstName = if (obj.has("firstName") && !obj.get("firstName").isJsonNull) obj.get("firstName").asString else null
            val lastName = if (obj.has("lastName") && !obj.get("lastName").isJsonNull) obj.get("lastName").asString else null
            val email = if (obj.has("email") && !obj.get("email").isJsonNull) obj.get("email").asString else null
            val contactNumber = if (obj.has("contactNumber") && !obj.get("contactNumber").isJsonNull) obj.get("contactNumber").asString else null
            val profilePicture = if (obj.has("profilePicture") && !obj.get("profilePicture").isJsonNull) obj.get("profilePicture").asString else null
            return BookingUserInfo(id = id, firstName = firstName, lastName = lastName, email = email, contactNumber = contactNumber, profilePicture = profilePicture)
        }
        if (json.isJsonPrimitive) {
            return BookingUserInfo(id = json.asString)
        }
        return null
    }
}

/**
 * Booking model matching the backend Booking schema
 */
data class Booking(
    @SerializedName(value = "id", alternate = ["_id"])
    val id: String = "",
    @JsonAdapter(FlexibleBookingHostelInfoDeserializer::class)
    val hostelId: BookingHostelInfo? = null,
    @JsonAdapter(FlexibleBookingUserInfoDeserializer::class)
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
