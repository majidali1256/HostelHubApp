package com.hostelhub.ui.fraud

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hostelhub.data.model.FraudReport
import com.hostelhub.data.model.FraudType
import com.hostelhub.data.repository.FraudRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FraudReportUiState(
    val isLoading: Boolean = false,
    val isSubmitted: Boolean = false,
    val errorMessage: String? = null,
    val submittedReport: FraudReport? = null
)

@HiltViewModel
class FraudViewModel @Inject constructor(
    private val fraudRepository: FraudRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FraudReportUiState())
    val uiState: StateFlow<FraudReportUiState> = _uiState.asStateFlow()

    fun submitReport(
        reportedUserId: String? = null,
        hostelId: String? = null,
        type: FraudType,
        description: String
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = fraudRepository.submitFraudReport(
                reportedUserId = reportedUserId,
                hostelId = hostelId,
                type = type,
                description = description
            )
            result.onSuccess { report ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSubmitted = true,
                    submittedReport = report
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to submit report"
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = FraudReportUiState()
    }
}
