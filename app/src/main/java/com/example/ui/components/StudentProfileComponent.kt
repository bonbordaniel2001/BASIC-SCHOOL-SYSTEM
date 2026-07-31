package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.StudentProfile
import com.example.ui.theme.GhanaEmeraldGreen
import com.example.ui.theme.GhanaGoldAccent
import com.example.ui.theme.GhanaNavyPrimary

/**
 * Reusable Student Profile Component for displaying key academic data and contact information.
 * Fully formatted and ready to be populated by or synced to Firestore data.
 */
@Composable
fun StudentProfileCard(
    student: StudentProfile,
    modifier: Modifier = Modifier,
    firestoreMapData: Map<String, Any?>? = null,
    onContactGuardianPhone: ((phone: String) -> Unit)? = null,
    onContactGuardianEmail: ((email: String) -> Unit)? = null,
    onSyncToFirestore: ((StudentProfile) -> Unit)? = null,
    isExpandedDefault: Boolean = true
) {
    val context = LocalContext.current
    var activeTab by remember { mutableStateOf(0) } // 0: Academic, 1: Contact, 2: Firestore Document Schema
    var showRawFirestoreJson by remember { mutableStateOf(false) }

    // Use passed firestoreMapData if available, otherwise build from StudentProfile
    val effectiveFirestoreMap = firestoreMapData ?: remember(student) { student.toFirestoreMap() }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("student_profile_card_${student.id}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // --- HEADER: AVATAR, NAME, INDEX NO & STATUS ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Initial Circle Avatar
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(GhanaNavyPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = student.fullName.take(2).uppercase(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.White
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = student.fullName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = GhanaNavyPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.testTag("student_profile_fullname")
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = GhanaGoldAccent.copy(alpha = 0.25f)
                        ) {
                            Text(
                                text = student.indexNumber,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GhanaNavyPrimary,
                                modifier = Modifier
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                    .testTag("student_profile_index_number")
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = GhanaNavyPrimary.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = student.className,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = GhanaNavyPrimary,
                                modifier = Modifier
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                    .testTag("student_profile_class_name")
                            )
                        }
                    }
                }

                // Enrollment Status Badge
                val statusColor = when (student.enrollmentStatus.uppercase()) {
                    "ACTIVE" -> Color(0xFF0F5132)
                    "GRADUATED" -> Color(0xFF084298)
                    "SUSPENDED" -> Color(0xFF842029)
                    else -> Color(0xFF664D03)
                }
                val statusBg = when (student.enrollmentStatus.uppercase()) {
                    "ACTIVE" -> Color(0xFFD1E7DD)
                    "GRADUATED" -> Color(0xFFCFE2FF)
                    "SUSPENDED" -> Color(0xFFF8D7DA)
                    else -> Color(0xFFFFF3CD)
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = statusBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.4f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(statusColor, CircleShape)
                        )
                        Text(
                            text = student.enrollmentStatus,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor,
                            modifier = Modifier.testTag("student_profile_status_badge")
                        )
                    }
                }
            }

            // Quick Action Buttons & Firestore Indicator Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Firestore Data Ready Indicator Pill
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = GhanaEmeraldGreen.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GhanaEmeraldGreen.copy(alpha = 0.3f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.CloudDone,
                            contentDescription = "Firestore Ready",
                            tint = GhanaEmeraldGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Firestore Data Ready",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F5132)
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Call Guardian Button
                    IconButton(
                        onClick = {
                            if (onContactGuardianPhone != null) {
                                onContactGuardianPhone(student.guardianPhone)
                            } else {
                                try {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${student.guardianPhone}"))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Call: ${student.guardianPhone}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .background(GhanaNavyPrimary.copy(alpha = 0.1f), CircleShape)
                            .testTag("student_profile_call_guardian")
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = "Call Guardian", tint = GhanaNavyPrimary, modifier = Modifier.size(16.dp))
                    }

                    // Email Guardian Button
                    if (student.guardianEmail.isNotBlank()) {
                        IconButton(
                            onClick = {
                                if (onContactGuardianEmail != null) {
                                    onContactGuardianEmail(student.guardianEmail)
                                } else {
                                    try {
                                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${student.guardianEmail}"))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Email: ${student.guardianEmail}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .background(GhanaEmeraldGreen.copy(alpha = 0.15f), CircleShape)
                                .testTag("student_profile_email_guardian")
                        ) {
                            Icon(Icons.Default.Email, contentDescription = "Email Guardian", tint = GhanaEmeraldGreen, modifier = Modifier.size(16.dp))
                        }
                    }

                    // Sync/Push to Firestore Action
                    IconButton(
                        onClick = {
                            onSyncToFirestore?.invoke(student)
                            Toast.makeText(context, "Profile synced with Firestore!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .background(GhanaGoldAccent.copy(alpha = 0.25f), CircleShape)
                            .testTag("student_profile_sync_firestore")
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = "Sync to Firestore", tint = GhanaNavyPrimary, modifier = Modifier.size(16.dp))
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            // Tab Controls (Academic Data / Contact Info / Firestore Schema)
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = Color.Transparent,
                contentColor = GhanaNavyPrimary,
                divider = {}
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("Academic Data", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier.testTag("student_profile_tab_academic")
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("Contact Info", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.ContactPhone, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier.testTag("student_profile_tab_contact")
                )
                Tab(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    text = { Text("Firestore Schema", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Storage, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier.testTag("student_profile_tab_firestore")
                )
            }

            // --- TAB CONTENT 0: ACADEMIC DATA ---
            if (activeTab == 0) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        InfoBox(
                            title = "Index Number",
                            value = student.indexNumber,
                            icon = Icons.Default.Badge,
                            modifier = Modifier.weight(1f)
                        )
                        InfoBox(
                            title = "Class & Stream",
                            value = student.className,
                            icon = Icons.Default.Class,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        InfoBox(
                            title = "Admission Date",
                            value = student.admissionDate,
                            icon = Icons.Default.CalendarToday,
                            modifier = Modifier.weight(1f)
                        )
                        InfoBox(
                            title = "Date of Birth",
                            value = student.dateOfBirth,
                            icon = Icons.Default.Cake,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        InfoBox(
                            title = "Gender",
                            value = student.gender,
                            icon = Icons.Default.Person,
                            modifier = Modifier.weight(1f)
                        )
                        InfoBox(
                            title = "Enrollment Status",
                            value = student.enrollmentStatus,
                            icon = Icons.Default.CheckCircle,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // --- TAB CONTENT 1: CONTACT INFORMATION ---
            if (activeTab == 1) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    InfoBox(
                        title = "Guardian Full Name",
                        value = student.guardianName,
                        icon = Icons.Default.PersonOutline,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        InfoBox(
                            title = "Guardian Phone",
                            value = student.guardianPhone,
                            icon = Icons.Default.Phone,
                            modifier = Modifier.weight(1f)
                        )
                        InfoBox(
                            title = "Guardian Email",
                            value = student.guardianEmail.ifBlank { "Not provided" },
                            icon = Icons.Default.Email,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    InfoBox(
                        title = "Residential Address",
                        value = student.residentialAddress,
                        icon = Icons.Default.Home,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // --- TAB CONTENT 2: FIRESTORE DOCUMENT SCHEMA DATA ---
            if (activeTab == 2) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E1E1E), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Cloud Firestore Document Payload",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = GhanaGoldAccent
                        )
                        Text(
                            text = "Collection: /students",
                            fontSize = 10.sp,
                            color = Color.LightGray,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))

                    effectiveFirestoreMap.forEach { (key, value) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "\"$key\":",
                                fontSize = 11.sp,
                                color = Color(0xFF9CDCFFE0),
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = when (value) {
                                    is String -> "\"$value\""
                                    null -> "null"
                                    else -> value.toString()
                                },
                                fontSize = 11.sp,
                                color = Color(0xFFCE9178),
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Reusable Dialog Wrapper for displaying Student Profile details in a modal overlay
 */
@Composable
fun StudentProfileDialog(
    student: StudentProfile,
    onDismiss: () -> Unit,
    onContactGuardianPhone: ((phone: String) -> Unit)? = null,
    onContactGuardianEmail: ((email: String) -> Unit)? = null,
    onSyncToFirestore: ((StudentProfile) -> Unit)? = null
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(12.dp)
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
                    Text(
                        text = "Student Profile Overview",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = GhanaNavyPrimary
                    )

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                StudentProfileCard(
                    student = student,
                    onContactGuardianPhone = onContactGuardianPhone,
                    onContactGuardianEmail = onContactGuardianEmail,
                    onSyncToFirestore = onSyncToFirestore
                )
            }
        }
    }
}

/**
 * Reusable Student List View component with an integrated top Search Bar
 * allowing quick filtering of students by name, index number, ID, or class.
 */
@Composable
fun StudentListViewWithSearch(
    studentList: List<StudentProfile>,
    modifier: Modifier = Modifier,
    searchQuery: String = "",
    onSearchQueryChange: ((String) -> Unit)? = null,
    onContactGuardianPhone: ((phone: String) -> Unit)? = null,
    onContactGuardianEmail: ((email: String) -> Unit)? = null,
    onSyncToFirestore: ((StudentProfile) -> Unit)? = null
) {
    var internalSearchQuery by remember { mutableStateOf("") }
    val effectiveQuery = onSearchQueryChange?.let { searchQuery } ?: internalSearchQuery

    val filteredStudents = remember(studentList, effectiveQuery) {
        if (effectiveQuery.isBlank()) {
            studentList
        } else {
            studentList.filter { student ->
                student.fullName.contains(effectiveQuery, ignoreCase = true) ||
                student.indexNumber.contains(effectiveQuery, ignoreCase = true) ||
                student.id.toString().contains(effectiveQuery) ||
                student.className.contains(effectiveQuery, ignoreCase = true) ||
                student.guardianName.contains(effectiveQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // --- TOP SEARCH BAR ---
        OutlinedTextField(
            value = effectiveQuery,
            onValueChange = { query ->
                if (onSearchQueryChange != null) {
                    onSearchQueryChange(query)
                } else {
                    internalSearchQuery = query
                }
            },
            placeholder = { Text("Search student by name, ID or index number...", fontSize = 13.sp) },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search Students",
                    tint = GhanaNavyPrimary,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (effectiveQuery.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            if (onSearchQueryChange != null) {
                                onSearchQueryChange("")
                            } else {
                                internalSearchQuery = ""
                            }
                        },
                        modifier = Modifier.testTag("clear_student_search_button")
                    ) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "Clear Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GhanaNavyPrimary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("student_list_search_bar")
        )

        // Filter status / Result count banner
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (effectiveQuery.isBlank()) {
                    "Showing all ${studentList.size} students"
                } else {
                    "Found ${filteredStudents.size} of ${studentList.size} matching students"
                },
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (effectiveQuery.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = GhanaGoldAccent.copy(alpha = 0.2f),
                    modifier = Modifier.clickable {
                        if (onSearchQueryChange != null) {
                            onSearchQueryChange("")
                        } else {
                            internalSearchQuery = ""
                        }
                    }
                ) {
                    Text(
                        text = "Clear Filter",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = GhanaNavyPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }

        // --- STUDENT LIST / EMPTY SEARCH RESULTS STATE ---
        if (filteredStudents.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.PersonSearch,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        text = "No students found matching \"$effectiveQuery\"",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Try searching with a different name, index number, or ID.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                filteredStudents.forEach { profile ->
                    StudentProfileCard(
                        student = profile,
                        onContactGuardianPhone = onContactGuardianPhone,
                        onContactGuardianEmail = onContactGuardianEmail,
                        onSyncToFirestore = onSyncToFirestore
                    )
                }
            }
        }
    }
}

/**
 * Helper Box Component for Key-Value Pairs inside the Profile Card
 */
@Composable
private fun InfoBox(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(GhanaNavyPrimary.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = GhanaNavyPrimary, modifier = Modifier.size(14.dp))
            }

            Column {
                Text(
                    text = title,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
