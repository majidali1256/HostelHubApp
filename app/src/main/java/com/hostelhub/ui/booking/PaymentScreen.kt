package com.hostelhub.ui.booking

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hostelhub.data.model.*
import com.hostelhub.data.repository.BookingRepository
import com.hostelhub.data.repository.UserRepository
import com.hostelhub.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentInstructionsScreen(
    bookingId: String,
    ownerName: String = "Property Owner",
    bankName: String = "HBL",
    accountTitle: String = "Property Owner",
    accountNumber: String = "1234567890",
    amount: Double = 15000.0,
    onNavigateBack: () -> Unit,
    onPaymentUploaded: () -> Unit,
    viewModel: PaymentViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    
    LaunchedEffect(bookingId) {
        viewModel.loadPaymentInfo(bookingId)
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.selectImage(uri)
    }

    val currentAmount = uiState.booking?.totalPrice?.takeIf { it > 0 } ?: amount
    val displayBank = uiState.bankDetails.bankName?.takeIf { it.isNotBlank() } ?: bankName
    val displayTitle = uiState.bankDetails.accountTitle?.takeIf { it.isNotBlank() } ?: accountTitle
    val displayAcc = uiState.bankDetails.accountNumber?.takeIf { it.isNotBlank() } ?: accountNumber
    val displayIban = uiState.bankDetails.iban
    val displayJazz = uiState.bankDetails.jazzCashNumber?.takeIf { it.isNotBlank() } ?: "03001234567"
    val displayEasy = uiState.bankDetails.easyPaisaNumber?.takeIf { it.isNotBlank() } ?: "03211234567"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payment Instructions & Receipt") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Amount Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Primary),
                    shape = RoundedCornerShape(26.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Amount to Pay",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextOnPrimary.copy(alpha = 0.8f)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Rs ${currentAmount.toInt()}",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = TextOnPrimary
                        )
                    }
                }
                
                Spacer(Modifier.height(20.dp))
                
                // Payment Methods Selection Chips
                Text(
                    "Select Payment Method",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilterChip(
                        selected = uiState.selectedPaymentMethod == PaymentMethod.BANK_TRANSFER,
                        onClick = { viewModel.updatePaymentMethod(PaymentMethod.BANK_TRANSFER) },
                        label = { Text("Bank Transfer") },
                        leadingIcon = { Icon(Icons.Default.AccountBalance, null, modifier = Modifier.size(16.dp)) }
                    )
                    FilterChip(
                        selected = uiState.selectedPaymentMethod == PaymentMethod.JAZZCASH,
                        onClick = { viewModel.updatePaymentMethod(PaymentMethod.JAZZCASH) },
                        label = { Text("JazzCash") },
                        leadingIcon = { Icon(Icons.Default.PhoneAndroid, null, modifier = Modifier.size(16.dp)) }
                    )
                    FilterChip(
                        selected = uiState.selectedPaymentMethod == PaymentMethod.EASYPAISA,
                        onClick = { viewModel.updatePaymentMethod(PaymentMethod.EASYPAISA) },
                        label = { Text("EasyPaisa") },
                        leadingIcon = { Icon(Icons.Default.PhoneAndroid, null, modifier = Modifier.size(16.dp)) }
                    )
                }
                
                Spacer(Modifier.height(16.dp))
                
                // Active Method Details Card
                when (uiState.selectedPaymentMethod) {
                    PaymentMethod.BANK_TRANSFER -> {
                        val details = mutableListOf(
                            "Bank Name" to displayBank,
                            "Account Title" to displayTitle,
                            "Account Number" to displayAcc
                        )
                        if (!displayIban.isNullOrBlank()) details.add("IBAN" to displayIban)
                        
                        PaymentMethodCard(
                            title = "Bank Account Transfer",
                            icon = Icons.Default.AccountBalance,
                            details = details,
                            onCopy = { clipboardManager.setText(AnnotatedString(displayAcc)) }
                        )
                    }
                    PaymentMethod.JAZZCASH -> {
                        PaymentMethodCard(
                            title = "JazzCash Mobile Wallet",
                            icon = Icons.Default.PhoneAndroid,
                            details = listOf(
                                "Account Title" to displayTitle,
                                "JazzCash Number" to displayJazz
                            ),
                            onCopy = { clipboardManager.setText(AnnotatedString(displayJazz)) }
                        )
                    }
                    PaymentMethod.EASYPAISA -> {
                        PaymentMethodCard(
                            title = "EasyPaisa Mobile Wallet",
                            icon = Icons.Default.PhoneAndroid,
                            details = listOf(
                                "Account Title" to displayTitle,
                                "EasyPaisa Number" to displayEasy
                            ),
                            onCopy = { clipboardManager.setText(AnnotatedString(displayEasy)) }
                        )
                    }
                }
                
                Spacer(Modifier.height(20.dp))
                
                // Instructions
                Card(
                    colors = CardDefaults.cardColors(containerColor = Warning.copy(alpha = 0.1f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, null, tint = Warning)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Important Instructions",
                                fontWeight = FontWeight.SemiBold,
                                color = Warning
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "1. Transfer the exact amount to the selected account above.\n" +
                            "2. Take a screenshot or picture of your payment receipt.\n" +
                            "3. Enter the Transaction ID (TID) and upload the receipt below.\n" +
                            "4. Once verified by the property owner, your room is secured!",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                
                Spacer(Modifier.height(24.dp))
                
                // Upload Receipt Modal Section
                Text(
                    "Upload Payment Receipt",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(12.dp))
                
                if (uiState.receiptUploaded) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Success.copy(alpha = 0.1f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, null, tint = Success, modifier = Modifier.size(32.dp))
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Receipt Submitted Successfully", fontWeight = FontWeight.Bold, color = Success)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Your payment receipt is under review by the hostel owner. You will receive a notification once verified.",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = onPaymentUploaded,
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = CircleShape
                    ) {
                        Text("Back to Booking Details", fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    OutlinedTextField(
                        value = uiState.transactionId,
                        onValueChange = viewModel::updateTransactionId,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Transaction ID (TID / Ref #)") },
                        placeholder = { Text("e.g. TRX987654321") },
                        leadingIcon = { Icon(Icons.Default.Receipt, null) },
                        shape = CircleShape
                    )
                    
                    Spacer(Modifier.height(12.dp))
                    
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { imagePickerLauncher.launch("image/*") },
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = if (uiState.selectedImageUri != null) Primary.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (uiState.selectedImageUri != null) Icons.Default.Image else Icons.Default.CloudUpload,
                                null,
                                tint = Primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (uiState.selectedImageUri != null) "Receipt Image Selected" else "Tap to Pick Receipt Image",
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (uiState.selectedImageUri != null) Primary else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (uiState.selectedImageUri != null) uiState.selectedImageUri!!.lastPathSegment ?: "image.jpg" else "Supports PNG, JPG screenshot",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                            if (uiState.selectedImageUri != null) {
                                Icon(Icons.Default.CheckCircle, null, tint = Success)
                            }
                        }
                    }
                    
                    uiState.error?.let { err ->
                        Spacer(Modifier.height(12.dp))
                        Card(colors = CardDefaults.cardColors(containerColor = Error.copy(alpha = 0.1f))) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Error, null, tint = Error)
                                Spacer(Modifier.width(8.dp))
                                Text(err, color = Error, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(24.dp))
                    
                    Button(
                        onClick = { viewModel.uploadReceipt(bookingId, onPaymentUploaded) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = CircleShape,
                        enabled = !uiState.isSubmitting && uiState.transactionId.isNotBlank() && uiState.selectedImageUri != null
                    ) {
                        if (uiState.isSubmitting) {
                            CircularProgressIndicator(Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                            Spacer(Modifier.width(8.dp))
                            Text("Uploading Receipt...")
                        } else {
                            Icon(Icons.Default.Send, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Submit Payment Receipt", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentMethodCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    details: List<Pair<String, String>>,
    onCopy: () -> Unit
) {
    var showCopied by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = Primary)
                Spacer(Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.weight(1f))
                TextButton(onClick = {
                    onCopy()
                    showCopied = true
                }) {
                    Icon(
                        if (showCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                        null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(if (showCopied) "Copied" else "Copy")
                }
            }
            Spacer(Modifier.height(8.dp))
            details.forEach { (label, value) ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        label,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
    
    LaunchedEffect(showCopied) {
        if (showCopied) {
            kotlinx.coroutines.delay(2000)
            showCopied = false
        }
    }
}

@HiltViewModel
class PaymentViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bookingRepository: BookingRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    fun loadPaymentInfo(bookingId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            bookingRepository.getBookingById(bookingId)
                .onSuccess { booking ->
                    val isUploaded = booking.paymentStatus == PaymentStatus.SUBMITTED || booking.paymentStatus == PaymentStatus.VERIFIED
                    val hostelId = booking.hostelId?.id
                    
                    // Fetch bank details for owner if available
                    if (!hostelId.isNullOrBlank()) {
                        userRepository.getBankDetails(hostelId)
                            .onSuccess { bankDetails ->
                                _uiState.value = _uiState.value.copy(
                                    booking = booking,
                                    bankDetails = bankDetails,
                                    receiptUploaded = isUploaded,
                                    isLoading = false
                                )
                            }
                            .onFailure {
                                _uiState.value = _uiState.value.copy(
                                    booking = booking,
                                    receiptUploaded = isUploaded,
                                    isLoading = false
                                )
                            }
                    } else {
                        _uiState.value = _uiState.value.copy(
                            booking = booking,
                            receiptUploaded = isUploaded,
                            isLoading = false
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load booking details"
                    )
                }
        }
    }

    fun selectImage(uri: Uri?) {
        if (uri != null) {
            _uiState.value = _uiState.value.copy(selectedImageUri = uri, error = null)
        }
    }

    fun updateTransactionId(v: String) {
        _uiState.value = _uiState.value.copy(transactionId = v, error = null)
    }

    fun updatePaymentMethod(method: String) {
        _uiState.value = _uiState.value.copy(selectedPaymentMethod = method)
    }

    fun uploadReceipt(bookingId: String, onSuccessCallback: () -> Unit) {
        val state = _uiState.value
        val uri = state.selectedImageUri
        val tid = state.transactionId.trim()

        if (uri == null || tid.isBlank()) {
            _uiState.value = state.copy(error = "Please enter Transaction ID and select a receipt image.")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isSubmitting = true, error = null)
            try {
                val tempFile = File(context.cacheDir, "receipt_${System.currentTimeMillis()}.jpg")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(tempFile).use { output ->
                        input.copyTo(output)
                    }
                }

                bookingRepository.uploadPaymentReceipt(
                    bookingId = bookingId,
                    receiptFile = tempFile,
                    transactionId = tid,
                    paymentMethod = state.selectedPaymentMethod
                ).onSuccess {
                    tempFile.delete()
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        receiptUploaded = true
                    )
                    onSuccessCallback()
                }.onFailure { err ->
                    tempFile.delete()
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        error = err.message ?: "Failed to upload payment receipt"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    error = e.message ?: "Error preparing image file"
                )
            }
        }
    }
}

data class PaymentUiState(
    val booking: Booking? = null,
    val bankDetails: BankDetails = BankDetails(),
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val receiptUploaded: Boolean = false,
    val transactionId: String = "",
    val selectedPaymentMethod: String = PaymentMethod.BANK_TRANSFER,
    val selectedImageUri: Uri? = null,
    val error: String? = null
)
