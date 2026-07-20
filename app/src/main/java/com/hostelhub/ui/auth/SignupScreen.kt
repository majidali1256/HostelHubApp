package com.hostelhub.ui.auth

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Main Icon with Name
            AuthHeader()

            Spacer(modifier = Modifier.height(12.dp))

            // Elevated Card Container exactly matching Login screen style without scroll
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 400.dp),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Tab Switcher inside Card
                    AuthTabSwitcher(
                        isSignUp = true,
                        onTabSelected = { isSignupSelected ->
                            if (!isSignupSelected) {
                                onNavigateToLogin()
                            }
                        }
                    )

                    // Role Selection
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
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

                    // Row 1: First Name & Last Name
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.firstName,
                            onValueChange = { viewModel.updateField(SignupField.FIRST_NAME, it) },
                            modifier = Modifier.weight(1f),
                            label = { Text("First Name", style = MaterialTheme.typography.labelSmall) },
                            placeholder = { Text("John", style = MaterialTheme.typography.bodySmall) },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = uiState.lastName,
                            onValueChange = { viewModel.updateField(SignupField.LAST_NAME, it) },
                            modifier = Modifier.weight(1f),
                            label = { Text("Last Name", style = MaterialTheme.typography.labelSmall) },
                            placeholder = { Text("Doe", style = MaterialTheme.typography.bodySmall) },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // Row 2: Username & Phone Number side-by-side
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.username,
                            onValueChange = { viewModel.updateField(SignupField.USERNAME, it) },
                            modifier = Modifier.weight(1f),
                            label = { Text("Username", style = MaterialTheme.typography.labelSmall) },
                            placeholder = { Text("user123", style = MaterialTheme.typography.bodySmall) },
                            leadingIcon = { Icon(Icons.Default.Person, null, tint = Primary, modifier = Modifier.size(18.dp)) },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = uiState.contactNumber,
                            onValueChange = { viewModel.updateField(SignupField.CONTACT, it) },
                            modifier = Modifier.weight(1f),
                            label = { Text("Phone", style = MaterialTheme.typography.labelSmall) },
                            placeholder = { Text("03001234567", style = MaterialTheme.typography.bodySmall) },
                            leadingIcon = { Icon(Icons.Default.Phone, null, tint = Primary, modifier = Modifier.size(18.dp)) },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Next
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // Row 3: Email Address full width
                    OutlinedTextField(
                        value = uiState.email,
                        onValueChange = { viewModel.updateField(SignupField.EMAIL, it) },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        label = { Text("Email Address", style = MaterialTheme.typography.labelSmall) },
                        placeholder = { Text("name@example.com", style = MaterialTheme.typography.bodySmall) },
                        leadingIcon = { Icon(Icons.Default.Email, null, tint = Primary, modifier = Modifier.size(18.dp)) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Row 4: Password & Confirm Password side-by-side
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.password,
                            onValueChange = { viewModel.updateField(SignupField.PASSWORD, it) },
                            modifier = Modifier.weight(1f),
                            label = { Text("Password", style = MaterialTheme.typography.labelSmall) },
                            placeholder = { Text("••••••", style = MaterialTheme.typography.bodySmall) },
                            leadingIcon = { Icon(Icons.Default.Lock, null, tint = Primary, modifier = Modifier.size(18.dp)) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }, modifier = Modifier.size(28.dp)) {
                                    Icon(
                                        if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = uiState.confirmPassword,
                            onValueChange = { viewModel.updateField(SignupField.CONFIRM_PASSWORD, it) },
                            modifier = Modifier.weight(1f),
                            label = { Text("Confirm", style = MaterialTheme.typography.labelSmall) },
                            placeholder = { Text("••••••", style = MaterialTheme.typography.bodySmall) },
                            leadingIcon = { Icon(Icons.Default.Lock, null, tint = Primary, modifier = Modifier.size(18.dp)) },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = { viewModel.signup(onSignupSuccess) }),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // Error Message
                    AnimatedVisibility(visible = uiState.error != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Error.copy(alpha = 0.12f))
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Error, null, tint = Error, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = uiState.error ?: "",
                                    color = Error,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                                )
                            }
                        }
                    }

                    // Create Account Button
                    Button(
                        onClick = { viewModel.signup(onSignupSuccess) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 8.dp),
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
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

                    // Compact Social Login Buttons
                    SocialLoginRow(
                        onGoogleClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://10.0.2.2:5001/api/auth/google?origin=hostelhub://oauth"))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                android.widget.Toast.makeText(context, "Could not open browser for Google Sign-In", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        },
                        onFacebookClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://10.0.2.2:5001/api/auth/facebook?origin=hostelhub://oauth"))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                android.widget.Toast.makeText(context, "Could not open browser for Facebook Sign-In", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
        }
    }
}
