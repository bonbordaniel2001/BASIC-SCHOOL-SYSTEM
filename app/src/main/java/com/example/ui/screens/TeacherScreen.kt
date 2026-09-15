package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceRecord
import com.example.data.model.ClassAssignment
import com.example.data.model.ClassTimetable
import com.example.data.model.ClockInLog
import com.example.data.model.DailyStudentAttendance
import com.example.data.model.DigitalResource
import com.example.data.model.LessonPlan
import com.example.data.model.StudentGrade
import com.example.ui.components.ClassGpaTrendOverviewCard
import com.example.ui.components.GpaSparklineCanvas
import com.example.ui.components.GpaTrendBadge
import com.example.ui.components.LoadingOverlay
import com.example.ui.components.LoadingSpinner
import com.example.ui.components.UploadResourceDialog
import com.example.ui.theme.GhanaEmeraldGreen
import com.example.ui.theme.GhanaGoldAccent
import com.example.ui.theme.GhanaGreenContainer
import com.example.ui.theme.GhanaNavyPrimary
import com.example.ui.viewmodel.SchoolViewModel
import com.example.ui.viewmodel.SimulatedGeofenceState
import com.example.util.GpaCalculator

data class StudentMatrixData(
    val studentId: Long,
    val name: String,
    val className: String,
    val term1Avg: Double?,
    val term2Avg: Double?,
    val term3Avg: Double?,
    val overallCumulativeAvg: Double,
    val totalSubjectEntries: Int,
    val topGradeLetter: String,
    val cumulativeGpa: Double = 0.0,
    val gpaTrend: GpaCalculator.StudentGpaTrend? = null
)

