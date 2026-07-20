package com.hostelhub.ui.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hostelhub.data.model.FraudReport
import com.hostelhub.data.model.FraudReportStatus
import com.hostelhub.data.model.RiskLevel
import com.hostelhub.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminFraudDashboardScreen(
    navController: NavController,
    viewModel: AdminFraudViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    com.hostelhub.ui.components.navigation.HostelHubScaffold(
        title = "Fraud Detection & Security",
        currentRoute = com.hostelhub.ui.navigation.Screen.AdminFraudDashboard.route,
        onNavigate = { route ->
            when (route) {
                com.hostelhub.ui.navigation.Screen.AdminFraudDashboard.route -> {}
                com.hostelhub.ui.navigation.Screen.Home.route -> navController.popBackStack()
                else -> navController.navigate(route)
            }
        },
        userRole = "admin",
        username = "System Admin"
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Stats Summary Grid
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FraudStatCard(
                        title = "Total Reports",
                        value = uiState.stats.totalReports.toString(),
                        icon = Icons.Default.Assessment,
                        color = Primary,
                        modifier = Modifier.weight(1f)
                    )
                    FraudStatCard(
                        title = "Pending Action",
                        value = uiState.stats.pendingReports.toString(),
                        icon = Icons.Default.PendingActions,
                        color = Warning,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FraudStatCard(
                        title = "High Risk Alerts",
                        value = uiState.stats.highRiskHostels.toString(),
                        icon = Icons.Default.Warning,
                        color = Error,
                        modifier = Modifier.weight(1f)
                    )
                    FraudStatCard(
                        title = "Confirmed Scams",
                        value = uiState.stats.confirmedReports.toString(),
                        icon = Icons.Default.Gavel,
                        color = Error.copy(alpha = 0.8f),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Filter Tabs
            val filters = listOf("All", "Pending", "Investigating", "High Risk")
            ScrollableTabRow(
                selectedTabIndex = filters.indexOf(uiState.selectedFilter),
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = Primary
            ) {
                filters.forEach { filter ->
                    Tab(
                        selected = uiState.selectedFilter == filter,
                        onClick = { viewModel.setFilter(filter) },
                        text = {
                            Text(
                                text = filter,
                                fontWeight = if (uiState.selectedFilter == filter) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // Reports List
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primary)
                }
            } else if (uiState.reports.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.VerifiedUser,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Success
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Security Queue Clear",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "No fraud reports match the selected filter criterion.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(uiState.reports, key = { it.id }) { report ->
                        AdminFraudReportItemCard(
                            report = report,
                            onUpdateStatus = { status, notes ->
                                viewModel.updateReportStatus(report.id, status, notes)
                            },
                            onDelete = {
                                viewModel.deleteReport(report.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FraudStatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = color
                )
            }
        }
    }
}

@Composable
fun AdminFraudReportItemCard(
    report: FraudReport,
    onUpdateStatus: (FraudReportStatus, String?) -> Unit,
    onDelete: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    var showActionDialog by remember { mutableStateOf(false) }

    val statusColor = when (report.status) {
        FraudReportStatus.PENDING -> Warning
        FraudReportStatus.INVESTIGATING -> Primary
        FraudReportStatus.CONFIRMED -> Error
        FraudReportStatus.DISMISSED -> Success
    }

    val riskColor = when (report.aiAnalysis?.riskLevel) {
        RiskLevel.HIGH, RiskLevel.CRITICAL -> Error
        RiskLevel.MEDIUM -> Warning
        RiskLevel.LOW -> Success
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = androidx.compose.foundation.BorderStroke(
            if (report.aiAnalysis?.riskLevel == RiskLevel.HIGH) 1.5.dp else 0.5.dp,
            if (report.aiAnalysis?.riskLevel == RiskLevel.HIGH) Error.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = statusColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = report.status.name,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = statusColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = report.type.name.replace("_", " "),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = report.hostel?.name ?: "Hostel ID: ${report.hostelId ?: "Unknown"}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (report.hostel?.location != null) {
                        Text(
                            text = report.hostel.location,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // AI Risk Chip
                report.aiAnalysis?.let { analysis ->
                    Column(horizontalAlignment = Alignment.End) {
                        Surface(
                            color = riskColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, riskColor)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Speed, contentDescription = null, tint = riskColor, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Risk ${analysis.totalRiskScore}/100 (${analysis.riskLevel.name})",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = riskColor
                                )
                            }
                        }
                        Text(
                            text = "AI Confidence: ${analysis.confidence}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "\"${report.description}\"",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Reported by: ${report.reporter?.firstName ?: "Student"} (${report.createdAt?.take(10) ?: "Recent"})",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isExpanded) "Hide AI & Actions" else "Inspect Details",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                    Icon(
                        if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(12.dp))

                    report.aiAnalysis?.let { analysis ->
                        Text(
                            text = "AI Trust & Fraud Analysis",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = analysis.reasoning ?: "Automated inspection flagged suspicious indicators across multi-layer check.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (!analysis.flags.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                analysis.flags.forEach { flag ->
                                    Surface(
                                        color = Error.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "🚩 $flag",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Error,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Action Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (report.status != FraudReportStatus.INVESTIGATING) {
                            OutlinedButton(
                                onClick = { onUpdateStatus(FraudReportStatus.INVESTIGATING, "Under active review by admin") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Investigate", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        if (report.status != FraudReportStatus.CONFIRMED) {
                            Button(
                                onClick = { onUpdateStatus(FraudReportStatus.CONFIRMED, "Fraud confirmed. Listing suspended.") },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Error),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Gavel, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Confirm & Ban", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        if (report.status != FraudReportStatus.DISMISSED) {
                            OutlinedButton(
                                onClick = { onUpdateStatus(FraudReportStatus.DISMISSED, "False alarm / verified legitimate") },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Success),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Dismiss", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }
}
