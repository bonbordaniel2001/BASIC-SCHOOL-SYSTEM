package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GhanaGoldAccent
import com.example.ui.theme.GhanaNavyPrimary

/**
 * Reusable LoadingSpinner component with custom styling and text for async operations across portal screens.
 */
@Composable
fun LoadingSpinner(
    modifier: Modifier = Modifier,
    message: String = "Synchronizing data...",
    subMessage: String? = "Please wait a moment while details are fetched",
    inline: Boolean = false
) {
    if (inline) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("inline_loading_spinner"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(28.dp),
                color = GhanaNavyPrimary,
                strokeWidth = 3.dp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = message,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GhanaNavyPrimary
                )
                if (subMessage != null) {
                    Text(
                        text = subMessage,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    } else {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(24.dp)
                .testTag("card_loading_spinner"),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                shadowElevation = 4.dp,
                modifier = Modifier.widthIn(max = 340.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(64.dp)
                            .background(GhanaNavyPrimary.copy(alpha = 0.08f), CircleShape)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(44.dp),
                            color = GhanaNavyPrimary,
                            trackColor = GhanaGoldAccent.copy(alpha = 0.3f),
                            strokeWidth = 4.dp
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = message,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = GhanaNavyPrimary,
                        textAlign = TextAlign.Center
                    )
                    if (subMessage != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = subMessage,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

/**
 * Overlay spinner for full-screen loading transitions
 */
@Composable
fun LoadingOverlay(
    isLoading: Boolean,
    message: String = "Fetching portal data...",
    content: (@Composable () -> Unit)? = null
) {
    if (content != null) {
        Box(modifier = Modifier.fillMaxSize()) {
            content()
            AnimatedVisibility(
                visible = isLoading,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.35f))
                        .testTag("fullscreen_loading_overlay"),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingSpinner(message = message)
                }
            }
        }
    } else {
        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f))
                    .testTag("fullscreen_loading_overlay"),
                contentAlignment = Alignment.Center
            ) {
                LoadingSpinner(message = message)
            }
        }
    }
}
