package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GhanaEmeraldGreen
import com.example.ui.theme.GhanaGoldAccent
import com.example.ui.theme.GhanaNavyPrimary
import com.example.ui.viewmodel.SchoolViewModel

@Composable
fun LoginScreen(
    viewModel: SchoolViewModel,
    onLoginSuccess: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val schoolName by viewModel.schoolName.collectAsState()
    val activeUserAccount by viewModel.activeUserAccount.collectAsState()

    var selectedPortal by remember { mutableStateOf("PROPRIETOR") } // "PROPRIETOR", "TEACHER", "GUARDIAN"
    var emailInput by remember { mutableStateOf(activeUserAccount?.email ?: "proprietor@sttalafor.edu.gh") }
    var passwordInput by remember { mutableStateOf("••••••••") }
    var fullNameInput by remember { mutableStateOf(activeUserAccount?.fullName ?: "Dr. Kwame Addo") }
    var phoneInput by remember { mutableStateOf(activeUserAccount?.phone ?: "0244123456") }
    var schoolNameInput by remember { mutableStateOf(schoolName) }

    var isPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Hero Logo & School Header
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(GhanaNavyPrimary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.School,
                contentDescription = "School Logo",
                tint = GhanaGoldAccent,
                modifier = Modifier.size(40.dp)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = schoolNameInput.ifBlank { "St. Talafor Primary & JHS" },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = GhanaNavyPrimary,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Unified Management & Learning Portal",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // --- STEP 1: PORTAL TYPE SELECTOR ---
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "1. Select Portal Type",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = GhanaNavyPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PortalTypeChip(
                        title = "Proprietor",
                        subtitle = "Admin Access",
                        icon = Icons.Default.AdminPanelSettings,
                        isSelected = selectedPortal == "PROPRIETOR",
                        onClick = {
                            selectedPortal = "PROPRIETOR"
                            if (emailInput.contains("teacher") || emailInput.contains("guardian")) {
                                emailInput = "proprietor@sttalafor.edu.gh"
                                fullNameInput = "Dr. Kwame Addo"
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("portal_select_proprietor")
                    )

                    PortalTypeChip(
                        title = "Teacher",
                        subtitle = "Workspace",
                        icon = Icons.Default.MenuBook,
                        isSelected = selectedPortal == "TEACHER",
                        onClick = {
                            selectedPortal = "TEACHER"
                            emailInput = "teacher.mensah@sttalafor.edu.gh"
                            fullNameInput = "Mr. Emmanuel Mensah"
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("portal_select_teacher")
                    )

                    PortalTypeChip(
                        title = "Guardian",
                        subtitle = "Parent/Student",
                        icon = Icons.Default.FamilyRestroom,
                        isSelected = selectedPortal == "GUARDIAN",
                        onClick = {
                            selectedPortal = "GUARDIAN"
                            emailInput = "parent.serwaa@gmail.com"
                            fullNameInput = "Madam Serwaa Mensah"
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("portal_select_guardian")
                    )
                }
            }
        }

        // --- STEP 2: CREDENTIALS INPUT FORM ---
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "2. Enter Credentials",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = GhanaNavyPrimary
                )

                if (errorMessage != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Text(
                                text = errorMessage ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                // Full Name Input
                OutlinedTextField(
                    value = fullNameInput,
                    onValueChange = {
                        fullNameInput = it
                        errorMessage = null
                    },
                    label = { Text("Full Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_fullname_input")
                )

                // Email / Username Input
                OutlinedTextField(
                    value = emailInput,
                    onValueChange = {
                        emailInput = it
                        errorMessage = null
                    },
                    label = { Text("Email Address / Staff ID") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_email_input")
                )

                // Password Input
                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = {
                        passwordInput = it
                        errorMessage = null
                    },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle Password"
                            )
                        }
                    },
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_password_input")
                )

                // Phone Input
                OutlinedTextField(
                    value = phoneInput,
                    onValueChange = { phoneInput = it },
                    label = { Text("Contact Phone (Optional)") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // School Name Input
                OutlinedTextField(
                    value = schoolNameInput,
                    onValueChange = { schoolNameInput = it },
                    label = { Text("School Name") },
                    leadingIcon = { Icon(Icons.Default.School, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Login Submit Button
                Button(
                    onClick = {
                        when {
                            fullNameInput.isBlank() -> {
                                errorMessage = "Please enter your full name."
                            }
                            emailInput.isBlank() || !emailInput.contains("@") -> {
                                errorMessage = "Please enter a valid email address."
                            }
                            passwordInput.length < 4 -> {
                                errorMessage = "Password must be at least 4 characters."
                            }
                            else -> {
                                isLoading = true
                                viewModel.signUpOrLoginUser(
                                    fullName = fullNameInput,
                                    email = emailInput,
                                    phone = phoneInput,
                                    role = selectedPortal,
                                    schoolNameInput = schoolNameInput
                                )
                                isLoading = false
                                onLoginSuccess()
                            }
                        }
                    },
                    enabled = !isLoading,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GhanaNavyPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("login_submit_button")
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Login, contentDescription = null)
                            Text(
                                text = "Sign In as $selectedPortal",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }

        // --- DEMO PRESET QUICK LOGIN BUTTONS ---
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = GhanaNavyPrimary.copy(alpha = 0.04f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Quick Demo Access (Single Click)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = GhanaNavyPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.signUpOrLoginUser(
                                fullName = "Dr. Kwame Addo",
                                email = "proprietor@sttalafor.edu.gh",
                                phone = "0244123456",
                                role = "PROPRIETOR",
                                schoolNameInput = "St. Talafor Primary & JHS"
                            )
                            onLoginSuccess()
                        },
                        modifier = Modifier.weight(1f).testTag("demo_login_proprietor")
                    ) {
                        Text("Proprietor", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.signUpOrLoginUser(
                                fullName = "Mr. Emmanuel Mensah",
                                email = "teacher.mensah@sttalafor.edu.gh",
                                phone = "0244888999",
                                role = "TEACHER",
                                schoolNameInput = "St. Talafor Primary & JHS"
                            )
                            onLoginSuccess()
                        },
                        modifier = Modifier.weight(1f).testTag("demo_login_teacher")
                    ) {
                        Text("Teacher", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.signUpOrLoginUser(
                                fullName = "Madam Serwaa Mensah",
                                email = "parent.serwaa@gmail.com",
                                phone = "0244111222",
                                role = "GUARDIAN",
                                schoolNameInput = "St. Talafor Primary & JHS"
                            )
                            onLoginSuccess()
                        },
                        modifier = Modifier.weight(1f).testTag("demo_login_guardian")
                    ) {
                        Text("Guardian", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun PortalTypeChip(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) GhanaNavyPrimary else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.height(84.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) GhanaGoldAccent else GhanaNavyPrimary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = if (isSelected) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
