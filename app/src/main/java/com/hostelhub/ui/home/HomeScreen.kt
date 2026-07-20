package com.hostelhub.ui.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.hostelhub.data.model.Hostel
import com.hostelhub.data.model.RoomCategory
import com.hostelhub.ui.components.*
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
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Search Bar & Actions (TripGlide Pill Style)
            item {
                SearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = viewModel::search,
                    onAiSearchClick = onNavigateToSearch,
                    onFilterClick = onNavigateToFilters,
                    onMapClick = onNavigateToMap,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
                )
            }

            // Applied Filters Banner (if any)
            if (appliedFilters != null) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = Secondary.copy(alpha = 0.15f),
                            shape = CircleShape,
                            border = BorderStroke(1.dp, Secondary.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
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
                            Text("Clear", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline)
                        }
                    }
                }
            }
            
            // Sector Chips (TripGlide Horizontally Scrollable Pill Row)
            item {
                Text(
                    "Explore Sectors",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )
                SectorFilter(
                    selectedSector = uiState.selectedSector,
                    onSectorSelected = { sector ->
                        viewModel.filterBySector(sector)
                    },
                    modifier = Modifier.padding(bottom = 14.dp)
                )
            }
            
            // Room Category Filter (TripGlide Horizontally Scrollable Pill Row)
            item {
                Text(
                    "Room Categories",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
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
                    modifier = Modifier.padding(bottom = 18.dp)
                )
            }
            
            // Loading State (~24px skeletons)
            if (uiState.isLoading) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        repeat(2) {
                            HostelCardSkeleton()
                        }
                    }
                }
            }
            
            // Error State
            uiState.error?.let { error ->
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Error.copy(alpha = 0.12f))
                    ) {
                        Row(
                            modifier = Modifier.padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Error, null, tint = Error)
                            Spacer(Modifier.width(12.dp))
                            Text(error, color = Error, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                            TextButton(onClick = viewModel::loadHostels) {
                                Text("Retry", fontWeight = FontWeight.Bold, textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline)
                            }
                        }
                    }
                }
            }
            
            // Empty State
            if (!uiState.isLoading && uiState.filteredHostels.isEmpty() && uiState.error == null) {
                item {
                    EmptyState(
                        icon = Icons.Default.SearchOff,
                        title = "No hostels found",
                        subtitle = "Try adjusting your filters or sector criteria to see more available spaces.",
                        actionLabel = "Clear Filters",
                        onAction = {
                            selectedCategory = null
                            onClearFilters()
                            viewModel.clearFilters()
                        },
                        modifier = Modifier.padding(top = 24.dp)
                    )
                }
            }
            
            // Hostel List (~26px cards with photo header overlay buttons & nested CTA)
            items(uiState.filteredHostels, key = { it.id }) { hostel ->
                HostelCard(
                    hostel = hostel,
                    onClick = { onNavigateToHostel(hostel.id) },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
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
    val isDark = isSystemInDarkTheme()

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Search hostels or ask AI...", style = MaterialTheme.typography.bodyMedium) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) },
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Clear, "Clear", modifier = Modifier.size(18.dp))
                        }
                    }
                    IconButton(
                        onClick = onAiSearchClick,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Secondary.copy(alpha = 0.12f))
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
            shape = CircleShape, // Fully pill-shaped search bar
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                focusedBorderColor = Primary,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )
        
        Surface(
            onClick = onFilterClick,
            shape = CircleShape,
            color = Primary.copy(alpha = if (isDark) 0.2f else 0.12f),
            border = BorderStroke(1.dp, Primary.copy(alpha = 0.3f)),
            modifier = Modifier.size(52.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Tune, "Filters", tint = Primary, modifier = Modifier.size(22.dp))
            }
        }
        
        Surface(
            onClick = onMapClick,
            shape = CircleShape,
            color = Secondary.copy(alpha = if (isDark) 0.2f else 0.12f),
            border = BorderStroke(1.dp, Secondary.copy(alpha = 0.3f)),
            modifier = Modifier.size(52.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Map, "Map View", tint = Secondary, modifier = Modifier.size(22.dp))
            }
        }
    }
}

