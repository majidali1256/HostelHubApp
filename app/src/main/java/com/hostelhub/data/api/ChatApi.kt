package com.hostelhub.data.api

import com.hostelhub.data.model.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

/**
 * Chat/Messaging API endpoints
 */
interface ChatApi {

    @GET("conversations")
    suspend fun getConversations(): Response<List<Conversation>>

    @POST("conversations")
    suspend fun createConversation(@Body request: CreateConversationRequest): Response<Conversation>

    @GET("conversations/{id}")
    suspend fun getConversation(@Path("id") id: String): Response<Conversation>

    @DELETE("conversations/{id}")
    suspend fun deleteConversation(@Path("id") id: String): Response<MessageResponse>

    @GET("conversations/{id}/messages")
    suspend fun getMessages(
        @Path("id") conversationId: String,
        @Query("limit") limit: Int = 50,
        @Query("before") before: String? = null
    ): Response<List<Message>>

    @POST("messages")
    suspend fun sendMessage(@Body request: SendMessageRequest): Response<Message>

    @PATCH("messages/{id}/read")
    suspend fun markAsRead(@Path("id") messageId: String): Response<Message>

    @PATCH("conversations/{id}/read")
    suspend fun markConversationAsRead(@Path("id") conversationId: String): Response<Conversation>

    @Multipart
    @POST("messages/attachment")
    suspend fun sendAttachment(
        @Part("conversationId") conversationId: okhttp3.RequestBody,
        @Part attachment: MultipartBody.Part
    ): Response<Message>
}

data class CreateConversationRequest(
    val participantId: String,
    val hostelId: String? = null
)

data class SendMessageRequest(
    val conversationId: String,
    val content: String,
    val type: String = "text"
)

