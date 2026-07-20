package com.hostelhub.ui.auth

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hostelhub.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(
    onNavigateToLogin: () -> Unit,
    onSignupSuccess: () -> Unit,
    viewModel: SignupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    var showAccountChooser by remember { mutableStateOf<String?>(null) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Web App Aligned Hero Header
            AuthHeader()
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Elevated Card Container (max-w-[400px] matching web app Login.tsx)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 400.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Tab Switcher inside Card matching Login.tsx
                    AuthTabSwitcher(
                        isSignUp = true,
                        onTabSelected = { isSignupSelected ->
                            if (!isSignupSelected) {
                                onNavigateToLogin()
                            }
                        }
                    )
                    
                    // Role Selection (Customer vs Owner)
                    Text(
                        text = "I am a",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(start = 4.dp, bottom = 6.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        RoleChip(
                            label = "Customer",
                            icon = Icons.Default.Person,
                            selected = uiState.role == "customer",
                            onClick = { viewModel.updateRole("customer") },
                            modifier = Modifier.weight(1f)
                        )
                        RoleChip(
                            label = "Owner",
                            icon = Icons.Default.Business,
                            selected = uiState.role == "owner",
                            onClick = { viewModel.updateRole("owner") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    // First Name & Last Name row exactly matching Login.tsx
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "First Name",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                            OutlinedTextField(
                                value = uiState.firstName,
                                onValueChange = { viewModel.updateField(SignupField.FIRST_NAME, it) },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("John") },
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Last Name",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                            OutlinedTextField(
                                value = uiState.lastName,
                                onValueChange = { viewModel.updateField(SignupField.LAST_NAME, it) },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Doe") },
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp)
                            )
                        }
                    }
                    
                    // Username matching Login.tsx
                    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)) {
                        Text(
                            text = "Username",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        OutlinedTextField(
                            value = uiState.username,
                            onValueChange = { viewModel.updateField(SignupField.USERNAME, it) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Username") },
                            leadingIcon = { Icon(Icons.Default.Person, null, tint = Primary) },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp)
                        )
                    }
                    
                    // Email Address matching Login.tsx
                    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)) {
                        Text(
                            text = "Email Address",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        OutlinedTextField(
                            value = uiState.email,
                            onValueChange = { viewModel.updateField(SignupField.EMAIL, it) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Email Address") },
                            leadingIcon = { Icon(Icons.Default.Email, null, tint = Primary) },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp)
                        )
                    }
                    
                    // Phone Number matching Login.tsx (03001234567)
                    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)) {
                        Text(
                            text = "Phone Number",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        OutlinedTextField(
                            value = uiState.contactNumber,
                            onValueChange = { viewModel.updateField(SignupField.CONTACT, it) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Phone Number (e.g. 03001234567)") },
                            leadingIcon = { Icon(Icons.Default.Phone, null, tint = Primary) },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Next
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp)
                        )
                    }
                    
                    // Password & Confirm Password matching Login.tsx
                    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)) {
                        Text(
                            text = "Password",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        OutlinedTextField(
                            value = uiState.password,
                            onValueChange = { viewModel.updateField(SignupField.PASSWORD, it) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, null, tint = Primary) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        null
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp)
                        )
                    }
                    
                    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)) {
                        Text(
                            text = "Confirm Password",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        OutlinedTextField(
                            value = uiState.confirmPassword,
                            onValueChange = { viewModel.updateField(SignupField.CONFIRM_PASSWORD, it) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Confirm") },
                            leadingIcon = { Icon(Icons.Default.Lock, null, tint = Primary) },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = { viewModel.signup(onSignupSuccess) }),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp)
                        )
                    }
                    
                    // Error Message
                    AnimatedVisibility(visible = uiState.error != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Error.copy(alpha = 0.12f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Error, null, tint = Error, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = uiState.error ?: "",
                                    color = Error,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                                )
                            }
                        }
                    }
                    
                    // Create Account Button matching Login.tsx
                    Button(
                        onClick = { viewModel.signup(onSignupSuccess) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 8.dp),
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = TextOnPrimary
                            )
                        } else {
                            Text(
                                text = "Create Account",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextOnPrimary
                            )
                        }
                    }
                    
                    // Social Login Stack matching Login.tsx
                    SocialLoginStack(
                        onGoogleClick = {
                            showAccountChooser = "Google"
                        },
                        onFacebookClick = {
                            showAccountChooser = "Facebook"
                        }
                    )
                }
            }
            
            // Footer Terms matching Login.tsx
            AuthFooter()

            if (showAccountChooser != null) {
                SocialAccountChooserDialog(
                    provider = showAccountChooser!!,
                    onDismiss = { showAccountChooser = null },
                    onAccountSelected = { account ->
                        val currentProvider = showAccountChooser!!
                        showAccountChooser = null
                        viewModel.socialLogin(
                            provider = currentProvider,
                            providerId = account.id,
                            email = account.email,
                            name = account.name
                        ) {
                            onSignupSuccess()
                        }
                    },
                    onOpenBrowserOAuth = {
                        val currentProvider = showAccountChooser!!
                        showAccountChooser = null
                        val providerPath = if (currentProvider.equals("Google", ignoreCase = true)) "google" else "facebook"
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("http://10.0.2.2:5003/api/auth/$providerPath?origin=hostelhub://oauth")
                        )
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoleChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)) },
        leadingIcon = { Icon(icon, null, modifier = Modifier.size(18.dp)) },
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Primary.copy(alpha = 0.15f),
            selectedLabelColor = Primary,
            selectedLeadingIconColor = Primary
        )
    )
}