@Composable
private fun SectorFilter(
    selectedSector: String?,
    onSectorSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val sectors = listOf("All", "Near NUST", "G-11 Sector", "H-12 Sector", "Johar Town", "DHA", "Gulberg")
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 20.dp)
    ) {
        items(sectors) { sector ->
            val isSelected = (selectedSector == sector || (selectedSector == null && sector == "All"))
            val bgColor by animateColorAsState(
                targetValue = if (isSelected) Primary else MaterialTheme.colorScheme.surface,
                label = "sectorBg"
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) TextOnPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                label = "sectorText"
            )

            Surface(
                shape = CircleShape,
                color = bgColor,
                border = if (!isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)) else null,
                modifier = Modifier
                    .height(40.dp)
                    .clickable { onSectorSelected(sector) }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    if (isSelected) {
                        Icon(Icons.Default.Place, null, tint = TextOnPrimary, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                    }
                    Text(
                        text = sector,
                        style = MaterialTheme.typography.labelLarge,
                        color = textColor,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryFilter(
    selectedCategory: String?,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 20.dp)
    ) {
        items(RoomCategory.all) { category ->
            val isSelected = selectedCategory == category
            val bgColor by animateColorAsState(
                targetValue = if (isSelected) Primary else MaterialTheme.colorScheme.surface,
                label = "catBg"
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) TextOnPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                label = "catText"
            )

            Surface(
                shape = CircleShape,
                color = bgColor,
                border = if (!isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)) else null,
                modifier = Modifier
                    .height(40.dp)
                    .clickable { onCategorySelected(category) }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    if (isSelected) {
                        Icon(Icons.Default.Check, null, tint = TextOnPrimary, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                    }
                    Text(
                        text = category,
                        style = MaterialTheme.typography.labelLarge,
                        color = textColor,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * TripGlide Restyled HostelCard: ~26px rounded shape, overlay buttons on photo, rating pill + underlined link, nested footer CTA
 */
@Composable
fun HostelCard(
    hostel: Hostel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFavorite by remember { mutableStateOf(false) }
    val isDark = isSystemInDarkTheme()
    val cardElevation = if (isDark) 0.dp else 12.dp

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = cardElevation,
                shape = RoundedCornerShape(26.dp),
                spotColor = Color.Black.copy(alpha = 0.15f)
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = if (isDark) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)) else null
    ) {
        Column {
            // Photo Header (~220dp with circular overlay buttons)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
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
                
                // Dark Gradient Overlay for bottom title contrast
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                                startY = 110f
                            )
                        )
                )

                // Top Left: Verified Badge
                if (hostel.verified) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(14.dp),
                        shape = CircleShape,
                        color = Success
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Verified,
                                null,
                                modifier = Modifier.size(14.dp),
                                tint = Color.White
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "Verified",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Top Right: Circular Overlay Favorite Button over photo (TripGlide pattern)
                TripGlideOverlayIconButton(
                    icon = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    onClick = { isFavorite = !isFavorite },
                    isFavorite = isFavorite,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(14.dp)
                )
                
                // Bottom of Image: Price Pill Badge + Title & Location
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = hostel.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.LocationOn,
                                null,
                                modifier = Modifier.size(15.dp),
                                tint = Color.White.copy(alpha = 0.85f)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = hostel.location,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.85f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Surface(
                            shape = CircleShape,
                            color = Primary
                        ) {
                            Text(
                                text = "Rs ${hostel.price.toInt()}/mo",
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                color = TextOnPrimary,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        if (hostel.price <= 38000) {
                            Spacer(Modifier.height(4.dp))
                            Surface(
                                shape = CircleShape,
                                color = Success.copy(alpha = 0.95f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.ThumbUp, null, tint = Color.White, modifier = Modifier.size(10.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        "Fair Price",
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Details & Amenities Row
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Star Rating inside bordered pill + Underlined Review Count (TripGlide pattern)
                    TripGlideRatingPill(
                        rating = hostel.rating,
                        reviewCount = hostel.reviews?.size ?: 0
                    )
                    
                    // Category Tag
                    Surface(
                        shape = CircleShape,
                        color = Secondary.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, Secondary.copy(alpha = 0.25f))
                    ) {
                        Text(
                            text = hostel.category,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = Secondary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(Modifier.height(14.dp))
                
                // Amenities Row
                if (hostel.amenities.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        hostel.amenities.take(4).forEach { amenity ->
                            AmenityChip(amenity = amenity)
                        }
                        if (hostel.amenities.size > 4) {
                            Text(
                                text = "+${hostel.amenities.size - 4} more",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Nested CTA right inside the card footer (TripGlide rule: "Cards nest their own call-to-action inside the card")
                TripGlideCardFooterAction(
                    text = "View property & rooms",
                    onClick = onClick
                )
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
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                icon,
                amenity,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = amenity,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
