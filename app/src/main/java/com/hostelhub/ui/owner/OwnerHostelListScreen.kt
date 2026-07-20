package com.hostelhub.ui.owner

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.hostelhub.data.model.Hostel
import com.hostelhub.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerHostelListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddProperty: () -> Unit,
    onNavigateToEditProperty: (String) -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateRoute: (String) -> Unit = {},
    viewModel: OwnerHostelViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var hostelToDelete by remember { mutableStateOf<Hostel?>(null) }

    val tabs = listOf("All", "Active", "Pending Review", "Inactive")

    com.hostelhub.ui.components.navigation.HostelHubScaffold(
        title = "My Properties Hub",
        currentRoute = com.hostelhub.ui.navigation.Screen.OwnerHostelList.route,
        onNavigate = { route ->
            when (route) {
                com.hostelhub.ui.navigation.Screen.OwnerHostelList.route -> {}
                com.hostelhub.ui.navigation.Screen.Home.route -> onNavigateBack()
                else -> onNavigateRoute(route)
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToAddProperty,
                containerColor = Primary,
                contentColor = TextOnPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("Add Property", fontWeight = FontWeight.Bold)
            }
        },
        userRole = "owner",
        username = "Hostel Owner"
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Stats Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(
                        value = "${uiState.hostels.size}",
                        label = "Total Listed",
                        icon = Icons.Outlined.Home,
                        color = Primary
                    )
                    StatItem(
                        value = "${uiState.activeHostels.size}",
                        label = "Active / Live",
                        icon = Icons.Outlined.CheckCircle,
                        color = Success
                    )
                    StatItem(
                        value = "${uiState.pendingHostels.size}",
                        label = "Pending Review",
                        icon = Icons.Outlined.HourglassEmpty,
                        color = Warning
                    )
                }
            }

            // Tabs Row
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 16.dp,
                containerColor = Color.Transparent,
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    val count = when (index) {
                        0 -> uiState.hostels.size
                        1 -> uiState.activeHostels.size
                        2 -> uiState.pendingHostels.size
                        else -> uiState.inactiveHostels.size
                    }
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                "$title ($count)",
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Error or Success Snackbars
            uiState.error?.let { err ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Error.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ErrorOutline, null, tint = Error)
                        Spacer(Modifier.width(8.dp))
                        Text(err, color = Error, modifier = Modifier.weight(1f))
                        IconButton(onClick = { viewModel.clearMessage() }) {
                            Icon(Icons.Default.Close, null, tint = Error)
                        }
                    }
                }
            }

            uiState.successMessage?.let { msg ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Success.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircleOutline, null, tint = Success)
                        Spacer(Modifier.width(8.dp))
                        Text(msg, color = Success, modifier = Modifier.weight(1f))
                        IconButton(onClick = { viewModel.clearMessage() }) {
                            Icon(Icons.Default.Close, null, tint = Success)
                        }
                    }
                }
            }

            // Content List
            val currentList = when (selectedTab) {
                0 -> uiState.hostels
                1 -> uiState.activeHostels
                2 -> uiState.pendingHostels
                else -> uiState.inactiveHostels
            }

            if (uiState.isLoading && uiState.hostels.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary)
                }
            } else if (currentList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Outlined.HomeWork,
                            null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "No properties in this category",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = onNavigateToAddProperty) {
                            Icon(Icons.Default.Add, null)
                            Spacer(Modifier.width(8.dp))
                            Text("List New Property")
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    items(currentList) { hostel ->
                        OwnerPropertyCard(
                            hostel = hostel,
                            onClickDetail = { onNavigateToDetail(hostel.id) },
                            onClickEdit = { onNavigateToEditProperty(hostel.id) },
                            onClickToggleStatus = { viewModel.toggleStatus(hostel) },
                            onClickDelete = { hostelToDelete = hostel }
                        )
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    hostelToDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { hostelToDelete = null },
            title = { Text("Delete Property?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete '${target.name}'? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteListing(target.id)
                        hostelToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { hostelToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(4.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
        }
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun OwnerPropertyCard(
    hostel: Hostel,
    onClickDetail: () -> Unit,
    onClickEdit: () -> Unit,
    onClickToggleStatus: () -> Unit,
    onClickDelete: () -> Unit
) {
    val isActive = hostel.status.equals("Available", ignoreCase = true) || hostel.status.equals("active", ignoreCase = true)
    val isPending = hostel.status.equals("Inactive", ignoreCase = true) && !hostel.verified && (hostel.riskScore ?: 0) > 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClickDetail() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(modifier = Modifier.height(160.dp)) {
                val imageUrl = hostel.images.firstOrNull() ?: ""
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Status Badge
                Surface(
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopStart),
                    shape = RoundedCornerShape(8.dp),
                    color = when {
                        isPending -> Warning
                        isActive -> Success
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Text(
                        text = when {
                            isPending -> "Pending Review"
                            isActive -> "Active"
                            else -> "Inactive"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            isPending -> Color.Black
                            isActive -> Color.White
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Fairness Badge
                hostel.fairnessLabel?.let { label ->
                    Surface(
                        modifier = Modifier
                            .padding(12.dp)
                            .align(Alignment.TopEnd),
                        shape = RoundedCornerShape(8.dp),
                        color = when (label) {
                            "Below Market" -> Primary
                            "Fair Price" -> Secondary
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        hostel.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "Rs ${hostel.price.toInt()} /mo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                }

                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, Modifier.size(14.dp), tint = Primary)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        hostel.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (hostel.moderationNotice != null) {
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        color = Warning.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, null, tint = Warning, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                hostel.moderationNotice,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(Modifier.height(8.dp))

                // Actions Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onClickToggleStatus) {
                        Icon(
                            if (isActive) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                            null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(if (isActive) "Deactivate" else "Activate")
                    }
                    TextButton(onClick = onClickEdit) {
                        Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Edit")
                    }
                    IconButton(onClick = onClickDelete) {
                        Icon(Icons.Default.Delete, null, tint = Error, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}
