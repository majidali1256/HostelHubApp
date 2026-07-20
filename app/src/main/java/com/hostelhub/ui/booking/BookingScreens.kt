package com.hostelhub.ui.booking

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hostelhub.ui.components.EmptyState
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
    val isDark = isSystemInDarkTheme()
    
    LaunchedEffect(hostelId) {
        viewModel.loadHostel(hostelId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Reserve Space", 
                        style = MaterialTheme.typography.titleLarge, 
                        fontWeight = FontWeight.ExtraBold
                    ) 
                },
                navigationIcon = {
                    Surface(
                        onClick = onNavigateBack,
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.ArrowBack, "Back", modifier = Modifier.size(20.dp))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            // Property Info Card (~26px TripGlide Card)
            uiState.hostel?.let { hostel ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = if (isDark) 0.dp else 8.dp,
                            shape = RoundedCornerShape(26.dp),
                            spotColor = Color.Black.copy(alpha = 0.15f)
                        ),
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Primary.copy(alpha = 0.12f),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Hotel, null, tint = Primary, modifier = Modifier.size(22.dp))
                            }
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                hostel.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, null, tint = Primary, modifier = Modifier.size(13.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    hostel.location,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                        Surface(shape = CircleShape, color = Primary) {
                            Text(
                                "Rs ${hostel.price.toInt()}/mo",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextOnPrimary,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
            
            // Check-in Date Selector Pill Card
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Check-in Date", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold)
                Card(
                    onClick = { isCheckIn = true; showDatePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(shape = CircleShape, color = Primary.copy(alpha = 0.12f), modifier = Modifier.size(40.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.CalendarMonth, null, tint = Primary, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Selected Check-in", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            Text(
                                uiState.checkInDate?.let {
                                    java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.US).format(it)
                                } ?: "Select date",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Icon(Icons.Default.EditCalendar, null, tint = Primary, modifier = Modifier.size(20.dp))
                    }
                }
            }
            
            // Check-out Date Selector Pill Card
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Check-out Date", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold)
                Card(
                    onClick = { isCheckIn = false; showDatePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(shape = CircleShape, color = Secondary.copy(alpha = 0.12f), modifier = Modifier.size(40.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.CalendarMonth, null, tint = Secondary, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Selected Check-out", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            Text(
                                uiState.checkOutDate?.let {
                                    java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.US).format(it)
                                } ?: "Select date",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Icon(Icons.Default.EditCalendar, null, tint = Secondary, modifier = Modifier.size(20.dp))
                    }
                }
            }
            
            // Notes Input Field
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Special Requests (Optional)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold)
                OutlinedTextField(
                    value = uiState.notes,
                    onValueChange = viewModel::updateNotes,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g., preference for bottom bunk, dietary requirements...") },
                    minLines = 3,
                    shape = RoundedCornerShape(22.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                        focusedBorderColor = Primary
                    )
                )
            }
            
            // Price Summary Card (~26px TripGlide elevated container)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = if (isDark) 0.dp else 6.dp,
                        shape = RoundedCornerShape(26.dp),
                        spotColor = Primary.copy(alpha = 0.2f)
                    ),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = if (isDark) 0.18f else 0.08f)),
                border = BorderStroke(1.dp, Primary.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Receipt, null, tint = Primary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Reservation Price Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = Primary)
                    }
                    Spacer(Modifier.height(14.dp))
                    
                    uiState.hostel?.let { hostel ->
                        val checkIn = uiState.checkInDate
                        val checkOut = uiState.checkOutDate
                        val months = if (checkIn != null && checkOut != null) {
                            val diffInMillis = checkOut.time - checkIn.time
                            val diffInDays = diffInMillis / (1000 * 60 * 60 * 24)
                            (diffInDays / 30).coerceAtLeast(1)
                        } else 1L

                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Monthly Room Tariff", style = MaterialTheme.typography.bodyMedium)
                            Text("Rs ${hostel.price.toInt()}", fontWeight = FontWeight.Medium)
                        }
                        Spacer(Modifier.height(6.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Stay Duration", style = MaterialTheme.typography.bodyMedium)
                            Text("$months month(s)", fontWeight = FontWeight.Medium)
                        }
                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = Primary.copy(alpha = 0.25f))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Total Estimated Payable", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
                            Surface(shape = CircleShape, color = Primary) {
                                Text(
                                    "Rs ${(hostel.price * months).toInt()}", 
                                    fontWeight = FontWeight.ExtraBold, 
                                    color = TextOnPrimary,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            // Error Card
            uiState.error?.let { error ->
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Error.copy(alpha = 0.12f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Error, null, tint = Error)
                        Spacer(Modifier.width(10.dp))
                        Text(error, color = Error, fontWeight = FontWeight.Medium)
                    }
                }
            }
            
            // Confirm Booking Button (TripGlide Pill Button)
            Button(
                onClick = { viewModel.createBooking(onBookingSuccess) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = TextOnPrimary),
                elevation = if (isDark) ButtonDefaults.buttonElevation(0.dp) else ButtonDefaults.buttonElevation(8.dp),
                enabled = !uiState.isLoading && uiState.checkInDate != null && uiState.checkOutDate != null
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.5.dp)
                } else {
                    Icon(Icons.Default.BookOnline, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("Confirm Space Reservation", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
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
                    Text("Confirm Date", fontWeight = FontWeight.Bold, color = Primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
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
    val isDark = isSystemInDarkTheme()
    
    LaunchedEffect(Unit) {
        viewModel.loadBookings()
    }
    
    com.hostelhub.ui.components.navigation.HostelHubScaffold(
        title = "My Reservations",
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
                    CircularProgressIndicator(color = Primary, strokeWidth = 3.dp)
                }
            }
            uiState.bookings.isEmpty() -> {
                Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    EmptyState(
                        icon = Icons.Default.BookOnline,
                        title = "No Reservations Yet",
                        subtitle = "You haven't made any booking reservations. Explore properties and book your ideal stay today.",
                        actionLabel = "Explore Hostels",
                        onAction = onNavigateBack
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(uiState.bookings.size) { index ->
                        val booking = uiState.bookings[index]
                        Card(
                            onClick = { onNavigateToBookingDetail(booking.id) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(
                                    elevation = if (isDark) 0.dp else 8.dp,
                                    shape = RoundedCornerShape(26.dp),
                                    spotColor = Color.Black.copy(alpha = 0.12f)
                                ),
                            shape = RoundedCornerShape(26.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Surface(shape = CircleShape, color = Primary.copy(alpha = 0.12f), modifier = Modifier.size(44.dp)) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(Icons.Default.Hotel, null, tint = Primary, modifier = Modifier.size(20.dp))
                                            }
                                        }
                                        Spacer(Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                booking.hostelId?.name ?: "Property Reservation",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.ExtraBold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                booking.hostelId?.location ?: "Location not specified",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    StatusChip(booking.status.name)
                                }
                                
                                Spacer(Modifier.height(16.dp))
                                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                                Spacer(Modifier.height(14.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Check-in Date", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                        Spacer(Modifier.height(2.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.CalendarToday, null, tint = Primary, modifier = Modifier.size(14.dp))
                                            Spacer(Modifier.width(6.dp))
                                            Text(booking.checkIn, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Check-out Date", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                        Spacer(Modifier.height(2.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.CalendarToday, null, tint = Secondary, modifier = Modifier.size(14.dp))
                                            Spacer(Modifier.width(6.dp))
                                            Text(booking.checkOut, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
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
}

@Composable
private fun StatusChip(status: String) {
    val (color, bgColor) = when (status.lowercase()) {
        "confirmed" -> Success to Success.copy(alpha = 0.15f)
        "pending" -> Warning to Warning.copy(alpha = 0.15f)
        "cancelled", "rejected" -> Error to Error.copy(alpha = 0.15f)
        else -> Secondary to Secondary.copy(alpha = 0.15f)
    }
    
    Surface(
        shape = CircleShape, 
        color = bgColor,
        border = BorderStroke(1.dp, color.copy(alpha = 0.35f))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(color, CircleShape)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                status.uppercase(),
                color = color,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}
