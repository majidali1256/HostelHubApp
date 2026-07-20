package com.hostelhub.ui.booking

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hostelhub.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingFormScreen(
    hostelId: String,
    onNavigateBack: () -> Unit,
    onBookingSuccess: () -> Unit,
    viewModel: BookingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }
    var isCheckIn by remember { mutableStateOf(true) }
    
    LaunchedEffect(hostelId) {
        viewModel.loadHostel(hostelId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Book Hostel") },
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
                .padding(16.dp)
        ) {
            // Hostel Info Card
            uiState.hostel?.let { hostel ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                hostel.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                hostel.location,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        Text(
                            "Rs ${hostel.price.toInt()}/mo",
                            style = MaterialTheme.typography.titleMedium,
                            color = Primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            // Check-in Date
            Text("Check-in Date", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            OutlinedCard(
                onClick = { isCheckIn = true; showDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CalendarMonth, null, tint = Primary)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        uiState.checkInDate?.let {
                            java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.US).format(it)
                        } ?: "Select date",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Check-out Date
            Text("Check-out Date", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            OutlinedCard(
                onClick = { isCheckIn = false; showDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CalendarMonth, null, tint = Primary)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        uiState.checkOutDate?.let {
                            java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.US).format(it)
                        } ?: "Select date",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Notes
            Text("Notes (Optional)", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = uiState.notes,
                onValueChange = viewModel::updateNotes,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Any special requests...") },
                minLines = 3,
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(Modifier.height(24.dp))
            
            // Price Summary
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Price Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(12.dp))
                    
                    uiState.hostel?.let { hostel ->
                        val checkIn = uiState.checkInDate
                        val checkOut = uiState.checkOutDate
                        val months = if (checkIn != null && checkOut != null) {
                            val diffInMillis = checkOut.time - checkIn.time
                            val diffInDays = diffInMillis / (1000 * 60 * 60 * 24)
                            (diffInDays / 30).coerceAtLeast(1)
                        } else 1L

                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Monthly Rent")
                            Text("Rs ${hostel.price.toInt()}")
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Duration")
                            Text("$months month(s)")
                        }
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total", fontWeight = FontWeight.Bold)
                            Text("Rs ${(hostel.price * months).toInt()}", fontWeight = FontWeight.Bold, color = Primary)
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            // Error
            uiState.error?.let { error ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Error.copy(alpha = 0.1f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(12.dp)) {
                        Icon(Icons.Default.Error, null, tint = Error)
                        Spacer(Modifier.width(8.dp))
                        Text(error, color = Error)
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
            
            // Book Button
            Button(
                onClick = { viewModel.createBooking(onBookingSuccess) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !uiState.isLoading && uiState.checkInDate != null && uiState.checkOutDate != null
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Icon(Icons.Default.BookOnline, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Confirm Booking", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
    
    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = System.currentTimeMillis()
        )
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val calendar = java.util.Calendar.getInstance()
                        calendar.timeInMillis = millis
                        val year = calendar.get(java.util.Calendar.YEAR)
                        val month = calendar.get(java.util.Calendar.MONTH)
                        val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
                        if (isCheckIn) viewModel.updateCheckInDate(year, month, day)
                        else viewModel.updateCheckOutDate(year, month, day)
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingHistoryScreen(
    onNavigateBack: () -> Unit,
    onNavigateToBookingDetail: (String) -> Unit,
    onNavigateRoute: (String) -> Unit = {},
    viewModel: BookingHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadBookings()
    }
    
    com.hostelhub.ui.components.navigation.HostelHubScaffold(
        title = "My Bookings",
        currentRoute = com.hostelhub.ui.navigation.Screen.BookingHistory.route,
        onNavigate = { route ->
            when (route) {
                com.hostelhub.ui.navigation.Screen.BookingHistory.route -> {}
                com.hostelhub.ui.navigation.Screen.Home.route -> onNavigateBack()
                else -> onNavigateRoute(route)
            }
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.bookings.isEmpty() -> {
                Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.BookOnline, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                        Spacer(Modifier.height(16.dp))
                        Text("No bookings yet", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
            else -> {
                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.bookings.size) { index ->
                        val booking = uiState.bookings[index]
                        Card(
                            onClick = { onNavigateToBookingDetail(booking.id) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            booking.hostelId?.name ?: "Hostel",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            booking.hostelId?.location ?: "",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                    StatusChip(booking.status.name)
                                }
                                Spacer(Modifier.height(12.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                                    Column {
                                        Text("Check-in", style = MaterialTheme.typography.labelSmall)
                                        Text(booking.checkIn, style = MaterialTheme.typography.bodyMedium)
                                    }
                                    Column {
                                        Text("Check-out", style = MaterialTheme.typography.labelSmall)
                                        Text(booking.checkOut, style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusChip(status: String) {
    val (color, bgColor) = when (status.lowercase()) {
        "confirmed" -> Success to Success.copy(alpha = 0.1f)
        "pending" -> Warning to Warning.copy(alpha = 0.1f)
        "cancelled", "rejected" -> Error to Error.copy(alpha = 0.1f)
        else -> Secondary to Secondary.copy(alpha = 0.1f)
    }
    
    Surface(shape = RoundedCornerShape(8.dp), color = bgColor) {
        Text(
            status,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            color = color,
            style = MaterialTheme.typography.labelMedium
        )
    }
}
