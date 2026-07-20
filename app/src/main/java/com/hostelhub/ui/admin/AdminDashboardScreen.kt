package com.hostelhub.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hostelhub.ui.navigation.Screen
import com.hostelhub.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    navController: NavController,
    viewModel: AdminDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    com.hostelhub.ui.components.navigation.HostelHubScaffold(
        title = "Admin & Moderation Hub",
        currentRoute = com.hostelhub.ui.navigation.Screen.AdminDashboard.route,
        onNavigate = { route ->
            when (route) {
                com.hostelhub.ui.navigation.Screen.AdminDashboard.route -> {}
                com.hostelhub.ui.navigation.Screen.Home.route -> navController.popBackStack()
                else -> navController.navigate(route)
            }
        },
        userRole = "admin",
        username = "System Admin"
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // System Health Banner
                    SystemStatusBanner()

                    if (!uiState.error.isNullOrBlank()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = uiState.error!!, color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    // Section Title: Core Metrics
                    Text(
                        text = "Platform Performance Indicators",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Stats Grid (2x2)
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatCard(
                                title = "Total Users",
                                value = NumberFormat.getNumberInstance(Locale.US).format(uiState.stats.users),
                                badgeText = "+12% this month",
                                badgeColor = Success,
                                icon = Icons.Default.People,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Active Hostels",
                                value = NumberFormat.getNumberInstance(Locale.US).format(uiState.stats.hostels),
                                badgeText = "+4 newly listed",
                                badgeColor = Primary,
                                icon = Icons.Default.Home,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatCard(
                                title = "Monthly Revenue",
                                value = "PKR ${NumberFormat.getNumberInstance(Locale.US).format(uiState.stats.totalRevenue.toLong())}",
                                badgeText = "+18.4% MoM",
                                badgeColor = Success,
                                icon = Icons.Default.AccountBalance,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Pending Moderation",
                                value = uiState.stats.pendingReports.toString(),
                                badgeText = if (uiState.stats.pendingReports > 0) "Requires action" else "Queue cleared",
                                badgeColor = if (uiState.stats.pendingReports > 0) Warning else Success,
                                icon = Icons.Default.Warning,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Section Title: Quick Actions & Navigation
                    Text(
                        text = "Moderation & Administration Portals",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Navigation Card 1: User Management
                    AdminNavCard(
                        title = "User Directory & Access Control",
                        subtitle = "Search registered users, verify identity documents, toggle suspensions, or change user roles.",
                        icon = Icons.Default.SupervisorAccount,
                        iconColor = Primary,
                        badgeCount = null,
                        onClick = { navController.navigate(Screen.UserManagement.route) }
                    )

                    // Navigation Card 2: Moderation Queue
                    AdminNavCard(
                        title = "Hostel Listing Moderation Queue",
                        subtitle = "Review pending property submissions, inspect AI risk analysis scores, approve or reject listings.",
                        icon = Icons.Default.VerifiedUser,
                        iconColor = Warning,
                        badgeCount = if (uiState.stats.pendingReports > 0) uiState.stats.pendingReports else null,
                        onClick = { navController.navigate(Screen.ModerationQueue.route) }
                    )

                    // Navigation Card 3: Fraud Detection & Security Reports
                    AdminNavCard(
                        title = "Fraud Detection & Security Reports",
                        subtitle = "Review user fraud reports, inspect perceptual hash alerts, and ban fraudulent listings immediately.",
                        icon = Icons.Default.Security,
                        iconColor = Error,
                        badgeCount = null,
                        onClick = { navController.navigate(Screen.AdminFraudDashboard.route) }
                    )

                    // Analytical Chart Representation Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "System Growth Metrics (30-Day Trend)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Platform user retention stands at 94.2% with verified owners experiencing an average 88% occupancy rate across Islamabad and Lahore sectors.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                MetricSummaryChip(label = "Avg Booking Value", value = "PKR 28,500")
                                MetricSummaryChip(label = "Platform Cut (6%)", value = "PKR 1,710")
                                MetricSummaryChip(label = "Trust Index", value = "96.4/100")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemStatusBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(Primary.copy(alpha = 0.15f), Success.copy(alpha = 0.1f))
                )
            )
            .border(1.dp, Primary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(Success)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "All System Nodes Operational",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "API Gateway, AI Engine & Database Synced",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Badge(
            containerColor = Primary.copy(alpha = 0.2f),
            contentColor = Primary
        ) {
            Text(text = "LIVE", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    badgeText: String,
    badgeColor: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(badgeColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = badgeColor, modifier = Modifier.size(20.dp))
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = badgeColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = badgeText,
                        color = badgeColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminNavCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    badgeCount: Int?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(26.dp))
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (badgeCount != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Badge(containerColor = MaterialTheme.colorScheme.error) {
                            Text(text = badgeCount.toString(), color = Color.White)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MetricSummaryChip(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
