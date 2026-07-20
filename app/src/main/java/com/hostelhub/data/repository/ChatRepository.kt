package com.hostelhub.data.repository

import android.util.Log
import com.hostelhub.BuildConfig
import com.hostelhub.data.api.*
import com.hostelhub.data.model.*
import com.hostelhub.utils.DateUtils
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "ChatRepository"

@Singleton
class ChatRepository @Inject constructor(
    private val chatApi: ChatApi
) {
    // Demo conversations for testing
    private val demoConversations = mutableListOf<Conversation>()
    private val demoMessages = mutableMapOf<String, MutableList<Message>>()


    init {
        if (BuildConfig.DEMO_MODE) {
            initDemoData()
        }
    }

    private fun initDemoData() {
        val demoConvo = Conversation(
            id = "demo_convo_1",
            participants = listOf(
                ParticipantInfo(id = "demo_user", firstName = "Demo", lastName = "User", email = "demo@test.com"),
                ParticipantInfo(id = "owner_1", firstName = "Ahmed", lastName = "Khan", email = "ahmed@hostel.com")
            ),
            hostelId = "demo_hostel_1",
            type = "direct",
            lastMessage = LastMessage(
                content = "Hello, I'm interested in your hostel",
                senderId = "demo_user",
                timestamp = "2024-12-15T10:30:00Z"
            ),
            createdAt = "2024-12-15T10:00:00Z"
        )
        demoConversations.add(demoConvo)

        demoMessages["demo_convo_1"] = mutableListOf(
            Message(
                id = "msg_1",
                conversationId = "demo_convo_1",
                senderId = "demo_user",
                content = "Hello, I'm interested in your hostel",
                type = MessageType.TEXT,
                createdAt = "2024-12-15T10:30:00Z"
            ),
            Message(
                id = "msg_2",
                conversationId = "demo_convo_1",
                senderId = "owner_1",
                content = "Hi! Thank you for your interest. Would you like to schedule a viewing?",
                type = MessageType.TEXT,
                createdAt = "2024-12-15T10:35:00Z"
            )
        )
    }

    suspend fun getConversations(): Result<List<Conversation>> {
        if (BuildConfig.DEMO_MODE) {
            delay(500)
            return Result.success(demoConversations)
        }

        return try {
            val response = chatApi.getConversations()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch conversations"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching conversations", e)
            Result.failure(Exception("Failed to fetch conversations: ${e.message}"))
        }
    }

    suspend fun getConversation(id: String): Result<Conversation> {
        if (BuildConfig.DEMO_MODE) {
            delay(300)
            val conversation = demoConversations.find { it.id == id }
            return if (conversation != null) {
                Result.success(conversation)
            } else {
                Result.failure(Exception("Conversation not found"))
            }
        }

        return try {
            val response = chatApi.getConversation(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Conversation not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createConversation(participantId: String, hostelId: String? = null): Result<Conversation> {
        if (BuildConfig.DEMO_MODE) {
            delay(500)
            val newConvo = Conversation(
                id = "demo_convo_${System.currentTimeMillis()}",
                participants = listOf(
                    ParticipantInfo(id = "demo_user", firstName = "Demo", lastName = "User"),
                    ParticipantInfo(id = participantId, firstName = "Hostel", lastName = "Owner")
                ),
            hostelId = hostelId,
            type = "direct",
            createdAt = DateUtils.getCurrentTimestamp()
        )
        demoConversations.add(newConvo)
            demoMessages[newConvo.id] = mutableListOf()
            return Result.success(newConvo)
        }

        return try {
            val response = chatApi.createConversation(CreateConversationRequest(participantId, hostelId))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to create conversation"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMessages(conversationId: String, limit: Int = 50, before: String? = null): Result<List<Message>> {
        if (BuildConfig.DEMO_MODE) {
            delay(300)
            val messages = demoMessages[conversationId] ?: emptyList()
            return Result.success(messages.sortedByDescending { it.createdAt })
        }

        return try {
            val response = chatApi.getMessages(conversationId, limit, before)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch messages"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendMessage(conversationId: String, content: String, type: String = "text"): Result<Message> {
        if (BuildConfig.DEMO_MODE) {
            delay(300)
            val newMessage = Message(
                id = "msg_${System.currentTimeMillis()}",
                conversationId = conversationId,
                senderId = "demo_user",
                content = content,
                type = MessageType.TEXT,
                createdAt = DateUtils.getCurrentTimestamp()
            )
            demoMessages.getOrPut(conversationId) { mutableListOf() }.add(newMessage)

            // Update last message in conversation
            val convoIndex = demoConversations.indexOfFirst { it.id == conversationId }
            if (convoIndex >= 0) {
                demoConversations[convoIndex] = demoConversations[convoIndex].copy(
                    lastMessage = LastMessage(content, "demo_user", newMessage.createdAt ?: ""),
                    updatedAt = newMessage.createdAt
                )
            }
            return Result.success(newMessage)
        }

        return try {
            val response = chatApi.sendMessage(SendMessageRequest(conversationId, content, type))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to send message"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markAsRead(messageId: String): Result<Message> {
        if (BuildConfig.DEMO_MODE) {
            delay(100)
            return Result.success(Message(id = messageId))
        }

        return try {
            val response = chatApi.markAsRead(messageId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to mark as read"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markConversationAsRead(conversationId: String): Result<Conversation> {
        if (BuildConfig.DEMO_MODE) {
            delay(100)
            val convo = demoConversations.find { it.id == conversationId } ?: return Result.failure(Exception("Not found"))
            return Result.success(convo)
        }

        return try {
            val response = chatApi.markConversationAsRead(conversationId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to mark conversation as read"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteConversation(id: String): Result<String> {
        if (BuildConfig.DEMO_MODE) {
            delay(300)
            demoConversations.removeIf { it.id == id }
            demoMessages.remove(id)
            return Result.success("Conversation deleted")
        }

        return try {
            val response = chatApi.deleteConversation(id)
            if (response.isSuccessful) {
                Result.success("Conversation deleted")
            } else {
                Result.failure(Exception("Failed to delete conversation"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

