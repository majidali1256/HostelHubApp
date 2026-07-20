package com.hostelhub.ui.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import com.hostelhub.ui.theme.isAppInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.hostelhub.data.model.Hostel
import com.hostelhub.data.repository.HostelRepository
import com.hostelhub.ui.components.TripGlideDragHandle
import com.hostelhub.ui.components.TripGlideRatingPill
import com.hostelhub.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapSearchScreen(
    hostels: List<Hostel> = emptyList(),
    onNavigateBack: () -> Unit,
    onHostelClick: (String) -> Unit,
    onRequestLocation: () -> Unit = {},
    viewModel: MapSearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val displayHostels = if (uiState.hostels.isNotEmpty()) uiState.hostels else hostels
    var selectedRadius by remember { mutableStateOf(5) }
    var selectedHostel by remember { mutableStateOf<Hostel?>(null) }
    val isDark = isAppInDarkTheme()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Interactive Radar Map", 
                        style = MaterialTheme.typography.titleLarge, 
                        fontWeight = FontWeight.ExtraBold 
                    ) 
                },
                navigationIcon = {
                    Surface(
                        onClick = onNavigateBack,
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.ArrowBack, "Back", modifier = Modifier.size(20.dp))
                        }
                    }
                },
                actions = {
                    Surface(
                        onClick = { viewModel.loadHostels() },
                        shape = CircleShape,
                        color = Primary.copy(alpha = if (isDark) 0.2f else 0.12f),
                        border = BorderStroke(1.dp, Primary.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Refresh, "Refresh", tint = Primary, modifier = Modifier.size(20.dp))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Radius Selector Pill Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Radius:", 
                        style = MaterialTheme.typography.labelMedium, 
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(listOf(1, 3, 5, 10)) { radius ->
                            val isSelected = selectedRadius == radius
                            val bgColor by animateColorAsState(
                                targetValue = if (isSelected) Primary else MaterialTheme.colorScheme.surface,
                                label = "radBg"
                            )
                            val textColor by animateColorAsState(
                                targetValue = if (isSelected) TextOnPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                label = "radText"
                            )

                            Surface(
                                shape = CircleShape,
                                color = bgColor,
                                border = if (!isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)) else null,
                                modifier = Modifier
                                    .height(36.dp)
                                    .clickable { selectedRadius = radius }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                ) {
                                    if (isSelected) {
                                        Icon(Icons.Default.Radar, null, tint = TextOnPrimary, modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = "${radius} km",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = textColor,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                // Interactive Radar Canvas Card (~26px TripGlide elevated container)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .shadow(
                            elevation = if (isDark) 0.dp else 10.dp,
                            shape = RoundedCornerShape(26.dp),
                            spotColor = Color.Black.copy(alpha = 0.15f)
                        ),
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isDark) 0.35f else 0.5f)),
                    border = if (isDark) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)) else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Radar/Grid Canvas
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val center = Offset(size.width / 2, size.height / 2)
                            val maxRadius = minOf(size.width, size.height) * 0.45f
                            
                            // Draw radar rings
                            for (i in 1..3) {
                                drawCircle(
                                    color = Primary.copy(alpha = if (isDark) 0.25f else 0.18f),
                                    radius = maxRadius * (i / 3f),
                                    center = center,
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                                        width = 2.5f,
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
                                    )
                                )
                            }
                            
                            // User Center Marker
                            drawCircle(
                                color = Primary.copy(alpha = 0.25f),
                                radius = 28f,
                                center = center
                            )
                            drawCircle(
                                color = Primary,
                                radius = 12f,
                                center = center
                            )
                        }

                        // Plotted Price Pills for each hostel (TripGlide Pill Badges)
                        displayHostels.forEachIndexed { index, hostel ->
                            val angle = (index * 137.5f) % 360f // Golden angle dispersion
                            val distFactor = 0.25f + ((index * 17) % 60) / 100f
                            
                            Box(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .offset(
                                        x = ((kotlin.math.cos(Math.toRadians(angle.toDouble())) * distFactor * 140).toInt()).dp,
                                        y = ((kotlin.math.sin(Math.toRadians(angle.toDouble())) * distFactor * 160).toInt()).dp
                                    )
                                    .clickable { selectedHostel = hostel }
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (selectedHostel?.id == hostel.id) Secondary else Primary,
                                    shadowElevation = if (isDark) 0.dp else 6.dp,
                                    border = BorderStroke(2.dp, Color.White)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Hotel,
                                            null,
                                            tint = Color.White,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            "Rs ${hostel.price.toInt() / 1000}k",
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                            }
                        }

                        // Map watermark/indicator pill at bottom left
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(14.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.TouchApp, null, tint = Primary, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "Tap price pins to preview details",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }

                // List header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Nearby Hostels (${displayHostels.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Surface(
                        shape = CircleShape,
                        color = Primary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            "Within ${selectedRadius}km",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // List View of Nearby Hostels (TripGlide ~20px cards)
                LazyColumn(
                    modifier = Modifier.weight(0.85f),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(displayHostels, key = { it.id }) { hostel ->
                        NearbyHostelItem(
                            hostel = hostel,
                            onClick = { onHostelClick(hostel.id) }
                        )
                    }
                }
            }

            // Bottom Preview Sheet / Card for Selected Pin (With TripGlideDragHandle & ~26px curves)
            AnimatedVisibility(
                visible = selectedHostel != null,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(20.dp)
            ) {
                selectedHostel?.let { hostel ->
                    Card(
                        onClick = { onHostelClick(hostel.id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = if (isDark) 0.dp else 16.dp,
                                shape = RoundedCornerShape(26.dp),
                                spotColor = Color.Black.copy(alpha = 0.25f)
                            ),
                        shape = RoundedCornerShape(26.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = if (isDark) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)) else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            TripGlideDragHandle()
                            Spacer(Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(hostel.resolvedImages.firstOrNull() ?: "")
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = hostel.name,
                                    modifier = Modifier
                                        .size(86.dp)
                                        .clip(RoundedCornerShape(20.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text(
                                            hostel.name, 
                                            style = MaterialTheme.typography.titleMedium, 
                                            fontWeight = FontWeight.ExtraBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(
                                            onClick = { selectedHostel = null }, 
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Close, "Close", modifier = Modifier.size(18.dp))
                                        }
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.LocationOn, null, tint = Primary, modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            hostel.location,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TripGlideRatingPill(
                                            rating = hostel.rating,
                                            reviewCount = hostel.reviews?.size ?: 0
                                        )
                                        Surface(
                                            shape = CircleShape,
                                            color = Primary
                                        ) {
                                            Text(
                                                "Rs ${hostel.price.toInt()}/mo",
                                                fontWeight = FontWeight.ExtraBold,
                                                color = TextOnPrimary,
                                                style = MaterialTheme.typography.labelMedium,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                            )
                                        }
                                    }
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
private fun NearbyHostelItem(
    hostel: Hostel,
    onClick: () -> Unit
) {
    val isDark = isAppInDarkTheme()
    
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isDark) 0.dp else 4.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = Color.Black.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = if (isDark) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)) else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = Primary.copy(alpha = if (isDark) 0.2f else 0.12f),
                modifier = Modifier.size(48.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.NearMe, null, tint = Primary, modifier = Modifier.size(16.dp))
                    Text("1.2km", style = MaterialTheme.typography.labelSmall, color = Primary, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(hostel.name, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        hostel.location, 
                        style = MaterialTheme.typography.bodySmall, 
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            Surface(
                shape = CircleShape,
                color = Primary
            ) {
                Text(
                    "Rs ${hostel.price.toInt()}/mo",
                    fontWeight = FontWeight.ExtraBold,
                    color = TextOnPrimary,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@HiltViewModel
class MapSearchViewModel @Inject constructor(
    private val hostelRepository: HostelRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(MapSearchUiState())
    val uiState: StateFlow<MapSearchUiState> = _uiState.asStateFlow()

    init {
        loadHostels()
    }

    fun loadHostels() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            hostelRepository.getHostels()
                .onSuccess { list ->
                    _uiState.value = _uiState.value.copy(isLoading = false, hostels = list)
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
        }
    }
}

data class MapSearchUiState(
    val hostels: List<Hostel> = emptyList(),
    val isLoading: Boolean = false
)
