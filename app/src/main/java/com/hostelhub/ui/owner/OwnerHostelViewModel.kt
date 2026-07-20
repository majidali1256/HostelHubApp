package com.hostelhub.ui.owner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hostelhub.data.api.CreateHostelRequest
import com.hostelhub.data.model.Hostel
import com.hostelhub.data.repository.HostelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OwnerHostelUiState(
    val hostels: List<Hostel> = emptyList(),
    val activeHostels: List<Hostel> = emptyList(),
    val pendingHostels: List<Hostel> = emptyList(),
    val inactiveHostels: List<Hostel> = emptyList(),
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class OwnerHostelViewModel @Inject constructor(
    private val repository: HostelRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OwnerHostelUiState())
    val uiState: StateFlow<OwnerHostelUiState> = _uiState.asStateFlow()

    init {
        loadOwnerHostels()
    }

    fun loadOwnerHostels() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = repository.getOwnerHostels()
            result.onSuccess { list ->
                val active = list.filter { it.status.equals("Available", ignoreCase = true) || it.status.equals("active", ignoreCase = true) }
                val pending = list.filter { it.status.equals("Inactive", ignoreCase = true) && !it.verified && (it.riskScore ?: 0) > 0 }
                val inactive = list.filter { !active.contains(it) && !pending.contains(it) }
                
                _uiState.update {
                    it.copy(
                        hostels = list,
                        activeHostels = active,
                        pendingHostels = pending,
                        inactiveHostels = inactive,
                        isLoading = false
                    )
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load properties"
                    )
                }
            }
        }
    }

    fun createListing(
        name: String,
        location: String,
        price: Double,
        capacity: Int?,
        description: String?,
        amenities: List<String>,
        category: String,
        genderPreference: String,
        images: List<String>,
        propertyRules: List<String>?,
        roomsJson: String?,
        latitude: Double?,
        longitude: Double?,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null, successMessage = null) }
            val request = CreateHostelRequest(
                name = name,
                location = location,
                price = price,
                capacity = capacity,
                description = description,
                amenities = amenities,
                category = category,
                genderPreference = genderPreference,
                images = images,
                propertyRules = propertyRules,
                rooms = roomsJson,
                latitude = latitude,
                longitude = longitude
            )
            val result = repository.createHostel(request)
            result.onSuccess {
                _uiState.update { state ->
                    state.copy(
                        isSubmitting = false,
                        successMessage = "Property listed successfully!"
                    )
                }
                loadOwnerHostels()
                onSuccess()
            }.onFailure { e ->
                _uiState.update { state ->
                    state.copy(
                        isSubmitting = false,
                        error = e.message ?: "Failed to create listing"
                    )
                }
            }
        }
    }

    fun updateListing(
        id: String,
        name: String,
        location: String,
        price: Double,
        capacity: Int?,
        description: String?,
        amenities: List<String>,
        category: String,
        genderPreference: String,
        images: List<String>,
        propertyRules: List<String>?,
        roomsJson: String?,
        latitude: Double?,
        longitude: Double?,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null, successMessage = null) }
            val request = CreateHostelRequest(
                name = name,
                location = location,
                price = price,
                capacity = capacity,
                description = description,
                amenities = amenities,
                category = category,
                genderPreference = genderPreference,
                images = images,
                propertyRules = propertyRules,
                rooms = roomsJson,
                latitude = latitude,
                longitude = longitude
            )
            val result = repository.updateHostel(id, request)
            result.onSuccess {
                _uiState.update { state ->
                    state.copy(
                        isSubmitting = false,
                        successMessage = "Property updated successfully!"
                    )
                }
                loadOwnerHostels()
                onSuccess()
            }.onFailure { e ->
                _uiState.update { state ->
                    state.copy(
                        isSubmitting = false,
                        error = e.message ?: "Failed to update listing"
                    )
                }
            }
        }
    }

    fun deleteListing(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = repository.deleteHostel(id)
            result.onSuccess {
                _uiState.update { state ->
                    state.copy(successMessage = "Property deleted successfully")
                }
                loadOwnerHostels()
            }.onFailure { e ->
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to delete property"
                    )
                }
            }
        }
    }

    fun toggleStatus(hostel: Hostel) {
        viewModelScope.launch {
            val newStatus = if (hostel.status.equals("Available", ignoreCase = true) || hostel.status.equals("active", ignoreCase = true)) {
                "Inactive"
            } else {
                "Available"
            }
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = repository.updateHostelStatus(hostel.id, newStatus)
            result.onSuccess {
                _uiState.update { state ->
                    state.copy(successMessage = "Status updated to $newStatus")
                }
                loadOwnerHostels()
            }.onFailure { e ->
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to change status"
                    )
                }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}
