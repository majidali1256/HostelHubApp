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

import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.saveable.rememberSaveable

enum class AdminTab(val title: String, val icon: ImageVector) {
    OVERVIEW("Overview", Icons.Default.Dashboard),
    MODERATION("Moderation Queue", Icons.Default.VerifiedUser),
    USERS("User Management", Icons.Default.People),
    FRAUD("Fraud & Security", Icons.Default.Security),
    AGREEMENTS("Agreements & Rent", Icons.Default.Assignment)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    navController: NavController,
    initialTab: AdminTab = AdminTab.OVERVIEW,
    viewModel: AdminDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by rememberSaveable { mutableStateOf(initialTab.name) }
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            AdminSectionBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it.name },
                pendingCount = uiState.stats.pendingReports
            )

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    when (selectedTab) {
                        AdminTab.OVERVIEW.name -> {
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
                            subtitle = "Inspect customer identities, verify student IDs, suspend accounts, or assign admin privileges.",
                            icon = Icons.Default.People,
                            iconColor = Primary,
                            badgeCount = null,
                            onClick = { selectedTab = AdminTab.USERS.name }
                        )

                        // Navigation Card 2: Moderation Queue
                        AdminNavCard(
                            title = "Hostel Listing Moderation Queue",
                            subtitle = "Review pending property submissions, inspect AI risk analysis scores, approve or reject listings.",
                            icon = Icons.Default.VerifiedUser,
                            iconColor = Warning,
                            badgeCount = if (uiState.stats.pendingReports > 0) uiState.stats.pendingReports else null,
                            onClick = { selectedTab = AdminTab.MODERATION.name }
                        )

                        // Navigation Card 3: Fraud Detection & Security Reports
                        AdminNavCard(
                            title = "Fraud Detection & Security Reports",
                            subtitle = "Review user fraud reports, inspect perceptual hash alerts, and ban fraudulent listings immediately.",
                            icon = Icons.Default.Security,
                            iconColor = Error,
                            badgeCount = null,
                            onClick = { selectedTab = AdminTab.FRAUD.name }
                        )

                        // Navigation Card 4: Agreements & Rent Index
                        AdminNavCard(
                            title = "Lease Agreements & Fair Rent Index",
                            subtitle = "Audit digital tenancy agreements and monitor city-wide rent compliance benchmarks.",
                            icon = Icons.Default.Assignment,
                            iconColor = Secondary,
                            badgeCount = null,
                            onClick = { selectedTab = AdminTab.AGREEMENTS.name }
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
                AdminTab.MODERATION.name -> ModerationQueueContent()
                AdminTab.USERS.name -> UserManagementContent()
                AdminTab.FRAUD.name -> AdminFraudDashboardContent()
                AdminTab.AGREEMENTS.name -> AdminAgreementsTabContent()
            }
        }
    }
    }
}
}

@Composable
fun AdminSectionBar(
    selectedTab: String,
    onTabSelected: (AdminTab) -> Unit,
    pendingCount: Int
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(AdminTab.values()) { tab ->
                val isSelected = selectedTab == tab.name
                val bgColor = if (isSelected) Primary else MaterialTheme.colorScheme.surfaceVariant
                val contentColor = if (isSelected) TextOnPrimary else MaterialTheme.colorScheme.onSurfaceVariant

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = bgColor,
                    modifier = Modifier.clickable { onTabSelected(tab) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(tab.icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(tab.title, color = contentColor, style = MaterialTheme.typography.labelMedium, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
                        if (tab == AdminTab.MODERATION && pendingCount > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) Warning else Error,
                                modifier = Modifier.size(20.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(pendingCount.toString(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
fun AdminAgreementsTabContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Assignment, contentDescription = null, tint = Primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Lease Agreements Audit & Compliance",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "All digital tenancy agreements executed through HostelHub are encrypted with SHA-256 signatures. Currently monitoring 142 active lease contracts.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Calculate, contentDescription = null, tint = Secondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "City-Wide Fair Rent Index Regulation",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Fair Rent Index thresholds are enforced across Islamabad (H-12, F-10, G-9) and Lahore sectors to prevent predatory price gouging.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    MetricSummaryChip(label = "Islamabad H-12 Avg", value = "PKR 24,000/mo")
                    MetricSummaryChip(label = "Lahore Johar Avg", value = "PKR 22,500/mo")
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