@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var email by remember { mutableStateOf("") }
    var step by remember { mutableStateOf(1) }
    var code by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    if (step > 1) step-- else onNavigateBack()
                }) {
                    Icon(Icons.Default.ArrowBack, "Back")
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Icon(
                when (step) {
                    1 -> Icons.Default.LockReset
                    2 -> Icons.Default.Pin
                    else -> Icons.Default.VpnKey
                },
                null,
                modifier = Modifier.size(80.dp),
                tint = Primary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                when (step) {
                    1 -> "Forgot Password?"
                    2 -> "Enter Verification Code"
                    else -> "Reset Password"
                },
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                when (step) {
                    1 -> "Enter your email and we'll send you a 6-digit verification code"
                    2 -> "We sent a 6-digit verification code to $email"
                    else -> "Create a new secure password for your account"
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            when (step) {
                1 -> {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Email") },
                        leadingIcon = { Icon(Icons.Default.Email, null, tint = Primary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = { 
                            viewModel.forgotPassword(email) { step = 2 }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        enabled = !uiState.isLoading && email.isNotBlank()
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = TextOnPrimary
                            )
                        } else {
                            Text("Send Verification Code", fontWeight = FontWeight.Bold, color = TextOnPrimary)
                        }
                    }
                }
                2 -> {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { if (it.length <= 6) code = it.filter { char -> char.isDigit() } },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("6-Digit Code") },
                        leadingIcon = { Icon(Icons.Default.Pin, null, tint = Primary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 6.sp,
                            textAlign = TextAlign.Center
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = { 
                            viewModel.verifyResetCode(email, code) { step = 3 }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        enabled = !uiState.isLoading && code.length == 6
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = TextOnPrimary
                            )
                        } else {
                            Text("Verify Code", fontWeight = FontWeight.Bold, color = TextOnPrimary)
                        }
                    }
                }
                3 -> {
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("New Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = Primary) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Confirm Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = Primary) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = { 
                            if (newPassword == confirmPassword) {
                                viewModel.resetPasswordWithCode(email, code, newPassword) {
                                    onNavigateBack()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        enabled = !uiState.isLoading && newPassword.length >= 6 && newPassword == confirmPassword
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = TextOnPrimary
                            )
                        } else {
                            Text("Reset Password", fontWeight = FontWeight.Bold, color = TextOnPrimary)
                        }
                    }
                }
            }
            
            AnimatedVisibility(visible = uiState.message != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = Success.copy(alpha = 0.12f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = Success)
                        Spacer(Modifier.width(8.dp))
                        Text(uiState.message ?: "", color = Success)
                    }
                }
            }

            AnimatedVisibility(visible = uiState.error != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = Error.copy(alpha = 0.12f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Error, null, tint = Error)
                        Spacer(Modifier.width(8.dp))
                        Text(uiState.error ?: "", color = Error)
                    }
                }
            }
        }
    }
}
