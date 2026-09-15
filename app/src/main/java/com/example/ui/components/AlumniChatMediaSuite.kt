package com.example.ui.components

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.GhanaEmeraldGreen
import com.example.ui.theme.GhanaGoldAccent
import com.example.ui.theme.GhanaNavyPrimary
import kotlinx.coroutines.delay

/**
 * Data model for St. Talafor Alumni Stickers.
 */
data class AlumniSticker(
    val id: String,
    val emoji: String,
    val title: String,
    val subtitle: String,
    val gradientColors: List<Color>
)

val ALUMNI_STICKER_PACK = listOf(
    AlumniSticker("stk_1", "🎓", "Proud Talaforian", "St. Talafor Int. School", listOf(GhanaNavyPrimary, Color(0xFF1E3A8A))),
    AlumniSticker("stk_2", "🏆", "Excellence & Honor", "BECE Distinctions", listOf(Color(0xFFB45309), GhanaGoldAccent)),
    AlumniSticker("stk_3", "🇬🇭", "Nyame Nhyira Mo", "God Bless Our Alma Mater", listOf(Color(0xFF991B1B), GhanaEmeraldGreen)),
    AlumniSticker("stk_4", "📚", "Knowledge is Light", "Academic Heritage", listOf(Color(0xFF0F766E), Color(0xFF14B8A6))),
    AlumniSticker("stk_5", "🌟", "Class of 2018", "Forever Legends", listOf(Color(0xFF6B21A8), Color(0xFFA855F7))),
    AlumniSticker("stk_6", "🚀", "Tertiary & Beyond", "Future Shapers", listOf(Color(0xFF1D4ED8), Color(0xFF38BDF8))),
    AlumniSticker("stk_7", "🔥", "BECE Champions", "100% Pass Rate", listOf(Color(0xFFC2410C), Color(0xFFF97316))),
    AlumniSticker("stk_8", "💡", "Future Leaders", "Talafor Scholars", listOf(Color(0xFFEAB308), Color(0xFFCA8A04))),
    AlumniSticker("stk_9", "❤️", "Talafor Forever", "Once A Talaforian", listOf(Color(0xFFBE123C), Color(0xFFFB7185))),
    AlumniSticker("stk_10", "🤝", "Alumni Connect", "Mentorship & Growth", listOf(GhanaNavyPrimary, Color(0xFF0284C7))),
    AlumniSticker("stk_11", "🎖️", "Hall of Fame", "Distinguished Service", listOf(Color(0xFF854D0E), GhanaGoldAccent)),
    AlumniSticker("stk_12", "👏", "Ayekoo!", "Well Done, Fellow Alum", listOf(GhanaEmeraldGreen, Color(0xFF15803D)))
)

val CHAT_EMOJI_LIST = listOf(
    "😊", "😂", "🥳", "😎", "🥰", "😍", "🤩", "🤔",
    "😇", "👏", "👍", "🙌", "🤝", "💪", "🔥", "💯",
    "🎓", "🏫", "📚", "✏️", "🏆", "🎖️", "🥇", "🇬🇭",
    "❤️", "⭐", "🌟", "✨", "💡", "🚀", "🎉", "🙏",
    "😃", "😄", "😁", "😆", "🤗", "👋", "✌️", "🎯"
)

/**
 * Bottom Sheet / Dialog presenting the rich media action choices:
 * - Camera Photo
 * - Camera Video
 * - Device Gallery Image
 * - Device Audio / Music
 * - Voice Note Recorder
 * - Alumni Stickers
 */
