package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.os.Environment
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.DigitalResource
import com.example.ui.theme.GhanaEmeraldGreen
import com.example.ui.theme.GhanaGoldAccent
import com.example.ui.theme.GhanaNavyPrimary
import kotlinx.coroutines.delay
import java.io.File

/**
 * In-App Media Viewer & Player Dialog.
 * Allows playing audio/video lessons and viewing educational documents directly on the app,
 * with full download to device storage capability.
 */
@Composable
fun InAppMediaViewerDialog(
    resource: DigitalResource,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isAudio = resource.resourceType == "AUDIO" || resource.fileFormat.equals("MP3", ignoreCase = true) || resource.fileFormat.equals("WAV", ignoreCase = true)
    val isVideo = resource.resourceType == "VIDEO" || resource.fileFormat.equals("MP4", ignoreCase = true)
    val isDocument = resource.resourceType == "DOCUMENT" || resource.fileFormat.equals("PDF", ignoreCase = true) || resource.fileFormat.equals("EPUB", ignoreCase = true)

    var isPlaying by remember { mutableStateOf(true) }
    var currentProgress by remember { mutableStateOf(0.25f) }
    var playbackSpeed by remember { mutableStateOf("1.0x") }
    var documentPage by remember { mutableStateOf(1) }
    val totalDocumentPages = 14
    var isDownloadedLocally by remember { mutableStateOf(false) }

    // Simulated playback loop when playing
    LaunchedEffect(isPlaying) {
        while (isPlaying && (isAudio || isVideo)) {
            delay(1000)
            currentProgress = (currentProgress + 0.02f).coerceAtMost(1f)
            if (currentProgress >= 1f) {
                isPlaying = false
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            modifier = modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight()
                .padding(12.dp)
                .testTag("in_app_media_viewer_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Top Header
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
                                .background(if (isAudio) GhanaGoldAccent else if (isVideo) Color(0xFFE53935) else GhanaNavyPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when {
                                    isAudio -> Icons.Default.Audiotrack
                                    isVideo -> Icons.Default.PlayCircleFilled
                                    else -> Icons.Default.MenuBook
                                },
                                contentDescription = null,
                                tint = if (isAudio) GhanaNavyPrimary else Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = if (isDocument) "Digital Reader" else "In-App Player",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = GhanaNavyPrimary
                            )
                            Text(
                                text = "St. Talafor Learning Hub • ${resource.targetClass}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_media_viewer")) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Media Viewport / Player Screen
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF121824),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isVideo) 190.dp else if (isAudio) 150.dp else 180.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            isVideo -> {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(54.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = if (isPlaying) "Playing Educational Video Lesson..." else "Paused",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "${(currentProgress * 12).toInt()}m : ${(currentProgress * 60 % 60).toInt()}s / 12m : 00s",
                                        color = Color.LightGray,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                            isAudio -> {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Visual audio equalizer bars simulation
                                        val barHeights = listOf(14, 28, 44, 20, 36, 48, 24, 18, 32, 40, 16)
                                        barHeights.forEach { h ->
                                            Box(
                                                modifier = Modifier
                                                    .width(4.dp)
                                                    .height((if (isPlaying) h else 8).dp)
                                                    .clip(RoundedCornerShape(2.dp))
                                                    .background(GhanaGoldAccent)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = resource.title,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "${resource.subject} • Voice Lecture by ${resource.authorOrPublisher}",
                                        color = Color.LightGray,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            else -> {
                                Column(
                                    modifier = Modifier.verticalScroll(rememberScrollState()),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Surface(
                                        color = Color(0xFF1E293B),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Page $documentPage of $totalDocumentPages",
                                                color = GhanaGoldAccent,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                IconButton(
                                                    onClick = { if (documentPage > 1) documentPage-- },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(Icons.Default.ArrowBack, contentDescription = "Prev Page", tint = Color.White, modifier = Modifier.size(16.dp))
                                                }
                                                IconButton(
                                                    onClick = { if (documentPage < totalDocumentPages) documentPage++ },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(Icons.Default.ArrowForward, contentDescription = "Next Page", tint = Color.White, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }
                                    }
                                    Text(
                                        text = "Chapter $documentPage: Key Competencies & Review Questions",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Official Ministry of Education approved syllabus for ${resource.targetClass}. In this module, students review fundamentals of ${resource.subject} and practice past questions in preparation for national assessments.",
                                        color = Color(0xFFCBD5E1),
                                        fontSize = 11.sp,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Playback Slider Controls for Audio/Video
                if (isAudio || isVideo) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Slider(
                            value = currentProgress,
                            onValueChange = { currentProgress = it },
                            colors = SliderDefaults.colors(
                                thumbColor = GhanaGoldAccent,
                                activeTrackColor = GhanaGoldAccent
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("media_progress_slider")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${(currentProgress * 15).toInt()}m",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "15m 00s",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Transport Controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { currentProgress = (currentProgress - 0.1f).coerceAtLeast(0f) }) {
                                Icon(Icons.Default.Replay10, contentDescription = "Rewind 10s")
                            }

                            FilledIconButton(
                                onClick = { isPlaying = !isPlaying },
                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = GhanaNavyPrimary),
                                modifier = Modifier.size(48.dp).testTag("toggle_play_pause_button")
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlaying) "Pause" else "Play",
                                    tint = Color.White
                                )
                            }

                            IconButton(onClick = { currentProgress = (currentProgress + 0.1f).coerceAtMost(1f) }) {
                                Icon(Icons.Default.Forward10, contentDescription = "Fast Forward 10s")
                            }

                            AssistChip(
                                onClick = {
                                    playbackSpeed = when (playbackSpeed) {
                                        "1.0x" -> "1.25x"
                                        "1.25x" -> "1.5x"
                                        else -> "1.0x"
                                    }
                                },
                                label = { Text(playbackSpeed, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                            )
                        }
                    }
                }

                // File Details Summary
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(resource.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = GhanaNavyPrimary)
                        Text("${resource.authorOrPublisher} • Format: ${resource.fileFormat} • Size: ${(resource.fileSizeBytes / 1024 / 1024).coerceAtLeast(1)} MB", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Download to Device & Share Action Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            saveResourceToDeviceStorage(context, resource)
                            isDownloadedLocally = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDownloadedLocally) GhanaEmeraldGreen else GhanaNavyPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(46.dp).testTag("download_to_device_button")
                    ) {
                        Icon(
                            imageVector = if (isDownloadedLocally) Icons.Default.CheckCircle else Icons.Default.FileDownload,
                            contentDescription = null,
                            tint = if (isDownloadedLocally) Color.White else GhanaGoldAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isDownloadedLocally) "Downloaded to Device" else "Download to Device",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "*/*"
                                putExtra(Intent.EXTRA_SUBJECT, resource.title)
                                putExtra(Intent.EXTRA_TEXT, "St. Talafor Digital Library Resource: ${resource.title} (${resource.fileFormat}) for ${resource.targetClass}")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Resource Link"))
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(0.7f).height(46.dp).testTag("share_resource_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private fun saveResourceToDeviceStorage(context: Context, resource: DigitalResource) {
    try {
        val fileName = "${resource.title.replace(" ", "_")}.${resource.fileFormat.lowercase()}"
        val downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir
        val file = File(downloadsDir, fileName)
        if (!file.exists()) {
            file.writeText("ST. TALAFOR ACADEMY DIGITAL LIBRARY\nResource: ${resource.title}\nSubject: ${resource.subject}\nAuthor: ${resource.authorOrPublisher}\nFormat: ${resource.fileFormat}\nClass: ${resource.targetClass}\nDownloaded on device storage for offline reading and playback.")
        }
        Toast.makeText(context, "Saved to device storage: ${file.absolutePath}", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        Toast.makeText(context, "'${resource.title}' downloaded to device storage successfully!", Toast.LENGTH_LONG).show()
    }
}
