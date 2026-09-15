package com.example.ui.components

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.UserAccount
import com.example.ui.theme.GhanaGoldAccent
import com.example.ui.theme.GhanaNavyPrimary

@Composable
fun AuthDialog(
    currentActiveUser: UserAccount?,
    currentSchoolName: String,
    onDismissRequest: () -> Unit,
    onSignUpOrLogin: (fullName: String, email: String, phone: String, role: String, schoolName: String) -> Unit,
    onSwitchExistingUser: (UserAccount) -> Unit
) {
    var selectedRole by remember { mutableStateOf(currentActiveUser?.role ?: "PROPRIETOR") }
    var fullName by remember { mutableStateOf(currentActiveUser?.fullName ?: "") }
    var email by remember { mutableStateOf(currentActiveUser?.email ?: "") }
    var phone by remember { mutableStateOf(currentActiveUser?.phone ?: "0244123456") }
    var schoolNameInput by remember { mutableStateOf(currentSchoolName) }
    var isCreatingNew by remember { mutableStateOf(false) }
    var isForgotPasswordMode by remember { mutableStateOf(false) }

    // Forgot Password Flow States
    var forgotEmail by remember { mutableStateOf(currentActiveUser?.email ?: "") }
    var verificationStep by remember { mutableStateOf(1) } // 1: Enter Email, 2: Code & New Password, 3: Success
    var generatedCode by remember { mutableStateOf("849201") }
    var enteredCode by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var resetError by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("auth_dialog_surface"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(GhanaNavyPrimary)
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .background(GhanaGoldAccent, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Auth Lock",
                                        tint = GhanaNavyPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Account Portal",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                    Text(
                                        text = "Role Access & Authentication",
                                        color = GhanaGoldAccent,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            IconButton(
                                onClick = onDismissRequest,
                                modifier = Modifier.testTag("auth_dialog_close_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    if (isForgotPasswordMode) {
                        // FORGOT PASSWORD FLOW
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Forgot Password Recovery",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = GhanaNavyPrimary
                                )
                                TextButton(
                                    onClick = {
                                        isForgotPasswordMode = false
                                        verificationStep = 1
                                        resetError = null
                                    },
                                    modifier = Modifier.testTag("back_to_login_button")
                                ) {
                                    Text("Back to Sign In")
                                }
                            }

                            // Stepper indicator
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.weight(1f).height(4.dp),
                                    color = if (verificationStep >= 1) GhanaNavyPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(2.dp)
                                ) {}
                                Surface(
                                    modifier = Modifier.weight(1f).height(4.dp),
                                    color = if (verificationStep >= 2) GhanaNavyPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(2.dp)
                                ) {}
                                Surface(
                                    modifier = Modifier.weight(1f).height(4.dp),
                                    color = if (verificationStep >= 3) GhanaGoldAccent else MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(2.dp)
                                ) {}
                            }

                            if (verificationStep == 1) {
                                Surface(
                                    color = GhanaNavyPrimary.copy(alpha = 0.05f),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MarkEmailRead,
                                            contentDescription = null,
                                            tint = GhanaNavyPrimary,
                                            modifier = Modifier.size(28.dp)
                                        )
                                        Text(
                                            text = "Enter your registered email address to receive a 6-digit security verification code.",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                OutlinedTextField(
                                    value = forgotEmail,
                                    onValueChange = {
                                        forgotEmail = it
                                        resetError = null
                                    },
                                    label = { Text("Account Email Address") },
                                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("forgot_email_input")
                                )

                                if (resetError != null) {
                                    Text(
                                        text = resetError!!,
                                        color = MaterialTheme.colorScheme.error,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Button(
                                    onClick = {
                                        if (forgotEmail.isBlank() || !forgotEmail.contains("@")) {
                                            resetError = "Please enter a valid email address."
                                        } else {
                                            resetError = null
                                            // Simulate code dispatch
                                            generatedCode = (100000..999999).random().toString()
                                            verificationStep = 2
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("trigger_email_verification_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = GhanaNavyPrimary)
                                ) {
                                    Icon(imageVector = Icons.Default.Send, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Send Verification Code")
                                }
                            } else if (verificationStep == 2) {
                                Surface(
                                    color = GhanaGoldAccent.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = GhanaNavyPrimary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = "Verification Email Sent!",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = GhanaNavyPrimary
                                            )
                                        }
                                        Text(
                                            text = "A 6-digit code has been dispatched to $forgotEmail.",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Simulated Security Code: $generatedCode",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = GhanaNavyPrimary
                                        )
                                    }
                                }

                                OutlinedTextField(
                                    value = enteredCode,
                                    onValueChange = {
                                        enteredCode = it
                                        resetError = null
                                    },
                                    label = { Text("6-Digit Verification Code") },
                                    leadingIcon = { Icon(Icons.Default.VpnKey, contentDescription = null) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("verification_code_input")
                                )

                                OutlinedTextField(
                                    value = newPassword,
                                    onValueChange = {
                                        newPassword = it
                                        resetError = null
                                    },
                                    label = { Text("New Password") },
                                    leadingIcon = { Icon(Icons.Default.LockReset, contentDescription = null) },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("new_password_input")
                                )

                                OutlinedTextField(
                                    value = confirmPassword,
                                    onValueChange = {
                                        confirmPassword = it
                                        resetError = null
                                    },
                                    label = { Text("Confirm New Password") },
                                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("confirm_password_input")
                                )

                                if (resetError != null) {
                                    Text(
                                        text = resetError!!,
                                        color = MaterialTheme.colorScheme.error,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Button(
                                    onClick = {
                                        if (enteredCode.trim() != generatedCode) {
                                            resetError = "Invalid verification code. Use code $generatedCode"
                                        } else if (newPassword.isBlank() || newPassword.length < 4) {
                                            resetError = "Password must be at least 4 characters long."
                                        } else if (newPassword != confirmPassword) {
                                            resetError = "Passwords do not match."
                                        } else {
                                            resetError = null
                                            verificationStep = 3
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("verify_and_reset_password_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = GhanaNavyPrimary)
                                ) {
                                    Icon(imageVector = Icons.Default.VerifiedUser, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Verify Code & Reset Password")
                                }
                            } else if (verificationStep == 3) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .background(Color(0xFF2E7D32), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }

                                    Text(
                                        text = "Password Reset Successful!",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = Color(0xFF2E7D32)
                                    )

                                    Text(
                                        text = "Your security credentials for $forgotEmail have been updated. You can now sign in with your new password.",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Button(
                                        onClick = {
                                            isForgotPasswordMode = false
                                            verificationStep = 1
                                            onSignUpOrLogin("Recovered User", forgotEmail, "0244123456", selectedRole, currentSchoolName)
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp)
                                            .testTag("login_with_new_password_button"),
                                        colors = ButtonDefaults.buttonColors(containerColor = GhanaNavyPrimary)
                                    ) {
                                        Text("Continue to Workspace")
                                    }
                                }
                            }
                        }
                    } else // Quick Demo Switch Cards
                    if (!isCreatingNew) {
                        Text(
                            text = "Quick Role Switch (1-Tap Demo Accounts)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            DemoRoleCard(
                                title = "Dr. Kwabena Mensah",
                                roleName = "PROPRIETOR",
                                subtitle = "Full Control Panel & Financial Approvals",
                                icon = Icons.Default.AdminPanelSettings,
                                color = GhanaNavyPrimary,
                                isSelected = currentActiveUser?.role == "PROPRIETOR",
                                onClick = {
                                    onSwitchExistingUser(
                                        UserAccount(
                                            fullName = "Dr. Kwabena Mensah",
                                            email = "proprietor@sttalafor.edu.gh",
                                            phone = "0244987654",
                                            role = "PROPRIETOR",
                                            schoolName = currentSchoolName,
                                            isLoggedIn = true
                                        )
                                    )
                                }
                            )

                            DemoRoleCard(
                                title = "Mr. Kojo Mensah",
                                roleName = "TEACHER",
                                subtitle = "Attendance, Geofence Clock-In & Grades",
                                icon = Icons.Default.School,
                                color = Color(0xFF1E88E5),
                                isSelected = currentActiveUser?.role == "TEACHER",
                                onClick = {
                                    onSwitchExistingUser(
                                        UserAccount(
                                            fullName = "Mr. Kojo Mensah",
                                            email = "kojo.mensah@sttalafor.edu.gh",
                                            phone = "0208112233",
                                            role = "TEACHER",
                                            schoolName = currentSchoolName,
                                            isLoggedIn = true
                                        )
                                    )
                                }
                            )

                            DemoRoleCard(
                                title = "Mrs. Grace Mensah",
                                roleName = "GUARDIAN",
                                subtitle = "MoMo Fee Statements & Student Ledgers",
                                icon = Icons.Default.FamilyRestroom,
                                color = Color(0xFF2E7D32),
                                isSelected = currentActiveUser?.role == "GUARDIAN",
                                onClick = {
                                    onSwitchExistingUser(
                                        UserAccount(
                                            fullName = "Mrs. Grace Mensah",
                                            email = "grace.mensah@gmail.com",
                                            phone = "0244123456",
                                            role = "GUARDIAN",
                                            schoolName = currentSchoolName,
                                            isLoggedIn = true
                                        )
                                    )
                                }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = {
                                    isForgotPasswordMode = true
                                    verificationStep = 1
                                    resetError = null
                                },
                                modifier = Modifier.testTag("forgot_password_trigger_button")
                            ) {
                                Icon(imageVector = Icons.Default.LockReset, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Forgot Password?", fontSize = 12.sp, color = GhanaNavyPrimary, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { isCreatingNew = true },
                                modifier = Modifier.testTag("create_new_account_toggle_button")
                            ) {
                                Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("New Account")
                            }
                        }
                    } else {
                        // Registration Form
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Create Custom Account",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            TextButton(
                                onClick = { isCreatingNew = false },
                                modifier = Modifier.testTag("switch_to_quick_switch_button")
                            ) {
                                Text("Back to Quick Switch")
                            }
                        }

                        // Select Role Tabs
                        Text(
                            text = "Select Account Role:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            RoleTabChip(
                                label = "Proprietor",
                                roleKey = "PROPRIETOR",
                                selectedRole = selectedRole,
                                onSelect = { selectedRole = "PROPRIETOR" },
                                modifier = Modifier.weight(1f)
                            )
                            RoleTabChip(
                                label = "Teacher",
                                roleKey = "TEACHER",
                                selectedRole = selectedRole,
                                onSelect = { selectedRole = "TEACHER" },
                                modifier = Modifier.weight(1f)
                            )
                            RoleTabChip(
                                label = "Guardian",
                                roleKey = "GUARDIAN",
                                selectedRole = selectedRole,
                                onSelect = { selectedRole = "GUARDIAN" },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Form Fields
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("Full Name") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_full_name_input")
                        )

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Address") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_email_input")
                        )

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Phone Number (Ghana MoMo / Contact)") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_phone_input")
                        )

                        // Special Proprietor Field: School Name
                        AnimatedVisibility(visible = selectedRole == "PROPRIETOR") {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedTextField(
                                    value = schoolNameInput,
                                    onValueChange = { schoolNameInput = it },
                                    label = { Text("School / Institution Name") },
                                    leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = null) },
                                    placeholder = { Text("e.g. St. Talafor Primary & JHS") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("auth_school_name_input"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = GhanaGoldAccent,
                                        focusedLabelColor = GhanaNavyPrimary
                                    )
                                )
                                Text(
                                    text = "As Proprietor, you can customize the institution name across the portal.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = {
                                val name = if (fullName.isBlank()) "New $selectedRole" else fullName
                                val mail = if (email.isBlank()) "${selectedRole.lowercase()}@sttalafor.edu.gh" else email
                                onSignUpOrLogin(name, mail, phone, selectedRole, schoolNameInput)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("submit_signup_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = GhanaNavyPrimary)
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Register & Access $selectedRole Dashboard")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DemoRoleCard(
    title: String,
    roleName: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) GhanaGoldAccent else MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .testTag("demo_account_card_${roleName.lowercase()}"),
        color = if (isSelected) color.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Surface(
                        color = color.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = roleName,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = color,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Active",
                    tint = GhanaGoldAccent,
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Text(
                    text = "Switch",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = GhanaNavyPrimary
                )
            }
        }
    }
}

@Composable
private fun RoleTabChip(
    label: String,
    roleKey: String,
    selectedRole: String,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSelected = selectedRole == roleKey
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onSelect() }
            .testTag("role_tab_chip_${roleKey.lowercase()}"),
        color = if (isSelected) GhanaNavyPrimary else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Box(
            modifier = Modifier.padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}
