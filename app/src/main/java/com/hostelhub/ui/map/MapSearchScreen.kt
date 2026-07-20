package com.hostelhub.ui.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.hostelhub.data.model.Hostel
import com.hostelhub.data.repository.HostelRepository
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
    var hasLocationPermission by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Interactive Map Discovery") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadHostels() }) {
                        Icon(Icons.Default.Refresh, "Refresh")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Radius Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Radius:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    listOf(1, 3, 5, 10).forEach { radius ->
                        FilterChip(
                            selected = selectedRadius == radius,
                            onClick = { selectedRadius = radius },
                            label = { Text("${radius}km") }
                        )
                    }
                }

                // Interactive Map Canvas Area
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Background Radar/Grid Canvas
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val center = Offset(size.width / 2, size.height / 2)
                            val maxRadius = minOf(size.width, size.height) * 0.45f
                            
                            // Draw radar rings
                            for (i in 1..3) {
                                drawCircle(
                                    color = Primary.copy(alpha = 0.15f),
                                    radius = maxRadius * (i / 3f),
                                    center = center,
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                                        width = 2f,
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                    )
                                )
                            }
                            
                            // User Center Marker
                            drawCircle(
                                color = Primary.copy(alpha = 0.3f),
                                radius = 24f,
                                center = center
                            )
                            drawCircle(
                                color = Primary,
                                radius = 10f,
                                center = center
                            )
                        }

                        // Plotted Price Pills for each hostel
                        displayHostels.forEachIndexed { index, hostel ->
                            // Calculate pseudo-coordinates or deterministic layout around radar
                            val angle = (index * 137.5f) % 360f // Golden angle for even dispersion
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
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (selectedHostel?.id == hostel.id) Secondary else Primary,
                                    shadowElevation = 6.dp,
                                    modifier = Modifier.border(2.dp, Color.White, RoundedCornerShape(14.dp))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Hotel,
                                            null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            "Rs ${hostel.price.toInt() / 1000}k",
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        // Map watermark/indicator
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(12.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                        ) {
                            Text(
                                "Tap price pins to view details • Center is your location",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                // List header
                Text(
                    "${displayHostels.size} Hostels within ${selectedRadius}km radius",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                // List View of Nearby Hostels
                LazyColumn(
                    modifier = Modifier.weight(0.8f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(displayHostels) { hostel ->
                        NearbyHostelItem(
                            hostel = hostel,
                            onClick = { onHostelClick(hostel.id) }
                        )
                    }
                }
            }

            // Bottom Preview Sheet / Card for Selected Pin
            AnimatedVisibility(
                visible = selectedHostel != null,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                selectedHostel?.let { hostel ->
                    Card(
                        onClick = { onHostelClick(hostel.id) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(hostel.images.firstOrNull() ?: "")
                                    .crossfade(true)
                                    .build(),
                                contentDescription = hostel.name,
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(hostel.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(
                                    hostel.location,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Spacer(Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, null, tint = Accent, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("${hostel.rating}", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                IconButton(onClick = { selectedHostel = null }) {
                                    Icon(Icons.Default.Close, "Close")
                                }
                                Text("Rs ${hostel.price.toInt()}/mo", fontWeight = FontWeight.Bold, color = Primary, style = MaterialTheme.typography.labelLarge)
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
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Primary.copy(alpha = 0.1f)
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.NearMe, null, tint = Primary, modifier = Modifier.size(18.dp))
                    Text("1.2km", style = MaterialTheme.typography.labelSmall, color = Primary)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(hostel.name, fontWeight = FontWeight.SemiBold)
                Text(hostel.location, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
            Text("Rs ${hostel.price.toInt()}/mo", fontWeight = FontWeight.Bold, color = Primary)
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