@Composable
fun AlumniMediaUploadMenuSheet(
    onSelectOption: (String) -> Unit, // "CAMERA_PHOTO", "CAMERA_VIDEO", "DEVICE_IMAGE", "DEVICE_AUDIO", "VOICE_RECORD", "STICKER", "DOCUMENT"
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp)
                .testTag("alumni_media_menu_sheet")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Share with Alumni Hub",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = GhanaNavyPrimary
                        )
                        Text(
                            text = "Select media, capture camera or record voice",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Grid of Actions
                val mediaOptions = listOf(
                    MediaMenuAction("CAMERA_PHOTO", "Take Picture", "Use camera snapshot", Icons.Default.PhotoCamera, Color(0xFF0284C7)),
                    MediaMenuAction("CAMERA_VIDEO", "Record Video", "Camera video clip", Icons.Default.Videocam, Color(0xFFDC2626)),
                    MediaMenuAction("DEVICE_IMAGE", "Device Images", "Photos from gallery", Icons.Default.Image, Color(0xFF16A34A)),
                    MediaMenuAction("DEVICE_AUDIO", "Device Audio", "Audio & songs", Icons.Default.Audiotrack, Color(0xFF9333EA)),
                    MediaMenuAction("VOICE_RECORD", "Record Voice", "Instant voice memo", Icons.Default.Mic, Color(0xFFEA580C)),
                    MediaMenuAction("STICKER", "Alumni Stickers", "Custom badges & pride", Icons.Default.Loyalty, Color(0xFFCA8A04)),
                    MediaMenuAction("DOCUMENT", "Document / PDF", "Transcripts & files", Icons.Default.Description, Color(0xFF475569))
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.height(300.dp)
                ) {
                    items(mediaOptions) { item ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = item.color.copy(alpha = 0.1f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, item.color.copy(alpha = 0.3f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectOption(item.id)
                                    onDismiss()
                                }
                                .testTag("media_option_${item.id.lowercase()}")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(item.color),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.title,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = item.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = GhanaNavyPrimary
                                    )
                                    Text(
                                        text = item.subtitle,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

data class MediaMenuAction(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color
)

/**
 * Interactive Camera Viewfinder & Recording Dialog.
 * Enables both taking pictures and recording video using device camera simulation.
 */
@Composable
fun CameraCaptureDialog(
    initialMode: String = "PHOTO", // "PHOTO" or "VIDEO"
    onCapturePhoto: (fileName: String, caption: String) -> Unit,
    onRecordVideo: (fileName: String, caption: String, durationSeconds: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var isVideoMode by remember { mutableStateOf(initialMode == "VIDEO") }
    var isFrontCamera by remember { mutableStateOf(false) }
    var isFlashOn by remember { mutableStateOf(false) }
    var isRecordingVideo by remember { mutableStateOf(false) }
    var recordingSeconds by remember { mutableStateOf(0) }
    var capturedPhotoName by remember { mutableStateOf<String?>(null) }
    var recordedVideoName by remember { mutableStateOf<String?>(null) }
    var captionText by remember { mutableStateOf("") }

    // Recording timer
    LaunchedEffect(isRecordingVideo) {
        if (isRecordingVideo) {
            recordingSeconds = 0
            while (isRecordingVideo) {
                delay(1000)
                recordingSeconds++
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .testTag("camera_capture_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Camera Top Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { isFlashOn = !isFlashOn }) {
                            Icon(
                                imageVector = if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                contentDescription = "Flash",
                                tint = if (isFlashOn) GhanaGoldAccent else Color.White
                            )
                        }
                        IconButton(onClick = { isFrontCamera = !isFrontCamera }) {
                            Icon(
                                imageVector = Icons.Default.FlipCameraAndroid,
                                contentDescription = "Switch Camera",
                                tint = Color.White
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isVideoMode) Color(0xFFDC2626).copy(alpha = 0.2f) else Color(0xFF0284C7).copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = if (isVideoMode) "🎥 VIDEO MODE" else "📷 PHOTO MODE",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (isVideoMode) Color(0xFFFF6B6B) else Color(0xFF38BDF8),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                // Camera Viewfinder Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF263238), Color(0xFF102027), Color(0xFF000A12))
                            )
                        )
                        .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (capturedPhotoName == null && recordedVideoName == null) {
                        // Live Viewfinder
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (isVideoMode) Icons.Default.Videocam else Icons.Default.PhotoCamera,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.size(54.dp)
                            )
                            Text(
                                text = if (isFrontCamera) "Front Selfie Camera Active" else "Rear HD Camera Active",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            )
                            if (isRecordingVideo) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color.Red))
                                    Text(
                                        text = "RECORDING: ${String.format("%02d:%02d", recordingSeconds / 60, recordingSeconds % 60)}",
                                        color = Color.Red,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    } else {
                        // Captured Preview
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                imageVector = if (capturedPhotoName != null) Icons.Default.CheckCircle else Icons.Default.SlowMotionVideo,
                                contentDescription = null,
                                tint = GhanaEmeraldGreen,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = capturedPhotoName ?: recordedVideoName ?: "Media Ready",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = if (capturedPhotoName != null) "Photo Snapshot Captured (1080x1920)" else "Video Clip (${recordingSeconds}s) Encoded MP4",
                                color = Color.LightGray,
                                fontSize = 11.sp
                            )
                            TextButton(
                                onClick = {
                                    capturedPhotoName = null
                                    recordedVideoName = null
                                    recordingSeconds = 0
                                }
                            ) {
                                Text("Retake", color = Color(0xFFFFCC00))
                            }
                        }
                    }
                }

                // Optional Caption Input
                OutlinedTextField(
                    value = captionText,
                    onValueChange = { captionText = it },
                    placeholder = { Text("Add a caption for alumni...", color = Color.Gray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = GhanaGoldAccent,
                        unfocusedBorderColor = Color.Gray
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("camera_caption_input")
                )

                // Shutter & Controls
                if (capturedPhotoName == null && recordedVideoName == null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Mode Switcher Button
                        IconButton(
                            onClick = {
                                isVideoMode = !isVideoMode
                                isRecordingVideo = false
                            }
                        ) {
                            Icon(
                                imageVector = if (isVideoMode) Icons.Default.CameraAlt else Icons.Default.Videocam,
                                contentDescription = "Switch Mode",
                                tint = Color.White
                            )
                        }

                        // Shutter Button
                        if (!isVideoMode) {
                            // Snap Photo
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .clickable {
                                        val photoName = "Camera_Snap_${System.currentTimeMillis() % 10000}.jpg"
                                        capturedPhotoName = photoName
                                        Toast.makeText(context, "Picture taken!", Toast.LENGTH_SHORT).show()
                                    }
                                    .testTag("camera_shutter_photo_btn"),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, Color.Black, CircleShape)
                                        .background(Color.White)
                                )
                            }
                        } else {
                            // Video Record / Stop
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(CircleShape)
                                    .background(if (isRecordingVideo) Color.Red else Color.White)
                                    .clickable {
                                        if (!isRecordingVideo) {
                                            isRecordingVideo = true
                                        } else {
                                            isRecordingVideo = false
                                            recordedVideoName = "Camera_Video_${System.currentTimeMillis() % 10000}.mp4"
                                            Toast.makeText(context, "Video clip recorded!", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    .testTag("camera_shutter_video_btn"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isRecordingVideo) {
                                    Box(modifier = Modifier.size(24.dp).clip(RoundedCornerShape(4.dp)).background(Color.White))
                                } else {
                                    Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.Red))
                                }
                            }
                        }

                        // Spacer to balance layout
                        Spacer(modifier = Modifier.size(48.dp))
                    }
                } else {
                    // Confirm & Send Button
                    Button(
                        onClick = {
                            if (capturedPhotoName != null) {
                                onCapturePhoto(capturedPhotoName!!, captionText.ifBlank { "Photo from camera" })
                            } else if (recordedVideoName != null) {
                                onRecordVideo(recordedVideoName!!, captionText.ifBlank { "Video from camera" }, maxOf(recordingSeconds, 3))
                            }
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GhanaEmeraldGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("camera_send_media_btn")
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Send to Alumni Chat", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Live Voice Note Recorder Dialog.
 */
@Composable
fun VoiceRecorderDialog(
    onSendVoiceNote: (fileName: String, durationText: String) -> Unit,
    onDismiss: () -> Unit
) {
    var isRecording by remember { mutableStateOf(false) }
    var recordingSeconds by remember { mutableStateOf(0) }
    var isRecorded by remember { mutableStateOf(false) }
    var isPlayingPreview by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingSeconds = 0
            while (isRecording) {
                delay(1000)
                recordingSeconds++
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp)
                .testTag("voice_recorder_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Record Voice Note", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = GhanaNavyPrimary)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Waveform / Timer Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(GhanaNavyPrimary.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = String.format("%02d:%02d", recordingSeconds / 60, recordingSeconds % 60),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            color = if (isRecording) Color.Red else GhanaNavyPrimary
                        )

                        // Animated Wave Bars
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val barHeights = listOf(12, 24, 38, 18, 30, 42, 20, 34, 16, 28, 36, 14)
                            barHeights.forEachIndexed { idx, baseHeight ->
                                val heightMultiplier = if (isRecording) ((idx % 3 + 1) * 0.4f + 0.6f) else 0.5f
                                Box(
                                    modifier = Modifier
                                        .width(4.dp)
                                        .height((baseHeight * heightMultiplier).dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(if (isRecording) Color.Red else GhanaNavyPrimary)
                                )
                            }
                        }
                    }
                }

                // Control Center
                if (!isRecorded) {
                    if (!isRecording) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEA580C))
                                    .clickable { isRecording = true }
                                    .testTag("start_recording_btn"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Mic, contentDescription = "Start Recording", tint = Color.White, modifier = Modifier.size(32.dp))
                            }
                            Text("Tap mic to start speaking", fontSize = 12.sp, color = Color.Gray)
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(Color.Red)
                                    .clickable {
                                        isRecording = false
                                        isRecorded = true
                                    }
                                    .testTag("stop_recording_btn"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = "Stop Recording", tint = Color.White, modifier = Modifier.size(32.dp))
                            }
                            Text("Recording in progress... Tap to finish", fontSize = 12.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // Preview & Send
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                isRecorded = false
                                recordingSeconds = 0
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Discard", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                val durationText = String.format("%02d:%02d", recordingSeconds / 60, maxOf(1, recordingSeconds % 60))
                                val fileName = "Voice_Note_${System.currentTimeMillis() % 10000}.m4a"
                                onSendVoiceNote(fileName, durationText)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GhanaNavyPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(2f).testTag("send_voice_note_btn")
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Send Voice Note", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Device Media & Audio Selector Dialog.
 */
@Composable
fun DeviceMediaPickerDialog(
    mediaCategory: String, // "IMAGE" or "AUDIO" or "DOCUMENT"
    onMediaSelected: (fileName: String, mediaUrl: String, caption: String) -> Unit,
    onDismiss: () -> Unit
) {
    var captionText by remember { mutableStateOf("") }
    var selectedFileIndex by remember { mutableStateOf(0) }

    val sampleFiles = when (mediaCategory) {
        "AUDIO" -> listOf(
            Triple("St_Talafor_School_Anthem.mp3", "4.2 MB • 03:15 Audio", "https://audio.example.com/anthem.mp3"),
            Triple("Speech_Day_Keynote_2019.m4a", "6.8 MB • 05:40 Audio", "https://audio.example.com/keynote.m4a"),
            Triple("Class_of_2018_Farewell_Song.mp3", "3.5 MB • 02:50 Audio", "https://audio.example.com/farewell.mp3"),
            Triple("Headmaster_Excellence_Address.wav", "8.1 MB • 06:10 Audio", "https://audio.example.com/address.wav")
        )
        "DOCUMENT" -> listOf(
            Triple("Alumni_Constitution_2026.pdf", "1.2 MB • GES Certified Document", "https://doc.example.com/constitution.pdf"),
            Triple("BECE_Historical_Honour_Roll.pdf", "3.4 MB • Official Records", "https://doc.example.com/honour.pdf"),
            Triple("Alumni_Dues_Statement.pdf", "850 KB • Financial Report", "https://doc.example.com/statement.pdf")
        )
        else -> listOf(
            Triple("Campus_Reunion_Group_2024.jpg", "2.4 MB • 1920x1080 JPEG", "https://images.unsplash.com/photo-1523050854058-8df90110c9f1?w=600"),
            Triple("Speech_Day_Graduation_Class.jpg", "3.1 MB • High Definition", "https://images.unsplash.com/photo-1541339907198-e08756dedf3f?w=600"),
            Triple("BECE_Award_Celebration.png", "1.9 MB • Certificate Photo", "https://images.unsplash.com/photo-1523240795612-9a054b0db644?w=600"),
            Triple("Old_Library_Archive_Photo.jpg", "2.8 MB • Campus Milestone", "https://images.unsplash.com/photo-1509062522246-3755977927d7?w=600")
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp)
                .testTag("device_media_picker_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = when (mediaCategory) {
                                "AUDIO" -> "Select Audio from Device"
                                "DOCUMENT" -> "Select Document / Record"
                                else -> "Select Image from Device Gallery"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = GhanaNavyPrimary
                        )
                        Text(
                            text = "Browse local files on device storage",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // File list
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    sampleFiles.forEachIndexed { index, (name, meta, url) ->
                        val isSelected = selectedFileIndex == index
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) GhanaNavyPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, GhanaNavyPrimary) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedFileIndex = index }
                                .padding(2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = when (mediaCategory) {
                                        "AUDIO" -> Icons.Default.Audiotrack
                                        "DOCUMENT" -> Icons.Default.PictureAsPdf
                                        else -> Icons.Default.Image
                                    },
                                    contentDescription = null,
                                    tint = if (isSelected) GhanaNavyPrimary else Color.Gray
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(name, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = GhanaNavyPrimary)
                                    Text(meta, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (isSelected) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GhanaNavyPrimary, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = captionText,
                    onValueChange = { captionText = it },
                    label = { Text("Caption or Message (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("device_media_caption_input")
                )

                Button(
                    onClick = {
                        val file = sampleFiles[selectedFileIndex]
                        onMediaSelected(file.first, file.third, captionText.ifBlank { "Shared ${file.first}" })
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GhanaNavyPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("device_media_send_btn")
                ) {
                    Icon(Icons.Default.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Send to Alumni Chat", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Dedicated Emoji & Sticker Drawer/Tray.
 * Displays emojis and St. Talafor stickers.
 */
@Composable
fun EmojiAndStickerTray(
    onSelectEmoji: (String) -> Unit,
    onSendSticker: (AlumniSticker) -> Unit,
    onClose: () -> Unit
) {
    var activeTab by remember { mutableStateOf(0) } // 0: Emojis, 1: Stickers

    Card(
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .testTag("emoji_sticker_tray")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Tab Switcher and Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        label = { Text("😊 Emojis") },
                        modifier = Modifier.testTag("tray_tab_emojis")
                    )
                    FilterChip(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        label = { Text("🏷️ Stickers (${ALUMNI_STICKER_PACK.size})") },
                        modifier = Modifier.testTag("tray_tab_stickers")
                    )
                }
                IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Close Tray")
                }
            }

            if (activeTab == 0) {
                // Emojis Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(CHAT_EMOJI_LIST) { emoji ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .clickable { onSelectEmoji(emoji) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(emoji, fontSize = 22.sp)
                        }
                    }
                }
            } else {
                // Stickers Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(ALUMNI_STICKER_PACK) { sticker ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.Transparent,
                            border = androidx.compose.foundation.BorderStroke(1.dp, GhanaGoldAccent.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(85.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Brush.verticalGradient(sticker.gradientColors))
                                .clickable { onSendSticker(sticker) }
                                .testTag("sticker_${sticker.id}")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(sticker.emoji, fontSize = 26.sp)
                                Text(
                                    text = sticker.title,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                                Text(
                                    text = sticker.subtitle,
                                    fontSize = 8.sp,
                                    color = Color.White.copy(alpha = 0.8f),
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
