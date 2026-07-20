package com.hostelhub.data.socket

import android.util.Log
import com.hostelhub.BuildConfig
import com.hostelhub.data.local.TokenManager
import com.hostelhub.data.model.Message
import com.hostelhub.data.model.Notification
import com.google.gson.Gson
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocketManager @Inject constructor(
    private val tokenManager: TokenManager,
    private val gson: Gson
) {
    private var socket: Socket? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()
    
    private val _messages = MutableSharedFlow<Message>()
    val messages: SharedFlow<Message> = _messages.asSharedFlow()
    
    private val _notifications = MutableSharedFlow<Notification>()
    val notifications: SharedFlow<Notification> = _notifications.asSharedFlow()
    
    private val _typingUsers = MutableStateFlow<Set<String>>(emptySet())
    val typingUsers: StateFlow<Set<String>> = _typingUsers.asStateFlow()
    
    fun connect() {
        scope.launch {
            try {
                val token = tokenManager.accessToken.first()
                if (token == null) {
                    Log.w(TAG, "No token available for socket connection")
                    return@launch
                }
                
                val baseUrl = BuildConfig.BASE_URL.replace("/api/", "")
                
                val options = IO.Options().apply {
                    auth = mapOf("token" to token)
                    transports = arrayOf("websocket")
                    reconnection = true
                    reconnectionAttempts = 5
                    reconnectionDelay = 1000
                }
                
                socket = IO.socket(baseUrl, options)
                setupListeners()
                socket?.connect()
                
                Log.d(TAG, "Socket connecting to $baseUrl")
            } catch (e: Exception) {
                Log.e(TAG, "Socket connection error", e)
                _connectionState.value = ConnectionState.Error(e.message ?: "Connection failed")
            }
        }
    }
    
    private fun setupListeners() {
        socket?.apply {
            on(Socket.EVENT_CONNECT) {
                Log.d(TAG, "Socket connected")
                _connectionState.value = ConnectionState.Connected
            }
            
            on(Socket.EVENT_DISCONNECT) {
                Log.d(TAG, "Socket disconnected")
                _connectionState.value = ConnectionState.Disconnected
            }
            
            on(Socket.EVENT_CONNECT_ERROR) { args ->
                val error = args.firstOrNull()?.toString() ?: "Unknown error"
                Log.e(TAG, "Socket connection error: $error")
                _connectionState.value = ConnectionState.Error(error)
            }
            
            on("message:new") { args ->
                try {
                    val data = args.firstOrNull() as? JSONObject
                    data?.let {
                        val message = gson.fromJson(it.toString(), Message::class.java)
                        scope.launch {
                            _messages.emit(message)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing message", e)
                }
            }
            
            on("user:typing") { args ->
                try {
                    val data = args.firstOrNull() as? JSONObject
                    val userId = data?.getString("userId")
                    userId?.let {
                        _typingUsers.value = _typingUsers.value + it
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing typing event", e)
                }
            }

            on("user:stopped-typing") { args ->
                try {
                    val data = args.firstOrNull() as? JSONObject
                    val userId = data?.getString("userId")
                    userId?.let {
                        _typingUsers.value = _typingUsers.value - it
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing stopped-typing event", e)
                }
            }
            
            on("message:read") { args ->
                Log.d(TAG, "Message read: ${args.firstOrNull()}")
            }

            val notificationHandler: (Array<Any>) -> Unit = { args ->
                try {
                    val data = args.firstOrNull() as? JSONObject
                    data?.let {
                        val notification = gson.fromJson(it.toString(), Notification::class.java)
                        scope.launch {
                            _notifications.emit(notification)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing notification event", e)
                }
            }
            on("notification:new", notificationHandler)
            on("notification", notificationHandler)
        }
    }
    
    fun joinConversation(conversationId: String) {
        socket?.emit("join:conversation", conversationId)
        Log.d(TAG, "Joined conversation: $conversationId")
    }
    
    fun leaveConversation(conversationId: String) {
        socket?.emit("leave:conversation", conversationId)
        Log.d(TAG, "Left conversation: $conversationId")
    }
    
    fun sendTyping(conversationId: String, isTyping: Boolean) {
        if (isTyping) {
            socket?.emit("typing:start", conversationId)
        } else {
            socket?.emit("typing:stop", conversationId)
        }
    }
    
    fun sendOnlineStatus() {
        socket?.emit("user:online")
    }
    
    fun markAsRead(conversationId: String, messageId: String) {
        val data = JSONObject().apply {
            put("conversationId", conversationId)
            put("messageId", messageId)
        }
        socket?.emit("mark_read", data)
    }
    
    fun disconnect() {
        socket?.disconnect()
        socket?.off()
        socket = null
        _connectionState.value = ConnectionState.Disconnected
        Log.d(TAG, "Socket disconnected and cleaned up")
    }
    
    fun isConnected(): Boolean = socket?.connected() == true
    
    companion object {
        private const val TAG = "SocketManager"
    }
}

sealed class ConnectionState {
    object Disconnected : ConnectionState()
    object Connecting : ConnectionState()
    object Connected : ConnectionState()
    data class Error(val message: String) : ConnectionState()
}
