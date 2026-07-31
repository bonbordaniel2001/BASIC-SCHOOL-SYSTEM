package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ClassAssignment
import com.example.data.model.ClassTimetable
import com.example.data.model.ClockInLog
import com.example.data.model.DigitalResource
import com.example.data.model.DirectMessage
import com.example.data.model.LessonPlan
import com.example.data.model.MessageLog
import com.example.data.model.StaffMember
import com.example.data.model.StudentFeePayment
import com.example.data.model.StudentGrade
import com.example.data.model.StudentLedger
import com.example.data.model.StudentProfile
import com.example.data.model.TeacherLoanRequest
import com.example.data.model.TransactionApproval
import com.example.ui.components.LoadingOverlay
import com.example.ui.components.LoadingSpinner
import com.example.ui.components.RoleDelegationDialog
import com.example.ui.components.StudentListViewWithSearch
import com.example.ui.components.StudentProfileCard
import com.example.ui.components.StudentProfileDialog
import com.example.ui.components.UploadResourceDialog
import com.example.ui.theme.GhanaEmeraldGreen
import com.example.ui.theme.GhanaGoldAccent
import com.example.ui.theme.GhanaGoldContainer
import com.example.ui.theme.GhanaNavyPrimary
import com.example.ui.viewmodel.SchoolViewModel

