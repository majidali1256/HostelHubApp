package com.hostelhub.ui.agreements

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hostelhub.data.model.Agreement
import com.hostelhub.data.model.AgreementStatus
import com.hostelhub.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgreementsScreen(
    onNavigateBack: () -> Unit,
    onViewAgreement: (String) -> Unit,
    onNavigateRoute: (String) -> Unit = {},
    viewModel: AgreementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadMyAgreements()
    }

    com.hostelhub.ui.components.navigation.HostelHubScaffold(
        title = "Agreements & Leases",
        currentRoute = com.hostelhub.ui.navigation.Screen.Agreements.route,
        onNavigate = { route ->
            when (route) {
                com.hostelhub.ui.navigation.Screen.Agreements.route -> {}
                com.hostelhub.ui.navigation.Screen.Home.route -> onNavigateBack()
                else -> onNavigateRoute(route)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Stats Banner
            AgreementStatsBanner(agreements = uiState.agreements)

            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AgreementFilter.values().forEach { filter ->
                    val isSelected = uiState.filterType == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setFilter(filter) },
                        label = {
                            Text(
                                text = filter.displayName,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Primary.copy(alpha = 0.15f),
                            selectedLabelColor = Primary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (isSelected) Primary else MaterialTheme.colorScheme.outlineVariant,
                            selectedBorderColor = Primary
                        )
                    )
                }
            }

            // Error Banner
            AnimatedVisibility(
                visible = uiState.error != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ErrorOutline, null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = uiState.error ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.clearMessages() }) {
                            Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }
            }

            // Success Banner
            AnimatedVisibility(
                visible = uiState.successMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    color = Success.copy(alpha = 0.15f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = Success)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = uiState.successMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = Success,
                            modifier = Modifier.weight(1f),
                            fontWeight = FontWeight.SemiBold
                        )
                        IconButton(onClick = { viewModel.clearMessages() }) {
                            Icon(Icons.Default.Close, null, tint = Success)
                        }
                    }
                }
            }

            // List Content
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primary)
                }
            } else if (uiState.filteredAgreements.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Assignment,
                            null,
                            modifier = Modifier.size(72.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No agreements found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Agreements and e-signature contracts will appear here once your hostel room booking is confirmed.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(uiState.filteredAgreements, key = { it.id }) { agreement ->
                        AgreementCardItem(
                            agreement = agreement,
                            onClick = { onViewAgreement(agreement.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AgreementStatsBanner(agreements: List<Agreement>) {
    val total = agreements.size
    val active = agreements.count { it.status == AgreementStatus.ACTIVE || it.status == AgreementStatus.SIGNED }
    val pending = agreements.count { it.status == AgreementStatus.PENDING || it.status == AgreementStatus.DRAFT }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatBox(title = "Total Leases", count = total.toString(), icon = Icons.Default.Description, color = Primary)
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(36.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )
            StatBox(title = "Pending Action", count = pending.toString(), icon = Icons.Default.PendingActions, color = Warning)
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(36.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )
            StatBox(title = "Active Leases", count = active.toString(), icon = Icons.Default.VerifiedUser, color = Success)
        }
    }
}

@Composable
private fun StatBox(title: String, count: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = CircleShape,
            color = color.copy(alpha = 0.12f),
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = count,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AgreementCardItem(
    agreement: Agreement,
    onClick: () -> Unit
) {
    val hostelName = agreement.hostel?.name ?: if (agreement.title.isNotBlank()) agreement.title else "Hostel Rental Agreement"
    val dateRange = if (agreement.duration.startDate.isNotBlank() && agreement.duration.endDate.isNotBlank()) {
        "${agreement.duration.startDate} to ${agreement.duration.endDate}"
    } else {
        "Annual Tenancy Term"
    }

    val isTenantSigned = agreement.signatures.any { it.role.equals("tenant", ignoreCase = true) }
    val isOwnerSigned = agreement.signatures.any { it.role.equals("landlord", ignoreCase = true) || it.role.equals("owner", ignoreCase = true) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Primary.copy(alpha = 0.1f),
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Description, contentDescription = null, tint = Primary)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = hostelName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DateRange, null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = dateRange,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                AgreementStatusBadge(status = agreement.status)
            }

            Spacer(modifier = Modifier.height(14.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(14.dp))

            // Rent & Deposit Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Monthly Rent", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("PKR ${agreement.rentAmount.toInt()}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("Security Deposit", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("PKR ${agreement.deposit.toInt()}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("Parties", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Tenant & Owner", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Signatures & Action Footer
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        SignatureRoleChip(role = "Tenant", isSigned = isTenantSigned)
                        SignatureRoleChip(role = "Landlord", isSigned = isOwnerSigned)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "View Contract",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                        Icon(
                            Icons.Default.ChevronRight,
                            null,
                            modifier = Modifier.size(16.dp),
                            tint = Primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AgreementStatusBadge(status: AgreementStatus) {
    val (color, text) = when (status) {
        AgreementStatus.ACTIVE, AgreementStatus.SIGNED -> Success to "Active Lease"
        AgreementStatus.PENDING -> Warning to "Pending Sign"
        AgreementStatus.DRAFT -> Secondary to "Draft"
        AgreementStatus.EXPIRED -> Color.Gray to "Expired"
        AgreementStatus.TERMINATED -> Error to "Terminated"
    }

    Surface(
        shape = CircleShape,
        color = color.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SignatureRoleChip(role: String, isSigned: Boolean) {
    val color = if (isSigned) Success else Warning
    val icon = if (isSigned) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked

    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, modifier = Modifier.size(14.dp), tint = color)
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = role,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSigned) Success else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isSigned) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
