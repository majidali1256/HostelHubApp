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
fun ReportFraudModal(
    hostelId: String? = null,
    hostelName: String? = null,
    onDismiss: () -> Unit,
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

    ModalBottomSheet(
        onDismissRequest = {
            viewModel.resetState()
            onDismiss()
        },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = null,
                        tint = Error,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (hostelName != null) "Report $hostelName" else "Report Listing",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = {
                    viewModel.resetState()
                    onDismiss()
                }) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (uiState.isSubmitted) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Success
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Report Submitted Successfully",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Our moderation team and automated fraud detection system will review this listing immediately.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
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
                Card(
                    colors = CardDefaults.cardColors(containerColor = Warning.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Warning, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Only report genuine safety concerns or fraudulent listings. False reports may result in account review.",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                uiState.errorMessage?.let { errorMsg ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Error.copy(alpha = 0.1f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ErrorOutline, null, tint = Error)
                            Spacer(Modifier.width(10.dp))
                            Text(errorMsg, color = Error, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                Text(
                    text = "Select Reason",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

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
                            androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedReason == reason,
                                onClick = { selectedReason = reason }
                            )
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(reason, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    desc,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Additional Details",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Please describe the exact issue encountered...") },
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

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
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = selectedReason != null && description.isNotBlank() && !uiState.isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = Error)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = MaterialTheme.colorScheme.onError,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Flag, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Submit Security Report", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
