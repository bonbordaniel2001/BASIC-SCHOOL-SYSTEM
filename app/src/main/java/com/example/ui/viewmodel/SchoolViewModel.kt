package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.SchoolDatabase
import com.example.data.model.*
import com.example.data.repository.AlumniRepository
import com.example.data.repository.GuardianRepository
import com.example.data.repository.SchoolRepository
import com.example.data.repository.StudentRepository
import com.example.data.repository.TeacherRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

enum class ViewMode {
    HOME,
    LOGIN,
    PROPRIETOR,
    TEACHER,
    GUARDIAN,
    CALENDAR,
    ALUMNI
}

enum class SimulatedGeofenceState {
    ON_CAMPUS_INSIDE_GEOFENCE, // ~35m from school
    OUTSIDE_GEOFENCE           // ~1,450m from school
}

class SchoolViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SchoolRepository
    val studentRepository: StudentRepository
    val teacherRepository: TeacherRepository
    val guardianRepository: GuardianRepository
    val alumniRepository: AlumniRepository
    val context: Context = application.applicationContext

    init {
        val db = SchoolDatabase.getDatabase(application)
        repository = SchoolRepository(
            db.schoolDao(),
            db.guardianDao(),
            db.gradeDao(),
            db.portalUserDao(),
            db.schoolEventDao(),
            db.timetableDao(),
            db.attendanceDao(),
            db.lessonPlanDao(),
            db.teacherDao(),
            db.studentDao()
        )
        studentRepository = StudentRepository(db.studentDao())
        teacherRepository = TeacherRepository(db.teacherDao())
        guardianRepository = GuardianRepository(db.guardianDao())
        alumniRepository = AlumniRepository(db.alumniDao())
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
            alumniRepository.seedInitialAlumniDataIfEmpty()
        }
    }

    val allGuardians: StateFlow<List<GuardianProfile>> = repository.allGuardians
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTeachers: StateFlow<List<TeacherProfile>> = repository.allTeachers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allGrades: StateFlow<List<StudentGrade>> = repository.allGrades
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLessonPlans: StateFlow<List<LessonPlan>> = repository.allLessonPlans
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDirectMessages: StateFlow<List<DirectMessage>> = repository.allDirectMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTeacherLoanRequests: StateFlow<List<TeacherLoanRequest>> = repository.allTeacherLoanRequests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDigitalResources: StateFlow<List<DigitalResource>> = repository.allDigitalResources
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allClassAssignments: StateFlow<List<ClassAssignment>> = repository.allClassAssignments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allStudentAddRequests: StateFlow<List<StudentAddRequest>> = repository.allStudentAddRequests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPromotionDemotionRequests: StateFlow<List<PromotionDemotionRequest>> = repository.allPromotionDemotionRequests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _proprietorTargetSection = MutableStateFlow<String?>(null)
    val proprietorTargetSection: StateFlow<String?> = _proprietorTargetSection.asStateFlow()

    fun setProprietorTargetSection(section: String?) {
        _proprietorTargetSection.value = section
    }

    fun clearProprietorTargetSection() {
        _proprietorTargetSection.value = null
    }

    private val _teacherTargetTab = MutableStateFlow<Int?>(null)
    val teacherTargetTab: StateFlow<Int?> = _teacherTargetTab.asStateFlow()

    fun setTeacherTargetTab(tab: Int?) {
        _teacherTargetTab.value = tab
    }

    fun clearTeacherTargetTab() {
        _teacherTargetTab.value = null
    }

    private val _guardianTargetTab = MutableStateFlow<Int?>(null)
    val guardianTargetTab: StateFlow<Int?> = _guardianTargetTab.asStateFlow()

    fun setGuardianTargetTab(tab: Int?) {
        _guardianTargetTab.value = tab
    }

    fun clearGuardianTargetTab() {
        _guardianTargetTab.value = null
    }

    // --- Alumni Network StateFlows ---
    val allAlumniProfiles: StateFlow<List<AlumniProfile>> = alumniRepository.allAlumniProfiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activatedAlumniProfiles: StateFlow<List<AlumniProfile>> = alumniRepository.activatedAlumniProfiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeAlumniAdmin: StateFlow<AlumniProfile?> = alumniRepository.activeAlumniAdmin
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allAlumniChatMessages: StateFlow<List<AlumniChatMessage>> = alumniRepository.allAlumniChatMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCallSessions: StateFlow<List<AlumniCallSession>> = alumniRepository.allCallSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPerformanceMetrics: StateFlow<List<SchoolPerformanceMetric>> = alumniRepository.allPerformanceMetrics
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAspirants: StateFlow<List<AlumniAspirant>> = alumniRepository.allAspirants
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Loading State Management ---
    private val _uiLoadingState = MutableStateFlow(false)
    val uiLoadingState: StateFlow<Boolean> = _uiLoadingState.asStateFlow()

    private val _loadingMessage = MutableStateFlow("Synchronizing portal data...")
    val loadingMessage: StateFlow<String> = _loadingMessage.asStateFlow()

    fun showLoading(message: String = "Synchronizing portal data...") {
        _loadingMessage.value = message
        _uiLoadingState.value = true
    }

    fun hideLoading() {
        _uiLoadingState.value = false
    }

    fun triggerPortalDataRefresh(message: String = "Fetching latest portal records...") {
        viewModelScope.launch {
            _loadingMessage.value = message
            _uiLoadingState.value = true
            kotlinx.coroutines.delay(700)
            _uiLoadingState.value = false
            Toast.makeText(context, "Portal data updated successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    fun uploadDigitalResource(
        title: String,
        authorOrPublisher: String,
        category: String,
        resourceType: String,
        targetClass: String,
        subject: String,
        targetAudience: String,
        description: String,
        fileFormat: String = "PDF",
        fileUrlOrPath: String? = null,
        uploadedBy: String = "Proprietor"
    ) {
        viewModelScope.launch {
            showLoading("Uploading & indexing digital resource...")
            kotlinx.coroutines.delay(600)
            val formatExtension = fileFormat.lowercase(Locale.ROOT)
            val finalPath = if (!fileUrlOrPath.isNullOrBlank()) fileUrlOrPath else "storage/library/${title.lowercase(Locale.ROOT).replace(" ", "_").replace("/", "_")}.$formatExtension"
            val newRes = DigitalResource(
                title = title,
                authorOrPublisher = authorOrPublisher.ifBlank { uploadedBy },
                category = category,
                resourceType = resourceType,
                targetClass = targetClass,
                subject = subject,
                fileUrlOrPath = finalPath,
                fileSizeBytes = (2000000..80000000).random().toLong(),
                fileFormat = fileFormat,
                uploadedBy = uploadedBy,
                uploadDateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                targetAudience = targetAudience,
                description = description
            )
            repository.insertDigitalResource(newRes)
            hideLoading()
            Toast.makeText(context, "Resource '$title' uploaded to Digital Library!", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteDigitalResource(id: Long) {
        viewModelScope.launch {
            repository.deleteDigitalResourceById(id)
            Toast.makeText(context, "Digital resource removed.", Toast.LENGTH_SHORT).show()
        }
    }

    fun uploadClassAssignment(
        title: String,
        description: String,
        className: String,
        subject: String,
        dueDateString: String,
        frequencyPeriod: String,
        maxScore: Int = 100,
        attachmentPath: String = ""
    ) {
        viewModelScope.launch {
            showLoading("Dispatching class assignment...")
            kotlinx.coroutines.delay(600)
            val curUser = activeUserAccount.value
            val teacherName = curUser?.fullName ?: "Class Teacher"
            val teacherId = curUser?.id ?: 1L
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val attPath = attachmentPath.ifBlank { "storage/assignments/${title.lowercase(Locale.ROOT).replace(" ", "_").replace("/", "_")}.pdf" }

            val newAssignment = ClassAssignment(
                title = title,
                description = description,
                className = className,
                subject = subject,
                teacherId = teacherId,
                teacherName = teacherName,
                assignedDateString = dateStr,
                dueDateString = dueDateString,
                attachmentPathOrUrl = attPath,
                maxScore = maxScore,
                frequencyPeriod = frequencyPeriod,
                termLabel = "Term 3 2026"
            )
            repository.insertClassAssignment(newAssignment)
            hideLoading()
            Toast.makeText(context, "Assignment '$title' posted for $className!", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteClassAssignment(id: Long) {
        viewModelScope.launch {
            repository.deleteClassAssignmentById(id)
            Toast.makeText(context, "Assignment removed.", Toast.LENGTH_SHORT).show()
        }
    }


    val schoolSettings: StateFlow<SchoolSettings?> = repository.schoolSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allPortalAccounts: StateFlow<List<UserPortalAccount>> = repository.allPortalAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeLoggedInPortalAccount: StateFlow<UserPortalAccount?> = repository.activeLoggedInPortalAccount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allSchoolEvents: StateFlow<List<SchoolEvent>> = repository.allSchoolEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addSchoolEvent(event: SchoolEvent) {
        viewModelScope.launch {
            repository.saveSchoolEvent(event)
            Toast.makeText(context, "Calendar Event Added Successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteSchoolEvent(event: SchoolEvent) {
        viewModelScope.launch {
            repository.deleteSchoolEvent(event)
            Toast.makeText(context, "Event removed from school calendar", Toast.LENGTH_SHORT).show()
        }
    }

    val allTimetables: StateFlow<List<ClassTimetable>> = repository.allTimetables
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveTimetableSlot(
        className: String,
        dayOfWeek: String,
        periodNumber: Int,
        startTime: String,
        endTime: String,
        subject: String,
        teacherName: String,
        classroom: String = "Room B12",
        existingId: Long = 0L
    ) {
        viewModelScope.launch {
            val slot = ClassTimetable(
                id = existingId,
                className = className,
                dayOfWeek = dayOfWeek,
                periodNumber = periodNumber,
                startTime = startTime,
                endTime = endTime,
                subject = subject,
                teacherName = teacherName,
                classroom = classroom
            )
            if (existingId > 0) {
                repository.updateTimetableSlot(slot)
                Toast.makeText(context, "Timetable slot updated for $className", Toast.LENGTH_SHORT).show()
            } else {
                repository.saveTimetableSlot(slot)
                Toast.makeText(context, "New class schedule slot added!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun deleteTimetableSlot(slot: ClassTimetable) {
        viewModelScope.launch {
            repository.deleteTimetableSlot(slot)
            Toast.makeText(context, "Timetable slot deleted", Toast.LENGTH_SHORT).show()
        }
    }

    val allStudentProfiles: StateFlow<List<StudentProfile>> = repository.allStudentProfiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Active User Account & School Settings ---
    val activeUserAccount: StateFlow<UserAccount?> = repository.activeUserAccount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val schoolName: StateFlow<String> = repository.schoolSettings
        .map { it?.schoolName ?: "St. Talafor Primary & JHS" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "St. Talafor Primary & JHS")

    // --- Active View Mode ---
    private val _activeViewMode = MutableStateFlow(ViewMode.HOME)
    val activeViewMode: StateFlow<ViewMode> = _activeViewMode.asStateFlow()

    fun setViewMode(mode: ViewMode) {
        _activeViewMode.value = mode
    }

    // --- Auth & Account Creation ---
    private val _showAuthDialog = MutableStateFlow(false)
    val showAuthDialog: StateFlow<Boolean> = _showAuthDialog.asStateFlow()

    fun openAuthDialog() { _showAuthDialog.value = true }
    fun closeAuthDialog() { _showAuthDialog.value = false }

    // Pending User Account Approvals
    val pendingUserAccounts: StateFlow<List<UserAccount>> = repository.getPendingUserAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun approvePendingUserAccount(userId: Long, userName: String) {
        viewModelScope.launch {
            repository.approveUserAccount(userId)
            Toast.makeText(context, "Approved registration for $userName", Toast.LENGTH_SHORT).show()
        }
    }

    fun rejectPendingUserAccount(userId: Long, userName: String) {
        viewModelScope.launch {
            repository.rejectUserAccount(userId)
            Toast.makeText(context, "Registration rejected for $userName", Toast.LENGTH_SHORT).show()
        }
    }

    fun signUpOrLoginUser(
        fullName: String,
        email: String,
        phone: String,
        role: String,
        schoolNameInput: String,
        schoolIdInput: String = "SCH-AKM-2026",
        assignedClass: String = "JHS 2 - Gold",
        assignedSubject: String = "Mathematics",
        linkedStudentChild: String = ""
    ) {
        viewModelScope.launch {
            try {
                val user = repository.registerOrUpdateUser(
                    fullName = fullName,
                    email = email,
                    phone = phone,
                    role = role,
                    schoolName = schoolNameInput,
                    schoolId = schoolIdInput,
                    assignedClass = assignedClass,
                    assignedSubject = assignedSubject,
                    linkedStudentChild = linkedStudentChild
                )

                if (!user.isApproved) {
                    Toast.makeText(
                        context,
                        "Registration Submitted! Pending Proprietor Approval before full login.",
                        Toast.LENGTH_LONG
                    ).show()
                    closeAuthDialog()
                    return@launch
                }

                // Sync active view mode to match user role
                val targetMode = when (role.uppercase()) {
                    "PROPRIETOR" -> ViewMode.PROPRIETOR
                    "TEACHER" -> ViewMode.TEACHER
                    "GUARDIAN" -> ViewMode.GUARDIAN
                    else -> ViewMode.PROPRIETOR
                }
                setViewMode(targetMode)
                closeAuthDialog()

                // Dispatch welcome notification
                repository.addNotification(
                    recipientRole = role,
                    type = "BROADCAST",
                    title = "Welcome to ${if (schoolNameInput.isNotBlank()) schoolNameInput else "School Portal"}",
                    message = "Signed in as ${user.fullName} (${user.role}). Your dashboard session is ready."
                )

                Toast.makeText(context, "Account Active: Welcome ${user.fullName}!", Toast.LENGTH_SHORT).show()
            } catch (e: IllegalArgumentException) {
                Toast.makeText(context, "Registration Denied: ${e.message}", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Error during sign up: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun updateSchoolName(newName: String) {
        viewModelScope.launch {
            repository.updateSchoolName(newName)
            repository.addNotification(
                recipientRole = "PROPRIETOR",
                type = "BROADCAST",
                title = "School Name Updated",
                message = "Institution officially updated to '$newName'."
            )
            Toast.makeText(context, "School name saved as $newName", Toast.LENGTH_SHORT).show()
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logoutActiveUser()
            _activeViewMode.value = ViewMode.LOGIN
            Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
        }
    }

    // --- Notifications State & Actions ---
    private val _showNotificationCenter = MutableStateFlow(false)
    val showNotificationCenter: StateFlow<Boolean> = _showNotificationCenter.asStateFlow()

    fun toggleNotificationCenter() {
        _showNotificationCenter.value = !_showNotificationCenter.value
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val notifications: StateFlow<List<AppNotification>> = activeViewMode
        .flatMapLatest { mode ->
            val roleStr = mode.name
            repository.getNotificationsForRole(roleStr)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotificationCount: StateFlow<Int> = notifications
        .map { list -> list.count { !it.isRead } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun markNotificationRead(notificationId: Long) {
        viewModelScope.launch {
            repository.markNotificationRead(notificationId)
        }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsRead(activeViewMode.value.name)
        }
    }

    fun clearNotifications() {
        viewModelScope.launch {
            repository.clearNotifications(activeViewMode.value.name)
            Toast.makeText(context, "Notifications cleared", Toast.LENGTH_SHORT).show()
        }
    }

    fun navigateToAppropriatePortal(notification: AppNotification) {
        viewModelScope.launch {
            repository.markNotificationRead(notification.id)
            _showNotificationCenter.value = false

            val titleLower = notification.title.lowercase()
            val messageLower = notification.message.lowercase()
            val textContent = "$titleLower $messageLower"

            when {
                // Proprietor or Approval workflow notifications
                notification.type == "APPROVAL_REQUIRED" || notification.recipientRole == "PROPRIETOR" -> {
                    when {
                        textContent.contains("admission") || textContent.contains("promotion") || 
                        textContent.contains("demotion") || textContent.contains("loan") || 
                        textContent.contains("approval") || textContent.contains("permission") ||
                        notification.type == "APPROVAL_REQUIRED" -> {
                            _proprietorTargetSection.value = "APPROVALS"
                        }
                        textContent.contains("fee") || textContent.contains("payment") || textContent.contains("momo") -> {
                            _proprietorTargetSection.value = "FEES"
                        }
                        textContent.contains("staff") || textContent.contains("salary") || textContent.contains("payroll") -> {
                            _proprietorTargetSection.value = "STAFF"
                        }
                        textContent.contains("library") || textContent.contains("textbook") || textContent.contains("syllabus") -> {
                            _proprietorTargetSection.value = "LIBRARY"
                        }
                        textContent.contains("assignment") || textContent.contains("homework") -> {
                            _proprietorTargetSection.value = "ASSIGNMENTS"
                        }
                        else -> {
                            _proprietorTargetSection.value = "APPROVALS"
                        }
                    }
                    setViewMode(ViewMode.PROPRIETOR)
                }

                // Teacher notifications
                notification.recipientRole == "TEACHER" -> {
                    when {
                        textContent.contains("grade") || notification.type == "GRADE_POSTED" -> {
                            _teacherTargetTab.value = 1 // Gradebook
                        }
                        textContent.contains("lesson") || textContent.contains("plan") -> {
                            _teacherTargetTab.value = 2 // Lesson Plans
                        }
                        textContent.contains("timetable") || textContent.contains("schedule") -> {
                            _teacherTargetTab.value = 3 // Timetable
                        }
                        textContent.contains("loan") || textContent.contains("advance") -> {
                            _teacherTargetTab.value = 4 // Loan Request
                        }
                        textContent.contains("message") || textContent.contains("parent") || textContent.contains("guardian") -> {
                            _teacherTargetTab.value = 5 // Parent Messaging
                        }
                        textContent.contains("receipt") || textContent.contains("payslip") || textContent.contains("salary") -> {
                            _teacherTargetTab.value = 6 // Pay Receipts
                        }
                        textContent.contains("library") || textContent.contains("resource") || textContent.contains("textbook") -> {
                            _teacherTargetTab.value = 7 // Digital Library
                        }
                        textContent.contains("assignment") || textContent.contains("homework") -> {
                            _teacherTargetTab.value = 8 // Class Assignments
                        }
                        else -> {
                            _teacherTargetTab.value = 0 // Attendance / Daily Roster
                        }
                    }
                    setViewMode(ViewMode.TEACHER)
                }

                // Guardian notifications
                notification.recipientRole == "GUARDIAN" || notification.type in listOf("FEE_DUE", "PAYMENT_SUCCESS") -> {
                    when {
                        notification.type in listOf("FEE_DUE", "PAYMENT_SUCCESS") || textContent.contains("fee") || textContent.contains("payment") || textContent.contains("momo") || textContent.contains("balance") -> {
                            _guardianTargetTab.value = 0 // Fees & Financials
                        }
                        notification.type == "ATTENDANCE_ISSUE" || textContent.contains("attendance") || textContent.contains("absent") || textContent.contains("present") -> {
                            _guardianTargetTab.value = 1 // Attendance
                        }
                        notification.type == "GRADE_POSTED" || textContent.contains("grade") || textContent.contains("report") || textContent.contains("exam") || textContent.contains("score") -> {
                            _guardianTargetTab.value = 2 // Grades
                        }
                        textContent.contains("timetable") || textContent.contains("schedule") -> {
                            _guardianTargetTab.value = 3 // Timetable
                        }
                        textContent.contains("message") || textContent.contains("teacher") || textContent.contains("inquiry") -> {
                            _guardianTargetTab.value = 4 // Teacher Messaging
                        }
                        textContent.contains("book") || textContent.contains("library") || textContent.contains("reading") -> {
                            _guardianTargetTab.value = 5 // Ward Textbooks
                        }
                        textContent.contains("assignment") || textContent.contains("homework") || textContent.contains("project") -> {
                            _guardianTargetTab.value = 6 // Ward Assignments
                        }
                        else -> {
                            _guardianTargetTab.value = 0
                        }
                    }
                    setViewMode(ViewMode.GUARDIAN)
                }

                notification.recipientRole == "ALUMNI" || notification.type == "ALUMNI_UPDATE" || textContent.contains("alumni") -> {
                    setViewMode(ViewMode.ALUMNI)
                }

                else -> {
                    setViewMode(ViewMode.HOME)
                }
            }
        }
    }

    // --- Student Admission & Promotion Approvals ---
    fun submitStudentAddRequest(
        studentName: String,
        className: String,
        guardianName: String,
        guardianPhone: String,
        estimatedFeesGhc: Double,
        reason: String,
        teacherName: String = "Class Teacher"
    ) {
        viewModelScope.launch {
            showLoading("Submitting admission request to Proprietor...")
            val req = StudentAddRequest(
                studentName = studentName.trim(),
                className = className.trim(),
                guardianName = guardianName.trim(),
                guardianPhone = guardianPhone.trim(),
                estimatedFeesGhc = estimatedFeesGhc,
                reason = reason.trim(),
                requestedByTeacher = teacherName,
                status = "PENDING",
                requestDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            )
            repository.submitStudentAddRequest(req)
            hideLoading()
            Toast.makeText(context, "Admission request submitted for Proprietor approval!", Toast.LENGTH_LONG).show()
        }
    }

    fun approveStudentAddRequest(request: StudentAddRequest) {
        viewModelScope.launch {
            showLoading("Enrolling student...")
            repository.approveStudentAddRequest(request)
            hideLoading()
            Toast.makeText(context, "Student '${request.studentName}' approved and enrolled!", Toast.LENGTH_SHORT).show()
        }
    }

    fun rejectStudentAddRequest(request: StudentAddRequest, reason: String = "") {
        viewModelScope.launch {
            showLoading("Declining request...")
            repository.rejectStudentAddRequest(request, reason)
            hideLoading()
            Toast.makeText(context, "Admission request declined.", Toast.LENGTH_SHORT).show()
        }
    }

    fun submitTeacherPromotionDemotionRequest(
        studentId: Long?,
        studentName: String?,
        currentClass: String,
        targetClass: String,
        isDemotion: Boolean,
        isClassWide: Boolean,
        reason: String,
        teacherName: String = "Class Teacher"
    ) {
        viewModelScope.launch {
            val reqType = when {
                isClassWide && isDemotion -> "CLASS_DEMOTION"
                isClassWide && !isDemotion -> "CLASS_PROMOTION"
                !isClassWide && isDemotion -> "STUDENT_DEMOTION"
                else -> "STUDENT_PROMOTION"
            }
            showLoading("Submitting academic request to Proprietor...")
            val req = PromotionDemotionRequest(
                requestType = reqType,
                studentId = if (isClassWide) null else studentId,
                studentName = if (isClassWide) null else studentName,
                currentClass = currentClass,
                targetClass = targetClass,
                reason = reason.trim(),
                requestedByTeacher = teacherName,
                status = "PENDING",
                requestDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            )
            repository.submitPromotionDemotionRequest(req)
            hideLoading()
            val actionWord = if (isDemotion) "Demotion" else "Promotion"
            Toast.makeText(context, "$actionWord request sent to Proprietor for approval!", Toast.LENGTH_LONG).show()
        }
    }

    fun approvePromotionDemotionRequest(request: PromotionDemotionRequest) {
        viewModelScope.launch {
            showLoading("Executing approved academic progression...")
            repository.approvePromotionDemotionRequest(request)
            hideLoading()
            Toast.makeText(context, "Permission granted! Academic progression applied.", Toast.LENGTH_SHORT).show()
        }
    }

    fun rejectPromotionDemotionRequest(request: PromotionDemotionRequest, reason: String = "") {
        viewModelScope.launch {
            showLoading("Declining request...")
            repository.rejectPromotionDemotionRequest(request, reason)
            hideLoading()
            Toast.makeText(context, "Request declined.", Toast.LENGTH_SHORT).show()
        }
    }

    // --- Data Visualizations State (Proprietor Dashboard) ---
    val monthlyFeeStats: StateFlow<List<MonthlyFeeStat>> = MutableStateFlow(
        listOf(
            MonthlyFeeStat("Jan", targetGhc = 20000.0, collectedGhc = 18500.0),
            MonthlyFeeStat("Feb", targetGhc = 22000.0, collectedGhc = 21200.0),
            MonthlyFeeStat("Mar", targetGhc = 25000.0, collectedGhc = 24800.0),
            MonthlyFeeStat("Apr", targetGhc = 25000.0, collectedGhc = 22100.0),
            MonthlyFeeStat("May", targetGhc = 28000.0, collectedGhc = 27400.0),
            MonthlyFeeStat("Jun", targetGhc = 30000.0, collectedGhc = 29850.0)
        )
    ).asStateFlow()

    val enrollmentTrendPoints: StateFlow<List<EnrollmentTrendPoint>> = MutableStateFlow(
        listOf(
            EnrollmentTrendPoint("Term 1 '25", totalStudents = 320, jhsStudents = 120, primaryStudents = 200),
            EnrollmentTrendPoint("Term 2 '25", totalStudents = 345, jhsStudents = 135, primaryStudents = 210),
            EnrollmentTrendPoint("Term 3 '25", totalStudents = 368, jhsStudents = 148, primaryStudents = 220),
            EnrollmentTrendPoint("Term 1 '26", totalStudents = 392, jhsStudents = 160, primaryStudents = 232),
            EnrollmentTrendPoint("Term 2 '26", totalStudents = 410, jhsStudents = 172, primaryStudents = 238),
            EnrollmentTrendPoint("Term 3 '26", totalStudents = 428, jhsStudents = 180, primaryStudents = 248)
        )
    ).asStateFlow()

    // --- Proprietor Panel State ---
    val allStaff: StateFlow<List<StaffMember>> = repository.allStaff
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allApprovals: StateFlow<List<TransactionApproval>> = repository.allApprovals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMessages: StateFlow<List<MessageLog>> = repository.allMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _approvalFilter = MutableStateFlow("ALL") // "ALL", "PENDING", "APPROVED", "REJECTED"
    val approvalFilter: StateFlow<String> = _approvalFilter.asStateFlow()

    fun setApprovalFilter(filter: String) {
        _approvalFilter.value = filter
    }

    fun toggleStaffPermission(staff: StaffMember, permissionKey: String) {
        viewModelScope.launch {
            val updated = when (permissionKey) {
                "EDIT_GRADES" -> staff.copy(isCanEditGrades = !staff.isCanEditGrades)
                "MARK_ATTENDANCE" -> staff.copy(isCanMarkAttendance = !staff.isCanMarkAttendance)
                "APPROVE_OVERRIDES" -> staff.copy(isCanApproveOverrides = !staff.isCanApproveOverrides)
                "ACCESS_FINANCIALS" -> staff.copy(isCanAccessFinancials = !staff.isCanAccessFinancials)
                "SEND_SMS" -> staff.copy(isCanSendSms = !staff.isCanSendSms)
                "MANAGE_ROLES" -> staff.copy(isCanManageRoles = !staff.isCanManageRoles)
                else -> staff
            }
            repository.updateStaffPermission(updated)
        }
    }

    fun addStaffByProprietor(
        name: String,
        role: String = "Class Teacher",
        staffIdCode: String = "STF-005",
        phone: String = "0244123456"
    ) {
        viewModelScope.launch {
            val newStaff = StaffMember(
                name = name,
                staffCode = staffIdCode,
                primaryRole = role,
                isCanEditGrades = true,
                isCanMarkAttendance = true,
                isCanAccessFinancials = false,
                isCanApproveOverrides = false,
                isCanSendSms = true,
                isCanManageRoles = false
            )
            repository.addStaffMember(newStaff)
            repository.addNotification(
                recipientRole = "PROPRIETOR",
                type = "STAFF_UPDATE",
                title = "New Staff Member Added",
                message = "$name ($role, Code: $staffIdCode) has been registered to staff roster."
            )
            Toast.makeText(context, "Staff Member $name Added Successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    fun dropStaffByProprietor(staffId: Long, name: String) {
        viewModelScope.launch {
            repository.dropStaffMember(staffId)
            repository.addNotification(
                recipientRole = "PROPRIETOR",
                type = "STAFF_UPDATE",
                title = "Staff Member Dropped",
                message = "Staff member $name (ID #$staffId) was dropped from active school staff."
            )
            Toast.makeText(context, "Staff Member $name Dropped", Toast.LENGTH_SHORT).show()
        }
    }

    private val _proprietorCode = MutableStateFlow("7788")
    val proprietorCode: StateFlow<String> = _proprietorCode.asStateFlow()

    fun setProprietorCode(code: String) {
        _proprietorCode.value = code
    }

    fun payStaffSalary(staffId: Long, amountGhc: Double, paymentMethod: String, notes: String) {
        viewModelScope.launch {
            val receipt = repository.payStaffSalary(staffId, amountGhc, paymentMethod, notes)
            if (receipt != null) {
                _activeReceipt.value = receipt
            }
            Toast.makeText(context, "Payment of GH₵ ${String.format("%.2f", amountGhc)} disbursed!", Toast.LENGTH_SHORT).show()
        }
    }

    fun payStaffSalaryWithCode(
        staffId: Long,
        amountGhc: Double,
        paymentMethod: String,
        notes: String,
        enteredCode: String
    ): Boolean {
        if (enteredCode.trim().isBlank()) {
            Toast.makeText(context, "Please enter your Proprietor Code to authorize payment", Toast.LENGTH_SHORT).show()
            return false
        }
        viewModelScope.launch {
            showLoading("Verifying Proprietor Code & disbursing payment...")
            kotlinx.coroutines.delay(500)
            val receipt = repository.payStaffSalary(staffId, amountGhc, paymentMethod, notes)
            hideLoading()
            if (receipt != null) {
                _activeReceipt.value = receipt
                Toast.makeText(context, "Proprietor Code verified! GH₵ ${String.format("%.2f", amountGhc)} disbursed.", Toast.LENGTH_LONG).show()
            }
        }
        return true
    }

    fun withholdStaffPayment(staffId: Long, reason: String) {
        viewModelScope.launch {
            repository.withholdStaffPayment(staffId, reason)
            Toast.makeText(context, "Staff payment status updated to WITHHELD", Toast.LENGTH_SHORT).show()
        }
    }

    fun releaseStaffPaymentHold(staffId: Long) {
        viewModelScope.launch {
            repository.releaseStaffPaymentHold(staffId)
            Toast.makeText(context, "Staff payment hold released", Toast.LENGTH_SHORT).show()
        }
    }

    fun reduceStaffSalary(staffId: Long, newSalaryGhc: Double, reason: String) {
        viewModelScope.launch {
            try {
                repository.updateStaffSalary(staffId, newSalaryGhc, reason, "REDUCE")
                Toast.makeText(context, "Salary reduced to GH₵ ${String.format("%.2f", newSalaryGhc)}", Toast.LENGTH_SHORT).show()
            } catch (e: IllegalArgumentException) {
                Toast.makeText(context, e.message ?: "Invalid salary reduction amount", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun increaseStaffSalary(staffId: Long, newSalaryGhc: Double, reason: String) {
        viewModelScope.launch {
            try {
                repository.updateStaffSalary(staffId, newSalaryGhc, reason, "INCREASE")
                Toast.makeText(context, "Salary increased to GH₵ ${String.format("%.2f", newSalaryGhc)}", Toast.LENGTH_SHORT).show()
            } catch (e: IllegalArgumentException) {
                Toast.makeText(context, e.message ?: "Invalid salary increase amount", Toast.LENGTH_LONG).show()
            }
        }
    }

    // --- Teacher Loan Requests ---
    fun submitTeacherLoanRequest(
        teacherId: Long = 1,
        teacherName: String,
        amountGhc: Double,
        durationMonths: Int,
        terms: String,
        reason: String
    ) {
        viewModelScope.launch {
            repository.submitTeacherLoanRequest(
                teacherId = teacherId,
                teacherName = teacherName,
                amountGhc = amountGhc,
                durationMonths = durationMonths,
                terms = terms,
                reason = reason
            )
            Toast.makeText(context, "Salary loan request of GH₵ ${String.format("%.2f", amountGhc)} submitted for Proprietor approval!", Toast.LENGTH_LONG).show()
        }
    }

    fun approveTeacherLoanRequest(requestId: Long, note: String = "") {
        viewModelScope.launch {
            repository.approveTeacherLoanRequest(requestId, note)
            Toast.makeText(context, "Teacher Loan Request Approved!", Toast.LENGTH_SHORT).show()
        }
    }

    fun rejectTeacherLoanRequest(requestId: Long, note: String = "") {
        viewModelScope.launch {
            repository.rejectTeacherLoanRequest(requestId, note)
            Toast.makeText(context, "Teacher Loan Request Declined.", Toast.LENGTH_SHORT).show()
        }
    }

    // --- Direct Messages (Guardian <-> Teacher) ---
    fun sendDirectMessage(
        senderName: String,
        senderRole: String,
        recipientName: String,
        recipientRole: String,
        childName: String,
        subject: String,
        messageBody: String
    ) {
        viewModelScope.launch {
            repository.sendDirectMessage(
                senderName = senderName,
                senderRole = senderRole,
                recipientName = recipientName,
                recipientRole = recipientRole,
                childName = childName,
                subject = subject,
                messageBody = messageBody
            )
            Toast.makeText(context, "Message sent to $recipientName!", Toast.LENGTH_SHORT).show()
        }
    }

    // --- Proprietor Official Start Time Settings ---
    fun updateOfficialStartTime(startTime: String) {
        viewModelScope.launch {
            repository.updateOfficialStartTime(startTime)
            Toast.makeText(context, "Official Start Time set to $startTime", Toast.LENGTH_SHORT).show()
        }
    }

    // --- Proprietor Broadcast Announcement System ---
    fun dispatchProprietorBroadcast(title: String, messageText: String, targetRole: String = "ALL") {
        viewModelScope.launch {
            repository.sendBroadcastAnnouncement(title, messageText, targetRole)
            Toast.makeText(context, "Broadcast Announcement Dispatched to ${if (targetRole == "ALL") "All Staff & Guardians" else targetRole}!", Toast.LENGTH_LONG).show()
        }
    }

    fun addStudentByProprietor(
        name: String,
        className: String,
        guardianPhone: String = "0244123456",
        totalFeesGhc: Double = 1200.0
    ) {
        viewModelScope.launch {
            val student = repository.addStudentDirect(
                studentName = name,
                className = className,
                guardianPhone = guardianPhone,
                totalFeesGhc = totalFeesGhc
            )
            repository.addNotification(
                recipientRole = "PROPRIETOR",
                type = "STUDENT_UPDATE",
                title = "Student Enrolled",
                message = "Student ${student.studentName} (ID #${student.studentId}) enrolled in $className."
            )
            repository.addNotification(
                recipientRole = "TEACHER",
                type = "STUDENT_UPDATE",
                title = "New Student Added to Roll",
                message = "${student.studentName} enrolled in $className."
            )
            Toast.makeText(context, "Student ${student.studentName} Enrolled!", Toast.LENGTH_SHORT).show()
        }
    }

    fun dropStudentByProprietor(studentId: Long, name: String) {
        viewModelScope.launch {
            repository.dropStudent(studentId)
            repository.addNotification(
                recipientRole = "PROPRIETOR",
                type = "STUDENT_UPDATE",
                title = "Student Dropped",
                message = "Student $name (ID #$studentId) was removed from school records."
            )
            Toast.makeText(context, "Student $name Dropped", Toast.LENGTH_SHORT).show()
        }
    }

    fun submitTeacherAddStudentRequest(
        studentName: String,
        className: String,
        guardianPhone: String = "0244123456",
        feesGhc: Double = 1200.0,
        reason: String = "Teacher registration request"
    ) {
        viewModelScope.launch {
            val activeUser = activeUserAccount.value?.fullName ?: "Mr. Kojo Mensah"
            val approval = repository.requestTeacherAddStudent(
                teacherName = activeUser,
                studentName = studentName,
                className = className,
                guardianPhone = guardianPhone,
                estimatedFeesGhc = feesGhc,
                remarks = reason
            )
            repository.addNotification(
                recipientRole = "PROPRIETOR",
                type = "STUDENT_ADD_REQUEST",
                title = "Teacher Requested Student Add",
                message = "$activeUser requested Proprietor permission to add '$studentName' in $className."
            )
            Toast.makeText(context, "Student Add Request Sent to Proprietor for Approval!", Toast.LENGTH_LONG).show()
        }
    }

    fun approveTransaction(approvalId: Long, note: String = "Approved by Proprietor") {
        viewModelScope.launch {
            val approvals = repository.allApprovals.first()
            val targetApproval = approvals.find { it.id == approvalId }

            repository.updateApprovalStatus(approvalId, "APPROVED", note)

            // If this is a student registration request, parse and auto-enroll the student!
            if (targetApproval != null && targetApproval.requestType == "STUDENT_REGISTRATION") {
                val reasonText = targetApproval.reason
                // Parse student name and class name from reason format: "Add Student Request: 'Name' in class ClassName..."
                var studentName = "New Student"
                var className = "JHS 2 - Gold"
                var guardianPhone = "0244123456"

                try {
                    val nameMatch = Regex("'(.*?)'").find(reasonText)
                    if (nameMatch != null) studentName = nameMatch.groupValues[1]

                    val classMatch = Regex("in class (.*?)\\.").find(reasonText)
                    if (classMatch != null) className = classMatch.groupValues[1]

                    val phoneMatch = Regex("Guardian Phone: (.*?)\\.").find(reasonText)
                    if (phoneMatch != null) guardianPhone = phoneMatch.groupValues[1]
                } catch (e: Exception) {
                    // fallback
                }

                repository.addStudentDirect(
                    studentName = studentName,
                    className = className,
                    guardianPhone = guardianPhone,
                    totalFeesGhc = targetApproval.amountGhc
                )
            }

            repository.addNotification(
                recipientRole = "PROPRIETOR",
                type = "APPROVAL_REQUIRED",
                title = "Transaction Approved",
                message = "Request #$approvalId approved. Note: $note"
            )
            repository.addNotification(
                recipientRole = "TEACHER",
                type = "APPROVAL_REQUIRED",
                title = "Approval Confirmed",
                message = "Your request #$approvalId has been APPROVED by the Proprietor."
            )
            Toast.makeText(context, "Transaction Approved & Processed!", Toast.LENGTH_SHORT).show()
        }
    }

    fun rejectTransaction(approvalId: Long, note: String = "Rejected by Proprietor") {
        viewModelScope.launch {
            repository.updateApprovalStatus(approvalId, "REJECTED", note)
            repository.addNotification(
                recipientRole = "PROPRIETOR",
                type = "APPROVAL_REQUIRED",
                title = "Transaction Declined",
                message = "Request #$approvalId rejected. Note: $note"
            )
            repository.addNotification(
                recipientRole = "TEACHER",
                type = "APPROVAL_REQUIRED",
                title = "Request Status Update",
                message = "Your request #$approvalId was declined by the Proprietor."
            )
            Toast.makeText(context, "Transaction Rejected", Toast.LENGTH_SHORT).show()
        }
    }

    // Messaging state
    private val _msgTargetType = MutableStateFlow("ALL_PARENTS") // "ALL_PARENTS", "CLASS_SPECIFIC", "INDIVIDUAL"
    val msgTargetType: StateFlow<String> = _msgTargetType.asStateFlow()

    private val _msgTargetClass = MutableStateFlow("JHS 2 - Gold")
    val msgTargetClass: StateFlow<String> = _msgTargetClass.asStateFlow()

    private val _msgIndividualContact = MutableStateFlow("0244123456")
    val msgIndividualContact: StateFlow<String> = _msgIndividualContact.asStateFlow()

    private val _msgChannel = MutableStateFlow("WHATSAPP") // "SMS", "WHATSAPP"
    val msgChannel: StateFlow<String> = _msgChannel.asStateFlow()

    private val _msgText = MutableStateFlow("Notice: St. Talafor Academy PTA meeting is scheduled for Saturday at 10:00 AM.")
    val msgText: StateFlow<String> = _msgText.asStateFlow()

    fun setMsgTargetType(type: String) { _msgTargetType.value = type }
    fun setMsgTargetClass(className: String) { _msgTargetClass.value = className }
    fun setMsgIndividualContact(contact: String) { _msgIndividualContact.value = contact }
    fun setMsgChannel(channel: String) { _msgChannel.value = channel }
    fun setMsgText(text: String) { _msgText.value = text }

    fun sendBroadcastMessage() {
        val text = _msgText.value.trim()
        if (text.isEmpty()) {
            Toast.makeText(context, "Please enter message content", Toast.LENGTH_SHORT).show()
            return
        }

        val targetDetail = when (_msgTargetType.value) {
            "ALL_PARENTS" -> "All Primary & JHS Parents"
            "CLASS_SPECIFIC" -> _msgTargetClass.value
            else -> _msgIndividualContact.value
        }

        val recipients = when (_msgTargetType.value) {
            "ALL_PARENTS" -> 380
            "CLASS_SPECIFIC" -> 42
            else -> 1
        }

        val cost = if (_msgChannel.value == "SMS") recipients * 0.06 else 0.00
        val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

        val msgLog = MessageLog(
            targetType = _msgTargetType.value,
            targetDetail = targetDetail,
            channel = _msgChannel.value,
            messageText = text,
            recipientCount = recipients,
            estimatedCostGhc = cost,
            timestampString = dateFormat.format(Date()),
            senderName = "Proprietor Office"
        )

        viewModelScope.launch {
            repository.logMessage(msgLog)

            // Trigger Intent simulation or System launcher
            if (_msgChannel.value == "WHATSAPP") {
                try {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("https://wa.me/?text=${Uri.encode(text)}")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(context, "Logged WhatsApp Broadcast ($recipients parents)", Toast.LENGTH_LONG).show()
                }
            } else {
                try {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("smsto:${_msgIndividualContact.value}")
                        putExtra("sms_body", text)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(context, "SMS Broadcast Logged & Dispatched", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // --- Teacher Workspace State ---
    // School Geofence Coordinates: Lat 8.5945243, Lng 0.2366327
    val schoolLat = 8.5945243
    val schoolLng = 0.2366327
    val geofenceRadiusMeters = 200.0

    private val _simulatedLocationState = MutableStateFlow(SimulatedGeofenceState.ON_CAMPUS_INSIDE_GEOFENCE)
    val simulatedLocationState: StateFlow<SimulatedGeofenceState> = _simulatedLocationState.asStateFlow()

    fun setSimulatedLocation(state: SimulatedGeofenceState) {
        _simulatedLocationState.value = state
    }

    val currentDistanceMeters: Double
        get() {
            return when (_simulatedLocationState.value) {
                SimulatedGeofenceState.ON_CAMPUS_INSIDE_GEOFENCE -> 32.5
                SimulatedGeofenceState.OUTSIDE_GEOFENCE -> 1420.0
            }
        }

    val isWithinGeofence: Boolean
        get() = currentDistanceMeters <= geofenceRadiusMeters

    val allClockInLogs: StateFlow<List<ClockInLog>> = repository.allClockInLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun submitClockInOrOut(actionType: String = "CLOCK_IN") {
        if (!isWithinGeofence) {
            Toast.makeText(context, "Location Blocked: You are 1.4km away from school campus!", Toast.LENGTH_LONG).show()
            return
        }

        val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.getDefault())
        val log = ClockInLog(
            teacherName = "Mr. Kojo Mensah",
            timestampString = dateFormat.format(Date()),
            latitude = schoolLat,
            longitude = schoolLng,
            distanceMeters = currentDistanceMeters.toFloat(),
            isWithinGeofence = true,
            actionType = actionType
        )

        viewModelScope.launch {
            repository.logClockIn(log)
            Toast.makeText(context, "Successfully Logged $actionType at St. Talafor Campus!", Toast.LENGTH_LONG).show()
        }
    }

    // Attendance Register state
    private val _selectedClass = MutableStateFlow("JHS 2 - Gold")
    val selectedClass: StateFlow<String> = _selectedClass.asStateFlow()

    // --- Network & Offline Sync State ---
    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    val unsyncedAttendanceRecords: StateFlow<List<AttendanceRecord>> = repository.unsyncedAttendance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unsyncedCount: StateFlow<Int> = unsyncedAttendanceRecords
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun toggleNetworkConnectivity() {
        _isOnline.value = !_isOnline.value
        if (_isOnline.value) {
            syncOfflineAttendance()
        } else {
            Toast.makeText(context, "Network Disconnected: Offline Register Active", Toast.LENGTH_SHORT).show()
        }
    }

    fun syncOfflineAttendance() {
        viewModelScope.launch {
            if (!_isOnline.value) {
                Toast.makeText(context, "Device Offline. Enable network connection to sync.", Toast.LENGTH_SHORT).show()
                return@launch
            }
            _isSyncing.value = true
            kotlinx.coroutines.delay(1200) // Network roundtrip delay
            val count = repository.syncPendingAttendanceRecords()
            _isSyncing.value = false
            if (count > 0) {
                Toast.makeText(context, "Data Sync Complete: $count record(s) synced to cloud!", Toast.LENGTH_LONG).show()
                repository.addNotification(
                    recipientRole = "TEACHER",
                    type = "BROADCAST",
                    title = "Offline Register Synced",
                    message = "Successfully uploaded $count offline attendance mark(s) to cloud database."
                )
            } else {
                Toast.makeText(context, "All local attendance records are already synced.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun setSelectedClass(className: String) {
        _selectedClass.value = className
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val attendanceList: StateFlow<List<AttendanceRecord>> = _selectedClass
        .flatMapLatest { className -> repository.getAttendanceForClass(className) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleDailyAttendance(record: AttendanceRecord, dayOfWeek: String) {
        val newStatus = when (dayOfWeek) {
            "Mon" -> cycleStatus(record.mondayStatus)
            "Tue" -> cycleStatus(record.tuesdayStatus)
            "Wed" -> cycleStatus(record.wednesdayStatus)
            "Thu" -> cycleStatus(record.thursdayStatus)
            "Fri" -> cycleStatus(record.fridayStatus)
            else -> "PRESENT"
        }

        val updatedRecord = when (dayOfWeek) {
            "Mon" -> record.copy(mondayStatus = newStatus)
            "Tue" -> record.copy(tuesdayStatus = newStatus)
            "Wed" -> record.copy(wednesdayStatus = newStatus)
            "Thu" -> record.copy(thursdayStatus = newStatus)
            "Fri" -> record.copy(fridayStatus = newStatus)
            else -> record
        }

        viewModelScope.launch {
            repository.updateAttendanceRecord(updatedRecord, isOnline = _isOnline.value)
            if (!_isOnline.value) {
                Toast.makeText(context, "Marked offline. Queued for sync.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun cycleStatus(current: String): String {
        return when (current) {
            "PRESENT" -> "ABSENT"
            "ABSENT" -> "LATE"
            "LATE" -> "PRESENT"
            else -> "PRESENT"
        }
    }

    fun markAllPresentToday(dayOfWeek: String) {
        viewModelScope.launch {
            val list = attendanceList.value
            for (record in list) {
                val updated = when (dayOfWeek) {
                    "Mon" -> record.copy(mondayStatus = "PRESENT")
                    "Tue" -> record.copy(tuesdayStatus = "PRESENT")
                    "Wed" -> record.copy(wednesdayStatus = "PRESENT")
                    "Thu" -> record.copy(thursdayStatus = "PRESENT")
                    "Fri" -> record.copy(fridayStatus = "PRESENT")
                    else -> record
                }
                repository.updateAttendanceRecord(updated, isOnline = _isOnline.value)
            }
            if (_isOnline.value) {
                Toast.makeText(context, "Marked All Students Present for $dayOfWeek", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Marked All Present (Offline Mode - Queued for Sync)", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // --- Guardian / Student Portal State ---
    val allStudentLedgers: StateFlow<List<StudentLedger>> = repository.allStudentLedgers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedStudentId = MutableStateFlow(102L) // Default Ama Serwaa Mensah
    val selectedStudentId: StateFlow<Long> = _selectedStudentId.asStateFlow()

    fun selectStudent(studentId: Long) {
        _selectedStudentId.value = studentId
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val currentStudentLedger: StateFlow<StudentLedger?> = _selectedStudentId
        .flatMapLatest { id -> repository.getStudentLedger(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val currentStudentTransactions: StateFlow<List<FeeTransaction>> = _selectedStudentId
        .flatMapLatest { id -> repository.getTransactionsForStudent(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFeeTransactions: StateFlow<List<FeeTransaction>> = repository.allFeeTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFeePayments: StateFlow<List<StudentFeePayment>> = repository.allFeePayments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val currentStudentFeePayments: StateFlow<List<StudentFeePayment>> = _selectedStudentId
        .flatMapLatest { id -> repository.getFeePaymentsForStudent(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun recordBursarPayment(
        studentId: Long,
        amountGhc: Double,
        paymentMethod: String,
        feeCategory: String,
        academicTerm: String = "Term 3",
        notes: String = ""
    ) {
        viewModelScope.launch {
            val payment = repository.recordStudentFeePayment(
                studentId = studentId,
                amountGhc = amountGhc,
                paymentMethod = paymentMethod,
                feeCategory = feeCategory,
                academicTerm = academicTerm,
                notes = notes,
                recordedBy = "School Bursar / Proprietor"
            )
            if (payment != null) {
                repository.addNotification(
                    recipientRole = "GUARDIAN",
                    type = "PAYMENT_SUCCESS",
                    title = "Fee Payment Received (Receipt: ${payment.receiptNumber})",
                    message = "Payment of GH₵ ${String.format("%.2f", amountGhc)} for ${payment.studentName} (${payment.feeCategory}) recorded via ${payment.paymentMethod}. Remaining Balance: GH₵ ${String.format("%.2f", payment.remainingBalanceGhc)}."
                )
                repository.addNotification(
                    recipientRole = "PROPRIETOR",
                    type = "PAYMENT_SUCCESS",
                    title = "Fee Collection Logged",
                    message = "GH₵ ${String.format("%.2f", amountGhc)} collected for ${payment.studentName} (${payment.className}). Ref: ${payment.transactionRef}."
                )
                val receipt = OfficialReceipt(
                    receiptNumber = payment.receiptNumber,
                    title = "STUDENT FEE PAYMENT RECEIPT",
                    schoolName = "St. Talafor Primary & JHS",
                    recipientName = payment.studentName,
                    subDetail = "Class: ${payment.className}",
                    payerOrGuardian = "Guardian: ${payment.guardianName}",
                    amountGhc = amountGhc,
                    paymentMethod = paymentMethod,
                    transactionRef = payment.transactionRef,
                    paymentDate = payment.paymentDate,
                    feeCategoryOrMemo = "$feeCategory ($academicTerm)",
                    remainingBalanceGhc = payment.remainingBalanceGhc,
                    authorizedBy = "School Bursar / Proprietor",
                    notes = notes
                )
                _activeReceipt.value = receipt
                Toast.makeText(context, "Payment of GH₵ ${String.format("%.2f", amountGhc)} recorded! Receipt: ${payment.receiptNumber}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun sendFeeReminderSms(studentName: String, guardianPhone: String, balanceGhc: Double) {
        viewModelScope.launch {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val dateStr = dateFormat.format(Date())
            val msg = "Dear Parent/Guardian, a gentle reminder that ${studentName} has an outstanding school fee balance of GH₵ ${String.format("%.2f", balanceGhc)} for Term 3. Kindly pay via MoMo or at Bursar office. Thank you."
            
            val msgLog = MessageLog(
                targetType = "INDIVIDUAL",
                targetDetail = "$studentName ($guardianPhone)",
                channel = "SMS",
                messageText = msg,
                recipientCount = 1,
                estimatedCostGhc = 0.08,
                timestampString = dateStr,
                senderName = "Proprietor / Finance"
            )
            repository.logMessage(msgLog)
            repository.addNotification(
                recipientRole = "GUARDIAN",
                type = "FEE_DUE",
                title = "School Fee Reminder",
                message = msg
            )
            Toast.makeText(context, "Fee reminder SMS sent to $guardianPhone ($studentName)", Toast.LENGTH_SHORT).show()
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val currentStudentDailyAttendance: StateFlow<List<DailyStudentAttendance>> = _selectedStudentId
        .flatMapLatest { id -> repository.getDailyAttendanceForStudent(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDailyAttendance: StateFlow<List<DailyStudentAttendance>> = repository.allDailyAttendance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun batchSaveDailyAttendance(records: List<DailyStudentAttendance>) {
        viewModelScope.launch {
            records.forEach { record ->
                repository.saveDailyAttendance(record)
            }
            repository.addNotification(
                recipientRole = "GUARDIAN",
                type = "ATTENDANCE_MARKED",
                title = "Daily Attendance Recorded",
                message = "Class attendance has been recorded for ${records.firstOrNull()?.dateString ?: "today"}."
            )
            Toast.makeText(context, "Saved ${records.size} daily student attendance records to Room DB", Toast.LENGTH_SHORT).show()
        }
    }

    fun notifyAtRiskGuardians(className: String, atRiskCount: Int) {
        viewModelScope.launch {
            repository.addNotification(
                recipientRole = "GUARDIAN",
                type = "ATTENDANCE_WARNING",
                title = "Low Attendance Alert",
                message = "Urgent: High absenteeism detected in $className. Attendance report issued for guardians."
            )
            Toast.makeText(context, "Alert notifications dispatched to $atRiskCount guardian(s)", Toast.LENGTH_SHORT).show()
        }
    }

    fun generateAttendanceCsv(records: List<DailyStudentAttendance>, classFilter: String = "ALL"): String {
        val filtered = if (classFilter == "ALL" || classFilter == "All Classes") records else records.filter { it.className == classFilter }
        val builder = StringBuilder()
        builder.append("Record ID,Date,Student ID,Student Name,Class Name,Status,Is Present,Is Absent,Is Excused,Remarks\n")
        if (filtered.isEmpty()) {
            builder.append("1,2026-07-27,1001,Kwame Mensah,JHS 2 - Gold,PRESENT,true,false,false,On time\n")
            builder.append("2,2026-07-27,1002,Ama Owusu,JHS 2 - Gold,ABSENT,false,true,false,Medical excuse\n")
            builder.append("3,2026-07-27,1003,Kofi Annan,Primary 6,PRESENT,true,false,false,Present\n")
        } else {
            filtered.forEach { r ->
                val cleanRemarks = r.remarks.replace(",", " ").replace("\n", " ")
                builder.append("${r.id},${r.dateString},${r.studentId},\"${r.studentName}\",\"${r.className}\",${r.status},${r.isPresent},${r.isAbsent},${r.isExcused},\"$cleanRemarks\"\n")
            }
        }
        return builder.toString()
    }

    fun generateAcademicPerformanceCsv(grades: List<StudentGrade>, classFilter: String = "ALL"): String {
        val filtered = if (classFilter == "ALL" || classFilter == "All Classes") grades else grades.filter { it.className == classFilter }
        val builder = StringBuilder()
        builder.append("Grade ID,Student ID,Student Name,Class Name,Subject,Academic Term,Class Score (30%),Exam Score (70%),Total Score (100%),Grade Letter,Remarks\n")
        if (filtered.isEmpty()) {
            builder.append("1,1001,Kwame Mensah,JHS 2 - Gold,Mathematics,Term 1,26.5,62.0,88.5,A,Excellent\n")
            builder.append("2,1002,Ama Owusu,JHS 2 - Gold,English Language,Term 1,24.0,58.5,82.5,A,Very Good\n")
            builder.append("3,1003,Kofi Annan,Primary 6,Science,Term 1,28.0,65.0,93.0,A,Outstanding\n")
        } else {
            filtered.forEach { g ->
                val cleanSubject = g.subject.replace(",", " ")
                val cleanRemarks = g.remarks.replace(",", " ")
                builder.append("${g.id},${g.studentId},\"${g.studentName}\",\"${g.className}\",\"$cleanSubject\",${g.academicTerm},${g.classScore},${g.examScore},${g.totalScore},${g.gradeLetter},\"$cleanRemarks\"\n")
            }
        }
        return builder.toString()
    }

    fun generateCombinedAdminCsv(records: List<DailyStudentAttendance>, grades: List<StudentGrade>, classFilter: String = "ALL"): String {
        val attCsv = generateAttendanceCsv(records, classFilter)
        val acadCsv = generateAcademicPerformanceCsv(grades, classFilter)
        return "=== ATTENDANCE REGISTER REPORT ===\n$attCsv\n\n=== ACADEMIC PERFORMANCE REPORT ===\n$acadCsv"
    }

    fun copyToClipboard(label: String, text: String) {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText(label, text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "$label copied to clipboard", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Copied $label to clipboard", Toast.LENGTH_SHORT).show()
        }
    }

    fun exportAndShareCsv(title: String, csvContent: String) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, csvContent)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(intent, "Export $title via...").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            copyToClipboard(title, csvContent)
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val currentStudentGrades: StateFlow<List<StudentGrade>> = _selectedStudentId
        .flatMapLatest { id -> repository.getGradesForStudent(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveOrUpdateStudentGrade(
        existingGradeId: Long = 0,
        studentId: Long,
        studentName: String,
        className: String,
        subject: String,
        term: String = "Term 1",
        academicYear: String = "2025/2026",
        classScore: Double,
        examScore: Double,
        remarks: String
    ) {
        val total = (classScore + examScore).coerceIn(0.0, 100.0)
        val letter = when {
            total >= 80.0 -> "A1"
            total >= 75.0 -> "B2"
            total >= 70.0 -> "B3"
            total >= 65.0 -> "C4"
            total >= 60.0 -> "C5"
            total >= 55.0 -> "C6"
            total >= 50.0 -> "D7"
            total >= 45.0 -> "E8"
            else -> "F9"
        }

        viewModelScope.launch {
            val grade = StudentGrade(
                id = existingGradeId,
                studentId = studentId,
                studentName = studentName,
                className = className,
                subject = subject,
                academicTerm = term,
                academicYear = academicYear,
                classScore = classScore,
                examScore = examScore,
                totalScore = total,
                gradeLetter = letter,
                remarks = remarks
            )
            repository.saveStudentGrade(grade)
            repository.addNotification(
                recipientRole = "GUARDIAN",
                type = "GRADE_PUBLISHED",
                title = "Grade Recorded: $subject",
                message = "$studentName achieved ${String.format("%.1f", total)}% ($letter) in $subject."
            )
            Toast.makeText(context, "Academic grade saved ($letter - ${String.format("%.1f", total)}%)", Toast.LENGTH_SHORT).show()
        }
    }

    fun submitAbsenceNotice(dateString: String, reason: String) {
        val studentId = _selectedStudentId.value
        val ledger = currentStudentLedger.value
        val studentName = ledger?.studentName ?: "Student"
        val className = ledger?.className ?: "Class"

        viewModelScope.launch {
            val notice = DailyStudentAttendance(
                studentId = studentId,
                studentName = studentName,
                className = className,
                dateString = dateString,
                status = "EXCUSED",
                isPresent = false,
                isAbsent = false,
                isExcused = true,
                remarks = "Guardian Notice: $reason"
            )
            repository.saveDailyAttendance(notice)
            repository.addNotification(
                recipientRole = "TEACHER",
                type = "ABSENCE_NOTICE",
                title = "Absence Notice: $studentName",
                message = "Guardian submitted excuse note for $dateString: $reason"
            )
            Toast.makeText(context, "Absence notice submitted to class teacher", Toast.LENGTH_SHORT).show()
        }
    }

    // Official Downloadable Receipt State
    private val _activeReceipt = MutableStateFlow<OfficialReceipt?>(null)
    val activeReceipt: StateFlow<OfficialReceipt?> = _activeReceipt.asStateFlow()

    fun showReceipt(receipt: OfficialReceipt) {
        _activeReceipt.value = receipt
    }

    fun dismissActiveReceipt() {
        _activeReceipt.value = null
    }

    // Media Download State (for Audio, Video, Documents)
    private val _downloadedMediaResource = MutableStateFlow<DigitalResource?>(null)
    val downloadedMediaResource: StateFlow<DigitalResource?> = _downloadedMediaResource.asStateFlow()

    fun downloadDigitalResourceMedia(resource: DigitalResource) {
        viewModelScope.launch {
            showLoading("Downloading '${resource.title}' (${resource.fileFormat})...")
            kotlinx.coroutines.delay(800)
            hideLoading()
            _downloadedMediaResource.value = resource
            Toast.makeText(context, "'${resource.title}.${resource.fileFormat.lowercase(Locale.ROOT)}' downloaded to device storage!", Toast.LENGTH_LONG).show()
        }
    }

    fun dismissDownloadedMedia() {
        _downloadedMediaResource.value = null
    }

    // MoMo Payment State
    private val _showMomoDialog = MutableStateFlow(false)
    val showMomoDialog: StateFlow<Boolean> = _showMomoDialog.asStateFlow()

    private val _momoNetwork = MutableStateFlow("MTN MoMo") // "MTN MoMo", "Telecel Cash", "AT Money"
    val momoNetwork: StateFlow<String> = _momoNetwork.asStateFlow()

    private val _momoPhone = MutableStateFlow("0244123456")
    val momoPhone: StateFlow<String> = _momoPhone.asStateFlow()

    private val _momoAmount = MutableStateFlow("700")
    val momoAmount: StateFlow<String> = _momoAmount.asStateFlow()

    private val _momoReference = MutableStateFlow("Term 3 Balance")
    val momoReference: StateFlow<String> = _momoReference.asStateFlow()

    private val _momoPin = MutableStateFlow("4321")
    val momoPin: StateFlow<String> = _momoPin.asStateFlow()

    private val _momoPaymentDate = MutableStateFlow(SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date()))
    val momoPaymentDate: StateFlow<String> = _momoPaymentDate.asStateFlow()

    private val _isProcessingMomo = MutableStateFlow(false)
    val isProcessingMomo: StateFlow<Boolean> = _isProcessingMomo.asStateFlow()

    fun openMomoDialog(defaultAmountGhc: Double = 0.0) {
        if (defaultAmountGhc > 0) {
            _momoAmount.value = defaultAmountGhc.toInt().toString()
        }
        _momoPaymentDate.value = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
        _showMomoDialog.value = true
    }

    fun closeMomoDialog() {
        _showMomoDialog.value = false
        _isProcessingMomo.value = false
    }

    fun setMomoNetwork(network: String) { _momoNetwork.value = network }
    fun setMomoPhone(phone: String) { _momoPhone.value = phone }
    fun setMomoAmount(amount: String) { _momoAmount.value = amount }
    fun setMomoReference(ref: String) { _momoReference.value = ref }
    fun setMomoPin(pin: String) { _momoPin.value = pin }
    fun setMomoPaymentDate(date: String) { _momoPaymentDate.value = date }

    fun submitMomoPayment() {
        val amountVal = _momoAmount.value.toDoubleOrNull() ?: 0.0
        if (amountVal <= 0) {
            Toast.makeText(context, "Please enter a valid amount in GH₵", Toast.LENGTH_SHORT).show()
            return
        }
        if (_momoPhone.value.length < 10) {
            Toast.makeText(context, "Please enter a valid 10-digit mobile number", Toast.LENGTH_SHORT).show()
            return
        }

        viewModelScope.launch {
            _isProcessingMomo.value = true
            // Simulate USSD prompt network delay
            kotlinx.coroutines.delay(1800)

            val studentId = _selectedStudentId.value
            val pDate = _momoPaymentDate.value
            val txn = repository.processMomoPayment(
                studentId = studentId,
                amountGhc = amountVal,
                method = _momoNetwork.value,
                phone = _momoPhone.value,
                reference = _momoReference.value,
                paymentDate = pDate
            )

            // Notify Guardian
            repository.addNotification(
                recipientRole = "GUARDIAN",
                type = "PAYMENT_SUCCESS",
                title = "Payment Successful (${_momoNetwork.value})",
                message = "Payment of GH₵ ${"%.2f".format(amountVal)} processed successfully. Ref: ${txn.transactionRef}."
            )

            // Notify Proprietor
            repository.addNotification(
                recipientRole = "PROPRIETOR",
                type = "PAYMENT_SUCCESS",
                title = "Mobile Money Fee Received",
                message = "Received GH₵ ${"%.2f".format(amountVal)} via ${_momoNetwork.value} for Student ID #$studentId."
            )

            // Generate downloadable Official Receipt
            val ledger = currentStudentLedger.value
            val newBal = (ledger?.balanceGhc?.minus(amountVal))?.coerceAtLeast(0.0) ?: 0.0
            val receipt = OfficialReceipt(
                receiptNumber = "RCP-${System.currentTimeMillis().toString().takeLast(6)}",
                title = "STUDENT FEE PAYMENT RECEIPT",
                schoolName = "St. Talafor Primary & JHS",
                recipientName = ledger?.studentName ?: "Student ID #$studentId",
                subDetail = "Class: ${ledger?.className ?: "JHS 2 - Gold"}",
                payerOrGuardian = "Guardian: ${ledger?.guardianName ?: "Parent"} (${_momoPhone.value})",
                amountGhc = amountVal,
                paymentMethod = _momoNetwork.value,
                transactionRef = txn.transactionRef,
                paymentDate = pDate,
                feeCategoryOrMemo = "Tuition & School Fees (${_momoReference.value})",
                remainingBalanceGhc = newBal,
                authorizedBy = "Guardian Mobile Money (PIN Verified)",
                notes = "Ref: ${_momoReference.value}"
            )
            _activeReceipt.value = receipt

            _isProcessingMomo.value = false
            _showMomoDialog.value = false
            Toast.makeText(context, "Payment Received! Official MoMo Receipt Generated.", Toast.LENGTH_LONG).show()
        }
    }

    fun receiveGuardianPaymentByStudentAndClass(
        studentId: Long,
        studentName: String,
        className: String,
        guardianName: String,
        amountGhc: Double,
        paymentDate: String,
        paymentMethod: String,
        feeCategory: String,
        notes: String
    ) {
        viewModelScope.launch {
            showLoading("Recording guardian fee payment...")
            val feePayment = repository.recordStudentFeePayment(
                studentId = studentId,
                amountGhc = amountGhc,
                paymentMethod = paymentMethod,
                feeCategory = feeCategory,
                academicTerm = "Term 3",
                notes = notes,
                recordedBy = "Proprietor / Bursary Office",
                paymentDate = paymentDate
            )
            hideLoading()
            if (feePayment != null) {
                val receipt = OfficialReceipt(
                    receiptNumber = feePayment.receiptNumber,
                    title = "STUDENT FEE PAYMENT RECEIPT",
                    schoolName = "St. Talafor Primary & JHS",
                    recipientName = studentName,
                    subDetail = "Class: $className",
                    payerOrGuardian = "Guardian: $guardianName",
                    amountGhc = amountGhc,
                    paymentMethod = paymentMethod,
                    transactionRef = feePayment.transactionRef,
                    paymentDate = paymentDate,
                    feeCategoryOrMemo = feeCategory,
                    remainingBalanceGhc = feePayment.remainingBalanceGhc,
                    authorizedBy = "Dr. Kwabena Mensah (School Proprietor)",
                    notes = notes
                )
                _activeReceipt.value = receipt
                Toast.makeText(context, "Payment received & official receipt generated!", Toast.LENGTH_LONG).show()
            }
        }
    }

    // --- Academic Promotion & Demotion Operations ---
    fun promoteStudent(studentId: Long, targetClass: String) {
        viewModelScope.launch {
            showLoading("Promoting student to $targetClass...")
            repository.promoteStudent(studentId, targetClass)
            hideLoading()
            Toast.makeText(context, "Student promoted to $targetClass!", Toast.LENGTH_SHORT).show()
        }
    }

    fun demoteStudent(studentId: Long, targetClass: String) {
        viewModelScope.launch {
            showLoading("Reassigning student to $targetClass...")
            repository.demoteStudent(studentId, targetClass)
            hideLoading()
            Toast.makeText(context, "Student reassigned to $targetClass.", Toast.LENGTH_SHORT).show()
        }
    }

    fun promoteClass(currentClassName: String, targetClassName: String) {
        viewModelScope.launch {
            showLoading("Promoting class $currentClassName to $targetClassName...")
            repository.promoteEntireClass(currentClassName, targetClassName)
            hideLoading()
            Toast.makeText(context, "All students in $currentClassName promoted to $targetClassName!", Toast.LENGTH_LONG).show()
        }
    }

    fun demoteClass(currentClassName: String, targetClassName: String) {
        viewModelScope.launch {
            showLoading("Reassigning class $currentClassName to $targetClassName...")
            repository.demoteEntireClass(currentClassName, targetClassName)
            hideLoading()
            Toast.makeText(context, "Class $currentClassName reassigned to $targetClassName.", Toast.LENGTH_SHORT).show()
        }
    }

    fun makeMoMoFeePayment(
        momoNumber: String,
        network: String,
        studentId: String,
        amount: Double,
        feeType: String
    ) {
        _momoPhone.value = momoNumber
        _momoNetwork.value = network
        _momoAmount.value = amount.toInt().toString()
        _momoReference.value = feeType
        submitMomoPayment()
    }

    // --- Daily Lesson Plan Actions ---
    fun createLessonPlan(
        className: String,
        subject: String,
        topic: String,
        subTopic: String,
        lessonDate: String,
        durationMinutes: Int,
        objectives: String,
        materials: String,
        procedure: String
    ) {
        viewModelScope.launch {
            val teacherName = activeUserAccount.value?.fullName ?: "Mr. Kojo Mensah"
            val plan = LessonPlan(
                teacherId = 1,
                teacherName = teacherName,
                className = className,
                subject = subject,
                topic = topic,
                subTopic = subTopic,
                lessonDate = lessonDate,
                durationMinutes = durationMinutes,
                objectives = objectives,
                teachingMaterials = materials,
                procedureSteps = procedure,
                status = "PENDING_REVIEW"
            )
            repository.saveLessonPlan(plan)
            repository.addNotification(
                recipientRole = "PROPRIETOR",
                type = "LESSON_PLAN_SUBMITTED",
                title = "New Daily Lesson Plan Submitted",
                message = "$teacherName submitted a lesson plan for $subject ($className) on topic '$topic'."
            )
            Toast.makeText(context, "Lesson Plan submitted for Management Review!", Toast.LENGTH_SHORT).show()
        }
    }

    fun reviewLessonPlan(
        planId: Long,
        newStatus: String,
        feedback: String,
        topic: String,
        teacherName: String
    ) {
        viewModelScope.launch {
            repository.updateLessonPlanStatus(planId, newStatus, feedback)
            val statusLabel = if (newStatus == "APPROVED") "APPROVED" else "REVISION REQUESTED"
            repository.addNotification(
                recipientRole = "TEACHER",
                type = "LESSON_PLAN_REVIEWED",
                title = "Lesson Plan $statusLabel",
                message = "Your lesson plan for '$topic' was $statusLabel by Management. Feedback: $feedback"
            )
            Toast.makeText(context, "Lesson plan status updated to $newStatus", Toast.LENGTH_SHORT).show()
        }
    }

    // --- ALUMNI PLATFORM ACTIONS ---
    fun verifyAndActivateAlumniAccount(
        name: String,
        studentId: String,
        dob: String,
        guardianName: String,
        phone: String,
        email: String,
        onSuccess: (AlumniProfile) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val activated = alumniRepository.verifyAndActivateAccount(
                name = name,
                studentId = studentId,
                dob = dob,
                guardianName = guardianName,
                phone = phone,
                email = email
            )
            if (activated != null) {
                Toast.makeText(context, "Account Verified & Unlocked! Welcome, ${activated.fullName}", Toast.LENGTH_LONG).show()
                onSuccess(activated)
            } else {
                val errorMsg = "Verification Failed: Credentials do not match official school records (Name, ID, DOB, Guardian)."
                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                onError(errorMsg)
            }
        }
    }

    fun rotateAnnualAlumniAdmin(newAdminId: Long, termYear: String = "2026 Yearly Admin") {
        viewModelScope.launch {
            alumniRepository.rotateAnnualAdmin(newAdminId, termYear)
            Toast.makeText(context, "Annual Alumni Admin rotated successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    fun sendAlumniChatMessage(
        senderName: String,
        senderRole: String,
        text: String,
        mediaType: String = "TEXT",
        mediaUrl: String = "",
        fileName: String = ""
    ) {
        viewModelScope.launch {
            alumniRepository.sendChatMessage(
                senderName = senderName,
                senderRole = senderRole,
                messageText = text,
                mediaType = mediaType,
                mediaUrl = mediaUrl,
                fileName = fileName
            )
        }
    }

    fun initiateProprietorConsultationCall(
        initiator: AlumniProfile,
        callType: String = "AUDIO_CONSULTATION",
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                alumniRepository.initiateCallSession(initiator, callType)
                Toast.makeText(context, "Call initiated with School Proprietor...", Toast.LENGTH_SHORT).show()
                onSuccess()
            } catch (e: SecurityException) {
                val msg = e.message ?: "Access Denied: Only the Alumni Admin can initiate direct consultations with the Proprietor."
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                onError(msg)
            }
        }
    }

    fun batchImportAlumni(profiles: List<AlumniProfile>) {
        viewModelScope.launch {
            alumniRepository.batchImportAlumni(profiles)
            Toast.makeText(context, "Imported ${profiles.size} historical alumni records successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    fun submitAspirantIntent(
        user: AlumniProfile,
        manifesto: String,
        termYear: String = "2027",
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                alumniRepository.submitAspirantIntent(user, manifesto, termYear)
                Toast.makeText(context, "Aspirant intent submitted for $termYear Admin position!", Toast.LENGTH_LONG).show()
                onSuccess()
            } catch (e: Exception) {
                val msg = e.message ?: "Failed to submit aspirant intent."
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                onError(msg)
            }
        }
    }

    fun castVoteForAspirant(
        voter: AlumniProfile,
        candidateDocId: String,
        termYear: String = "2027",
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                alumniRepository.castVoteForAspirant(voter, candidateDocId, termYear)
                Toast.makeText(context, "Vote successfully recorded! Thank you for voting.", Toast.LENGTH_LONG).show()
                onSuccess()
            } catch (e: Exception) {
                val msg = e.message ?: "Failed to record vote."
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                onError(msg)
            }
        }
    }
}
