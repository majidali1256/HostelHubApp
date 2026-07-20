package com.hostelhub.ui.fairrent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hostelhub.data.model.MarketBenchmarksResponse
import com.hostelhub.data.model.PredictRentRequest
import com.hostelhub.data.model.PredictRentResponse
import com.hostelhub.data.repository.HostelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FairRentUiState(
    val selectedLocation: String = "Islamabad H-12",
    val selectedRoomType: String = "Single",
    val selectedCapacity: Int = 1,
    val selectedAmenities: Set<String> = setOf("WiFi", "AC", "Generator"),
    val isLoading: Boolean = false,
    val prediction: PredictRentResponse? = null,
    val benchmarks: MarketBenchmarksResponse? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class FairRentViewModel @Inject constructor(
    private val hostelRepository: HostelRepository
) : ViewModel() {

    val availableLocations = listOf(
        "Islamabad H-12",
        "Islamabad G-11",
        "Islamabad F-10",
        "Lahore Johar Town",
        "Lahore Gulberg",
        "Lahore DHA",
        "Karachi DHA Phase 6",
        "Karachi Gulshan-e-Iqbal",
        "Rawalpindi Satellite Town"
    )

    val availableRoomTypes = listOf("Single", "Shared 2-Bed", "Shared 3-Bed", "Private Suite")

    val allAmenities = listOf(
        "WiFi", "AC", "Generator", "Attached Bath",
        "Laundry", "Mess / Food", "Study Area", "Security Cameras",
        "Gym", "Biometric Access", "Parking", "Cleaners"
    )

    private val _uiState = MutableStateFlow(FairRentUiState())
    val uiState: StateFlow<FairRentUiState> = _uiState.asStateFlow()

    init {
        loadBenchmarks(_uiState.value.selectedLocation)
        calculateFairRent()
    }

    fun updateLocation(location: String) {
        _uiState.value = _uiState.value.copy(selectedLocation = location)
        loadBenchmarks(location)
    }

    fun updateRoomType(roomType: String) {
        val cap = when (roomType) {
            "Single", "Private Suite" -> 1
            "Shared 2-Bed" -> 2
            "Shared 3-Bed" -> 3
            else -> 1
        }
        _uiState.value = _uiState.value.copy(selectedRoomType = roomType, selectedCapacity = cap)
    }

    fun updateCapacity(capacity: Int) {
        _uiState.value = _uiState.value.copy(selectedCapacity = capacity)
    }

    fun toggleAmenity(amenity: String) {
        val current = _uiState.value.selectedAmenities.toMutableSet()
        if (current.contains(amenity)) {
            current.remove(amenity)
        } else {
            current.add(amenity)
        }
        _uiState.value = _uiState.value.copy(selectedAmenities = current)
    }

    fun loadBenchmarks(location: String) {
        viewModelScope.launch {
            hostelRepository.getMarketBenchmarks(location)
                .onSuccess { bench ->
                    _uiState.value = _uiState.value.copy(benchmarks = bench)
                }
        }
    }

    fun calculateFairRent() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val request = PredictRentRequest(
                location = _uiState.value.selectedLocation,
                roomType = _uiState.value.selectedRoomType,
                capacity = _uiState.value.selectedCapacity,
                amenities = _uiState.value.selectedAmenities.toList()
            )
            hostelRepository.predictRent(request)
                .onSuccess { response ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        prediction = response
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Failed to calculate fair rent"
                    )
                }
        }
    }
}
