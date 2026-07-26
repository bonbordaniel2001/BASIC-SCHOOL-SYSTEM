package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SchoolEvent
import com.example.ui.theme.GhanaEmeraldGreen
import com.example.ui.theme.GhanaGoldAccent
import com.example.ui.theme.GhanaNavyPrimary
import com.example.ui.viewmodel.SchoolViewModel

@Composable
fun SchoolCalendarScreen(
    viewModel: SchoolViewModel,
    modifier: Modifier = Modifier
) {
    val allEvents by viewModel.allSchoolEvents.collectAsState()
    val activeUserAccount by viewModel.activeUserAccount.collectAsState()
    val schoolName by viewModel.schoolName.collectAsState()

    var selectedCategory by remember { mutableStateOf("ALL") } // "ALL", "PARENTS", "TEACHERS", "STUDENTS"
    var selectedMonthName by remember { mutableStateOf("August 2026") }
    var selectedDateFilter by remember { mutableStateOf<String?>(null) } // e.g., "2026-08-15"
    var showAddEventDialog by remember { mutableStateOf(false) }

    // Filter events by category and selected date
    val filteredEvents = remember(allEvents, selectedCategory, selectedDateFilter) {
        allEvents.filter { event ->
            val matchesAudience = when (selectedCategory) {
                "PARENTS" -> event.targetAudience == "PARENTS" || event.targetAudience == "ALL"
                "TEACHERS" -> event.targetAudience == "TEACHERS" || event.targetAudience == "ALL"
                "STUDENTS" -> event.targetAudience == "STUDENTS" || event.targetAudience == "ALL"
                else -> true
            }
            val matchesDate = selectedDateFilter == null || event.dateString == selectedDateFilter
            matchesAudience && matchesDate
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddEventDialog = true },
                containerColor = GhanaNavyPrimary,
                contentColor = GhanaGoldAccent,
                shape = CircleShape,
                modifier = Modifier.testTag("add_calendar_event_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Calendar Event")
            }
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // --- HERO HEADER: ACADEMIC CALENDAR BANNER ---
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = GhanaNavyPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
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
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(GhanaGoldAccent),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        tint = GhanaNavyPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "School Academic Calendar",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${schoolName.ifBlank { "ST TALAFOR ACCADEMY" }} • 2025/2026 Term 3",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = GhanaGoldAccent
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "${allEvents.size} Events",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Term Milestone Countdown Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MilestoneCountdownChip(
                                title = "Science Exhibition",
                                date = "15 Aug '26",
                                daysLeft = "20 Days Left",
                                modifier = Modifier.weight(1f)
                            )
                            MilestoneCountdownChip(
                                title = "End of Term Exams",
                                date = "05 Sep '26",
                                daysLeft = "41 Days Left",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // --- MONTHLY CALENDAR GRID NAVIGATOR ---
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    selectedMonthName = when (selectedMonthName) {
                                        "September 2026" -> "August 2026"
                                        "October 2026" -> "September 2026"
                                        else -> "July 2026"
                                    }
                                }
                            ) {
                                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Month")
                            }

                            Text(
                                text = selectedMonthName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = GhanaNavyPrimary
                            )

                            IconButton(
                                onClick = {
                                    selectedMonthName = when (selectedMonthName) {
                                        "August 2026" -> "September 2026"
                                        "September 2026" -> "October 2026"
                                        else -> "November 2026"
                                    }
                                }
                            ) {
                                Icon(Icons.Default.ChevronRight, contentDescription = "Next Month")
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Days of Week Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { day ->
                                Text(
                                    text = day,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.width(36.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // 31-Day Interactive Grid
                        val daysInMonth = (1..31).toList()
                        val eventDates = remember(allEvents) { allEvents.map { it.dateString } }

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            daysInMonth.chunked(7).forEach { weekChunk ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    weekChunk.forEach { dayNum ->
                                        val dayFormatted = if (dayNum < 10) "0$dayNum" else "$dayNum"
                                        val fullDateStr = "2026-08-$dayFormatted"
                                        val hasEvent = eventDates.contains(fullDateStr)
                                        val isSelected = selectedDateFilter == fullDateStr

                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(
                                                    when {
                                                        isSelected -> GhanaNavyPrimary
                                                        hasEvent -> GhanaGoldAccent.copy(alpha = 0.25f)
                                                        dayNum == 25 -> GhanaEmeraldGreen.copy(alpha = 0.15f) // Today
                                                        else -> Color.Transparent
                                                    }
                                                )
                                                .border(
                                                    width = if (dayNum == 25) 1.5.dp else 0.dp,
                                                    color = if (dayNum == 25) GhanaEmeraldGreen else Color.Transparent,
                                                    shape = RoundedCornerShape(10.dp)
                                                )
                                                .clickable {
                                                    selectedDateFilter = if (isSelected) null else fullDateStr
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    text = dayNum.toString(),
                                                    fontSize = 12.sp,
                                                    fontWeight = if (isSelected || dayNum == 25) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                                )
                                                if (hasEvent) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(4.dp)
                                                            .clip(CircleShape)
                                                            .background(if (isSelected) GhanaGoldAccent else GhanaNavyPrimary)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    // Fill empty slots if last week is short
                                    repeat(7 - weekChunk.size) {
                                        Spacer(modifier = Modifier.size(38.dp))
                                    }
                                }
                            }
                        }

                        if (selectedDateFilter != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Filtered by date: $selectedDateFilter",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = GhanaNavyPrimary
                                )
                                TextButton(onClick = { selectedDateFilter = null }) {
                                    Text("Clear Filter", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }

            // --- CATEGORY FILTER CHIPS ---
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategory == "ALL",
                            onClick = { selectedCategory = "ALL" },
                            label = { Text("All Events") },
                            leadingIcon = { Icon(Icons.Default.Grid3x3, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.testTag("filter_all_events")
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedCategory == "PARENTS",
                            onClick = { selectedCategory = "PARENTS" },
                            label = { Text("PTA / Parents") },
                            leadingIcon = { Icon(Icons.Default.FamilyRestroom, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.testTag("filter_parents_events")
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedCategory == "TEACHERS",
                            onClick = { selectedCategory = "TEACHERS" },
                            label = { Text("Staff / CPD") },
                            leadingIcon = { Icon(Icons.Default.CoPresent, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.testTag("filter_teachers_events")
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedCategory == "STUDENTS",
                            onClick = { selectedCategory = "STUDENTS" },
                            label = { Text("Students / Sports") },
                            leadingIcon = { Icon(Icons.Default.SportsBasketball, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.testTag("filter_students_events")
                        )
                    }
                }
            }

            // --- SCHEDULED EVENTS SECTION HEADER ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Upcoming School Events (${filteredEvents.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = GhanaNavyPrimary
                    )
                }
            }

            // --- EVENT CARDS LIST ---
            if (filteredEvents.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.EventBusy, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No events scheduled for this filter.", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text("Tap '+' to post a new school event.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                items(filteredEvents, key = { it.id }) { event ->
                    EventCardItem(
                        event = event,
                        onDelete = { viewModel.deleteSchoolEvent(event) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    // --- ADD EVENT DIALOG ---
    if (showAddEventDialog) {
        AddEventModalDialog(
            onDismiss = { showAddEventDialog = false },
            onConfirm = { newEvent ->
                viewModel.addSchoolEvent(newEvent)
                showAddEventDialog = false
            }
        )
    }
}

@Composable
private fun MilestoneCountdownChip(
    title: String,
    date: String,
    daysLeft: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White.copy(alpha = 0.12f),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = date, fontSize = 10.sp, color = GhanaGoldAccent)
                Text(text = daysLeft, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            }
        }
    }
}

@Composable
private fun EventCardItem(
    event: SchoolEvent,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("event_card_${event.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (event.isImportant) GhanaNavyPrimary else GhanaGoldAccent.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = event.dateString.takeLast(2),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (event.isImportant) GhanaGoldAccent else GhanaNavyPrimary
                            )
                            Text(
                                text = "AUG",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (event.isImportant) Color.White else GhanaNavyPrimary
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = event.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = GhanaNavyPrimary
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(text = event.timeString, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(text = event.location, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Delete Event", tint = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = event.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = GhanaNavyPrimary.copy(alpha = 0.08f)
                ) {
                    Text(
                        text = "Target: ${event.targetAudience}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = GhanaNavyPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                if (event.isImportant) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Text(
                            text = "Mandatory / Important",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AddEventModalDialog(
    onDismiss: () -> Unit,
    onConfirm: (SchoolEvent) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var dateString by remember { mutableStateOf("2026-08-25") }
    var timeString by remember { mutableStateOf("09:00 AM") }
    var location by remember { mutableStateOf("School Assembly Hall") }
    var organizer by remember { mutableStateOf("School Administration") }
    var audience by remember { mutableStateOf("ALL") } // "ALL", "STUDENTS", "TEACHERS", "PARENTS"
    var description by remember { mutableStateOf("") }
    var isImportant by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add School Calendar Event", fontWeight = FontWeight.Bold, color = GhanaNavyPrimary)
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Event Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = dateString,
                        onValueChange = { dateString = it },
                        label = { Text("Date (YYYY-MM-DD)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = timeString,
                        onValueChange = { timeString = it },
                        label = { Text("Time") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Event Description") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Mark as Important Event", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = isImportant, onCheckedChange = { isImportant = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(
                            SchoolEvent(
                                title = title,
                                dateString = dateString,
                                timeString = timeString,
                                description = description.ifBlank { "Official school activity event." },
                                targetAudience = audience,
                                location = location,
                                organizer = organizer,
                                isImportant = isImportant
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GhanaNavyPrimary)
            ) {
                Text("Save Event")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
