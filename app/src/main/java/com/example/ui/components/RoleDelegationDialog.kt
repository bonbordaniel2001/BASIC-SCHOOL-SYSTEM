package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.StaffMember
import com.example.ui.theme.GhanaGoldAccent

@Composable
fun RoleDelegationDialog(
    staffList: List<StaffMember>,
    onTogglePermission: (StaffMember, String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedStaff by remember(staffList) {
        mutableStateOf(staffList.firstOrNull())
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("role_delegation_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Dialog Title
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
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Role Delegation Matrix",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Assign custom staff permissions",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_role_dialog_button")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Staff Selector Tabs / Dropdown
                Text(
                    text = "Select Staff Member:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                ScrollableTabRow(
                    selectedTabIndex = staffList.indexOf(selectedStaff).coerceAtLeast(0),
                    edgePadding = 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    staffList.forEach { staff ->
                        Tab(
                            selected = selectedStaff?.id == staff.id,
                            onClick = { selectedStaff = staff },
                            text = {
                                Text(
                                    text = staff.name.take(15),
                                    fontWeight = if (selectedStaff?.id == staff.id) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Permission Toggles Matrix for active staff
                selectedStaff?.let { staff ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${staff.name} (${staff.staffCode})",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(4.dp)
                                ) {
                                    Text(
                                        text = staff.primaryRole,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(8.dp))

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 280.dp)
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                PermissionSwitchRow(
                                    title = "Edit & Submit Grades",
                                    description = "Allow entering quarterly student assessment marks",
                                    isChecked = staff.isCanEditGrades,
                                    onCheckedChange = { onTogglePermission(staff, "EDIT_GRADES") },
                                    testTag = "toggle_edit_grades"
                                )

                                PermissionSwitchRow(
                                    title = "Mark Attendance Register",
                                    description = "Record daily & weekly classroom attendance",
                                    isChecked = staff.isCanMarkAttendance,
                                    onCheckedChange = { onTogglePermission(staff, "MARK_ATTENDANCE") },
                                    testTag = "toggle_mark_attendance"
                                )

                                PermissionSwitchRow(
                                    title = "Approve Fee Overrides",
                                    description = "Grant fee waivers or payment grace periods",
                                    isChecked = staff.isCanApproveOverrides,
                                    onCheckedChange = { onTogglePermission(staff, "APPROVE_OVERRIDES") },
                                    testTag = "toggle_approve_overrides"
                                )

                                PermissionSwitchRow(
                                    title = "Access Financial Records",
                                    description = "View school bursar ledger and fee reports",
                                    isChecked = staff.isCanAccessFinancials,
                                    onCheckedChange = { onTogglePermission(staff, "ACCESS_FINANCIALS") },
                                    testTag = "toggle_access_financials"
                                )

                                PermissionSwitchRow(
                                    title = "SMS & WhatsApp Broadcasts",
                                    description = "Send announcements to parent groups",
                                    isChecked = staff.isCanSendSms,
                                    onCheckedChange = { onTogglePermission(staff, "SEND_SMS") },
                                    testTag = "toggle_send_sms"
                                )

                                PermissionSwitchRow(
                                    title = "Manage Staff Roles",
                                    description = "Proprietor privilege: Grant admin access",
                                    isChecked = staff.isCanManageRoles,
                                    onCheckedChange = { onTogglePermission(staff, "MANAGE_ROLES") },
                                    testTag = "toggle_manage_roles"
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_role_delegation_button")
                ) {
                    Text("Save Permissions Matrix", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun PermissionSwitchRow(
    title: String,
    description: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag)
        )
    }
}
