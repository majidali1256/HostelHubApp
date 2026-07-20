package com.hostelhub.ui.hostel

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import com.hostelhub.data.model.Review
import com.hostelhub.ui.components.TripGlideOverlayIconButton
import com.hostelhub.ui.components.TripGlideRatingPill
import com.hostelhub.ui.components.TripGlideStickyBottomBar
import com.hostelhub.ui.reviews.*
import com.hostelhub.ui.fairrent.*
import com.hostelhub.ui.trust.TrustScoreBadgeWidget
import com.hostelhub.ui.fraud.ReportFraudModal
import com.hostelhub.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HostelDetailScreen(
    hostelId: String,
    onNavigateBack: () -> Unit,
    onNavigateToBooking: () -> Unit,
    onNavigateToReviews: () -> Unit,
    onNavigateToChat: (String) -> Unit,
    viewModel: HostelDetailViewModel = hiltViewModel(),
    reviewViewModel: ReviewViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val reviewUiState by reviewViewModel.uiState.collectAsState()
    var showWriteReviewModal by remember { mutableStateOf(false) }
    var showReportFraudModal by remember { mutableStateOf(false) }
    var isFavorite by remember { mutableStateOf(false) }
    val isDark = isSystemInDarkTheme()
    
    LaunchedEffect(hostelId) {
        viewModel.loadHostel(hostelId)
        reviewViewModel.loadHostelReviews(hostelId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    TripGlideOverlayIconButton(
                        icon = Icons.Default.ArrowBack,
                        onClick = onNavigateBack,
                        modifier = Modifier.padding(start = 14.dp, top = 8.dp)
                    )
                },
                actions = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(end = 14.dp, top = 8.dp)
                    ) {
                        TripGlideOverlayIconButton(
                            icon = Icons.Default.Share,
                            onClick = { /* Share */ }
                        )
                        TripGlideOverlayIconButton(
                            icon = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            onClick = { isFavorite = !isFavorite },
                            isFavorite = isFavorite
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            uiState.hostel?.let { hostel ->
                TripGlideStickyBottomBar(
                    buttonText = "Reserve Room Now",
                    onButtonClick = onNavigateToBooking,
                    priceOrTitleInfo = {
                        Column {
                            Text(
                                "Rs ${hostel.price.toInt()}/mo",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Primary
                            )
                            Text(
                                "All inclusive • Verified space",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                            )
                        }
                    }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary, strokeWidth = 3.dp)
            }
        } else if (uiState.error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                    Surface(shape = CircleShape, color = Error.copy(alpha = 0.15f), modifier = Modifier.size(64.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Error, null, tint = Error, modifier = Modifier.size(32.dp))
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(uiState.error ?: "Error loading property details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { viewModel.loadHostel(hostelId) }, shape = CircleShape) {
                        Text("Retry Loading", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            uiState.hostel?.let { hostel ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 110.dp)
                ) {
                    // Photo Header Carousel (320.dp with bottom gradient for clarity)
                    item {
                        Box(modifier = Modifier.height(320.dp)) {
                            val pagerState = rememberPagerState(pageCount = { hostel.resolvedImages.size.coerceAtLeast(1) })
                            
                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier.fillMaxSize()
                            ) { page ->
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(hostel.resolvedImages.getOrNull(page) ?: "")
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            
                            // Bottom Dark Gradient
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                                            startY = 160f
                                        )
                                    )
                            )
                            
                            // Page Indicator Pills
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(18.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                repeat(hostel.resolvedImages.size.coerceAtLeast(1)) { index ->
                                    Box(
                                        modifier = Modifier
                                            .size(width = if (index == pagerState.currentPage) 20.dp else 8.dp, height = 8.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (index == pagerState.currentPage) Color.White
                                                else Color.White.copy(alpha = 0.45f)
                                            )
                                    )
                                }
                            }
                            
                            // Verified Pill Badge
                            if (hostel.verified) {
                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(18.dp),
                                    shape = CircleShape,
                                    color = Success
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Verified, null, Modifier.size(15.dp), tint = Color.White)
                                        Spacer(Modifier.width(6.dp))
                                        Text("Verified Inspection", color = Color.White, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                    
                    // Title, Location & Quick Stat Pills
                    item {
                        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
                            Text(
                                hostel.name,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, null, Modifier.size(18.dp), tint = Primary)
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    hostel.location,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            
                            Spacer(Modifier.height(20.dp))
                            
                            // TripGlide Stat Pill Cards
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                StatPillCard(
                                    icon = Icons.Default.Star,
                                    value = String.format("%.1f", hostel.rating),
                                    label = "Rating Score",
                                    tint = Accent,
                                    modifier = Modifier.weight(1f)
                                )
                                StatPillCard(
                                    icon = Icons.Default.RateReview,
                                    value = "${hostel.reviews?.size ?: 0}",
                                    label = "User Reviews",
                                    tint = Primary,
                                    modifier = Modifier.weight(1f)
                                )
                                StatPillCard(
                                    icon = Icons.Default.Category,
                                    value = hostel.category,
                                    label = "Room Type",
                                    tint = Secondary,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // AI Price Guidance Card (Module 10)
                    uiState.fairnessAnalysis?.let { analysis ->
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 8.dp)
                                    .shadow(
                                        elevation = if (isDark) 0.dp else 8.dp,
                                        shape = RoundedCornerShape(26.dp),
                                        spotColor = Primary.copy(alpha = 0.15f)
                                    ),
                                shape = RoundedCornerShape(26.dp),
                                colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = if (isDark) 0.18f else 0.08f)),
                                border = BorderStroke(1.dp, Primary.copy(alpha = 0.35f))
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(shape = CircleShape, color = Primary, modifier = Modifier.size(32.dp)) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(Icons.Default.AutoAwesome, null, tint = TextOnPrimary, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                            Spacer(Modifier.width(10.dp))
                                            Text("AI Price Guidance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = Primary)
                                        }
                                        FairnessBadgeWidget(label = analysis.fairnessLabel)
                                    }
                                    Spacer(Modifier.height(10.dp))
                                    Text(
                                        "Market Benchmark Band: PKR ${analysis.predictedRange.min.toInt() / 1000}k – ${analysis.predictedRange.max.toInt() / 1000}k / month",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (analysis.reasoning.isNotEmpty()) {
                                        Spacer(Modifier.height(6.dp))
                                        Text(
                                            analysis.reasoning,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // About Description Card
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 10.dp),
                            shape = RoundedCornerShape(26.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Text(
                                    "Property Description",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    hostel.description ?: "No description available",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
                                )
                            }
                        }
                    }
                    
                    // Amenities Carousel
                    item {
                        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
                            Text(
                                "Included Amenities",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(Modifier.height(14.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                items(hostel.amenities) { amenity ->
                                    AmenityPillItem(amenity)
                                }
                            }
                        }
                    }

                    // Available Rooms & Beds Inventory (TripGlide ~26px Cards)
                    if (!hostel.rooms.isNullOrEmpty()) {
                        item {
                            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
                                Text(
                                    "Available Rooms & Bed Status",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(Modifier.height(12.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                    hostel.rooms.forEach { room ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .shadow(
                                                    elevation = if (isDark) 0.dp else 6.dp,
                                                    shape = RoundedCornerShape(24.dp),
                                                    spotColor = Color.Black.copy(alpha = 0.12f)
                                                ),
                                            shape = RoundedCornerShape(24.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                                        ) {
                                            Column(modifier = Modifier.padding(16.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Surface(shape = CircleShape, color = Primary.copy(alpha = 0.12f), modifier = Modifier.size(38.dp)) {
                                                            Box(contentAlignment = Alignment.Center) {
                                                                Icon(Icons.Outlined.MeetingRoom, null, tint = Primary, modifier = Modifier.size(20.dp))
                                                            }
                                                        }
                                                        Spacer(Modifier.width(12.dp))
                                                        Column {
                                                            Text(room.name, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
                                                            Text("${room.type} Room", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f))
                                                        }
                                                    }
                                                    Surface(
                                                        color = Primary,
                                                        shape = CircleShape
                                                    ) {
                                                        Text(
                                                            "Rs ${room.price.toInt()}/mo",
                                                            style = MaterialTheme.typography.labelMedium,
                                                            color = TextOnPrimary,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                                        )
                                                    }
                                                }

                                                Spacer(Modifier.height(14.dp))
                                                Text("Individual Bed Availability:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                                Spacer(Modifier.height(8.dp))
                                                FlowRowOrGridBeds(beds = room.beds)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    // Owner Concierge Card
                    uiState.owner?.let { owner ->
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 10.dp)
                                    .shadow(
                                        elevation = if (isDark) 0.dp else 8.dp,
                                        shape = RoundedCornerShape(26.dp),
                                        spotColor = Color.Black.copy(alpha = 0.12f)
                                    ),
                                shape = RoundedCornerShape(26.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(18.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = owner.profilePicture ?: "",
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(60.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            owner.username,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        Text(
                                            "Verified Property Host",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                                        )
                                        Spacer(Modifier.height(6.dp))
                                        TrustScoreBadgeWidget(
                                            score = owner.trustScore ?: 94,
                                            isCompact = true
                                        )
                                    }
                                    Surface(
                                        onClick = { onNavigateToChat(owner.id) },
                                        shape = CircleShape,
                                        color = Primary,
                                        shadowElevation = if (isDark) 0.dp else 6.dp,
                                        modifier = Modifier.size(46.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Chat, "Chat Host", tint = TextOnPrimary, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    // Reviews Section Widget
                    item {
                        Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
                            val displayReviews = if (reviewUiState.reviews.isNotEmpty()) reviewUiState.reviews else (hostel.reviews ?: emptyList())
                            ReviewsSectionWidget(
                                reviews = displayReviews,
                                ratingStats = reviewUiState.ratingStats,
                                onSeeAllClick = onNavigateToReviews,
                                onWriteReviewClick = { showWriteReviewModal = true },
                                onHelpfulClick = { reviewViewModel.toggleHelpful(it) }
                            )
                        }
                    }

                    // Report Suspicious Listing Banner
                    item {
                        Card(
                            onClick = { showReportFraudModal = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Error.copy(alpha = if (isDark) 0.18f else 0.08f)),
                            border = BorderStroke(1.dp, Error.copy(alpha = 0.35f))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(shape = CircleShape, color = Error, modifier = Modifier.size(38.dp)) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Flag, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Report Suspicious Listing",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Error
                                    )
                                    Text(
                                        text = "Flag inaccurate photos, off-platform fees, or scam concerns.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                                    )
                                }
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Error)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showWriteReviewModal && uiState.hostel != null) {
        WriteReviewModal(
            hostelName = uiState.hostel!!.name,
            onDismiss = { showWriteReviewModal = false },
            onSubmit = { rating, cleanliness, accuracy, communication, location, value, title, comment ->
                reviewViewModel.submitReview(
                    hostelId = hostelId,
                    bookingId = null,
                    rating = rating,
                    cleanliness = cleanliness,
                    accuracy = accuracy,
                    communication = communication,
                    location = location,
                    value = value,
                    title = title,
                    comment = comment
                ) {
                    showWriteReviewModal = false
                }
            },
            isLoading = reviewUiState.isSubmitting
        )
    }

    if (showReportFraudModal && uiState.hostel != null) {
        ReportFraudModal(
            hostelId = hostelId,
            hostelName = uiState.hostel!!.name,
            onDismiss = { showReportFraudModal = false },
            onReportSubmitted = { showReportFraudModal = false }
        )
    }
}

@Composable
private fun StatPillCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = tint.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, tint.copy(alpha = 0.25f)),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp)
        ) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f))
        }
    }
}

@Composable
private fun AmenityPillItem(amenity: String) {
    val icon = when (amenity.lowercase()) {
        "wifi" -> Icons.Default.Wifi
        "ac" -> Icons.Default.AcUnit
        "parking" -> Icons.Default.LocalParking
        "laundry" -> Icons.Default.LocalLaundryService
        "kitchen" -> Icons.Default.Kitchen
        "security" -> Icons.Default.Security
        "gym" -> Icons.Default.FitnessCenter
        "tv" -> Icons.Default.Tv
        "geyser" -> Icons.Default.HotTub
        "meals" -> Icons.Default.Restaurant
        else -> Icons.Default.CheckCircle
    }
    
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(shape = CircleShape, color = Primary.copy(alpha = 0.12f), modifier = Modifier.size(30.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, amenity, modifier = Modifier.size(16.dp), tint = Primary)
                }
            }
            Text(
                amenity,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun FlowRowOrGridBeds(beds: List<com.hostelhub.data.model.Bed>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        beds.forEach { bed ->
            val color = if (bed.isOccupied) Error else Success
            Surface(
                shape = CircleShape,
                color = color.copy(alpha = 0.14f),
                border = BorderStroke(1.dp, color.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.Bed,
                        null,
                        tint = color,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "${bed.bedNumber} (${if (bed.isOccupied) "Booked" else "Available"})",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                }
            }
        }
    }
}
