package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.StudentLedger
import com.example.ui.theme.GhanaEmeraldGreen
import com.example.ui.theme.GhanaGoldAccent
import com.example.ui.theme.GhanaNavyPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Dialog allowing the Proprietor to receive fee payments from guardians
 * filtered by Student Name and Class. Automatically generates an official downloadable receipt.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiveGuardianPaymentDialog(
    allStudentLedgers: List<StudentLedger>,
    onDismiss: () -> Unit,
    onSubmitPayment: (
        studentId: Long,
        studentName: String,
        className: String,
        guardianName: String,
        amountGhc: Double,
        paymentDate: String,
        paymentMethod: String,
        feeCategory: String,
        notes: String
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val classList = remember(allStudentLedgers) {
        listOf("ALL") + allStudentLedgers.map { it.className }.distinct().filter { it.isNotBlank() }.sorted()
    }
    var selectedClassFilter by remember { mutableStateOf("ALL") }
    var studentSearchQuery by remember { mutableStateOf("") }
    var selectedStudentLedger by remember { mutableStateOf<StudentLedger?>(null) }

    val todayFormatted = remember {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
    }
    var paymentDateInput by remember { mutableStateOf(todayFormatted) }
    var amountInput by remember { mutableStateOf("") }
    var selectedPaymentMethod by remember { mutableStateOf("Cash at Bursar") }
    var selectedFeeCategory by remember { mutableStateOf("Tuition & School Fees") }
    var notesInput by remember { mutableStateOf("") }

    val paymentMethods = listOf("Cash at Bursar", "MTN MoMo", "Telecel Cash", "Bank Transfer", "Cheque")
    val feeCategories = listOf("Tuition & School Fees", "Feeding / Canteen", "PTA Levy", "ICT Lab Fee", "Examination Fee")

    val filteredLedgers = remember(allStudentLedgers, selectedClassFilter, studentSearchQuery) {
        allStudentLedgers.filter { ledger ->
            (selectedClassFilter == "ALL" || ledger.className.equals(selectedClassFilter, ignoreCase = true)) &&
            (studentSearchQuery.isBlank() || ledger.studentName.contains(studentSearchQuery, ignoreCase = true) || ledger.guardianName.contains(studentSearchQuery, ignoreCase = true))
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = modifier
                .fillMaxWidth(0.94f)
                .wrapContentHeight()
                .padding(vertical = 16.dp)
                .testTag("receive_guardian_payment_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
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
                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = GhanaGoldAccent)
                        }
                        Column {
                            Text(
                                text = "Receive Guardian Payment",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = GhanaNavyPrimary
                            )
                            Text(
                                text = "Filter by Class & Student Name",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // STEP 1: CLASS SELECTION CHIPS
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("1. Filter by Class:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = GhanaNavyPrimary)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        classList.forEach { cls ->
                            FilterChip(
                                selected = selectedClassFilter == cls,
                                onClick = {
                                    selectedClassFilter = cls
                                    selectedStudentLedger = null
                                },
                                label = { Text(cls, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GhanaNavyPrimary,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag("filter_class_chip_$cls")
                            )
                        }
                    }
                }

                // STEP 2: SEARCH & SELECT STUDENT BY NAME
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("2. Select Student by Name:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = GhanaNavyPrimary)
                    OutlinedTextField(
                        value = studentSearchQuery,
                        onValueChange = { studentSearchQuery = it },
                        placeholder = { Text("Search student or guardian name...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth().testTag("student_search_input")
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        modifier = Modifier.fillMaxWidth().heightIn(max = 160.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(6.dp).verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (filteredLedgers.isEmpty()) {
                                Text("No students found in $selectedClassFilter", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(8.dp))
                            } else {
                                filteredLedgers.forEach { ledger ->
                                    val isSelected = selectedStudentLedger?.studentId == ledger.studentId
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) GhanaNavyPrimary.copy(alpha = 0.15f) else Color.Transparent,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedStudentLedger = ledger
                                                if (amountInput.isBlank() && ledger.balanceGhc > 0) {
                                                    amountInput = ledger.balanceGhc.toInt().toString()
                                                }
                                            }
                                            .padding(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(ledger.studentName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                Text("Class: ${ledger.className} • Guardian: ${ledger.guardianName}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text("Bal: GH₵ ${String.format("%.2f", ledger.balanceGhc)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (ledger.balanceGhc > 0) Color(0xFFD84315) else Color(0xFF2E7D32))
                                                if (isSelected) {
                                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GhanaNavyPrimary, modifier = Modifier.size(14.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // STEP 3: PAYMENT INPUTS IF STUDENT SELECTED
                if (selectedStudentLedger != null) {
                    val ledger = selectedStudentLedger!!
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = GhanaEmeraldGreen.copy(alpha = 0.08f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GhanaEmeraldGreen.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Selected Student: ${ledger.studentName}", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = GhanaNavyPrimary)
                                    Text("Class: ${ledger.className} • Guardian: ${ledger.guardianName} (${ledger.guardianPhone})", fontSize = 11.sp)
                                }
                                Text(
                                    "Balance: GH₵ ${String.format("%.2f", ledger.balanceGhc)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD84315)
                                )
                            }
                        }
                    }

                    // Payment Amount Input
                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { amountInput = it },
                        label = { Text("Amount to Receive (GH₵)") },
                        leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth().testTag("receive_amount_input")
                    )

                    // Payment Date Input (Allowed to add date during payment)
                    OutlinedTextField(
                        value = paymentDateInput,
                        onValueChange = { paymentDateInput = it },
                        label = { Text("Payment Date") },
                        leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth().testTag("receive_payment_date_input")
                    )

                    // Payment Method Chips
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Payment Method:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            paymentMethods.forEach { method ->
                                FilterChip(
                                    selected = selectedPaymentMethod == method,
                                    onClick = { selectedPaymentMethod = method },
                                    label = { Text(method, fontSize = 10.sp) }
                                )
                            }
                        }
                    }

                    // Fee Category Chips
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Fee Category:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            feeCategories.forEach { cat ->
                                FilterChip(
                                    selected = selectedFeeCategory == cat,
                                    onClick = { selectedFeeCategory = cat },
                                    label = { Text(cat, fontSize = 10.sp) }
                                )
                            }
                        }
                    }

                    // Notes / Reference Memo
                    OutlinedTextField(
                        value = notesInput,
                        onValueChange = { notesInput = it },
                        label = { Text("Bursary Notes / Reference (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Action: Receive & Generate Receipt
                    Button(
                        onClick = {
                            val amt = amountInput.toDoubleOrNull() ?: 0.0
                            if (amt <= 0) {
                                Toast.makeText(context, "Please enter a valid amount in GH₵", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            onSubmitPayment(
                                ledger.studentId,
                                ledger.studentName,
                                ledger.className,
                                ledger.guardianName,
                                amt,
                                if (paymentDateInput.isNotBlank()) paymentDateInput else todayFormatted,
                                selectedPaymentMethod,
                                selectedFeeCategory,
                                notesInput
                            )
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GhanaNavyPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("confirm_receive_payment_button")
                    ) {
                        Icon(Icons.Default.Receipt, contentDescription = null, tint = GhanaGoldAccent)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Receive GH₵ ${amountInput.ifBlank { "0.00" }} & Generate Receipt", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
