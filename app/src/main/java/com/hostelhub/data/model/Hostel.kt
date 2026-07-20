package com.hostelhub.data.model

import com.google.gson.annotations.SerializedName

/**
 * Hostel model matching the backend Hostel schema
 */
data class Hostel(
    @SerializedName(value = "id", alternate = ["_id"])
    val id: String = "",
    val name: String = "",
    val location: String = "",
    val price: Double = 0.0,
    val capacity: Int? = null,
    val description: String? = null,
    val amenities: List<String> = emptyList(),
    val rating: Double = 0.0,
    val reviews: List<Review>? = null,
    val ownerId: String? = null,
    val verified: Boolean = false,
    val images: List<String> = emptyList(),
    val videos: List<String>? = null,
    val tour360: List<String>? = null,
    val propertyRules: List<String>? = null,
    val category: String = "Shared Room",
    val genderPreference: String? = null,
    val status: String = "Available",
    val coordinates: Coordinates? = null,
    val rooms: List<Room>? = null,
    val fairnessLabel: String? = null,
    val riskScore: Int? = null,
    val moderationNotice: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
) {
    /** Images with relative paths resolved to full URLs for the emulator */
    val resolvedImages: List<String>
        get() = images.map { resolveImageUrl(it) }

    companion object {
        private const val SERVER_BASE = "http://10.0.2.2:5003"

        /** Convert relative /uploads/ paths and http://localhost:5003 URLs to emulator-reachable URLs */
        fun resolveImageUrl(url: String): String {
            return when {
                url.startsWith("http://localhost:5003") -> url.replace("http://localhost:5003", SERVER_BASE)
                url.startsWith("http://127.0.0.1:5003") -> url.replace("http://127.0.0.1:5003", SERVER_BASE)
                url.startsWith("/uploads") -> "$SERVER_BASE$url"
                url.startsWith("http") -> url  // already absolute (e.g. unsplash)
                else -> url
            }
        }
    }
}

data class Bed(
    @SerializedName(value = "id", alternate = ["_id", "bedId"])
    val id: String = "",
    val bedNumber: String = "Bed 1",
    val bookingId: String? = null,
    val isOccupied: Boolean = false
)

data class Room(
    @SerializedName(value = "id", alternate = ["_id", "roomId"])
    val id: String = "",
    val name: String = "Room 101",
    val type: String = "Shared", // 'Single', 'Double', 'Triple', 'Shared'
    val price: Double = 0.0,
    val beds: List<Bed> = emptyList()
)

data class Coordinates(
    val type: String = "Point",
    val coordinates: List<Double> = emptyList()
) {
    val latitude: Double get() = coordinates.getOrElse(1) { 0.0 }
    val longitude: Double get() = coordinates.getOrElse(0) { 0.0 }
}

data class Review(
    @SerializedName(value = "id", alternate = ["_id"])
    val id: String = "",
    val hostelId: String? = null,
    val bookingId: String? = null,
    @SerializedName("reviewerId")
    val rawReviewerId: com.google.gson.JsonElement? = null,
    val userId: String = "",
    val username: String? = null,
    val rating: Int = 0,
    val cleanliness: Int = 0,
    val accuracy: Int = 0,
    val communication: Int = 0,
    val location: Int = 0,
    val value: Int = 0,
    val title: String? = null,
    val comment: String = "",
    val photos: List<String> = emptyList(),
    val helpfulCount: Int = 0,
    val helpfulVotes: List<String> = emptyList(),
    val response: OwnerReply? = null,
    @SerializedName(value = "createdAt", alternate = ["date"])
    val createdAt: String? = null
) {
    val reviewerInfo: ReviewerInfo?
        get() = try {
            if (rawReviewerId != null && rawReviewerId.isJsonObject) {
                com.google.gson.Gson().fromJson(rawReviewerId, ReviewerInfo::class.java)
            } else null
        } catch (e: Exception) { null }

    val actualUserId: String
        get() = if (rawReviewerId != null && rawReviewerId.isJsonPrimitive) {
            rawReviewerId.asString
        } else {
            reviewerInfo?.id ?: userId
        }

    val displayUsername: String
        get() = reviewerInfo?.fullName?.takeIf { it.isNotBlank() }
            ?: username?.takeIf { it.isNotBlank() }
            ?: "Anonymous Student"

    val displayAvatar: String?
        get() = reviewerInfo?.profilePicture
}

data class ReviewerInfo(
    @SerializedName(value = "id", alternate = ["_id"])
    val id: String = "",
    val firstName: String? = null,
    val lastName: String? = null,
    val profilePicture: String? = null
) {
    val fullName: String
        get() = listOfNotNull(firstName, lastName)
            .filter { it.isNotBlank() }
            .joinToString(" ")
            .takeIf { it.isNotBlank() } ?: "Student"
}

data class OwnerReply(
    val content: String = "",
    val responderId: ReviewerInfo? = null,
    @SerializedName(value = "respondedAt", alternate = ["createdAt", "date"])
    val respondedAt: String? = null
)

data class RatingStatistics(
    val avgRating: Float = 0f,
    val avgCleanliness: Float = 0f,
    val avgAccuracy: Float = 0f,
    val avgCommunication: Float = 0f,
    val avgLocation: Float = 0f,
    val avgValue: Float = 0f,
    val totalReviews: Int = 0,
    val breakdown: Map<String, Int> = emptyMap()
)

// Room categories matching web app
object RoomCategory {
    const val SHARED_ROOM = "Shared Room"
    const val PRIVATE_ROOM = "Private Room"
    const val ENTIRE_PLACE = "Entire Place"
    const val DORMITORY = "Dormitory"
    
    val all = listOf(SHARED_ROOM, PRIVATE_ROOM, ENTIRE_PLACE, DORMITORY)
}

// Gender preference options
object GenderPreference {
    const val BOYS = "boys"
    const val GIRLS = "girls"
    const val ANY = "any"
    
    val all = listOf(BOYS, GIRLS, ANY)
}

// Common amenities
object Amenities {
    val common = listOf(
        "WiFi", "AC", "Parking", "Laundry", "Kitchen",
        "Security", "CCTV", "Generator", "Geyser", "TV",
        "Meals", "Gym", "Study Room", "Common Area"
    )
}
