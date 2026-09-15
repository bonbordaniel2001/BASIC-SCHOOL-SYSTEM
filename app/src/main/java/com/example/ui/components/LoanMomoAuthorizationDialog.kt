package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.TeacherLoanRequest
import com.example.ui.theme.GhanaEmeraldGreen
import com.example.ui.theme.GhanaGoldAccent
import com.example.ui.theme.GhanaNavyPrimary

@Composable
fun LoanMomoAuthorizationDialog(
    loan: TeacherLoanRequest,
    onConfirmDisbursement: (network: String, phone: String, reference: String, pin: String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedNetwork by remember { mutableStateOf("MTN Mobile Money") }
    var recipientPhone by remember { mutableStateOf("0244123456") }
    var proprietorPin by remember { mutableStateOf("") }
    var isPinVisible by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    
    // 0: MoMo USSD Console Mode, 1: Direct Authorization Form
    var activeConsoleTab by remember { mutableStateOf(0) }
    var ussdStep by remember { mutableStateOf(1) } // 1: Initial Prompt, 2: Pin Entry, 3: Success
    var consoleInputText by remember { mutableStateOf("") }

    val shortcode = when (selectedNetwork) {
        "Telecel Cash" -> "*110#"
        "AT Money" -> "*110#"
        else -> "*170#"
    }

    Dialog(onDismissRequest = { if (!isProcessing) onDismiss() }) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp)
                .testTag("loan_momo_authorization_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
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
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFCC00)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Payments,
                                contentDescription = null,
                                tint = Color(0xFF1E1E1E),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "MoMo Loan Console",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = GhanaNavyPrimary
                            )
                            Text(
                                text = "Authorise Staff Loan Payout",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_loan_momo_dialog")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Loan Details Summary Card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = GhanaGoldAccent.copy(alpha = 0.12f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = loan.teacherName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = GhanaNavyPrimary
                            )
                            Text(
                                text = "GH₵ ${String.format("%.2f", loan.amountGhc)}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = GhanaEmeraldGreen
                            )
                        }
                        Text(
                            text = "Purpose: ${loan.reason}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Terms: ${loan.repaymentDurationMonths} Months (${loan.repaymentTerms})",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Console / Form Tab Switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (activeConsoleTab == 0) GhanaNavyPrimary else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { activeConsoleTab = 0 }
                            .testTag("loan_tab_console")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Terminal,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (activeConsoleTab == 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "USSD Console",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (activeConsoleTab == 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (activeConsoleTab == 1) GhanaNavyPrimary else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { activeConsoleTab = 1 }
                            .testTag("loan_tab_form")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Assignment,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (activeConsoleTab == 1) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Direct Form",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (activeConsoleTab == 1) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Network Selection
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Disbursement Network",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("MTN Mobile Money", "Telecel Cash", "AT Money").forEach { net ->
                            val isSelected = selectedNetwork == net
                            val chipColor = when (net) {
                                "MTN Mobile Money" -> Color(0xFFFFCC00)
                                "Telecel Cash" -> Color(0xFFE60000)
                                else -> Color(0xFF0055AA)
                            }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) chipColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, chipColor) else null,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        selectedNetwork = net
                                        ussdStep = 1
                                    }
                                    .padding(vertical = 4.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = net.split(" ").first(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (isSelected) GhanaNavyPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = when (net) {
                                            "MTN Mobile Money" -> "*170#"
                                            else -> "*110#"
                                        },
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }

                // Teacher MoMo Phone Number
                OutlinedTextField(
                    value = recipientPhone,
                    onValueChange = { recipientPhone = it },
                    label = { Text("Teacher MoMo Phone Number") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = GhanaNavyPrimary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("loan_momo_phone_input")
                )

                if (activeConsoleTab == 0) {
                    // ==========================================
                    // MODE 0: INTERACTIVE MoMo SYSTEM CONSOLE
                    // ==========================================
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0D1B2A))
                            .border(1.dp, Color(0xFF335C67), RoundedCornerShape(12.dp))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF00FF66)))
                                Text(
                                    text = "$selectedNetwork System Console ($shortcode)",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00FF66)
                                )
                            }
                            Text(
                                text = "LIVE SIMULATION",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                color = Color.Yellow
                            )
                        }

                        Divider(color = Color(0xFF335C67), thickness = 0.5.dp)

                        when (ussdStep) {
                            1 -> {
                                Text(
                                    text = ">> DIALED: $shortcode\n>> SESSION OPENED\n\nAUTHORISE SALARY LOAN DISBURSEMENT:\n1. Payout GH₵ ${String.format("%.2f", loan.amountGhc)} to ${loan.teacherName} ($recipientPhone)\n2. Reference: LOAN-${loan.id}\n3. School Wallet: ST. TALAFOR INT SCH\n\nPress 'Proceed' to enter Proprietor Authorization PIN.",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = Color(0xFFE0E1DD),
                                    lineHeight = 16.sp
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { ussdStep = 2 },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF66)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f).testTag("loan_console_proceed_btn")
                                    ) {
                                        Text("Proceed to PIN", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            try {
                                                val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                                                    data = Uri.parse("tel:" + Uri.encode(shortcode))
                                                }
                                                context.startActivity(dialIntent)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Cannot launch dialer: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFCC00)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Dial Phone USSD", fontSize = 10.sp)
                                    }
                                }
                            }

                            2 -> {
                                Text(
                                    text = ">> ST. TALAFOR WALLET AUTHORISATION\n>> Pay GH₵ ${String.format("%.2f", loan.amountGhc)} to ${loan.teacherName}?\n>> Fee: GH₵ 0.00\n>> Enter 4-digit Proprietor MoMo PIN:",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = Color(0xFF00FF66),
                                    lineHeight = 16.sp
                                )

                                OutlinedTextField(
                                    value = proprietorPin,
                                    onValueChange = { if (it.length <= 4) proprietorPin = it },
                                    label = { Text("Proprietor PIN", color = Color.LightGray) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                    visualTransformation = PasswordVisualTransformation(),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = Color(0xFF00FF66),
                                        unfocusedBorderColor = Color.Gray
                                    ),
                                    modifier = Modifier.fillMaxWidth().testTag("loan_console_pin_input")
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            if (proprietorPin.length >= 4) {
                                                isProcessing = true
                                                onConfirmDisbursement(selectedNetwork, recipientPhone, "LOAN-${loan.id}", proprietorPin)
                                            } else {
                                                Toast.makeText(context, "Please enter 4-digit PIN", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF66)),
                                        shape = RoundedCornerShape(8.dp),
                                        enabled = !isProcessing && proprietorPin.length >= 4,
                                        modifier = Modifier.weight(1f).testTag("loan_console_authorise_btn")
                                    ) {
                                        if (isProcessing) {
                                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.Black)
                                        } else {
                                            Text("Authorise & Payout", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        }
                                    }

                                    OutlinedButton(
                                        onClick = { ussdStep = 1 },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                                    ) {
                                        Text("Back", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // ==========================================
                    // MODE 1: DIRECT AUTHORIZATION FORM
                    // ==========================================
                    OutlinedTextField(
                        value = "LOAN-${loan.id}-${loan.teacherName.replace(" ", "_")}",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Payment Reference") },
                        leadingIcon = { Icon(Icons.Default.Tag, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = proprietorPin,
                        onValueChange = { if (it.length <= 4) proprietorPin = it },
                        label = { Text("Proprietor MoMo Wallet PIN") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = GhanaNavyPrimary) },
                        trailingIcon = {
                            IconButton(onClick = { isPinVisible = !isPinVisible }) {
                                Icon(
                                    imageVector = if (isPinVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle PIN"
                                )
                            }
                        },
                        visualTransformation = if (isPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("loan_form_pin_input")
                    )

                    Button(
                        onClick = {
                            if (recipientPhone.isBlank()) {
                                Toast.makeText(context, "Please enter recipient phone number", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            isProcessing = true
                            onConfirmDisbursement(selectedNetwork, recipientPhone, "LOAN-${loan.id}", proprietorPin)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GhanaEmeraldGreen),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isProcessing,
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("loan_direct_authorise_btn")
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                        } else {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Authorise GH₵ ${String.format("%.2f", loan.amountGhc)} Loan Payout", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
