package com.hostelhub.ui.owner

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.google.gson.Gson
import com.hostelhub.data.model.*
import com.hostelhub.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertyListingScreen(
    hostelId: String? = null,
    onNavigateBack: () -> Unit,
    onListingCreated: () -> Unit,
    viewModel: OwnerHostelViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isEditMode = !hostelId.isNullOrBlank()
    
    // Find existing property if in edit mode
    val existingHostel = remember(hostelId, uiState.hostels) {
        if (isEditMode) uiState.hostels.find { it.id == hostelId } else null
    }

    var hostelName by remember { mutableStateOf(existingHostel?.name ?: "") }
    var description by remember { mutableStateOf(existingHostel?.description ?: "") }
    var location by remember { mutableStateOf(existingHostel?.location ?: "") }
    var price by remember { mutableStateOf(existingHostel?.price?.toInt()?.toString() ?: "") }
    var capacity by remember { mutableStateOf(existingHostel?.capacity?.toString() ?: "20") }
    var selectedCategory by remember { mutableStateOf(existingHostel?.category ?: RoomCategory.all.first()) }
    var selectedGender by remember { mutableStateOf(existingHostel?.genderPreference ?: GenderPreference.all.first()) }
    var selectedAmenities by remember { mutableStateOf(existingHostel?.amenities?.toSet() ?: setOf("WiFi", "Security", "Geyser")) }
    
    // Coordinates & Rules
    var latitude by remember { mutableStateOf(existingHostel?.coordinates?.latitude?.takeIf { it != 0.0 }?.toString() ?: "31.5204") }
    var longitude by remember { mutableStateOf(existingHostel?.coordinates?.longitude?.takeIf { it != 0.0 }?.toString() ?: "74.3587") }
    var newRule by remember { mutableStateOf("") }
    var propertyRules by remember { mutableStateOf(existingHostel?.propertyRules ?: listOf("No Smoking inside rooms", "Quiet hours after 11 PM", "Visitors allowed in lobby only")) }

    // Room Inventory
    var roomsList by remember {
        mutableStateOf(
            existingHostel?.rooms ?: listOf(
                Room(id = "room_1", name = "Shared 3-Bed Dorm", type = "Shared", price = 15000.0, beds = listOf(Bed("b1", "Bed 1"), Bed("b2", "Bed 2"), Bed("b3", "Bed 3"))),
                Room(id = "room_2", name = "Private Single", type = "Single", price = 25000.0, beds = listOf(Bed("b4", "Bed 1")))
            )
        )
    }
    var showAddRoomDialog by remember { mutableStateOf(false) }

    // Photos
    var imageUrls by remember { mutableStateOf(existingHostel?.images ?: listOf("https://images.unsplash.com/photo-1555854877-bab0e564b8d5?w=800", "https://images.unsplash.com/photo-1595526114035-0d45ed16cfbf?w=800")) }
    var newImageUrlInput by remember { mutableStateOf("") }

    var currentStep by remember { mutableIntStateOf(0) }
    val stepLabels = listOf("Basic Info", "Location & Rules", "Rooms & Beds", "Amenities", "Photos")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Property" else "List New Property", fontWeight = FontWeight.Bold) },
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
        ) {
            // Progress Bar
            LinearProgressIndicator(
                progress = (currentStep + 1) / stepLabels.size.toFloat(),
                modifier = Modifier.fillMaxWidth(),
                color = Primary
            )

            // Step Indicator Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                stepLabels.forEachIndexed { index, label ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Surface(
                            shape = CircleShape,
                            color = if (index <= currentStep) Primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (index < currentStep) {
                                    Icon(Icons.Default.Check, null, tint = TextOnPrimary, modifier = Modifier.size(16.dp))
                                } else {
                                    Text(
                                        "${index + 1}",
                                        color = if (index <= currentStep) TextOnPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (index <= currentStep) Primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            maxLines = 1
                        )
                    }
                }
            }

            Divider()

            // Content Area
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (currentStep) {
                    0 -> {
                        // Step 1: Basic Info
                        Text("Basic Information", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Provide key details about your hostel or shared living space.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        OutlinedTextField(
                            value = hostelName,
                            onValueChange = { hostelName = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Property Name *") },
                            leadingIcon = { Icon(Icons.Default.HomeWork, null) },
                            shape = RoundedCornerShape(12.dp)
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = price,
                                onValueChange = { price = it.filter { c -> c.isDigit() || c == '.' } },
                                modifier = Modifier.weight(1f),
                                label = { Text("Base Monthly Rent *") },
                                prefix = { Text("Rs ") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = capacity,
                                onValueChange = { capacity = it.filter { c -> c.isDigit() } },
                                modifier = Modifier.weight(1f),
                                label = { Text("Total Capacity") },
                                suffix = { Text("beds") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Description & Highlights") },
                            minLines = 3,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Text("Property Category", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(RoomCategory.all) { category ->
                                FilterChip(
                                    selected = selectedCategory == category,
                                    onClick = { selectedCategory = category },
                                    label = { Text(category) }
                                )
                            }
                        }

                        Text("Gender Preference", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(GenderPreference.all) { gender ->
                                FilterChip(
                                    selected = selectedGender == gender,
                                    onClick = { selectedGender = gender },
                                    label = { Text(gender.replaceFirstChar { it.uppercase() }) }
                                )
                            }
                        }
                    }
                    1 -> {
                        // Step 2: Location, Coordinates & Property Rules
                        Text("Location & Property Rules", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Set exact address, map pin coordinates, and house rules.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Street Address / Area / Sector *") },
                            leadingIcon = { Icon(Icons.Default.LocationOn, null) },
                            shape = RoundedCornerShape(12.dp)
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = latitude,
                                onValueChange = { latitude = it },
                                modifier = Modifier.weight(1f),
                                label = { Text("Latitude") },
                                leadingIcon = { Icon(Icons.Outlined.Map, null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = longitude,
                                onValueChange = { longitude = it },
                                modifier = Modifier.weight(1f),
                                label = { Text("Longitude") },
                                leadingIcon = { Icon(Icons.Outlined.Map, null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.GpsFixed, null, tint = Primary)
                                Spacer(Modifier.width(8.dp))
                                Text("Map pinning enables accurate student nearby search and distance calculation.", style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                        Text("Property Rules for Tenants", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = newRule,
                                onValueChange = { newRule = it },
                                modifier = Modifier.weight(1f),
                                label = { Text("Add House Rule") },
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (newRule.isNotBlank()) {
                                        propertyRules = propertyRules + newRule.trim()
                                        newRule = ""
                                    }
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Add, null)
                            }
                        }

                        propertyRules.forEach { rule ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Outlined.Rule, null, modifier = Modifier.size(18.dp), tint = Primary)
                                        Spacer(Modifier.width(8.dp))
                                        Text(rule, style = MaterialTheme.typography.bodyMedium)
                                    }
                                    IconButton(
                                        onClick = { propertyRules = propertyRules - rule },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                    2 -> {
                        // Step 3: Room & Bed Inventory Configuration
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Room & Bed Inventory", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                Text("Configure individual rooms and available beds.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Button(onClick = { showAddRoomDialog = true }, shape = RoundedCornerShape(12.dp)) {
                                Icon(Icons.Default.Add, null)
                                Spacer(Modifier.width(4.dp))
                                Text("Add Room")
                            }
                        }

                        if (roomsList.isEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Outlined.Bed, null, modifier = Modifier.size(48.dp), tint = Primary)
                                    Spacer(Modifier.height(8.dp))
                                    Text("No rooms configured yet", fontWeight = FontWeight.SemiBold)
                                    Text("Add rooms so students can view and select beds during booking.", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        } else {
                            roomsList.forEachIndexed { idx, room ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Outlined.MeetingRoom, null, tint = Primary)
                                                Spacer(Modifier.width(8.dp))
                                                Text(room.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                            }
                                            Surface(
                                                color = Secondary.copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    "${room.type} • Rs ${room.price.toInt()}/mo",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = Secondary,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }

                                        Spacer(Modifier.height(8.dp))
                                        Text("Beds Inventory (${room.beds.size}):", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                                        Spacer(Modifier.height(4.dp))

                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            room.beds.forEach { bed ->
                                                Surface(
                                                    color = if (bed.isOccupied) Error.copy(alpha = 0.15f) else Success.copy(alpha = 0.15f),
                                                    shape = RoundedCornerShape(6.dp)
                                                ) {
                                                    Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(
                                                            Icons.Outlined.Bed,
                                                            null,
                                                            tint = if (bed.isOccupied) Error else Success,
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                        Spacer(Modifier.width(4.dp))
                                                        Text(
                                                            bed.bedNumber,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = if (bed.isOccupied) Error else Success
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                            TextButton(onClick = { roomsList = roomsList.filterIndexed { i, _ -> i != idx } }) {
                                                Icon(Icons.Default.Delete, null, tint = Error, modifier = Modifier.size(16.dp))
                                                Spacer(Modifier.width(4.dp))
                                                Text("Remove Room", color = Error, style = MaterialTheme.typography.labelMedium)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    3 -> {
                        // Step 4: Amenities
                        Text("Amenities & Facilities", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Select all amenities provided in this property.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        val allAmenities = Amenities.common
                        allAmenities.chunked(2).forEach { pair ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                pair.forEach { amenity ->
                                    val isSelected = amenity in selectedAmenities
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            selectedAmenities = if (isSelected) selectedAmenities - amenity else selectedAmenities + amenity
                                        },
                                        label = { Text(amenity) },
                                        leadingIcon = if (isSelected) {
                                            { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                                        } else null,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (pair.size == 1) Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                    4 -> {
                        // Step 5: Photos
                        Text("Property Photos", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("High quality photos attract 3x more students and improve fraud trust scores.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = newImageUrlInput,
                                onValueChange = { newImageUrlInput = it },
                                modifier = Modifier.weight(1f),
                                label = { Text("Add Photo URL") },
                                placeholder = { Text("https://...") },
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (newImageUrlInput.isNotBlank()) {
                                        imageUrls = imageUrls + newImageUrlInput.trim()
                                        newImageUrlInput = ""
                                    }
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.AddPhotoAlternate, null)
                            }
                        }

                        if (imageUrls.isEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth().height(140.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("No photos added yet. Add at least 1 image URL.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        } else {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(imageUrls) { url ->
                                    Box(modifier = Modifier.size(130.dp)) {
                                        AsyncImage(
                                            model = url,
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                        IconButton(
                                            onClick = { imageUrls = imageUrls - url },
                                            modifier = Modifier.align(Alignment.TopEnd)
                                        ) {
                                            Surface(shape = CircleShape, color = Error) {
                                                Icon(Icons.Default.Close, null, tint = TextOnPrimary, modifier = Modifier.padding(4.dp).size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Navigation Actions
            Surface(
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (currentStep > 0) {
                        OutlinedButton(
                            onClick = { currentStep-- },
                            modifier = Modifier.weight(1f).height(52.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.ArrowBack, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Back")
                        }
                    }

                    Button(
                        onClick = {
                            if (currentStep < stepLabels.size - 1) {
                                currentStep++
                            } else {
                                // Submit
                                val roomsJson = Gson().toJson(roomsList)
                                val latVal = latitude.toDoubleOrNull() ?: 31.5204
                                val lngVal = longitude.toDoubleOrNull() ?: 74.3587
                                val priceVal = price.toDoubleOrNull() ?: 15000.0
                                val capVal = capacity.toIntOrNull() ?: 20

                                if (isEditMode && existingHostel != null) {
                                    viewModel.updateListing(
                                        id = existingHostel.id,
                                        name = hostelName,
                                        location = location,
                                        price = priceVal,
                                        capacity = capVal,
                                        description = description,
                                        amenities = selectedAmenities.toList(),
                                        category = selectedCategory,
                                        genderPreference = selectedGender,
                                        images = imageUrls,
                                        propertyRules = propertyRules,
                                        roomsJson = roomsJson,
                                        latitude = latVal,
                                        longitude = lngVal,
                                        onSuccess = onListingCreated
                                    )
                                } else {
                                    viewModel.createListing(
                                        name = hostelName,
                                        location = location,
                                        price = priceVal,
                                        capacity = capVal,
                                        description = description,
                                        amenities = selectedAmenities.toList(),
                                        category = selectedCategory,
                                        genderPreference = selectedGender,
                                        images = imageUrls,
                                        propertyRules = propertyRules,
                                        roomsJson = roomsJson,
                                        latitude = latVal,
                                        longitude = lngVal,
                                        onSuccess = onListingCreated
                                    )
                                }
                            }
                        },
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !uiState.isSubmitting && (hostelName.isNotBlank() && location.isNotBlank() && price.isNotBlank())
                    ) {
                        if (uiState.isSubmitting) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = TextOnPrimary)
                        } else if (currentStep < stepLabels.size - 1) {
                            Text("Next")
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.Default.ArrowForward, null)
                        } else {
                            Icon(Icons.Default.Check, null)
                            Spacer(Modifier.width(8.dp))
                            Text(if (isEditMode) "Update Listing" else "Publish Property")
                        }
                    }
                }
            }
        }
    }

    // Add Room Dialog
    if (showAddRoomDialog) {
        var roomName by remember { mutableStateOf("Room ${roomsList.size + 101}") }
        var roomType by remember { mutableStateOf("Shared") }
        var roomPrice by remember { mutableStateOf(price) }
        var bedCount by remember { mutableStateOf("3") }

        AlertDialog(
            onDismissRequest = { showAddRoomDialog = false },
            title = { Text("Configure Room & Beds", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = roomName,
                        onValueChange = { roomName = it },
                        label = { Text("Room Name / Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = roomPrice,
                        onValueChange = { roomPrice = it.filter { c -> c.isDigit() } },
                        label = { Text("Room Monthly Rent (Rs)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = bedCount,
                        onValueChange = { bedCount = it.filter { c -> c.isDigit() } },
                        label = { Text("Number of Beds in Room") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Room Type", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Single", "Double", "Triple", "Shared").forEach { type ->
                            FilterChip(
                                selected = roomType == type,
                                onClick = { roomType = type },
                                label = { Text(type, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val count = bedCount.toIntOrNull()?.coerceAtLeast(1) ?: 1
                        val beds = (1..count).map { i -> Bed(id = "b_${System.currentTimeMillis()}_$i", bedNumber = "Bed $i") }
                        val newRoom = Room(
                            id = "r_${System.currentTimeMillis()}",
                            name = roomName.ifBlank { "Room ${roomsList.size + 1}" },
                            type = roomType,
                            price = roomPrice.toDoubleOrNull() ?: 15000.0,
                            beds = beds
                        )
                        roomsList = roomsList + newRoom
                        showAddRoomDialog = false
                    }
                ) {
                    Text("Add Room")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddRoomDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
