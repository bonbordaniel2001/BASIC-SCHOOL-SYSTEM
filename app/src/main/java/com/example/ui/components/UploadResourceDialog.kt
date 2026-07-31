package com.example.ui.components

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.example.ui.theme.GhanaEmeraldGreen
import com.example.ui.theme.GhanaGoldAccent
import com.example.ui.theme.GhanaNavyPrimary

/**
 * Modern Upload Resource Dialog for Teachers and Proprietors.
 * Supports uploading files from Local Storage 📂 (Videos, Documents, Audio, Images)
 * or via Website Links / Online URLs 🔗.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadResourceDialog(
    onDismiss: () -> Unit,
    onUpload: (
        title: String,
        authorOrPublisher: String,
        category: String,
        resourceType: String,
        targetClass: String,
        subject: String,
        targetAudience: String,
        description: String,
        fileFormat: String,
        fileUrlOrPath: String,
        uploadedByRole: String
    ) -> Unit,
    userRole: String = "Proprietor" // "Proprietor" or "Teacher"
) {
    val context = LocalContext.current
    var uploadSourceTab by remember { mutableStateOf(0) } // 0: Local File 📂, 1: Website Link 🔗

    // Form Fields
    var resourceTitle by remember { mutableStateOf("") }
    var authorPublisher by remember { mutableStateOf(if (userRole == "Teacher") "Subject Teacher" else "Proprietor / Admin") }
    var category by remember { mutableStateOf("TEXTBOOK") }
    var resourceType by remember { mutableStateOf("DOCUMENT") } // "DOCUMENT", "VIDEO", "AUDIO", "IMAGE"
    var targetClass by remember { mutableStateOf("ALL") }
    var subject by remember { mutableStateOf("ALL") }
    var targetAudience by remember { mutableStateOf("ALL") }
    var description by remember { mutableStateOf("") }
    var fileFormat by remember { mutableStateOf("PDF") }
    var customUrlOrPath by remember { mutableStateOf("") }
    var selectedFileName by remember { mutableStateOf("") }

    // System Activity Launcher for picking local files from device
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val fileName = uri.lastPathSegment?.substringAfterLast("/") ?: "selected_media_file"
            selectedFileName = fileName
            customUrlOrPath = uri.toString()
            if (resourceTitle.isBlank()) {
                resourceTitle = fileName.replace("_", " ").substringBeforeLast(".")
            }

            // Auto-detect format and resource type
            when {
                fileName.endsWith(".mp4", ignoreCase = true) || fileName.endsWith(".webm", ignoreCase = true) -> {
                    resourceType = "VIDEO"
                    fileFormat = "MP4"
                    category = "PROMOTIONAL_VIDEO"
                }
                fileName.endsWith(".mp3", ignoreCase = true) || fileName.endsWith(".wav", ignoreCase = true) || fileName.endsWith(".m4a", ignoreCase = true) -> {
                    resourceType = "AUDIO"
                    fileFormat = "MP3"
                    category = "AUDIO_LESSON"
                }
                fileName.endsWith(".pdf", ignoreCase = true) -> {
                    resourceType = "DOCUMENT"
                    fileFormat = "PDF"
                }
                fileName.endsWith(".epub", ignoreCase = true) -> {
                    resourceType = "DOCUMENT"
                    fileFormat = "EPUB"
                }
                fileName.endsWith(".png", ignoreCase = true) || fileName.endsWith(".jpg", ignoreCase = true) -> {
                    resourceType = "IMAGE"
                    fileFormat = "PNG"
                    category = "SCHOOL_MEDIA"
                }
            }
            Toast.makeText(context, "Selected file: $fileName", Toast.LENGTH_SHORT).show()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(GhanaNavyPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Plus Upload",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        text = "Upload Library Resource",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = GhanaNavyPrimary
                    )
                    Text(
                        text = "Upload local files 📂 or web links 🔗 ($userRole Portal)",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // SOURCE SELECTOR TAB: Local File vs Website Link
                TabRow(
                    selectedTabIndex = uploadSourceTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    contentColor = GhanaNavyPrimary,
                    modifier = Modifier.clip(RoundedCornerShape(10.dp))
                ) {
                    Tab(
                        selected = uploadSourceTab == 0,
                        onClick = { uploadSourceTab = 0 },
                        text = { Text("Local Files 📂", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.testTag("upload_tab_local_file")
                    )
                    Tab(
                        selected = uploadSourceTab == 1,
                        onClick = { uploadSourceTab = 1 },
                        text = { Text("Website Link 🔗", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.testTag("upload_tab_web_link")
                    )
                }

                // TAB 0: LOCAL FILE PICKER PANEL
                if (uploadSourceTab == 0) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = GhanaEmeraldGreen.copy(alpha = 0.08f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GhanaEmeraldGreen.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Select File from Local Storage 📂",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFF0F5132)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = { filePickerLauncher.launch("*/*") },
                                    colors = ButtonDefaults.buttonColors(containerColor = GhanaEmeraldGreen),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("browse_local_file_button")
                                ) {
                                    Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Browse Storage", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Text(
                                    text = if (selectedFileName.isNotBlank()) selectedFileName else "No file chosen yet",
                                    fontSize = 11.sp,
                                    color = if (selectedFileName.isNotBlank()) GhanaNavyPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (selectedFileName.isNotBlank()) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            HorizontalDivider(color = GhanaEmeraldGreen.copy(alpha = 0.2f))

                            // Quick Local Sample Presets
                            Text("Or select quick media type preset:", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                FilterChip(
                                    selected = resourceType == "VIDEO",
                                    onClick = {
                                        resourceType = "VIDEO"
                                        fileFormat = "MP4"
                                        category = "PROMOTIONAL_VIDEO"
                                        if (resourceTitle.isBlank()) resourceTitle = "Physics Experiment Video"
                                        customUrlOrPath = "storage/local/videos/physics_lab_demo.mp4"
                                        selectedFileName = "physics_lab_demo.mp4"
                                    },
                                    label = { Text("🎬 Video", fontSize = 10.sp) },
                                    modifier = Modifier.testTag("preset_video_chip")
                                )
                                FilterChip(
                                    selected = resourceType == "DOCUMENT",
                                    onClick = {
                                        resourceType = "DOCUMENT"
                                        fileFormat = "PDF"
                                        category = "TEXTBOOK"
                                        if (resourceTitle.isBlank()) resourceTitle = "JHS Mathematics Textbook"
                                        customUrlOrPath = "storage/local/documents/math_jhs2.pdf"
                                        selectedFileName = "math_jhs2.pdf"
                                    },
                                    label = { Text("📄 PDF", fontSize = 10.sp) },
                                    modifier = Modifier.testTag("preset_doc_chip")
                                )
                                FilterChip(
                                    selected = resourceType == "AUDIO",
                                    onClick = {
                                        resourceType = "AUDIO"
                                        fileFormat = "MP3"
                                        category = "AUDIO_LESSON"
                                        if (resourceTitle.isBlank()) resourceTitle = "English Pronunciation Audio"
                                        customUrlOrPath = "storage/local/audio/english_lesson_01.mp3"
                                        selectedFileName = "english_lesson_01.mp3"
                                    },
                                    label = { Text("🎵 Audio", fontSize = 10.sp) },
                                    modifier = Modifier.testTag("preset_audio_chip")
                                )
                            }
                        }
                    }
                }

                // TAB 1: WEBSITE LINK / ONLINE URL INPUT
                if (uploadSourceTab == 1) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = GhanaNavyPrimary.copy(alpha = 0.08f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GhanaNavyPrimary.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Provide Website URL or Online Media Link 🔗",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = GhanaNavyPrimary
                            )

                            OutlinedTextField(
                                value = customUrlOrPath,
                                onValueChange = { customUrlOrPath = it },
                                label = { Text("Website or Direct Media Link") },
                                placeholder = { Text("https://youtube.com/watch?v=... or https://drive.google.com/...") },
                                leadingIcon = { Icon(Icons.Default.Link, contentDescription = null, tint = GhanaNavyPrimary) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("upload_website_url_input")
                            )

                            Text(
                                text = "Supported: YouTube, Vimeo, Google Drive, SoundCloud, or Direct PDF/MP4/MP3 URLs.",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // RESOURCE TITLE
                OutlinedTextField(
                    value = resourceTitle,
                    onValueChange = { resourceTitle = it },
                    label = { Text("Title / Resource Name *") },
                    placeholder = { Text("e.g. Integrated Science BECE Revision") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("upload_resource_title_input")
                )

                // AUTHOR / PUBLISHER
                OutlinedTextField(
                    value = authorPublisher,
                    onValueChange = { authorPublisher = it },
                    label = { Text("Author / Publisher / Instructor") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // CATEGORY SELECTOR
                Column {
                    Text("Category:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GhanaNavyPrimary)
                    val categories = listOf("TEXTBOOK", "SYLLABUS", "CURRICULUM", "REFERENCE", "SCHOOL_HISTORY", "PROMOTIONAL_VIDEO", "SCHOOL_MEDIA", "AUDIO_LESSON")
                    ScrollableTabRow(
                        selectedTabIndex = categories.indexOf(category).coerceAtLeast(0),
                        edgePadding = 0.dp
                    ) {
                        categories.forEach { cat ->
                            Tab(
                                selected = category == cat,
                                onClick = { category = cat }
                            ) {
                                Text(
                                    text = cat.replace("_", " "),
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 8.dp)
                                )
                            }
                        }
                    }
                }

                // FILE FORMAT & RESOURCE TYPE CHIPS
                Column {
                    Text("File Format & Media Type:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GhanaNavyPrimary)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("PDF", "MP4", "MP3", "EPUB", "DOCX", "WAV", "PNG").forEach { fmt ->
                            FilterChip(
                                selected = fileFormat == fmt,
                                onClick = {
                                    fileFormat = fmt
                                    resourceType = when (fmt) {
                                        "MP4" -> "VIDEO"
                                        "MP3", "WAV" -> "AUDIO"
                                        "PNG", "JPG" -> "IMAGE"
                                        else -> "DOCUMENT"
                                    }
                                },
                                label = { Text(fmt, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                            )
                        }
                    }
                }

                // TARGET CLASS
                Column {
                    Text("Target Class:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GhanaNavyPrimary)
                    val classes = listOf("ALL", "JHS 1", "JHS 2 - Gold", "JHS 3", "Primary 4", "Primary 5", "Primary 6")
                    ScrollableTabRow(
                        selectedTabIndex = classes.indexOf(targetClass).coerceAtLeast(0),
                        edgePadding = 0.dp
                    ) {
                        classes.forEach { cls ->
                            Tab(
                                selected = targetClass == cls,
                                onClick = { targetClass = cls }
                            ) {
                                Text(cls, fontSize = 10.sp, modifier = Modifier.padding(vertical = 6.dp, horizontal = 8.dp))
                            }
                        }
                    }
                }

                // SUBJECT
                Column {
                    Text("Subject:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GhanaNavyPrimary)
                    val subjects = listOf("ALL", "Mathematics", "Integrated Science", "English Language", "Social Studies", "ICT")
                    ScrollableTabRow(
                        selectedTabIndex = subjects.indexOf(subject).coerceAtLeast(0),
                        edgePadding = 0.dp
                    ) {
                        subjects.forEach { sub ->
                            Tab(
                                selected = subject == sub,
                                onClick = { subject = sub }
                            ) {
                                Text(sub, fontSize = 10.sp, modifier = Modifier.padding(vertical = 6.dp, horizontal = 8.dp))
                            }
                        }
                    }
                }

                // DESCRIPTION & NOTES
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Resource Description & Notes") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (resourceTitle.isNotBlank()) {
                        val finalPath = if (customUrlOrPath.isNotBlank()) {
                            customUrlOrPath
                        } else {
                            if (uploadSourceTab == 1) "https://library.school.edu/resources/${resourceTitle.lowercase().replace(" ", "_")}"
                            else "storage/library/${resourceTitle.lowercase().replace(" ", "_")}.${fileFormat.lowercase()}"
                        }

                        onUpload(
                            resourceTitle,
                            authorPublisher,
                            category,
                            resourceType,
                            targetClass,
                            subject,
                            targetAudience,
                            description,
                            fileFormat,
                            finalPath,
                            userRole
                        )
                        onDismiss()
                    } else {
                        Toast.makeText(context, "Please enter a resource title", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GhanaNavyPrimary),
                modifier = Modifier.testTag("submit_upload_resource_plus_button")
            ) {
                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Upload Resource")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
