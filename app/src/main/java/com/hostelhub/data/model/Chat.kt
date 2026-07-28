package com.hostelhub.data.model

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type

class FlexibleStringDeserializer : JsonDeserializer<String> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): String {
        if (json == null || json.isJsonNull) return ""
        if (json.isJsonPrimitive) return json.asString
        if (json.isJsonObject) {
            val obj = json.asJsonObject
            return when {
                obj.has("_id") && !obj.get("_id").isJsonNull -> obj.get("_id").asString
                obj.has("id") && !obj.get("id").isJsonNull -> obj.get("id").asString
                else -> ""
            }
        }
        return ""
    }
}

/**
 * Chat conversation
 */
data class Conversation(
    @SerializedName(value = "id", alternate = ["_id"])
    val id: String = "",
    val participants: List<ParticipantInfo> = emptyList(),
    val hostelId: String? = null,
    val type: String = "direct", // "direct", "group"
    val lastMessage: LastMessage? = null,
    val unreadCount: Map<String, Int>? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class ParticipantInfo(
    @SerializedName(value = "id", alternate = ["_id"])
    val id: String = "",
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val profilePicture: String? = null
) {
    val displayName: String
        get() = when {
            !firstName.isNullOrBlank() && !lastName.isNullOrBlank() -> "$firstName $lastName"
            !firstName.isNullOrBlank() -> firstName
            !email.isNullOrBlank() -> email.substringBefore("@")
            else -> "User"
        }
}

data class LastMessage(
    val content: String = "",
    @JsonAdapter(FlexibleStringDeserializer::class)
    val senderId: String = "",
    val timestamp: String = ""
)

/**
 * Paginated chat messages response
 */
data class MessagesResponse(
    val messages: List<Message> = emptyList(),
    val hasMore: Boolean = false
)

/**
 * Chat message
 */
data class Message(
    @SerializedName(value = "id", alternate = ["_id"])
    val id: String = "",
    @JsonAdapter(FlexibleStringDeserializer::class)
    val conversationId: String = "",
    @JsonAdapter(FlexibleStringDeserializer::class)
    val senderId: String = "",
    val content: String = "",
    val type: MessageType = MessageType.TEXT,
    val attachments: List<Attachment>? = null,
    val readBy: List<ReadReceipt>? = null,
    val createdAt: String? = null
)

data class Attachment(
    val url: String = "",
    val type: String = "",
    val name: String = "",
    val size: Long = 0
)

data class ReadReceipt(
    val userId: String = "",
    val readAt: String = ""
)

enum class MessageType {
    @SerializedName("text") TEXT,
    @SerializedName("image") IMAGE,
    @SerializedName("file") FILE,
    @SerializedName("audio") AUDIO
}

/**
 * Notification model
 */
data class Notification(
    @SerializedName(value = "id", alternate = ["_id"])
    val id: String = "",
    val userId: String = "",
    val type: String = "", // "booking", "message", "review", "appointment", "system", "payment"
    val title: String = "",
    val message: String = "",
    val data: NotificationData? = null,
    val read: Boolean = false,
    val readAt: String? = null,
    val actionUrl: String? = null,
    val priority: String = "medium", // "low", "medium", "high"
    val createdAt: String? = null
)

data class NotificationData(
    val bookingId: String? = null,
    val messageId: String? = null,
    val reviewId: String? = null,
    val appointmentId: String? = null,
    val hostelId: String? = null,
    val conversationId: String? = null,
    val amount: Double? = null
)

