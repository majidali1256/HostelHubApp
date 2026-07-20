package com.hostelhub.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hostelhub.data.local.TokenManager
import com.hostelhub.data.model.BankDetails
import com.hostelhub.data.model.User
import com.hostelhub.data.repository.UserRepository
import com.hostelhub.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit,
    onSaveSuccess: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.saveProfile(onSaveSuccess) },
                        enabled = !uiState.isLoading
                    ) {
                        Text("Save")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoadingProfile) {
            Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Username
                OutlinedTextField(
                    value = uiState.username,
                    onValueChange = viewModel::updateUsername,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Username") },
                    leadingIcon = { Icon(Icons.Default.Person, null) },
                    shape = RoundedCornerShape(12.dp)
                )
                
                // First Name
                OutlinedTextField(
                    value = uiState.firstName,
                    onValueChange = viewModel::updateFirstName,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("First Name") },
                    shape = RoundedCornerShape(12.dp)
                )
                
                // Last Name
                OutlinedTextField(
                    value = uiState.lastName,
                    onValueChange = viewModel::updateLastName,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Last Name") },
                    shape = RoundedCornerShape(12.dp)
                )
                
                // Email (read-only)
                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Email") },
                    leadingIcon = { Icon(Icons.Default.Email, null) },
                    enabled = false,
                    shape = RoundedCornerShape(12.dp)
                )
                
                // Contact Number
                OutlinedTextField(
                    value = uiState.contactNumber,
                    onValueChange = viewModel::updateContactNumber,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Phone Number") },
                    leadingIcon = { Icon(Icons.Default.Phone, null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(12.dp)
                )
                
                // Error
                uiState.error?.let { error ->
                    Card(colors = CardDefaults.cardColors(containerColor = Error.copy(alpha = 0.1f))) {
                        Row(Modifier.padding(12.dp)) {
                            Icon(Icons.Default.Error, null, tint = Error)
                            Spacer(Modifier.width(8.dp))
                            Text(error, color = Error)
                        }
                    }
                }
                
                // Success
                uiState.successMessage?.let { message ->
                    Card(colors = CardDefaults.cardColors(containerColor = Success.copy(alpha = 0.1f))) {
                        Row(Modifier.padding(12.dp)) {
                            Icon(Icons.Default.CheckCircle, null, tint = Success)
                            Spacer(Modifier.width(8.dp))
                            Text(message, color = Success)
                        }
                    }
                }
                
                Spacer(Modifier.height(8.dp))
                
                // Save Button
                Button(
                    onClick = { viewModel.saveProfile(onSaveSuccess) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Icon(Icons.Default.Save, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Save Changes", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val tokenManager: TokenManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()
    
    private var userId: String = ""
    
    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingProfile = true)
            
            userId = tokenManager.userId.first() ?: ""
            if (userId.isNotEmpty()) {
                userRepository.getUserById(userId)
                    .onSuccess { user ->
                        _uiState.value = _uiState.value.copy(
                            isLoadingProfile = false,
                            username = user.username,
                            firstName = user.firstName ?: "",
                            lastName = user.lastName ?: "",
                            email = user.email,
                            contactNumber = user.contactNumber ?: ""
                        )
                    }
                    .onFailure { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoadingProfile = false,
                            error = error.message
                        )
                    }
            }
        }
    }
    
    fun updateUsername(value: String) { _uiState.value = _uiState.value.copy(username = value, error = null) }
    fun updateFirstName(value: String) { _uiState.value = _uiState.value.copy(firstName = value, error = null) }
    fun updateLastName(value: String) { _uiState.value = _uiState.value.copy(lastName = value, error = null) }
    fun updateContactNumber(value: String) { _uiState.value = _uiState.value.copy(contactNumber = value, error = null) }
    
    fun saveProfile(onSuccess: () -> Unit) {
        if (userId.isEmpty()) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val updates = mapOf(
                "username" to _uiState.value.username,
                "firstName" to _uiState.value.firstName,
                "lastName" to _uiState.value.lastName,
                "contactNumber" to _uiState.value.contactNumber
            )
            
            userRepository.updateUser(userId, updates)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Profile updated successfully"
                    )
                    onSuccess()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to update profile"
                    )
                }
        }
    }
}

