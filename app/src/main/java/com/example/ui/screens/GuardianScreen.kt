package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ClassAssignment
import com.example.data.model.ClassTimetable
import com.example.data.model.DailyStudentAttendance
import com.example.data.model.DigitalResource
import com.example.data.model.FeeTransaction
import com.example.data.model.StudentFeePayment
import com.example.data.model.StudentGrade
import com.example.data.model.StudentLedger
import com.example.ui.components.LoadingOverlay
import com.example.ui.components.LoadingSpinner
import com.example.ui.components.MomoPaymentDialog
import com.example.ui.theme.GhanaEmeraldGreen
import com.example.ui.theme.GhanaGoldAccent
import com.example.ui.theme.GhanaNavyPrimary
import com.example.ui.viewmodel.SchoolViewModel

@Composable
fun GuardianScreen(
    viewModel: SchoolViewModel,
    modifier: Modifier = Modifier
) {
    val studentLedgers by viewModel.allStudentLedgers.collectAsState()
    val selectedStudentId by viewModel.selectedStudentId.collectAsState()
    val currentLedger by viewModel.currentStudentLedger.collectAsState()
    val transactions by viewModel.currentStudentTransactions.collectAsState()
    val feePayments by viewModel.currentStudentFeePayments.collectAsState()
    val attendanceRecords by viewModel.currentStudentDailyAttendance.collectAsState()
    val studentGrades by viewModel.currentStudentGrades.collectAsState()
    val allTimetables by viewModel.allTimetables.collectAsState()
    val allDirectMessages by viewModel.allDirectMessages.collectAsState()
    val allDigitalResources by viewModel.allDigitalResources.collectAsState()
    val allClassAssignments by viewModel.allClassAssignments.collectAsState()
    val uiLoadingState by viewModel.uiLoadingState.collectAsState()
    val loadingMessage by viewModel.loadingMessage.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0: Financials, 1: Attendance, 2: Grades, 3: Timetable, 4: Teacher Messaging, 5: Ward Textbooks, 6: Ward Assignments
    var guardianLibCategoryFilter by remember { mutableStateOf("ALL") }
    var guardianAssignmentPeriodFilter by remember { mutableStateOf("ALL") }
    var guardianTeacherRecipient by remember { mutableStateOf("Mr. Kojo Mensah (Class Teacher)") }
    var guardianMessageSubject by remember { mutableStateOf("Inquiry regarding homework & attendance") }
    var guardianMessageBody by remember { mutableStateOf("Good afternoon Mr. Mensah, I would like to confirm if Ama completed all homework assignments for this week.") }
    var guardianTimetableDayFilter by remember { mutableStateOf("ALL") }
    var attendanceFilter by remember { mutableStateOf("ALL") } // ALL, PRESENT, ABSENT, EXCUSED
    var showAbsenceNoticeDialog by remember { mutableStateOf(false) }
    var absenceDateInput by remember { mutableStateOf("2026-07-28") }
    var absenceReasonInput by remember { mutableStateOf("Medical / Dental Appointment") }

    var selectedFeePaymentForReceipt by remember { mutableStateOf<StudentFeePayment?>(null) }
    var showDigitalReceiptDialog by remember { mutableStateOf(false) }

    val showMomoDialog by viewModel.showMomoDialog.collectAsState()
    val momoNetwork by viewModel.momoNetwork.collectAsState()
    val momoPhone by viewModel.momoPhone.collectAsState()
    val momoAmount by viewModel.momoAmount.collectAsState()
    val momoReference by viewModel.momoReference.collectAsState()
    val isProcessingMomo by viewModel.isProcessingMomo.collectAsState()

    if (showMomoDialog) {
        MomoPaymentDialog(
            selectedNetwork = momoNetwork,
            phone = momoPhone,
            amount = momoAmount,
            reference = momoReference,
            isProcessing = isProcessingMomo,
            onNetworkSelected = { viewModel.setMomoNetwork(it) },
            onPhoneChange = { viewModel.setMomoPhone(it) },
            onAmountChange = { viewModel.setMomoAmount(it) },
            onReferenceChange = { viewModel.setMomoReference(it) },
            onSubmitPayment = { viewModel.submitMomoPayment() },
            onDismiss = { viewModel.closeMomoDialog() }
        )
    }

    if (showDigitalReceiptDialog && selectedFeePaymentForReceipt != null) {
        val r = selectedFeePaymentForReceipt!!
        AlertDialog(
            onDismissRequest = { showDigitalReceiptDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(GhanaNavyPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = GhanaGoldAccent, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text("Official Fee Receipt", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(r.receiptNumber, fontSize = 11.sp, color = GhanaNavyPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = GhanaNavyPrimary.copy(alpha = 0.06f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("AKOMA PRIMARY & JHS", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = GhanaNavyPrimary)
                            Text("Official Student Fee Payment Receipt", fontSize = 10.sp, color = Color.Gray)
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Student Name:", fontSize = 11.sp, color = Color.Gray)
                            Text(r.studentName, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Class:", fontSize = 11.sp, color = Color.Gray)
                            Text(r.className, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Fee Category:", fontSize = 11.sp, color = Color.Gray)
                            Text(r.feeCategory, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GhanaNavyPrimary)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Academic Term:", fontSize = 11.sp, color = Color.Gray)
                            Text(r.academicTerm, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Payment Date:", fontSize = 11.sp, color = Color.Gray)
                            Text(r.paymentDate, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Payment Method:", fontSize = 11.sp, color = Color.Gray)
                            Text(r.paymentMethod, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Transaction Ref:", fontSize = 11.sp, color = Color.Gray)
                            Text(r.transactionRef, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Recorded By:", fontSize = 11.sp, color = Color.Gray)
                            Text(r.recordedBy, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Amount Paid:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("GH₵ ${String.format("%.2f", r.amountPaidGhc)}", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2E7D32))
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Remaining Balance:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Text("GH₵ ${String.format("%.2f", r.remainingBalanceGhc)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GhanaGoldAccent)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.copyToClipboard("Fee Receipt ${r.receiptNumber}", "Official Receipt #${r.receiptNumber}\nStudent: ${r.studentName} (${r.className})\nAmount: GH₵ ${String.format("%.2f", r.amountPaidGhc)}\nCategory: ${r.feeCategory}\nRef: ${r.transactionRef}\nRemaining Balance: GH₵ ${String.format("%.2f", r.remainingBalanceGhc)}")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GhanaNavyPrimary)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy Receipt", fontSize = 11.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDigitalReceiptDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showAbsenceNoticeDialog) {
        AlertDialog(
            onDismissRequest = { showAbsenceNoticeDialog = false },
            icon = { Icon(Icons.Default.EventNote, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Submit Absence Notice", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Notify the class teacher in advance for your ward's absence.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = absenceDateInput,
                        onValueChange = { absenceDateInput = it },
                        label = { Text("Date (YYYY-MM-DD)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = absenceReasonInput,
                        onValueChange = { absenceReasonInput = it },
                        label = { Text("Reason for Absence") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.submitAbsenceNotice(absenceDateInput, absenceReasonInput)
                        showAbsenceNoticeDialog = false
                    },
                    modifier = Modifier.testTag("submit_absence_button")
                ) {
                    Text("Submit Notice")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAbsenceNoticeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        // --- GUARDIAN PORTAL GROUPED TASK HUB & DASHBOARD NAVIGATION ---
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("guardian_grouped_task_hub_card")
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
                                    text = "Guardian Portal Tasks",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = GhanaNavyPrimary
                                )
                                Text(
                                    text = "Parental task shortcuts & app navigation",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Button taking guardian to the App Dashboard
                        Button(
                            onClick = { viewModel.setViewMode(com.example.ui.viewmodel.ViewMode.HOME) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GhanaNavyPrimary),
                            modifier = Modifier.testTag("guardian_task_go_to_dashboard")
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
                            onClick = { viewModel.openMomoDialog() },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GhanaEmeraldGreen),
                            modifier = Modifier.weight(1f).testTag("guardian_task_pay_fees")
                        ) {
                            Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Pay Fees (MoMo)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { selectedTab = 2 },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GhanaNavyPrimary),
                            modifier = Modifier.weight(1f).testTag("guardian_task_terminal_report")
                        ) {
                            Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Report Card", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- SECTION 1: WARD SELECTOR (Guardian Wards) ---
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Select Student Ward:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        studentLedgers.forEach { ledger ->
                            val isSelected = ledger.studentId == selectedStudentId
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                ),
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("ward_card_${ledger.studentId}")
                                    .padding(vertical = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { viewModel.selectStudent(ledger.studentId) },
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column {
                                        Text(
                                            text = ledger.studentName,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "${ledger.className} • ${ledger.indexNumber}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- SECTION 2: NAVIGATION TAB ROW ---
        item {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.primary,
                edgePadding = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Fees & Financials", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null) },
                    modifier = Modifier.testTag("tab_financials")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Attendance", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                    modifier = Modifier.testTag("tab_attendance")
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Grades", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    icon = { Icon(Icons.Default.MilitaryTech, contentDescription = null) },
                    modifier = Modifier.testTag("tab_grades")
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("Timetable", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    icon = { Icon(Icons.Default.Schedule, contentDescription = null) },
                    modifier = Modifier.testTag("tab_timetable")
                )
                Tab(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    text = { Text("Teacher Messaging", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    icon = { Icon(Icons.Default.QuestionAnswer, contentDescription = null) },
                    modifier = Modifier.testTag("tab_teacher_messaging")
                )
                Tab(
                    selected = selectedTab == 5,
                    onClick = { selectedTab = 5 },
                    text = { Text("Ward Textbooks", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    icon = { Icon(Icons.Default.MenuBook, contentDescription = null) },
                    modifier = Modifier.testTag("tab_ward_textbooks")
                )
                Tab(
                    selected = selectedTab == 6,
                    onClick = { selectedTab = 6 },
                    text = { Text("Ward Assignments", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    icon = { Icon(Icons.Default.Assignment, contentDescription = null) },
                    modifier = Modifier.testTag("tab_ward_assignments")
                )
            }
        }

        if (selectedTab == 0) {
            // --- TAB 0: FINANCIAL LEDGER OVERVIEW ---
            currentLedger?.let { ledger ->
                item {
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = GhanaNavyPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("financial_ledger_card")
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column {
                                    Text(
                                        text = "Term 3 Financial Ledger",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = GhanaGoldAccent,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = ledger.studentName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (ledger.balanceGhc <= 0) Color(0xFFD1E7DD) else Color(0xFFFFF3CD)
                                ) {
                                    Text(
                                        text = if (ledger.balanceGhc <= 0) "CLEARED" else "BALANCE DUE",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (ledger.balanceGhc <= 0) Color(0xFF0F5132) else Color(0xFF664D03),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Total School Fees",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.LightGray
                                    )
                                    Text(
                                        text = "GH₵ ${String.format("%.2f", ledger.totalFeesGhc)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                Column {
                                    Text(
                                        text = "Amount Paid",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.LightGray
                                    )
                                    Text(
                                        text = "GH₵ ${String.format("%.2f", ledger.paidFeesGhc)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF81C784)
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Outstanding Balance",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.LightGray
                                    )
                                    Text(
                                        text = "GH₵ ${String.format("%.2f", ledger.balanceGhc)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = GhanaGoldAccent
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            val progress = if (ledger.totalFeesGhc > 0) (ledger.paidFeesGhc / ledger.totalFeesGhc).toFloat() else 1f
                            LinearProgressIndicator(
                                progress = { progress },
                                color = GhanaGoldAccent,
                                trackColor = Color(0xFF1E3A5F),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(CircleShape)
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            Button(
                                onClick = { viewModel.openMomoDialog(ledger.balanceGhc) },
                                enabled = ledger.balanceGhc > 0,
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GhanaGoldAccent, contentColor = GhanaNavyPrimary),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("pay_with_momo_button")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Payments,
                                        contentDescription = null,
                                        tint = GhanaNavyPrimary
                                    )
                                    Text(
                                        text = if (ledger.balanceGhc > 0) "Pay GH₵ ${String.format("%.2f", ledger.balanceGhc)} with MoMo" else "Fees Fully Settled (No Balance)",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "Itemized Fee Breakdown (Term 3)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            FeeBreakdownRow("Tuition & Academic Assessment", ledger.termTuitionGhc)
                            FeeBreakdownRow("PTA Infrastructure Levy", ledger.ptaLevyGhc)
                            FeeBreakdownRow("ICT Computer & Science Lab Fee", ledger.ictLabFeeGhc)
                            FeeBreakdownRow("Hot Meal & Feeding Support", ledger.feedingFeeGhc)

                            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Total Required",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "GH₵ ${String.format("%.2f", ledger.totalFeesGhc)}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Payment History & Official Receipts",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Official digital receipts & MoMo payment history",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (feePayments.isEmpty() && transactions.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No payment receipts recorded yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                itemsIndexed(feePayments, key = { index, fp -> "fp_${fp.id}_$index" }) { _, fp ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedFeePaymentForReceipt = fp
                                showDigitalReceiptDialog = true
                            }
                            .testTag("fee_payment_card_${fp.id}")
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
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(GhanaNavyPrimary.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ReceiptLong,
                                        contentDescription = null,
                                        tint = GhanaNavyPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = fp.feeCategory,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Surface(
                                            color = GhanaNavyPrimary,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = fp.receiptNumber,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "${fp.paymentMethod} • Ref: ${fp.transactionRef}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "${fp.paymentDate} • Rem Bal: GH₵ ${String.format("%.2f", fp.remainingBalanceGhc)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "GH₵ ${String.format("%.2f", fp.amountPaidGhc)}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF2E7D32)
                                )
                                Text(
                                    text = "View Receipt",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GhanaNavyPrimary
                                )
                            }
                        }
                    }
                }

                itemsIndexed(transactions, key = { index, txn -> "txn_${txn.id}_$index" }) { _, txn ->
                    ReceiptHistoryCard(transaction = txn)
                }
            }
        } else if (selectedTab == 1) {
            // --- TAB 1: DAILY ATTENDANCE REPORT VIEW FOR GUARDIAN ---
            val totalRecorded = attendanceRecords.size
            val presentCount = attendanceRecords.count { it.status == "PRESENT" || it.isPresent }
            val absentCount = attendanceRecords.count { it.status == "ABSENT" || it.isAbsent }
            val excusedCount = attendanceRecords.count { it.status == "EXCUSED" || it.isExcused }
            val ratePct = if (totalRecorded > 0) ((presentCount + excusedCount).toDouble() / totalRecorded * 100).toInt() else 100

            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Attendance Overview",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "$ratePct% Attendance Rate",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (ratePct >= 85) Color(0xFFD1E7DD) else Color(0xFFF8D7DA)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (ratePct >= 85) Icons.Default.CheckCircle else Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = if (ratePct >= 85) Color(0xFF0F5132) else Color(0xFF842029),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = if (ratePct >= 85) "Good Standing" else "Requires Attention",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (ratePct >= 85) Color(0xFF0F5132) else Color(0xFF842029)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatBox(label = "Total Days", value = "$totalRecorded", color = MaterialTheme.colorScheme.onPrimaryContainer)
                            StatBox(label = "Present", value = "$presentCount", color = Color(0xFF2E7D32))
                            StatBox(label = "Absent", value = "$absentCount", color = Color(0xFFC62828))
                            StatBox(label = "Excused", value = "$excusedCount", color = Color(0xFFE65100))
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { showAbsenceNoticeDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("submit_absence_notice_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Submit Absence Excuse Notice", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Filter Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("ALL", "PRESENT", "ABSENT", "EXCUSED").forEach { f ->
                        FilterChip(
                            selected = attendanceFilter == f,
                            onClick = { attendanceFilter = f },
                            label = { Text(f) },
                            modifier = Modifier.testTag("attendance_filter_$f")
                        )
                    }
                }
            }

            val filteredList = attendanceRecords.filter {
                when (attendanceFilter) {
                    "PRESENT" -> it.status == "PRESENT" || it.isPresent
                    "ABSENT" -> it.status == "ABSENT" || it.isAbsent
                    "EXCUSED" -> it.status == "EXCUSED" || it.isExcused
                    else -> true
                }
            }

            if (filteredList.isEmpty()) {
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
                            Text("No attendance records found for selected filter.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                itemsIndexed(filteredList, key = { index, record -> "att_${record.id}_$index" }) { _, record ->
                    AttendanceRecordCard(record = record)
                }
            }
        } else {
            // --- TAB 2: ACADEMIC GRADES & PERFORMANCE REPORT CARD ---
            val totalGrades = studentGrades.size
            val avgScore = if (totalGrades > 0) studentGrades.map { it.totalScore }.average() else 0.0
            val highestGrade = studentGrades.maxByOrNull { it.totalScore }
            val distinctionCount = studentGrades.count { it.gradeLetter == "A1" || it.gradeLetter == "B2" }

            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = GhanaNavyPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("academic_performance_summary_card")
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "ACADEMIC REPORT CARD",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = GhanaGoldAccent,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = currentLedger?.studentName ?: "Student",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "${currentLedger?.className ?: "Class"} • Term 1 Assessment",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.LightGray
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (avgScore >= 80) Color(0xFFD1E7DD) else Color(0xFFFFF3CD)
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = if (avgScore >= 80) "DISTINCTION" else "GOOD STANDING",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (avgScore >= 80) Color(0xFF0F5132) else Color(0xFF664D03)
                                    )
                                    Text(
                                        text = "${String.format("%.1f", avgScore)}% Avg",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (avgScore >= 80) Color(0xFF0F5132) else Color(0xFF664D03)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Subjects Graded", style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
                                Text("$totalGrades", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Distinctions (A1/B2)", style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
                                Text("$distinctionCount", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = GhanaGoldAccent)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Top Subject", style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
                                Text(highestGrade?.subject ?: "N/A", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Subject Academic Performance Breakdown",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (studentGrades.isEmpty()) {
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
                            Text("No official subject grades published yet for this term.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                itemsIndexed(studentGrades, key = { index, grade -> "grade_${grade.id}_$index" }) { _, grade ->
                    SubjectGradeCard(grade = grade)
                }
            }
        }

        if (selectedTab == 3) {
            // --- TAB 3: WARD'S CLASS TIMETABLE VIEW ---
            val wardClassName = currentLedger?.className ?: "JHS 2 - Gold"
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth().testTag("guardian_timetable_header")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "📅 ${currentLedger?.studentName ?: "Ward"}'s Timetable",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = GhanaNavyPrimary
                                )
                                Text(
                                    text = "Class: $wardClassName • Weekly Subject Schedule",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = GhanaEmeraldGreen.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "Active Schedule",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GhanaEmeraldGreen,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Day Filter Chips
                        Text("Select Day:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("ALL", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday").forEach { day ->
                                FilterChip(
                                    selected = guardianTimetableDayFilter == day,
                                    onClick = { guardianTimetableDayFilter = day },
                                    label = { Text(day, fontSize = 10.sp) },
                                    modifier = Modifier.testTag("guardian_filter_day_$day")
                                )
                            }
                        }
                    }
                }
            }

            val wardSlots = allTimetables.filter { slot ->
                (slot.className.equals(wardClassName, ignoreCase = true) || slot.className.contains(wardClassName, ignoreCase = true) || wardClassName.contains(slot.className, ignoreCase = true)) &&
                        (guardianTimetableDayFilter == "ALL" || slot.dayOfWeek.equals(guardianTimetableDayFilter, ignoreCase = true))
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

            if (wardSlots.isEmpty()) {
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
                            Text("No timetable schedule posted yet for $wardClassName ($guardianTimetableDayFilter)", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        }
                    }
                }
            } else {
                items(wardSlots) { slot ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .testTag("guardian_timetable_slot_${slot.id}")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
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
                                    text = "⏰ ${slot.startTime} - ${slot.endTime}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = slot.subject,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "👨‍🏫 Teacher: ${slot.teacherName}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                 Text(
                                    text = "🏫 ${slot.classroom}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = GhanaNavyPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        if (selectedTab == 4) {
            // ==========================================
            // FEATURE 5: GUARDIAN TO TEACHER MESSAGING
            // ==========================================
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth().testTag("guardian_teacher_messaging_card")
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
                                Text("Message Class Teacher", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = GhanaNavyPrimary)
                                Text("Direct communication regarding child academic welfare & homework", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                        OutlinedTextField(
                            value = guardianTeacherRecipient,
                            onValueChange = { guardianTeacherRecipient = it },
                            label = { Text("Teacher Name / Role") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("guardian_msg_teacher_field")
                        )

                        OutlinedTextField(
                            value = guardianMessageSubject,
                            onValueChange = { guardianMessageSubject = it },
                            label = { Text("Subject") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("guardian_msg_subject_field")
                        )

                        OutlinedTextField(
                            value = guardianMessageBody,
                            onValueChange = { guardianMessageBody = it },
                            label = { Text("Message Body") },
                            minLines = 3,
                            modifier = Modifier.fillMaxWidth().testTag("guardian_msg_body_field")
                        )

                        Button(
                            onClick = {
                                viewModel.sendDirectMessage(
                                    senderName = "Mrs. Grace Mensah (Guardian)",
                                    senderRole = "GUARDIAN",
                                    recipientName = guardianTeacherRecipient,
                                    recipientRole = "TEACHER",
                                    childName = currentLedger?.studentName ?: "Ama Serwaa Mensah",
                                    subject = guardianMessageSubject,
                                    messageBody = guardianMessageBody
                                )
                                guardianMessageBody = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GhanaNavyPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("guardian_send_message_button")
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Send Direct Message to Teacher", fontWeight = FontWeight.Bold)
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
                        Text(text = "No messages exchanged yet.", fontSize = 13.sp, modifier = Modifier.padding(20.dp))
                    }
                }
            } else {
                items(allDirectMessages) { msg ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth().testTag("guardian_msg_item_${msg.id}")
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
                                        color = if (msg.senderRole == "GUARDIAN") GhanaEmeraldGreen.copy(alpha = 0.15f) else GhanaNavyPrimary.copy(alpha = 0.1f)
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

        if (selectedTab == 5) {
            // ==========================================
            // FEATURE: WARD TEXTBOOKS & DIGITAL LIBRARY (GUARDIAN RBAC)
            // ==========================================
            val wardName = currentLedger?.studentName ?: "Ama Serwaa Mensah"
            val wardClass = currentLedger?.className ?: "JHS 2 - Gold"

            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth().testTag("guardian_ward_textbooks_card")
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
                                Icon(Icons.Default.MenuBook, contentDescription = null, tint = GhanaNavyPrimary)
                            }
                            Column {
                                Text("Ward Digital Library & Textbooks", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = GhanaNavyPrimary)
                                Text("Official syllabus books & learning resources for $wardName", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                                    Text("Displaying learning resources & textbooks strictly for ward: $wardName ($wardClass).", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }

                        // Category Filter Chips
                        val categories = listOf("ALL", "TEXTBOOK", "SYLLABUS", "CURRICULUM", "SCHOOL_HISTORY")
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            categories.forEach { cat ->
                                FilterChip(
                                    selected = guardianLibCategoryFilter == cat,
                                    onClick = { guardianLibCategoryFilter = cat },
                                    label = { Text(cat.replace("_", " "), fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = GhanaNavyPrimary,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.testTag("guardian_lib_filter_${cat}")
                                )
                            }
                        }

                        val wardAccessibleResources = allDigitalResources.filter { res ->
                            (res.targetClass == "ALL" || res.targetClass == wardClass) &&
                            res.targetAudience in listOf("ALL", "GUARDIANS_ONLY")
                        }.filter { res ->
                            guardianLibCategoryFilter == "ALL" || res.category == guardianLibCategoryFilter
                        }

                        if (wardAccessibleResources.isEmpty()) {
                            Text("No textbooks or learning materials posted for $wardClass yet.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                wardAccessibleResources.forEach { res ->
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                        modifier = Modifier.fillMaxWidth().testTag("guardian_resource_item_${res.id}")
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

                                                Button(
                                                    onClick = {
                                                        viewModel.triggerPortalDataRefresh("Downloading '${res.title}' for $wardName...")
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = GhanaNavyPrimary),
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                    modifier = Modifier.testTag("download_ward_resource_${res.id}")
                                                ) {
                                                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Download", fontSize = 11.sp)
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

        if (selectedTab == 6) {
            // ==========================================
            // FEATURE: WARD HOMEWORK & ASSIGNMENT TRACKING
            // ==========================================
            val wardName = currentLedger?.studentName ?: "Ama Serwaa Mensah"
            val wardClass = currentLedger?.className ?: "JHS 2 - Gold"

            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth().testTag("guardian_ward_assignments_card")
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
                                Icon(Icons.Default.Assignment, contentDescription = null, tint = GhanaNavyPrimary)
                            }
                            Column {
                                Text("Ward Homework & Assignments", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = GhanaNavyPrimary)
                                Text("Track homework assigned to $wardName ($wardClass)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                        // Frequency Period Filters
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("Filter Period:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            listOf("ALL", "DAILY", "WEEKLY", "TERMLY").forEach { period ->
                                FilterChip(
                                    selected = guardianAssignmentPeriodFilter == period,
                                    onClick = { guardianAssignmentPeriodFilter = period },
                                    label = { Text(period, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = GhanaNavyPrimary,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.testTag("guardian_assignment_filter_${period}")
                                )
                            }
                        }

                        val wardAssignments = allClassAssignments.filter { ass ->
                            ass.className == wardClass
                        }.filter { ass ->
                            guardianAssignmentPeriodFilter == "ALL" || ass.frequencyPeriod == guardianAssignmentPeriodFilter
                        }

                        if (wardAssignments.isEmpty()) {
                            Text("No pending homework assignments found for $wardClass in this period.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                wardAssignments.forEach { ass ->
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                        modifier = Modifier.fillMaxWidth().testTag("guardian_assignment_item_${ass.id}")
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(ass.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                    Text("Subject: ${ass.subject} • Teacher: ${ass.teacherName}", fontSize = 11.sp, color = GhanaNavyPrimary, fontWeight = FontWeight.SemiBold)
                                                }

                                                Button(
                                                    onClick = {
                                                        viewModel.triggerPortalDataRefresh("Downloading assignment attachment for '${ass.title}'...")
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = GhanaNavyPrimary),
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                    modifier = Modifier.testTag("download_assignment_attachment_${ass.id}")
                                                ) {
                                                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Attachment", fontSize = 10.sp)
                                                }
                                            }

                                            Text(ass.description, fontSize = 11.sp)
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Surface(shape = RoundedCornerShape(4.dp), color = GhanaGoldAccent.copy(alpha = 0.25f)) {
                                                    Text("Period: ${ass.frequencyPeriod}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = GhanaNavyPrimary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
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
}

@Composable
private fun SubjectGradeCard(grade: StudentGrade) {
    val (badgeBg, badgeText) = when (grade.gradeLetter) {
        "A1" -> Pair(Color(0xFFD1E7DD), Color(0xFF0F5132))
        "B2", "B3" -> Pair(Color(0xFFCFF4FC), Color(0xFF055160))
        "C4", "C5", "C6" -> Pair(Color(0xFFFFF3CD), Color(0xFF664D03))
        else -> Pair(Color(0xFFF8D7DA), Color(0xFF842029))
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("subject_grade_card_${grade.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = grade.subject,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${grade.academicTerm} (${grade.academicYear})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = badgeBg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = grade.gradeLetter,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = badgeText
                        )
                        Text(
                            text = "(${String.format("%.1f", grade.totalScore)}%)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = badgeText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Continuous Assessment (Class): ${String.format("%.1f", grade.classScore)} / 30",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Final Exam: ${String.format("%.1f", grade.examScore)} / 70",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { (grade.totalScore / 100.0).toFloat().coerceIn(0f, 1f) },
                color = badgeText,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
            )

            if (grade.remarks.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Comment,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Teacher Remark: \"${grade.remarks}\"",
                            style = MaterialTheme.typography.bodySmall,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatBox(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = color)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun AttendanceRecordCard(record: DailyStudentAttendance) {
    val statusColor = when (record.status) {
        "PRESENT" -> Color(0xFF2E7D32)
        "ABSENT" -> Color(0xFFC62828)
        "EXCUSED" -> Color(0xFFE65100)
        else -> MaterialTheme.colorScheme.primary
    }

    val statusBg = when (record.status) {
        "PRESENT" -> Color(0xFFD1E7DD)
        "ABSENT" -> Color(0xFFF8D7DA)
        "EXCUSED" -> Color(0xFFFFF3CD)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val icon = when (record.status) {
        "PRESENT" -> Icons.Default.CheckCircle
        "ABSENT" -> Icons.Default.Cancel
        "EXCUSED" -> Icons.Default.Info
        else -> Icons.Default.Help
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("attendance_card_${record.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(statusBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column {
                    Text(
                        text = record.dateString,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = record.className,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (record.remarks.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = record.remarks,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = statusBg
            ) {
                Text(
                    text = record.status,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = statusColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun FeeBreakdownRow(title: String, amount: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "GH₵ ${String.format("%.2f", amount)}",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ReceiptHistoryCard(transaction: FeeTransaction) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("receipt_card_${transaction.id}")
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
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = transaction.description,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${transaction.paymentMethod} • ${transaction.transactionRef}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = transaction.dateString,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "+ GH₵ ${String.format("%.2f", transaction.amountGhc)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF2E7D32)
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFD1E7DD)
                ) {
                    Text(
                        text = transaction.status,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F5132),
                        fontSize = 9.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
