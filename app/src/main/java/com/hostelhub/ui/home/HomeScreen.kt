package com.hostelhub.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.hostelhub.data.model.Hostel
import com.hostelhub.data.model.RoomCategory
import com.hostelhub.ui.components.NotificationBadgeWidget
import com.hostelhub.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToHostel: (String) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToBookings: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToSearch: () -> Unit = {},
    onNavigateToFilters: () -> Unit = {},
    onNavigateToMap: () -> Unit = {},
    onNavigateRoute: (String) -> Unit = {},
    appliedFilters: com.hostelhub.ui.search.FilterOptions? = null,
    onClearFilters: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(appliedFilters) {
        appliedFilters?.let { opts ->
            viewModel.applyFilters(
                minPrice = opts.minPrice,
                maxPrice = opts.maxPrice,
                category = opts.category,
                genderPreference = opts.genderPreference,
                verifiedOnly = opts.verifiedOnly,
                amenities = opts.amenities
            )
        }
    }
    
    com.hostelhub.ui.components.navigation.HostelHubScaffold(
        title = "Dashboard",
        currentRoute = com.hostelhub.ui.navigation.Screen.Home.route,
        onNavigate = { route ->
            when (route) {
                com.hostelhub.ui.navigation.Screen.Home.route -> {}
                com.hostelhub.ui.navigation.Screen.Profile.route -> onNavigateToProfile()
                com.hostelhub.ui.navigation.Screen.ChatList.route -> onNavigateToChat()
                com.hostelhub.ui.navigation.Screen.BookingHistory.route -> onNavigateToBookings()
                com.hostelhub.ui.navigation.Screen.Search.route -> onNavigateToSearch()
                else -> onNavigateRoute(route)
            }
        },
        onNotificationsClick = onNavigateToNotifications,
        onChatClick = onNavigateToChat,
        unreadNotifications = uiState.unreadNotificationCount,
        unreadMessages = 0,
        userRole = uiState.userRole,
        username = uiState.username
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // Search Bar & Actions
            item {
                SearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = viewModel::search,
                    onAiSearchClick = onNavigateToSearch,
                    onFilterClick = onNavigateToFilters,
                    onMapClick = onNavigateToMap,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }

            // Applied Filters Banner (if any)
            if (appliedFilters != null) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = Secondary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.FilterAlt, null, tint = Secondary, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "Active Filters Applied",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Secondary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        TextButton(onClick = {
                            onClearFilters()
                            viewModel.clearFilters()
                        }) {
                            Text("Clear", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
            
            // Sector Chips
            item {
                Text(
                    "Explore Sectors",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                SectorFilter(
                    selectedSector = uiState.selectedSector,
                    onSectorSelected = { sector ->
                        viewModel.filterBySector(sector)
                    },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            // Room Category Filter
            item {
                Text(
                    "Room Categories",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                CategoryFilter(
                    selectedCategory = selectedCategory,
                    onCategorySelected = { category ->
                        selectedCategory = if (selectedCategory == category) null else category
                        if (selectedCategory != null) {
                            viewModel.applyFilters(category = selectedCategory)
                        } else {
                            viewModel.clearFilters()
                        }
                    },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Loading State
            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            
            // Error State
            uiState.error?.let { error ->
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Error.copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Error, null, tint = Error)
                            Spacer(Modifier.width(12.dp))
                            Text(error, color = Error)
                            Spacer(Modifier.weight(1f))
                            TextButton(onClick = viewModel::loadHostels) {
                                Text("Retry")
                            }
                        }
                    }
                }
            }
            
            // Empty State
            if (!uiState.isLoading && uiState.filteredHostels.isEmpty() && uiState.error == null) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.SearchOff,
                            null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "No hostels found",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
            
            // Hostel List
            items(uiState.filteredHostels) { hostel ->
                HostelCard(
                    hostel = hostel,
                    onClick = { onNavigateToHostel(hostel.id) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onAiSearchClick: () -> Unit = {},
    onFilterClick: () -> Unit = {},
    onMapClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Search hostels or ask AI...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Clear, "Clear", modifier = Modifier.size(18.dp))
                        }
                    }
                    IconButton(
                        onClick = onAiSearchClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            "AI Smart Search",
                            tint = Secondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        )
        Surface(
            onClick = onFilterClick,
            shape = RoundedCornerShape(16.dp),
            color = Primary.copy(alpha = 0.1f),
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Tune, "Filters", tint = Primary)
            }
        }
        Surface(
            onClick = onMapClick,
            shape = RoundedCornerShape(16.dp),
            color = Secondary.copy(alpha = 0.1f),
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Map, "Map View", tint = Secondary)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SectorFilter(
    selectedSector: String?,
    onSectorSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val sectors = listOf("All", "Near NUST", "G-11 Sector", "H-12 Sector", "Johar Town", "DHA", "Gulberg")
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(sectors) { sector ->
            FilterChip(
                selected = (selectedSector == sector || (selectedSector == null && sector == "All")),
                onClick = { onSectorSelected(sector) },
                label = { Text(sector) },
                leadingIcon = if (selectedSector == sector || (selectedSector == null && sector == "All")) {
                    { Icon(Icons.Default.Place, null, Modifier.size(16.dp)) }
                } else null
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryFilter(
    selectedCategory: String?,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(RoomCategory.all) { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                label = { Text(category) },
                leadingIcon = if (selectedCategory == category) {
                    { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                } else null
            )
        }
    }
}

@Composable
fun HostelCard(
    hostel: Hostel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(hostel.resolvedImages.firstOrNull() ?: "")
                        .crossfade(true)
                        .build(),
                    contentDescription = hostel.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Gradient Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)),
                                startY = 100f
                            )
                        )
                )
                
                // Price Badge & Fair Rent Indicator
                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Primary
                    ) {
                        Text(
                            text = "Rs ${hostel.price.toInt()}/mo",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = TextOnPrimary,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (hostel.price <= 38000) {
                        Spacer(Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Success.copy(alpha = 0.9f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.ThumbUp, null, tint = Color.White, modifier = Modifier.size(10.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "Fair Price",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
                
                // Verified Badge
                if (hostel.verified) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = Success
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Verified,
                                null,
                                modifier = Modifier.size(14.dp),
                                tint = TextOnPrimary
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "Verified",
                                color = TextOnPrimary,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
                
                // Title at bottom of image
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                ) {
                    Text(
                        text = hostel.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocationOn,
                            null,
                            modifier = Modifier.size(14.dp),
                            tint = Color.White.copy(alpha = 0.8f)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = hostel.location,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            
            // Details
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                // Rating and Category
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Rating
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Star,
                            null,
                            modifier = Modifier.size(18.dp),
                            tint = Accent
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = String.format("%.1f", hostel.rating),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = " (${hostel.reviews?.size ?: 0})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    
                    // Category
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Secondary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = hostel.category,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Secondary
                        )
                    }
                }
                
                Spacer(Modifier.height(8.dp))
                
                // Amenities Preview
                if (hostel.amenities.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        hostel.amenities.take(4).forEach { amenity ->
                            AmenityChip(amenity = amenity)
                        }
                        if (hostel.amenities.size > 4) {
                            Text(
                                text = "+${hostel.amenities.size - 4}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.align(Alignment.CenterVertically)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AmenityChip(amenity: String) {
    val icon = when (amenity.lowercase()) {
        "wifi" -> Icons.Default.Wifi
        "ac" -> Icons.Default.AcUnit
        "parking" -> Icons.Default.LocalParking
        "laundry" -> Icons.Default.LocalLaundryService
        "kitchen" -> Icons.Default.Kitchen
        "security" -> Icons.Default.Security
        "gym" -> Icons.Default.FitnessCenter
        "tv" -> Icons.Default.Tv
        else -> Icons.Default.CheckCircle
    }
    
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Icon(
            icon,
            amenity,
            modifier = Modifier
                .padding(6.dp)
                .size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