data class EditProfileUiState(
    val username: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val contactNumber: String = "",
    val isLoadingProfile: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

// Bank Details Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankDetailsScreen(
    onNavigateBack: () -> Unit,
    viewModel: BankDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bank Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Bank Account Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            OutlinedTextField(
                value = uiState.bankName,
                onValueChange = viewModel::updateBankName,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Bank Name") },
                leadingIcon = { Icon(Icons.Default.AccountBalance, null) },
                shape = RoundedCornerShape(12.dp)
            )
            
            OutlinedTextField(
                value = uiState.accountTitle,
                onValueChange = viewModel::updateAccountTitle,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Account Title") },
                leadingIcon = { Icon(Icons.Default.Person, null) },
                shape = RoundedCornerShape(12.dp)
            )
            
            OutlinedTextField(
                value = uiState.accountNumber,
                onValueChange = viewModel::updateAccountNumber,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Account Number") },
                leadingIcon = { Icon(Icons.Default.Numbers, null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp)
            )
            
            OutlinedTextField(
                value = uiState.iban,
                onValueChange = viewModel::updateIban,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("IBAN (Optional)") },
                shape = RoundedCornerShape(12.dp)
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                "Mobile Wallets",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            OutlinedTextField(
                value = uiState.jazzCashNumber,
                onValueChange = viewModel::updateJazzCash,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("JazzCash Number") },
                leadingIcon = { Icon(Icons.Default.PhoneAndroid, null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                shape = RoundedCornerShape(12.dp)
            )
            
            OutlinedTextField(
                value = uiState.easyPaisaNumber,
                onValueChange = viewModel::updateEasyPaisa,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("EasyPaisa Number") },
                leadingIcon = { Icon(Icons.Default.PhoneAndroid, null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                shape = RoundedCornerShape(12.dp)
            )
            
            // Error/Success
            uiState.error?.let {
                Card(colors = CardDefaults.cardColors(containerColor = Error.copy(alpha = 0.1f))) {
                    Row(Modifier.padding(12.dp)) {
                        Icon(Icons.Default.Error, null, tint = Error)
                        Spacer(Modifier.width(8.dp))
                        Text(it, color = Error)
                    }
                }
            }
            
            uiState.successMessage?.let {
                Card(colors = CardDefaults.cardColors(containerColor = Success.copy(alpha = 0.1f))) {
                    Row(Modifier.padding(12.dp)) {
                        Icon(Icons.Default.CheckCircle, null, tint = Success)
                        Spacer(Modifier.width(8.dp))
                        Text(it, color = Success)
                    }
                }
            }
            
            Spacer(Modifier.height(8.dp))
            
            Button(
                onClick = viewModel::saveBankDetails,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Icon(Icons.Default.Save, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Save Bank Details", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@HiltViewModel
class BankDetailsViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(BankDetailsUiState())
    val uiState: StateFlow<BankDetailsUiState> = _uiState.asStateFlow()
    
    init {
        loadBankDetails()
    }

    fun loadBankDetails() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            userRepository.getMyBankDetails()
                .onSuccess { details ->
                    _uiState.value = _uiState.value.copy(
                        bankName = details.bankName ?: "",
                        accountTitle = details.accountTitle ?: "",
                        accountNumber = details.accountNumber ?: "",
                        iban = details.iban ?: "",
                        jazzCashNumber = details.jazzCashNumber ?: "",
                        easyPaisaNumber = details.easyPaisaNumber ?: "",
                        isLoading = false
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
        }
    }

    fun updateBankName(v: String) { _uiState.value = _uiState.value.copy(bankName = v, error = null, successMessage = null) }
    fun updateAccountTitle(v: String) { _uiState.value = _uiState.value.copy(accountTitle = v, error = null, successMessage = null) }
    fun updateAccountNumber(v: String) { _uiState.value = _uiState.value.copy(accountNumber = v, error = null, successMessage = null) }
    fun updateIban(v: String) { _uiState.value = _uiState.value.copy(iban = v, error = null, successMessage = null) }
    fun updateJazzCash(v: String) { _uiState.value = _uiState.value.copy(jazzCashNumber = v, error = null, successMessage = null) }
    fun updateEasyPaisa(v: String) { _uiState.value = _uiState.value.copy(easyPaisaNumber = v, error = null, successMessage = null) }
    
    fun saveBankDetails() {
        val state = _uiState.value
        
        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, error = null)
            
            val bankDetails = BankDetails(
                bankName = state.bankName.takeIf { it.isNotBlank() },
                accountTitle = state.accountTitle.takeIf { it.isNotBlank() },
                accountNumber = state.accountNumber.takeIf { it.isNotBlank() },
                iban = state.iban.takeIf { it.isNotBlank() },
                jazzCashNumber = state.jazzCashNumber.takeIf { it.isNotBlank() },
                easyPaisaNumber = state.easyPaisaNumber.takeIf { it.isNotBlank() }
            )
            
            userRepository.updateBankDetails(bankDetails)
                .onSuccess { message ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = message
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to save bank details"
                    )
                }
        }
    }
}

data class BankDetailsUiState(
    val bankName: String = "",
    val accountTitle: String = "",
    val accountNumber: String = "",
    val iban: String = "",
    val jazzCashNumber: String = "",
    val easyPaisaNumber: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)
