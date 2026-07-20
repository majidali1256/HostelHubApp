package com.hostelhub.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hostelhub.data.model.User
import com.hostelhub.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    val isLoggedIn = authRepository.isLoggedIn
    
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    
    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(email = email, error = null)
    }
    
    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(password = password, error = null)
    }
    
    fun login(onSuccess: () -> Unit) {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password
        
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Please fill in all fields")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            authRepository.login(email, password)
                .onSuccess { user ->
                    _uiState.value = _uiState.value.copy(isLoading = false, user = user)
                    onSuccess()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Login failed"
                    )
                }
        }
    }
    
    fun forgotPassword(email: String, onSuccess: () -> Unit) {
        if (email.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Please enter your email")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            authRepository.forgotPassword(email)
                .onSuccess { message ->
                    _uiState.value = _uiState.value.copy(isLoading = false, message = message)
                    onSuccess()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to send reset email"
                    )
                }
        }
    }

    fun verifyResetCode(email: String, code: String, onSuccess: () -> Unit) {
        if (code.isBlank() || code.length < 4) {
            _uiState.value = _uiState.value.copy(error = "Please enter the verification code")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            authRepository.verifyResetCode(email, code)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = null)
                    onSuccess()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Invalid or expired code"
                    )
                }
        }
    }

    fun resetPasswordWithCode(email: String, code: String, newPassword: String, onSuccess: () -> Unit) {
        if (newPassword.length < 6) {
            _uiState.value = _uiState.value.copy(error = "Password must be at least 6 characters")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            authRepository.resetPasswordWithCode(email, code, newPassword)
                .onSuccess { message ->
                    _uiState.value = _uiState.value.copy(isLoading = false, message = message, error = null)
                    onSuccess()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to reset password"
                    )
                }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun socialLogin(provider: String, providerId: String, email: String, name: String, picture: String? = null, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            authRepository.socialLogin(provider, providerId, email, name, picture)
                .onSuccess { user ->
                    _uiState.value = _uiState.value.copy(isLoading = false, user = user)
                    onSuccess()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "$provider login failed"
                    )
                }
        }
    }

    fun saveOAuthTokens(accessToken: String, refreshToken: String, userId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            authRepository.saveOAuthTokens(accessToken, refreshToken, userId)
            _uiState.value = _uiState.value.copy(isLoading = false)
            onSuccess()
        }
    }
}

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val message: String? = null,
    val user: User? = null
)

@HiltViewModel
class SignupViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SignupUiState())
    val uiState: StateFlow<SignupUiState> = _uiState.asStateFlow()
    
    fun updateField(field: SignupField, value: String) {
        _uiState.value = when (field) {
            SignupField.EMAIL -> _uiState.value.copy(email = value, error = null)
            SignupField.PASSWORD -> _uiState.value.copy(password = value, error = null)
            SignupField.CONFIRM_PASSWORD -> _uiState.value.copy(confirmPassword = value, error = null)
            SignupField.USERNAME -> _uiState.value.copy(username = value, error = null)
            SignupField.FIRST_NAME -> _uiState.value.copy(firstName = value, error = null)
            SignupField.LAST_NAME -> _uiState.value.copy(lastName = value, error = null)
            SignupField.CONTACT -> _uiState.value.copy(contactNumber = value, error = null)
        }
    }
    
    fun updateRole(role: String) {
        _uiState.value = _uiState.value.copy(role = role)
    }
    
    fun signup(onSuccess: () -> Unit) {
        val state = _uiState.value
        
        // Validation
        if (state.email.isBlank() || state.password.isBlank() || state.username.isBlank()) {
            _uiState.value = state.copy(error = "Please fill in all required fields")
            return
        }
        
        if (state.password != state.confirmPassword) {
            _uiState.value = state.copy(error = "Passwords do not match")
            return
        }
        
        if (state.password.length < 6) {
            _uiState.value = state.copy(error = "Password must be at least 6 characters")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, error = null)
            
            authRepository.signup(
                email = state.email.trim(),
                password = state.password,
                username = state.username.trim(),
                role = state.role,
                firstName = state.firstName.takeIf { it.isNotBlank() },
                lastName = state.lastName.takeIf { it.isNotBlank() },
                contactNumber = state.contactNumber.takeIf { it.isNotBlank() }
            )
                .onSuccess { user ->
                    _uiState.value = _uiState.value.copy(isLoading = false, user = user)
                    onSuccess()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Signup failed"
                    )
                }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun socialLogin(provider: String, providerId: String, email: String, name: String, picture: String? = null, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            authRepository.socialLogin(provider, providerId, email, name, picture)
                .onSuccess { user ->
                    _uiState.value = _uiState.value.copy(isLoading = false, user = user)
                    onSuccess()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "$provider signup failed"
                    )
                }
        }
    }

    fun saveOAuthTokens(accessToken: String, refreshToken: String, userId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            authRepository.saveOAuthTokens(accessToken, refreshToken, userId)
            _uiState.value = _uiState.value.copy(isLoading = false)
            onSuccess()
        }
    }
}

enum class SignupField {
    EMAIL, PASSWORD, CONFIRM_PASSWORD, USERNAME, FIRST_NAME, LAST_NAME, CONTACT
}

data class SignupUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val username: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val contactNumber: String = "",
    val role: String = "customer",
    val isLoading: Boolean = false,
    val error: String? = null,
    val user: User? = null
)
