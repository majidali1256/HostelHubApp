package com.hostelhub.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.hostelhub.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToBankDetails: () -> Unit,
    onNavigateToIdentityVerification: () -> Unit = {},
    onNavigateToAdminDashboard: () -> Unit = {},
    onNavigateToAdminFraudDashboard: () -> Unit = {},
    onNavigateToOwnerHostelList: () -> Unit = {},
    onNavigateToBookingVerification: () -> Unit = {},
    onNavigateToOwnerReviewManagement: () -> Unit = {},
    onNavigateToFairRentEstimator: () -> Unit = {},
    onNavigateToAgreements: () -> Unit = {},
    onNavigateToSettings: () -> Unit,
    onLogout: () -> Unit,
    onNavigateRoute: (String) -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }
    
    val isDark = isSystemInDarkTheme()
    com.hostelhub.ui.components.navigation.HostelHubScaffold(
        title = "My Profile",
        currentRoute = com.hostelhub.ui.navigation.Screen.Profile.route,
        onNavigate = { route ->
            when (route) {
                com.hostelhub.ui.navigation.Screen.Home.route -> onNavigateBack()
                com.hostelhub.ui.navigation.Screen.Profile.route -> {}
                com.hostelhub.ui.navigation.Screen.Settings.route -> onNavigateToSettings()
                com.hostelhub.ui.navigation.Screen.Agreements.route -> onNavigateToAgreements()
                com.hostelhub.ui.navigation.Screen.AdminDashboard.route -> onNavigateToAdminDashboard()
                com.hostelhub.ui.navigation.Screen.AdminFraudDashboard.route -> onNavigateToAdminFraudDashboard()
                com.hostelhub.ui.navigation.Screen.FairRentEstimator.route -> onNavigateToFairRentEstimator()
                com.hostelhub.ui.navigation.Screen.OwnerHostelList.route -> onNavigateToOwnerHostelList()
                else -> onNavigateRoute(route)
            }
        },
        onLogout = { showLogoutDialog = true },
        userRole = uiState.user?.role?.name?.lowercase() ?: "admin",
        username = uiState.user?.let { "${it.firstName ?: ""} ${it.lastName ?: ""}".trim().ifEmpty { it.username } } ?: "System Admin",
        userEmail = uiState.user?.email ?: "admin@gmail.com"
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // Profile Header
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .shadow(if (isDark) 0.dp else 8.dp, RoundedCornerShape(26.dp), spotColor = Color.Black.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = if (isDark) 0.16f else 0.1f)),
                    border = BorderStroke(1.dp, Primary.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Avatar
                        AsyncImage(
                            model = uiState.user?.profilePicture ?: "",
                            contentDescription = "Profile",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        
                        Spacer(Modifier.height(16.dp))
                        
                        // Name
                        Text(
                            uiState.user?.let { "${it.firstName ?: ""} ${it.lastName ?: ""}".trim().ifEmpty { it.username } } ?: "User",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Text(
                            uiState.user?.email ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        
                        Spacer(Modifier.height(16.dp))
                        
                        // Role Badge
                        uiState.user?.role?.let { role ->
                            Surface(
                                shape = CircleShape,
                                color = when (role.name.lowercase()) {
                                    "owner" -> Secondary.copy(alpha = 0.2f)
                                    "admin" -> Primary.copy(alpha = 0.2f)
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            ) {
                                Text(
                                    role.name.replaceFirstChar { it.uppercase() },
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = when (role.name.lowercase()) {
                                        "owner" -> Secondary
                                        "admin" -> Primary
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }
                        
                        // Trust Score
                        uiState.user?.trustScore?.let { score ->
                            Spacer(Modifier.height(12.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Shield,
                                    null,
                                    tint = if (score >= 80) Success else if (score >= 50) Warning else Error,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "Trust Score: $score%",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        
                        Spacer(Modifier.height(16.dp))
                        
                        // Edit Profile Button
                        OutlinedButton(
                            onClick = onNavigateToEditProfile,
                            shape = CircleShape,
                            border = BorderStroke(1.5.dp, Primary)
                        ) {
                            Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Edit Profile")
                        }
                    }
                }
                
                // Menu Items
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        "Account",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    
                    ProfileMenuItem(
                        icon = Icons.Outlined.Person,
                        title = "Personal Information",
                        subtitle = "Manage your personal details",
                        onClick = onNavigateToEditProfile
                    )
                    
                    ProfileMenuItem(
                        icon = Icons.Outlined.AccountBalance,
                        title = "Bank Details",
                        subtitle = "Payment account information",
                        onClick = onNavigateToBankDetails
                    )
                    
                    ProfileMenuItem(
                        icon = Icons.Outlined.VerifiedUser,
                        title = "Identity Verification",
                        subtitle = if (uiState.user?.isVerified == true || uiState.user?.verificationStatus?.lowercase() == "verified") "Verified" else "Not verified",
                        onClick = onNavigateToIdentityVerification
                    )
                    
                    ProfileMenuItem(
                        icon = Icons.Outlined.Assignment,
                        title = "My Tenancy Agreements & Leases",
                        subtitle = "View active contracts & e-sign documents",
                        onClick = onNavigateToAgreements
                    )

                    Spacer(Modifier.height(16.dp))

                    Text(
                        "Platform Administration",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Primary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    ProfileMenuItem(
                        icon = Icons.Outlined.AdminPanelSettings,
                        title = "Admin & Moderation Hub",
                        subtitle = "Manage users, verify IDs & moderate listings",
                        onClick = onNavigateToAdminDashboard
                    )
                    
                    ProfileMenuItem(
                        icon = Icons.Outlined.Security,
                        title = "Fraud Detection & Security Reports",
                        subtitle = "Review scam alerts, AI risk scores & ban listings",
                        onClick = onNavigateToAdminFraudDashboard
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    
                    Text(
                        "Property Owner Hub",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Secondary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    ProfileMenuItem(
                        icon = Icons.Outlined.HomeWork,
                        title = "My Properties & Rooms",
                        subtitle = "Manage listings, rooms inventory & pricing",
                        onClick = onNavigateToOwnerHostelList
                    )
                    
                    ProfileMenuItem(
                        icon = Icons.Outlined.Assignment,
                        title = "Tenancy Contracts & E-Signatures",
                        subtitle = "Generate digital leases & check tenant signatures",
                        onClick = onNavigateToAgreements
                    )
                    
                    ProfileMenuItem(
                        icon = Icons.Outlined.FactCheck,
                        title = "Payment Verification Hub",
                        subtitle = "Review receipts & confirm student bookings",
                        onClick = onNavigateToBookingVerification
                    )

                    ProfileMenuItem(
                        icon = Icons.Outlined.RateReview,
                        title = "Student Reviews & Trust System",
                        subtitle = "Monitor ratings & post official host responses",
                        onClick = onNavigateToOwnerReviewManagement
                    )

                    ProfileMenuItem(
                        icon = Icons.Outlined.AutoAwesome,
                        title = "AI Fair Rent Estimator",
                        subtitle = "Benchmark market valuation & check price fairness",
                        onClick = onNavigateToFairRentEstimator
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    
                    Text(
                        "Preferences",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    
                    ProfileMenuItem(
                        icon = Icons.Outlined.Notifications,
                        title = "Notifications",
                        subtitle = "Configure notification preferences",
                        onClick = { }
                    )
                    
                    ProfileMenuItem(
                        icon = Icons.Outlined.Settings,
                        title = "Settings",
                        subtitle = "App settings and preferences",
                        onClick = onNavigateToSettings
                    )
                    
                    ProfileMenuItem(
                        icon = Icons.Outlined.Help,
                        title = "Help & Support",
                        subtitle = "Get help with the app",
                        onClick = { }
                    )
                    
                    Spacer(Modifier.height(24.dp))
                    
                    // Logout Button
                    Button(
                        onClick = { showLogoutDialog = true },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Error),
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.Logout, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Logout")
                    }
                    
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
    
    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            shape = RoundedCornerShape(26.dp),
            title = { Text("Logout", fontWeight = FontWeight.ExtraBold) },
            text = { Text("Are you sure you want to logout from your account?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout(onLogout)
                    }
                ) {
                    Text("Logout", color = Error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .shadow(if (isDark) 0.dp else 4.dp, RoundedCornerShape(22.dp), spotColor = Color.Black.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = Primary.copy(alpha = 0.12f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = Primary, modifier = Modifier.size(22.dp))
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                )
            }
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.ChevronRight,
                        null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
