package com.hostelhub.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hostelhub.data.local.TokenManager
import com.hostelhub.data.model.Hostel
import com.hostelhub.data.repository.HostelRepository
import com.hostelhub.data.repository.NotificationRepository
import com.hostelhub.data.socket.SocketManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val hostelRepository: HostelRepository,
    private val notificationRepository: NotificationRepository,
    private val socketManager: SocketManager,
    private val tokenManager: TokenManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        loadHostels()
        loadUnreadNotifications()
        observeSocketNotifications()
        observeUserInfo()
    }

    private fun observeUserInfo() {
        viewModelScope.launch {
            tokenManager.userRole.collect { role ->
                val resolvedRole = role ?: "admin" // Default to admin for full visibility demo
                val name = when (resolvedRole.lowercase()) {
                    "admin" -> "System Admin"
                    "owner" -> "Hostel Owner"
                    else -> "Customer Stay"
                }
                _uiState.update { it.copy(userRole = resolvedRole, username = name) }
            }
        }
    }

    private fun loadUnreadNotifications() {
        viewModelScope.launch {
            notificationRepository.getUnreadCount()
                .onSuccess { count ->
                    _uiState.update { it.copy(unreadNotificationCount = count) }
                }
        }
    }

    private fun observeSocketNotifications() {
        viewModelScope.launch {
            socketManager.notifications.collect { notification ->
                if (!notification.read) {
                    _uiState.update { it.copy(unreadNotificationCount = it.unreadNotificationCount + 1) }
                }
            }
        }
    }
    
    fun loadHostels() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            hostelRepository.getHostels()
                .onSuccess { hostels ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        hostels = hostels,
                        filteredHostels = hostels
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load hostels"
                    )
                }
        }
    }
    
    fun search(query: String) {
        val filtered = if (query.isBlank()) {
            _uiState.value.hostels
        } else {
            _uiState.value.hostels.filter { hostel ->
                hostel.name.contains(query, ignoreCase = true) ||
                hostel.location.contains(query, ignoreCase = true) ||
                (hostel.description?.contains(query, ignoreCase = true) == true) ||
                hostel.category.contains(query, ignoreCase = true)
            }
        }
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            filteredHostels = filtered
        )
    }

    fun filterBySector(sector: String?) {
        _uiState.value = _uiState.value.copy(selectedSector = sector)
        if (sector == null || sector == "All") {
            _uiState.value = _uiState.value.copy(filteredHostels = _uiState.value.hostels)
        } else {
            val filtered = _uiState.value.hostels.filter { hostel ->
                hostel.location.contains(sector, ignoreCase = true) ||
                hostel.name.contains(sector, ignoreCase = true) ||
                (hostel.description?.contains(sector, ignoreCase = true) == true)
            }
            _uiState.value = _uiState.value.copy(filteredHostels = filtered)
        }
    }
    
    fun applyFilters(
        minPrice: Int? = null,
        maxPrice: Int? = null,
        category: String? = null,
        genderPreference: String? = null,
        verifiedOnly: Boolean = false,
        amenities: List<String>? = null,
        location: String? = null,
        sortBy: String? = null
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            hostelRepository.searchHostels(
                minPrice = minPrice,
                maxPrice = maxPrice,
                amenities = amenities,
                roomCategories = category?.let { listOf(it) },
                genderPreference = genderPreference,
                verifiedOnly = if (verifiedOnly) true else null,
                location = location,
                sortBy = sortBy
            )
                .onSuccess { hostels ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        filteredHostels = hostels
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
        }
    }
    
    fun clearFilters() {
        _uiState.value = _uiState.value.copy(
            searchQuery = "",
            selectedSector = null,
            filteredHostels = _uiState.value.hostels
        )
    }
}

data class HomeUiState(
    val hostels: List<Hostel> = emptyList(),
    val filteredHostels: List<Hostel> = emptyList(),
    val searchQuery: String = "",
    val selectedSector: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val unreadNotificationCount: Int = 0,
    val userRole: String = "admin",
    val username: String = "System Admin"
)
