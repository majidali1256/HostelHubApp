package com.hostelhub.ui.hostel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hostelhub.data.model.Hostel
import com.hostelhub.data.model.User
import com.hostelhub.data.repository.HostelRepository
import com.hostelhub.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HostelDetailViewModel @Inject constructor(
    private val hostelRepository: HostelRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HostelDetailUiState())
    val uiState: StateFlow<HostelDetailUiState> = _uiState.asStateFlow()
    
    fun loadHostel(hostelId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            hostelRepository.getHostelById(hostelId)
                .onSuccess { hostel ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        hostel = hostel
                    )
                    // Load owner info
                    hostel.ownerId?.let { loadOwner(it) }
                    loadFairnessAnalysis(hostelId)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load hostel"
                    )
                }
        }
    }
    
    private fun loadFairnessAnalysis(hostelId: String) {
        viewModelScope.launch {
            hostelRepository.getFairnessAnalysis(hostelId)
                .onSuccess { analysis ->
                    _uiState.value = _uiState.value.copy(fairnessAnalysis = analysis)
                }
        }
    }
    
    private fun loadOwner(ownerId: String) {
        viewModelScope.launch {
            userRepository.getUserById(ownerId)
                .onSuccess { owner ->
                    _uiState.value = _uiState.value.copy(owner = owner)
                }
        }
    }
    
    fun addReview(rating: Int, comment: String, onSuccess: () -> Unit) {
        val hostelId = _uiState.value.hostel?.id ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmittingReview = true)
            
            hostelRepository.addReview(hostelId, rating, comment)
                .onSuccess { updatedHostel ->
                    _uiState.value = _uiState.value.copy(
                        isSubmittingReview = false,
                        hostel = updatedHostel
                    )
                    onSuccess()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isSubmittingReview = false,
                        reviewError = error.message
                    )
                }
        }
    }
    
    fun setCurrentImageIndex(index: Int) {
        _uiState.value = _uiState.value.copy(currentImageIndex = index)
    }
}

data class HostelDetailUiState(
    val hostel: Hostel? = null,
    val owner: User? = null,
    val fairnessAnalysis: com.hostelhub.data.model.FairnessAnalysisResponse? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentImageIndex: Int = 0,
    val isSubmittingReview: Boolean = false,
    val reviewError: String? = null
)

