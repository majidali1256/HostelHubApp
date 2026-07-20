package com.hostelhub.ui.booking

import androidx.compose.animation.*
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
import com.hostelhub.data.model.BookingStatus
import com.hostelhub.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDetailScreen(
    bookingId: String,
    onNavigateBack: () -> Unit,
    onNavigateToPayment: () -> Unit,
    onNavigateToAgreements: () -> Unit = {},
    onNavigateToChat: (String) -> Unit,
    onCancelBooking: () -> Unit
) {
    var showCancelDialog by remember { mutableStateOf(false) }
    
    // Sample booking data
    val booking = remember {
        mapOf(
            "hostelName" to "Sunrise Hostel",
            "location" to "Gulberg III, Lahore",
            "checkIn" to "Jan 15, 2024",
            "checkOut" to "Feb 15, 2024",
            "roomType" to "Double Sharing",
            "price" to 15000,
            "status" to BookingStatus.CONFIRMED,
            "ownerName" to "Muhammad Ali",
            "ownerPhone" to "+92 300 1234567"
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Booking Details") },
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
            // Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Success.copy(alpha = 0.1f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, null, tint = Success)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Booking Confirmed", fontWeight = FontWeight.SemiBold, color = Success)
                        Text("Your booking has been confirmed by the owner", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            
            // Hostel Info
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        booking["hostelName"] as String,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(16.dp), tint = Primary)
                        Spacer(Modifier.width(4.dp))
                        Text(booking["location"] as String, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            
            // Booking Details
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Booking Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(12.dp))
                    
                    DetailRow(icon = Icons.Default.CalendarToday, label = "Check-in", value = booking["checkIn"] as String)
                    DetailRow(icon = Icons.Default.CalendarToday, label = "Check-out", value = booking["checkOut"] as String)
                    DetailRow(icon = Icons.Default.Bed, label = "Room Type", value = booking["roomType"] as String)
                    
                    Divider(modifier = Modifier.padding(vertical = 12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Amount", fontWeight = FontWeight.SemiBold)
                        Text(
                            "Rs ${booking["price"]}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                    }
                }
            }
            
            // Owner Contact
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Property Owner", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(12.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = Primary.copy(alpha = 0.1f),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    (booking["ownerName"] as String).first().uppercase(),
                                    color = Primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(booking["ownerName"] as String, fontWeight = FontWeight.Medium)
                            Text(
                                booking["ownerPhone"] as String,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        IconButton(onClick = { onNavigateToChat("owner-conversation-id") }) {
                            Icon(Icons.Default.Chat, null, tint = Primary)
                        }
                    }
                }
            }
            
            // Action Buttons
            Button(
                onClick = onNavigateToAgreements,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Secondary)
            ) {
                Icon(Icons.Default.Description, null)
                Spacer(Modifier.width(8.dp))
                Text("Digital Tenancy Agreement & E-Sign", fontWeight = FontWeight.SemiBold)
            }

            Button(
                onClick = onNavigateToPayment,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Payment, null)
                Spacer(Modifier.width(8.dp))
                Text("View Payment Details", fontWeight = FontWeight.SemiBold)
            }
            
            OutlinedButton(
                onClick = { showCancelDialog = true },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Error)
            ) {
                Icon(Icons.Default.Cancel, null)
                Spacer(Modifier.width(8.dp))
                Text("Cancel Booking")
            }
        }
    }
    
    // Cancel Dialog
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            icon = { Icon(Icons.Default.Warning, null, tint = Warning) },
            title = { Text("Cancel Booking?") },
            text = { Text("Are you sure you want to cancel this booking? Cancellation fees may apply.") },
            confirmButton = {
                Button(
                    onClick = {
                        showCancelDialog = false
                        onCancelBooking()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Error)
                ) {
                    Text("Yes, Cancel")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("No, Keep Booking")
                }
            }
        )
    }
}

@Composable
private fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, modifier = Modifier.size(20.dp), tint = Primary)
        Spacer(Modifier.width(12.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.width(80.dp)
        )
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}
