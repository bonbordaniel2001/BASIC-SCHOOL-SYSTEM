package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LessonPlan
import com.example.data.model.MessageLog
import com.example.data.model.StaffMember
import com.example.data.model.StudentGrade
import com.example.data.model.StudentLedger
import com.example.data.model.TransactionApproval
import com.example.ui.components.RoleDelegationDialog
import com.example.ui.theme.GhanaEmeraldGreen
import com.example.ui.theme.GhanaGoldAccent
import com.example.ui.theme.GhanaNavyPrimary
import com.example.ui.viewmodel.SchoolViewModel

@Composable
fun ProprietorScreen(
    viewModel: SchoolViewModel,
    modifier: Modifier = Modifier
) {
    val staffList by viewModel.allStaff.collectAsState()
    val approvalsList by viewModel.allApprovals.collectAsState()
    val messageLogs by viewModel.allMessages.collectAsState()
    val filterStatus by viewModel.approvalFilter.collectAsState()
    val schoolName by viewModel.schoolName.collectAsState()
    val monthlyFeeStats by viewModel.monthlyFeeStats.collectAsState()
    val enrollmentTrendPoints by viewModel.enrollmentTrendPoints.collectAsState()
    val allGrades by viewModel.allGrades.collectAsState()
    val allLessonPlans by viewModel.allLessonPlans.collectAsState()
    val allStudentLedgers by viewModel.allStudentLedgers.collectAsState()

    var showRoleModal by remember { mutableStateOf(false) }
    var showEditSchoolDialog by remember { mutableStateOf(false) }
    var showMigrationDialog by remember { mutableStateOf(false) }
    var tempSchoolNameInput by remember { mutableStateOf(schoolName) }

    // Add / Drop Staff State
    var showAddStaffDialog by remember { mutableStateOf(false) }
    var newStaffName by remember { mutableStateOf("") }
    var newStaffRole by remember { mutableStateOf("Class Teacher") }
    var newStaffCode by remember { mutableStateOf("") }
    var newStaffPhone by remember { mutableStateOf("0244123456") }

    var showDropStaffDialog by remember { mutableStateOf(false) }
    var staffToDrop by remember { mutableStateOf<StaffMember?>(null) }

    // Add / Drop Student State
    var showAddStudentDialog by remember { mutableStateOf(false) }
    var newStudentName by remember { mutableStateOf("") }
    var newStudentClass by remember { mutableStateOf("JHS 2 - Gold") }
    var newStudentPhone by remember { mutableStateOf("0244123456") }
    var newStudentFees by remember { mutableStateOf("1200.0") }

    var showDropStudentDialog by remember { mutableStateOf(false) }
    var studentToDrop by remember { mutableStateOf<StudentLedger?>(null) }

    // Lesson Plan Review Dialog State
    var showPlanReviewDialog by remember { mutableStateOf(false) }
    var selectedPlanToReview by remember { mutableStateOf<LessonPlan?>(null) }
    var reviewFeedbackText by remember { mutableStateOf("") }
    var reviewTargetStatus by remember { mutableStateOf("APPROVED") }

    // Add Staff Dialog
    if (showAddStaffDialog) {
        AlertDialog(
            onDismissRequest = { showAddStaffDialog = false },
            title = { Text("Register New Teacher / Staff", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newStaffName,
                        onValueChange = { newStaffName = it },
                        label = { Text("Staff Full Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("add_staff_name_field")
                    )
                    OutlinedTextField(
                        value = newStaffRole,
                        onValueChange = { newStaffRole = it },
                        label = { Text("Role / Subject (e.g. Class Teacher)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("add_staff_role_field")
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newStaffCode,
                            onValueChange = { newStaffCode = it },
                            label = { Text("Staff ID Code") },
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("add_staff_code_field")
                        )
                        OutlinedTextField(
                            value = newStaffPhone,
                            onValueChange = { newStaffPhone = it },
                            label = { Text("Phone") },
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("add_staff_phone_field")
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newStaffName.isNotBlank()) {
                            viewModel.addStaffByProprietor(
                                name = newStaffName,
                                role = newStaffRole.ifBlank { "Class Teacher" },
                                staffIdCode = newStaffCode.ifBlank { "STF-00${staffList.size + 1}" },
                                phone = newStaffPhone.ifBlank { "0244123456" }
                            )
                            showAddStaffDialog = false
                            newStaffName = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GhanaNavyPrimary),
                    modifier = Modifier.testTag("confirm_add_staff_button")
                ) {
                    Text("Add Staff Member")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddStaffDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Drop Staff Dialog
    if (showDropStaffDialog && staffToDrop != null) {
        val st = staffToDrop!!
        AlertDialog(
            onDismissRequest = { showDropStaffDialog = false },
            title = { Text("Drop Staff Member?", fontWeight = FontWeight.Bold, color = Color(0xFFC62828)) },
            text = { Text("Are you sure you want to drop staff member '${st.name}' (${st.primaryRole}, Code: ${st.staffCode}) from the institution?", fontSize = 13.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.dropStaffByProprietor(st.id, st.name)
                        showDropStaffDialog = false
                        staffToDrop = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                    modifier = Modifier.testTag("confirm_drop_staff_button")
                ) {
                    Text("Drop Staff")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDropStaffDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Add Student Dialog
    if (showAddStudentDialog) {
        AlertDialog(
            onDismissRequest = { showAddStudentDialog = false },
            title = { Text("Enroll New Student", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newStudentName,
                        onValueChange = { newStudentName = it },
                        label = { Text("Student Full Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("add_student_name_field")
                    )
                    OutlinedTextField(
                        value = newStudentClass,
                        onValueChange = { newStudentClass = it },
                        label = { Text("Class (e.g. JHS 2 - Gold)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("add_student_class_field")
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newStudentPhone,
                            onValueChange = { newStudentPhone = it },
                            label = { Text("Guardian Phone") },
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("add_student_phone_field")
                        )
                        OutlinedTextField(
                            value = newStudentFees,
                            onValueChange = { newStudentFees = it },
                            label = { Text("Term Fees (GH₵)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("add_student_fees_field")
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newStudentName.isNotBlank()) {
                            viewModel.addStudentByProprietor(
                                name = newStudentName,
                                className = newStudentClass.ifBlank { "JHS 2 - Gold" },
                                guardianPhone = newStudentPhone.ifBlank { "0244123456" },
                                totalFeesGhc = newStudentFees.toDoubleOrNull() ?: 1200.0
                            )
                            showAddStudentDialog = false
                            newStudentName = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GhanaNavyPrimary),
                    modifier = Modifier.testTag("confirm_add_student_button")
                ) {
                    Text("Enroll Student")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddStudentDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Drop Student Dialog
    if (showDropStudentDialog && studentToDrop != null) {
        val st = studentToDrop!!
        AlertDialog(
            onDismissRequest = { showDropStudentDialog = false },
            title = { Text("Drop Student Record?", fontWeight = FontWeight.Bold, color = Color(0xFFC62828)) },
            text = { Text("Are you sure you want to drop student '${st.studentName}' (ID #${st.studentId}, Class: ${st.className})? This will remove their ledger and profile records.", fontSize = 13.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.dropStudentByProprietor(st.studentId, st.studentName)
                        showDropStudentDialog = false
                        studentToDrop = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                    modifier = Modifier.testTag("confirm_drop_student_button")
                ) {
                    Text("Drop Student")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDropStudentDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showPlanReviewDialog && selectedPlanToReview != null) {
        val plan = selectedPlanToReview!!
        AlertDialog(
            onDismissRequest = { showPlanReviewDialog = false },
            title = {
                Column {
                    Text("Lesson Plan Management Review", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("${plan.teacherName} • ${plan.subject} (${plan.className})", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Topic: ${plan.topic}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Objectives: ${plan.objectives}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Date: ${plan.lessonDate} (${plan.durationMinutes} mins)", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    Text("Decision:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FilterChip(
                            selected = reviewTargetStatus == "APPROVED",
                            onClick = { reviewTargetStatus = "APPROVED" },
                            label = { Text("Approve Plan", fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFD1E7DD), selectedLabelColor = Color(0xFF0F5132)),
                            modifier = Modifier.testTag("review_status_approve")
                        )
                        FilterChip(
                            selected = reviewTargetStatus == "REVISION_REQUESTED",
                            onClick = { reviewTargetStatus = "REVISION_REQUESTED" },
                            label = { Text("Request Revision", fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Error, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFF8D7DA), selectedLabelColor = Color(0xFF842029)),
                            modifier = Modifier.testTag("review_status_revision")
                        )
                    }

                    OutlinedTextField(
                        value = reviewFeedbackText,
                        onValueChange = { reviewFeedbackText = it },
                        label = { Text("Management Feedback / Recommendations") },
                        modifier = Modifier.fillMaxWidth().testTag("review_feedback_text_field")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.reviewLessonPlan(
                            planId = plan.id,
                            newStatus = reviewTargetStatus,
                            feedback = reviewFeedbackText,
                            topic = plan.topic,
                            teacherName = plan.teacherName
                        )
                        showPlanReviewDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (reviewTargetStatus == "APPROVED") GhanaNavyPrimary else Color(0xFF842029)
                    ),
                    modifier = Modifier.testTag("submit_lesson_plan_review_button")
                ) {
                    Text(if (reviewTargetStatus == "APPROVED") "Approve & Send Feedback" else "Send Revision Request")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPlanReviewDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showMigrationDialog) {
        com.example.ui.components.RoomToFirestoreMigrationDialog(
            onDismissRequest = { showMigrationDialog = false }
        )
    }

    val filteredApprovals = remember(approvalsList, filterStatus) {
        if (filterStatus == "ALL") approvalsList
        else approvalsList.filter { it.status == filterStatus }
    }

    if (showRoleModal) {
        RoleDelegationDialog(
            staffList = staffList,
            onTogglePermission = { staff, key -> viewModel.toggleStaffPermission(staff, key) },
            onDismiss = { showRoleModal = false }
        )
    }

    if (showEditSchoolDialog) {
        AlertDialog(
            onDismissRequest = { showEditSchoolDialog = false },
            title = { Text("Set Institution Name", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("As Proprietor, set or edit the official name of your institution:", fontSize = 13.sp)
                    OutlinedTextField(
                        value = tempSchoolNameInput,
                        onValueChange = { tempSchoolNameInput = it },
                        label = { Text("School Name") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_school_name_text_field")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tempSchoolNameInput.isNotBlank()) {
                            viewModel.updateSchoolName(tempSchoolNameInput)
                        }
                        showEditSchoolDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GhanaNavyPrimary),
                    modifier = Modifier.testTag("save_school_name_button")
                ) {
                    Text("Save Name")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showEditSchoolDialog = false },
                    modifier = Modifier.testTag("cancel_school_name_button")
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        // --- SECTION 0: PROPRIETOR INSTITUTION SETTINGS CARD ---
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = GhanaNavyPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("proprietor_school_banner_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = GhanaGoldAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Proprietor Control Panel",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GhanaGoldAccent
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = schoolName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        IconButton(
                            onClick = {
                                tempSchoolNameInput = schoolName
                                showEditSchoolDialog = true
                            },
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.15f), CircleShape)
                                .testTag("edit_school_name_icon_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit School Name",
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        color = Color.White.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showMigrationDialog = true }
                            .testTag("open_room_to_firestore_migration_plan_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = null,
                                    tint = GhanaGoldAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = "Room-to-Firestore Migration Strategy",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Manage offline SQLite & live Cloud database sync",
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = GhanaGoldAccent
                            )
                        }
                    }
                }
            }
        }

        // --- SECTION 1: DATA VISUALIZATIONS & ANALYTICS ---
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Executive Performance Analytics",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // Chart 1: Monthly Fee Collection Bar Chart
                com.example.ui.components.MonthlyFeeBarChart(
                    feeStats = monthlyFeeStats
                )

                // Chart 2: Enrollment Growth Line Chart
                com.example.ui.components.EnrollmentTrendLineChart(
                    trendPoints = enrollmentTrendPoints
                )
            }
        }

        // --- SECTION 2: ROLE DELEGATION MATRIX LAUNCHER ---
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("role_delegation_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
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
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(GhanaNavyPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ManageAccounts,
                                    contentDescription = null,
                                    tint = GhanaGoldAccent,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Role Delegation Matrix",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "${staffList.size} Staff Members Configured",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = { showRoleModal = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("open_role_matrix_button")
                        ) {
                            Text("Configure Matrix", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Brief Staff Permissions Summary Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        staffList.take(3).forEach { staff ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = staff.name.take(12),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = staff.primaryRole,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 10.sp,
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        if (staff.isCanEditGrades) BadgeChip("Grades")
                                        if (staff.isCanAccessFinancials) BadgeChip("Finance")
                                        if (staff.isCanApproveOverrides) BadgeChip("Override")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- SECTION: PROPRIETOR STAFF & STUDENT ROSTER MANAGEMENT ---
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("proprietor_staff_student_roster_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Institution Staff & Student Roster",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = GhanaNavyPrimary
                            )
                            Text(
                                text = "${staffList.size} Staff Members • ${allStudentLedgers.size} Enrolled Students",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Button(
                                onClick = { showAddStaffDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GhanaNavyPrimary),
                                modifier = Modifier.testTag("proprietor_add_staff_button")
                            ) {
                                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+ Staff", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { showAddStudentDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GhanaEmeraldGreen),
                                modifier = Modifier.testTag("proprietor_add_student_button")
                            ) {
                                Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+ Student", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Staff Roster (Add / Drop Staff):", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        staffList.forEach { staff ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(GhanaNavyPrimary),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = staff.name.take(1),
                                                fontWeight = FontWeight.Bold,
                                                color = GhanaGoldAccent,
                                                fontSize = 14.sp
                                            )
                                        }
                                        Column {
                                            Text(staff.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text("${staff.primaryRole} • ${staff.staffCode}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            staffToDrop = staff
                                            showDropStaffDialog = true
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC62828)),
                                        modifier = Modifier.testTag("drop_staff_button_${staff.id}")
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Drop", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Student Roll (Add / Drop Students):", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        allStudentLedgers.forEach { st ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primaryContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Face, contentDescription = null, tint = GhanaNavyPrimary, modifier = Modifier.size(20.dp))
                                        }
                                        Column {
                                            Text(st.studentName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text("ID #${st.studentId} • ${st.className} • GH₵ ${String.format("%.2f", st.balanceGhc)} Bal", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            studentToDrop = st
                                            showDropStudentDialog = true
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC62828)),
                                        modifier = Modifier.testTag("drop_student_button_${st.studentId}")
                                    ) {
                                        Icon(Icons.Default.PersonRemove, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Drop", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- SECTION: ACADEMIC PERFORMANCE & GRADE ANALYTICS (PROPRIETOR VIEW) ---
        item {
            val totalGradesCount = allGrades.size
            val schoolAvgScore = if (totalGradesCount > 0) allGrades.map { it.totalScore }.average() else 0.0
            val distinctionsCount = allGrades.count { it.gradeLetter == "A1" || it.gradeLetter == "B2" }
            val distinctionPct = if (totalGradesCount > 0) (distinctionsCount.toDouble() / totalGradesCount * 100).toInt() else 0

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("proprietor_academic_analytics_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
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
                                    .clip(CircleShape)
                                    .background(GhanaNavyPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MilitaryTech,
                                    contentDescription = null,
                                    tint = GhanaGoldAccent,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Academic Performance Overview",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "$totalGradesCount Subject Assessments Processed",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = GhanaEmeraldGreen.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "${String.format("%.1f", schoolAvgScore)}% Avg",
                                fontWeight = FontWeight.ExtraBold,
                                color = GhanaNavyPrimary,
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Distinction Rate", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$distinctionPct%", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = GhanaNavyPrimary)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Total Graded Subjects", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$totalGradesCount", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("A1 / B2 Counts", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$distinctionsCount", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2E7D32))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Subject Average Breakdown:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    val subjectsGrouped = allGrades.groupBy { it.subject }
                    subjectsGrouped.forEach { (sub, gradesList) ->
                        val subAvg = gradesList.map { it.totalScore }.average()
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(sub, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                LinearProgressIndicator(
                                    progress = { (subAvg / 100.0).toFloat().coerceIn(0f, 1f) },
                                    color = GhanaNavyPrimary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.width(100.dp).height(6.dp).clip(CircleShape)
                                )
                                Text("${String.format("%.1f", subAvg)}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GhanaNavyPrimary)
                            }
                        }
                    }
                }
            }
        }

        // --- SECTION 2: TRANSACTION APPROVAL QUEUE ---
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Transaction Approval Queue",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Loan requests & payment fee overrides",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Badge(
                        containerColor = if (filteredApprovals.any { it.status == "PENDING" }) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            text = "${approvalsList.count { it.status == "PENDING" }} Pending",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Approval Filter Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("ALL", "PENDING", "APPROVED", "REJECTED").forEach { status ->
                        FilterChip(
                            selected = filterStatus == status,
                            onClick = { viewModel.setApprovalFilter(status) },
                            label = { Text(status, fontSize = 12.sp) },
                            modifier = Modifier.testTag("filter_chip_$status")
                        )
                    }
                }
            }
        }

        if (filteredApprovals.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No transaction requests matching filter.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(filteredApprovals, key = { it.id }) { approval ->
                ApprovalQueueCard(
                    approval = approval,
                    onApprove = { viewModel.approveTransaction(approval.id) },
                    onReject = { viewModel.rejectTransaction(approval.id) }
                )
            }
        }

        // --- SECTION: DAILY LESSON PLANS MANAGEMENT REVIEW ---
        item {
            val pendingCount = allLessonPlans.count { it.status == "PENDING_REVIEW" }
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("proprietor_lesson_plan_section_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Teacher Daily Lesson Plans Review",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = GhanaNavyPrimary
                            )
                            Text(
                                text = "Review, approve, or request revisions on teacher pedagogical notes by class & subject",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (pendingCount > 0) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFFFF3CD)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.Pending, contentDescription = null, tint = Color(0xFF664D03), modifier = Modifier.size(12.dp))
                                    Text("$pendingCount Pending", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF664D03))
                                }
                            }
                        }
                    }
                }
            }
        }

        if (allLessonPlans.isEmpty()) {
            item {
                Text("No teacher lesson plans submitted yet.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            items(allLessonPlans, key = { it.id }) { plan ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth().testTag("proprietor_lesson_plan_item_${plan.id}")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = plan.teacherName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "${plan.subject} • ${plan.className} • ${plan.lessonDate}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            val (statusLabel, statusBg, statusColor) = when (plan.status) {
                                "APPROVED" -> Triple("APPROVED", Color(0xFFD1E7DD), Color(0xFF0F5132))
                                "REVISION_REQUESTED" -> Triple("REVISION REQUESTED", Color(0xFFF8D7DA), Color(0xFF842029))
                                else -> Triple("PENDING REVIEW", Color(0xFFFFF3CD), Color(0xFF664D03))
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = statusBg
                            ) {
                                Text(
                                    text = statusLabel,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = statusColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Topic: ${plan.topic}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        if (plan.subTopic.isNotBlank()) {
                            Text("Subtopic: ${plan.subTopic}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text("🎯 Objectives: ${plan.objectives}", fontSize = 11.sp)
                        if (plan.teachingMaterials.isNotBlank()) {
                            Text("📚 Materials: ${plan.teachingMaterials}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (plan.procedureSteps.isNotBlank()) {
                            Text("📝 Procedure: ${plan.procedureSteps}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        if (plan.managementFeedback.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Feedback given: ${plan.managementFeedback}", fontSize = 11.sp, modifier = Modifier.padding(8.dp), color = GhanaNavyPrimary)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = {
                                    selectedPlanToReview = plan
                                    reviewTargetStatus = if (plan.status == "APPROVED") "APPROVED" else "APPROVED"
                                    reviewFeedbackText = plan.managementFeedback.ifBlank { "Approved by Proprietor." }
                                    showPlanReviewDialog = true
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GhanaNavyPrimary),
                                modifier = Modifier.testTag("review_lesson_plan_button_${plan.id}")
                            ) {
                                Icon(Icons.Default.RateReview, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (plan.status == "PENDING_REVIEW") "Review Plan" else "Edit Review", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // --- SECTION 3: SMS / WHATSAPP MESSAGING PANEL ---
        item {
            MessagingPanel(viewModel = viewModel)
        }
    }
}

@Composable
private fun BadgeChip(text: String) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = GhanaGoldAccent.copy(alpha = 0.2f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 9.sp,
            color = GhanaNavyPrimary,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
        )
    }
}

@Composable
private fun ApprovalQueueCard(
    approval: TransactionApproval,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("approval_card_${approval.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = approval.requestorName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${approval.requestorRole} • ${approval.dateString}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                StatusChip(status = approval.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = approval.requestType.replace("_", " "),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = approval.reason,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = "GH₵ ${String.format("%.2f", approval.amountGhc)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (approval.decisionNote.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Note: ${approval.decisionNote}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (approval.status == "PENDING") {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onReject,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("reject_approval_${approval.id}")
                    ) {
                        Text("Reject", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onApprove,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("approve_approval_${approval.id}")
                    ) {
                        Text("Approve Request", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusChip(status: String) {
    val (bgColor, textColor) = when (status) {
        "APPROVED" -> Color(0xFFD1E7DD) to Color(0xFF0F5132)
        "REJECTED" -> Color(0xFFF8D7DA) to Color(0xFF842029)
        else -> Color(0xFFFFF3CD) to Color(0xFF664D03)
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor
    ) {
        Text(
            text = status,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun MessagingPanel(viewModel: SchoolViewModel) {
    val targetType by viewModel.msgTargetType.collectAsState()
    val targetClass by viewModel.msgTargetClass.collectAsState()
    val contact by viewModel.msgIndividualContact.collectAsState()
    val channel by viewModel.msgChannel.collectAsState()
    val messageText by viewModel.msgText.collectAsState()
    val messageLogs by viewModel.allMessages.collectAsState()

    val characterCount = messageText.length
    val smsBatches = (characterCount / 160) + 1
    val recipients = when (targetType) {
        "ALL_PARENTS" -> 380
        "CLASS_SPECIFIC" -> 42
        else -> 1
    }
    val costGhc = if (channel == "SMS") recipients * smsBatches * 0.06 else 0.00

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("messaging_panel_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Sms,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        text = "SMS & WhatsApp Broadcast Panel",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Low-bandwidth parent messaging & template engine",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Channel Selector (SMS vs WhatsApp)
            Text("Communication Channel:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = channel == "WHATSAPP",
                    onClick = { viewModel.setMsgChannel("WHATSAPP") },
                    label = { Text("WhatsApp Direct") },
                    leadingIcon = { Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier.testTag("channel_whatsapp")
                )
                FilterChip(
                    selected = channel == "SMS",
                    onClick = { viewModel.setMsgChannel("SMS") },
                    label = { Text("GSM SMS Broadcast") },
                    leadingIcon = { Icon(Icons.Default.Sms, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier.testTag("channel_sms")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Target Filter Selector
            Text("Target Recipients:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = targetType == "ALL_PARENTS",
                    onClick = { viewModel.setMsgTargetType("ALL_PARENTS") },
                    label = { Text("All Parents (380)") },
                    modifier = Modifier.testTag("target_all_parents")
                )
                FilterChip(
                    selected = targetType == "CLASS_SPECIFIC",
                    onClick = { viewModel.setMsgTargetType("CLASS_SPECIFIC") },
                    label = { Text("Class Specific") },
                    modifier = Modifier.testTag("target_class")
                )
                FilterChip(
                    selected = targetType == "INDIVIDUAL",
                    onClick = { viewModel.setMsgTargetType("INDIVIDUAL") },
                    label = { Text("Individual") },
                    modifier = Modifier.testTag("target_individual")
                )
            }

            if (targetType == "CLASS_SPECIFIC") {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = targetClass,
                    onValueChange = { viewModel.setMsgTargetClass(it) },
                    label = { Text("Select Class") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            } else if (targetType == "INDIVIDUAL") {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = contact,
                    onValueChange = { viewModel.setMsgIndividualContact(it) },
                    label = { Text("Parent Contact Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Message Body Text Field
            OutlinedTextField(
                value = messageText,
                onValueChange = { viewModel.setMsgText(it) },
                label = { Text("Message Body") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .testTag("msg_body_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Low-bandwidth stats preview box
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$characterCount chars ($smsBatches batch) • $recipients recipients",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (channel == "SMS") "Est. Cost: GH₵ ${String.format("%.2f", costGhc)}" else "Free via WhatsApp",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = { viewModel.sendBroadcastMessage() },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("send_broadcast_button")
            ) {
                Icon(imageVector = Icons.Default.Send, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Dispatch Broadcast Message", fontWeight = FontWeight.Bold)
            }

            if (messageLogs.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(10.dp))

                Text("Recent Broadcast Logs:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                messageLogs.take(2).forEach { log ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${log.targetDetail} (${log.channel})",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = log.messageText,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = log.timestampString.take(11),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
