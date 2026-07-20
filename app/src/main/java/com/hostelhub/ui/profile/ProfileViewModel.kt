package com.hostelhub.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hostelhub.data.local.TokenManager
import com.hostelhub.data.model.User
import com.hostelhub.data.repository.AuthRepository
import com.hostelhub.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    
    init {
        loadProfile()
    }
    
    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val userId = tokenManager.userId.first()
            if (userId != null) {
                userRepository.getUserById(userId)
                    .onSuccess { user ->
                        _uiState.value = _uiState.value.copy(isLoading = false, user = user)
                    }
                    .onFailure { error ->
                        _uiState.value = _uiState.value.copy(isLoading = false, error = error.message)
                    }
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Not logged in")
            }
        }
    }
    
    fun logout(onLogoutComplete: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onLogoutComplete()
        }
    }
}

data class ProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
