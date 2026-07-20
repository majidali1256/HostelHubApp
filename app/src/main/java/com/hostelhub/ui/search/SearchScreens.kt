package com.hostelhub.ui.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hostelhub.data.model.Hostel
import com.hostelhub.data.repository.HostelRepository
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
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Search") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // AI Search Info
            Card(
                colors = CardDefaults.cardColors(containerColor = Secondary.copy(alpha = 0.1f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(16.dp)) {
                    Icon(Icons.Default.AutoAwesome, null, tint = Secondary)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            "AI-Powered Search",
                            fontWeight = FontWeight.SemiBold,
                            color = Secondary
                        )
                        Text(
                            "Describe what you're looking for in natural language",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g., \"Affordable hostel near university with WiFi and AC\"") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.aiSearch(searchQuery) }) {
                            Icon(Icons.Default.Send, "Search", tint = Primary)
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { viewModel.aiSearch(searchQuery) }),
                shape = RoundedCornerShape(16.dp),
                minLines = 2
            )
            
            Spacer(Modifier.height(8.dp))
            
            // Quick Suggestions
            Text(
                "Try asking:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            
            Spacer(Modifier.height(8.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SuggestionChip(
                    onClick = { 
                        searchQuery = "Cheap hostels near campus"
                        viewModel.aiSearch(searchQuery)
                    },
                    label = { Text("Near campus") }
                )
                SuggestionChip(
                    onClick = { 
                        searchQuery = "Girls hostel with mess facility"
                        viewModel.aiSearch(searchQuery)
                    },
                    label = { Text("Girls + Mess") }
                )
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Results
            when {
                uiState.isLoading -> {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Secondary)
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "AI is searching...",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
                uiState.aiResponse != null -> {
                    // AI Response Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = 0.05f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, null, tint = Primary)
                                Spacer(Modifier.width(8.dp))
                                Text("AI Recommendation", fontWeight = FontWeight.SemiBold, color = Primary)
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                uiState.aiResponse ?: "",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
                    // Results List
                    if (uiState.suggestions.isNotEmpty()) {
                        Text(
                            "AI Suggested Matches (${uiState.suggestions.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(8.dp))
                        
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            items(uiState.suggestions) { suggestion ->
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    if (!suggestion.matchReason.isNullOrBlank()) {
                                        Surface(
                                            color = Secondary.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.AutoAwesome, null, tint = Secondary, modifier = Modifier.size(16.dp))
                                                Spacer(Modifier.width(6.dp))
                                                Text(
                                                    suggestion.matchReason,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = Secondary,
                                                    fontWeight = FontWeight.Medium
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
                        }
                    } else if (uiState.results.isNotEmpty()) {
                        Text(
                            "Found ${uiState.results.size} hostels",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(8.dp))
                        
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(uiState.results) { hostel ->
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
                        colors = CardDefaults.cardColors(containerColor = Error.copy(alpha = 0.1f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.padding(16.dp)) {
                            Icon(Icons.Default.Error, null, tint = Error)
                            Spacer(Modifier.width(8.dp))
                            Text(uiState.error ?: "", color = Error)
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

// Filters Screen
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
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Filters") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, "Close")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        minPrice = ""
                        maxPrice = ""
                        selectedCategory = null
                        selectedGender = null
                        verifiedOnly = false
                        selectedAmenities = emptySet()
                    }) {
                        Text("Reset")
                    }
                }
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Button(
                    onClick = {
                        onApplyFilters(FilterOptions(
                            minPrice = minPrice.toIntOrNull(),
                            maxPrice = maxPrice.toIntOrNull(),
                            category = selectedCategory,
                            genderPreference = selectedGender,
                            verifiedOnly = verifiedOnly,
                            amenities = selectedAmenities.toList()
                        ))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Apply Filters", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Price Range
            Text("Price Range", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = minPrice,
                    onValueChange = { minPrice = it.filter { c -> c.isDigit() } },
                    modifier = Modifier.weight(1f),
                    label = { Text("Min") },
                    prefix = { Text("Rs ") },
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = maxPrice,
                    onValueChange = { maxPrice = it.filter { c -> c.isDigit() } },
                    modifier = Modifier.weight(1f),
                    label = { Text("Max") },
                    prefix = { Text("Rs ") },
                    shape = RoundedCornerShape(12.dp)
                )
            }
            
            // Room Category
            Text("Room Type", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                categories.forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = if (selectedCategory == category) null else category },
                        label = { Text(category) }
                    )
                }
            }
            
            // Gender Preference
            Text("Gender Preference", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                genderOptions.forEach { gender ->
                    FilterChip(
                        selected = selectedGender == gender,
                        onClick = { selectedGender = if (selectedGender == gender) null else gender },
                        label = { Text(gender) }
                    )
                }
            }
            
            // Verified Only
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Verified Only", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        "Show only verified hostels",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Switch(checked = verifiedOnly, onCheckedChange = { verifiedOnly = it })
            }
            
            // Amenities
            Text("Amenities", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Column {
                amenities.chunked(4).forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        row.forEach { amenity ->
                            FilterChip(
                                selected = amenity in selectedAmenities,
                                onClick = {
                                    selectedAmenities = if (amenity in selectedAmenities)
                                        selectedAmenities - amenity
                                    else
                                        selectedAmenities + amenity
                                },
                                label = { Text(amenity) }
                            )
                        }
                    }
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
