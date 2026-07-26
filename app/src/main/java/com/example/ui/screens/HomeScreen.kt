package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserAccount
import com.example.ui.theme.GhanaEmeraldGreen
import com.example.ui.theme.GhanaGoldAccent
import com.example.ui.theme.GhanaGoldContainer
import com.example.ui.theme.GhanaNavyPrimary
import com.example.ui.viewmodel.SchoolViewModel
import com.example.ui.viewmodel.ViewMode

@Composable
fun HomeScreen(
    viewModel: SchoolViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val schoolName by viewModel.schoolName.collectAsState()
    val activeUserAccount by viewModel.activeUserAccount.collectAsState()
    val unreadNotificationCount by viewModel.unreadNotificationCount.collectAsState()
    val allLessonPlans by viewModel.allLessonPlans.collectAsState()
    val allGrades by viewModel.allGrades.collectAsState()

    // Auth Form State (Embedded on Dashboard)
    var selectedAuthTab by remember { mutableStateOf(0) } // 0: Sign In, 1: Create Account
    var fullNameInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("0244123456") }
    var passwordInput by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("PROPRIETOR") }
    var schoolNameInput by remember { mutableStateOf(schoolName) }

    var isPasswordVisible by remember { mutableStateOf(false) }
    var authErrorMessage by remember { mutableStateOf<String?>(null) }
    var showCreateAccountDialog by remember { mutableStateOf(false) }

    // Account Creation Dialog
    if (showCreateAccountDialog) {
        AlertDialog(
            onDismissRequest = { showCreateAccountDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = null,
                        tint = GhanaNavyPrimary
                    )
                    Text("Create New Account", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Fill in your details to register a new user account.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    OutlinedTextField(
                        value = fullNameInput,
                        onValueChange = { fullNameInput = it },
                        label = { Text("Full Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("dialog_create_fullname")
                    )

                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Email / Staff ID") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth().testTag("dialog_create_email")
                    )

                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = { phoneInput = it },
                        label = { Text("Contact Phone") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth().testTag("dialog_create_phone")
                    )

                    Text("Select Account Role:", style = MaterialTheme.typography.labelSmall)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("PROPRIETOR", "TEACHER", "GUARDIAN").forEach { roleOption ->
                            FilterChip(
                                selected = selectedRole == roleOption,
                                onClick = { selectedRole = roleOption },
                                label = { Text(roleOption, fontSize = 11.sp) },
                                modifier = Modifier.weight(1f).testTag("dialog_role_$roleOption")
                            )
                        }
                    }

                    OutlinedTextField(
                        value = schoolNameInput,
                        onValueChange = { schoolNameInput = it },
                        label = { Text("School / Institution Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("dialog_create_school_name")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (fullNameInput.isBlank() || emailInput.isBlank()) {
                            Toast.makeText(context, "Please complete all fields", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.signUpOrLoginUser(
                            fullName = fullNameInput,
                            email = emailInput,
                            phone = phoneInput,
                            role = selectedRole,
                            schoolNameInput = schoolNameInput
                        )
                        showCreateAccountDialog = false
                        Toast.makeText(context, "Account Created Successfully!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GhanaNavyPrimary),
                    modifier = Modifier.testTag("dialog_create_account_submit")
                ) {
                    Text("Register Account")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateAccountDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- HERO DASHBOARD HEADER ---
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = GhanaNavyPrimary),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.fillMaxWidth().testTag("home_dashboard_hero_card")
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(GhanaGoldAccent),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = "School Logo",
                                    tint = GhanaNavyPrimary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = schoolName.ifBlank { "St. Talafor Academy" },
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Central School Management & Learning Portal",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GhanaGoldAccent
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = GhanaGoldAccent.copy(alpha = 0.2f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(GhanaEmeraldGreen)
                                )
                                Text(
                                    text = "Term 3 • 2026",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Dashboard Quick KPI Pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DashboardStatPill(
                            label = "Enrolled Students",
                            value = "1,248",
                            icon = Icons.Default.Group,
                            modifier = Modifier.weight(1f)
                        )
                        DashboardStatPill(
                            label = "Staff Members",
                            value = "48 Active",
                            icon = Icons.Default.Badge,
                            modifier = Modifier.weight(1f)
                        )
                        DashboardStatPill(
                            label = "Campus Status",
                            value = "Geofenced",
                            icon = Icons.Default.LocationOn,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // --- ACCOUNT STATUS & AUTHENTICATION DASHBOARD CARD ---
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth().testTag("account_session_dashboard_card")
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = GhanaNavyPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "Account Session & Security",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = GhanaNavyPrimary
                        )
                    }

                    if (activeUserAccount != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = GhanaEmeraldGreen.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GhanaEmeraldGreen, modifier = Modifier.size(12.dp))
                                Text("Signed In", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GhanaEmeraldGreen)
                            }
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(12.dp))
                                Text("Signed Out", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (activeUserAccount != null) {
                    val user = activeUserAccount!!
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = GhanaNavyPrimary.copy(alpha = 0.05f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GhanaNavyPrimary.copy(alpha = 0.15f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(GhanaNavyPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = user.fullName.take(1).uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = GhanaGoldAccent
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = user.fullName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = GhanaGoldContainer
                                    ) {
                                        Text(
                                            text = user.role,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = GhanaNavyPrimary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text("📧 ${user.email}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("📞 ${user.phone}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Sign Out Button
                        OutlinedButton(
                            onClick = {
                                viewModel.logout()
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                            modifier = Modifier.weight(1f).testTag("home_sign_out_button")
                        ) {
                            Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sign Out", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        // Create Account Button
                        Button(
                            onClick = {
                                fullNameInput = ""
                                emailInput = ""
                                showCreateAccountDialog = true
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GhanaNavyPrimary),
                            modifier = Modifier.weight(1f).testTag("home_create_account_button")
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("New Account", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "You are currently in guest mode. Sign in to your account or create a new profile to access management features.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showCreateAccountDialog = true },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f).testTag("guest_create_account_button")
                            ) {
                                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Create Account", fontSize = 12.sp)
                            }

                            Button(
                                onClick = { viewModel.openAuthDialog() },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GhanaNavyPrimary),
                                modifier = Modifier.weight(1f).testTag("guest_sign_in_button")
                            ) {
                                Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Sign In", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // --- EMBEDDED DASHBOARD AUTHENTICATION & QUICK ACCOUNT FORM ---
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth().testTag("embedded_auth_form_card")
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                TabRow(
                    selectedTabIndex = selectedAuthTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    contentColor = GhanaNavyPrimary,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedAuthTab == 0,
                        onClick = { selectedAuthTab = 0 },
                        text = { Text("Quick Sign In", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                        icon = { Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.testTag("auth_tab_sign_in")
                    )
                    Tab(
                        selected = selectedAuthTab == 1,
                        onClick = { selectedAuthTab = 1 },
                        text = { Text("Create Account", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                        icon = { Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.testTag("auth_tab_create_account")
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (authErrorMessage != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                    ) {
                        Text(
                            text = authErrorMessage ?: "",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                if (selectedAuthTab == 0) {
                    // --- SIGN IN FORM & 1-CLICK DEMO ACCOUNTS ---
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Select Target Portal Role:", style = MaterialTheme.typography.labelSmall)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("PROPRIETOR", "TEACHER", "GUARDIAN").forEach { roleOpt ->
                                FilterChip(
                                    selected = selectedRole == roleOpt,
                                    onClick = {
                                        selectedRole = roleOpt
                                        when (roleOpt) {
                                            "PROPRIETOR" -> {
                                                emailInput = "proprietor@sttalafor.edu.gh"
                                                fullNameInput = "Dr. Kwabena Mensah"
                                            }
                                            "TEACHER" -> {
                                                emailInput = "mensah@sttalafor.edu.gh"
                                                fullNameInput = "Mr. Kojo Mensah"
                                            }
                                            "GUARDIAN" -> {
                                                emailInput = "grace.mensah@gmail.com"
                                                fullNameInput = "Madam Grace Mensah"
                                            }
                                        }
                                    },
                                    label = { Text(roleOpt, fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f).testTag("chip_sign_in_role_$roleOpt")
                                )
                            }
                        }

                        OutlinedTextField(
                            value = emailInput.ifBlank { "proprietor@sttalafor.edu.gh" },
                            onValueChange = { emailInput = it },
                            label = { Text("Email Address / Username") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("home_signin_email_input")
                        )

                        OutlinedTextField(
                            value = passwordInput.ifBlank { "••••••••" },
                            onValueChange = { passwordInput = it },
                            label = { Text("Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Icon(if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = null)
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("home_signin_password_input")
                        )

                        Button(
                            onClick = {
                                val targetName = fullNameInput.ifBlank {
                                    when (selectedRole) {
                                        "PROPRIETOR" -> "Dr. Kwabena Mensah"
                                        "TEACHER" -> "Mr. Kojo Mensah"
                                        else -> "Madam Grace Mensah"
                                    }
                                }
                                val targetEmail = emailInput.ifBlank {
                                    when (selectedRole) {
                                        "PROPRIETOR" -> "proprietor@sttalafor.edu.gh"
                                        "TEACHER" -> "mensah@sttalafor.edu.gh"
                                        else -> "grace.mensah@gmail.com"
                                    }
                                }
                                viewModel.signUpOrLoginUser(
                                    fullName = targetName,
                                    email = targetEmail,
                                    phone = phoneInput,
                                    role = selectedRole,
                                    schoolNameInput = schoolName
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GhanaNavyPrimary),
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("home_signin_submit_button")
                        ) {
                            Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sign In as $selectedRole", fontWeight = FontWeight.Bold)
                        }

                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))

                        Text("Instant 1-Click Demo Accounts:", style = MaterialTheme.typography.labelSmall, color = GhanaNavyPrimary)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.signUpOrLoginUser(
                                        fullName = "Dr. Kwabena Mensah",
                                        email = "proprietor@sttalafor.edu.gh",
                                        phone = "0244987654",
                                        role = "PROPRIETOR",
                                        schoolNameInput = schoolName
                                    )
                                },
                                modifier = Modifier.weight(1f).testTag("quick_demo_proprietor")
                            ) {
                                Text("Proprietor", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    viewModel.signUpOrLoginUser(
                                        fullName = "Mr. Kojo Mensah",
                                        email = "mensah@sttalafor.edu.gh",
                                        phone = "0208112233",
                                        role = "TEACHER",
                                        schoolNameInput = schoolName
                                    )
                                },
                                modifier = Modifier.weight(1f).testTag("quick_demo_teacher")
                            ) {
                                Text("Teacher", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    viewModel.signUpOrLoginUser(
                                        fullName = "Madam Grace Mensah",
                                        email = "grace.mensah@gmail.com",
                                        phone = "0244123456",
                                        role = "GUARDIAN",
                                        schoolNameInput = schoolName
                                    )
                                },
                                modifier = Modifier.weight(1f).testTag("quick_demo_guardian")
                            ) {
                                Text("Guardian", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    // --- CREATE ACCOUNT FORM ---
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = fullNameInput,
                            onValueChange = { fullNameInput = it; authErrorMessage = null },
                            label = { Text("Full Name (e.g. Samuel Osei)") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("home_create_fullname_input")
                        )

                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it; authErrorMessage = null },
                            label = { Text("Email Address") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("home_create_email_input")
                        )

                        OutlinedTextField(
                            value = phoneInput,
                            onValueChange = { phoneInput = it },
                            label = { Text("Contact Phone") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text("Select Role to Register:", style = MaterialTheme.typography.labelSmall)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("PROPRIETOR", "TEACHER", "GUARDIAN").forEach { r ->
                                FilterChip(
                                    selected = selectedRole == r,
                                    onClick = { selectedRole = r },
                                    label = { Text(r, fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f).testTag("home_create_role_$r")
                                )
                            }
                        }

                        Button(
                            onClick = {
                                if (fullNameInput.isBlank()) {
                                    authErrorMessage = "Please enter your full name."
                                    return@Button
                                }
                                if (emailInput.isBlank() || !emailInput.contains("@")) {
                                    authErrorMessage = "Please enter a valid email address."
                                    return@Button
                                }
                                viewModel.signUpOrLoginUser(
                                    fullName = fullNameInput,
                                    email = emailInput,
                                    phone = phoneInput,
                                    role = selectedRole,
                                    schoolNameInput = schoolName
                                )
                                Toast.makeText(context, "Account Created! Welcome $fullNameInput", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GhanaNavyPrimary),
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("home_create_submit_button")
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Register New Account", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- DASHBOARD PORTAL NAVIGATION SHORTCUTS & ROLE-BASED LAYOUT ---
        val userRole = activeUserAccount?.role?.uppercase()

        // Role Layout Indicator & View Switcher Bar
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = GhanaNavyPrimary.copy(alpha = 0.06f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, GhanaNavyPrimary.copy(alpha = 0.15f)),
            modifier = Modifier.fillMaxWidth().testTag("home_role_dashboard_header")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = when (userRole) {
                            "PROPRIETOR" -> "Executive Proprietor Dashboard"
                            "TEACHER" -> "Teacher Daily Classroom Dashboard"
                            "GUARDIAN" -> "Guardian & Ward Family Dashboard"
                            else -> "School Portal Workspace Shortcuts"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = GhanaNavyPrimary
                    )
                    Text(
                        text = if (userRole != null) "Customized for $userRole account role" else "Sign in to customize your personal dashboard",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = GhanaGoldContainer
                ) {
                    Text(
                        text = userRole ?: "GUEST MODE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = GhanaNavyPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // --- DYNAMIC ROLE DASHBOARD LAYOUTS ---
        when (userRole) {
            "PROPRIETOR" -> {
                // ==========================================
                // 1. PROPRIETOR ROLE DASHBOARD LAYOUT
                // ==========================================
                val pendingPlans = allLessonPlans.filter { it.status == "PENDING_REVIEW" }

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth().testTag("role_dashboard_proprietor_layout")
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = GhanaNavyPrimary)
                                Text("Proprietor Executive Controls", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            Button(
                                onClick = { viewModel.setViewMode(ViewMode.PROPRIETOR) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GhanaNavyPrimary),
                                modifier = Modifier.testTag("launch_full_proprietor_portal")
                            ) {
                                Text("Full Portal", fontSize = 11.sp)
                                Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
                            }
                        }

                        // Executive KPIs
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = GhanaNavyPrimary.copy(alpha = 0.08f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Term Fees", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("GHC 124,500", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = GhanaNavyPrimary)
                                    Text("92% Paid", fontSize = 10.sp, color = GhanaEmeraldGreen)
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = GhanaGoldAccent.copy(alpha = 0.15f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Pending Plans", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${pendingPlans.size} Plans", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = Color(0xFFB45309))
                                    Text("Review Needed", fontSize = 10.sp, color = Color(0xFFB45309))
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = GhanaEmeraldGreen.copy(alpha = 0.12f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Active Staff", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("48 Teachers", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = GhanaEmeraldGreen)
                                    Text("100% Present", fontSize = 10.sp, color = GhanaEmeraldGreen)
                                }
                            }
                        }

                        // Quick Actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.setViewMode(ViewMode.PROPRIETOR) },
                                modifier = Modifier.weight(1f).testTag("action_review_lesson_plans")
                            ) {
                                Icon(Icons.Default.RateReview, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Lesson Plans", fontSize = 10.sp)
                            }

                            OutlinedButton(
                                onClick = { viewModel.setViewMode(ViewMode.PROPRIETOR) },
                                modifier = Modifier.weight(1f).testTag("action_broadcast_sms")
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("SMS Alert", fontSize = 10.sp)
                            }
                        }

                        // Widget: Recent Lesson Plans Pending Approval
                        if (pendingPlans.isNotEmpty()) {
                            Text("Pending Lesson Plans Needing Review:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            pendingPlans.take(2).forEach { plan ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(plan.teacherName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text("${plan.subject} • ${plan.className} • ${plan.topic}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }

                                        Button(
                                            onClick = {
                                                viewModel.reviewLessonPlan(
                                                    planId = plan.id,
                                                    newStatus = "APPROVED",
                                                    feedback = "Approved from Home Executive Dashboard",
                                                    topic = plan.topic,
                                                    teacherName = plan.teacherName
                                                )
                                                Toast.makeText(context, "Lesson Plan Approved!", Toast.LENGTH_SHORT).show()
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = GhanaNavyPrimary),
                                            modifier = Modifier.testTag("quick_approve_plan_${plan.id}")
                                        ) {
                                            Text("Approve", fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            "TEACHER" -> {
                // ==========================================
                // 2. TEACHER ROLE DASHBOARD LAYOUT
                // ==========================================
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth().testTag("role_dashboard_teacher_layout")
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.CoPresent, contentDescription = null, tint = GhanaEmeraldGreen)
                                Text("Teacher Classroom Station", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            Button(
                                onClick = { viewModel.setViewMode(ViewMode.TEACHER) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GhanaEmeraldGreen),
                                modifier = Modifier.testTag("launch_full_teacher_portal")
                            ) {
                                Text("Full Portal", fontSize = 11.sp)
                                Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
                            }
                        }

                        // Teacher KPIs
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = GhanaEmeraldGreen.copy(alpha = 0.12f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Class", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("JHS 2 A", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = GhanaEmeraldGreen)
                                    Text("42 Students", fontSize = 10.sp)
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = GhanaNavyPrimary.copy(alpha = 0.08f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Attendance", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("95% Recorded", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = GhanaNavyPrimary)
                                    Text("Today Completed", fontSize = 10.sp, color = GhanaEmeraldGreen)
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = GhanaGoldAccent.copy(alpha = 0.15f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Lesson Plans", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("Submitted", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = Color(0xFFB45309))
                                    Text("Approved by Head", fontSize = 10.sp, color = GhanaEmeraldGreen)
                                }
                            }
                        }

                        // Quick Classroom Actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.setViewMode(ViewMode.TEACHER) },
                                colors = ButtonDefaults.buttonColors(containerColor = GhanaEmeraldGreen),
                                modifier = Modifier.weight(1f).testTag("action_submit_lesson_plan")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("New Lesson Plan", fontSize = 10.sp)
                            }

                            OutlinedButton(
                                onClick = { viewModel.setViewMode(ViewMode.TEACHER) },
                                modifier = Modifier.weight(1f).testTag("action_mark_attendance")
                            ) {
                                Icon(Icons.Default.Checklist, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Attendance Register", fontSize = 10.sp)
                            }
                        }

                        // Today's Timetable Snippet
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Today's Schedule (JHS 2 Mathematics & Science):", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("• 08:00 AM - 09:30 AM: Mathematics - Algebra & Quadratic Equations", fontSize = 11.sp)
                                Text("• 10:30 AM - 12:00 PM: Integrated Science - Photosynthesis Lab", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            "GUARDIAN" -> {
                // ==========================================
                // 3. GUARDIAN ROLE DASHBOARD LAYOUT
                // ==========================================
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth().testTag("role_dashboard_guardian_layout")
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.FamilyRestroom, contentDescription = null, tint = Color(0xFFD97706))
                                Text("Guardian & Ward Summary", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            Button(
                                onClick = { viewModel.setViewMode(ViewMode.GUARDIAN) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                                modifier = Modifier.testTag("launch_full_guardian_portal")
                            ) {
                                Text("Full Portal", fontSize = 11.sp)
                                Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
                            }
                        }

                        // Guardian KPIs
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFFFF3CD),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Outstanding Fees", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("GHC 450.00", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = Color(0xFF842029))
                                    Text("Due Term 3", fontSize = 10.sp, color = Color(0xFF842029))
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = GhanaEmeraldGreen.copy(alpha = 0.12f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Attendance Rate", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("96.8%", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = GhanaEmeraldGreen)
                                    Text("Kwame Mensah", fontSize = 10.sp)
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = GhanaNavyPrimary.copy(alpha = 0.08f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Term Grade", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("Grade A (84%)", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = GhanaNavyPrimary)
                                    Text("Class Rank #3", fontSize = 10.sp)
                                }
                            }
                        }

                        // Mobile Money Quick Fee Payment Widget
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = GhanaNavyPrimary.copy(alpha = 0.05f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GhanaNavyPrimary.copy(alpha = 0.15f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Mobile Money Tuition Payment (MTN / Telecel)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = GhanaNavyPrimary, modifier = Modifier.size(16.dp))
                                }

                                Text("Ward: Kwame Mensah (JHS 2) • Balance: GHC 450.00", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                Button(
                                    onClick = {
                                        viewModel.makeMoMoFeePayment(
                                            momoNumber = "0244123456",
                                            network = "MTN MoMo",
                                            studentId = "STU-001",
                                            amount = 450.0,
                                            feeType = "Tuition Fee"
                                        )
                                        Toast.makeText(context, "MoMo Prompt Sent to 0244123456!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = GhanaEmeraldGreen),
                                    modifier = Modifier.fillMaxWidth().testTag("home_guardian_momo_payment_button")
                                ) {
                                    Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Pay GHC 450.00 via MTN MoMo", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            else -> {
                // ==========================================
                // 4. GUEST / DEFAULT DASHBOARD PORTAL SHORTCUTS
                // ==========================================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PortalShortcutCard(
                        title = "Proprietor Portal",
                        subtitle = "Admin & Finance Control",
                        description = "Manage staff, tuition fee ledgers, SMS broadcasts, and approve teacher daily lesson plans.",
                        icon = Icons.Default.AdminPanelSettings,
                        accentColor = GhanaNavyPrimary,
                        onClick = { viewModel.setViewMode(ViewMode.PROPRIETOR) },
                        testTag = "shortcut_proprietor_card",
                        modifier = Modifier.weight(1f)
                    )

                    PortalShortcutCard(
                        title = "Teacher Portal",
                        subtitle = "Classroom & Attendance",
                        description = "Upload daily lesson plans, submit terminal grades, and record student attendance.",
                        icon = Icons.Default.CoPresent,
                        accentColor = GhanaEmeraldGreen,
                        onClick = { viewModel.setViewMode(ViewMode.TEACHER) },
                        testTag = "shortcut_teacher_card",
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PortalShortcutCard(
                        title = "Guardian Portal",
                        subtitle = "Parent & Ward Reports",
                        description = "View student academic report cards, fee breakdown, MoMo payments, and attendance.",
                        icon = Icons.Default.FamilyRestroom,
                        accentColor = Color(0xFFD97706),
                        onClick = { viewModel.setViewMode(ViewMode.GUARDIAN) },
                        testTag = "shortcut_guardian_card",
                        modifier = Modifier.weight(1f)
                    )

                    PortalShortcutCard(
                        title = "School Calendar",
                        subtitle = "Events & Schedule",
                        description = "Track school term calendar, PTA meetings, holidays, and exam timetables.",
                        icon = Icons.Default.CalendarMonth,
                        accentColor = Color(0xFF2563EB),
                        onClick = { viewModel.setViewMode(ViewMode.CALENDAR) },
                        testTag = "shortcut_calendar_card",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardStatPill(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.12f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = GhanaGoldAccent, modifier = Modifier.size(14.dp))
                Text(label, fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
private fun PortalShortcutCard(
    title: String,
    subtitle: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .testTag(testTag)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(22.dp))
                }

                Icon(Icons.Default.ArrowForward, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
            }

            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(subtitle, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = accentColor)
            }

            Text(description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3, overflow = TextOverflow.Ellipsis)
        }
    }
}
