package com.hostelhub.ui.agreements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hostelhub.data.model.Agreement
import com.hostelhub.data.model.AgreementStatus
import com.hostelhub.data.repository.AgreementRepository
import com.hostelhub.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class AgreementFilter(val displayName: String) {
    ALL("All Contracts"),
    PENDING_SIGNATURE("Pending My Signature"),
    ACTIVE("Signed / Active"),
    COMPLETED("Completed / Expired")
}

data class AgreementUiState(
    val isLoading: Boolean = false,
    val agreements: List<Agreement> = emptyList(),
    val filteredAgreements: List<Agreement> = emptyList(),
    val selectedAgreement: Agreement? = null,
    val filterType: AgreementFilter = AgreementFilter.ALL,
    val isSigning: Boolean = false,
    val isGenerating: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class AgreementViewModel @Inject constructor(
    private val agreementRepository: AgreementRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AgreementUiState())
    val uiState: StateFlow<AgreementUiState> = _uiState.asStateFlow()

    init {
        loadMyAgreements()
    }

    fun loadMyAgreements() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = agreementRepository.getMyAgreements()
            result.fold(
                onSuccess = { list ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            agreements = list,
                            filteredAgreements = applyFilter(list, state.filterType)
                        )
                    }
                },
                onFailure = { ex ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = ex.message ?: "Failed to load agreements."
                        )
                    }
                }
            )
        }
    }

    fun setFilter(filter: AgreementFilter) {
        _uiState.update { state ->
            state.copy(
                filterType = filter,
                filteredAgreements = applyFilter(state.agreements, filter)
            )
        }
    }

    private fun applyFilter(list: List<Agreement>, filter: AgreementFilter): List<Agreement> {
        return when (filter) {
            AgreementFilter.ALL -> list
            AgreementFilter.PENDING_SIGNATURE -> list.filter { agreement ->
                agreement.status == AgreementStatus.PENDING || agreement.status == AgreementStatus.DRAFT
            }
            AgreementFilter.ACTIVE -> list.filter { agreement ->
                agreement.status == AgreementStatus.ACTIVE || agreement.status == AgreementStatus.SIGNED
            }
            AgreementFilter.COMPLETED -> list.filter { agreement ->
                agreement.status == AgreementStatus.EXPIRED || agreement.status == AgreementStatus.TERMINATED
            }
        }
    }

    fun loadAgreementDetail(agreementId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = agreementRepository.getAgreement(agreementId)
            result.fold(
                onSuccess = { agreement ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            selectedAgreement = agreement
                        )
                    }
                },
                onFailure = { ex ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = ex.message ?: "Failed to load agreement details."
                        )
                    }
                }
            )
        }
    }

    fun signAgreement(agreementId: String, signatureBase64: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSigning = true, error = null) }
            val result = agreementRepository.signAgreement(agreementId, signatureBase64)
            result.fold(
                onSuccess = { updatedAgreement ->
                    _uiState.update { state ->
                        val updatedList = state.agreements.map { if (it.id == agreementId) updatedAgreement else it }
                        state.copy(
                            isSigning = false,
                            agreements = updatedList,
                            filteredAgreements = applyFilter(updatedList, state.filterType),
                            selectedAgreement = updatedAgreement,
                            successMessage = "Digital signature successfully recorded. Contract is now legally binding!"
                        )
                    }
                },
                onFailure = { ex ->
                    _uiState.update {
                        it.copy(
                            isSigning = false,
                            error = ex.message ?: "Failed to sign agreement."
                        )
                    }
                }
            )
        }
    }

    fun generateAgreement(
        bookingId: String,
        hostelId: String,
        studentId: String,
        ownerId: String,
        termsAndConditions: String,
        monthlyRent: Double
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isGenerating = true, error = null) }
            val result = agreementRepository.generateAgreement(
                bookingId, hostelId, studentId, ownerId, termsAndConditions, monthlyRent
            )
            result.fold(
                onSuccess = { newAgreement ->
                    _uiState.update { state ->
                        val updatedList = listOf(newAgreement) + state.agreements
                        state.copy(
                            isGenerating = false,
                            agreements = updatedList,
                            filteredAgreements = applyFilter(updatedList, state.filterType),
                            selectedAgreement = newAgreement,
                            successMessage = "Digital tenancy agreement generated successfully!"
                        )
                    }
                },
                onFailure = { ex ->
                    _uiState.update {
                        it.copy(
                            isGenerating = false,
                            error = ex.message ?: "Failed to generate agreement."
                        )
                    }
                }
            )
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}
