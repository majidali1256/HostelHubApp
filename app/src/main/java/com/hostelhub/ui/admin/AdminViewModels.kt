package com.hostelhub.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hostelhub.data.model.*
import com.hostelhub.data.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// ==================== Dashboard ViewModel ====================

data class AdminDashboardUiState(
    val stats: AdminStats = AdminStats(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminDashboardUiState())
    val uiState: StateFlow<AdminDashboardUiState> = _uiState.asStateFlow()

    init {
        loadStats()
    }

    fun loadStats() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            adminRepository.getDashboardStats()
                .onSuccess { stats ->
                    _uiState.value = _uiState.value.copy(isLoading = false, stats = stats)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load admin statistics"
                    )
                }
        }
    }
}

// ==================== User Management ViewModel ====================

data class UserManagementUiState(
    val users: List<UserManagementItem> = emptyList(),
    val searchQuery: String = "",
    val selectedRole: String = "All",
    val selectedStatus: String = "All",
    val isLoading: Boolean = false,
    val error: String? = null,
    val actionMessage: String? = null
)

@HiltViewModel
class UserManagementViewModel @Inject constructor(
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserManagementUiState())
    val uiState: StateFlow<UserManagementUiState> = _uiState.asStateFlow()

    init {
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, actionMessage = null)
            val roleParam = if (_uiState.value.selectedRole == "All") null else _uiState.value.selectedRole.lowercase()
            val statusParam = if (_uiState.value.selectedStatus == "All") null else _uiState.value.selectedStatus.lowercase()
            val searchParam = _uiState.value.searchQuery.ifBlank { null }

            adminRepository.getUsers(search = searchParam, role = roleParam, status = statusParam)
                .onSuccess { response ->
                    _uiState.value = _uiState.value.copy(isLoading = false, users = response.users)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load user directory"
                    )
                }
        }
    }

    fun searchUsers(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        loadUsers()
    }

    fun filterByRole(role: String) {
        _uiState.value = _uiState.value.copy(selectedRole = role)
        loadUsers()
    }

    fun filterByStatus(status: String) {
        _uiState.value = _uiState.value.copy(selectedStatus = status)
        loadUsers()
    }

    fun performAction(userId: String, action: String, reason: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, actionMessage = null)
            adminRepository.performUserAction(userId, action, reason)
                .onSuccess { updatedUser ->
                    val updatedList = _uiState.value.users.map { user ->
                        if (user.id == userId) updatedUser else user
                    }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        users = updatedList,
                        actionMessage = "Successfully updated user (${action.uppercase()})"
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to update user status"
                    )
                }
        }
    }

    fun clearActionMessage() {
        _uiState.value = _uiState.value.copy(actionMessage = null, error = null)
    }
}

// ==================== Moderation Queue ViewModel ====================

data class ModerationQueueUiState(
    val pendingHostels: List<PendingHostel> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val actionMessage: String? = null
)

@HiltViewModel
class ModerationQueueViewModel @Inject constructor(
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ModerationQueueUiState())
    val uiState: StateFlow<ModerationQueueUiState> = _uiState.asStateFlow()

    init {
        loadPendingQueue()
    }

    fun loadPendingQueue() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, actionMessage = null)
            adminRepository.getPendingHostels()
                .onSuccess { list ->
                    _uiState.value = _uiState.value.copy(isLoading = false, pendingHostels = list)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load moderation queue"
                    )
                }
        }
    }

    fun moderateHostel(hostelId: String, status: String, notes: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, actionMessage = null)
            adminRepository.moderateHostel(hostelId, status, notes)
                .onSuccess { _ ->
                    val filtered = _uiState.value.pendingHostels.filter { it.id != hostelId }
                    val msg = if (status == "active") "Listing approved and published instantly!" else "Listing rejected and archived."
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        pendingHostels = filtered,
                        actionMessage = msg
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to update listing status"
                    )
                }
        }
    }

    fun clearActionMessage() {
        _uiState.value = _uiState.value.copy(actionMessage = null, error = null)
    }
}
