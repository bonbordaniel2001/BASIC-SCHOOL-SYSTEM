package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserAccount
import com.example.ui.theme.GhanaGoldAccent
import com.example.ui.theme.GhanaGoldContainer
import com.example.ui.theme.GhanaNavyPrimary
import com.example.ui.viewmodel.ViewMode

@Composable
fun RoleSelectorBar(
    currentMode: ViewMode,
    schoolName: String,
    currentUserAccount: UserAccount?,
    unreadNotificationCount: Int,
    onModeSelected: (ViewMode) -> Unit,
    onOpenAuthDialog: () -> Unit,
    onOpenNotificationCenter: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            // Header Row: Logo, School Name, Notification Bell & Account Switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(GhanaNavyPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = "School Logo",
                            tint = GhanaGoldAccent,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (schoolName.isNotBlank()) schoolName else "Akoma Primary & JHS",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Sibi, Oti Region, Ghana",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Notification Bell Icon with Badge
                    IconButton(
                        onClick = onOpenNotificationCenter,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("notification_bell_icon_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadNotificationCount > 0) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = Color.White
                                    ) {
                                        Text(
                                            text = if (unreadNotificationCount > 99) "99+" else unreadNotificationCount.toString(),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = GhanaNavyPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // Account / Profile Pill
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = GhanaNavyPrimary.copy(alpha = 0.08f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { onOpenAuthDialog() }
                            .testTag("open_auth_dialog_pill")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(GhanaNavyPrimary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = currentUserAccount?.fullName?.take(1) ?: "U",
                                    color = GhanaGoldAccent,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column {
                                Text(
                                    text = currentUserAccount?.fullName?.split(" ")?.firstOrNull() ?: "User",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = currentUserAccount?.role ?: currentMode.name,
                                    fontSize = 9.sp,
                                    color = GhanaNavyPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "Switch Account",
                                modifier = Modifier.size(16.dp),
                                tint = GhanaNavyPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 5 View Mode Navigation Tabs (Home, Proprietor, Teacher, Guardian, Calendar)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ModeTabButton(
                    title = "Home",
                    icon = Icons.Default.Home,
                    isSelected = currentMode == ViewMode.HOME,
                    onClick = { onModeSelected(ViewMode.HOME) },
                    testTag = "tab_home",
                    modifier = Modifier.weight(1f)
                )

                ModeTabButton(
                    title = "Proprietor",
                    icon = Icons.Default.AdminPanelSettings,
                    isSelected = currentMode == ViewMode.PROPRIETOR,
                    onClick = { onModeSelected(ViewMode.PROPRIETOR) },
                    testTag = "tab_proprietor",
                    modifier = Modifier.weight(1f)
                )

                ModeTabButton(
                    title = "Teacher",
                    icon = Icons.Default.CoPresent,
                    isSelected = currentMode == ViewMode.TEACHER,
                    onClick = { onModeSelected(ViewMode.TEACHER) },
                    testTag = "tab_teacher",
                    modifier = Modifier.weight(1f)
                )

                ModeTabButton(
                    title = "Guardian",
                    icon = Icons.Default.FamilyRestroom,
                    isSelected = currentMode == ViewMode.GUARDIAN,
                    onClick = { onModeSelected(ViewMode.GUARDIAN) },
                    testTag = "tab_guardian",
                    modifier = Modifier.weight(1f)
                )

                ModeTabButton(
                    title = "Calendar",
                    icon = Icons.Default.CalendarMonth,
                    isSelected = currentMode == ViewMode.CALENDAR,
                    onClick = { onModeSelected(ViewMode.CALENDAR) },
                    testTag = "tab_calendar",
                    modifier = Modifier.weight(1f)
                )

                ModeTabButton(
                    title = "Alumni",
                    icon = Icons.Default.Groups,
                    isSelected = currentMode == ViewMode.ALUMNI,
                    onClick = { onModeSelected(ViewMode.ALUMNI) },
                    testTag = "tab_alumni",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ModeTabButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) GhanaNavyPrimary else Color.Transparent
    val contentColor = if (isSelected) GhanaGoldAccent else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = backgroundColor,
        modifier = modifier
            .testTag(testTag)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else contentColor,
                fontSize = 12.sp,
                maxLines = 1
            )
        }
    }
}
