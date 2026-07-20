package com.hostelhub.ui.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hostelhub.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class VerificationViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VerificationUiState())
    val uiState: StateFlow<VerificationUiState> = _uiState.asStateFlow()

    init {
        loadVerificationStatus()
    }

    fun loadVerificationStatus() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            userRepository.getVerificationStatus()
                .onSuccess { response ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        status = response.status,
                        trustScore = response.trustScore ?: 50,
                        rejectionReason = response.rejectionReason,
                        documentPath = response.document,
                        isSubmitted = response.status == "pending" || response.status == "verified"
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to fetch verification status"
                    )
                }
        }
    }

    fun submitVerificationDocument(
        context: Context,
        uris: List<Uri>,
        documentType: String = "CNIC"
    ) {
        if (uris.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "Please select at least one document photo")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, error = null)

            try {
                // Security Pillar 4: Enforce 5MB limit before uploading
                val uri = uris.first()
                val tempFile = File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")
                
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(tempFile).use { output ->
                        input.copyTo(output)
                    }
                }

                if (tempFile.length() > 5 * 1024 * 1024) {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        error = "File size exceeds 5MB limit. Please select a smaller image or document."
                    )
                    tempFile.delete()
                    return@launch
                }

                userRepository.uploadVerificationDocument(tempFile, documentType)
                    .onSuccess {
                        tempFile.delete()
                        _uiState.value = _uiState.value.copy(
                            isSubmitting = false,
                            isSubmitted = true,
                            status = "pending",
                            trustScore = 75
                        )
                        loadVerificationStatus()
                    }
                    .onFailure { error ->
                        tempFile.delete()
                        _uiState.value = _uiState.value.copy(
                            isSubmitting = false,
                            error = error.message ?: "Failed to upload verification document"
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    error = "Error processing file: ${e.localizedMessage}"
                )
            }
        }
    }
}

data class VerificationUiState(
    val status: String = "unverified",
    val trustScore: Int = 50,
    val documentPath: String? = null,
    val rejectionReason: String? = null,
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val isSubmitted: Boolean = false,
    val error: String? = null
)
