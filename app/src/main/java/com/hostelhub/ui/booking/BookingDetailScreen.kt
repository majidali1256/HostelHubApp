package com.hostelhub.ui.booking

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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
    val isDark = isSystemInDarkTheme()
    
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
                title = { 
                    Text("Reservation Details", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold) 
                },
                navigationIcon = {
                    Surface(
                        onClick = onNavigateBack,
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.padding(start = 8.dp).size(40.dp)
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
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Status Banner Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = if (isDark) 0.dp else 6.dp,
                        shape = RoundedCornerShape(24.dp),
                        spotColor = Success.copy(alpha = 0.25f)
                    ),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Success.copy(alpha = if (isDark) 0.18f else 0.1f)),
                border = BorderStroke(1.dp, Success.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(shape = CircleShape, color = Success, modifier = Modifier.size(42.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.CheckCircle, null, tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text("Reservation Confirmed", fontWeight = FontWeight.ExtraBold, color = Success, style = MaterialTheme.typography.titleMedium)
                        Text("Your space has been verified and confirmed by the host.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                    }
                }
            }
            
            // Property Info Header Card
            Card(
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
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        booking["hostelName"] as String,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(16.dp), tint = Primary)
                        Spacer(Modifier.width(6.dp))
                        Text(booking["location"] as String, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f))
                    }
                }
            }
            
            // Booking Details Summary Card
            Card(
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
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Stay Schedule & Room", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.height(14.dp))
                    
                    DetailRow(icon = Icons.Default.CalendarToday, label = "Check-in", value = booking["checkIn"] as String)
                    DetailRow(icon = Icons.Default.CalendarToday, label = "Check-out", value = booking["checkOut"] as String)
                    DetailRow(icon = Icons.Default.Bed, label = "Room Type", value = booking["roomType"] as String)
                    
                    Divider(modifier = Modifier.padding(vertical = 14.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total Tariff Paid", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
                        Surface(shape = CircleShape, color = Primary) {
                            Text(
                                "Rs ${booking["price"]}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextOnPrimary,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
            
            // Owner Contact Card
            Card(
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
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Property Concierge / Host", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.height(14.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Primary.copy(alpha = 0.15f),
                            modifier = Modifier.size(52.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    (booking["ownerName"] as String).first().uppercase(),
                                    color = Primary,
                                    fontWeight = FontWeight.ExtraBold,
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(booking["ownerName"] as String, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
                            Text(
                                booking["ownerPhone"] as String,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                            )
                        }
                        Surface(
                            onClick = { onNavigateToChat("owner-conversation-id") },
                            shape = CircleShape,
                            color = Primary,
                            shadowElevation = if (isDark) 0.dp else 6.dp,
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Chat, null, tint = TextOnPrimary, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(8.dp))

            // Pill Action Buttons
            Button(
                onClick = onNavigateToAgreements,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Secondary, contentColor = TextOnPrimary),
                elevation = if (isDark) ButtonDefaults.buttonElevation(0.dp) else ButtonDefaults.buttonElevation(6.dp)
            ) {
                Icon(Icons.Default.Description, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Text("Digital Tenancy Agreement & E-Sign", fontWeight = FontWeight.ExtraBold)
            }

            Button(
                onClick = onNavigateToPayment,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = TextOnPrimary),
                elevation = if (isDark) ButtonDefaults.buttonElevation(0.dp) else ButtonDefaults.buttonElevation(6.dp)
            ) {
                Icon(Icons.Default.Payment, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Text("View Payment Schedule & Invoice", fontWeight = FontWeight.ExtraBold)
            }
            
            OutlinedButton(
                onClick = { showCancelDialog = true },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = CircleShape,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Error),
                border = BorderStroke(1.5.dp, Error.copy(alpha = 0.6f))
            ) {
                Icon(Icons.Default.Cancel, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Text("Cancel Reservation", fontWeight = FontWeight.Bold)
            }
        }
    }
    
    // Cancel Dialog
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            icon = { 
                Surface(shape = CircleShape, color = Warning.copy(alpha = 0.15f), modifier = Modifier.size(48.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Warning, null, tint = Warning, modifier = Modifier.size(24.dp))
                    }
                }
            },
            title = { Text("Cancel Reservation?", fontWeight = FontWeight.ExtraBold) },
            text = { Text("Are you sure you want to cancel this reservation? Early cancellation policies or deduction charges may apply.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)) },
            shape = RoundedCornerShape(26.dp),
            confirmButton = {
                Button(
                    onClick = {
                        showCancelDialog = false
                        onCancelBooking()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Error, contentColor = Color.White),
                    shape = CircleShape
                ) {
                    Text("Yes, Cancel", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("No, Keep Reservation", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
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
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(shape = CircleShape, color = Primary.copy(alpha = 0.12f), modifier = Modifier.size(34.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, modifier = Modifier.size(16.dp), tint = Primary)
            }
        }
        Spacer(Modifier.width(14.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
            modifier = Modifier.width(95.dp),
            fontWeight = FontWeight.Medium
        )
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.ExtraBold)
    }
}