@Composable
fun TeacherScreen(
    viewModel: SchoolViewModel,
    modifier: Modifier = Modifier
) {
    val selectedClass by viewModel.selectedClass.collectAsState()
    val attendanceList by viewModel.attendanceList.collectAsState()
    val clockInLogs by viewModel.allClockInLogs.collectAsState()
    val simState by viewModel.simulatedLocationState.collectAsState()

    val isOnline by viewModel.isOnline.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val unsyncedCount by viewModel.unsyncedCount.collectAsState()

    val currentDistance = viewModel.currentDistanceMeters
    val isWithinGeofence = viewModel.isWithinGeofence

    // Compute weekly class attendance statistics
    val totalPossibleDays = attendanceList.size * 5
    val totalAbsences = attendanceList.sumOf { record ->
        listOf(record.mondayStatus, record.tuesdayStatus, record.wednesdayStatus, record.thursdayStatus, record.fridayStatus)
            .count { it == "ABSENT" }
    }
    val attendancePercentage = if (totalPossibleDays > 0) {
        ((totalPossibleDays - totalAbsences).toDouble() / totalPossibleDays) * 100
    } else 100.0

    val mostAbsentStudent = attendanceList.maxByOrNull { record ->
        listOf(record.mondayStatus, record.tuesdayStatus, record.wednesdayStatus, record.thursdayStatus, record.fridayStatus)
            .count { it == "ABSENT" }
    }

    val allGrades by viewModel.allGrades.collectAsState()
    val studentLedgers by viewModel.allStudentLedgers.collectAsState()
    val allStudentProfiles by viewModel.allStudentProfiles.collectAsState()
    val allLessonPlans by viewModel.allLessonPlans.collectAsState()
    val allTimetables by viewModel.allTimetables.collectAsState()
    val allTeacherLoanRequests by viewModel.allTeacherLoanRequests.collectAsState()
    val allDirectMessages by viewModel.allDirectMessages.collectAsState()
    val allDigitalResources by viewModel.allDigitalResources.collectAsState()
    val allClassAssignments by viewModel.allClassAssignments.collectAsState()
    val uiLoadingState by viewModel.uiLoadingState.collectAsState()
    val loadingMessage by viewModel.loadingMessage.collectAsState()

    val teacherTargetTab by viewModel.teacherTargetTab.collectAsState()
    var activeTeacherTab by remember { mutableStateOf(0) } // 0: Attendance, 1: Gradebook, 2: Lesson Plans, 3: Timetable, 4: Loan Request, 5: Parent Messaging, 6: Pay Receipts, 7: Digital Library, 8: Class Assignments

    LaunchedEffect(teacherTargetTab) {
        teacherTargetTab?.let { target ->
            activeTeacherTab = target
            viewModel.clearTeacherTargetTab()
        }
    }

    // Teacher Digital Library & Assignment Upload States
    val teacherAssignedClass = "JHS 2 - Gold"
    val teacherAssignedSubject = "Mathematics"
    var teacherLibCategoryFilter by remember { mutableStateOf("ALL") }
    var showUploadResourceDialog by remember { mutableStateOf(false) }

    var showUploadAssignmentDialog by remember { mutableStateOf(false) }
    var assignmentTitleInput by remember { mutableStateOf("") }
    var assignmentDescInput by remember { mutableStateOf("") }
    var assignmentClassInput by remember { mutableStateOf("JHS 2 - Gold") }
    var assignmentSubjectInput by remember { mutableStateOf("Mathematics") }
    var assignmentDueDateInput by remember { mutableStateOf("2026-08-05") }
    var assignmentPeriodInput by remember { mutableStateOf("WEEKLY") } // "DAILY", "WEEKLY", "TERMLY"
    var assignmentMaxScoreInput by remember { mutableStateOf("100") }

    // Teacher Loan Request State (Feature 1)
    var inputLoanAmount by remember { mutableStateOf("1200.0") }
    var inputLoanDurationMonths by remember { mutableStateOf("6") }
    var inputLoanTerms by remember { mutableStateOf("Monthly salary deduction of GH₵ 200.00") }
    var inputLoanReason by remember { mutableStateOf("Emergency medical and dependent school fees advance") }

    // Direct Messaging State (Feature 5)
    var inputMessageRecipient by remember { mutableStateOf("Mrs. Grace Mensah") }
    var inputMessageChildName by remember { mutableStateOf("Ama Serwaa Mensah") }
    var inputMessageSubject by remember { mutableStateOf("BECE Mathematics Homework & Prep") }
    var inputMessageBody by remember { mutableStateOf("Good day Mrs. Mensah, Ama is performing exceptionally well in algebra. Please ensure she continues daily practice.") }

    // Digital Pay Receipt State (Feature 4)
    var showDigitalPayReceiptDialog by remember { mutableStateOf(false) }
    var timetableClassFilter by remember { mutableStateOf("JHS 2 - Gold") }
    var timetableDayFilter by remember { mutableStateOf("ALL") }

    var showScheduleSlotDialog by remember { mutableStateOf(false) }
    var editingSlot by remember { mutableStateOf<ClassTimetable?>(null) }
    var inputSlotClassName by remember { mutableStateOf("JHS 2 - Gold") }
    var inputSlotDayOfWeek by remember { mutableStateOf("Monday") }
    var inputSlotPeriodNumber by remember { mutableStateOf("1") }
    var inputSlotStartTime by remember { mutableStateOf("08:00 AM") }
    var inputSlotEndTime by remember { mutableStateOf("08:45 AM") }
    var inputSlotSubject by remember { mutableStateOf("Mathematics") }
    var inputSlotTeacherName by remember { mutableStateOf("Mr. Emmanuel Mensah") }
    var inputSlotClassroom by remember { mutableStateOf("Block J2-A") }
    var selectedGradeSubject by remember { mutableStateOf("Mathematics") }
    var selectedGradeClass by remember { mutableStateOf("JHS 2 - Gold") }
    var selectedGradeTerm by remember { mutableStateOf("Term 1") } // "Term 1", "Term 2", "Term 3", "All Terms / Cumulative"
    var gradebookViewMode by remember { mutableStateOf(0) } // 0: Subject Marksheet, 1: Cumulative Matrix, 2: GPA Trends & Analytics
    var gpaTrendFilter by remember { mutableStateOf("ALL") } // "ALL", "IMPROVING", "STEADY", "DECLINING"
    var gpaTrendSearchQuery by remember { mutableStateOf("") }

    var showGradeEntryDialog by remember { mutableStateOf(false) }
    var targetGradeStudentId by remember { mutableStateOf(101L) }
    var targetGradeStudentName by remember { mutableStateOf("Ama Serwaa Mensah") }
    var targetGradeTerm by remember { mutableStateOf("Term 1") }
    var targetGradeSubject by remember { mutableStateOf("Mathematics") }
    var inputClassScore by remember { mutableStateOf("28.0") }
    var inputExamScore by remember { mutableStateOf("64.0") }
    var inputTeacherRemarks by remember { mutableStateOf("Good academic progress and active participation.") }

    // Teacher Request Add Student State
    var showRequestAddStudentDialog by remember { mutableStateOf(false) }
    var inputReqStudentName by remember { mutableStateOf("") }
    var inputReqStudentClass by remember { mutableStateOf("JHS 2 - Gold") }
    var inputReqGuardianPhone by remember { mutableStateOf("0244123456") }
    var inputReqEstimatedFees by remember { mutableStateOf("1200.0") }
    var inputReqReason by remember { mutableStateOf("New transfer student joining class.") }

    // Daily Attendance Tracking State
    val allDailyAttendance by viewModel.allDailyAttendance.collectAsState()
    var showDailyAttendanceDialog by remember { mutableStateOf(false) }
    var inputDailyAttendanceDate by remember { mutableStateOf("2026-07-27") }
    var inputDailyAttendanceClass by remember { mutableStateOf("JHS 2 - Gold") }
    val dailyAttendanceStatusMap = remember { mutableStateMapOf<Long, String>() }
    val dailyAttendanceRemarksMap = remember { mutableStateMapOf<Long, String>() }

    // Attendance Summary Report State
    var showAttendanceSummaryReportDialog by remember { mutableStateOf(false) }
    var summaryReportClassFilter by remember { mutableStateOf("JHS 2 - Gold") }
    var summaryReportPeriodFilter by remember { mutableStateOf("ALL") }

    // Academic Promotion / Demotion State
    val activeUserAccount by viewModel.activeUserAccount.collectAsState()
    var showClassPromotionDialog by remember { mutableStateOf(false) }
    val downloadedMediaResource by viewModel.downloadedMediaResource.collectAsState()
    var playingMediaResource by remember { mutableStateOf<DigitalResource?>(null) }

    if (showClassPromotionDialog) {
        val teacherName = activeUserAccount?.fullName ?: "Class Teacher"
        com.example.ui.components.ClassPromotionDialog(
            userRole = "Teacher",
            allStudents = allStudentProfiles,
            onDismiss = { showClassPromotionDialog = false },
            onPromoteStudent = { studentId, targetClass ->
                viewModel.promoteStudent(studentId, targetClass)
            },
            onDemoteStudent = { studentId, targetClass ->
                viewModel.demoteStudent(studentId, targetClass)
            },
            onPromoteClass = { currentClass, targetClass ->
                viewModel.promoteClass(currentClass, targetClass)
            },
            onDemoteClass = { currentClass, targetClass ->
                viewModel.demoteClass(currentClass, targetClass)
            },
            onRequestPromoteClass = { currentClass, targetClass, reason ->
                viewModel.submitTeacherPromotionDemotionRequest(
                    studentId = null,
                    studentName = null,
                    currentClass = currentClass,
                    targetClass = targetClass,
                    isDemotion = false,
                    isClassWide = true,
                    reason = reason,
                    teacherName = teacherName
                )
            },
            onRequestDemoteClass = { currentClass, targetClass, reason ->
                viewModel.submitTeacherPromotionDemotionRequest(
                    studentId = null,
                    studentName = null,
                    currentClass = currentClass,
                    targetClass = targetClass,
                    isDemotion = true,
                    isClassWide = true,
                    reason = reason,
                    teacherName = teacherName
                )
            },
            onRequestPromoteStudent = { studentId, studentName, currentClass, targetClass, reason ->
                viewModel.submitTeacherPromotionDemotionRequest(
                    studentId = studentId,
                    studentName = studentName,
                    currentClass = currentClass,
                    targetClass = targetClass,
                    isDemotion = false,
                    isClassWide = false,
                    reason = reason,
                    teacherName = teacherName
                )
            },
            onRequestDemoteStudent = { studentId, studentName, currentClass, targetClass, reason ->
                viewModel.submitTeacherPromotionDemotionRequest(
                    studentId = studentId,
                    studentName = studentName,
                    currentClass = currentClass,
                    targetClass = targetClass,
                    isDemotion = true,
                    isClassWide = false,
                    reason = reason,
                    teacherName = teacherName
                )
            }
        )
    }

    if (downloadedMediaResource != null) {
        com.example.ui.components.DownloadedMediaDialog(
            resource = downloadedMediaResource!!,
            onDismiss = { viewModel.dismissDownloadedMedia() }
        )
    }

    if (playingMediaResource != null) {
        com.example.ui.components.InAppMediaViewerDialog(
            resource = playingMediaResource!!,
            onDismiss = { playingMediaResource = null }
        )
    }

    // Request Add Student Dialog
    if (showRequestAddStudentDialog) {
        AlertDialog(
            onDismissRequest = { showRequestAddStudentDialog = false },
            title = {
                Column {
                    Text("Register New Student", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Requires Proprietor Permission & Approval", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = inputReqStudentName,
                        onValueChange = { inputReqStudentName = it },
                        label = { Text("Student Full Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("teacher_req_student_name_field")
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = inputReqStudentClass,
                            onValueChange = { inputReqStudentClass = it },
                            label = { Text("Class") },
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("teacher_req_student_class_field")
                        )

                        OutlinedTextField(
                            value = inputReqGuardianPhone,
                            onValueChange = { inputReqGuardianPhone = it },
                            label = { Text("Guardian Phone") },
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("teacher_req_guardian_phone_field")
                        )
                    }

                    OutlinedTextField(
                        value = inputReqEstimatedFees,
                        onValueChange = { inputReqEstimatedFees = it },
                        label = { Text("Estimated Term Fees (GH₵)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("teacher_req_fees_field")
                    )

                    OutlinedTextField(
                        value = inputReqReason,
                        onValueChange = { inputReqReason = it },
                        label = { Text("Reason / Remarks for Proprietor") },
                        modifier = Modifier.fillMaxWidth().testTag("teacher_req_reason_field")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputReqStudentName.isNotBlank()) {
                            viewModel.submitTeacherAddStudentRequest(
                                studentName = inputReqStudentName,
                                className = inputReqStudentClass,
                                guardianPhone = inputReqGuardianPhone,
                                feesGhc = inputReqEstimatedFees.toDoubleOrNull() ?: 1200.0,
                                reason = inputReqReason
                            )
                            showRequestAddStudentDialog = false
                            inputReqStudentName = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GhanaNavyPrimary),
                    modifier = Modifier.testTag("submit_teacher_req_student_button")
                ) {
                    Text("Send Request to Proprietor")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRequestAddStudentDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Daily Attendance Marking Dialog
    if (showDailyAttendanceDialog) {
        AlertDialog(
            onDismissRequest = { showDailyAttendanceDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.FactCheck, contentDescription = null, tint = GhanaNavyPrimary)
                    Column {
                        Text("Mark Daily Attendance", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Record daily presence status & remarks", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inputDailyAttendanceDate,
                            onValueChange = { inputDailyAttendanceDate = it },
                            label = { Text("Date (YYYY-MM-DD)", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1.2f).testTag("daily_attendance_date_field")
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text("Class", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf("JHS 2 - Gold", "Primary 6", "Primary 4").forEach { cls ->
                                    FilterChip(
                                        selected = inputDailyAttendanceClass == cls,
                                        onClick = { inputDailyAttendanceClass = cls },
                                        label = { Text(cls.take(5), fontSize = 10.sp) },
                                        modifier = Modifier.testTag("daily_class_$cls")
                                    )
                                }
                            }
                        }
                    }

                    // Quick Bulk Actions Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Student Roster:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedButton(
                                onClick = {
                                    val targetStudents = studentLedgers.filter { it.className == inputDailyAttendanceClass }
                                    targetStudents.forEach { st -> dailyAttendanceStatusMap[st.studentId] = "PRESENT" }
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp).testTag("bulk_present_button")
                            ) {
                                Text("All Present", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GhanaEmeraldGreen)
                            }

                            OutlinedButton(
                                onClick = {
                                    val targetStudents = studentLedgers.filter { it.className == inputDailyAttendanceClass }
                                    targetStudents.forEach { st -> dailyAttendanceStatusMap[st.studentId] = "ABSENT" }
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp).testTag("bulk_absent_button")
                            ) {
                                Text("All Absent", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                            }
                        }
                    }

                    HorizontalDivider()

                    val targetClassStudents = studentLedgers.filter { it.className == inputDailyAttendanceClass }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        targetClassStudents.forEach { st ->
                            val currentStatus = dailyAttendanceStatusMap[st.studentId] ?: "PRESENT"
                            val currentRemark = dailyAttendanceRemarksMap[st.studentId] ?: ""

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(st.studentName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Text("ID #${st.studentId} • ${st.className}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            FilterChip(
                                                selected = currentStatus == "PRESENT",
                                                onClick = { dailyAttendanceStatusMap[st.studentId] = "PRESENT" },
                                                label = { Text("P", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = GhanaEmeraldGreen,
                                                    selectedLabelColor = Color.White
                                                ),
                                                modifier = Modifier.height(28.dp).testTag("status_present_${st.studentId}")
                                            )

                                            FilterChip(
                                                selected = currentStatus == "ABSENT",
                                                onClick = { dailyAttendanceStatusMap[st.studentId] = "ABSENT" },
                                                label = { Text("A", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = Color(0xFFC62828),
                                                    selectedLabelColor = Color.White
                                                ),
                                                modifier = Modifier.height(28.dp).testTag("status_absent_${st.studentId}")
                                            )

                                            FilterChip(
                                                selected = currentStatus == "EXCUSED",
                                                onClick = { dailyAttendanceStatusMap[st.studentId] = "EXCUSED" },
                                                label = { Text("L/E", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = Color(0xFFE65100),
                                                    selectedLabelColor = Color.White
                                                ),
                                                modifier = Modifier.height(28.dp).testTag("status_excused_${st.studentId}")
                                            )
                                        }
                                    }

                                    if (currentStatus != "PRESENT") {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        OutlinedTextField(
                                            value = currentRemark,
                                            onValueChange = { dailyAttendanceRemarksMap[st.studentId] = it },
                                            placeholder = { Text("Optional remarks/reason...", fontSize = 10.sp) },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth().height(42.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val targetClassStudents = studentLedgers.filter { it.className == inputDailyAttendanceClass }
                        val records = targetClassStudents.map { st ->
                            val stStatus = dailyAttendanceStatusMap[st.studentId] ?: "PRESENT"
                            val stRemark = dailyAttendanceRemarksMap[st.studentId] ?: ""
                            DailyStudentAttendance(
                                studentId = st.studentId,
                                studentName = st.studentName,
                                className = st.className,
                                dateString = inputDailyAttendanceDate,
                                status = stStatus,
                                isPresent = stStatus == "PRESENT",
                                isAbsent = stStatus == "ABSENT",
                                isExcused = stStatus == "EXCUSED",
                                remarks = stRemark
                            )
                        }
                        viewModel.batchSaveDailyAttendance(records)
                        showDailyAttendanceDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GhanaNavyPrimary),
                    modifier = Modifier.testTag("save_daily_attendance_button")
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save Daily Register", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDailyAttendanceDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Attendance Summary Report Dialog
    if (showAttendanceSummaryReportDialog) {
        AlertDialog(
            onDismissRequest = { showAttendanceSummaryReportDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Assessment, contentDescription = null, tint = GhanaNavyPrimary)
                    Column {
                        Text("Attendance Summary Report", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Class presence analytics & summary report card", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
            text = {
                val classStudents = if (summaryReportClassFilter == "All Classes") studentLedgers else studentLedgers.filter { it.className == summaryReportClassFilter }

                val classDailyRecords = if (summaryReportClassFilter == "All Classes") allDailyAttendance else allDailyAttendance.filter { it.className == summaryReportClassFilter }

                val totalRecordedLogs = classDailyRecords.size
                val presentCount = classDailyRecords.count { it.isPresent || it.status == "PRESENT" }
                val absentCount = classDailyRecords.count { it.isAbsent || it.status == "ABSENT" }
                val excusedCount = classDailyRecords.count { it.isExcused || it.status == "EXCUSED" }

                val classAttendanceRate = if (totalRecordedLogs > 0) {
                    (presentCount.toDouble() / totalRecordedLogs) * 100.0
                } else 92.5

                val atRiskStudentsCount = classStudents.count { st ->
                    val stRecords = classDailyRecords.filter { it.studentId == st.studentId }
                    if (stRecords.isNotEmpty()) {
                        val stPresent = stRecords.count { it.isPresent || it.status == "PRESENT" }
                        (stPresent.toDouble() / stRecords.size) < 0.75
                    } else false
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Filters Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Class:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        listOf("JHS 2 - Gold", "Primary 6", "Primary 4", "All Classes").forEach { cls ->
                            FilterChip(
                                selected = summaryReportClassFilter == cls,
                                onClick = { summaryReportClassFilter = cls },
                                label = { Text(cls.take(6), fontSize = 10.sp) },
                                modifier = Modifier.testTag("report_class_$cls")
                            )
                        }
                    }

                    // Summary KPI Metric Cards Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(
                                containerColor = if (classAttendanceRate >= 90) GhanaEmeraldGreen.copy(alpha = 0.15f) else Color(0xFFFFF3CD)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Overall Rate", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    "${"%.1f".format(classAttendanceRate)}%",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (classAttendanceRate >= 90) GhanaEmeraldGreen else Color(0xFF856404)
                                )
                                Text(
                                    if (classAttendanceRate >= 90) "Excellent" else "Attention",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (classAttendanceRate >= 90) GhanaEmeraldGreen else Color(0xFF856404)
                                )
                            }
                        }

                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Present / Total", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$presentCount / $totalRecordedLogs", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text("$absentCount Abs • $excusedCount Exc", fontSize = 9.sp, color = Color.Gray)
                            }
                        }

                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(
                                containerColor = if (atRiskStudentsCount > 0) Color(0xFFF8D7DA) else GhanaEmeraldGreen.copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("At Risk (<75%)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$atRiskStudentsCount Student(s)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (atRiskStudentsCount > 0) Color.Red else GhanaEmeraldGreen)
                                Text("Needs Follow-up", fontSize = 9.sp, color = Color.Gray)
                            }
                        }
                    }

                    HorizontalDivider()

                    Text("Student Attendance Summary Roster:", fontWeight = FontWeight.Bold, fontSize = 12.sp)

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        classStudents.forEach { st ->
                            val stRecords = classDailyRecords.filter { it.studentId == st.studentId }
                            val stTotal = if (stRecords.isNotEmpty()) stRecords.size else 5
                            val stPresent = if (stRecords.isNotEmpty()) stRecords.count { it.isPresent || it.status == "PRESENT" } else 5
                            val stAbsent = if (stRecords.isNotEmpty()) stRecords.count { it.isAbsent || it.status == "ABSENT" } else 0
                            val rate = (stPresent.toDouble() / stTotal) * 100.0

                            Surface(
                                shape = RoundedCornerShape(10.dp),
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
                                    Column(modifier = Modifier.weight(1.5f)) {
                                        Text(st.studentName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text("ID #${st.studentId} • ${st.className}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("${"%.1f".format(rate)}%", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Text("$stPresent Pres / $stAbsent Abs", fontSize = 9.sp, color = Color.Gray)
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = when {
                                                rate >= 90.0 -> GhanaEmeraldGreen
                                                rate >= 75.0 -> GhanaGoldAccent
                                                else -> Color.Red
                                            }
                                        ) {
                                            Text(
                                                text = when {
                                                    rate >= 90.0 -> "EXCELLENT"
                                                    rate >= 75.0 -> "REGULAR"
                                                    else -> "AT RISK"
                                                },
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            val classStudents = if (summaryReportClassFilter == "All Classes") studentLedgers else studentLedgers.filter { it.className == summaryReportClassFilter }
                            val atRiskCount = classStudents.count { st ->
                                val stRecords = allDailyAttendance.filter { it.studentId == st.studentId }
                                if (stRecords.isNotEmpty()) {
                                    val stPresent = stRecords.count { it.isPresent || it.status == "PRESENT" }
                                    (stPresent.toDouble() / stRecords.size) < 0.75
                                } else false
                            }
                            viewModel.notifyAtRiskGuardians(summaryReportClassFilter, atRiskCount)
                        },
                        modifier = Modifier.testTag("notify_at_risk_button")
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Notify At-Risk", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            showAttendanceSummaryReportDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GhanaNavyPrimary),
                        modifier = Modifier.testTag("export_summary_report_button")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export Report", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showAttendanceSummaryReportDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Daily Lesson Plan Form & Filter State
    var showCreateLessonPlanDialog by remember { mutableStateOf(false) }
    var inputPlanClass by remember { mutableStateOf("JHS 2 - Gold") }
    var inputPlanSubject by remember { mutableStateOf("Mathematics") }
    var inputPlanTopic by remember { mutableStateOf("") }
    var inputPlanSubtopic by remember { mutableStateOf("") }
    var inputPlanDate by remember { mutableStateOf("2026-07-28") }
    var inputPlanDuration by remember { mutableStateOf("60") }
    var inputPlanObjectives by remember { mutableStateOf("") }
    var inputPlanMaterials by remember { mutableStateOf("Textbooks, Whiteboard, Model Charts") }
    var inputPlanProcedure by remember { mutableStateOf("1. Introduction & Previous Lesson Review (10 mins)\n2. Main Instructional Presentation (25 mins)\n3. Pair Work & Problem Solving Activity (15 mins)\n4. Summary & Exit Ticket Evaluation (10 mins)") }

    var selectedPlanFilterSubject by remember { mutableStateOf("All") }
    var selectedPlanFilterClass by remember { mutableStateOf("All") }

    val subjectsList = listOf("Mathematics", "English Language", "Integrated Science", "Social Studies", "ICT", "RME")
    val classesList = listOf("JHS 2 - Gold", "Primary 6", "Primary 4")

    // Create Lesson Plan Dialog
    if (showCreateLessonPlanDialog) {
        AlertDialog(
            onDismissRequest = { showCreateLessonPlanDialog = false },
            title = {
                Column {
                    Text("Upload Daily Lesson Plan", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Submit for Management Review & Approval", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Target Class:", style = MaterialTheme.typography.labelSmall)
                            OutlinedTextField(
                                value = inputPlanClass,
                                onValueChange = { inputPlanClass = it },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("input_plan_class_field")
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Subject:", style = MaterialTheme.typography.labelSmall)
                            OutlinedTextField(
                                value = inputPlanSubject,
                                onValueChange = { inputPlanSubject = it },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("input_plan_subject_field")
                            )
                        }
                    }

                    OutlinedTextField(
                        value = inputPlanTopic,
                        onValueChange = { inputPlanTopic = it },
                        label = { Text("Lesson Topic (e.g. Quadratic Equations)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_plan_topic_field")
                    )

                    OutlinedTextField(
                        value = inputPlanSubtopic,
                        onValueChange = { inputPlanSubtopic = it },
                        label = { Text("Sub-Topic / Specific Area") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_plan_subtopic_field")
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = inputPlanDate,
                            onValueChange = { inputPlanDate = it },
                            label = { Text("Lesson Date") },
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("input_plan_date_field")
                        )
                        OutlinedTextField(
                            value = inputPlanDuration,
                            onValueChange = { inputPlanDuration = it },
                            label = { Text("Duration (Mins)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("input_plan_duration_field")
                        )
                    }

                    OutlinedTextField(
                        value = inputPlanObjectives,
                        onValueChange = { inputPlanObjectives = it },
                        label = { Text("Learning Objectives & Expected Outcomes") },
                        modifier = Modifier.fillMaxWidth().testTag("input_plan_objectives_field")
                    )

                    OutlinedTextField(
                        value = inputPlanMaterials,
                        onValueChange = { inputPlanMaterials = it },
                        label = { Text("Teaching Materials / Lab Equipment") },
                        modifier = Modifier.fillMaxWidth().testTag("input_plan_materials_field")
                    )

                    OutlinedTextField(
                        value = inputPlanProcedure,
                        onValueChange = { inputPlanProcedure = it },
                        label = { Text("Lesson Procedure & Time Allocation") },
                        modifier = Modifier.fillMaxWidth().testTag("input_plan_procedure_field")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputPlanTopic.isBlank() || inputPlanObjectives.isBlank()) {
                            return@Button
                        }
                        viewModel.createLessonPlan(
                            className = inputPlanClass,
                            subject = inputPlanSubject,
                            topic = inputPlanTopic,
                            subTopic = inputPlanSubtopic,
                            lessonDate = inputPlanDate,
                            durationMinutes = inputPlanDuration.toIntOrNull() ?: 60,
                            objectives = inputPlanObjectives,
                            materials = inputPlanMaterials,
                            procedure = inputPlanProcedure
                        )
                        showCreateLessonPlanDialog = false
                        inputPlanTopic = ""
                        inputPlanSubtopic = ""
                        inputPlanObjectives = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GhanaNavyPrimary),
                    modifier = Modifier.testTag("submit_lesson_plan_button")
                ) {
                    Text("Submit Lesson Plan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateLessonPlanDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Grade Entry Dialog for Teacher
    if (showGradeEntryDialog) {
        val dialogTermsList = listOf("Term 1", "Term 2", "Term 3")

        AlertDialog(
            onDismissRequest = { showGradeEntryDialog = false },
            title = {
                Column {
                    Text("Grade & Marks Entry", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("$targetGradeStudentName • $targetGradeSubject ($selectedGradeClass)", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Term Selector Chips
                    Text("1. Academic Term:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        dialogTermsList.forEach { term ->
                            FilterChip(
                                selected = targetGradeTerm == term,
                                onClick = { targetGradeTerm = term },
                                label = { Text(term, fontSize = 11.sp) },
                                modifier = Modifier.testTag("dialog_term_chip_$term")
                            )
                        }
                    }

                    // Subject Selector Chips
                    Text("2. Academic Subject:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        subjectsList.take(3).forEach { sub ->
                            FilterChip(
                                selected = targetGradeSubject == sub,
                                onClick = { targetGradeSubject = sub },
                                label = { Text(sub, fontSize = 10.sp) },
                                modifier = Modifier.testTag("dialog_subject_chip_$sub")
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        subjectsList.drop(3).forEach { sub ->
                            FilterChip(
                                selected = targetGradeSubject == sub,
                                onClick = { targetGradeSubject = sub },
                                label = { Text(sub, fontSize = 10.sp) },
                                modifier = Modifier.testTag("dialog_subject_chip_$sub")
                            )
                        }
                    }

                    Text("3. Assessment Marks:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = inputClassScore,
                            onValueChange = { inputClassScore = it },
                            label = { Text("Class CA (30/40)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("input_class_score_field")
                        )
                        OutlinedTextField(
                            value = inputExamScore,
                            onValueChange = { inputExamScore = it },
                            label = { Text("Final Exam (70/60)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("input_exam_score_field")
                        )
                    }

                    val classVal = inputClassScore.toDoubleOrNull() ?: 0.0
                    val examVal = inputExamScore.toDoubleOrNull() ?: 0.0
                    val totalVal = (classVal + examVal).coerceIn(0.0, 100.0)
                    val subjectGpa = GpaCalculator.scoreToGpa(totalVal)
                    val letterVal = "${GpaCalculator.scoreToLetter(totalVal)} (${GpaCalculator.letterToDescription(GpaCalculator.scoreToLetter(totalVal))})"

                    // Live Student Cumulative Average & GPA Impact Calculation
                    val existingStudentGrades = allGrades.filter {
                        it.studentId == targetGradeStudentId &&
                        !(it.subject.equals(targetGradeSubject, ignoreCase = true) && it.academicTerm == targetGradeTerm)
                    }
                    val currentAvg = if (existingStudentGrades.isNotEmpty()) existingStudentGrades.map { it.totalScore }.average() else 0.0
                    val currentGpaAvg = if (existingStudentGrades.isNotEmpty()) GpaCalculator.calculateGpaForGrades(existingStudentGrades) else 0.0
                    val newScoresList = existingStudentGrades.map { it.totalScore } + totalVal
                    val predictedAvg = newScoresList.average()

                    val predictedGpaList = existingStudentGrades + StudentGrade(
                        studentId = targetGradeStudentId,
                        studentName = targetGradeStudentName,
                        className = selectedGradeClass,
                        subject = targetGradeSubject,
                        academicTerm = targetGradeTerm,
                        totalScore = totalVal
                    )
                    val predictedGpa = GpaCalculator.calculateGpaForGrades(predictedGpaList)
                    val gpaShift = predictedGpa - currentGpaAvg

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.fillMaxWidth().testTag("dialog_gpa_calculation_surface")
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Calculated Term Mark:", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                Text("${String.format("%.1f", totalVal)}% • $letterVal", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Subject Grade Point (4.0 Scale):", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = GhanaNavyPrimary)
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = GhanaGoldAccent.copy(alpha = 0.3f)
                                ) {
                                    Text("${String.format("%.2f", subjectGpa)} / 4.00 GPA", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = GhanaNavyPrimary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Predicted Cumulative GPA:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text("Cumulative Avg: ${String.format("%.1f", predictedAvg)}%", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("${String.format("%.2f", predictedGpa)} GPA", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = GhanaNavyPrimary)
                                    GpaTrendBadge(
                                        direction = if (gpaShift >= 0.05) GpaCalculator.GpaTrendDirection.IMPROVING else if (gpaShift <= -0.05) GpaCalculator.GpaTrendDirection.DECLINING else GpaCalculator.GpaTrendDirection.STEADY,
                                        delta = gpaShift
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = inputTeacherRemarks,
                        onValueChange = { inputTeacherRemarks = it },
                        label = { Text("Teacher Remarks / Guidance") },
                        modifier = Modifier.fillMaxWidth().testTag("input_teacher_remarks_field")
                    )

                    // Quick Remarks Presets
                    Text("Preset Remarks:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("Exceptional progress.", "Consistent effort.", "Needs extra math practice.").forEach { remark ->
                            SuggestionChip(
                                onClick = { inputTeacherRemarks = remark },
                                label = { Text(remark, fontSize = 9.sp) },
                                modifier = Modifier.testTag("preset_remark_$remark")
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val classScore = inputClassScore.toDoubleOrNull() ?: 0.0
                        val examScore = inputExamScore.toDoubleOrNull() ?: 0.0
                        viewModel.saveOrUpdateStudentGrade(
                            studentId = targetGradeStudentId,
                            studentName = targetGradeStudentName,
                            className = selectedGradeClass,
                            subject = targetGradeSubject,
                            term = targetGradeTerm,
                            academicYear = "2025/2026",
                            classScore = classScore,
                            examScore = examScore,
                            remarks = inputTeacherRemarks
                        )
                        showGradeEntryDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GhanaNavyPrimary),
                    modifier = Modifier.testTag("save_student_grade_button")
                ) {
                    Text("Publish Grade")
                }
            },
            dismissButton = {
                TextButton(onClick = { showGradeEntryDialog = false }) {
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
        // --- TEACHER PORTAL GROUPED TASK HUB & DASHBOARD NAVIGATION ---
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("teacher_grouped_task_hub_card")
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
                                    text = "Teacher Portal Tasks",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = GhanaNavyPrimary
                                )
                                Text(
                                    text = "Grouped teaching task shortcuts & navigation",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Button taking teacher to the App Dashboard
                        Button(
                            onClick = { viewModel.setViewMode(com.example.ui.viewmodel.ViewMode.HOME) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GhanaNavyPrimary),
                            modifier = Modifier.testTag("teacher_task_go_to_dashboard")
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
                        // Button for registering student on teacher's portal
                        Button(
                            onClick = { showRequestAddStudentDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GhanaNavyPrimary),
                            modifier = Modifier.weight(1f).testTag("teacher_task_register_student")
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Register Student", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { activeTeacherTab = 0 },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GhanaEmeraldGreen),
                            modifier = Modifier.weight(1f).testTag("teacher_task_mark_attendance")
                        ) {
                            Icon(Icons.Default.HowToReg, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Mark Attendance", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { activeTeacherTab = 1 },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("teacher_task_gradebook")
                        ) {
                            Icon(Icons.Default.Grading, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Gradebook / Marks", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { activeTeacherTab = 2 },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("teacher_task_lesson_plan")
                        ) {
                            Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Lesson Plans", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { showClassPromotionDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = GhanaNavyPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("teacher_open_promotion_dialog_button")
                    ) {
                        Icon(Icons.Default.School, contentDescription = null, tint = GhanaGoldAccent, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Promote / Demote Students or Class", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // --- TOP TAB ROW FOR TEACHER ---
        item {
            ScrollableTabRow(
                selectedTabIndex = activeTeacherTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.primary,
                edgePadding = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
            ) {
                Tab(
                    selected = activeTeacherTab == 0,
                    onClick = { activeTeacherTab = 0 },
                    text = { Text("Attendance", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    icon = { Icon(Icons.Default.HowToReg, contentDescription = null) },
                    modifier = Modifier.testTag("teacher_tab_attendance")
                )
                Tab(
                    selected = activeTeacherTab == 1,
                    onClick = { activeTeacherTab = 1 },
                    text = { Text("Gradebook", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    icon = { Icon(Icons.Default.School, contentDescription = null) },
                    modifier = Modifier.testTag("teacher_tab_gradebook")
                )
                Tab(
                    selected = activeTeacherTab == 2,
                    onClick = { activeTeacherTab = 2 },
                    text = { Text("Lesson Plans", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    icon = { Icon(Icons.Default.Description, contentDescription = null) },
                    modifier = Modifier.testTag("teacher_tab_lesson_plans")
                )
                Tab(
                    selected = activeTeacherTab == 3,
                    onClick = { activeTeacherTab = 3 },
                    text = { Text("Timetable", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                    modifier = Modifier.testTag("teacher_tab_timetable")
                )
                Tab(
                    selected = activeTeacherTab == 4,
                    onClick = { activeTeacherTab = 4 },
                    text = { Text("Loan Request", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    icon = { Icon(Icons.Default.MonetizationOn, contentDescription = null) },
                    modifier = Modifier.testTag("teacher_tab_loan_request")
                )
                Tab(
                    selected = activeTeacherTab == 5,
                    onClick = { activeTeacherTab = 5 },
                    text = { Text("Parent Messages", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    icon = { Icon(Icons.Default.QuestionAnswer, contentDescription = null) },
                    modifier = Modifier.testTag("teacher_tab_parent_messages")
                )
                Tab(
                    selected = activeTeacherTab == 6,
                    onClick = { activeTeacherTab = 6 },
                    text = { Text("Pay Receipts", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    icon = { Icon(Icons.Default.ReceiptLong, contentDescription = null) },
                    modifier = Modifier.testTag("teacher_tab_pay_receipts")
                )
                Tab(
                    selected = activeTeacherTab == 7,
                    onClick = { activeTeacherTab = 7 },
                    text = { Text("Digital Library", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    icon = { Icon(Icons.Default.MenuBook, contentDescription = null) },
                    modifier = Modifier.testTag("teacher_tab_digital_library")
                )
                Tab(
                    selected = activeTeacherTab == 8,
                    onClick = { activeTeacherTab = 8 },
                    text = { Text("Assignments", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    icon = { Icon(Icons.Default.Assignment, contentDescription = null) },
                    modifier = Modifier.testTag("teacher_tab_assignments")
                )
            }
        }

        if (activeTeacherTab == 0) {
        // --- SECTION 1: GEOFENCED CLOCK-IN PANEL ---
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("geofenced_clock_in_card")
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
                                    .background(if (isWithinGeofence) GhanaGreenContainer else Color(0xFFF8D7DA)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isWithinGeofence) Icons.Default.GpsFixed else Icons.Default.GpsOff,
                                    contentDescription = null,
                                    tint = if (isWithinGeofence) GhanaEmeraldGreen else Color(0xFF842029),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Geofenced Staff Clock-In",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "St. Talafor Campus GPS (8.5945° N, 0.2366° E)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Simulation mode switcher button to test both inside & outside geofence
                        OutlinedButton(
                            onClick = {
                                if (simState == SimulatedGeofenceState.ON_CAMPUS_INSIDE_GEOFENCE) {
                                    viewModel.setSimulatedLocation(SimulatedGeofenceState.OUTSIDE_GEOFENCE)
                                } else {
                                    viewModel.setSimulatedLocation(SimulatedGeofenceState.ON_CAMPUS_INSIDE_GEOFENCE)
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("toggle_geofence_simulation")
                        ) {
                            Text(
                                text = if (isWithinGeofence) "Simulate Out" else "Simulate In",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Location Status Box
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isWithinGeofence) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
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
                                    text = if (isWithinGeofence) "GEOFENCE UNLOCKED" else "OUTSIDE CAMPUS BOUNDARY",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isWithinGeofence) Color(0xFF2E7D32) else Color(0xFFC62828)
                                )
                                Text(
                                    text = "Current Distance: ${currentDistance.toInt()}m from school center (Max: 200m)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Icon(
                                imageVector = if (isWithinGeofence) Icons.Default.CheckCircle else Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (isWithinGeofence) Color(0xFF2E7D32) else Color(0xFFC62828)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Clock-In Submit Button (Enabled ONLY when inside Geofence)
                    Button(
                        onClick = { viewModel.submitClockInOrOut("CLOCK_IN") },
                        enabled = isWithinGeofence,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GhanaNavyPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("clock_in_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.AccessTime, contentDescription = null)
                            Text(
                                text = if (isWithinGeofence) "Clock-In to St. Talafor Campus" else "Clock-In Locked (Get Closer to Campus)",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (clockInLogs.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Last Clock-In: ${clockInLogs.first().timestampString} (${clockInLogs.first().distanceMeters.toInt()}m)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // --- SECTION 2: WEEKLY CLASS ATTENDANCE REGISTER MATRIX ---
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Offline Register Sync & Network Status Control
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isOnline) Color(0xFFE8F5E9) else Color(0xFFFFF3CD)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("offline_attendance_sync_card")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
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
                                    .background(if (isOnline) GhanaEmeraldGreen else Color(0xFF856404)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isOnline) Icons.Default.CloudDone else Icons.Default.CloudOff,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = if (isOnline) "ONLINE REGISTER (AUTO-SYNC)" else "OFFLINE MODE ACTIVE",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isOnline) Color(0xFF0F5132) else Color(0xFF856404)
                                )
                                Text(
                                    text = if (unsyncedCount > 0) "$unsyncedCount attendance record(s) pending sync" else "All local records synced to cloud",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.toggleNetworkConnectivity() },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("toggle_network_offline_button")
                            ) {
                                Text(
                                    text = if (isOnline) "Simulate Offline" else "Go Online",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Button(
                                onClick = { viewModel.syncOfflineAttendance() },
                                enabled = isOnline && unsyncedCount > 0 && !isSyncing,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GhanaNavyPrimary),
                                modifier = Modifier.testTag("sync_offline_attendance_button")
                            ) {
                                if (isSyncing) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Text("Sync ($unsyncedCount)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Weekly Attendance Register",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Automated weekly total absenteeism matrix",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(
                            onClick = { showDailyAttendanceDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("open_daily_attendance_dialog_button")
                        ) {
                            Icon(imageVector = Icons.Default.FactCheck, contentDescription = null, modifier = Modifier.size(14.dp), tint = GhanaNavyPrimary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Mark Daily", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GhanaNavyPrimary)
                        }

                        Button(
                            onClick = { showAttendanceSummaryReportDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GhanaGoldAccent),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("open_attendance_summary_report_button")
                        ) {
                            Icon(imageVector = Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(14.dp), tint = GhanaNavyPrimary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Summary Report", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GhanaNavyPrimary)
                        }

                        Button(
                            onClick = { showRequestAddStudentDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GhanaNavyPrimary),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("teacher_request_add_student_button")
                        ) {
                            Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ Add Student", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                    // Class Selector Dropdown / Chips
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("JHS 2 - Gold", "Primary 6", "Primary 4").forEach { className ->
                            FilterChip(
                                selected = selectedClass == className,
                                onClick = { viewModel.setSelectedClass(className) },
                                label = { Text(className.take(8), fontSize = 11.sp) },
                                modifier = Modifier.testTag("select_class_$className")
                            )
                        }
                    }
                }

                // Class Overview Summary Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
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
                                text = "Weekly Class Rate",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "${String.format("%.1f", attendancePercentage)}%",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        if (mostAbsentStudent != null) {
                            val count = listOf(
                                mostAbsentStudent.mondayStatus, mostAbsentStudent.tuesdayStatus,
                                mostAbsentStudent.wednesdayStatus, mostAbsentStudent.thursdayStatus, mostAbsentStudent.fridayStatus
                            ).count { it == "ABSENT" }

                            if (count > 0) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "High Absenteeism Alert",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    Text(
                                        text = "${mostAbsentStudent.studentName} ($count days absent)",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = { viewModel.markAllPresentToday("Wed") },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("mark_all_present_button")
                        ) {
                            Text("All Present Today", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Attendance Table Header
        item {
            Card(
                shape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp),
                colors = CardDefaults.cardColors(containerColor = GhanaNavyPrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Student Name",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.weight(2.2f)
                    )
                    Text("M", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = GhanaGoldAccent, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                    Text("T", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = GhanaGoldAccent, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                    Text("W", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = GhanaGoldAccent, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                    Text("T", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = GhanaGoldAccent, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                    Text("F", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = GhanaGoldAccent, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                    Text("Abs", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center, modifier = Modifier.weight(1.2f))
                }
                Spacer(modifier = Modifier.height(6.dp))
                attendanceList.forEach { record ->
                    val weeklyAbsences = listOf(
                        record.mondayStatus, record.tuesdayStatus, record.wednesdayStatus, record.thursdayStatus, record.fridayStatus
                    ).count { it == "ABSENT" }

                    AttendanceRow(
                        record = record,
                        weeklyAbsenceCount = weeklyAbsences,
                        onToggleDay = { day -> viewModel.toggleDailyAttendance(record, day) }
                    )
                }
            }
        }

        if (activeTeacherTab == 1) {
        // --- SECTION: ACADEMIC GRADEBOOK, MARKS ENTRY & CUMULATIVE REPORT CARDS ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth().testTag("gradebook_filter_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Gradebook Filter & Academic Setup",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Class Selection Chips
                        Text("1. Target Class:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            classesList.forEach { cls ->
                                FilterChip(
                                    selected = selectedGradeClass == cls,
                                    onClick = { selectedGradeClass = cls },
                                    label = { Text(cls, fontSize = 11.sp) },
                                    modifier = Modifier.testTag("class_chip_$cls")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Academic Term Selection Chips
                        Text("2. Academic Term:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("Term 1", "Term 2", "Term 3", "All Terms / Cumulative").forEach { term ->
                                FilterChip(
                                    selected = selectedGradeTerm == term,
                                    onClick = { selectedGradeTerm = term },
                                    label = { Text(term, fontSize = 11.sp) },
                                    modifier = Modifier.testTag("term_chip_$term")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Subject Selection Chips
                        Text("3. Academic Subject:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            subjectsList.take(3).forEach { sub ->
                                FilterChip(
                                    selected = selectedGradeSubject == sub,
                                    onClick = { selectedGradeSubject = sub },
                                    label = { Text(sub, fontSize = 11.sp) },
                                    modifier = Modifier.testTag("subject_chip_$sub")
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            subjectsList.drop(3).forEach { sub ->
                                FilterChip(
                                    selected = selectedGradeSubject == sub,
                                    onClick = { selectedGradeSubject = sub },
                                    label = { Text(sub, fontSize = 11.sp) },
                                    modifier = Modifier.testTag("subject_chip_$sub")
                                )
                            }
                        }
                    }
                }
            }

            // Cumulative Analytics Summary Header Banner
            val classGrades = allGrades.filter {
                it.className == selectedGradeClass &&
                (selectedGradeTerm == "All Terms / Cumulative" || it.academicTerm == selectedGradeTerm)
            }
            val classAvg = if (classGrades.isNotEmpty()) classGrades.map { it.totalScore }.average() else 0.0
            val passRate = if (classGrades.isNotEmpty()) (classGrades.count { it.totalScore >= 50.0 }.toDouble() / classGrades.size * 100) else 0.0
            val distinctionRate = if (classGrades.isNotEmpty()) (classGrades.count { it.totalScore >= 75.0 }.toDouble() / classGrades.size * 100) else 0.0

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = GhanaNavyPrimary),
                    modifier = Modifier.fillMaxWidth().testTag("class_cumulative_analytics_card")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Class Performance Dashboard",
                                color = GhanaGoldAccent,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Surface(
                                color = Color.White.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text(
                                    text = "$selectedGradeClass • $selectedGradeTerm",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Class Average", color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp)
                                Text(
                                    text = if (classGrades.isNotEmpty()) "${String.format("%.1f", classAvg)}%" else "--",
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Pass Rate (≥50%)", color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp)
                                Text(
                                    text = if (classGrades.isNotEmpty()) "${String.format("%.1f", passRate)}%" else "--",
                                    color = GhanaGoldAccent,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Distinction (≥75%)", color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp)
                                Text(
                                    text = if (classGrades.isNotEmpty()) "${String.format("%.1f", distinctionRate)}%" else "--",
                                    color = Color(0xFF81C784),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Entries", color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp)
                                Text(
                                    text = "${classGrades.size}",
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp
                                )
                            }
                        }
                    }
                }

            // View Mode Selector Segmented Chips
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = gradebookViewMode == 0,
                        onClick = { gradebookViewMode = 0 },
                        leadingIcon = { Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(14.dp)) },
                        label = { Text("Subject Marksheet", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.weight(1f).testTag("view_mode_marksheet_chip")
                    )
                    FilterChip(
                        selected = gradebookViewMode == 1,
                        onClick = { gradebookViewMode = 1 },
                        leadingIcon = { Icon(Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(14.dp)) },
                        label = { Text("Cumulative Matrix", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.weight(1f).testTag("view_mode_matrix_chip")
                    )
                    FilterChip(
                        selected = gradebookViewMode == 2,
                        onClick = { gradebookViewMode = 2 },
                        leadingIcon = { Icon(Icons.Default.TrendingUp, contentDescription = null, modifier = Modifier.size(14.dp)) },
                        label = { Text("GPA Trends", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.weight(1f).testTag("view_mode_gpa_chip")
                    )
                }

                if (studentLedgers.isEmpty()) {
                    Text("No students registered for $selectedGradeClass.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else if (gradebookViewMode == 0) {
                    // --- MODE 0: SUBJECT MARKSHEET VIEW ---
                    Text(
                        text = "Subject Marks & Student Roll ($selectedGradeSubject • $selectedGradeTerm)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    val matchingSubjectGrades = allGrades.filter {
                        it.subject.equals(selectedGradeSubject, ignoreCase = true) &&
                        (selectedGradeTerm == "All Terms / Cumulative" || it.academicTerm == selectedGradeTerm)
                    }

                    studentLedgers.forEach { ledger ->
                    val studentAllGrades = allGrades.filter { it.studentId == ledger.studentId }
                    val studentCumulativeAvg = if (studentAllGrades.isNotEmpty()) studentAllGrades.map { it.totalScore }.average() else 0.0
                    val studentTrend = GpaCalculator.computeStudentGpaTrend(ledger.studentId, ledger.studentName, ledger.className, studentAllGrades)
                    val existingGrade = matchingSubjectGrades.find { it.studentId == ledger.studentId }

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth().testTag("teacher_grade_item_${ledger.studentId}")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = ledger.studentName,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                        GpaTrendBadge(
                                            direction = studentTrend.trendDirection,
                                            delta = studentTrend.gpaDeltaRecent
                                        )
                                    }
                                    Text(
                                        text = "ID: #${ledger.studentId} • ${ledger.className}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        horizontalAlignment = Alignment.End
                                    ) {
                                        Text("Cumulative GPA", fontSize = 9.sp, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = if (studentAllGrades.isNotEmpty()) "${String.format("%.2f", studentTrend.cumulativeGpa)} / 4.00" else "No Data",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 12.sp,
                                            color = GhanaNavyPrimary
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("$selectedGradeSubject ($selectedGradeTerm):", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    if (existingGrade != null) {
                                        val subGpa = GpaCalculator.scoreToGpa(existingGrade.totalScore)
                                        Text(
                                            text = "CA: ${existingGrade.classScore} | Exam: ${existingGrade.examScore} | Total: ${existingGrade.totalScore}% (${existingGrade.gradeLetter} • ${String.format("%.2f", subGpa)} GPA)",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        if (existingGrade.remarks.isNotBlank()) {
                                            Text(
                                                text = "Remarks: \"${existingGrade.remarks}\"",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = "Grade Status: Pending Entry",
                                            fontSize = 11.sp,
                                            color = Color(0xFFC62828),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                Button(
                                    onClick = {
                                        targetGradeStudentId = ledger.studentId
                                        targetGradeStudentName = ledger.studentName
                                        targetGradeSubject = selectedGradeSubject
                                        targetGradeTerm = if (selectedGradeTerm == "All Terms / Cumulative") "Term 1" else selectedGradeTerm
                                        inputClassScore = existingGrade?.classScore?.toString() ?: "28.0"
                                        inputExamScore = existingGrade?.examScore?.toString() ?: "60.0"
                                        inputTeacherRemarks = existingGrade?.remarks ?: "Good academic effort."
                                        showGradeEntryDialog = true
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (existingGrade != null) MaterialTheme.colorScheme.secondary else GhanaNavyPrimary
                                    ),
                                    modifier = Modifier.testTag("grade_student_${ledger.studentId}")
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (existingGrade != null) "Edit Grade" else "Enter Grade", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                    }
                } else if (gradebookViewMode == 1) {
                    // --- MODE 1: CUMULATIVE CLASS REPORT MATRIX VIEW ---
                    Text(
                        text = "Class Ranking & Multi-Term Cumulative Report Matrix",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    val matrixDataList = studentLedgers.map { ledger ->
                        val studentGrades = allGrades.filter { it.studentId == ledger.studentId }
                        val t1Grades = studentGrades.filter { it.academicTerm == "Term 1" }
                        val t2Grades = studentGrades.filter { it.academicTerm == "Term 2" }
                        val t3Grades = studentGrades.filter { it.academicTerm == "Term 3" }

                        val t1Avg = if (t1Grades.isNotEmpty()) t1Grades.map { it.totalScore }.average() else null
                        val t2Avg = if (t2Grades.isNotEmpty()) t2Grades.map { it.totalScore }.average() else null
                        val t3Avg = if (t3Grades.isNotEmpty()) t3Grades.map { it.totalScore }.average() else null
                        val overallAvg = if (studentGrades.isNotEmpty()) studentGrades.map { it.totalScore }.average() else 0.0

                        val trend = GpaCalculator.computeStudentGpaTrend(ledger.studentId, ledger.studentName, ledger.className, studentGrades)

                        StudentMatrixData(
                            studentId = ledger.studentId,
                            name = ledger.studentName,
                            className = ledger.className,
                            term1Avg = t1Avg,
                            term2Avg = t2Avg,
                            term3Avg = t3Avg,
                            overallCumulativeAvg = overallAvg,
                            totalSubjectEntries = studentGrades.size,
                            topGradeLetter = trend.topLetterClassification,
                            cumulativeGpa = trend.cumulativeGpa,
                            gpaTrend = trend
                        )
                    }.sortedByDescending { it.cumulativeGpa }

                    matrixDataList.forEachIndexed { index, data ->
                    val rank = index + 1

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth().testTag("student_matrix_card_${data.studentId}")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Rank Medal Badge
                                    Surface(
                                        shape = CircleShape,
                                        color = when (rank) {
                                            1 -> GhanaGoldAccent
                                            2 -> Color(0xFFC0C0C0)
                                            3 -> Color(0xFFCD7F32)
                                            else -> MaterialTheme.colorScheme.primaryContainer
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "$rank",
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 13.sp,
                                                color = if (rank <= 3) GhanaNavyPrimary else MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text(
                                                text = data.name,
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.titleSmall
                                            )
                                            data.gpaTrend?.let {
                                                GpaTrendBadge(direction = it.trendDirection, delta = it.gpaDeltaRecent)
                                            }
                                        }
                                        Text(
                                            text = "ID: #${data.studentId} • ${data.className} • ${data.totalSubjectEntries} Subject Entries",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Surface(
                                    color = when {
                                        data.overallCumulativeAvg >= 80.0 -> GhanaGreenContainer
                                        data.overallCumulativeAvg >= 70.0 -> MaterialTheme.colorScheme.primaryContainer
                                        else -> MaterialTheme.colorScheme.secondaryContainer
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("Cumulative GPA", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = if (data.totalSubjectEntries > 0) "${String.format("%.2f", data.cumulativeGpa)} GPA" else "N/A",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 14.sp,
                                            color = GhanaNavyPrimary
                                        )
                                        Text(
                                            text = if (data.totalSubjectEntries > 0) "(${String.format("%.1f", data.overallCumulativeAvg)}%)" else "",
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Term breakdown row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.weight(1f).padding(end = 4.dp)
                                ) {
                                    Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Term 1 Avg", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(
                                            text = data.term1Avg?.let { "${String.format("%.1f", it)}%" } ?: "--",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.weight(1f).padding(horizontal = 2.dp)
                                ) {
                                    Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Term 2 Avg", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(
                                            text = data.term2Avg?.let { "${String.format("%.1f", it)}%" } ?: "--",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.weight(1f).padding(start = 4.dp)
                                ) {
                                    Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Term 3 Avg", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(
                                            text = data.term3Avg?.let { "${String.format("%.1f", it)}%" } ?: "--",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Classification: ${data.topGradeLetter}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                OutlinedButton(
                                    onClick = {
                                        targetGradeStudentId = data.studentId
                                        targetGradeStudentName = data.name
                                        targetGradeSubject = selectedGradeSubject
                                        targetGradeTerm = if (selectedGradeTerm == "All Terms / Cumulative") "Term 1" else selectedGradeTerm
                                        inputClassScore = "28.0"
                                        inputExamScore = "60.0"
                                        inputTeacherRemarks = "Solid cumulative term effort."
                                        showGradeEntryDialog = true
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.testTag("matrix_entry_button_${data.studentId}")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("Add Mark", fontSize = 10.sp)
                                }
                            }
                        }
                    }
                    }
                } else {
                    // --- MODE 2: GPA TRENDS & ANALYTICS VIEW ---
                    val allStudentTrends = studentLedgers.map { ledger ->
                        val stGrades = allGrades.filter { it.studentId == ledger.studentId }
                        GpaCalculator.computeStudentGpaTrend(ledger.studentId, ledger.studentName, ledger.className, stGrades)
                    }

                    val classGradesForTrends = allGrades.filter { it.className == selectedGradeClass }
                    val classSummary = GpaCalculator.computeClassGpaTrendSummary(selectedGradeClass, classGradesForTrends, allStudentTrends)

                    ClassGpaTrendOverviewCard(summary = classSummary)

                    Spacer(modifier = Modifier.height(8.dp))

                    // Search Query & Filter Chips
                    OutlinedTextField(
                        value = gpaTrendSearchQuery,
                        onValueChange = { gpaTrendSearchQuery = it },
                        placeholder = { Text("Search student by name or ID...", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        trailingIcon = {
                            if (gpaTrendSearchQuery.isNotEmpty()) {
                                IconButton(onClick = { gpaTrendSearchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("gpa_trend_search_field"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "ALL" to "All Students",
                            "IMPROVING" to "Improving 📈",
                            "STEADY" to "Steady ➡️",
                            "DECLINING" to "Support 📉"
                        ).forEach { (key, label) ->
                            FilterChip(
                                selected = gpaTrendFilter == key,
                                onClick = { gpaTrendFilter = key },
                                label = { Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                modifier = Modifier.testTag("gpa_filter_chip_$key")
                            )
                        }
                    }

                    val filteredTrends = allStudentTrends.filter { trend ->
                        val matchesSearch = gpaTrendSearchQuery.isBlank() ||
                                trend.studentName.contains(gpaTrendSearchQuery, ignoreCase = true) ||
                                trend.studentId.toString().contains(gpaTrendSearchQuery)

                        val matchesFilter = when (gpaTrendFilter) {
                            "IMPROVING" -> trend.trendDirection == GpaCalculator.GpaTrendDirection.IMPROVING
                            "STEADY" -> trend.trendDirection == GpaCalculator.GpaTrendDirection.STEADY
                            "DECLINING" -> trend.trendDirection == GpaCalculator.GpaTrendDirection.DECLINING
                            else -> true
                        }

                        matchesSearch && matchesFilter
                    }.sortedByDescending { it.cumulativeGpa }

                    if (filteredTrends.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                        ) {
                            Text(
                                text = "No student GPA trend data matching current filters.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    } else {
                        filteredTrends.forEach { trend ->
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier.fillMaxWidth().testTag("gpa_trend_student_card_${trend.studentId}")
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = trend.studentName,
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.titleSmall
                                            )
                                            Text(
                                                text = "ID: #${trend.studentId} • ${trend.className} • ${trend.totalSubjectsCount} Subjects",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = GhanaNavyPrimary
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text("Cumulative", fontSize = 8.sp, color = GhanaGoldAccent, fontWeight = FontWeight.Bold)
                                                Text(
                                                    text = "${String.format("%.2f", trend.cumulativeGpa)} GPA",
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 14.sp,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        GpaTrendBadge(
                                            direction = trend.trendDirection,
                                            delta = trend.gpaDeltaRecent
                                        )

                                        Text(
                                            text = "Classification: ${trend.topLetterClassification} (${GpaCalculator.letterToDescription(trend.topLetterClassification)})",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    // Sparkline Trend Visualizer Canvas
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Text("GPA Trajectory Curve (Term 1 → Term 3):", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            GpaSparklineCanvas(
                                                term1Gpa = trend.term1Gpa,
                                                term2Gpa = trend.term2Gpa,
                                                term3Gpa = trend.term3Gpa
                                            )
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        Button(
                                            onClick = {
                                                targetGradeStudentId = trend.studentId
                                                targetGradeStudentName = trend.studentName
                                                targetGradeSubject = selectedGradeSubject
                                                targetGradeTerm = if (selectedGradeTerm == "All Terms / Cumulative") "Term 1" else selectedGradeTerm
                                                inputClassScore = "28.0"
                                                inputExamScore = "60.0"
                                                inputTeacherRemarks = "GPA Trend Assessment"
                                                showGradeEntryDialog = true
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = GhanaNavyPrimary),
                                            modifier = Modifier.testTag("trend_enter_grade_btn_${trend.studentId}")
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Input / Edit Grade", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (activeTeacherTab == 2) {
        // --- TAB 2: DAILY LESSON PLANS & MANAGEMENT REVIEW ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth().testTag("teacher_lesson_plan_header_card")
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Daily Lesson Plans",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = GhanaNavyPrimary
                                )
                                Text(
                                    text = "Upload & track daily pedagogical plans by subject and class for Management Review",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Button(
                                onClick = { showCreateLessonPlanDialog = true },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GhanaNavyPrimary),
                                modifier = Modifier.testTag("open_create_lesson_plan_dialog_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("New Plan", fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text("Filter by Subject:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("All", "Mathematics", "Integrated Science", "ICT").forEach { sub ->
                                FilterChip(
                                    selected = selectedPlanFilterSubject == sub,
                                    onClick = { selectedPlanFilterSubject = sub },
                                    label = { Text(sub, fontSize = 11.sp) },
                                    modifier = Modifier.testTag("filter_plan_subject_$sub")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text("Filter by Class:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("All", "JHS 2 - Gold", "Primary 6", "Primary 4").forEach { cls ->
                                FilterChip(
                                    selected = selectedPlanFilterClass == cls,
                                    onClick = { selectedPlanFilterClass = cls },
                                    label = { Text(cls, fontSize = 11.sp) },
                                    modifier = Modifier.testTag("filter_plan_class_$cls")
                                )
                            }
                        }
                    }
                }
            }

            val filteredPlans = allLessonPlans.filter { plan ->
                (selectedPlanFilterSubject == "All" || plan.subject.equals(selectedPlanFilterSubject, ignoreCase = true)) &&
                (selectedPlanFilterClass == "All" || plan.className.equals(selectedPlanFilterClass, ignoreCase = true))
            }

                if (filteredPlans.isEmpty()) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No lesson plans found for selected filters.", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                            Text("Click '+ New Plan' above to upload a daily lesson plan.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    filteredPlans.forEach { plan ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth().testTag("teacher_lesson_plan_item_${plan.id}")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = GhanaNavyPrimary.copy(alpha = 0.1f)
                                    ) {
                                        Text(
                                            text = plan.subject,
                                            fontWeight = FontWeight.Bold,
                                            color = GhanaNavyPrimary,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.secondaryContainer
                                    ) {
                                        Text(
                                            text = plan.className,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                val statusText = when (plan.status) {
                                    "APPROVED" -> "APPROVED"
                                    "REVISION_REQUESTED" -> "REVISION REQUESTED"
                                    else -> "PENDING REVIEW"
                                }
                                val statusBg = when (plan.status) {
                                    "APPROVED" -> Color(0xFFD1E7DD)
                                    "REVISION_REQUESTED" -> Color(0xFFF8D7DA)
                                    else -> Color(0xFFFFF3CD)
                                }
                                val statusColor = when (plan.status) {
                                    "APPROVED" -> Color(0xFF0F5132)
                                    "REVISION_REQUESTED" -> Color(0xFF842029)
                                    else -> Color(0xFF664D03)
                                }

                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = statusBg
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (plan.status == "APPROVED") Icons.Default.CheckCircle else if (plan.status == "REVISION_REQUESTED") Icons.Default.Error else Icons.Default.Schedule,
                                            contentDescription = null,
                                            tint = statusColor,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(statusText, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = statusColor)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = plan.topic,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold
                            )

                            if (plan.subTopic.isNotBlank()) {
                                Text(
                                    text = "Subtopic: ${plan.subTopic}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Text("📅 Date: ${plan.lessonDate}", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                Text("⏱️ Duration: ${plan.durationMinutes} mins", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            Spacer(modifier = Modifier.height(10.dp))

                            Text("🎯 Objectives:", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text(plan.objectives, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)

                            if (plan.teachingMaterials.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("📚 Materials & Lab Equipment:", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                Text(plan.teachingMaterials, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            if (plan.procedureSteps.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("📝 Procedure & Timeline:", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                Text(plan.procedureSteps, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            if (plan.managementFeedback.isNotBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (plan.status == "APPROVED") GhanaEmeraldGreen.copy(alpha = 0.12f) else Color(0xFFFFF3CD),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("💬 Management Feedback:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = GhanaNavyPrimary)
                                        Text(plan.managementFeedback, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

        if (activeTeacherTab == 3) {
            // ==========================================
            // WEEKLY CLASS TIMETABLE MANAGEMENT
            // ==========================================
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth().testTag("teacher_timetable_card")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "📅 Weekly Timetable Schedule",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = GhanaNavyPrimary
                                )
                                Text(
                                    text = "Manage subjects, periods & classroom assignments",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Button(
                                onClick = {
                                    editingSlot = null
                                    inputSlotClassName = timetableClassFilter
                                    inputSlotDayOfWeek = if (timetableDayFilter == "ALL") "Monday" else timetableDayFilter
                                    inputSlotPeriodNumber = "1"
                                    inputSlotStartTime = "08:00 AM"
                                    inputSlotEndTime = "08:45 AM"
                                    inputSlotSubject = "Mathematics"
                                    inputSlotTeacherName = "Mr. Emmanuel Mensah"
                                    inputSlotClassroom = "Block J2-A"
                                    showScheduleSlotDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GhanaNavyPrimary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("add_timetable_slot_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Slot", fontSize = 12.sp)
                            }
                        }

                        // Filter by Class
                        Text("Select Class:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("JHS 2 - Gold", "JHS 1", "Primary 4 - Harmony").forEach { className ->
                                FilterChip(
                                    selected = timetableClassFilter == className,
                                    onClick = { timetableClassFilter = className },
                                    label = { Text(className, fontSize = 11.sp) },
                                    modifier = Modifier.testTag("filter_class_$className")
                                )
                            }
                        }

                        // Filter by Day of Week
                        Text("Day of Week:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("ALL", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday").forEach { day ->
                                FilterChip(
                                    selected = timetableDayFilter == day,
                                    onClick = { timetableDayFilter = day },
                                    label = { Text(day, fontSize = 10.sp) },
                                    modifier = Modifier.testTag("filter_day_$day")
                                )
                            }
                        }
                    }
                }
            }

            // Timetable Slots List
            val filteredSlots = allTimetables.filter { slot ->
                (timetableClassFilter.isEmpty() || slot.className.equals(timetableClassFilter, ignoreCase = true) || slot.className.contains(timetableClassFilter, ignoreCase = true)) &&
                        (timetableDayFilter == "ALL" || slot.dayOfWeek.equals(timetableDayFilter, ignoreCase = true))
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

            if (filteredSlots.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.EventBusy, contentDescription = null, modifier = Modifier.size(36.dp), tint = Color.Gray)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No timetable slots found for $timetableClassFilter ($timetableDayFilter)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("Click 'Add Slot' above to schedule subjects for this class.", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                }
            } else {
                items(filteredSlots) { slot ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .testTag("timetable_slot_card_${slot.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
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
                                    text = "👨‍🏫 Teacher: ${slot.teacherName}  |  🏫 Room: ${slot.classroom}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row {
                                IconButton(
                                    onClick = {
                                        editingSlot = slot
                                        inputSlotClassName = slot.className
                                        inputSlotDayOfWeek = slot.dayOfWeek
                                        inputSlotPeriodNumber = slot.periodNumber.toString()
                                        inputSlotStartTime = slot.startTime
                                        inputSlotEndTime = slot.endTime
                                        inputSlotSubject = slot.subject
                                        inputSlotTeacherName = slot.teacherName
                                        inputSlotClassroom = slot.classroom
                                        showScheduleSlotDialog = true
                                    },
                                    modifier = Modifier.size(32.dp).testTag("edit_slot_${slot.id}")
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Slot", modifier = Modifier.size(16.dp), tint = GhanaNavyPrimary)
                                }
                                IconButton(
                                    onClick = { viewModel.deleteTimetableSlot(slot) },
                                    modifier = Modifier.size(32.dp).testTag("delete_slot_${slot.id}")
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Slot", modifier = Modifier.size(16.dp), tint = Color.Red)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (activeTeacherTab == 4) {
            // ==========================================
            // FEATURE 1: TEACHER LOAN REQUEST PORTAL
            // ==========================================
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth().testTag("teacher_loan_request_card")
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
                                Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = GhanaNavyPrimary)
                            }
                            Column {
                                Text("Teacher Salary Loan Request", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = GhanaNavyPrimary)
                                Text("Submit salary advance or loan application for Proprietor approval", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                        OutlinedTextField(
                            value = inputLoanAmount,
                            onValueChange = { inputLoanAmount = it },
                            label = { Text("Loan Amount requested (GH₵)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("input_loan_amount_field")
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = inputLoanDurationMonths,
                                onValueChange = { inputLoanDurationMonths = it },
                                label = { Text("Repayment Duration (Months)") },
                                singleLine = true,
                                modifier = Modifier.weight(1f).testTag("input_loan_duration_field")
                            )

                            OutlinedTextField(
                                value = inputLoanTerms,
                                onValueChange = { inputLoanTerms = it },
                                label = { Text("Repayment Terms") },
                                singleLine = true,
                                modifier = Modifier.weight(1.5f).testTag("input_loan_terms_field")
                            )
                        }

                        OutlinedTextField(
                            value = inputLoanReason,
                            onValueChange = { inputLoanReason = it },
                            label = { Text("Reason / Justification for Loan") },
                            minLines = 2,
                            modifier = Modifier.fillMaxWidth().testTag("input_loan_reason_field")
                        )

                        Button(
                            onClick = {
                                val amt = inputLoanAmount.toDoubleOrNull() ?: 1000.0
                                val duration = inputLoanDurationMonths.toIntOrNull() ?: 6
                                viewModel.submitTeacherLoanRequest(
                                    teacherId = 1,
                                    teacherName = "Mr. Kojo Mensah",
                                    amountGhc = amt,
                                    durationMonths = duration,
                                    terms = inputLoanTerms,
                                    reason = inputLoanReason
                                )
                                inputLoanReason = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GhanaNavyPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("submit_teacher_loan_button")
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Submit Salary Loan Application", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                Text(
                    text = "📋 Submitted Salary Loan Requests History",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = GhanaNavyPrimary,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            if (allTeacherLoanRequests.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "No loan requests submitted yet.",
                            fontSize = 13.sp,
                            modifier = Modifier.padding(20.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(allTeacherLoanRequests) { req ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth().testTag("teacher_loan_req_item_${req.id}")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text("GH₵ ${String.format("%.2f", req.amountGhc)}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = GhanaNavyPrimary)
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text("${req.repaymentDurationMonths} Months", fontSize = 11.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }

                                val (badgeBg, badgeText, textColor) = when (req.status) {
                                    "APPROVED" -> Triple(Color(0xFFD1E7DD), "APPROVED", Color(0xFF0F5132))
                                    "REJECTED" -> Triple(Color(0xFFF8D7DA), "DECLINED", Color(0xFF842029))
                                    else -> Triple(Color(0xFFFFF3CD), "PENDING APPROVAL", Color(0xFF664D03))
                                }

                                Surface(shape = RoundedCornerShape(8.dp), color = badgeBg) {
                                    Text(badgeText, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = textColor, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Terms: ${req.repaymentTerms}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text("Reason: ${req.reason}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Requested Date: ${req.requestedDate}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))

                            if (req.decisionNote.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("💬 Proprietor Note: ${req.decisionNote}", fontSize = 11.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(8.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        if (activeTeacherTab == 5) {
            // ==========================================
            // FEATURE 5: GUARDIAN-TEACHER COMMUNICATION
            // ==========================================
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth().testTag("teacher_parent_msg_composer_card")
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
                                Icon(Icons.Default.QuestionAnswer, contentDescription = null, tint = GhanaNavyPrimary)
                            }
                            Column {
                                Text("Guardian Direct Messaging", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = GhanaNavyPrimary)
                                Text("Communicate directly with student guardians regarding academic progress", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = inputMessageRecipient,
                                onValueChange = { inputMessageRecipient = it },
                                label = { Text("Guardian Name") },
                                singleLine = true,
                                modifier = Modifier.weight(1f).testTag("msg_recipient_field")
                            )
                            OutlinedTextField(
                                value = inputMessageChildName,
                                onValueChange = { inputMessageChildName = it },
                                label = { Text("Student/Child") },
                                singleLine = true,
                                modifier = Modifier.weight(1f).testTag("msg_child_name_field")
                            )
                        }

                        OutlinedTextField(
                            value = inputMessageSubject,
                            onValueChange = { inputMessageSubject = it },
                            label = { Text("Subject") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("msg_subject_field")
                        )

                        OutlinedTextField(
                            value = inputMessageBody,
                            onValueChange = { inputMessageBody = it },
                            label = { Text("Message Body") },
                            minLines = 3,
                            modifier = Modifier.fillMaxWidth().testTag("msg_body_field")
                        )

                        Button(
                            onClick = {
                                viewModel.sendDirectMessage(
                                    senderName = "Mr. Kojo Mensah (Class Teacher)",
                                    senderRole = "TEACHER",
                                    recipientName = inputMessageRecipient,
                                    recipientRole = "GUARDIAN",
                                    childName = inputMessageChildName,
                                    subject = inputMessageSubject,
                                    messageBody = inputMessageBody
                                )
                                inputMessageBody = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GhanaNavyPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("send_direct_message_button")
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Send Direct Message to Guardian", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                Text(
                    text = "💬 Message Conversation History",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = GhanaNavyPrimary,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            if (allDirectMessages.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "No messages yet.", fontSize = 13.sp, modifier = Modifier.padding(20.dp))
                    }
                }
            } else {
                items(allDirectMessages) { msg ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth().testTag("direct_msg_item_${msg.id}")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("From: ${msg.senderName}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (msg.senderRole == "TEACHER") GhanaNavyPrimary.copy(alpha = 0.1f) else GhanaEmeraldGreen.copy(alpha = 0.15f)
                                    ) {
                                        Text(msg.senderRole, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                    }
                                }
                                Text(msg.timestampString, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text("To: ${msg.recipientName}  |  Re: Student ${msg.childName}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                            Text("Subject: ${msg.subject}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(msg.messageBody, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }

        if (activeTeacherTab == 6) {
            // ==========================================
            // FEATURE 4: TEACHER PAY RECEIPT GENERATION
            // ==========================================
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth().testTag("teacher_pay_receipt_overview_card")
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
                                Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = GhanaNavyPrimary)
                            }
                            Column {
                                Text("Teacher Monthly Pay Receipt", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = GhanaNavyPrimary)
                                Text("Official monthly salary breakdown & printable digital receipt", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = GhanaNavyPrimary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Staff Code: AK-T01  |  Mr. Kojo Mensah", color = GhanaGoldAccent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Base Monthly Salary: GH₵ 2,800.00", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Text("Payment Method: MTN MoMo / Direct Bank Transfer", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                            }
                        }

                        Button(
                            onClick = { showDigitalPayReceiptDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = GhanaGoldAccent),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("view_digital_pay_receipt_button")
                        ) {
                            Icon(Icons.Default.Receipt, contentDescription = null, tint = GhanaNavyPrimary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Generate & Export Digital Pay Receipt", fontWeight = FontWeight.Bold, color = GhanaNavyPrimary)
                        }
                    }
                }
            }
        }

        if (activeTeacherTab == 7) {
            // ==========================================
            // FEATURE: TEACHER DIGITAL LIBRARY & RBAC RESOURCES
            // ==========================================
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth().testTag("teacher_digital_library_card")
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
                                    Icon(Icons.Default.MenuBook, contentDescription = null, tint = GhanaNavyPrimary)
                                }
                                Column {
                                    Text("Digital Library & Resources", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = GhanaNavyPrimary)
                                    Text("Granular Access Control • Class & Subject Textbooks", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            FilledIconButton(
                                onClick = { showUploadResourceDialog = true },
                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = GhanaNavyPrimary),
                                modifier = Modifier.size(40.dp).testTag("teacher_open_upload_resource_plus_button")
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

                        // Granular RBAC Notification Banner
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = GhanaGoldAccent.copy(alpha = 0.18f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = GhanaNavyPrimary, modifier = Modifier.size(18.dp))
                                Column {
                                    Text("🔒 Granular Role-Based Access Enforced", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = GhanaNavyPrimary)
                                    Text("Access granted for $teacherAssignedClass & $teacherAssignedSubject resources + School-wide history & syllabi.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }

                        // Category Filter Chips
                        val categories = listOf("ALL", "TEXTBOOK", "SYLLABUS", "CURRICULUM", "SCHOOL_HISTORY", "PROMOTIONAL_VIDEO")
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            categories.forEach { cat ->
                                FilterChip(
                                    selected = teacherLibCategoryFilter == cat,
                                    onClick = { teacherLibCategoryFilter = cat },
                                    label = { Text(cat.replace("_", " "), fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = GhanaNavyPrimary,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.testTag("teacher_lib_filter_${cat}")
                                )
                            }
                        }

                        val teacherAccessibleResources = allDigitalResources.filter { res ->
                            val classMatch = res.targetClass == "ALL" || res.targetClass == teacherAssignedClass
                            val subjectMatch = res.subject == "ALL" || res.subject == teacherAssignedSubject
                            val categoryMatch = res.category in listOf("SCHOOL_HISTORY", "PROMOTIONAL_VIDEO", "SCHOOL_MEDIA", "SYLLABUS", "CURRICULUM")
                            (classMatch && subjectMatch) || categoryMatch
                        }.filter { res ->
                            teacherLibCategoryFilter == "ALL" || res.category == teacherLibCategoryFilter
                        }

                        if (teacherAccessibleResources.isEmpty()) {
                            Text("No textbooks or resources match your assigned class/subject filter.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                teacherAccessibleResources.forEach { res ->
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                        modifier = Modifier.fillMaxWidth().testTag("teacher_resource_item_${res.id}")
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(res.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                    Text("Publisher: ${res.authorOrPublisher} • Format: ${res.fileFormat}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }

                                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    Button(
                                                        onClick = {
                                                            viewModel.downloadDigitalResourceMedia(res)
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = GhanaNavyPrimary),
                                                        shape = RoundedCornerShape(8.dp),
                                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                        modifier = Modifier.testTag("download_resource_${res.id}")
                                                    ) {
                                                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("Download", fontSize = 11.sp)
                                                    }

                                                    OutlinedButton(
                                                        onClick = { playingMediaResource = res },
                                                        shape = RoundedCornerShape(8.dp),
                                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                        modifier = Modifier.testTag("play_resource_${res.id}")
                                                    ) {
                                                        Icon(if (res.resourceType == "DOCUMENT") Icons.Default.MenuBook else Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(if (res.resourceType == "DOCUMENT") "Read in App" else "Play on App", fontSize = 11.sp)
                                                    }
                                                }
                                            }

                                            if (res.description.isNotBlank()) {
                                                Text(res.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                                            }

                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Surface(shape = RoundedCornerShape(4.dp), color = GhanaGoldAccent.copy(alpha = 0.2f)) {
                                                    Text("Category: ${res.category.replace("_", " ")}", fontSize = 9.sp, color = GhanaNavyPrimary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                                }
                                                Surface(shape = RoundedCornerShape(4.dp), color = GhanaEmeraldGreen.copy(alpha = 0.15f)) {
                                                    Text("Class: ${res.targetClass}", fontSize = 9.sp, color = Color(0xFF0F5132), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                                }
                                                Surface(shape = RoundedCornerShape(4.dp), color = GhanaNavyPrimary.copy(alpha = 0.1f)) {
                                                    Text("Subject: ${res.subject}", fontSize = 9.sp, color = GhanaNavyPrimary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
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

        if (activeTeacherTab == 8) {
            // ==========================================
            // FEATURE: TEACHER CLASS ASSIGNMENTS UPLOAD & TRACKING
            // ==========================================
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth().testTag("teacher_assignments_card")
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
                                    Icon(Icons.Default.Assignment, contentDescription = null, tint = GhanaNavyPrimary)
                                }
                                Column {
                                    Text("Class Assignments Upload", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = GhanaNavyPrimary)
                                    Text("Create, upload & assign homework for your class", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            Button(
                                onClick = { showUploadAssignmentDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = GhanaNavyPrimary),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("open_upload_assignment_dialog_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("New Task", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                        val myClassAssignments = allClassAssignments.filter { ass ->
                            ass.className == teacherAssignedClass || ass.subject == teacherAssignedSubject
                        }

                        if (myClassAssignments.isEmpty()) {
                            Text("No assignments currently posted for $teacherAssignedClass.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                myClassAssignments.forEach { ass ->
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                        modifier = Modifier.fillMaxWidth().testTag("teacher_assignment_item_${ass.id}")
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(ass.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                    Text("Class: ${ass.className} • Subject: ${ass.subject}", fontSize = 11.sp, color = GhanaNavyPrimary, fontWeight = FontWeight.SemiBold)
                                                }

                                                IconButton(
                                                    onClick = { viewModel.deleteClassAssignment(ass.id) },
                                                    modifier = Modifier.size(28.dp).testTag("delete_assignment_teacher_${ass.id}")
                                                ) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(16.dp))
                                                }
                                            }

                                            Text(ass.description, fontSize = 11.sp)
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Surface(shape = RoundedCornerShape(4.dp), color = GhanaGoldAccent.copy(alpha = 0.25f)) {
                                                    Text("Frequency: ${ass.frequencyPeriod}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = GhanaNavyPrimary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                                }
                                                Surface(shape = RoundedCornerShape(4.dp), color = GhanaNavyPrimary.copy(alpha = 0.1f)) {
                                                    Text("Due: ${ass.dueDateString}", fontSize = 9.sp, color = GhanaNavyPrimary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                                }
                                                Surface(shape = RoundedCornerShape(4.dp), color = GhanaEmeraldGreen.copy(alpha = 0.15f)) {
                                                    Text("Max Score: ${ass.maxScore} pts", fontSize = 9.sp, color = Color(0xFF0F5132), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
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

    // --- DIALOG: TEACHER UPLOAD DIGITAL RESOURCE ---
    if (showUploadResourceDialog) {
        UploadResourceDialog(
            userRole = "Teacher",
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
                    uploadedBy = "Teacher ($teacherAssignedSubject)"
                )
            }
        )
    }

    // --- DIALOG: TEACHER UPLOAD ASSIGNMENT ---
    if (showUploadAssignmentDialog) {
        AlertDialog(
            onDismissRequest = { showUploadAssignmentDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Assignment, contentDescription = null, tint = GhanaNavyPrimary)
                    Column {
                        Text("Post Class Assignment", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Dispatch homework / project to students & guardians", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = assignmentTitleInput,
                        onValueChange = { assignmentTitleInput = it },
                        label = { Text("Assignment Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("assignment_title_input")
                    )

                    OutlinedTextField(
                        value = assignmentDescInput,
                        onValueChange = { assignmentDescInput = it },
                        label = { Text("Instructions & Homework Details") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth().testTag("assignment_desc_input")
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = assignmentClassInput,
                            onValueChange = { assignmentClassInput = it },
                            label = { Text("Class") },
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("assignment_class_input")
                        )
                        OutlinedTextField(
                            value = assignmentSubjectInput,
                            onValueChange = { assignmentSubjectInput = it },
                            label = { Text("Subject") },
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("assignment_subject_input")
                        )
                    }

                    Column {
                        Text("Frequency Period:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("DAILY", "WEEKLY", "TERMLY").forEach { period ->
                                FilterChip(
                                    selected = assignmentPeriodInput == period,
                                    onClick = { assignmentPeriodInput = period },
                                    label = { Text(period, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                                )
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = assignmentDueDateInput,
                            onValueChange = { assignmentDueDateInput = it },
                            label = { Text("Due Date (YYYY-MM-DD)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("assignment_duedate_input")
                        )
                        OutlinedTextField(
                            value = assignmentMaxScoreInput,
                            onValueChange = { assignmentMaxScoreInput = it },
                            label = { Text("Max Score") },
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("assignment_maxscore_input")
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (assignmentTitleInput.isNotBlank() && assignmentDescInput.isNotBlank()) {
                            viewModel.uploadClassAssignment(
                                title = assignmentTitleInput,
                                description = assignmentDescInput,
                                className = assignmentClassInput,
                                subject = assignmentSubjectInput,
                                dueDateString = assignmentDueDateInput,
                                frequencyPeriod = assignmentPeriodInput,
                                maxScore = assignmentMaxScoreInput.toIntOrNull() ?: 100
                            )
                            showUploadAssignmentDialog = false
                            assignmentTitleInput = ""
                            assignmentDescInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GhanaNavyPrimary),
                    modifier = Modifier.testTag("submit_upload_assignment_button")
                ) {
                    Text("Post Assignment")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUploadAssignmentDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showDigitalPayReceiptDialog) {
        AlertDialog(
            onDismissRequest = { showDigitalPayReceiptDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = GhanaNavyPrimary)
                    Column {
                        Text("Official Staff Pay Advice", fontWeight = FontWeight.Bold, color = GhanaNavyPrimary, fontSize = 16.sp)
                        Text("St. Talafor Academy Payroll Office", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Receipt No: RCP-PAY-2026-07-001", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("Date Disbursed: 28 Jul 2026", fontSize = 11.sp)
                            Text("Employee: Mr. Kojo Mensah (Senior Lead Teacher)", fontSize = 11.sp)
                        }
                    }

                    HorizontalDivider()

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Base Monthly Salary:", fontSize = 12.sp)
                        Text("GH₵ 2,800.00", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("BECE Prep & ICT Allowance:", fontSize = 12.sp)
                        Text("+ GH₵ 250.00", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = GhanaEmeraldGreen)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Gross Payable:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("GH₵ 3,050.00", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    HorizontalDivider()

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("SSNIT Tier-1 Pension (5.5%):", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("- GH₵ 154.00", fontSize = 11.sp, color = Color.Red)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("PAYE Tax Withholding:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("- GH₵ 210.00", fontSize = 11.sp, color = Color.Red)
                    }

                    HorizontalDivider()

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = GhanaNavyPrimary,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("NET SALARY PAID:", color = GhanaGoldAccent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("GH₵ 2,686.00", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        android.widget.Toast.makeText(viewModel.context, "Pay receipt exported & sent to print preview!", android.widget.Toast.LENGTH_LONG).show()
                        showDigitalPayReceiptDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GhanaNavyPrimary)
                ) {
                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Print / Export Receipt")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDigitalPayReceiptDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showScheduleSlotDialog) {
        AlertDialog(
            onDismissRequest = { showScheduleSlotDialog = false },
            title = {
                Text(
                    if (editingSlot == null) "Schedule Subject Slot" else "Edit Schedule Slot",
                    fontWeight = FontWeight.Bold,
                    color = GhanaNavyPrimary
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = inputSlotClassName,
                        onValueChange = { inputSlotClassName = it },
                        label = { Text("Class Name") },
                        modifier = Modifier.fillMaxWidth().testTag("input_slot_class_name")
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = inputSlotDayOfWeek,
                            onValueChange = { inputSlotDayOfWeek = it },
                            label = { Text("Day of Week") },
                            modifier = Modifier.weight(1f).testTag("input_slot_day")
                        )
                        OutlinedTextField(
                            value = inputSlotPeriodNumber,
                            onValueChange = { inputSlotPeriodNumber = it },
                            label = { Text("Period #") },
                            modifier = Modifier.weight(0.8f).testTag("input_slot_period")
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = inputSlotStartTime,
                            onValueChange = { inputSlotStartTime = it },
                            label = { Text("Start Time") },
                            modifier = Modifier.weight(1f).testTag("input_slot_start")
                        )
                        OutlinedTextField(
                            value = inputSlotEndTime,
                            onValueChange = { inputSlotEndTime = it },
                            label = { Text("End Time") },
                            modifier = Modifier.weight(1f).testTag("input_slot_end")
                        )
                    }
                    OutlinedTextField(
                        value = inputSlotSubject,
                        onValueChange = { inputSlotSubject = it },
                        label = { Text("Subject Name") },
                        modifier = Modifier.fillMaxWidth().testTag("input_slot_subject")
                    )
                    OutlinedTextField(
                        value = inputSlotTeacherName,
                        onValueChange = { inputSlotTeacherName = it },
                        label = { Text("Assigned Teacher") },
                        modifier = Modifier.fillMaxWidth().testTag("input_slot_teacher")
                    )
                    OutlinedTextField(
                        value = inputSlotClassroom,
                        onValueChange = { inputSlotClassroom = it },
                        label = { Text("Classroom / Location") },
                        modifier = Modifier.fillMaxWidth().testTag("input_slot_room")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val periodNum = inputSlotPeriodNumber.toIntOrNull() ?: 1
                        viewModel.saveTimetableSlot(
                            className = inputSlotClassName.trim(),
                            dayOfWeek = inputSlotDayOfWeek.trim(),
                            periodNumber = periodNum,
                            startTime = inputSlotStartTime.trim(),
                            endTime = inputSlotEndTime.trim(),
                            subject = inputSlotSubject.trim(),
                            teacherName = inputSlotTeacherName.trim(),
                            classroom = inputSlotClassroom.trim(),
                            existingId = editingSlot?.id ?: 0L
                        )
                        showScheduleSlotDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GhanaNavyPrimary)
                ) {
                    Text(if (editingSlot == null) "Save Schedule Slot" else "Update Slot")
                }
            },
            dismissButton = {
                TextButton(onClick = { showScheduleSlotDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AttendanceRow(
    record: AttendanceRecord,
    weeklyAbsenceCount: Int,
    onToggleDay: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("attendance_row_${record.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(2.2f)) {
                Text(
                    text = record.studentName,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (record.isSynced) Color(0xFFD1E7DD) else Color(0xFFFFF3CD)
                    ) {
                        Text(
                            text = if (record.isSynced) "Synced" else "Local (Offline)",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (record.isSynced) Color(0xFF0F5132) else Color(0xFF856404),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }

            StatusCell(status = record.mondayStatus, onClick = { onToggleDay("Mon") }, modifier = Modifier.weight(1f))
            StatusCell(status = record.tuesdayStatus, onClick = { onToggleDay("Tue") }, modifier = Modifier.weight(1f))
            StatusCell(status = record.wednesdayStatus, onClick = { onToggleDay("Wed") }, modifier = Modifier.weight(1f))
            StatusCell(status = record.thursdayStatus, onClick = { onToggleDay("Thu") }, modifier = Modifier.weight(1f))
            StatusCell(status = record.fridayStatus, onClick = { onToggleDay("Fri") }, modifier = Modifier.weight(1f))

            // Automated Weekly Absenteeism Count Badge
            Box(
                modifier = Modifier.weight(1.2f),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (weeklyAbsenceCount > 1) Color(0xFFF8D7DA) else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "$weeklyAbsenceCount days",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (weeklyAbsenceCount > 1) Color(0xFF842029) else MaterialTheme.colorScheme.onSurface,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    }
}

@Composable
fun StatusCell(
    status: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (label, bgColor, textColor) = when (status) {
        "PRESENT" -> Triple("P", Color(0xFFD1E7DD), Color(0xFF0F5132))
        "ABSENT" -> Triple("A", Color(0xFFF8D7DA), Color(0xFF842029))
        "LATE" -> Triple("L", Color(0xFFFFF3CD), Color(0xFF664D03))
        else -> Triple("P", Color(0xFFD1E7DD), Color(0xFF0F5132))
    }

    Box(
        modifier = modifier
            .padding(2.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = bgColor,
            modifier = Modifier.size(28.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = textColor
                )
            }
        }
    }
}
