package com.hostelhub.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hostelhub.data.model.FraudReport
import com.hostelhub.data.model.FraudReportStatus
import com.hostelhub.data.model.FraudStats
import com.hostelhub.data.repository.FraudRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminFraudUiState(
    val isLoading: Boolean = false,
    val reports: List<FraudReport> = emptyList(),
    val stats: FraudStats = FraudStats(),
    val selectedFilter: String = "All", // "All", "Pending", "Investigating", "High Risk"
    val errorMessage: String? = null
)

@HiltViewModel
class AdminFraudViewModel @Inject constructor(
    private val fraudRepository: FraudRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminFraudUiState(isLoading = true))
    val uiState: StateFlow<AdminFraudUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            val statusParam = when (_uiState.value.selectedFilter) {
                "Pending" -> "pending"
                "Investigating" -> "investigating"
                "Confirmed" -> "confirmed"
                else -> null
            }
            
            val riskParam = if (_uiState.value.selectedFilter == "High Risk") "high" else null

            val statsResult = fraudRepository.getFraudStats()
            val reportsResult = fraudRepository.getAllFraudReports(status = statusParam, riskLevel = riskParam)

            val currentStats = statsResult.getOrDefault(_uiState.value.stats)
            var currentReports = reportsResult.getOrDefault(_uiState.value.reports)
            
            if (_uiState.value.selectedFilter == "High Risk") {
                currentReports = currentReports.filter { 
                    it.aiAnalysis?.riskLevel?.name?.equals("high", ignoreCase = true) == true || 
                    it.aiAnalysis?.riskLevel?.name?.equals("critical", ignoreCase = true) == true 
                }
            }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                stats = currentStats,
                reports = currentReports
            )
        }
    }

    fun setFilter(filter: String) {
        if (_uiState.value.selectedFilter != filter) {
            _uiState.value = _uiState.value.copy(selectedFilter = filter)
            loadDashboardData()
        }
    }

    fun updateReportStatus(reportId: String, newStatus: FraudReportStatus, notes: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = fraudRepository.updateFraudReportStatus(reportId, newStatus, notes)
            result.onSuccess {
                loadDashboardData()
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to update report status"
                )
            }
        }
    }

    fun deleteReport(reportId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = fraudRepository.deleteFraudReport(reportId)
            result.onSuccess {
                loadDashboardData()
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to delete report"
                )
            }
        }
    }
}
