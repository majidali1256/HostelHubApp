package com.hostelhub.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hostelhub.data.model.UserManagementItem
import com.hostelhub.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(
    navController: NavController,
    viewModel: UserManagementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchInput by remember { mutableStateOf(uiState.searchQuery) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("User Directory & Access Control", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadUsers() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            // Action Feedback Banner
            if (!uiState.actionMessage.isNullOrBlank()) {
                Surface(
                    color = Success.copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Success)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = uiState.actionMessage!!, color = Success, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                        IconButton(onClick = { viewModel.clearActionMessage() }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Success, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            if (!uiState.error.isNullOrBlank()) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = uiState.error!!, color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(onClick = { viewModel.clearActionMessage() }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Search Bar & Filters
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = searchInput,
                    onValueChange = {
                        searchInput = it
                        viewModel.searchUsers(it)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search by name, email, or username...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchInput.isNotEmpty()) {
                            IconButton(onClick = {
                                searchInput = ""
                                viewModel.searchUsers("")
                            }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.background,
                        unfocusedContainerColor = MaterialTheme.colorScheme.background
                    )
                )

                // Role Filter Chips
                Text(text = "Filter by Role:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("All", "Customer", "Owner", "Admin").forEach { role ->
                        FilterChip(
                            selected = uiState.selectedRole.equals(role, ignoreCase = true),
                            onClick = { viewModel.filterByRole(role) },
                            label = { Text(role) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Primary.copy(alpha = 0.2f),
                                selectedLabelColor = Primary
                            )
                        )
                    }
                }

                // Status Filter Chips
                Text(text = "Filter by Account Status:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("All", "Active", "Banned").forEach { status ->
                        FilterChip(
                            selected = uiState.selectedStatus.equals(status, ignoreCase = true),
                            onClick = { viewModel.filterByStatus(status) },
                            label = { Text(status) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (status == "Banned") MaterialTheme.colorScheme.errorContainer else Success.copy(alpha = 0.2f),
                                selectedLabelColor = if (status == "Banned") MaterialTheme.colorScheme.error else Success
                            )
                        )
                    }
                }
            }

            Divider()

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.users.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.PersonOff, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "No users found matching your filters", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(text = "Try clearing search queries or selecting 'All' roles.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.users) { user ->
                        UserCard(
                            user = user,
                            onVerifyId = { viewModel.performAction(user.id, "verify") },
                            onSuspend = { viewModel.performAction(user.id, "ban") },
                            onActivate = { viewModel.performAction(user.id, "unban") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UserCard(
    user: UserManagementItem,
    onVerifyId: () -> Unit,
    onSuspend: () -> Unit,
    onActivate: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                when (user.role.lowercase()) {
                                    "owner" -> Primary.copy(alpha = 0.2f)
                                    "admin" -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                                    else -> Secondary.copy(alpha = 0.2f)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.displayName.take(1).uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = when (user.role.lowercase()) {
                                "owner" -> Primary
                                "admin" -> MaterialTheme.colorScheme.error
                                else -> Secondary
                            }
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = user.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            // Role Badge
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = when (user.role.lowercase()) {
                                    "owner" -> Primary.copy(alpha = 0.15f)
                                    "admin" -> MaterialTheme.colorScheme.errorContainer
                                    else -> Secondary.copy(alpha = 0.15f)
                                }
                            ) {
                                Text(
                                    text = user.role.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = when (user.role.lowercase()) {
                                        "owner" -> Primary
                                        "admin" -> MaterialTheme.colorScheme.error
                                        else -> Secondary
                                    },
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = user.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Status Badge
                if (user.isBanned) {
                    Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.errorContainer) {
                        Text("SUSPENDED", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                } else if (user.isVerified || user.verificationStatus == "verified") {
                    Surface(shape = RoundedCornerShape(6.dp), color = Success.copy(alpha = 0.15f)) {
                        Text("VERIFIED ID", color = Success, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                } else {
                    Surface(shape = RoundedCornerShape(6.dp), color = Warning.copy(alpha = 0.15f)) {
                        Text("PENDING ID", color = Warning, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }
            }

            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!user.isVerified && user.verificationStatus != "verified" && !user.isBanned) {
                    OutlinedButton(
                        onClick = onVerifyId,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Success),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Success),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Verify ID Doc", style = MaterialTheme.typography.labelMedium)
                    }
                }

                if (user.isBanned) {
                    Button(
                        onClick = onActivate,
                        colors = ButtonDefaults.buttonColors(containerColor = Success)
                    ) {
                        Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Restore Account", style = MaterialTheme.typography.labelMedium)
                    }
                } else if (user.role.lowercase() != "admin") {
                    OutlinedButton(
                        onClick = onSuspend,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Suspend User", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}
