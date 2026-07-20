package com.hostelhub.ui.fairrent

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hostelhub.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FairRentEstimatorScreen(
    onNavigateBack: () -> Unit,
    onNavigateRoute: (String) -> Unit = {},
    viewModel: FairRentViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    com.hostelhub.ui.components.navigation.HostelHubScaffold(
        title = "AI Fair Rent Tool",
        currentRoute = com.hostelhub.ui.navigation.Screen.FairRentEstimator.route,
        onNavigate = { route ->
            when (route) {
                com.hostelhub.ui.navigation.Screen.FairRentEstimator.route -> {}
                com.hostelhub.ui.navigation.Screen.Home.route -> onNavigateBack()
                else -> onNavigateRoute(route)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Primary,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.AutoAwesome, null, tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "ML Price Guidance Tool",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                        Text(
                            "Benchmark your property against real-time market data across Pakistani sectors to set competitive and fair rents.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Location Selector
            Text("Select Sector / Location", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(viewModel.availableLocations) { location ->
                    FilterChip(
                        selected = uiState.selectedLocation == location,
                        onClick = { viewModel.updateLocation(location) },
                        label = { Text(location) },
                        leadingIcon = if (uiState.selectedLocation == location) {
                            { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }

            // Room Type & Capacity
            Text("Room Type", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(viewModel.availableRoomTypes) { type ->
                    FilterChip(
                        selected = uiState.selectedRoomType == type,
                        onClick = { viewModel.updateRoomType(type) },
                        label = { Text(type) }
                    )
                }
            }

            // Amenities Selection Grid
            Text("Included Amenities (${uiState.selectedAmenities.size} selected)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            FlowRowAmenities(
                allAmenities = viewModel.allAmenities,
                selectedAmenities = uiState.selectedAmenities,
                onToggle = { viewModel.toggleAmenity(it) }
            )

            // Calculate Button
            Button(
                onClick = { viewModel.calculateFairRent() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.AutoAwesome, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Calculate AI Fair Rent Band", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                }
            }

            // Prediction Results Card
            AnimatedVisibility(visible = uiState.prediction != null) {
                uiState.prediction?.let { pred ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Primary.copy(alpha = 0.3f), RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("AI Valuation Report", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                FairnessBadgeWidget(label = pred.fairnessLabel)
                            }
                            Spacer(Modifier.height(12.dp))

                            Text(
                                "Recommended Monthly Rent:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                "PKR ${pred.minPrice.toInt() / 1000}k – ${pred.maxPrice.toInt() / 1000}k / mo",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Primary
                            )
                            Text(
                                "Optimal target point: PKR ${pred.estimatedPrice.toInt()}",
                                style = MaterialTheme.typography.labelMedium,
                                color = Accent,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(Modifier.height(16.dp))
                            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                            Spacer(Modifier.height(16.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Verified, null, tint = Success, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "Model Confidence Score: ${pred.confidenceScore}%",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                pred.reasoning,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // Market Benchmarks Card
            uiState.benchmarks?.let { bench ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Market Benchmarks for ${bench.location}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            BenchmarkStatItem("Sector Avg", "PKR ${bench.averagePrice.toInt()}")
                            BenchmarkStatItem("Min Market", "PKR ${bench.minMarketPrice.toInt()}")
                            BenchmarkStatItem("Max Market", "PKR ${bench.maxMarketPrice.toInt()}")
                        }
                        if (bench.priceByRoomType.isNotEmpty()) {
                            Spacer(Modifier.height(12.dp))
                            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                            Spacer(Modifier.height(12.dp))
                            Text("Averages by Room Type:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            bench.priceByRoomType.forEach { (type, price) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(type, style = MaterialTheme.typography.bodySmall)
                                    Text("PKR ${price.toInt()}/mo", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FlowRowAmenities(
    allAmenities: List<String>,
    selectedAmenities: Set<String>,
    onToggle: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        allAmenities.chunked(3).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowItems.forEach { amenity ->
                    val selected = selectedAmenities.contains(amenity)
                    FilterChip(
                        selected = selected,
                        onClick = { onToggle(amenity) },
                        label = { Text(amenity, style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.weight(1f)
                    )
                }
                // Fill empty slots if row has < 3 items
                repeat(3 - rowItems.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun BenchmarkStatItem(title: String, value: String) {
    Column {
        Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Primary)
    }
}

/**
 * Reusable Fairness Badge Widget per Module 10 specification
 * Displays Emerald for Fair Price, Indigo for Great Deal/Below Market, Amber for Premium
 */
@Composable
fun FairnessBadgeWidget(
    label: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when {
        label.contains("Great Deal", ignoreCase = true) || label.contains("Below Market", ignoreCase = true) ->
            Secondary.copy(alpha = 0.15f) to Secondary
        label.contains("Fair Price", ignoreCase = true) ->
            Success.copy(alpha = 0.15f) to Success
        else ->
            Warning.copy(alpha = 0.15f) to Warning
    }

    Surface(
        shape = RoundedCornerShape(100.dp),
        color = bgColor,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                when {
                    label.contains("Great Deal") -> Icons.Default.TrendingDown
                    label.contains("Fair Price") -> Icons.Default.CheckCircle
                    else -> Icons.Default.TrendingUp
                },
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}
