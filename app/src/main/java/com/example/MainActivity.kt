package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.components.AuthDialog
import com.example.ui.components.NotificationCenterSheet
import com.example.ui.components.RoleSelectorBar
import com.example.ui.screens.GuardianScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.ProprietorScreen
import com.example.ui.screens.SchoolCalendarScreen
import com.example.ui.screens.TeacherScreen
import com.example.ui.theme.AkomaSchoolTheme
import com.example.ui.viewmodel.SchoolViewModel
import com.example.ui.viewmodel.ViewMode

class MainActivity : ComponentActivity() {

    private val viewModel: SchoolViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AkomaSchoolTheme {
                val activeMode by viewModel.activeViewMode.collectAsState()
                val schoolName by viewModel.schoolName.collectAsState()
                val activeUserAccount by viewModel.activeUserAccount.collectAsState()
                val notifications by viewModel.notifications.collectAsState()
                val unreadCount by viewModel.unreadNotificationCount.collectAsState()
                val showAuthDialog by viewModel.showAuthDialog.collectAsState()
                val showNotificationCenter by viewModel.showNotificationCenter.collectAsState()

                if (showAuthDialog) {
                    AuthDialog(
                        currentActiveUser = activeUserAccount,
                        currentSchoolName = schoolName,
                        onDismissRequest = { viewModel.closeAuthDialog() },
                        onSignUpOrLogin = { fullName, email, phone, role, schoolNameInput ->
                            viewModel.signUpOrLoginUser(fullName, email, phone, role, schoolNameInput)
                        },
                        onSwitchExistingUser = { user ->
                            viewModel.signUpOrLoginUser(
                                fullName = user.fullName,
                                email = user.email,
                                phone = user.phone,
                                role = user.role,
                                schoolNameInput = user.schoolName
                            )
                        }
                    )
                }

                if (showNotificationCenter) {
                    NotificationCenterSheet(
                        roleName = activeMode.name,
                        notifications = notifications,
                        unreadCount = unreadCount,
                        onDismissRequest = { viewModel.toggleNotificationCenter() },
                        onMarkRead = { id -> viewModel.markNotificationRead(id) },
                        onMarkAllRead = { viewModel.markAllNotificationsRead() },
                        onClearAll = { viewModel.clearNotifications() }
                    )
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        RoleSelectorBar(
                            currentMode = activeMode,
                            schoolName = schoolName,
                            currentUserAccount = activeUserAccount,
                            unreadNotificationCount = unreadCount,
                            onModeSelected = { viewModel.setViewMode(it) },
                            onOpenAuthDialog = { viewModel.openAuthDialog() },
                            onOpenNotificationCenter = { viewModel.toggleNotificationCenter() },
                            modifier = Modifier.statusBarsPadding()
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        Crossfade(
                            targetState = activeMode,
                            animationSpec = tween(durationMillis = 250),
                            label = "ModeSwitch"
                        ) { mode ->
                            when (mode) {
                                ViewMode.HOME -> HomeScreen(viewModel = viewModel)
                                ViewMode.LOGIN -> LoginScreen(
                                    viewModel = viewModel,
                                    onLoginSuccess = {
                                        // Auto-redirect to the logged in role
                                    }
                                )
                                ViewMode.PROPRIETOR -> ProprietorScreen(viewModel = viewModel)
                                ViewMode.TEACHER -> TeacherScreen(viewModel = viewModel)
                                ViewMode.GUARDIAN -> GuardianScreen(viewModel = viewModel)
                                ViewMode.CALENDAR -> SchoolCalendarScreen(viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}

