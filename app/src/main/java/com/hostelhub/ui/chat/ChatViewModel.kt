package com.hostelhub.ui.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hostelhub.data.local.TokenManager
import com.hostelhub.data.model.Conversation
import com.hostelhub.data.model.Message
import com.hostelhub.data.repository.ChatRepository
import com.hostelhub.data.socket.SocketManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConversationsUiState(
    val conversations: List<Conversation> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class ChatRoomUiState(
    val conversation: Conversation? = null,
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false,
    val isPartnerTyping: Boolean = false,
    val error: String? = null,
    val currentUserId: String = ""
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val socketManager: SocketManager,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _conversationsUiState = MutableStateFlow(ConversationsUiState(isLoading = true))
    val conversationsUiState: StateFlow<ConversationsUiState> = _conversationsUiState.asStateFlow()

    private val _roomUiState = MutableStateFlow(ChatRoomUiState(isLoading = true))
    val roomUiState: StateFlow<ChatRoomUiState> = _roomUiState.asStateFlow()

    private var activeConversationId: String? = null
    private var typingJob: Job? = null
    private var isCurrentlyTyping = false

    init {
        // Collect current user ID and ensure socket is connected when chat is used
        viewModelScope.launch {
            tokenManager.userId.collect { userId ->
                _roomUiState.update { it.copy(currentUserId = userId ?: "") }
            }
        }

        // Collect incoming real-time messages from SocketManager
        viewModelScope.launch {
            socketManager.messages.collect { newMessage ->
                Log.d("ChatViewModel", "New message received via socket: ${newMessage.content}")
                
                // 1. If currently in the active conversation room, append the message
                val activeId = activeConversationId
                if (activeId != null && newMessage.conversationId == activeId) {
                    _roomUiState.update { state ->
                        if (state.messages.any { it.id == newMessage.id }) state
                        else state.copy(messages = state.messages + newMessage)
                    }
                    // Mark as read immediately if incoming from partner
                    if (newMessage.senderId != _roomUiState.value.currentUserId && newMessage.id.isNotEmpty()) {
                        chatRepository.markAsRead(newMessage.id)
                    }
                }

                // 2. Update conversation list preview if available
                _conversationsUiState.update { state ->
                    val updatedList = state.conversations.map { convo ->
                        if (convo.id == newMessage.conversationId) {
                            val isUnread = newMessage.senderId != _roomUiState.value.currentUserId && activeId != convo.id
                            val currentUnreadMap = convo.unreadCount?.toMutableMap() ?: mutableMapOf()
                            if (isUnread) {
                                val myId = _roomUiState.value.currentUserId
                                if (myId.isNotEmpty()) {
                                    currentUnreadMap[myId] = (currentUnreadMap[myId] ?: 0) + 1
                                }
                            }
                            convo.copy(
                                lastMessage = com.hostelhub.data.model.LastMessage(
                                    content = newMessage.content,
                                    senderId = newMessage.senderId,
                                    timestamp = newMessage.createdAt ?: ""
                                ),
                                updatedAt = newMessage.createdAt,
                                unreadCount = currentUnreadMap
                            )
                        } else {
                            convo
                        }
                    }
                    state.copy(conversations = updatedList)
                }
            }
        }

        // Collect typing indicators
        viewModelScope.launch {
            socketManager.typingUsers.collect { typingSet ->
                val activeId = activeConversationId
                if (activeId != null) {
                    val partnerTyping = typingSet.isNotEmpty()
                    _roomUiState.update { it.copy(isPartnerTyping = partnerTyping) }
                }
            }
        }
    }

    fun loadConversations() {
        viewModelScope.launch {
            _conversationsUiState.update { it.copy(isLoading = true, error = null) }
            if (!socketManager.isConnected()) {
                socketManager.connect()
            }
            val result = chatRepository.getConversations()
            if (result.isSuccess) {
                _conversationsUiState.update {
                    it.copy(
                        conversations = result.getOrNull() ?: emptyList(),
                        isLoading = false
                    )
                }
            } else {
                _conversationsUiState.update {
                    it.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Failed to load messages"
                    )
                }
            }
        }
    }

    fun loadAndJoinConversation(conversationOrParticipantId: String, hostelId: String? = null) {
        viewModelScope.launch {
            _roomUiState.update { it.copy(isLoading = true, error = null, messages = emptyList()) }
            if (!socketManager.isConnected()) {
                socketManager.connect()
            }

            // First, check if conversationOrParticipantId is an existing conversation ID
            var conversationResult = chatRepository.getConversation(conversationOrParticipantId)
            var conversation = conversationResult.getOrNull()

            // If not found, check if it's a participant ID and try creating/getting conversation with that participant
            if (conversation == null) {
                val createResult = chatRepository.createConversation(conversationOrParticipantId, hostelId)
                conversation = createResult.getOrNull()
            }

            if (conversation != null) {
                activeConversationId = conversation.id
                socketManager.joinConversation(conversation.id)

                // Load messages
                val messagesResult = chatRepository.getMessages(conversation.id)
                val loadedMessages = messagesResult.getOrNull() ?: emptyList()

                _roomUiState.update {
                    it.copy(
                        conversation = conversation,
                        messages = loadedMessages,
                        isLoading = false
                    )
                }

                // Mark conversation as read
                chatRepository.markConversationAsRead(conversation.id)
                // Also clear unread count in list locally
                _conversationsUiState.update { state ->
                    val updated = state.conversations.map { c ->
                        if (c.id == conversation.id) {
                            val clearedMap = c.unreadCount?.toMutableMap() ?: mutableMapOf()
                            val myId = _roomUiState.value.currentUserId
                            if (myId.isNotEmpty()) clearedMap[myId] = 0
                            c.copy(unreadCount = clearedMap)
                        } else c
                    }
                    state.copy(conversations = updated)
                }
            } else {
                _roomUiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Could not load or initialize conversation"
                    )
                }
            }
        }
    }

    fun sendMessage(content: String) {
        val activeId = activeConversationId ?: return
        if (content.isBlank()) return

        // Stop typing indicator before sending
        if (isCurrentlyTyping) {
            isCurrentlyTyping = false
            socketManager.sendTyping(activeId, false)
            typingJob?.cancel()
        }

        viewModelScope.launch {
            // Optimistic UI insert
            val tempMessage = Message(
                id = "temp_${System.currentTimeMillis()}",
                conversationId = activeId,
                senderId = _roomUiState.value.currentUserId,
                content = content,
                type = com.hostelhub.data.model.MessageType.TEXT,
                createdAt = com.hostelhub.utils.DateUtils.getCurrentTimestamp()
            )
            _roomUiState.update { state ->
                state.copy(messages = state.messages + tempMessage)
            }

            // Send via REST (which triggers backend to save and emit message:new via socket)
            val result = chatRepository.sendMessage(activeId, content)
            if (result.isSuccess) {
                val sentMsg = result.getOrNull()
                if (sentMsg != null) {
                    _roomUiState.update { state ->
                        state.copy(
                            messages = state.messages.map { if (it.id == tempMessage.id) sentMsg else it }
                        )
                    }
                }
            } else {
                // If failed, we keep the message or mark error
                Log.e("ChatViewModel", "Failed to send message: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    fun onTypingChanged(text: String) {
        val activeId = activeConversationId ?: return
        if (text.isNotEmpty() && !isCurrentlyTyping) {
            isCurrentlyTyping = true
            socketManager.sendTyping(activeId, true)
        }
        
        typingJob?.cancel()
        typingJob = viewModelScope.launch {
            delay(2000)
            if (isCurrentlyTyping) {
                isCurrentlyTyping = false
                socketManager.sendTyping(activeId, false)
            }
        }
    }

    fun leaveRoom() {
        val activeId = activeConversationId
        if (activeId != null) {
            if (isCurrentlyTyping) {
                socketManager.sendTyping(activeId, false)
                isCurrentlyTyping = false
            }
            socketManager.leaveConversation(activeId)
            activeConversationId = null
        }
    }

    fun deleteConversation(id: String) {
        viewModelScope.launch {
            val result = chatRepository.deleteConversation(id)
            if (result.isSuccess) {
                _conversationsUiState.update { state ->
                    state.copy(conversations = state.conversations.filter { it.id != id })
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        leaveRoom()
    }
}
