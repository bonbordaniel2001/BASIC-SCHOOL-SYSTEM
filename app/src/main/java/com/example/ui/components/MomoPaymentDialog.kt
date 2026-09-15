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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import com.example.ui.theme.GhanaEmeraldGreen
import com.example.ui.theme.GhanaGoldAccent
import com.example.ui.theme.GhanaNavyPrimary

@Composable
fun MomoPaymentDialog(
    selectedNetwork: String,
    phone: String,
    amount: String,
    reference: String,
    isProcessing: Boolean,
    onNetworkSelected: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onReferenceChange: (String) -> Unit,
    pin: String = "",
    onPinChange: (String) -> Unit = {},
    paymentDate: String = "",
    onPaymentDateChange: (String) -> Unit = {},
    onSubmitPayment: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var isPinVisible by remember { mutableStateOf(false) }
    // 0: Form Checkout, 1: MoMo Console & USSD Terminal
    var activePaymentMode by remember { mutableStateOf(0) }
    var ussdTerminalStep by remember { mutableStateOf(1) } // 1: Main Menu, 2: School Fees Prompt, 3: PIN Confirmation
    var consoleInputText by remember { mutableStateOf("") }

    val ussdShortcode = when (selectedNetwork) {
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
                .testTag("momo_payment_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
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
                                tint = Color.Black,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Mobile Money Payment",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Access MoMo Console & Authorize Payment",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (!isProcessing) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("close_momo_dialog")
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Payment Mode Selector Tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = activePaymentMode == 0,
                        onClick = { activePaymentMode = 0 },
                        label = { Text("Direct MoMo", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        leadingIcon = { Icon(Icons.Default.PhoneAndroid, contentDescription = null, modifier = Modifier.size(14.dp)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GhanaNavyPrimary,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.weight(1f).testTag("tab_direct_momo")
                    )

                    FilterChip(
                        selected = activePaymentMode == 1,
                        onClick = { activePaymentMode = 1 },
                        label = { Text("MoMo Console ($ussdShortcode)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        leadingIcon = { Icon(Icons.Default.Terminal, contentDescription = null, modifier = Modifier.size(14.dp)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GhanaGoldAccent,
                            selectedLabelColor = GhanaNavyPrimary
                        ),
                        modifier = Modifier.weight(1.2f).testTag("tab_momo_console")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (isProcessing) {
                    // Processing / Simulated USSD State with PIN access
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            color = GhanaGoldAccent,
                            strokeWidth = 4.dp,
                            modifier = Modifier.size(44.dp)
                        )
                        Text(
                            text = "Awaiting MoMo Console Authorization...",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "USSD Prompt sent to $selectedNetwork ($phone).\nAuthorizing payment of GH₵ $amount for St. Talafor Academy.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White
                                )

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.Black.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("MoMo PIN Used:", fontSize = 10.sp, color = Color.LightGray)
                                            Text(
                                                text = if (pin.isNotBlank()) "••••" else "(Awaiting SIM Input)",
                                                fontWeight = FontWeight.Bold,
                                                color = GhanaGoldAccent
                                            )
                                        }
                                        Badge(containerColor = GhanaEmeraldGreen) {
                                            Text("Awaiting Handset", color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else if (activePaymentMode == 1) {
                    // --- MoMo Console / USSD Terminal Mode ---
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Quick Action: Launch System Phone MoMo Console
                        OutlinedButton(
                            onClick = {
                                try {
                                    val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:" + Uri.encode(ussdShortcode))
                                    }
                                    context.startActivity(dialIntent)
                                    Toast.makeText(context, "Opening Phone Dialer with $ussdShortcode...", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Unable to launch dialer directly: $ussdShortcode", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = GhanaNavyPrimary),
                            modifier = Modifier.fillMaxWidth().testTag("launch_system_momo_dialer")
                        ) {
                            Icon(Icons.Default.PhoneForwarded, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Launch Handset Phone Console ($ussdShortcode)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        // In-App Interactive MoMo Console Terminal
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF0F172A),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF334155)),
                            modifier = Modifier.fillMaxWidth().testTag("in_app_momo_console_terminal")
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF22C55E)))
                                        Text("$selectedNetwork Console ($ussdShortcode)", color = Color(0xFF94A3B8), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                    }
                                    Text("SECURE SIM SESSION", color = GhanaGoldAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                }

                                HorizontalDivider(color = Color(0xFF334155))

                                when (ussdTerminalStep) {
                                    1 -> {
                                        Text(
                                            text = "=== $selectedNetwork Menu ===\n1) Transfer Money\n2) Pay Bill / School Fees (St. Talafor)\n3) Airtime & Bundles\n4) Allow Cash Out\n5) Financial Services\n6) My Approvals / Authorize Pending\n\nTarget School: St. Talafor Academy\nPending Authorization: GH₵ $amount",
                                            color = Color(0xFF38BDF8),
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            lineHeight = 16.sp
                                        )
                                    }
                                    2 -> {
                                        Text(
                                            text = "=== Authorize School Fee Payment ===\nRecipient: St. Talafor Academy\nStudent: $reference\nAmount: GH₵ $amount\nFee: GH₵ 0.00\n\nEnter 1 to Proceed\nEnter 2 to Cancel",
                                            color = Color(0xFFFDE047),
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            lineHeight = 16.sp
                                        )
                                    }
                                    3 -> {
                                        Text(
                                            text = "=== Security PIN Verification ===\nAuthorize GH₵ $amount to St. Talafor Academy?\nEnter 4-digit MoMo Secret PIN to confirm and debit wallet:",
                                            color = Color(0xFF4ADE80),
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Console Interactive Input Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    OutlinedTextField(
                                        value = if (ussdTerminalStep == 3) pin else consoleInputText,
                                        onValueChange = {
                                            if (ussdTerminalStep == 3) onPinChange(it) else consoleInputText = it
                                        },
                                        placeholder = {
                                            Text(
                                                if (ussdTerminalStep == 3) "Enter 4-digit PIN" else "Enter Menu Option (e.g. 2 or 6)",
                                                fontSize = 11.sp,
                                                color = Color(0xFF64748B)
                                            )
                                        },
                                        singleLine = true,
                                        visualTransformation = if (ussdTerminalStep == 3 && !isPinVisible) PasswordVisualTransformation() else VisualTransformation.None,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = GhanaGoldAccent,
                                            unfocusedBorderColor = Color(0xFF475569),
                                            cursorColor = GhanaGoldAccent
                                        ),
                                        modifier = Modifier.weight(1f).testTag("momo_console_command_input")
                                    )

                                    Button(
                                        onClick = {
                                            when (ussdTerminalStep) {
                                                1 -> {
                                                    // Move to confirmation prompt
                                                    ussdTerminalStep = 2
                                                    consoleInputText = ""
                                                }
                                                2 -> {
                                                    // Move to PIN prompt
                                                    ussdTerminalStep = 3
                                                    consoleInputText = ""
                                                }
                                                3 -> {
                                                    // Authorize payment!
                                                    onSubmitPayment()
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = GhanaGoldAccent),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier.testTag("send_console_command_button")
                                    ) {
                                        Text(
                                            text = if (ussdTerminalStep == 3) "Authorize" else "Send",
                                            color = GhanaNavyPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Network selection chip row
                        Text("Select Mobile Network:", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("MTN MoMo", "Telecel Cash", "AT Money").forEach { net ->
                                FilterChip(
                                    selected = selectedNetwork == net,
                                    onClick = { onNetworkSelected(net) },
                                    label = { Text(net, fontSize = 10.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // Fallback direct authorize button
                        Button(
                            onClick = onSubmitPayment,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GhanaNavyPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("console_full_authorize_button")
                        ) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = GhanaGoldAccent, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Complete Console Authorization (GH₵ $amount)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                } else {
                    // --- Direct MoMo Form Mode ---
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Network Selector
                        Text(
                            text = "Select Network Provider:",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MomoNetworkChip(
                                name = "MTN MoMo",
                                color = Color(0xFFFFCC00),
                                textColor = Color.Black,
                                isSelected = selectedNetwork == "MTN MoMo",
                                onClick = { onNetworkSelected("MTN MoMo") },
                                testTag = "momo_chip_mtn",
                                modifier = Modifier.weight(1f)
                            )

                            MomoNetworkChip(
                                name = "Telecel Cash",
                                color = Color(0xFFE60000),
                                textColor = Color.White,
                                isSelected = selectedNetwork == "Telecel Cash",
                                onClick = { onNetworkSelected("Telecel Cash") },
                                testTag = "momo_chip_telecel",
                                modifier = Modifier.weight(1f)
                            )

                            MomoNetworkChip(
                                name = "AT Money",
                                color = Color(0xFF003399),
                                textColor = Color.White,
                                isSelected = selectedNetwork == "AT Money",
                                onClick = { onNetworkSelected("AT Money") },
                                testTag = "momo_chip_at",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Phone Number Input
                        OutlinedTextField(
                            value = phone,
                            onValueChange = onPhoneChange,
                            label = { Text("MoMo Phone Number") },
                            placeholder = { Text("024XXXXXXX") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("momo_phone_input")
                        )

                        // Amount Input (GH₵)
                        OutlinedTextField(
                            value = amount,
                            onValueChange = onAmountChange,
                            label = { Text("Amount to Pay (GH₵)") },
                            leadingIcon = {
                                Text(
                                    "GH₵",
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(start = 12.dp)
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("momo_amount_input")
                        )

                        // Payment Date Input
                        OutlinedTextField(
                            value = paymentDate,
                            onValueChange = onPaymentDateChange,
                            label = { Text("Payment Date") },
                            placeholder = { Text("e.g. 10 Sep 2026") },
                            leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("momo_date_input")
                        )

                        // MoMo Secret PIN Input
                        OutlinedTextField(
                            value = pin,
                            onValueChange = onPinChange,
                            label = { Text("MoMo Secret PIN (to Authorize)") },
                            placeholder = { Text("Enter 4-digit PIN") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            trailingIcon = {
                                IconButton(
                                    onClick = { isPinVisible = !isPinVisible },
                                    modifier = Modifier.testTag("toggle_momo_pin_visibility")
                                ) {
                                    Icon(
                                        imageVector = if (isPinVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (isPinVisible) "Hide PIN" else "Access/Show PIN"
                                    )
                                }
                            },
                            visualTransformation = if (isPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("momo_pin_input")
                        )

                        // Reference Input
                        OutlinedTextField(
                            value = reference,
                            onValueChange = onReferenceChange,
                            label = { Text("Payment Reference / Memo") },
                            placeholder = { Text("e.g. Term 3 Tuition Deposit") },
                            leadingIcon = { Icon(Icons.Default.Receipt, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("momo_reference_input")
                        )

                        // Quick trigger for MoMo Console
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = GhanaNavyPrimary.copy(alpha = 0.08f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { activePaymentMode = 1 }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Terminal, contentDescription = null, tint = GhanaNavyPrimary, modifier = Modifier.size(16.dp))
                                    Text("Prefer USSD Console authorization?", fontSize = 11.sp, color = GhanaNavyPrimary)
                                }
                                Text("Open Console ➔", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GhanaNavyPrimary)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Submit & Authorize Button
                        Button(
                            onClick = onSubmitPayment,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GhanaNavyPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("momo_submit_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = GhanaGoldAccent)
                                Text(
                                    text = "Authorize & Pay GH₵ $amount",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MomoNetworkChip(
    name: String,
    color: Color,
    textColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) color else color.copy(alpha = 0.2f),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
        },
        modifier = modifier
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(vertical = 10.dp)
        ) {
            Text(
                text = name,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = if (isSelected) textColor else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
