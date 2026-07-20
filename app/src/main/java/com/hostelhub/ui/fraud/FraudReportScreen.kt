package com.hostelhub.ui.fraud

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
import com.hostelhub.data.model.FraudType
import com.hostelhub.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FraudReportScreen(
    hostelId: String? = null,
    hostelName: String? = null,
    onNavigateBack: () -> Unit,
    onReportSubmitted: () -> Unit,
    viewModel: FraudViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var selectedReason by remember { mutableStateOf<String?>(null) }
    var description by remember { mutableStateOf("") }
    
    val fraudReasons = listOf(
        "Fake listing" to "The hostel does not exist or details are fabricated",
        "Misleading photos" to "Photos don't match the actual hostel or duplicate images",
        "Price manipulation" to "Hidden charges or price different from listed",
        "Scam attempt" to "Owner asked for payment outside the platform",
        "Impersonation" to "Someone pretending to be owner or caretaker",
        "Safety concerns" to "Unsafe conditions or suspicious text",
        "Other" to "Other issues not listed above"
    )

    fun getFraudType(reason: String?): FraudType = when (reason) {
        "Fake listing" -> FraudType.FAKE_LISTING
        "Misleading photos" -> FraudType.DUPLICATE_IMAGES
        "Price manipulation" -> FraudType.PRICE_MANIPULATION
        "Scam attempt" -> FraudType.SCAM
        "Impersonation" -> FraudType.IMPERSONATION
        "Safety concerns" -> FraudType.SUSPICIOUS_TEXT
        else -> FraudType.OTHER
    }

    LaunchedEffect(uiState.isSubmitted) {
        if (uiState.isSubmitted) {
            // Screen will display success state
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Report Issue") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.resetState()
                        onNavigateBack()
                    }) {
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
            if (uiState.isSubmitted) {
                // Success State
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        null,
                        modifier = Modifier.size(80.dp),
                        tint = Success
                    )
                    Spacer(Modifier.height(24.dp))
                    Text(
                        "Report Submitted",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Thank you for helping keep Hostel Hub safe. Our AI Fraud Detection Engine and moderation team will review your report and take appropriate action immediately.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = {
                            viewModel.resetState()
                            onReportSubmitted()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Done")
                    }
                }
            } else {
                // Warning Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Warning.copy(alpha = 0.1f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(16.dp)) {
                        Icon(Icons.Default.Warning, null, tint = Warning)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                "Report Fraud or Scam",
                                fontWeight = FontWeight.SemiBold,
                                color = Warning
                            )
                            Text(
                                "False reports may result in account suspension. Please only report genuine issues.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                
                Spacer(Modifier.height(16.dp))

                uiState.errorMessage?.let { errorMsg ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Error.copy(alpha = 0.1f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ErrorOutline, null, tint = Error)
                            Spacer(Modifier.width(12.dp))
                            Text(errorMsg, color = Error, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                
                // Hostel Info (if provided)
                if (hostelName != null) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Home, null, tint = Primary)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Reporting", style = MaterialTheme.typography.labelSmall)
                                Text(hostelName, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
                
                // Reason Selection
                Text(
                    "What's the issue?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(Modifier.height(12.dp))
                
                fraudReasons.forEach { (reason, desc) ->
                    Card(
                        onClick = { selectedReason = reason },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedReason == reason)
                                Primary.copy(alpha = 0.1f)
                            else
                                MaterialTheme.colorScheme.surface
                        ),
                        border = if (selectedReason == reason)
                            androidx.compose.foundation.BorderStroke(2.dp, Primary)
                        else
                            null
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedReason == reason,
                                onClick = { selectedReason = reason }
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(reason, fontWeight = FontWeight.Medium)
                                Text(
                                    desc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                // Description
                Text(
                    "Additional Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Please provide more details about the issue...") },
                    minLines = 4,
                    shape = RoundedCornerShape(12.dp)
                )
                
                Spacer(Modifier.height(24.dp))
                
                // Submit Button
                Button(
                    onClick = {
                        selectedReason?.let { reason ->
                            viewModel.submitReport(
                                hostelId = hostelId,
                                type = getFraudType(reason),
                                description = description
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = selectedReason != null && description.isNotBlank() && !uiState.isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = Error)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onError
                        )
                    } else {
                        Icon(Icons.Default.Flag, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Submit Report", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
