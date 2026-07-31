package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AlumniProfile
import com.example.data.model.AlumniChatMessage
import com.example.data.model.SchoolPerformanceMetric
import com.example.data.model.AlumniAspirant
import com.example.ui.theme.GhanaEmeraldGreen
import com.example.ui.theme.GhanaGoldAccent
import com.example.ui.theme.GhanaGoldContainer
import com.example.ui.theme.GhanaNavyPrimary
import com.example.ui.viewmodel.SchoolViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlumniScreen(
    viewModel: SchoolViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allAlumniProfiles by viewModel.allAlumniProfiles.collectAsState()
    val activatedAlumniProfiles by viewModel.activatedAlumniProfiles.collectAsState()
    val activeAlumniAdmin by viewModel.activeAlumniAdmin.collectAsState()
    val allAlumniChatMessages by viewModel.allAlumniChatMessages.collectAsState()
    val allPerformanceMetrics by viewModel.allPerformanceMetrics.collectAsState()
    val allAspirants by viewModel.allAspirants.collectAsState()

    // Screen State
    var selectedTab by remember { mutableStateOf(0) } // 0: Verify, 1: Community, 2: Performance, 3: Elections & Voting, 4: Proprietor Consult
    var activeUserSession by remember { mutableStateOf<AlumniProfile?>(activatedAlumniProfiles.firstOrNull { it.isAlumniAdmin } ?: activatedAlumniProfiles.firstOrNull()) }

    // Verification Engine Form State
    var verifyNameInput by remember { mutableStateOf("Daniel Akuffo") }
    var verifyStudentIdInput by remember { mutableStateOf("AKM/2016/001") }
    var verifyDobInput by remember { mutableStateOf("2002-04-12") }
    var verifyGuardianNameInput by remember { mutableStateOf("Seth Akuffo") }
    var verifyPhoneInput by remember { mutableStateOf("0244987654") }
    var verifyEmailInput by remember { mutableStateOf("daniel.akuffo@alumni.edu.gh") }
    var verificationErrorMessage by remember { mutableStateOf<String?>(null) }

    // Chat State
    var chatInputText by remember { mutableStateOf("") }
    var showMediaUploadDialog by remember { mutableStateOf(false) }
    var selectedMediaType by remember { mutableStateOf("IMAGE") }
    var mediaUrlInput by remember { mutableStateOf("https://images.unsplash.com/photo-1523050854058-8df90110c9f1?w=600") }
    var fileNameInput by remember { mutableStateOf("Akoma_Reunion_Photo.jpg") }

    // Batch Import State
    var showBatchImportDialog by remember { mutableStateOf(false) }
    var batchYearInput by remember { mutableStateOf("2022") }
    var batchCountInput by remember { mutableStateOf("5") }

    // Call / ACL Security Dialog State
    var showSecurityBoundaryDialog by remember { mutableStateOf(false) }
    var securityDialogMessage by remember { mutableStateOf("") }
    var showCallActiveDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Alumni Network Platform", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                        Text(
                            text = if (activeUserSession != null) "Logged in as: ${activeUserSession?.fullName} (${if (activeUserSession?.isAlumniAdmin == true) "★ Yearly Admin" else "Verified Alumni"})" else "Account Locked / Unverified Session",
                            fontSize = 11.sp,
                            color = GhanaGoldAccent
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GhanaNavyPrimary),
                actions = {
                    IconButton(onClick = {
                        Toast.makeText(context, "Refreshing Alumni Portal Data...", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Navigation Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = GhanaNavyPrimary.copy(alpha = 0.05f),
                contentColor = GhanaNavyPrimary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("1. Verify & Unlock", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.LockReset, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("2. Community Hub", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Forum, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("3. Performance", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("4. Elections & Voting", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.HowToVote, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    text = { Text("5. Proprietor Consult", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (selectedTab) {
                0 -> VerificationAndActivationTab(
                    allProfiles = allAlumniProfiles,
                    activeUserSession = activeUserSession,
                    nameInput = verifyNameInput,
                    onNameChange = { verifyNameInput = it },
                    studentIdInput = verifyStudentIdInput,
                    onStudentIdChange = { verifyStudentIdInput = it },
                    dobInput = verifyDobInput,
                    onDobChange = { verifyDobInput = it },
                    guardianNameInput = verifyGuardianNameInput,
                    onGuardianNameChange = { verifyGuardianNameInput = it },
                    phoneInput = verifyPhoneInput,
                    onPhoneChange = { verifyPhoneInput = it },
                    emailInput = verifyEmailInput,
                    onEmailChange = { verifyEmailInput = it },
                    errorMessage = verificationErrorMessage,
                    onVerifyClick = {
                        verificationErrorMessage = null
                        viewModel.verifyAndActivateAlumniAccount(
                            name = verifyNameInput,
                            studentId = verifyStudentIdInput,
                            dob = verifyDobInput,
                            guardianName = verifyGuardianNameInput,
                            phone = verifyPhoneInput,
                            email = verifyEmailInput,
                            onSuccess = { profile ->
                                activeUserSession = profile
                            },
                            onError = { err ->
                                verificationErrorMessage = err
                            }
                        )
                    },
                    onSelectActiveSession = { profile ->
                        activeUserSession = profile
                        Toast.makeText(context, "Switched active user session to ${profile.fullName}", Toast.LENGTH_SHORT).show()
                    }
                )

                1 -> CommunityChatAndMediaTab(
                    chatMessages = allAlumniChatMessages,
                    activeUserSession = activeUserSession,
                    chatText = chatInputText,
                    onChatTextChange = { chatInputText = it },
                    onSendMessage = {
                        if (chatInputText.isNotBlank()) {
                            val senderName = activeUserSession?.fullName ?: "Anonymous Alumni"
                            val senderRole = if (activeUserSession?.isAlumniAdmin == true) "ALUMNI_ADMIN" else "ALUMNI_MEMBER"
                            viewModel.sendAlumniChatMessage(
                                senderName = senderName,
                                senderRole = senderRole,
                                text = chatInputText
                            )
                            chatInputText = ""
                        }
                    },
                    onOpenMediaUpload = { showMediaUploadDialog = true }
                )

                2 -> SchoolPerformanceTab(
                    performanceMetrics = allPerformanceMetrics,
                    allAlumniProfiles = allAlumniProfiles
                )

                3 -> AspirantIntentAndVotingTab(
                    allAspirants = allAspirants,
                    activeUserSession = activeUserSession,
                    onSubmitIntent = { manifesto, termYear ->
                        val user = activeUserSession
                        if (user == null) {
                            Toast.makeText(context, "Please verify your account in Tab 1 before submitting candidacy intent.", Toast.LENGTH_LONG).show()
                        } else {
                            viewModel.submitAspirantIntent(
                                user = user,
                                manifesto = manifesto,
                                termYear = termYear,
                                onSuccess = {},
                                onError = { err ->
                                    securityDialogMessage = err
                                    showSecurityBoundaryDialog = true
                                }
                            )
                        }
                    },
                    onCastVote = { candidateDocId, termYear ->
                        val user = activeUserSession
                        if (user == null) {
                            Toast.makeText(context, "Please verify your account in Tab 1 before casting a vote.", Toast.LENGTH_LONG).show()
                        } else {
                            viewModel.castVoteForAspirant(
                                voter = user,
                                candidateDocId = candidateDocId,
                                termYear = termYear,
                                onSuccess = {},
                                onError = { err ->
                                    securityDialogMessage = err
                                    showSecurityBoundaryDialog = true
                                }
                            )
                        }
                    }
                )

                4 -> ProprietorConsultationAndAdminTab(
                    activeUserSession = activeUserSession,
                    activeAdmin = activeAlumniAdmin,
                    activatedAlumni = activatedAlumniProfiles,
                    onInitiateCall = { callType ->
                        val currentSession = activeUserSession
                        if (currentSession == null) {
                            securityDialogMessage = "Please verify and unlock your account in Tab 1 before initiating consultation."
                            showSecurityBoundaryDialog = true
                        } else if (!currentSession.isAlumniAdmin) {
                            // ACL Security Check Triggered
                            securityDialogMessage = "🚫 ACCESS DENIED (ACL Boundary Restriction):\n\nDirect calling or private consultation with the Proprietor is restricted exclusively to the active Alumni Admin.\n\nAs a regular verified alumni member, you can communicate in the group hub, share media, or contact the Alumni Admin."
                            showSecurityBoundaryDialog = true
                        } else {
                            // Admin ACL Passed
                            viewModel.initiateProprietorConsultationCall(
                                initiator = currentSession,
                                callType = callType,
                                onSuccess = {
                                    showCallActiveDialog = true
                                },
                                onError = { err ->
                                    securityDialogMessage = err
                                    showSecurityBoundaryDialog = true
                                }
                            )
                        }
                    },
                    onRotateAdmin = { newAdminId ->
                        viewModel.rotateAnnualAlumniAdmin(newAdminId)
                    },
                    onOpenBatchImport = { showBatchImportDialog = true }
                )
            }
        }
    }

    // --- SECURITY BOUNDARY DIALOG ---
    if (showSecurityBoundaryDialog) {
        AlertDialog(
            onDismissRequest = { showSecurityBoundaryDialog = false },
            icon = { Icon(Icons.Default.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(36.dp)) },
            title = { Text("Access Control & Security Boundary", fontWeight = FontWeight.Bold) },
            text = { Text(securityDialogMessage, fontSize = 13.sp) },
            confirmButton = {
                Button(
                    onClick = { showSecurityBoundaryDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = GhanaNavyPrimary)
                ) {
                    Text("Understand Security ACL")
                }
            }
        )
    }

    // --- ACTIVE CALL SIMULATION DIALOG ---
    if (showCallActiveDialog) {
        AlertDialog(
            onDismissRequest = { showCallActiveDialog = false },
            icon = { Icon(Icons.Default.PhoneInTalk, contentDescription = null, tint = GhanaEmeraldGreen, modifier = Modifier.size(40.dp)) },
            title = { Text("Consultation Call Active", fontWeight = FontWeight.Bold) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("Connected to School Proprietor's Direct Line", fontWeight = FontWeight.Bold, color = GhanaEmeraldGreen)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Initiator: ${activeUserSession?.fullName} (Alumni Network Admin)", fontSize = 12.sp)
                    Text("Encrypted Peer Channel: Active (00:42)", fontSize = 11.sp, color = Color.Gray)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showCallActiveDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("End Call Session")
                }
            }
        )
    }

    // --- MEDIA UPLOAD DIALOG ---
    if (showMediaUploadDialog) {
        AlertDialog(
            onDismissRequest = { showMediaUploadDialog = false },
            title = { Text("Share Media or Records in Group", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Select media type and upload to the Alumni Group Hub.", fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = selectedMediaType == "IMAGE",
                            onClick = { selectedMediaType = "IMAGE" },
                            label = { Text("Photo") }
                        )
                        FilterChip(
                            selected = selectedMediaType == "VIDEO",
                            onClick = { selectedMediaType = "VIDEO" },
                            label = { Text("Video") }
                        )
                        FilterChip(
                            selected = selectedMediaType == "DOCUMENT",
                            onClick = { selectedMediaType = "DOCUMENT" },
                            label = { Text("Record/PDF") }
                        )
                    }

                    OutlinedTextField(
                        value = fileNameInput,
                        onValueChange = { fileNameInput = it },
                        label = { Text("File Title / Document Record Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = mediaUrlInput,
                        onValueChange = { mediaUrlInput = it },
                        label = { Text("Media Storage URL / Path") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val senderName = activeUserSession?.fullName ?: "Anonymous Alumni"
                        val senderRole = if (activeUserSession?.isAlumniAdmin == true) "ALUMNI_ADMIN" else "ALUMNI_MEMBER"
                        viewModel.sendAlumniChatMessage(
                            senderName = senderName,
                            senderRole = senderRole,
                            text = "Shared $selectedMediaType: $fileNameInput",
                            mediaType = selectedMediaType,
                            mediaUrl = mediaUrlInput,
                            fileName = fileNameInput
                        )
                        showMediaUploadDialog = false
                        Toast.makeText(context, "$selectedMediaType shared in alumni feed!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GhanaNavyPrimary)
                ) {
                    Text("Share Media")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMediaUploadDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // --- BATCH IMPORT DIALOG ---
    if (showBatchImportDialog) {
        AlertDialog(
            onDismissRequest = { showBatchImportDialog = false },
            title = { Text("Manual Batch Import Historical Alumni", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Import pre-created locked accounts for earlier graduating batches.", fontSize = 12.sp)
                    OutlinedTextField(
                        value = batchYearInput,
                        onValueChange = { batchYearInput = it },
                        label = { Text("Graduation Year (e.g. 2022)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = batchCountInput,
                        onValueChange = { batchCountInput = it },
                        label = { Text("Number of Student Records to Generate") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val year = batchYearInput.toIntOrNull() ?: 2022
                        val count = batchCountInput.toIntOrNull() ?: 3
                        val generatedList = mutableListOf<AlumniProfile>()
                        for (i in 1..count) {
                            generatedList.add(
                                AlumniProfile(
                                    fullName = "Historical Graduate #$i (Batch $year)",
                                    indexNumber = "AKM/$year/${String.format("%03d", i)}",
                                    dateOfBirth = "2000-05-10",
                                    guardianName = "Guardian $i",
                                    admissionDate = "${year - 9}-09-01",
                                    estimatedGraduationYear = year,
                                    actualGraduationYear = year,
                                    isAccountLocked = true,
                                    isActivated = false,
                                    batchTag = "Class of $year"
                                )
                            )
                        }
                        viewModel.batchImportAlumni(generatedList)
                        showBatchImportDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GhanaNavyPrimary)
                ) {
                    Text("Execute Batch Import")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBatchImportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// ==========================================
// TAB 1: VERIFICATION & ACCOUNT UNLOCK ENGINE
// ==========================================
@Composable
fun VerificationAndActivationTab(
    allProfiles: List<AlumniProfile>,
    activeUserSession: AlumniProfile?,
    nameInput: String,
    onNameChange: (String) -> Unit,
    studentIdInput: String,
    onStudentIdChange: (String) -> Unit,
    dobInput: String,
    onDobChange: (String) -> Unit,
    guardianNameInput: String,
    onGuardianNameChange: (String) -> Unit,
    phoneInput: String,
    onPhoneChange: (String) -> Unit,
    emailInput: String,
    onEmailChange: (String) -> Unit,
    errorMessage: String?,
    onVerifyClick: () -> Unit,
    onSelectActiveSession: (AlumniProfile) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = GhanaGoldContainer.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Key, contentDescription = null, tint = GhanaNavyPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Deferred Account Activation Engine", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = GhanaNavyPrimary)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Student accounts are pre-created in the database and REMAIN LOCKED until their completion date. Submit official credentials to verify identity and unlock your profile.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Active Session Selector (if activated accounts exist)
        val activatedList = allProfiles.filter { it.isActivated }
        if (activatedList.isNotEmpty()) {
            OutlinedCard(shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Active Verified Sessions (Switch User Context):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        activatedList.forEach { profile ->
                            val isSelected = activeUserSession?.id == profile.id
                            FilterChip(
                                selected = isSelected,
                                onClick = { onSelectActiveSession(profile) },
                                label = { Text("${profile.fullName} ${if (profile.isAlumniAdmin) "(★ Admin)" else ""}") },
                                leadingIcon = {
                                    if (profile.isAlumniAdmin) {
                                        Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp), tint = GhanaGoldAccent)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // Verification Form Card
        ElevatedCard(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Account Verification Form (4 Strict Record Matches)", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                OutlinedTextField(
                    value = nameInput,
                    onValueChange = onNameChange,
                    label = { Text("1. Full Student Name") },
                    modifier = Modifier.fillMaxWidth().testTag("verify_name_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = studentIdInput,
                    onValueChange = onStudentIdChange,
                    label = { Text("2. Student ID / Index Number") },
                    modifier = Modifier.fillMaxWidth().testTag("verify_id_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = dobInput,
                    onValueChange = onDobChange,
                    label = { Text("3. Date of Birth (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth().testTag("verify_dob_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = guardianNameInput,
                    onValueChange = onGuardianNameChange,
                    label = { Text("4. Official Guardian Name") },
                    modifier = Modifier.fillMaxWidth().testTag("verify_guardian_input"),
                    singleLine = true
                )

                Divider()

                OutlinedTextField(
                    value = phoneInput,
                    onValueChange = onPhoneChange,
                    label = { Text("Phone Number for Notifications") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = emailInput,
                    onValueChange = onEmailChange,
                    label = { Text("Email Address") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Button(
                    onClick = onVerifyClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("submit_verify_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = GhanaNavyPrimary)
                ) {
                    Icon(Icons.Default.LockOpen, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Verify & Unlock Account")
                }
            }
        }

        // Database Pre-Created Locked Accounts Status Overview
        Text("Pre-Created Historical Records Database (${allProfiles.size} total):", fontWeight = FontWeight.Bold, fontSize = 14.sp)

        allProfiles.forEach { profile ->
            OutlinedCard(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(profile.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Index: ${profile.indexNumber} | DOB: ${profile.dateOfBirth}", fontSize = 12.sp, color = Color.Gray)
                        Text("Guardian: ${profile.guardianName} | Admission: ${profile.admissionDate}", fontSize = 11.sp, color = Color.Gray)
                        Text("Batch: ${profile.batchTag} | Graduation: ${profile.actualGraduationYear}", fontSize = 11.sp, color = GhanaNavyPrimary)
                    }

                    Surface(
                        color = if (profile.isActivated) GhanaEmeraldGreen.copy(alpha = 0.2f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = if (profile.isActivated) "UNLOCKED" else "LOCKED",
                            color = if (profile.isActivated) GhanaEmeraldGreen else MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// TAB 2: COMMUNITY CHAT, MEDIA & PRIVACY HUB
// ==========================================
@Composable
fun CommunityChatAndMediaTab(
    chatMessages: List<AlumniChatMessage>,
    activeUserSession: AlumniProfile?,
    chatText: String,
    onChatTextChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onOpenMediaUpload: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Privacy Boundary Banner
        Surface(
            color = GhanaNavyPrimary,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Security, contentDescription = null, tint = GhanaGoldAccent)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Strict Privacy Boundary Enforced", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                    Text("Proprietors cannot view internal alumni group discussions, chats, or shared files unless explicitly invited.", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Chat Message History List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(chatMessages) { msg ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (msg.senderRole == "ALUMNI_ADMIN") GhanaGoldContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(msg.senderName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = GhanaNavyPrimary)
                            Text(msg.timestampString, fontSize = 10.sp, color = Color.Gray)
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(msg.messageText, fontSize = 13.sp)

                        if (msg.mediaType != "TEXT" && msg.fileName.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                color = GhanaNavyPrimary.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = when (msg.mediaType) {
                                            "IMAGE" -> Icons.Default.Image
                                            "VIDEO" -> Icons.Default.Videocam
                                            else -> Icons.Default.Description
                                        },
                                        contentDescription = null,
                                        tint = GhanaNavyPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("${msg.mediaType}: ${msg.fileName}", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = GhanaNavyPrimary)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Chat Input Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = onOpenMediaUpload,
                colors = IconButtonDefaults.iconButtonColors(containerColor = GhanaNavyPrimary.copy(alpha = 0.1f))
            ) {
                Icon(Icons.Default.AttachFile, contentDescription = "Attach Media", tint = GhanaNavyPrimary)
            }

            OutlinedTextField(
                value = chatText,
                onValueChange = onChatTextChange,
                placeholder = { Text("Message alumni network...") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_field"),
                singleLine = true
            )

            IconButton(
                onClick = onSendMessage,
                colors = IconButtonDefaults.iconButtonColors(containerColor = GhanaNavyPrimary)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
            }
        }
    }
}

// ==========================================
// TAB 3: UNIVERSAL SCHOOL PERFORMANCE VIEW
// ==========================================
@Composable
fun SchoolPerformanceTab(
    performanceMetrics: List<SchoolPerformanceMetric>,
    allAlumniProfiles: List<AlumniProfile>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = GhanaEmeraldGreen.copy(alpha = 0.15f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Public, contentDescription = null, tint = GhanaEmeraldGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Universal Read-Only Access", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = GhanaEmeraldGreen)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "All verified alumni users have open read-only access to view current school academic performance metrics, BECE pass rates, and campus milestones.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Text("Academic Performance & Growth Metrics", fontWeight = FontWeight.Bold, fontSize = 16.sp)

        performanceMetrics.forEach { metric ->
            ElevatedCard(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(metric.yearLabel, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = GhanaNavyPrimary)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricBadge(label = "BECE Pass Rate", value = "${metric.becePassRatePercentage}%", color = GhanaEmeraldGreen)
                        MetricBadge(label = "Overall Pass Rate", value = "${metric.overallPassRatePercentage}%", color = GhanaNavyPrimary)
                        MetricBadge(label = "Graduates", value = "${metric.totalGraduates}", color = GhanaGoldAccent)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Top Academic Subject: ${metric.topSubject}", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                    Text("Academic Highlight: ${metric.milestoneDescription}", fontSize = 12.sp, color = Color.Gray)
                    Text("Campus Infrastructure: ${metric.infrastructureProjects}", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }

        // Graduation Span Calculation Matrix
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Ghana Basic Education Span Matrix (~9-10 Years)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text("• Kindergarten (2 yrs) + Primary (6 yrs) + JHS (3 yrs) = 9-10 Year Span", fontSize = 12.sp)
                Text("• The system dynamically calculates estimated completion year based on admission date.", fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun MetricBadge(label: String, value: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = color)
            Text(label, fontSize = 10.sp, color = Color.Gray)
        }
    }
}

// ==========================================
// TAB 4: RESTRICTED PROPRIETOR CONSULTATION & ADMIN
// ==========================================
@Composable
fun ProprietorConsultationAndAdminTab(
    activeUserSession: AlumniProfile?,
    activeAdmin: AlumniProfile?,
    activatedAlumni: List<AlumniProfile>,
    onInitiateCall: (String) -> Unit,
    onRotateAdmin: (Long) -> Unit,
    onOpenBatchImport: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ACL Policy Banner
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Admin-Exclusive Proprietor Consultation ACL", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.error)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Only the designated active Alumni Admin is authorized to initiate direct calls or private consultations with the Proprietor on behalf of the alumni network.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Active Admin Card
        ElevatedCard(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Active Yearly Alumni Admin", fontSize = 12.sp, color = Color.Gray)
                        Text(activeAdmin?.fullName ?: "No Admin Assigned", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = GhanaNavyPrimary)
                        Text("Term Year: ${activeAdmin?.adminTermYear ?: "2026"}", fontSize = 12.sp, color = GhanaGoldAccent)
                    }

                    Icon(Icons.Default.Star, contentDescription = null, tint = GhanaGoldAccent, modifier = Modifier.size(32.dp))
                }

                Divider(modifier = Modifier.padding(vertical = 12.dp))

                Text("Direct Consultation Channels (Proprietor Line)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { onInitiateCall("AUDIO_CONSULTATION") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("call_proprietor_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = GhanaNavyPrimary)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Audio Call", fontSize = 12.sp)
                    }

                    Button(
                        onClick = { onInitiateCall("VIDEO_CONSULTATION") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("video_proprietor_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = GhanaEmeraldGreen)
                    ) {
                        Icon(Icons.Default.Videocam, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Video Call", fontSize = 12.sp)
                    }
                }
            }
        }

        // Annual Admin Rotation Management
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Annual Admin Rotation System", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Select a verified alumni member to assign as the new yearly Alumni Admin.", fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(10.dp))

                activatedAlumni.forEach { alumni ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${alumni.fullName} (${alumni.batchTag})", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        if (alumni.isAlumniAdmin) {
                            Text("Current Admin", color = GhanaGoldAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        } else {
                            OutlinedButton(
                                onClick = { onRotateAdmin(alumni.id) },
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text("Assign Admin", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // Manual Batch Import Tool Button
        Button(
            onClick = onOpenBatchImport,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = GhanaNavyPrimary)
        ) {
            Icon(Icons.Default.UploadFile, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Manual Batch Onboarding (Upload Historical Records)")
        }
    }
}

@Composable
fun AspirantIntentAndVotingTab(
    allAspirants: List<AlumniAspirant>,
    activeUserSession: AlumniProfile?,
    onSubmitIntent: (manifesto: String, termYear: String) -> Unit,
    onCastVote: (candidateDocId: String, termYear: String) -> Unit
) {
    var showIntentModal by remember { mutableStateOf(false) }
    var manifestoText by remember { mutableStateOf("Pledging to establish a revolving Tertiary Scholarship Fund and expand STEM mentorship for JHS graduates.") }
    var termYearText by remember { mutableStateOf("2027") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Banner Card
        Card(
            colors = CardDefaults.cardColors(containerColor = GhanaNavyPrimary),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.HowToVote, contentDescription = null, tint = GhanaGoldAccent, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Aspirant Intent & Time-Locked Voting", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                        Text("Annual Alumni Admin Elections", color = GhanaGoldAccent, fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Community members cast 1 vote per year. Voting opens strictly in the 7-day window prior to term start date. Double voting and self voting are blocked by backend transactions.",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp
                )
            }
        }

        // 7-Day Time-Lock Voting Window Countdown & Status Card
        Card(
            colors = CardDefaults.cardColors(containerColor = GhanaGoldContainer.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = GhanaEmeraldGreen,
                            modifier = Modifier.size(10.dp)
                        ) {}
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("VOTING WINDOW OPEN", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = GhanaNavyPrimary)
                    }
                    Text("7-Day Time Lock", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = GhanaNavyPrimary)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Term: 2027 Alumni Admin | Time Remaining: 3 Days, 11 Hours",
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = GhanaNavyPrimary
                )
            }
        }

        // Action Row: Submit Candidacy Intent
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Candidacy Declaration", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Qualified alumni members can submit their intent for the upcoming term year.", fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { showIntentModal = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("submit_intent_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = GhanaNavyPrimary)
                ) {
                    Icon(Icons.Default.Campaign, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Submit Aspirant Intent (2027 Term)")
                }
            }
        }

        // Candidates List & Voting Cards
        Text("Verified Aspirant Candidates (${allAspirants.size})", fontWeight = FontWeight.Bold, fontSize = 15.sp)

        if (allAspirants.isEmpty()) {
            Card(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("No aspirant candidates registered yet. Click above to submit intent!", color = Color.Gray, fontSize = 13.sp)
                }
            }
        } else {
            allAspirants.forEach { aspirant ->
                val isSelfCandidate = activeUserSession?.id.toString() == aspirant.userId
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(aspirant.candidateName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    if (isSelfCandidate) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = GhanaNavyPrimary.copy(alpha = 0.1f)
                                        ) {
                                            Text("You", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GhanaNavyPrimary)
                                        }
                                    }
                                }
                                Text("${aspirant.batchTag} • Term ${aspirant.termYear}", fontSize = 12.sp, color = Color.Gray)
                            }

                            // Vote count badge
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = GhanaEmeraldGreen.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, GhanaEmeraldGreen)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.HowToVote, contentDescription = null, tint = GhanaEmeraldGreen, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("${aspirant.voteCount} Votes", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = GhanaEmeraldGreen)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(aspirant.manifesto, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { onCastVote(aspirant.docId, aspirant.termYear) },
                            enabled = !isSelfCandidate,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("vote_candidate_${aspirant.docId}"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GhanaEmeraldGreen,
                                disabledContainerColor = Color.LightGray
                            )
                        ) {
                            Icon(Icons.Default.HowToVote, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (isSelfCandidate) "Cannot Vote for Self" else "Cast Vote for ${aspirant.candidateName.split(" ").first()}"
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Dialog for Submitting Intent
    if (showIntentModal) {
        AlertDialog(
            onDismissRequest = { showIntentModal = false },
            icon = { Icon(Icons.Default.Campaign, contentDescription = null, tint = GhanaNavyPrimary, modifier = Modifier.size(32.dp)) },
            title = { Text("Submit Aspirant Intent", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Declare your candidacy for the Alumni Admin position.", fontSize = 12.sp, color = Color.Gray)
                    OutlinedTextField(
                        value = termYearText,
                        onValueChange = { termYearText = it },
                        label = { Text("Term Year") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = manifestoText,
                        onValueChange = { manifestoText = it },
                        label = { Text("Manifesto & Key Goals") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        maxLines = 4
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSubmitIntent(manifestoText, termYearText)
                        showIntentModal = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GhanaNavyPrimary)
                ) {
                    Text("Submit Intent")
                }
            },
            dismissButton = {
                TextButton(onClick = { showIntentModal = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