@Composable
fun ProprietorScreen(
    viewModel: SchoolViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val staffList by viewModel.allStaff.collectAsState()
    val approvalsList by viewModel.allApprovals.collectAsState()
    val messageLogs by viewModel.allMessages.collectAsState()
    val filterStatus by viewModel.approvalFilter.collectAsState()
    val schoolName by viewModel.schoolName.collectAsState()
    val pendingUserAccounts by viewModel.pendingUserAccounts.collectAsState()
    val monthlyFeeStats by viewModel.monthlyFeeStats.collectAsState()
    val enrollmentTrendPoints by viewModel.enrollmentTrendPoints.collectAsState()
    val allGrades by viewModel.allGrades.collectAsState()
    val allLessonPlans by viewModel.allLessonPlans.collectAsState()
    val allStudentLedgers by viewModel.allStudentLedgers.collectAsState()
    val allDailyAttendance by viewModel.allDailyAttendance.collectAsState()
    val allFeePayments by viewModel.allFeePayments.collectAsState()
    val allFeeTransactions by viewModel.allFeeTransactions.collectAsState()
    val allTimetables by viewModel.allTimetables.collectAsState()
    val allTeacherLoanRequests by viewModel.allTeacherLoanRequests.collectAsState()
    val allDirectMessages by viewModel.allDirectMessages.collectAsState()
    val schoolSettings by viewModel.schoolSettings.collectAsState()
    val staffClockIns by viewModel.allClockInLogs.collectAsState()
    val allDigitalResources by viewModel.allDigitalResources.collectAsState()
    val allClassAssignments by viewModel.allClassAssignments.collectAsState()
    val allStudentProfiles by viewModel.allStudentProfiles.collectAsState()
    val uiLoadingState by viewModel.uiLoadingState.collectAsState()
    val loadingMessage by viewModel.loadingMessage.collectAsState()

    var selectedStudentProfileForModal by remember { mutableStateOf<StudentProfile?>(null) }

    // Proprietor Portal Navigation Bar Active Tab (0: Overview & Hub, 1: Fee Balances, 2: Staff & Roster, 3: Analytics & CSV, 4: Lesson Plans, 5: Timetable, 6: Loans, 7: Staff Clock-In, 8: Parent Messages, 9: Broadcast & SMS, 10: Digital Library, 11: Assignments, 12: Directory, 13: Roles)
    var activeProprietorTab by remember { mutableStateOf(0) }

    // Digital Library & Media Upload State
    var showUploadResourceDialog by remember { mutableStateOf(false) }
    var resourceTitleInput by remember { mutableStateOf("") }
    var resourceAuthorInput by remember { mutableStateOf("Ministry of Education / GES") }
    var resourceCategoryInput by remember { mutableStateOf("TEXTBOOK") }
    var resourceTypeInput by remember { mutableStateOf("DOCUMENT") }
    var resourceTargetClassInput by remember { mutableStateOf("JHS 2 - Gold") }
    var resourceSubjectInput by remember { mutableStateOf("Mathematics") }
    var resourceAudienceInput by remember { mutableStateOf("ALL") }
    var resourceDescriptionInput by remember { mutableStateOf("") }
    var resourceFileFormatInput by remember { mutableStateOf("PDF") }
    var libraryCategoryFilter by remember { mutableStateOf("ALL") }
    var librarySearchQuery by remember { mutableStateOf("") }

    // Assignment Tracking Oversight State
    var assignmentPeriodFilter by remember { mutableStateOf("ALL") } // "ALL", "DAILY", "WEEKLY", "TERMLY"
    var assignmentTeacherFilter by remember { mutableStateOf("ALL") }

    // Proprietor Broadcast Dispatcher State
    var broadcastTitleInput by remember { mutableStateOf("") }
    var broadcastBodyInput by remember { mutableStateOf("") }
    var broadcastAudienceInput by remember { mutableStateOf("ALL") }

    // Loan Decision State
    var selectedLoanForDecision by remember { mutableStateOf<TeacherLoanRequest?>(null) }
    var loanDecisionNoteInput by remember { mutableStateOf("") }
    var showLoanDecisionDialog by remember { mutableStateOf(false) }

    // School-Wide Master Timetable State
    var proprietorTimetableClassFilter by remember { mutableStateOf("ALL") }
    var proprietorTimetableDayFilter by remember { mutableStateOf("ALL") }
    var proprietorTimetableSearchQuery by remember { mutableStateOf("") }
    var showProprietorSlotDialog by remember { mutableStateOf(false) }
    var editingProprietorSlot by remember { mutableStateOf<ClassTimetable?>(null) }
    var propSlotClassName by remember { mutableStateOf("JHS 2 - Gold") }
    var propSlotDayOfWeek by remember { mutableStateOf("Monday") }
    var propSlotPeriodNumber by remember { mutableStateOf("1") }
    var propSlotStartTime by remember { mutableStateOf("08:00 AM") }
    var propSlotEndTime by remember { mutableStateOf("08:45 AM") }
    var propSlotSubject by remember { mutableStateOf("Mathematics") }
    var propSlotTeacherName by remember { mutableStateOf("Mr. Emmanuel Mensah") }
    var propSlotClassroom by remember { mutableStateOf("Block J2-A") }

    // Student Fee Payment & Outstanding Balances Tracker State
    var showRecordPaymentDialog by remember { mutableStateOf(false) }
    var selectedStudentForPayment by remember { mutableStateOf<StudentLedger?>(null) }
    var recordAmountInput by remember { mutableStateOf("500.0") }
    var recordCategoryInput by remember { mutableStateOf("Tuition") }
    var recordMethodInput by remember { mutableStateOf("Cash at Bursar") }
    var recordNotesInput by remember { mutableStateOf("") }
    var feeFilterStatus by remember { mutableStateOf("ALL") } // "ALL", "OVERDUE", "PARTIAL", "PAID"
    var feeSearchQuery by remember { mutableStateOf("") }
    var feeClassFilter by remember { mutableStateOf("ALL") }

    // Export CSV Administrative Reports State
    var showExportCsvDialog by remember { mutableStateOf(false) }
    var selectedCsvReportType by remember { mutableStateOf("ATTENDANCE") } // "ATTENDANCE", "ACADEMIC", "COMBINED"
    var selectedCsvClassFilter by remember { mutableStateOf("ALL") }

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

    // Staff Payroll & Salary Management State
    var showPaySalaryDialog by remember { mutableStateOf(false) }
    var staffToPay by remember { mutableStateOf<StaffMember?>(null) }
    var paySalaryAmountInput by remember { mutableStateOf("") }
    var paySalaryMethodInput by remember { mutableStateOf("MTN MoMo") }
    var paySalaryNotesInput by remember { mutableStateOf("") }

    var showWithholdSalaryDialog by remember { mutableStateOf(false) }
    var staffToWithhold by remember { mutableStateOf<StaffMember?>(null) }
    var withholdReasonInput by remember { mutableStateOf("") }

    var showAdjustSalaryDialog by remember { mutableStateOf(false) }
    var staffToAdjust by remember { mutableStateOf<StaffMember?>(null) }
    var isSalaryIncreaseMode by remember { mutableStateOf(true) }
    var adjustSalaryAmountInput by remember { mutableStateOf("") }
    var adjustSalaryReasonInput by remember { mutableStateOf("") }

    // Add / Drop Student State
    var showAddStudentDialog by remember { mutableStateOf(false) }
    var newStudentName by remember { mutableStateOf("") }
    var newStudentClass by remember { mutableStateOf("JHS 2 - Gold") }
    var newStudentPhone by remember { mutableStateOf("0244123456") }
    var newStudentFees by remember { mutableStateOf("1200.0") }

    var showDropStudentDialog by remember { mutableStateOf(false) }
    var studentToDrop by remember { mutableStateOf<StudentLedger?>(null) }

    // Student Search Query State for Room Database Students
    var studentSearchQuery by remember { mutableStateOf("") }
    val filteredStudentLedgers = remember(allStudentLedgers, studentSearchQuery) {
        if (studentSearchQuery.isBlank()) {
            allStudentLedgers
        } else {
            allStudentLedgers.filter {
                it.studentName.contains(studentSearchQuery, ignoreCase = true) ||
                it.studentId.toString().contains(studentSearchQuery) ||
                it.className.contains(studentSearchQuery, ignoreCase = true)
            }
        }
    }

    // Lesson Plan Review Dialog State
    var showPlanReviewDialog by remember { mutableStateOf(false) }
    var selectedPlanToReview by remember { mutableStateOf<LessonPlan?>(null) }
    var reviewFeedbackText by remember { mutableStateOf("") }
    var reviewTargetStatus by remember { mutableStateOf("APPROVED") }

    // Schedule Subject Slot Dialog for Proprietor
    if (showProprietorSlotDialog) {
        AlertDialog(
            onDismissRequest = { showProprietorSlotDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = GhanaNavyPrimary)
                    Column {
                        Text(
                            if (editingProprietorSlot == null) "Schedule Timetable Slot" else "Edit Timetable Slot",
                            fontWeight = FontWeight.Bold,
                            color = GhanaNavyPrimary
                        )
                        Text(
                            "Assign periods, subjects, teachers & classrooms",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = propSlotClassName,
                        onValueChange = { propSlotClassName = it },
                        label = { Text("Class Name (e.g. JHS 2 - Gold)") },
                        modifier = Modifier.fillMaxWidth().testTag("prop_slot_class_name")
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = propSlotDayOfWeek,
                            onValueChange = { propSlotDayOfWeek = it },
                            label = { Text("Day of Week") },
                            modifier = Modifier.weight(1f).testTag("prop_slot_day")
                        )
                        OutlinedTextField(
                            value = propSlotPeriodNumber,
                            onValueChange = { propSlotPeriodNumber = it },
                            label = { Text("Period #") },
                            modifier = Modifier.weight(0.8f).testTag("prop_slot_period")
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = propSlotStartTime,
                            onValueChange = { propSlotStartTime = it },
                            label = { Text("Start Time") },
                            modifier = Modifier.weight(1f).testTag("prop_slot_start")
                        )
                        OutlinedTextField(
                            value = propSlotEndTime,
                            onValueChange = { propSlotEndTime = it },
                            label = { Text("End Time") },
                            modifier = Modifier.weight(1f).testTag("prop_slot_end")
                        )
                    }
                    OutlinedTextField(
                        value = propSlotSubject,
                        onValueChange = { propSlotSubject = it },
                        label = { Text("Subject Name") },
                        modifier = Modifier.fillMaxWidth().testTag("prop_slot_subject")
                    )
                    OutlinedTextField(
                        value = propSlotTeacherName,
                        onValueChange = { propSlotTeacherName = it },
                        label = { Text("Assigned Teacher") },
                        modifier = Modifier.fillMaxWidth().testTag("prop_slot_teacher")
                    )
                    OutlinedTextField(
                        value = propSlotClassroom,
                        onValueChange = { propSlotClassroom = it },
                        label = { Text("Classroom / Location") },
                        modifier = Modifier.fillMaxWidth().testTag("prop_slot_room")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val periodNum = propSlotPeriodNumber.toIntOrNull() ?: 1
                        viewModel.saveTimetableSlot(
                            className = propSlotClassName.trim(),
                            dayOfWeek = propSlotDayOfWeek.trim(),
                            periodNumber = periodNum,
                            startTime = propSlotStartTime.trim(),
                            endTime = propSlotEndTime.trim(),
                            subject = propSlotSubject.trim(),
                            teacherName = propSlotTeacherName.trim(),
                            classroom = propSlotClassroom.trim(),
                            existingId = editingProprietorSlot?.id ?: 0L
                        )
                        showProprietorSlotDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GhanaNavyPrimary),
                    modifier = Modifier.testTag("prop_save_slot_button")
                ) {
                    Text(if (editingProprietorSlot == null) "Save Schedule Slot" else "Update Schedule Slot")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showProprietorSlotDialog = false },
                    modifier = Modifier.testTag("prop_cancel_slot_button")
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Export CSV Administrative Reports Dialog
    if (showExportCsvDialog) {
        val generatedCsvText = remember(selectedCsvReportType, selectedCsvClassFilter, allDailyAttendance, allGrades) {
            when (selectedCsvReportType) {
                "ATTENDANCE" -> viewModel.generateAttendanceCsv(allDailyAttendance, selectedCsvClassFilter)
                "ACADEMIC" -> viewModel.generateAcademicPerformanceCsv(allGrades, selectedCsvClassFilter)
                else -> viewModel.generateCombinedAdminCsv(allDailyAttendance, allGrades, selectedCsvClassFilter)
            }
        }

        val rowCount = remember(generatedCsvText) {
            val lines = generatedCsvText.lines().filter { it.isNotBlank() && !it.startsWith("===") }
            if (lines.size > 1) lines.size - 1 else 0
        }

        val reportTitle = when (selectedCsvReportType) {
            "ATTENDANCE" -> "Attendance Register CSV Report"
            "ACADEMIC" -> "Academic Performance CSV Report"
            else -> "Combined Administrative CSV Package"
        }

        AlertDialog(
            onDismissRequest = { showExportCsvDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, tint = GhanaNavyPrimary)
                    Column {
                        Text("Export CSV Reports", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Administrative export for attendance & grades", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Report Type Selector Tabs
                    Text("Select Report Type:", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        FilterChip(
                            selected = selectedCsvReportType == "ATTENDANCE",
                            onClick = { selectedCsvReportType = "ATTENDANCE" },
                            label = { Text("Attendance", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GhanaNavyPrimary,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.height(28.dp).testTag("csv_type_attendance")
                        )
                        FilterChip(
                            selected = selectedCsvReportType == "ACADEMIC",
                            onClick = { selectedCsvReportType = "ACADEMIC" },
                            label = { Text("Academic", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GhanaNavyPrimary,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.height(28.dp).testTag("csv_type_academic")
                        )
                        FilterChip(
                            selected = selectedCsvReportType == "COMBINED",
                            onClick = { selectedCsvReportType = "COMBINED" },
                            label = { Text("Combined", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GhanaGoldAccent,
                                selectedLabelColor = GhanaNavyPrimary
                            ),
                            modifier = Modifier.height(28.dp).testTag("csv_type_combined")
                        )
                    }

                    // Class Filter Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Class:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        listOf("ALL", "JHS 2 - Gold", "Primary 6", "Primary 4").forEach { cls ->
                            FilterChip(
                                selected = selectedCsvClassFilter == cls,
                                onClick = { selectedCsvClassFilter = cls },
                                label = { Text(if (cls == "ALL") "All" else cls.take(6), fontSize = 10.sp) },
                                modifier = Modifier.height(28.dp).testTag("csv_class_$cls")
                            )
                        }
                    }

                    // Metadata Card
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = GhanaNavyPrimary.copy(alpha = 0.08f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(reportTitle, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = GhanaNavyPrimary)
                                Text("Format: Standard CSV • Filter: $selectedCsvClassFilter", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Surface(
                                color = GhanaNavyPrimary,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "$rowCount Row(s)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Text("CSV Data Stream Preview:", fontWeight = FontWeight.Bold, fontSize = 11.sp)

                    // Scrollable Monospace Preview Container
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1E1E1E),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    ) {
                        LazyColumn(modifier = Modifier.padding(8.dp)) {
                            items(generatedCsvText.lines()) { line ->
                                Text(
                                    text = line,
                                    color = if (line.startsWith("Record") || line.startsWith("Grade") || line.startsWith("===")) GhanaGoldAccent else Color(0xFFD4D4D4),
                                    fontSize = 9.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = {
                            viewModel.copyToClipboard(reportTitle, generatedCsvText)
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("copy_csv_button")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy Text", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            viewModel.exportAndShareCsv(reportTitle, generatedCsvText)
                            showExportCsvDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GhanaNavyPrimary),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("export_share_csv_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export & Share CSV", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportCsvDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Record Bursar Payment Dialog
    if (showRecordPaymentDialog) {
        AlertDialog(
            onDismissRequest = { showRecordPaymentDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Payments, contentDescription = null, tint = GhanaNavyPrimary)
                    Column {
                        Text("Record Fee Payment", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Direct Cash or Bank Deposit Entry", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Select Student:", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    // Student selection dropdown list chips
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(allStudentLedgers) { st ->
                            FilterChip(
                                selected = selectedStudentForPayment?.studentId == st.studentId,
                                onClick = {
                                    selectedStudentForPayment = st
                                    recordAmountInput = if (st.balanceGhc > 0) st.balanceGhc.toString() else "500.0"
                                },
                                label = { Text("${st.studentName} (${st.className.take(6)})", fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GhanaNavyPrimary,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.height(30.dp).testTag("select_student_${st.studentId}")
                            )
                        }
                    }

                    selectedStudentForPayment?.let { st ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = GhanaNavyPrimary.copy(alpha = 0.08f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(st.studentName, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = GhanaNavyPrimary)
                                Text("Guardian: ${st.guardianName} (${st.guardianPhone})", fontSize = 10.sp)
                                Text("Outstanding Balance: GH₵ ${String.format("%.2f", st.balanceGhc)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GhanaGoldAccent)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = recordAmountInput,
                        onValueChange = { recordAmountInput = it },
                        label = { Text("Amount Paid (GH₵)") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("record_payment_amount_input")
                    )

                    Text("Fee Category:", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("Tuition", "Feeding", "PTA Levy", "ICT Lab").forEach { cat ->
                            FilterChip(
                                selected = recordCategoryInput == cat,
                                onClick = { recordCategoryInput = cat },
                                label = { Text(cat, fontSize = 10.sp) },
                                modifier = Modifier.height(28.dp).testTag("record_category_$cat")
                            )
                        }
                    }

                    Text("Payment Method:", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("Cash at Bursar", "Bank Deposit", "MTN MoMo", "Telecel Cash").forEach { mth ->
                            FilterChip(
                                selected = recordMethodInput == mth,
                                onClick = { recordMethodInput = mth },
                                label = { Text(mth.take(12), fontSize = 10.sp) },
                                modifier = Modifier.height(28.dp).testTag("record_method_${mth.take(4)}")
                            )
                        }
                    }

                    OutlinedTextField(
                        value = recordNotesInput,
                        onValueChange = { recordNotesInput = it },
                        label = { Text("Notes / Reference (Optional)") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("record_payment_notes_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val st = selectedStudentForPayment ?: allStudentLedgers.firstOrNull()
                        val amt = recordAmountInput.toDoubleOrNull() ?: 0.0
                        if (st != null && amt > 0) {
                            viewModel.recordBursarPayment(
                                studentId = st.studentId,
                                amountGhc = amt,
                                paymentMethod = recordMethodInput,
                                feeCategory = recordCategoryInput,
                                academicTerm = "Term 3",
                                notes = recordNotesInput
                            )
                            showRecordPaymentDialog = false
                        } else {
                            Toast.makeText(context, "Please select student and enter valid amount", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GhanaNavyPrimary),
                    modifier = Modifier.testTag("submit_record_payment_button")
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save & Issue Receipt", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRecordPaymentDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

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

    // Pay Staff Salary Dialog
    if (showPaySalaryDialog && staffToPay != null) {
        val staff = staffToPay!!
        AlertDialog(
            onDismissRequest = { showPaySalaryDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Payments, contentDescription = null, tint = GhanaNavyPrimary)
                    Text("Disburse Staff Payment", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        color = GhanaNavyPrimary.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(staff.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("${staff.primaryRole} • ${staff.staffCode}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Base Monthly Salary: GH₵ ${String.format("%.2f", staff.monthlySalaryGhc)}", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = GhanaNavyPrimary)
                        }
                    }

                    Text("Payment Channel:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("MTN MoMo", "Telecel Cash", "Bank Transfer", "Cash").forEach { method ->
                            FilterChip(
                                selected = paySalaryMethodInput == method,
                                onClick = { paySalaryMethodInput = method },
                                label = { Text(method, fontSize = 10.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = paySalaryAmountInput,
                        onValueChange = { paySalaryAmountInput = it },
                        label = { Text("Payout Amount (GH₵)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("pay_salary_amount_input")
                    )

                    OutlinedTextField(
                        value = paySalaryNotesInput,
                        onValueChange = { paySalaryNotesInput = it },
                        label = { Text("Memo / Note (Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("pay_salary_notes_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = paySalaryAmountInput.toDoubleOrNull() ?: staff.monthlySalaryGhc
                        viewModel.payStaffSalary(staff.id, amt, paySalaryMethodInput, paySalaryNotesInput)
                        showPaySalaryDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GhanaNavyPrimary),
                    modifier = Modifier.testTag("confirm_pay_salary_button")
                ) {
                    Text("Disburse Payout")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPaySalaryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Withhold Staff Payment Dialog
    if (showWithholdSalaryDialog && staffToWithhold != null) {
        val staff = staffToWithhold!!
        AlertDialog(
            onDismissRequest = { showWithholdSalaryDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Block, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Text("Withhold Staff Payment", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "You are putting a hold on salary payment for ${staff.name} (${staff.primaryRole}). Please state the reason:",
                        fontSize = 12.sp
                    )

                    OutlinedTextField(
                        value = withholdReasonInput,
                        onValueChange = { withholdReasonInput = it },
                        label = { Text("Reason for Withholding Payment") },
                        placeholder = { Text("e.g. Unexcused absence, Pending marks submission") },
                        modifier = Modifier.fillMaxWidth().testTag("withhold_reason_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (withholdReasonInput.isNotBlank()) {
                            viewModel.withholdStaffPayment(staff.id, withholdReasonInput)
                            showWithholdSalaryDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_withhold_salary_button")
                ) {
                    Text("Withhold Payment")
                }
            },
            dismissButton = {
                TextButton(onClick = { showWithholdSalaryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Adjust Salary Dialog (Increase or Reduce)
    if (showAdjustSalaryDialog && staffToAdjust != null) {
        val staff = staffToAdjust!!
        val currentSalary = staff.monthlySalaryGhc
        val targetSalary = adjustSalaryAmountInput.toDoubleOrNull() ?: currentSalary
        val diff = targetSalary - currentSalary

        val newSalParsed = adjustSalaryAmountInput.toDoubleOrNull()
        val isValidIncrease = isSalaryIncreaseMode && newSalParsed != null && newSalParsed > currentSalary
        val isValidReduction = !isSalaryIncreaseMode && newSalParsed != null && newSalParsed < currentSalary
        val isSalaryConstraintValid = isValidIncrease || isValidReduction

        AlertDialog(
            onDismissRequest = { showAdjustSalaryDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        imageVector = if (isSalaryIncreaseMode) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                        contentDescription = null,
                        tint = if (isSalaryIncreaseMode) GhanaEmeraldGreen else MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = if (isSalaryIncreaseMode) "Increase Staff Salary" else "Reduce Staff Salary",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        FilterChip(
                            selected = isSalaryIncreaseMode,
                            onClick = { isSalaryIncreaseMode = true },
                            label = { Text("Increase (Raise/Bonus)") },
                            leadingIcon = { Icon(Icons.Default.TrendingUp, contentDescription = null, modifier = Modifier.size(14.dp)) },
                            modifier = Modifier.weight(1f).testTag("adjust_mode_increase")
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        FilterChip(
                            selected = !isSalaryIncreaseMode,
                            onClick = { isSalaryIncreaseMode = false },
                            label = { Text("Reduce (Deduction)") },
                            leadingIcon = { Icon(Icons.Default.TrendingDown, contentDescription = null, modifier = Modifier.size(14.dp)) },
                            modifier = Modifier.weight(1f).testTag("adjust_mode_reduce")
                        )
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(staff.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Current Base Salary: GH₵ ${String.format("%.2f", currentSalary)}", fontSize = 12.sp, color = GhanaNavyPrimary)
                            if (diff != 0.0) {
                                Text(
                                    text = "Delta: ${if (diff > 0) "+" else ""}GH₵ ${String.format("%.2f", diff)} / month",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (diff > 0) GhanaEmeraldGreen else MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = adjustSalaryAmountInput,
                        onValueChange = { adjustSalaryAmountInput = it },
                        label = { Text(if (isSalaryIncreaseMode) "New Higher Salary (GH₵)" else "New Lower Salary (GH₵)") },
                        singleLine = true,
                        isError = newSalParsed != null && !isSalaryConstraintValid,
                        supportingText = {
                            if (newSalParsed != null && !isSalaryConstraintValid) {
                                Text(
                                    text = if (isSalaryIncreaseMode) "Validation Rule: New salary must be strictly greater than current salary (GH₵ ${String.format("%.2f", currentSalary)})." else "Validation Rule: New salary must be strictly less than current salary (GH₵ ${String.format("%.2f", currentSalary)}).",
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 11.sp
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("adjust_salary_amount_input")
                    )

                    OutlinedTextField(
                        value = adjustSalaryReasonInput,
                        onValueChange = { adjustSalaryReasonInput = it },
                        label = { Text("Reason for Adjustment") },
                        placeholder = { Text(if (isSalaryIncreaseMode) "e.g. Promotion, Performance raise" else "e.g. Lateness deduction, Reduced load") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("adjust_salary_reason_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newSal = adjustSalaryAmountInput.toDoubleOrNull()
                        if (newSal != null) {
                            try {
                                if (isSalaryIncreaseMode) {
                                    viewModel.increaseStaffSalary(staff.id, newSal, adjustSalaryReasonInput)
                                } else {
                                    viewModel.reduceStaffSalary(staff.id, newSal, adjustSalaryReasonInput)
                                }
                                showAdjustSalaryDialog = false
                            } catch (e: Exception) {
                                android.widget.Toast.makeText(viewModel.context, e.message ?: "Invalid salary adjustment", android.widget.Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    enabled = isSalaryConstraintValid,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSalaryIncreaseMode) GhanaEmeraldGreen else MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.testTag("confirm_adjust_salary_button")
                ) {
                    Text(if (isSalaryIncreaseMode) "Increase Salary" else "Reduce Salary")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdjustSalaryDialog = false }) {
                    Text("Cancel")
                }
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

                        }
                    }
                }
            }
        }

        // --- SECTION: PROPRIETOR TOP NAVIGATION TAB ROW ---
        item {
            ScrollableTabRow(
                selectedTabIndex = activeProprietorTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.primary,
                edgePadding = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
            ) {
                Tab(
                    selected = activeProprietorTab == 0,
                    onClick = { activeProprietorTab = 0 },
                    text = { Text("Overview & Hub", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    icon = { Icon(Icons.Default.Apps, contentDescription = null) },
                    modifier = Modifier.testTag("prop_tab_overview")
                )
                Tab(
                    selected = activeProprietorTab == 1,
                    onClick = { activeProprietorTab = 1 },
                    text = { Text("Fee Balances", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    icon = { Icon(Icons.Default.Payments, contentDescription = null) },
                    modifier = Modifier.testTag("prop_tab_fees")
                )
                Tab(
                    selected = activeProprietorTab == 2,
                    onClick = { activeProprietorTab = 2 },
                    text = { Text("Staff & Roster", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    icon = { Icon(Icons.Default.People, contentDescription = null) },
                    modifier = Modifier.testTag("prop_tab_staff")
                )
                Tab(
                    selected = activeProprietorTab == 3,
                    onClick = { activeProprietorTab = 3 },
                    text = { Text("Analytics & CSV", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    icon = { Icon(Icons.Default.BarChart, contentDescription = null) },
                    modifier = Modifier.testTag("prop_tab_analytics")
                )
                Tab(
                    selected = activeProprietorTab == 4,
                    onClick = { activeProprietorTab = 4 },
                    text = { Text("Lesson Plans", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    icon = { Icon(Icons.Default.Description, contentDescription = null) },
                    modifier = Modifier.testTag("prop_tab_lesson_plans")
                )
                Tab(
                    selected = activeProprietorTab == 5,
                    onClick = { activeProprietorTab = 5 },
                    text = { Text("Master Timetable", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                    modifier = Modifier.testTag("prop_tab_timetable")
                )
                Tab(
                    selected = activeProprietorTab == 6,
                    onClick = { activeProprietorTab = 6 },
                    text = { Text("Loan Approvals", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    icon = { Icon(Icons.Default.MonetizationOn, contentDescription = null) },
                    modifier = Modifier.testTag("prop_tab_loans")
                )
                Tab(
                    selected = activeProprietorTab == 7,
                    onClick = { activeProprietorTab = 7 },
                    text = { Text("Staff Clock-In", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    icon = { Icon(Icons.Default.AccessTime, contentDescription = null) },
                    modifier = Modifier.testTag("prop_tab_clockin")
                )
                Tab(
                    selected = activeProprietorTab == 8,
                    onClick = { activeProprietorTab = 8 },
                    text = { Text("Parent Messages", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    icon = { Icon(Icons.Default.QuestionAnswer, contentDescription = null) },
                    modifier = Modifier.testTag("prop_tab_parent_messages")
                )
                Tab(
                    selected = activeProprietorTab == 9,
                    onClick = { activeProprietorTab = 9 },
                    text = { Text("Broadcast & SMS", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    icon = { Icon(Icons.Default.Campaign, contentDescription = null) },
                    modifier = Modifier.testTag("prop_tab_broadcast")
                )
                Tab(
                    selected = activeProprietorTab == 10,
                    onClick = { activeProprietorTab = 10 },
                    text = { Text("Digital Library", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    icon = { Icon(Icons.Default.MenuBook, contentDescription = null) },
                    modifier = Modifier.testTag("prop_tab_digital_library")
                )
                Tab(
                    selected = activeProprietorTab == 11,
                    onClick = { activeProprietorTab = 11 },
                    text = { Text("Assignments", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    icon = { Icon(Icons.Default.Assignment, contentDescription = null) },
                    modifier = Modifier.testTag("prop_tab_assignments")
                )
                Tab(
                    selected = activeProprietorTab == 12,
                    onClick = { activeProprietorTab = 12 },
                    text = { Text("Student Directory", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    icon = { Icon(Icons.Default.FolderShared, contentDescription = null) },
                    modifier = Modifier.testTag("prop_tab_directory")
                )
                Tab(
                    selected = activeProprietorTab == 13,
                    onClick = { activeProprietorTab = 13 },
                    text = { Text("Roles & System", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = null) },
                    modifier = Modifier.testTag("prop_tab_roles")
                )
            }
        }

        // --- SECTION 0.5: PENDING USER ACCOUNT REGISTRATION APPROVALS ---
        if (pendingUserAccounts.isNotEmpty() && (activeProprietorTab == 0 || activeProprietorTab == 2 || activeProprietorTab == 13)) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GhanaGoldAccent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pending_user_account_approvals_card")
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = GhanaNavyPrimary)
                                Column {
                                    Text(
                                        text = "Pending Account Registrations (${pendingUserAccounts.size})",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = GhanaNavyPrimary
                                    )
                                    Text(
                                        text = "Proprietor approval required before user login",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        pendingUserAccounts.forEach { pendingUser ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = pendingUser.fullName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = GhanaGoldContainer
                                        ) {
                                            Text(
                                                text = pendingUser.role,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = GhanaNavyPrimary,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Text(text = "Email: ${pendingUser.email} | Phone: ${pendingUser.phone}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    if (pendingUser.role == "TEACHER") {
                                        Text(text = "Class: ${pendingUser.assignedClass} | Subject: ${pendingUser.assignedSubject}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = GhanaNavyPrimary)
                                    }
                                    if (pendingUser.role == "GUARDIAN") {
                                        Text(text = "Linked Ward: ${pendingUser.linkedStudentChild}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = GhanaNavyPrimary)
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { viewModel.approvePendingUserAccount(pendingUser.id, pendingUser.fullName) },
                                            colors = ButtonDefaults.buttonColors(containerColor = GhanaNavyPrimary),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f).testTag("approve_user_${pendingUser.id}")
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Approve", fontSize = 11.sp)
                                        }

                                        OutlinedButton(
                                            onClick = { viewModel.rejectPendingUserAccount(pendingUser.id, pendingUser.fullName) },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f).testTag("reject_user_${pendingUser.id}")
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Reject", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- SECTION: PROPRIETOR GROUPED TASK HUB & DASHBOARD NAVIGATION ---
        if (activeProprietorTab == 0) {
            item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("proprietor_grouped_task_hub_card")
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
                            Icon(Icons.Default.Apps, contentDescription = null, tint = GhanaNavyPrimary)
                            Column {
                                Text(
                                    text = "Proprietor Portal Tasks",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = GhanaNavyPrimary
                                )
                                Text(
                                    text = "Administrative task shortcuts & portal navigation",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Button taking user to the App Dashboard
                        Button(
                            onClick = { viewModel.setViewMode(com.example.ui.viewmodel.ViewMode.HOME) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GhanaNavyPrimary),
                            modifier = Modifier.testTag("proprietor_task_go_to_dashboard")
                        ) {
                            Icon(Icons.Default.Dashboard, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("App Dashboard", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showAddStudentDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GhanaEmeraldGreen),
                            modifier = Modifier.weight(1f).testTag("proprietor_task_enroll_student")
                        ) {
                            Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Enroll Student", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { showAddStaffDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GhanaNavyPrimary),
                            modifier = Modifier.weight(1f).testTag("proprietor_task_add_staff")
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Staff", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { studentSearchQuery = "" },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("proprietor_task_search_students")
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Search DB Students", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { showMigrationDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("proprietor_task_cloud_sync")
                        ) {
                            Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cloud Sync", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showExportCsvDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GhanaGoldAccent),
                            modifier = Modifier.weight(1.3f).testTag("proprietor_task_export_csv")
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(15.dp), tint = GhanaNavyPrimary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Export CSV Reports", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GhanaNavyPrimary)
                        }

                        Button(
                            onClick = {
                                editingProprietorSlot = null
                                propSlotClassName = "JHS 2 - Gold"
                                propSlotDayOfWeek = "Monday"
                                propSlotPeriodNumber = "1"
                                propSlotStartTime = "08:00 AM"
                                propSlotEndTime = "08:45 AM"
                                propSlotSubject = "Mathematics"
                                propSlotTeacherName = "Mr. Emmanuel Mensah"
                                propSlotClassroom = "Block J2-A"
                                showProprietorSlotDialog = true
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GhanaNavyPrimary),
                            modifier = Modifier.weight(1f).testTag("proprietor_task_add_timetable")
                        ) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Schedule Slot", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

        // --- SECTION 1.5: STUDENT FEE PAYMENT & OUTSTANDING BALANCES MONITOR ---
        if (activeProprietorTab == 0 || activeProprietorTab == 1) {
            item {
            val totalAssignedFees = allStudentLedgers.sumOf { it.totalFeesGhc }
            val totalCollectedFees = allStudentLedgers.sumOf { it.paidFeesGhc }
            val totalOutstandingBalance = allStudentLedgers.sumOf { it.balanceGhc }
            val overallCollectionRate = if (totalAssignedFees > 0) (totalCollectedFees / totalAssignedFees) * 100 else 0.0

            val filteredFeeLedgers = remember(allStudentLedgers, feeSearchQuery, feeFilterStatus, feeClassFilter) {
                allStudentLedgers.filter { st ->
                    val matchesQuery = st.studentName.contains(feeSearchQuery, ignoreCase = true) ||
                            st.className.contains(feeSearchQuery, ignoreCase = true) ||
                            st.studentId.toString().contains(feeSearchQuery)
                    val matchesClass = feeClassFilter == "ALL" || st.className.contains(feeClassFilter, ignoreCase = true)
                    val matchesStatus = when (feeFilterStatus) {
                        "OVERDUE" -> st.balanceGhc > 500.0
                        "PAID" -> st.balanceGhc <= 0.0
                        else -> true
                    }
                    matchesQuery && matchesClass && matchesStatus
                }
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("fee_payments_monitor_section")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(GhanaNavyPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = GhanaGoldAccent, modifier = Modifier.size(20.dp))
                            }
                            Column {
                                Text("Fee Payments & Outstanding Balances", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("${allStudentLedgers.size} Enrolled Students • Term 3 Ledger", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Button(
                            onClick = {
                                selectedStudentForPayment = allStudentLedgers.firstOrNull()
                                showRecordPaymentDialog = true
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GhanaNavyPrimary),
                            modifier = Modifier.testTag("proprietor_record_payment_btn")
                        ) {
                            Icon(Icons.Default.AddCard, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Record Payment", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Financial Summary Indicators
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = GhanaNavyPrimary.copy(alpha = 0.08f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Assigned Fees", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                                Text("GH₵ ${String.format("%.0f", totalAssignedFees)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GhanaNavyPrimary)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFE8F5E9),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Collected", fontSize = 10.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.SemiBold)
                                Text("GH₵ ${String.format("%.0f", totalCollectedFees)}", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2E7D32))
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFFF3E0),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Outstanding", fontSize = 10.sp, color = Color(0xFFE65100), fontWeight = FontWeight.SemiBold)
                                Text("GH₵ ${String.format("%.0f", totalOutstandingBalance)}", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFD84315))
                            }
                        }
                    }

                    // Progress Bar
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Term Fee Recovery Rate", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("${String.format("%.1f", overallCollectionRate)}%", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = GhanaNavyPrimary)
                        }
                        LinearProgressIndicator(
                            progress = { (overallCollectionRate / 100.0).toFloat().coerceIn(0f, 1f) },
                            color = GhanaNavyPrimary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))

                    // Search & Filters
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = feeSearchQuery,
                            onValueChange = { feeSearchQuery = it },
                            placeholder = { Text("Search student or class...", fontSize = 11.sp) },
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(48.dp).testTag("fee_search_input")
                        )

                        listOf("ALL", "OVERDUE", "PAID").forEach { status ->
                            FilterChip(
                                selected = feeFilterStatus == status,
                                onClick = { feeFilterStatus = status },
                                label = { Text(status, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GhanaNavyPrimary,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.height(32.dp).testTag("fee_filter_$status")
                            )
                        }
                    }

                    // Student Fee Balances List Roster
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        filteredFeeLedgers.take(6).forEach { st ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(if (st.balanceGhc > 500) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (st.balanceGhc > 0) Icons.Default.MoneyOff else Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = if (st.balanceGhc > 500) Color(0xFFC62828) else Color(0xFF2E7D32),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Text(st.studentName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = if (st.balanceGhc > 500) Color(0xFFFFCDD2) else if (st.balanceGhc > 0) Color(0xFFFFE0B2) else Color(0xFFC8E6C9)
                                                ) {
                                                    Text(
                                                        text = if (st.balanceGhc > 500) "OVERDUE" else if (st.balanceGhc > 0) "PARTIAL" else "SETTLED",
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = if (st.balanceGhc > 500) Color(0xFFB71C1C) else if (st.balanceGhc > 0) Color(0xFFE65100) else Color(0xFF1B5E20),
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = "${st.className} • Total: GH₵ ${st.totalFeesGhc.toInt()} • Paid: GH₵ ${st.paidFeesGhc.toInt()}",
                                                fontSize = 10.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "GH₵ ${String.format("%.2f", st.balanceGhc)}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (st.balanceGhc > 0) Color(0xFFD84315) else Color(0xFF2E7D32)
                                            )
                                            Text("Balance", fontSize = 9.sp, color = Color.Gray)
                                        }

                                        IconButton(
                                            onClick = {
                                                selectedStudentForPayment = st
                                                recordAmountInput = if (st.balanceGhc > 0) st.balanceGhc.toString() else "500.0"
                                                showRecordPaymentDialog = true
                                            },
                                            modifier = Modifier.size(30.dp).testTag("record_payment_for_${st.studentId}")
                                        ) {
                                            Icon(Icons.Default.AddCard, contentDescription = "Record Payment", tint = GhanaNavyPrimary, modifier = Modifier.size(16.dp))
                                        }

                                        if (st.balanceGhc > 0) {
                                            IconButton(
                                                onClick = {
                                                    viewModel.sendFeeReminderSms(st.studentName, st.guardianPhone, st.balanceGhc)
                                                },
                                                modifier = Modifier.size(30.dp).testTag("send_fee_sms_${st.studentId}")
                                            ) {
                                                Icon(Icons.Default.Sms, contentDescription = "Send Fee Reminder SMS", tint = GhanaGoldAccent, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Fee Payment Audit Log
                    if (allFeePayments.isNotEmpty()) {
                        Text("Recent Fee Receipts Audit Trail:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = GhanaNavyPrimary)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            allFeePayments.take(3).forEach { fp ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("${fp.receiptNumber} • ${fp.studentName} (${fp.feeCategory})", fontSize = 10.sp, color = Color.DarkGray)
                                    Text("GH₵ ${String.format("%.2f", fp.amountPaidGhc)} (${fp.paymentMethod})", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

        // --- SECTION 1: DATA VISUALIZATIONS & ANALYTICS ---
        if (activeProprietorTab == 0 || activeProprietorTab == 3) {
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

                // Chart 2: Student Term Performance Bar Chart
                com.example.ui.components.StudentTermPerformanceBarChart(
                    grades = allGrades
                )

                // Chart 3: Enrollment Growth Line Chart
                com.example.ui.components.EnrollmentTrendLineChart(
                    trendPoints = enrollmentTrendPoints
                )
            }
        }
    }

        // --- SECTION 2: ROLE DELEGATION MATRIX LAUNCHER ---
        if (activeProprietorTab == 0 || activeProprietorTab == 13) {
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
    }

        // --- SECTION: PROPRIETOR STAFF & STUDENT ROSTER MANAGEMENT ---
        if (activeProprietorTab == 0 || activeProprietorTab == 2) {
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
                    Text("Staff Payroll & Roster Management:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        staffList.forEach { staff ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    when (staff.paymentStatus) {
                                        "WITHHELD" -> MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                                        "PAID" -> GhanaEmeraldGreen.copy(alpha = 0.5f)
                                        else -> GhanaGoldAccent.copy(alpha = 0.5f)
                                    }
                                ),
                                modifier = Modifier.fillMaxWidth().testTag("staff_roster_card_${staff.id}")
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
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
                                                    .size(38.dp)
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

                                        // Payment Status Badge
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = when (staff.paymentStatus) {
                                                "PAID" -> GhanaEmeraldGreen.copy(alpha = 0.15f)
                                                "WITHHELD" -> MaterialTheme.colorScheme.errorContainer
                                                else -> GhanaGoldAccent.copy(alpha = 0.15f)
                                            }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = when (staff.paymentStatus) {
                                                        "PAID" -> Icons.Default.CheckCircle
                                                        "WITHHELD" -> Icons.Default.Block
                                                        else -> Icons.Default.Schedule
                                                    },
                                                    contentDescription = null,
                                                    modifier = Modifier.size(12.dp),
                                                    tint = when (staff.paymentStatus) {
                                                        "PAID" -> GhanaEmeraldGreen
                                                        "WITHHELD" -> MaterialTheme.colorScheme.error
                                                        else -> GhanaNavyPrimary
                                                    }
                                                )
                                                Text(
                                                    text = when (staff.paymentStatus) {
                                                        "PAID" -> "PAID"
                                                        "WITHHELD" -> "WITHHELD"
                                                        else -> "DUE"
                                                    },
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = when (staff.paymentStatus) {
                                                        "PAID" -> GhanaEmeraldGreen
                                                        "WITHHELD" -> MaterialTheme.colorScheme.error
                                                        else -> GhanaNavyPrimary
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Monthly Salary: GH₵ ${String.format("%.2f", staff.monthlySalaryGhc)}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = GhanaNavyPrimary
                                        )

                                        if (staff.lastPaymentDate.isNotBlank() && staff.paymentStatus == "PAID") {
                                            Text(
                                                text = "Paid: ${staff.lastPaymentDate}",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    if (staff.paymentStatus == "WITHHELD" && staff.withheldReason.isNotBlank()) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "Hold Reason: ${staff.withheldReason}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onErrorContainer,
                                                fontWeight = FontWeight.Medium,
                                                modifier = Modifier.padding(6.dp)
                                            )
                                        }
                                    }

                                    // Action Buttons: Pay, Withhold / Release Hold, Adjust (Increase/Reduce), Drop
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Button(
                                            onClick = {
                                                staffToPay = staff
                                                paySalaryAmountInput = staff.monthlySalaryGhc.toString()
                                                paySalaryMethodInput = "MTN MoMo"
                                                paySalaryNotesInput = ""
                                                showPaySalaryDialog = true
                                            },
                                            enabled = staff.paymentStatus != "WITHHELD",
                                            colors = ButtonDefaults.buttonColors(containerColor = GhanaNavyPrimary),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            modifier = Modifier.weight(1f).testTag("pay_staff_button_${staff.id}")
                                        ) {
                                            Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text("Pay", fontSize = 11.sp)
                                        }

                                        if (staff.paymentStatus == "WITHHELD") {
                                            Button(
                                                onClick = {
                                                    viewModel.releaseStaffPaymentHold(staff.id)
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = GhanaEmeraldGreen),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                modifier = Modifier.weight(1.2f).testTag("release_hold_staff_button_${staff.id}")
                                            ) {
                                                Icon(Icons.Default.LockReset, contentDescription = null, modifier = Modifier.size(13.dp))
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text("Release Hold", fontSize = 10.sp)
                                            }
                                        } else {
                                            OutlinedButton(
                                                onClick = {
                                                    staffToWithhold = staff
                                                    withholdReasonInput = ""
                                                    showWithholdSalaryDialog = true
                                                },
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                modifier = Modifier.weight(1f).testTag("withhold_staff_button_${staff.id}")
                                            ) {
                                                Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(13.dp))
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text("Withhold", fontSize = 11.sp)
                                            }
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                staffToAdjust = staff
                                                isSalaryIncreaseMode = true
                                                adjustSalaryAmountInput = staff.monthlySalaryGhc.toString()
                                                adjustSalaryReasonInput = ""
                                                showAdjustSalaryDialog = true
                                            },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = GhanaNavyPrimary),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            modifier = Modifier.weight(1f).testTag("adjust_salary_button_${staff.id}")
                                        ) {
                                            Icon(Icons.Default.TrendingUp, contentDescription = null, modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text("Adjust", fontSize = 11.sp)
                                        }

                                        IconButton(
                                            onClick = {
                                                staffToDrop = staff
                                                showDropStaffDialog = true
                                            },
                                            modifier = Modifier.size(32.dp).testTag("drop_staff_button_${staff.id}")
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Drop Staff", tint = Color(0xFFC62828), modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Student Roll (Room DB Roster):", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = GhanaNavyPrimary.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "${filteredStudentLedgers.size} of ${allStudentLedgers.size} Students",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GhanaNavyPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // --- SEARCH BAR UI COMPONENT FOR ROOM DATABASE STUDENTS ---
                    OutlinedTextField(
                        value = studentSearchQuery,
                        onValueChange = { studentSearchQuery = it },
                        placeholder = { Text("Search students by name, ID or class...", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = GhanaNavyPrimary)
                        },
                        trailingIcon = {
                            if (studentSearchQuery.isNotEmpty()) {
                                IconButton(onClick = { studentSearchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear Search", tint = Color.Gray)
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GhanaNavyPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("proprietor_student_search_bar")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (filteredStudentLedgers.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.PersonSearch,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "No students found matching \"$studentSearchQuery\"",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            filteredStudentLedgers.forEach { st ->
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
        }
    }

        // --- SECTION: ACADEMIC PERFORMANCE & GRADE ANALYTICS (PROPRIETOR VIEW) ---
        if (activeProprietorTab == 0 || activeProprietorTab == 3) {
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
    }

        // --- SECTION 2: TRANSACTION APPROVAL QUEUE ---
        if (activeProprietorTab == 0 || activeProprietorTab == 1 || activeProprietorTab == 13) {
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
            itemsIndexed(filteredApprovals, key = { index, approval -> "approval_${approval.id}_$index" }) { _, approval ->
                ApprovalQueueCard(
                    approval = approval,
                    onApprove = { viewModel.approveTransaction(approval.id) },
                    onReject = { viewModel.rejectTransaction(approval.id) }
                )
            }
        }
    }

        // --- SECTION: DAILY LESSON PLANS MANAGEMENT REVIEW ---
        if (activeProprietorTab == 0 || activeProprietorTab == 4) {
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
            itemsIndexed(allLessonPlans, key = { index, plan -> "plan_${plan.id}_$index" }) { _, plan ->
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
    }

        // --- SECTION: SCHOOL-WIDE MASTER TIMETABLE & CLASS SCHEDULES ---
        if (activeProprietorTab == 0 || activeProprietorTab == 5) {
            item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("proprietor_master_timetable_card")
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
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = GhanaNavyPrimary)
                            Column {
                                Text(
                                    text = "School-Wide Master Timetable",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = GhanaNavyPrimary
                                )
                                Text(
                                    text = "Weekly class schedules, period timings & teacher allocations",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = {
                                editingProprietorSlot = null
                                propSlotClassName = if (proprietorTimetableClassFilter == "ALL") "JHS 2 - Gold" else proprietorTimetableClassFilter
                                propSlotDayOfWeek = if (proprietorTimetableDayFilter == "ALL") "Monday" else proprietorTimetableDayFilter
                                propSlotPeriodNumber = "1"
                                propSlotStartTime = "08:00 AM"
                                propSlotEndTime = "08:45 AM"
                                propSlotSubject = "Mathematics"
                                propSlotTeacherName = "Mr. Emmanuel Mensah"
                                propSlotClassroom = "Block J2-A"
                                showProprietorSlotDialog = true
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GhanaNavyPrimary),
                            modifier = Modifier.testTag("proprietor_add_schedule_slot_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Slot", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Key Overview Badges
                    val totalSlots = allTimetables.size
                    val activeClasses = allTimetables.map { it.className }.distinct()
                    val assignedTeachers = allTimetables.map { it.teacherName }.distinct()
                    val totalSubjects = allTimetables.map { it.subject }.distinct()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = GhanaNavyPrimary.copy(alpha = 0.08f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("$totalSlots", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = GhanaNavyPrimary)
                                Text("Total Slots", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = GhanaEmeraldGreen.copy(alpha = 0.1f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${activeClasses.size}", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = GhanaEmeraldGreen)
                                Text("Classes", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = GhanaGoldAccent.copy(alpha = 0.2f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${assignedTeachers.size}", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = GhanaNavyPrimary)
                                Text("Teachers", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${totalSubjects.size}", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                                Text("Subjects", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    // Filters & Search
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Class Filter
                        Text("Filter Class:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = GhanaNavyPrimary)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val availableClasses = listOf("ALL", "JHS 2 - Gold", "JHS 1", "Primary 6", "Primary 4 - Harmony")
                            items(availableClasses) { clsName ->
                                FilterChip(
                                    selected = proprietorTimetableClassFilter == clsName,
                                    onClick = { proprietorTimetableClassFilter = clsName },
                                    label = { Text(clsName, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = GhanaNavyPrimary,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.height(30.dp).testTag("prop_filter_class_$clsName")
                                )
                            }
                        }

                        // Day Filter
                        Text("Filter Day of Week:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = GhanaNavyPrimary)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val daysList = listOf("ALL", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday")
                            items(daysList) { day ->
                                FilterChip(
                                    selected = proprietorTimetableDayFilter == day,
                                    onClick = { proprietorTimetableDayFilter = day },
                                    label = { Text(day, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = GhanaGoldAccent,
                                        selectedLabelColor = GhanaNavyPrimary
                                    ),
                                    modifier = Modifier.height(30.dp).testTag("prop_filter_day_$day")
                                )
                            }
                        }

                        // Search Field
                        OutlinedTextField(
                            value = proprietorTimetableSearchQuery,
                            onValueChange = { proprietorTimetableSearchQuery = it },
                            placeholder = { Text("Search subject, teacher, classroom...", fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            trailingIcon = {
                                if (proprietorTimetableSearchQuery.isNotEmpty()) {
                                    IconButton(onClick = { proprietorTimetableSearchQuery = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                }
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("prop_timetable_search_field")
                        )
                    }

                    // Slots Listing
                    val filteredPropSlots = remember(
                        allTimetables,
                        proprietorTimetableClassFilter,
                        proprietorTimetableDayFilter,
                        proprietorTimetableSearchQuery
                    ) {
                        allTimetables.filter { slot ->
                            val matchesClass = proprietorTimetableClassFilter == "ALL" || slot.className.equals(proprietorTimetableClassFilter, ignoreCase = true) || slot.className.contains(proprietorTimetableClassFilter, ignoreCase = true)
                            val matchesDay = proprietorTimetableDayFilter == "ALL" || slot.dayOfWeek.equals(proprietorTimetableDayFilter, ignoreCase = true)
                            val matchesSearch = proprietorTimetableSearchQuery.isBlank() ||
                                    slot.subject.contains(proprietorTimetableSearchQuery, ignoreCase = true) ||
                                    slot.teacherName.contains(proprietorTimetableSearchQuery, ignoreCase = true) ||
                                    slot.classroom.contains(proprietorTimetableSearchQuery, ignoreCase = true) ||
                                    slot.className.contains(proprietorTimetableSearchQuery, ignoreCase = true)
                            matchesClass && matchesDay && matchesSearch
                        }.sortedWith(compareBy({
                            when (it.dayOfWeek) {
                                "Monday" -> 1
                                "Tuesday" -> 2
                                "Wednesday" -> 3
                                "Thursday" -> 4
                                "Friday" -> 5
                                else -> 6
                            }
                        }, { it.periodNumber }))
                    }

                    if (filteredPropSlots.isEmpty()) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.EventBusy, contentDescription = null, modifier = Modifier.size(32.dp), tint = Color.Gray)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("No timetable schedule slots found matching filters", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Try adjusting class/day filters or click 'Add Slot' to create a new period.", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            filteredPropSlots.forEach { slot ->
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("prop_timetable_slot_${slot.id}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = GhanaNavyPrimary
                                                ) {
                                                    Text(
                                                        text = "${slot.dayOfWeek} • Period ${slot.periodNumber}",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = GhanaGoldAccent.copy(alpha = 0.25f)
                                                ) {
                                                    Text(
                                                        text = slot.className,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = GhanaNavyPrimary,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                                Text(
                                                    text = "${slot.startTime} - ${slot.endTime}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(6.dp))

                                            Text(
                                                text = slot.subject,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )

                                            Spacer(modifier = Modifier.height(2.dp))

                                            Text(
                                                text = "👨‍🏫 ${slot.teacherName}  •  🏫 ${slot.classroom}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Row {
                                            IconButton(
                                                onClick = {
                                                    editingProprietorSlot = slot
                                                    propSlotClassName = slot.className
                                                    propSlotDayOfWeek = slot.dayOfWeek
                                                    propSlotPeriodNumber = slot.periodNumber.toString()
                                                    propSlotStartTime = slot.startTime
                                                    propSlotEndTime = slot.endTime
                                                    propSlotSubject = slot.subject
                                                    propSlotTeacherName = slot.teacherName
                                                    propSlotClassroom = slot.classroom
                                                    showProprietorSlotDialog = true
                                                },
                                                modifier = Modifier.size(32.dp).testTag("prop_edit_slot_${slot.id}")
                                            ) {
                                                Icon(Icons.Default.Edit, contentDescription = "Edit Slot", modifier = Modifier.size(16.dp), tint = GhanaNavyPrimary)
                                            }
                                            IconButton(
                                                onClick = { viewModel.deleteTimetableSlot(slot) },
                                                modifier = Modifier.size(32.dp).testTag("prop_delete_slot_${slot.id}")
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete Slot", modifier = Modifier.size(16.dp), tint = Color.Red)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

        // --- SECTION 3: TEACHER SALARY LOAN REQUEST APPROVAL QUEUE ---
        if (activeProprietorTab == 0 || activeProprietorTab == 6) {
            item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("proprietor_loan_approval_card")
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(GhanaNavyPrimary.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = GhanaNavyPrimary)
                        }
                        Column {
                            Text("Teacher Salary Loan Approvals", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = GhanaNavyPrimary)
                            Text("Review, approve or decline staff salary advance requests", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    if (allTeacherLoanRequests.isEmpty()) {
                        Text("No staff salary loan requests pending approval.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            allTeacherLoanRequests.forEach { req ->
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth().testTag("loan_req_approval_item_${req.id}")
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(req.teacherName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                Text("Requested: GH₵ ${String.format("%.2f", req.amountGhc)}  •  Duration: ${req.repaymentDurationMonths} Months", fontSize = 12.sp, color = GhanaNavyPrimary, fontWeight = FontWeight.SemiBold)
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = when (req.status) {
                                                    "APPROVED" -> Color(0xFFD1E7DD)
                                                    "REJECTED" -> Color(0xFFF8D7DA)
                                                    else -> Color(0xFFFFF3CD)
                                                }
                                            ) {
                                                Text(
                                                    text = req.status,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = when (req.status) {
                                                        "APPROVED" -> Color(0xFF0F5132)
                                                        "REJECTED" -> Color(0xFF842029)
                                                        else -> Color(0xFF664D03)
                                                    },
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Terms: ${req.repaymentTerms}", fontSize = 11.sp)
                                        Text("Reason: ${req.reason}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                        if (req.status == "PENDING") {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Button(
                                                    onClick = {
                                                        viewModel.approveTeacherLoanRequest(req.id, "Approved by Proprietor")
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = GhanaEmeraldGreen),
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.weight(1f).testTag("approve_loan_button_${req.id}")
                                                ) {
                                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Approve Loan", fontSize = 11.sp)
                                                }

                                                OutlinedButton(
                                                    onClick = {
                                                        viewModel.rejectTeacherLoanRequest(req.id, "Declined due to budget constraints")
                                                    },
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.weight(1f).testTag("reject_loan_button_${req.id}")
                                                ) {
                                                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Red)
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Decline", fontSize = 11.sp, color = Color.Red)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

        // --- SECTION 4: PROPRIETOR STAFF CLOCK-IN & LATENESS TRACKING DASHBOARD ---
        if (activeProprietorTab == 0 || activeProprietorTab == 7) {
            item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("proprietor_clockin_tracking_card")
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
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
                                    .size(40.dp)
                                    .background(GhanaNavyPrimary.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Schedule, contentDescription = null, tint = GhanaNavyPrimary)
                            }
                            Column {
                                Text("Staff Clock-In & Lateness Tracking", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = GhanaNavyPrimary)
                                Text("Official Morning Threshold: ${schoolSettings?.officialStartTime ?: "08:00 AM"}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = GhanaNavyPrimary.copy(alpha = 0.1f)
                        ) {
                            Text(
                                "Threshold: ${schoolSettings?.officialStartTime ?: "08:00 AM"}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GhanaNavyPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    if (staffClockIns.isEmpty()) {
                        Text("No staff clock-in logs recorded today.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            staffClockIns.forEach { clockIn ->
                                Card(
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                    modifier = Modifier.fillMaxWidth().testTag("clockin_log_${clockIn.id}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(clockIn.teacherName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text("Timestamp: ${clockIn.timestampString}  •  ${clockIn.actionType}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (clockIn.isWithinGeofence) Color(0xFFD1E7DD) else Color(0xFFFFF3CD)
                                        ) {
                                            Text(
                                                text = if (clockIn.isWithinGeofence) "✅ GEOFENCE VERIFIED" else "📍 REMOTE LOG",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (clockIn.isWithinGeofence) Color(0xFF0F5132) else Color(0xFF664D03),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

        // --- SECTION 5: GUARDIAN-TEACHER COMMUNICATIONS MONITOR (READ-ONLY OVERSIGHT) ---
        if (activeProprietorTab == 0 || activeProprietorTab == 8) {
            item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("proprietor_messages_oversight_card")
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
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
                                    .size(40.dp)
                                    .background(GhanaNavyPrimary.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Visibility, contentDescription = null, tint = GhanaNavyPrimary)
                            }
                            Column {
                                Text("Guardian-Teacher Communications", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = GhanaNavyPrimary)
                                Text("Read-Only proprietor oversight & monitoring portal", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Surface(shape = RoundedCornerShape(6.dp), color = GhanaGoldAccent) {
                            Text("READ-ONLY", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = GhanaNavyPrimary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    if (allDirectMessages.isEmpty()) {
                        Text("No direct messages exchanged yet.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            allDirectMessages.take(5).forEach { msg ->
                                Card(
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("${msg.senderName} ➔ ${msg.recipientName}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Text(msg.timestampString, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Text("Re Student: ${msg.childName}  |  Subject: ${msg.subject}", fontSize = 10.sp, color = GhanaNavyPrimary, fontWeight = FontWeight.SemiBold)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(msg.messageBody, fontSize = 11.sp, maxLines = 2, color = MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

        // --- SECTION 6: PROPRIETOR BROADCAST NOTIFICATION SYSTEM ---
        if (activeProprietorTab == 0 || activeProprietorTab == 9) {
            item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("proprietor_broadcast_card")
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(GhanaGoldAccent, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Campaign, contentDescription = null, tint = GhanaNavyPrimary)
                        }
                        Column {
                            Text("Proprietor School-Wide Broadcast", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = GhanaNavyPrimary)
                            Text("Dispatch urgent alerts & announcements to staff and guardians", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("ALL" to "All Community", "TEACHERS" to "Teachers Only", "GUARDIANS" to "Guardians Only").forEach { (aud, label) ->
                            FilterChip(
                                selected = broadcastAudienceInput == aud,
                                onClick = { broadcastAudienceInput = aud },
                                label = { Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                modifier = Modifier.height(28.dp).testTag("broadcast_aud_$aud")
                            )
                        }
                    }

                    OutlinedTextField(
                        value = broadcastTitleInput,
                        onValueChange = { broadcastTitleInput = it },
                        label = { Text("Announcement Title / Subject") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("broadcast_title_field")
                    )

                    OutlinedTextField(
                        value = broadcastBodyInput,
                        onValueChange = { broadcastBodyInput = it },
                        label = { Text("Announcement Message Body") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth().testTag("broadcast_body_field")
                    )

                    Button(
                        onClick = {
                            if (broadcastTitleInput.isNotBlank() && broadcastBodyInput.isNotBlank()) {
                                viewModel.dispatchProprietorBroadcast(
                                    title = broadcastTitleInput,
                                    messageText = broadcastBodyInput,
                                    targetRole = broadcastAudienceInput
                                )
                                broadcastTitleInput = ""
                                broadcastBodyInput = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GhanaNavyPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("dispatch_broadcast_button")
                    ) {
                        Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Dispatch School-Wide Broadcast", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- SECTION 7: SMS / WHATSAPP MESSAGING PANEL ---
        item {
            MessagingPanel(viewModel = viewModel)
        }
    }

        // --- SECTION 8: DIGITAL LIBRARY & SCHOOL HISTORY REPOSITORY MANAGER ---
        if (activeProprietorTab == 0 || activeProprietorTab == 10) {
            item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("proprietor_digital_library_card")
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
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
                                    .size(40.dp)
                                    .background(GhanaNavyPrimary.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.MenuBook, contentDescription = null, tint = GhanaNavyPrimary)
                            }
                            Column {
                                Text("Digital Library & History Repository", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = GhanaNavyPrimary)
                                Text("Manage textbooks, syllabi, history docs & videos", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        FilledIconButton(
                            onClick = { showUploadResourceDialog = true },
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = GhanaNavyPrimary),
                            modifier = Modifier.size(40.dp).testTag("open_upload_resource_plus_button")
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Upload Digital Resource",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    // Category Filter Chips
                    val categories = listOf("ALL", "TEXTBOOK", "SYLLABUS", "CURRICULUM", "SCHOOL_HISTORY", "PROMOTIONAL_VIDEO")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(categories) { cat ->
                            FilterChip(
                                selected = libraryCategoryFilter == cat,
                                onClick = { libraryCategoryFilter = cat },
                                label = { Text(cat.replace("_", " "), fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GhanaNavyPrimary,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag("prop_lib_filter_${cat}")
                            )
                        }
                    }

                    val filteredResources = allDigitalResources.filter { res ->
                        (libraryCategoryFilter == "ALL" || res.category == libraryCategoryFilter) &&
                        (librarySearchQuery.isBlank() || res.title.contains(librarySearchQuery, ignoreCase = true) || res.subject.contains(librarySearchQuery, ignoreCase = true))
                    }

                    if (filteredResources.isEmpty()) {
                        Text("No digital resources match the selected filter.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            filteredResources.forEach { res ->
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                    modifier = Modifier.fillMaxWidth().testTag("digital_res_item_${res.id}")
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = when (res.fileFormat) {
                                                        "PDF" -> Color(0xFFF8D7DA)
                                                        "EPUB" -> Color(0xFFD1E7DD)
                                                        "MP4" -> Color(0xFFCFE2FF)
                                                        else -> Color(0xFFFFF3CD)
                                                    }
                                                ) {
                                                    Text(
                                                        res.fileFormat,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = when (res.fileFormat) {
                                                            "PDF" -> Color(0xFF842029)
                                                            "EPUB" -> Color(0xFF0F5132)
                                                            "MP4" -> Color(0xFF084298)
                                                            else -> Color(0xFF664D03)
                                                        },
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                                Column {
                                                    Text(res.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                    Text("By: ${res.authorOrPublisher} • ${res.uploadDateString}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            }

                                            IconButton(
                                                onClick = { viewModel.deleteDigitalResource(res.id) },
                                                modifier = Modifier.size(28.dp).testTag("delete_digital_res_${res.id}")
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete Resource", tint = Color.Red, modifier = Modifier.size(16.dp))
                                            }
                                        }

                                        if (res.description.isNotBlank()) {
                                            Text(res.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                                        }

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            BadgeChip(text = "Category: ${res.category.replace("_", " ")}")
                                            BadgeChip(text = "Class: ${res.targetClass}")
                                            BadgeChip(text = "Subject: ${res.subject}")
                                        }

                                        Text("File Storage Path: ${res.fileUrlOrPath} (${res.fileSizeBytes / 1024 / 1024} MB)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

        // --- SECTION 9: ASSIGNMENT ACTIVITY & TEACHER COMPLIANCE MONITORING DASHBOARD ---
        if (activeProprietorTab == 0 || activeProprietorTab == 11) {
            item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("proprietor_assignment_monitoring_card")
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(GhanaNavyPrimary.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Analytics, contentDescription = null, tint = GhanaNavyPrimary)
                        }
                        Column {
                            Text("Assignment Tracking & Teacher Monitoring", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = GhanaNavyPrimary)
                            Text("Monitor daily, weekly, and termly assignment compliance", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    // Frequency Period Filters
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("Frequency Period:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        listOf("ALL", "DAILY", "WEEKLY", "TERMLY").forEach { period ->
                            FilterChip(
                                selected = assignmentPeriodFilter == period,
                                onClick = { assignmentPeriodFilter = period },
                                label = { Text(period, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GhanaNavyPrimary,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag("assignment_period_filter_${period}")
                            )
                        }
                    }

                    val filteredAssignments = allClassAssignments.filter { ass ->
                        assignmentPeriodFilter == "ALL" || ass.frequencyPeriod == assignmentPeriodFilter
                    }

                    // Analytics Stat Cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val dailyCount = allClassAssignments.count { it.frequencyPeriod == "DAILY" }
                        val weeklyCount = allClassAssignments.count { it.frequencyPeriod == "WEEKLY" }
                        val termlyCount = allClassAssignments.count { it.frequencyPeriod == "TERMLY" }

                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = GhanaNavyPrimary.copy(alpha = 0.08f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(dailyCount.toString(), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = GhanaNavyPrimary)
                                Text("Daily Drills", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = GhanaEmeraldGreen.copy(alpha = 0.12f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(weeklyCount.toString(), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = GhanaEmeraldGreen)
                                Text("Weekly Homework", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = GhanaGoldAccent.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(termlyCount.toString(), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = GhanaNavyPrimary)
                                Text("Termly Projects", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    // Teacher Compliance Summary
                    Text("Teacher Assignment Posting Compliance:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = GhanaNavyPrimary)
                    val teacherList = listOf("Mr. Kojo Mensah", "Mrs. Abena Osei", "Miss Akosua Addo")
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        teacherList.forEach { teacher ->
                            val tAssignments = filteredAssignments.filter { it.teacherName == teacher }
                            val count = tAssignments.size
                            val isCompliant = count > 0

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(teacher, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("${count} assignments posted in period (${assignmentPeriodFilter})", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isCompliant) Color(0xFFD1E7DD) else Color(0xFFFFF3CD)
                                    ) {
                                        Text(
                                            text = if (isCompliant) "✅ ACTIVE ASSIGNER" else "⚠️ ATTENTION NEEDED",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isCompliant) Color(0xFF0F5132) else Color(0xFF664D03),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Detailed Assignment Feed
                    Text("School Assignment Log Feed:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = GhanaNavyPrimary)
                    if (filteredAssignments.isEmpty()) {
                        Text("No assignments recorded for this period filter.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            filteredAssignments.forEach { ass ->
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                    modifier = Modifier.fillMaxWidth().testTag("prop_assignment_item_${ass.id}")
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(ass.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Text("Teacher: ${ass.teacherName} • Class: ${ass.className} (${ass.subject})", fontSize = 11.sp, color = GhanaNavyPrimary, fontWeight = FontWeight.SemiBold)
                                            }
                                            IconButton(
                                                onClick = { viewModel.deleteClassAssignment(ass.id) },
                                                modifier = Modifier.size(28.dp).testTag("delete_assignment_${ass.id}")
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(16.dp))
                                            }
                                        }

                                        Text(ass.description, fontSize = 11.sp)
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            BadgeChip(text = "Period: ${ass.frequencyPeriod}")
                                            BadgeChip(text = "Due: ${ass.dueDateString}")
                                            BadgeChip(text = "Max Score: ${ass.maxScore} pts")
                                        }
                                        Text("Attachment: ${ass.attachmentPathOrUrl}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

        // --- SECTION 10: STUDENT PROFILES DIRECTORY & FIRESTORE DATA COMPONENT ---
        if (activeProprietorTab == 0 || activeProprietorTab == 12) {
            item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("proprietor_student_profiles_card")
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
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(GhanaNavyPrimary.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Badge, contentDescription = null, tint = GhanaNavyPrimary)
                            }
                            Column {
                                Text("Student Profiles Directory & Firestore Engine", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = GhanaNavyPrimary)
                                Text("Academic records, guardian contacts & Cloud Firestore schema sync", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = GhanaEmeraldGreen.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "${allStudentProfiles.size} Students Enrolled",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F5132),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    StudentListViewWithSearch(
                        studentList = allStudentProfiles,
                        onContactGuardianPhone = { phone ->
                            viewModel.triggerPortalDataRefresh("Initiating call to guardian: $phone")
                        },
                        onContactGuardianEmail = { email ->
                            viewModel.triggerPortalDataRefresh("Opening email compose for guardian: $email")
                        },
                        onSyncToFirestore = { student ->
                            viewModel.triggerPortalDataRefresh("Syncing '${student.fullName}' profile to Cloud Firestore...")
                        }
                    )
                }
            }
        }
    }
}

    // Modal Student Profile Dialog if clicked
    selectedStudentProfileForModal?.let { profile ->
        StudentProfileDialog(
            student = profile,
            onDismiss = { selectedStudentProfileForModal = null },
            onContactGuardianPhone = { phone ->
                viewModel.triggerPortalDataRefresh("Initiating call to guardian: $phone")
            },
            onSyncToFirestore = { student ->
                viewModel.triggerPortalDataRefresh("Syncing '${student.fullName}' profile to Cloud Firestore...")
            }
        )
    }

    // --- DIALOG: UPLOAD DIGITAL RESOURCE ---
    if (showUploadResourceDialog) {
        UploadResourceDialog(
            userRole = "Proprietor",
            onDismiss = { showUploadResourceDialog = false },
            onUpload = { title, author, category, type, targetClass, subject, audience, desc, format, urlOrPath, role ->
                viewModel.uploadDigitalResource(
                    title = title,
                    authorOrPublisher = author,
                    category = category,
                    resourceType = type,
                    targetClass = targetClass,
                    subject = subject,
                    targetAudience = audience,
                    description = desc,
                    fileFormat = format,
                    fileUrlOrPath = urlOrPath,
                    uploadedBy = role
                )
            }
        )
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
