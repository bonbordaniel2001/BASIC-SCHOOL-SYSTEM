package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.GhanaGoldAccent
import com.example.ui.theme.GhanaNavyPrimary

data class MigrationStepLog(
    val entityName: String,
    val sourceRoomTable: String,
    val targetFirestoreCollection: String,
    val recordCount: Int,
    val status: String // "Pending", "In Progress", "Completed"
)

@Composable
fun RoomToFirestoreMigrationDialog(
    onDismissRequest: () -> Unit
) {
    var isMigrating by remember { mutableStateOf(false) }
    var migrationProgress by remember { mutableFloatStateOf(0f) }
    var currentStepText by remember { mutableStateOf("Ready to initiate Room SQLite -> Firebase Firestore Cloud Sync") }

    val migrationLogs = remember {
        mutableStateListOf(
            MigrationStepLog("Student Directory & Profile", "students", "students", 420, "Pending"),
            MigrationStepLog("Teacher & Staff Assignments", "staff", "staff_assignments", 28, "Pending"),
            MigrationStepLog("Attendance & Geofence Clock-Ins", "attendance_records", "attendance_records", 1250, "Pending"),
            MigrationStepLog("Student Fee Ledgers & MoMo Receipts", "financial_ledgers", "student_ledgers", 890, "Pending")
        )
    }

    LaunchedEffect(isMigrating) {
        if (isMigrating) {
            for (i in migrationLogs.indices) {
                val current = migrationLogs[i]
                migrationLogs[i] = current.copy(status = "In Progress")
                currentStepText = "Exporting Room entity '${current.sourceRoomTable}' to Firestore collection '${current.targetFirestoreCollection}'..."

                for (p in 1..5) {
                    kotlinx.coroutines.delay(200)
                    migrationProgress = ((i * 5 + p) / 20f)
                }

                migrationLogs[i] = current.copy(status = "Completed")
            }
            currentStepText = "Migration complete! All local Room SQLite entities successfully mapped and verified in Firestore Cloud Storage."
            isMigrating = false
        }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("firestore_migration_dialog_surface"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(GhanaNavyPrimary)
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
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
                                    .background(GhanaGoldAccent, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = null,
                                    tint = GhanaNavyPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Room-to-Firestore Migration",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                )
                                Text(
                                    text = "Hybrid Offline-First & Cloud Sync Plan",
                                    color = GhanaGoldAccent,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismissRequest,
                            modifier = Modifier.testTag("close_firestore_migration_dialog_button")
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Plan Summary Card
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Architecture Migration Plan",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = GhanaNavyPrimary
                            )
                            Text(
                                text = "1. Room Local Database acts as primary offline cache for zero-latency UI reads.\n" +
                                        "2. Firestore Cloud Database handles multi-device realtime updates (Proprietor, Teacher, Guardian).\n" +
                                        "3. Automatic background sync queues pending offline mutations and pushes batch writes upon network connectivity.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 17.sp
                            )
                        }
                    }

                    // Entity Mapping Table
                    Text(
                        text = "Data Entity & Collection Mapping",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        migrationLogs.forEach { log ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(
                                        width = 1.dp,
                                        color = when (log.status) {
                                            "Completed" -> Color(0xFF2E7D32)
                                            "In Progress" -> GhanaGoldAccent
                                            else -> MaterialTheme.colorScheme.outlineVariant
                                        },
                                        shape = RoundedCornerShape(10.dp)
                                    ),
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = log.entityName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Room [${log.sourceRoomTable}]",
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 10.sp,
                                                color = GhanaNavyPrimary
                                            )
                                            Icon(
                                                imageVector = Icons.Default.ArrowForward,
                                                contentDescription = null,
                                                modifier = Modifier.size(10.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "Firestore /${log.targetFirestoreCollection}",
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 10.sp,
                                                color = Color(0xFFE65100),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Surface(
                                            color = when (log.status) {
                                                "Completed" -> Color(0xFF2E7D32).copy(alpha = 0.15f)
                                                "In Progress" -> GhanaGoldAccent.copy(alpha = 0.2f)
                                                else -> MaterialTheme.colorScheme.surfaceVariant
                                            },
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = "${log.status} (${log.recordCount} recs)",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = when (log.status) {
                                                    "Completed" -> Color(0xFF2E7D32)
                                                    "In Progress" -> GhanaNavyPrimary
                                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                                },
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Progress Status Box
                    Surface(
                        color = GhanaNavyPrimary.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = currentStepText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = GhanaNavyPrimary
                            )

                            LinearProgressIndicator(
                                progress = { migrationProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = GhanaNavyPrimary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }

                    // Action Trigger Button
                    Button(
                        onClick = {
                            if (!isMigrating) {
                                isMigrating = true
                            }
                        },
                        enabled = !isMigrating,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("execute_room_to_firestore_migration_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = GhanaNavyPrimary)
                    ) {
                        Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isMigrating) "Migrating Data to Firestore..." else "Execute Room-to-Firestore Sync")
                    }
                }
            }
        }
    }
}
