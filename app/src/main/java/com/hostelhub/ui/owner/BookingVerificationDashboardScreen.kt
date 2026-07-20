package com.hostelhub.ui.owner

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.hostelhub.data.model.*
import com.hostelhub.data.repository.BookingRepository
import com.hostelhub.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingVerificationDashboardScreen(
    onNavigateBack: () -> Unit,
    viewModel: BookingVerificationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Pending Review, 1 = Verified, 2 = All
    var previewReceiptBooking by remember { mutableStateOf<Booking?>(null) }
    var rejectBookingDialog by remember { mutableStateOf<Booking?>(null) }
    var rejectionReasonText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadSubmittedBookings()
    }

    val filteredList = remember(uiState.bookings, selectedTab) {
        when (selectedTab) {
            0 -> uiState.bookings.filter { it.paymentStatus == PaymentStatus.SUBMITTED || (it.paymentReceipt != null && !it.paymentReceipt.verified && it.paymentStatus != PaymentStatus.REJECTED) }
            1 -> uiState.bookings.filter { it.paymentStatus == PaymentStatus.VERIFIED || (it.paymentReceipt?.verified == true) }
            else -> uiState.bookings.filter { it.paymentReceipt != null || it.paymentStatus != PaymentStatus.PENDING }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payment Verification") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadSubmittedBookings() }) {
                        Icon(Icons.Default.Refresh, "Refresh")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            
            // Summary Banner
            Card(
                colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.VerifiedUser, null, tint = Primary, modifier = Modifier.size(32.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Owner Payment Verification Hub", fontWeight = FontWeight.Bold, color = Primary)
                        Text(
                            "Review student payment receipts and verify transactions to confirm bookings.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Filter Tabs
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        val count = uiState.bookings.count { it.paymentStatus == PaymentStatus.SUBMITTED }
                        Text("Pending Review ($count)", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal)
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Verified", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("All Receipts", fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) }
                )
            }

            Spacer(Modifier.height(12.dp))

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary)
                }
            } else if (filteredList.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.ReceiptLong,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = when (selectedTab) {
                                0 -> "No receipts pending verification."
                                1 -> "No verified payment receipts yet."
                                else -> "No payment receipts found."
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(filteredList) { booking ->
                        BookingReceiptCard(
                            booking = booking,
                            onViewReceipt = { previewReceiptBooking = booking },
                            onVerify = { viewModel.verifyPayment(booking.id, true, null) },
                            onReject = {
                                rejectionReasonText = ""
                                rejectBookingDialog = booking
                            },
                            isVerifying = uiState.isVerifying
                        )
                    }
                }
            }
        }
    }

    // Receipt Preview Modal
    previewReceiptBooking?.let { booking ->
        Dialog(onDismissRequest = { previewReceiptBooking = null }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 600.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Payment Receipt Preview", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        IconButton(onClick = { previewReceiptBooking = null }) {
                            Icon(Icons.Default.Close, null)
                        }
                    }
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    val receiptUrl = booking.paymentReceipt?.image
                    if (!receiptUrl.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(380.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = 0.05f)),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = receiptUrl,
                                contentDescription = "Receipt Image",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No Image Available / Demo Receipt", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("TID / Ref:", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Text(booking.transactionId ?: "N/A", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Method:", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Text(booking.paymentMethod?.uppercase() ?: "BANK TRANSFER", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(Modifier.height(16.dp))
                    
                    if (booking.paymentStatus == PaymentStatus.SUBMITTED) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = {
                                    previewReceiptBooking = null
                                    rejectionReasonText = ""
                                    rejectBookingDialog = booking
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Error)
                            ) {
                                Text("Reject")
                            }
                            Button(
                                onClick = {
                                    previewReceiptBooking = null
                                    viewModel.verifyPayment(booking.id, true, null)
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Success)
                            ) {
                                Text("Verify & Confirm")
                            }
                        }
                    }
                }
            }
        }
    }

    // Rejection Reason Modal
    rejectBookingDialog?.let { booking ->
        AlertDialog(
            onDismissRequest = { rejectBookingDialog = null },
            title = { Text("Reject Payment Receipt") },
            text = {
                Column {
                    Text("Please specify the reason for rejecting this payment receipt so the student can correct and re-submit:")
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = rejectionReasonText,
                        onValueChange = { rejectionReasonText = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("e.g. Invalid Transaction ID, blurry receipt image, incorrect amount") },
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val reason = rejectionReasonText.takeIf { it.isNotBlank() } ?: "Payment receipt verification failed."
                        viewModel.verifyPayment(booking.id, false, reason)
                        rejectBookingDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Error)
                ) {
                    Text("Reject & Notify")
                }
            },
            dismissButton = {
                TextButton(onClick = { rejectBookingDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookingReceiptCard(
    booking: Booking,
    onViewReceipt: () -> Unit,
    onVerify: () -> Unit,
    onReject: () -> Unit,
    isVerifying: Boolean
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = booking.hostelId?.name ?: "Hostel Booking",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Badge(
                    containerColor = when (booking.paymentStatus) {
                        PaymentStatus.VERIFIED -> Success.copy(alpha = 0.15f)
                        PaymentStatus.SUBMITTED -> Warning.copy(alpha = 0.15f)
                        PaymentStatus.REJECTED -> Error.copy(alpha = 0.15f)
                        else -> Secondary.copy(alpha = 0.15f)
                    },
                    contentColor = when (booking.paymentStatus) {
                        PaymentStatus.VERIFIED -> Success
                        PaymentStatus.SUBMITTED -> Warning
                        PaymentStatus.REJECTED -> Error
                        else -> Secondary
                    }
                ) {
                    Text(
                        text = booking.paymentStatus.name,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            val studentName = listOfNotNull(booking.customerId?.firstName, booking.customerId?.lastName).joinToString(" ").takeIf { it.isNotBlank() } ?: "Student #${booking.customerId?.id?.take(6)}"
            Text(
                text = "Student: $studentName (${booking.customerId?.email ?: "N/A"})",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Check-in: ${booking.checkIn}  |  Amount: Rs ${booking.totalPrice.toInt()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            if (!booking.transactionId.isNullOrBlank()) {
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Transaction ID:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    Text(booking.transactionId, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Primary)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Method:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    Text(booking.paymentMethod?.uppercase() ?: "BANK TRANSFER", style = MaterialTheme.typography.bodySmall)
                }
            }

            if (!booking.paymentReceipt?.rejectionReason.isNullOrBlank()) {
                Spacer(Modifier.height(6.dp))
                Card(colors = CardDefaults.cardColors(containerColor = Error.copy(alpha = 0.1f))) {
                    Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ErrorOutline, null, tint = Error, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Rejection Note: ${booking.paymentReceipt?.rejectionReason}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Error
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = onViewReceipt,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Visibility, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("View Receipt")
                }

                if (booking.paymentStatus == PaymentStatus.SUBMITTED) {
                    Button(
                        onClick = onVerify,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Success),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isVerifying
                    ) {
                        Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Verify")
                    }
                    IconButton(
                        onClick = onReject,
                        colors = IconButtonDefaults.iconButtonColors(containerColor = Error.copy(alpha = 0.1f), contentColor = Error)
                    ) {
                        Icon(Icons.Default.Close, "Reject")
                    }
                }
            }
        }
    }
}

@HiltViewModel
class BookingVerificationViewModel @Inject constructor(
    private val bookingRepository: BookingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookingVerificationUiState())
    val uiState: StateFlow<BookingVerificationUiState> = _uiState.asStateFlow()

    fun loadSubmittedBookings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            bookingRepository.getMyHostelBookings()
                .onSuccess { bookings ->
                    _uiState.value = _uiState.value.copy(
                        bookings = bookings,
                        isLoading = false
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load bookings"
                    )
                }
        }
    }

    fun verifyPayment(bookingId: String, approved: Boolean, rejectionReason: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isVerifying = true)
            bookingRepository.verifyPayment(bookingId, approved, rejectionReason)
                .onSuccess {
                    // Refresh bookings list
                    loadSubmittedBookings()
                    _uiState.value = _uiState.value.copy(isVerifying = false)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isVerifying = false,
                        error = error.message ?: "Verification failed"
                    )
                }
        }
    }
}

data class BookingVerificationUiState(
    val bookings: List<Booking> = emptyList(),
    val isLoading: Boolean = false,
    val isVerifying: Boolean = false,
    val error: String? = null
)
