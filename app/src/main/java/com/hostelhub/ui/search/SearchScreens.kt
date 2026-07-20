package com.hostelhub.ui.search

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import com.hostelhub.ui.theme.isAppInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hostelhub.data.model.Hostel
import com.hostelhub.data.repository.HostelRepository
import com.hostelhub.ui.components.*
import com.hostelhub.ui.home.HostelCard
import com.hostelhub.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AISearchScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHostel: (String) -> Unit,
    viewModel: AISearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val isDark = isAppInDarkTheme()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "AI Smart Discovery", 
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            // AI Search Info Banner (~26px TripGlide elevated card)
            Card(
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = Secondary.copy(alpha = if (isDark) 0.16f else 0.1f)),
                border = BorderStroke(1.dp, Secondary.copy(alpha = 0.35f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = if (isDark) 0.dp else 8.dp,
                        shape = RoundedCornerShape(26.dp),
                        spotColor = Secondary.copy(alpha = 0.2f)
                    )
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Secondary,
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.AutoAwesome, null, tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(
                            "AI-Powered Concierge",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Secondary
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "Describe your ideal room or campus in natural words—our AI finds exact matches.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(20.dp))
            
            // Search Input (TripGlide Pill Shape)
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { 
                    Text(
                        "e.g., \"Affordable room near NUST with WiFi & AC\"",
                        style = MaterialTheme.typography.bodyMedium
                    ) 
                },
                leadingIcon = { 
                    Icon(
                        Icons.Default.Search, 
                        null, 
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    ) 
                },
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 6.dp)) {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Clear, "Clear", modifier = Modifier.size(18.dp))
                            }
                        }
                        Surface(
                            onClick = { viewModel.aiSearch(searchQuery) },
                            shape = CircleShape,
                            color = if (searchQuery.isNotEmpty()) Primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Send, 
                                    "Search", 
                                    tint = if (searchQuery.isNotEmpty()) TextOnPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { viewModel.aiSearch(searchQuery) }),
                shape = CircleShape,
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                    focusedBorderColor = Primary,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
            
            Spacer(Modifier.height(16.dp))
            
            // Quick Suggestions Pill Row
            Text(
                "Try asking:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
            )
            
            val quickPrompts = listOf(
                "Cheap hostels near campus",
                "Girls hostel with mess facility",
                "Single room under Rs 35k",
                "Ac & high speed WiFi"
            )
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(quickPrompts) { prompt ->
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .height(36.dp)
                            .clickable { 
                                searchQuery = prompt
                                viewModel.aiSearch(searchQuery)
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, null, tint = Secondary, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                prompt, 
                                style = MaterialTheme.typography.labelMedium, 
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(20.dp))
            
            // Results Section
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f), 
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Secondary, strokeWidth = 3.dp)
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "AI Concierge is analyzing availability...",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
                uiState.aiResponse != null -> {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // AI Recommendation Summary Card
                        item {
                            Card(
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = if (isDark) 0.16f else 0.08f)),
                                border = BorderStroke(1.dp, Primary.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(shape = CircleShape, color = Primary, modifier = Modifier.size(32.dp)) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(Icons.Default.AutoAwesome, null, tint = TextOnPrimary, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                        Spacer(Modifier.width(10.dp))
                                        Text(
                                            "AI Concierge Summary", 
                                            style = MaterialTheme.typography.titleMedium, 
                                            fontWeight = FontWeight.ExtraBold, 
                                            color = Primary
                                        )
                                    }
                                    Spacer(Modifier.height(10.dp))
                                    Text(
                                        uiState.aiResponse ?: "",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                                    )
                                }
                            }
                        }
                        
                        // Results List
                        if (uiState.suggestions.isNotEmpty()) {
                            item {
                                Text(
                                    "Suggested Matches (${uiState.suggestions.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                            
                            items(uiState.suggestions, key = { it.hostel.id }) { suggestion ->
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (!suggestion.matchReason.isNullOrBlank()) {
                                        Surface(
                                            color = Secondary.copy(alpha = if (isDark) 0.2f else 0.12f),
                                            shape = CircleShape,
                                            border = BorderStroke(1.dp, Secondary.copy(alpha = 0.3f))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.AutoAwesome, null, tint = Secondary, modifier = Modifier.size(16.dp))
                                                Spacer(Modifier.width(6.dp))
                                                Text(
                                                    suggestion.matchReason,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = Secondary,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                    HostelCard(
                                        hostel = suggestion.hostel,
                                        onClick = { onNavigateToHostel(suggestion.hostel.id) }
                                    )
                                }
                            }
                        } else if (uiState.results.isNotEmpty()) {
                            item {
                                Text(
                                    "Found ${uiState.results.size} hostels",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                            
                            items(uiState.results, key = { it.id }) { hostel ->
                                HostelCard(
                                    hostel = hostel,
                                    onClick = { onNavigateToHostel(hostel.id) }
                                )
                            }
                        }
                    }
                }
                uiState.error != null -> {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Error.copy(alpha = 0.12f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Error, null, tint = Error)
                            Spacer(Modifier.width(12.dp))
                            Text(
                                uiState.error ?: "", 
                                color = Error, 
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@HiltViewModel
class AISearchViewModel @Inject constructor(
    private val hostelRepository: HostelRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AISearchUiState())
    val uiState: StateFlow<AISearchUiState> = _uiState.asStateFlow()
    
    fun aiSearch(query: String) {
        if (query.isBlank()) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            hostelRepository.aiSearch(query)
                .onSuccess { response ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        aiResponse = response.recommendation,
                        results = response.hostels,
                        suggestions = response.suggestions
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Search failed"
                    )
                }
        }
    }
}

data class AISearchUiState(
    val isLoading: Boolean = false,
    val aiResponse: String? = null,
    val results: List<Hostel> = emptyList(),
    val suggestions: List<com.hostelhub.data.api.AISearchSuggestion> = emptyList(),
    val error: String? = null
)

// Filters Screen (TripGlide Restyled: Pill-shaped chips and inputs, spring transitions)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltersScreen(
    onNavigateBack: () -> Unit,
    onApplyFilters: (FilterOptions) -> Unit
) {
    var minPrice by remember { mutableStateOf("") }
    var maxPrice by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedGender by remember { mutableStateOf<String?>(null) }
    var verifiedOnly by remember { mutableStateOf(false) }
    var selectedAmenities by remember { mutableStateOf(setOf<String>()) }
    
    val categories = listOf("Single", "Double", "Triple", "Dormitory")
    val genderOptions = listOf("Male", "Female", "Any")
    val amenities = listOf("WiFi", "AC", "Parking", "Laundry", "Kitchen", "Security", "Gym", "Meals")
    val isDark = isAppInDarkTheme()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Refine Preferences", 
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
                            Icon(Icons.Default.Close, "Close", modifier = Modifier.size(20.dp))
                        }
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            minPrice = ""
                            maxPrice = ""
                            selectedCategory = null
                            selectedGender = null
                            verifiedOnly = false
                            selectedAmenities = emptySet()
                        }
                    ) {
                        Text(
                            "Reset", 
                            style = MaterialTheme.typography.labelLarge, 
                            fontWeight = FontWeight.Bold,
                            color = Primary,
                            textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = if (isDark) 0.dp else 12.dp,
                border = if (isDark) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)) else null
            ) {
                Button(
                    onClick = {
                        onApplyFilters(
                            FilterOptions(
                                minPrice = minPrice.toIntOrNull(),
                                maxPrice = maxPrice.toIntOrNull(),
                                category = selectedCategory,
                                genderPreference = selectedGender,
                                verifiedOnly = verifiedOnly,
                                amenities = selectedAmenities.toList()
                            )
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                        .height(54.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = TextOnPrimary),
                    elevation = if (isDark) ButtonDefaults.buttonElevation(0.dp) else ButtonDefaults.buttonElevation(6.dp)
                ) {
                    Text("Apply Filter Preferences", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(26.dp)
        ) {
            // Price Range (Pill Inputs)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Budget Range (Rs / Month)", 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.ExtraBold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    OutlinedTextField(
                        value = minPrice,
                        onValueChange = { minPrice = it.filter { c -> c.isDigit() } },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Min Price") },
                        prefix = { Text("Rs ", fontWeight = FontWeight.Bold) },
                        shape = CircleShape,
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                            focusedBorderColor = Primary
                        )
                    )
                    OutlinedTextField(
                        value = maxPrice,
                        onValueChange = { maxPrice = it.filter { c -> c.isDigit() } },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Max Price") },
                        prefix = { Text("Rs ", fontWeight = FontWeight.Bold) },
                        shape = CircleShape,
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                            focusedBorderColor = Primary
                        )
                    )
                }
            }
            
            // Room Category (TripGlide Pill Row)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Room Type", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                FlowRowOrGrid(items = categories, selectedItem = selectedCategory, onSelect = {
                    selectedCategory = if (selectedCategory == it) null else it
                })
            }
            
            // Gender Preference (TripGlide Pill Row)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Gender Preference", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                FlowRowOrGrid(items = genderOptions, selectedItem = selectedGender, onSelect = {
                    selectedGender = if (selectedGender == it) null else it
                })
            }
            
            // Verified Only Card Toggle
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, if (verifiedOnly) Success.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = if (verifiedOnly) Success else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Verified, 
                                    null, 
                                    tint = if (verifiedOnly) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text("Verified Properties Only", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(
                                "Show only inspected spaces with badge",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                            )
                        }
                    }
                    Switch(
                        checked = verifiedOnly, 
                        onCheckedChange = { verifiedOnly = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Success, checkedTrackColor = Success.copy(alpha = 0.3f))
                    )
                }
            }
            
            // Amenities (TripGlide Pill Grid)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Required Amenities", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                amenities.chunked(3).forEach { rowItems ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        rowItems.forEach { amenity ->
                            val isSelected = amenity in selectedAmenities
                            val bgColor by animateColorAsState(
                                targetValue = if (isSelected) Primary else MaterialTheme.colorScheme.surface,
                                label = "amenityBg"
                            )
                            val textColor by animateColorAsState(
                                targetValue = if (isSelected) TextOnPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                label = "amenityText"
                            )
                            
                            Surface(
                                shape = CircleShape,
                                color = bgColor,
                                border = if (!isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)) else null,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .clickable {
                                        selectedAmenities = if (isSelected) selectedAmenities - amenity else selectedAmenities + amenity
                                    }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                ) {
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, null, tint = TextOnPrimary, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = amenity,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = textColor,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                        // Fill remaining spaces in chunk if less than 3
                        repeat(3 - rowItems.size) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FlowRowOrGrid(
    items: List<String>,
    selectedItem: String?,
    onSelect: (String) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items.forEach { item ->
            val isSelected = selectedItem == item
            val bgColor by animateColorAsState(
                targetValue = if (isSelected) Primary else MaterialTheme.colorScheme.surface,
                label = "flowBg"
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) TextOnPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                label = "flowText"
            )
            
            Surface(
                shape = CircleShape,
                color = bgColor,
                border = if (!isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)) else null,
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .clickable { onSelect(item) }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    if (isSelected) {
                        Icon(Icons.Default.Check, null, tint = TextOnPrimary, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                    }
                    Text(
                        text = item,
                        style = MaterialTheme.typography.labelMedium,
                        color = textColor,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

data class FilterOptions(
    val minPrice: Int? = null,
    val maxPrice: Int? = null,
    val category: String? = null,
    val genderPreference: String? = null,
    val verifiedOnly: Boolean = false,
    val amenities: List<String> = emptyList()
)
