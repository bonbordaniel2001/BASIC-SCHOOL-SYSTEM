package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.data.model.StudentProfile
import com.example.ui.theme.GhanaEmeraldGreen
import com.example.ui.theme.GhanaGoldAccent
import com.example.ui.theme.GhanaNavyPrimary

/**
 * Class & Student Promotion / Demotion Management Dialog.
 * Allows both Proprietor and Teachers to promote a class or students to the next class,
 * or demote them when necessary.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassPromotionDialog(
    allStudents: List<StudentProfile>,
    userRole: String, // "Proprietor" or "Teacher"
    defaultClassFilter: String = "ALL",
    onDismiss: () -> Unit,
    onPromoteClass: (currentClass: String, targetClass: String) -> Unit,
    onDemoteClass: (currentClass: String, targetClass: String) -> Unit,
    onPromoteStudent: (studentId: Long, targetClass: String) -> Unit,
    onDemoteStudent: (studentId: Long, targetClass: String) -> Unit,
    onRequestPromoteClass: ((currentClass: String, targetClass: String, reason: String) -> Unit)? = null,
    onRequestDemoteClass: ((currentClass: String, targetClass: String, reason: String) -> Unit)? = null,
    onRequestPromoteStudent: ((studentId: Long, studentName: String, currentClass: String, targetClass: String, reason: String) -> Unit)? = null,
    onRequestDemoteStudent: ((studentId: Long, studentName: String, currentClass: String, targetClass: String, reason: String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isTeacher = userRole.equals("Teacher", ignoreCase = true)
    var activeModeTab by remember { mutableStateOf(0) } // 0: Entire Class, 1: Individual Student
    var requestReason by remember { mutableStateOf("") }

    val standardClassProgression = remember {
        listOf(
            "Creche",
            "Nursery 1",
            "Nursery 2",
            "KG 1",
            "KG 2",
            "Basic 1",
            "Basic 2",
            "Basic 3",
            "Basic 4",
            "Basic 5",
            "Basic 6",
            "JHS 1",
            "JHS 2 - Gold",
            "JHS 3",
            "Graduated / Alumni"
        )
    }

    // Class Mode State
    val existingClasses = remember(allStudents) {
        val classes = allStudents.map { it.className }.distinct().filter { it.isNotBlank() }.sorted()
        if (classes.isEmpty()) standardClassProgression else classes
    }
    var selectedSourceClass by remember {
        mutableStateOf(if (defaultClassFilter != "ALL" && defaultClassFilter in existingClasses) defaultClassFilter else existingClasses.firstOrNull() ?: "JHS 2 - Gold")
    }

    fun getNextTargetClass(curr: String): String {
        val base = curr.split(" - ").firstOrNull() ?: curr
        val idx = standardClassProgression.indexOfFirst { it.startsWith(base) }
        return if (idx != -1 && idx < standardClassProgression.size - 1) {
            standardClassProgression[idx + 1]
        } else {
            "Graduated / Alumni"
        }
    }

    fun getPrevTargetClass(curr: String): String {
        val base = curr.split(" - ").firstOrNull() ?: curr
        val idx = standardClassProgression.indexOfFirst { it.startsWith(base) }
        return if (idx > 0) {
            standardClassProgression[idx - 1]
        } else {
            standardClassProgression.first()
        }
    }

    var customTargetClass by remember { mutableStateOf(getNextTargetClass(selectedSourceClass)) }

    LaunchedEffect(selectedSourceClass) {
        customTargetClass = getNextTargetClass(selectedSourceClass)
    }

    // Student Mode State
    var studentSearchQuery by remember { mutableStateOf("") }
    var selectedStudentId by remember { mutableStateOf<Long?>(null) }
    val selectedStudent = allStudents.firstOrNull { it.id == selectedStudentId }
    var studentTargetClass by remember { mutableStateOf("") }

    LaunchedEffect(selectedStudent) {
        if (selectedStudent != null) {
            studentTargetClass = getNextTargetClass(selectedStudent.className)
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
                .testTag("class_promotion_dialog")
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
                            Icon(
                                imageVector = Icons.Default.Upgrade,
                                contentDescription = null,
                                tint = GhanaGoldAccent,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Academic Promotion & Demotion",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = GhanaNavyPrimary
                            )
                            Text(
                                text = "Authorized by: $userRole Administration",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Mode Tabs: Whole Class vs Individual Student
                TabRow(
                    selectedTabIndex = activeModeTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    contentColor = GhanaNavyPrimary,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = activeModeTab == 0,
                        onClick = { activeModeTab = 0 },
                        text = { Text("Entire Class Promotion", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.testTag("tab_class_promotion")
                    )
                    Tab(
                        selected = activeModeTab == 1,
                        onClick = { activeModeTab = 1 },
                        text = { Text("Individual Student", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.testTag("tab_student_promotion")
                    )
                }

                if (isTeacher) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFFFBEB),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(20.dp))
                            Column {
                                Text("Proprietor Permission Required", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF92400E))
                                Text("As a teacher, your promotion or demotion request will be submitted to the Proprietor portal for administrative approval.", fontSize = 10.sp, color = Color(0xFFB45309))
                            }
                        }
                    }
                }

                if (activeModeTab == 0) {
                    // ==========================================
                    // TAB 0: ENTIRE CLASS PROMOTION / DEMOTION
                    // ==========================================
                    val studentsInSourceClass = allStudents.filter { it.className.equals(selectedSourceClass, ignoreCase = true) }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = GhanaNavyPrimary.copy(alpha = 0.05f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GhanaNavyPrimary.copy(alpha = 0.15f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "1. Select Class to Advance / Demote:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = GhanaNavyPrimary
                            )

                            // Source class chips
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                existingClasses.forEach { cls ->
                                    FilterChip(
                                        selected = selectedSourceClass == cls,
                                        onClick = { selectedSourceClass = cls },
                                        label = { Text(cls, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = GhanaNavyPrimary,
                                            selectedLabelColor = Color.White
                                        ),
                                        modifier = Modifier.testTag("source_class_chip_$cls")
                                    )
                                }
                            }

                            // Info badge on students affected
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = GhanaEmeraldGreen.copy(alpha = 0.12f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.People, contentDescription = null, tint = Color(0xFF0F5132), modifier = Modifier.size(16.dp))
                                    Text(
                                        text = "${studentsInSourceClass.size} Students currently enrolled in $selectedSourceClass",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF0F5132)
                                    )
                                }
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                            Text(
                                text = "2. Target Destination Class:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = GhanaNavyPrimary
                            )

                            OutlinedTextField(
                                value = customTargetClass,
                                onValueChange = { customTargetClass = it },
                                label = { Text("Destination Class") },
                                modifier = Modifier.fillMaxWidth().testTag("target_class_input")
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                standardClassProgression.forEach { cls ->
                                    AssistChip(
                                        onClick = { customTargetClass = cls },
                                        label = { Text(cls, fontSize = 10.sp) }
                                    )
                                }
                            }

                            if (isTeacher) {
                                OutlinedTextField(
                                    value = requestReason,
                                    onValueChange = { requestReason = it },
                                    label = { Text("Reason / Remarks for Proprietor") },
                                    placeholder = { Text("e.g. End of term academic performance review") },
                                    modifier = Modifier.fillMaxWidth().testTag("teacher_class_reason_input")
                                )
                            }
                        }
                    }

                    // Promote and Demote Class Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                val nextCls = if (customTargetClass.isNotBlank()) customTargetClass else getNextTargetClass(selectedSourceClass)
                                if (isTeacher && onRequestPromoteClass != null) {
                                    onRequestPromoteClass(selectedSourceClass, nextCls, requestReason)
                                } else {
                                    onPromoteClass(selectedSourceClass, nextCls)
                                    Toast.makeText(context, "All students in $selectedSourceClass promoted to $nextCls!", Toast.LENGTH_LONG).show()
                                }
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GhanaEmeraldGreen),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(48.dp).testTag("promote_entire_class_button")
                        ) {
                            Icon(Icons.Default.TrendingUp, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isTeacher) "Request Class Promotion" else "Promote Class (+1)", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                val prevCls = if (customTargetClass.isNotBlank()) customTargetClass else getPrevTargetClass(selectedSourceClass)
                                if (isTeacher && onRequestDemoteClass != null) {
                                    onRequestDemoteClass(selectedSourceClass, prevCls, requestReason)
                                } else {
                                    onDemoteClass(selectedSourceClass, prevCls)
                                    Toast.makeText(context, "Class $selectedSourceClass demoted to $prevCls.", Toast.LENGTH_LONG).show()
                                }
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(48.dp).testTag("demote_entire_class_button")
                        ) {
                            Icon(Icons.Default.TrendingDown, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isTeacher) "Request Class Demotion" else "Demote Class (-1)", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                } else {
                    // ==========================================
                    // TAB 1: INDIVIDUAL STUDENT PROMOTION / DEMOTION
                    // ==========================================
                    OutlinedTextField(
                        value = studentSearchQuery,
                        onValueChange = { studentSearchQuery = it },
                        label = { Text("Search Student by Name or Index No.") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth().testTag("student_promotion_search")
                    )

                    val filteredStudents = allStudents.filter {
                        studentSearchQuery.isBlank() ||
                        it.fullName.contains(studentSearchQuery, ignoreCase = true) ||
                        it.className.contains(studentSearchQuery, ignoreCase = true) ||
                        it.indexNumber.contains(studentSearchQuery, ignoreCase = true)
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp).verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (filteredStudents.isEmpty()) {
                                Text("No students found.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(8.dp))
                            } else {
                                filteredStudents.take(15).forEach { st ->
                                    val isSelected = selectedStudentId == st.id
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) GhanaNavyPrimary.copy(alpha = 0.15f) else Color.Transparent,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedStudentId = st.id }
                                            .padding(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(st.fullName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                Text("Current Class: ${st.className} • ID: ${st.indexNumber}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            if (isSelected) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GhanaNavyPrimary, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (selectedStudent != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = GhanaGoldAccent.copy(alpha = 0.15f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Target Class for ${selectedStudent.fullName}:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = GhanaNavyPrimary)
                                OutlinedTextField(
                                    value = studentTargetClass,
                                    onValueChange = { studentTargetClass = it },
                                    label = { Text("New Class Assignment") },
                                    modifier = Modifier.fillMaxWidth().testTag("student_target_class_input")
                                )

                                if (isTeacher) {
                                    OutlinedTextField(
                                        value = requestReason,
                                        onValueChange = { requestReason = it },
                                        label = { Text("Reason / Remarks for Proprietor") },
                                        placeholder = { Text("e.g. Exceptional academic performance") },
                                        modifier = Modifier.fillMaxWidth().testTag("teacher_student_reason_input")
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            val target = if (studentTargetClass.isNotBlank()) studentTargetClass else getNextTargetClass(selectedStudent.className)
                                            if (isTeacher && onRequestPromoteStudent != null) {
                                                onRequestPromoteStudent(selectedStudent.id, selectedStudent.fullName, selectedStudent.className, target, requestReason)
                                            } else {
                                                onPromoteStudent(selectedStudent.id, target)
                                                Toast.makeText(context, "${selectedStudent.fullName} promoted to $target!", Toast.LENGTH_LONG).show()
                                            }
                                            onDismiss()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = GhanaEmeraldGreen),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f).height(44.dp).testTag("promote_single_student_button")
                                    ) {
                                        Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(if (isTeacher) "Request Promotion" else "Promote", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }

                                    Button(
                                        onClick = {
                                            val target = if (studentTargetClass.isNotBlank()) studentTargetClass else getPrevTargetClass(selectedStudent.className)
                                            if (isTeacher && onRequestDemoteStudent != null) {
                                                onRequestDemoteStudent(selectedStudent.id, selectedStudent.fullName, selectedStudent.className, target, requestReason)
                                            } else {
                                                onDemoteStudent(selectedStudent.id, target)
                                                Toast.makeText(context, "${selectedStudent.fullName} demoted to $target.", Toast.LENGTH_LONG).show()
                                            }
                                            onDismiss()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f).height(44.dp).testTag("demote_single_student_button")
                                    ) {
                                        Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(if (isTeacher) "Request Demotion" else "Demote", fontWeight = FontWeight.Bold, fontSize = 11.sp)
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
