package com.hostelhub.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hostelhub.data.model.Notification
import com.hostelhub.data.repository.NotificationRepository
import com.hostelhub.data.socket.SocketManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class NotificationFilter(val displayName: String) {
    ALL("All"),
    UNREAD("Unread"),
    BOOKING("Booking"),
    PAYMENT("Payment"),
    MESSAGE("Messages"),
    SYSTEM("System")
}

data class NotificationUiState(
    val notifications: List<Notification> = emptyList(),
    val filteredNotifications: List<Notification> = emptyList(),
    val unreadCount: Int = 0,
    val selectedFilter: NotificationFilter = NotificationFilter.ALL,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val socketManager: SocketManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    init {
        loadNotifications()
        observeRealTimeNotifications()
    }

    fun loadNotifications() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val result = notificationRepository.getNotifications()
            val unreadResult = notificationRepository.getUnreadCount()
            
            result.fold(
                onSuccess = { list ->
                    val count = unreadResult.getOrDefault(list.count { !it.read })
                    _uiState.update { state ->
                        state.copy(
                            notifications = list,
                            unreadCount = count,
                            isLoading = false,
                            error = null
                        ).applyFilter(state.selectedFilter)
                    }
                },
                onFailure = { throwable ->
                    _uiState.update { 
                        it.copy(isLoading = false, error = throwable.message ?: "Failed to load notifications") 
                    }
                }
            )
        }
    }

    fun selectFilter(filter: NotificationFilter) {
        _uiState.update { state ->
            state.copy(selectedFilter = filter).applyFilter(filter)
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            // Optimistic update
            _uiState.update { state ->
                val updatedList = state.notifications.map { notif ->
                    if (notif.id == notificationId && !notif.read) {
                        notif.copy(read = true)
                    } else notif
                }
                val newUnread = updatedList.count { !it.read }
                state.copy(
                    notifications = updatedList,
                    unreadCount = newUnread
                ).applyFilter(state.selectedFilter)
            }
            
            notificationRepository.markAsRead(notificationId)
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            // Optimistic update
            _uiState.update { state ->
                val updatedList = state.notifications.map { it.copy(read = true) }
                state.copy(
                    notifications = updatedList,
                    unreadCount = 0
                ).applyFilter(state.selectedFilter)
            }
            
            notificationRepository.markAllAsRead()
        }
    }

    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            // Optimistic update
            _uiState.update { state ->
                val updatedList = state.notifications.filter { it.id != notificationId }
                val newUnread = updatedList.count { !it.read }
                state.copy(
                    notifications = updatedList,
                    unreadCount = newUnread
                ).applyFilter(state.selectedFilter)
            }
            
            notificationRepository.deleteNotification(notificationId)
        }
    }

    private fun observeRealTimeNotifications() {
        viewModelScope.launch {
            socketManager.notifications.collect { newNotification ->
                _uiState.update { state ->
                    // Avoid duplicate if already exists
                    if (state.notifications.any { it.id == newNotification.id }) {
                        state
                    } else {
                        val updatedList = listOf(newNotification) + state.notifications
                        val newUnread = if (!newNotification.read) state.unreadCount + 1 else state.unreadCount
                        state.copy(
                            notifications = updatedList,
                            unreadCount = newUnread
                        ).applyFilter(state.selectedFilter)
                    }
                }
            }
        }
    }

    private fun NotificationUiState.applyFilter(filter: NotificationFilter): NotificationUiState {
        val filtered = when (filter) {
            NotificationFilter.ALL -> notifications
            NotificationFilter.UNREAD -> notifications.filter { !it.read }
            NotificationFilter.BOOKING -> notifications.filter { it.type == "booking" }
            NotificationFilter.PAYMENT -> notifications.filter { it.type == "payment" }
            NotificationFilter.MESSAGE -> notifications.filter { it.type == "message" }
            NotificationFilter.SYSTEM -> notifications.filter { it.type in listOf("system", "review", "appointment") }
        }
        return copy(filteredNotifications = filtered)
    }
}
