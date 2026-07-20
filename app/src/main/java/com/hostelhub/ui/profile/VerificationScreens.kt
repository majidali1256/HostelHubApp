package com.hostelhub.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.hostelhub.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdentityVerificationScreen(
    onNavigateBack: () -> Unit,
    viewModel: VerificationViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    var selectedDocuments by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var selectedDocType by remember { mutableStateOf("National ID Card (CNIC)") }
    
    val documentPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        selectedDocuments = selectedDocuments + uris
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Identity Verification") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Current Status Card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = when (uiState.status.lowercase()) {
                            "verified" -> Success.copy(alpha = 0.15f)
                            "pending" -> Warning.copy(alpha = 0.15f)
                            "rejected" -> Error.copy(alpha = 0.15f)
                            else -> Secondary.copy(alpha = 0.1f)
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = when (uiState.status.lowercase()) {
                                "verified" -> Icons.Default.CheckCircle
                                "pending" -> Icons.Default.AccessTime
                                "rejected" -> Icons.Default.Error
                                else -> Icons.Default.Info
                            },
                            contentDescription = null,
                            tint = when (uiState.status.lowercase()) {
                                "verified" -> Success
                                "pending" -> Warning
                                "rejected" -> Error
                                else -> Secondary
                            }
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Status: ${uiState.status.replaceFirstChar { it.uppercase() }}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = when (uiState.status.lowercase()) {
                                    "verified" -> Success
                                    "pending" -> Warning
                                    "rejected" -> Error
                                    else -> Primary
                                }
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "Trust Score: ${uiState.trustScore}/100",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                if (!uiState.rejectionReason.isNullOrBlank() && uiState.status.lowercase() == "rejected") {
                    Spacer(Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Error.copy(alpha = 0.1f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Rejection Reason:", fontWeight = FontWeight.Bold, color = Error)
                            Text(uiState.rejectionReason!!, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                if (!uiState.error.isNullOrBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Error.copy(alpha = 0.1f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, null, tint = Error)
                            Spacer(Modifier.width(8.dp))
                            Text(uiState.error!!, color = Error, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                
                Spacer(Modifier.height(20.dp))
                
                Text(
                    "Select Document Type",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(Modifier.height(8.dp))
                
                // Document Type Selector Chips
                val docTypes = listOf("National ID Card (CNIC)", "Passport", "Selfie with ID")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    docTypes.forEach { type ->
                        FilterChip(
                            selected = selectedDocType == type,
                            onClick = { selectedDocType = type },
                            label = { Text(type, style = MaterialTheme.typography.bodySmall) }
                        )
                    }
                }
                
                Spacer(Modifier.height(20.dp))
                
                Text(
                    "Required Documents",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(Modifier.height(8.dp))
                
                DocumentRequirement("National ID Card (CNIC)", "Front and back (max 5MB)")
                DocumentRequirement("Passport", "Official identity page clearly visible")
                DocumentRequirement("Selfie with ID", "Hold your ID next to your face")
                
                Spacer(Modifier.height(24.dp))
                
                // Upload Section
                Text(
                    "Upload Documents",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(Modifier.height(12.dp))
                
                // Selected Documents Preview
                if (selectedDocuments.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(selectedDocuments) { uri ->
                            Box {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(RoundedCornerShape(22.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                IconButton(
                                    onClick = { selectedDocuments = selectedDocuments - uri },
                                    modifier = Modifier.align(Alignment.TopEnd)
                                ) {
                                    Icon(Icons.Default.Close, null, tint = Error)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
                
                // Upload Button
                OutlinedButton(
                    onClick = { documentPicker.launch("image/*") },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Select Document Image")
                }
                
                Spacer(Modifier.height(24.dp))
                
                // Submit Section
                if (uiState.isSubmitted || uiState.status.lowercase() == "pending" || uiState.status.lowercase() == "verified") {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (uiState.status.lowercase() == "verified") Success.copy(alpha = 0.1f) else Warning.copy(alpha = 0.1f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (uiState.status.lowercase() == "verified") Icons.Default.CheckCircle else Icons.Default.AccessTime,
                                null,
                                tint = if (uiState.status.lowercase() == "verified") Success else Warning
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    if (uiState.status.lowercase() == "verified") "Verification Approved" else "Verification Submitted",
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (uiState.status.lowercase() == "verified") Success else Warning
                                )
                                Text(
                                    if (uiState.status.lowercase() == "verified") "Your identity is verified. You now have full platform privileges." else "Your documents are under review by admin. This usually takes 1-2 business days.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                    if (uiState.status.lowercase() == "pending") {
                        Spacer(Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = { selectedDocuments = emptyList() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Upload Another / Re-submit")
                        }
                    }
                } else {
                    Button(
                        onClick = {
                            viewModel.submitVerificationDocument(context, selectedDocuments, selectedDocType)
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = CircleShape,
                        enabled = selectedDocuments.isNotEmpty() && !uiState.isSubmitting
                    ) {
                        if (uiState.isSubmitting) {
                            CircularProgressIndicator(Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Icon(Icons.Default.Done, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Submit for Verification", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DocumentRequirement(title: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.RadioButtonUnchecked,
            null,
            tint = Primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

// Settings Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isDarkPref by ThemePreferenceManager.getThemeModeFlow(context).collectAsState(initial = null)
    val darkMode = isDarkPref ?: isAppInDarkTheme()
    var notifications by remember { mutableStateOf(true) }
    var emailNotifications by remember { mutableStateOf(true) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
                .verticalScroll(rememberScrollState())
        ) {
            // Appearance
            SettingsSection(title = "Appearance") {
                SettingsToggleItem(
                    icon = Icons.Default.Settings,
                    title = "Dark Mode",
                    subtitle = "Use dark theme across all screens",
                    checked = darkMode,
                    onCheckedChange = { isChecked ->
                        coroutineScope.launch {
                            ThemePreferenceManager.setDarkMode(context, isChecked)
                        }
                    }
                )
            }
            
            // Notifications
            SettingsSection(title = "Notifications") {
                SettingsToggleItem(
                    icon = Icons.Default.Notifications,
                    title = "Push Notifications",
                    subtitle = "Receive push notifications",
                    checked = notifications,
                    onCheckedChange = { notifications = it }
                )
                SettingsToggleItem(
                    icon = Icons.Default.Email,
                    title = "Email Notifications",
                    subtitle = "Receive email updates",
                    checked = emailNotifications,
                    onCheckedChange = { emailNotifications = it }
                )
            }
            
            // About
            SettingsSection(title = "About") {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "App Version",
                    subtitle = "1.0.0",
                    onClick = {}
                )
                SettingsItem(
                    icon = Icons.Default.Lock,
                    title = "Privacy Policy",
                    subtitle = "Read our privacy policy",
                    onClick = {}
                )
                SettingsItem(
                    icon = Icons.Default.Description,
                    title = "Terms of Service",
                    subtitle = "Read our terms",
                    onClick = {}
                )
            }
            
            // Support
            SettingsSection(title = "Support") {
                SettingsItem(
                    icon = Icons.Default.Help,
                    title = "Help Center",
                    subtitle = "Get help with the app",
                    onClick = {}
                )
                SettingsItem(
                    icon = Icons.Default.Warning,
                    title = "Report a Bug",
                    subtitle = "Help us improve",
                    onClick = {}
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.titleSmall,
            color = Primary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        content()
    }
}

@Composable
private fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle, style = MaterialTheme.typography.bodySmall) },
        leadingContent = { Icon(icon, null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)) },
        trailingContent = { Icon(Icons.Default.KeyboardArrowRight, null) },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@Composable
private fun SettingsToggleItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle, style = MaterialTheme.typography.bodySmall) },
        leadingContent = { Icon(icon, null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)) },
        trailingContent = { Switch(checked = checked, onCheckedChange = onCheckedChange) }
    )
}

