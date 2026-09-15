package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.example.data.model.DigitalResource
import com.example.ui.theme.GhanaEmeraldGreen
import com.example.ui.theme.GhanaGoldAccent
import com.example.ui.theme.GhanaNavyPrimary

/**
 * Dialog shown upon successfully downloading a media item (Audio, Video, Document, etc.)
 * from the St. Talafor Digital Library by Proprietor, Teachers, or Guardians.
 */
@Composable
fun DownloadedMediaDialog(
    resource: DigitalResource,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isAudio = resource.resourceType == "AUDIO" || resource.fileFormat.equals("MP3", ignoreCase = true) || resource.fileFormat.equals("WAV", ignoreCase = true)
    val isVideo = resource.resourceType == "VIDEO" || resource.fileFormat.equals("MP4", ignoreCase = true)
    val isDocument = resource.resourceType == "DOCUMENT" || resource.fileFormat.equals("PDF", ignoreCase = true) || resource.fileFormat.equals("EPUB", ignoreCase = true)

    val iconVector = when {
        isAudio -> Icons.Default.Audiotrack
        isVideo -> Icons.Default.VideoLibrary
        isDocument -> Icons.Default.Description
        else -> Icons.Default.DownloadDone
    }

    val typeLabel = when {
        isAudio -> "Audio Media Lesson / Track"
        isVideo -> "Educational Video Resource"
        isDocument -> "Official Document / E-Book"
        else -> "Digital Resource"
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
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(16.dp)
                .testTag("downloaded_media_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
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
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(GhanaEmeraldGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(iconVector, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                        Column {
                            Text(
                                text = "Media Ready Offline",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = GhanaNavyPrimary
                            )
                            Text(
                                text = typeLabel,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // File Details Container
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(resource.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = GhanaNavyPrimary)
                        Text("Author/Source: ${resource.authorOrPublisher}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(shape = RoundedCornerShape(6.dp), color = GhanaGoldAccent.copy(alpha = 0.25f)) {
                                Text("Format: ${resource.fileFormat}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GhanaNavyPrimary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                            Surface(shape = RoundedCornerShape(6.dp), color = GhanaNavyPrimary.copy(alpha = 0.1f)) {
                                Text("Size: ${(resource.fileSizeBytes / 1024 / 1024).coerceAtLeast(1)} MB", fontSize = 10.sp, color = GhanaNavyPrimary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                            Surface(shape = RoundedCornerShape(6.dp), color = GhanaEmeraldGreen.copy(alpha = 0.15f)) {
                                Text("Target: ${resource.targetClass}", fontSize = 10.sp, color = Color(0xFF0F5132), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }

                        if (resource.description.isNotBlank()) {
                            Text(resource.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Text(
                            text = "Storage Location: Downloaded to device storage (${resource.fileUrlOrPath})",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            Toast.makeText(context, "Playing / Opening '${resource.title}' in media player...", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GhanaNavyPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("play_media_button")
                    ) {
                        Icon(
                            imageVector = if (isAudio || isVideo) Icons.Default.PlayArrow else Icons.Default.Visibility,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isAudio || isVideo) "Play / View" else "Open File", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "*/*"
                                putExtra(Intent.EXTRA_SUBJECT, resource.title)
                                putExtra(Intent.EXTRA_TEXT, "Shared from St. Talafor Digital Library: ${resource.title} (${resource.fileFormat}) by ${resource.authorOrPublisher}")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Media Resource"))
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("share_media_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share Media", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
