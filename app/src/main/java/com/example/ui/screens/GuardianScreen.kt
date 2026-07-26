package com.example.ui.screens

import androidx.compose.foundation.background
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
import com.example.data.model.DailyStudentAttendance
import com.example.data.model.FeeTransaction
import com.example.data.model.StudentGrade
import com.example.data.model.StudentLedger
import com.example.ui.components.MomoPaymentDialog
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
    val attendanceRecords by viewModel.currentStudentDailyAttendance.collectAsState()
    val studentGrades by viewModel.currentStudentGrades.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0: Financials, 1: Attendance Report, 2: Grades & Performance
    var attendanceFilter by remember { mutableStateOf("ALL") } // ALL, PRESENT, ABSENT, EXCUSED
    var showAbsenceNoticeDialog by remember { mutableStateOf(false) }
    var absenceDateInput by remember { mutableStateOf("2026-07-28") }
    var absenceReasonInput by remember { mutableStateOf("Medical / Dental Appointment") }

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

        // --- SECTION 2: NAVIGATION TAB ROW (Financials vs Attendance vs Grades) ---
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.primary,
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
                        text = "Payment Mini-Statement",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Official receipts & MoMo transaction log",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (transactions.isEmpty()) {
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
                items(transactions, key = { it.id }) { txn ->
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
                items(filteredList, key = { it.id }) { record ->
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
                items(studentGrades, key = { it.id }) { grade ->
                    SubjectGradeCard(grade = grade)
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
