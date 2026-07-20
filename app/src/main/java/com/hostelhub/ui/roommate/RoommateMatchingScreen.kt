package com.hostelhub.ui.roommate

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.hostelhub.ui.theme.*

data class RoommateMatch(
    val id: String,
    val name: String,
    val profilePicture: String? = null,
    val matchPercentage: Int,
    val age: Int,
    val occupation: String,
    val budget: Int,
    val interests: List<String>,
    val habits: List<String>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoommateMatchingScreen(
    onNavigateBack: () -> Unit,
    onViewProfile: (String) -> Unit,
    onSendRequest: (String) -> Unit
) {
    var showPreferencesSheet by remember { mutableStateOf(false) }
    
    // Sample matches
    val matches = remember {
        listOf(
            RoommateMatch(
                id = "1",
                name = "Ahmed Khan",
                matchPercentage = 92,
                age = 22,
                occupation = "Student",
                budget = 12000,
                interests = listOf("Gaming", "Sports", "Music"),
                habits = listOf("Early Riser", "Non-Smoker", "Clean")
            ),
            RoommateMatch(
                id = "2",
                name = "Ali Hassan",
                matchPercentage = 87,
                age = 24,
                occupation = "Software Engineer",
                budget = 15000,
                interests = listOf("Reading", "Movies", "Tech"),
                habits = listOf("Night Owl", "Quiet", "Organized")
            ),
            RoommateMatch(
                id = "3",
                name = "Usman Malik",
                matchPercentage = 78,
                age = 21,
                occupation = "Student",
                budget = 10000,
                interests = listOf("Cricket", "Photography"),
                habits = listOf("Social", "Non-Smoker")
            )
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Find Roommates") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showPreferencesSheet = true }) {
                        Icon(Icons.Default.Tune, "Preferences")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Secondary.copy(alpha = 0.1f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(16.dp)) {
                        Icon(Icons.Default.Groups, null, tint = Secondary)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("AI-Powered Matching", fontWeight = FontWeight.SemiBold, color = Secondary)
                            Text(
                                "We analyze preferences, habits, and lifestyle to find compatible roommates",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
            
            item {
                Text(
                    "${matches.size} Potential Matches",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            // Match Cards
            items(matches) { match ->
                RoommateMatchCard(
                    match = match,
                    onViewProfile = { onViewProfile(match.id) },
                    onSendRequest = { onSendRequest(match.id) }
                )
            }
        }
    }
    
    // Preferences Bottom Sheet
    if (showPreferencesSheet) {
        RoommatePreferencesSheet(
            onDismiss = { showPreferencesSheet = false }
        )
    }
}

@Composable
private fun RoommateMatchCard(
    match: RoommateMatch,
    onViewProfile: () -> Unit,
    onSendRequest: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Profile Picture
                if (match.profilePicture != null) {
                    AsyncImage(
                        model = match.profilePicture,
                        contentDescription = null,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Surface(
                        shape = CircleShape,
                        color = Primary.copy(alpha = 0.1f),
                        modifier = Modifier.size(60.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                match.name.first().uppercase(),
                                style = MaterialTheme.typography.headlineMedium,
                                color = Primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                Spacer(Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(match.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        "${match.occupation} • ${match.age} years",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                
                // Match Percentage
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when {
                        match.matchPercentage >= 80 -> Success.copy(alpha = 0.1f)
                        match.matchPercentage >= 60 -> Primary.copy(alpha = 0.1f)
                        else -> Warning.copy(alpha = 0.1f)
                    }
                ) {
                    Text(
                        "${match.matchPercentage}%",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontWeight = FontWeight.Bold,
                        color = when {
                            match.matchPercentage >= 80 -> Success
                            match.matchPercentage >= 60 -> Primary
                            else -> Warning
                        }
                    )
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            // Budget
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AttachMoney, null, modifier = Modifier.size(16.dp), tint = Primary)
                Spacer(Modifier.width(4.dp))
                Text(
                    "Budget: Rs ${match.budget}/mo",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(Modifier.height(12.dp))
            
            // Interests
            Text("Interests", style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(4.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(match.interests) { interest ->
                    SuggestionChip(
                        onClick = {},
                        label = { Text(interest, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
            
            Spacer(Modifier.height(8.dp))
            
            // Habits
            Text("Habits", style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(4.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(match.habits) { habit ->
                    AssistChip(
                        onClick = {},
                        label = { Text(habit, style = MaterialTheme.typography.labelSmall) },
                        leadingIcon = { Icon(Icons.Default.Check, null, Modifier.size(14.dp)) }
                    )
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Action Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onViewProfile,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Person, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("View Profile")
                }
                
                Button(
                    onClick = onSendRequest,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PersonAdd, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Connect")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoommatePreferencesSheet(onDismiss: () -> Unit) {
    var budget by remember { mutableStateOf(15000f) }
    var selectedGender by remember { mutableStateOf("Any") }
    var selectedOccupation by remember { mutableStateOf("Any") }
    var isSmoker by remember { mutableStateOf(false) }
    var isEarlyRiser by remember { mutableStateOf(true) }
    
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Roommate Preferences", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(24.dp))
            
            // Budget
            Text("Maximum Budget: Rs ${budget.toInt()}")
            Slider(
                value = budget,
                onValueChange = { budget = it },
                valueRange = 5000f..50000f,
                steps = 9
            )
            
            Spacer(Modifier.height(16.dp))
            
            // Gender
            Text("Gender Preference", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Any", "Male", "Female").forEach { gender ->
                    FilterChip(
                        selected = selectedGender == gender,
                        onClick = { selectedGender = gender },
                        label = { Text(gender) }
                    )
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Occupation
            Text("Occupation", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Any", "Student", "Professional").forEach { occ ->
                    FilterChip(
                        selected = selectedOccupation == occ,
                        onClick = { selectedOccupation = occ },
                        label = { Text(occ) }
                    )
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Lifestyle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Non-Smoker Only")
                Switch(checked = !isSmoker, onCheckedChange = { isSmoker = !it })
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Early Riser")
                Switch(checked = isEarlyRiser, onCheckedChange = { isEarlyRiser = it })
            }
            
            Spacer(Modifier.height(24.dp))
            
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Apply Preferences", fontWeight = FontWeight.SemiBold)
            }
            
            Spacer(Modifier.height(32.dp))
        }
    }
}
