package com.example.data.repository

import com.example.data.dao.AttendanceDao
import com.example.data.dao.GradeDao
import com.example.data.dao.GuardianDao
import com.example.data.dao.LessonPlanDao
import com.example.data.dao.PortalUserDao
import com.example.data.dao.SchoolDao
import com.example.data.dao.SchoolEventDao
import com.example.data.dao.TimetableDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SchoolRepository(
    private val dao: SchoolDao,
    private val guardianDao: GuardianDao? = null,
    private val gradeDao: GradeDao? = null,
    private val portalUserDao: PortalUserDao? = null,
    private val schoolEventDao: SchoolEventDao? = null,
    private val timetableDao: TimetableDao? = null,
    private val attendanceDao: AttendanceDao? = null,
    private val lessonPlanDao: LessonPlanDao? = null
) {

    val allStaff: Flow<List<StaffMember>> = dao.getAllStaff()
    val allApprovals: Flow<List<TransactionApproval>> = dao.getAllApprovals()
    val allMessages: Flow<List<MessageLog>> = dao.getAllMessages()
    val allClockInLogs: Flow<List<ClockInLog>> = dao.getAllClockInLogs()
    val allStudentLedgers: Flow<List<StudentLedger>> = dao.getAllStudentLedgers()
    val allStudentProfiles: Flow<List<StudentProfile>> = dao.getAllStudentProfiles()
    val activeUserAccount: Flow<UserAccount?> = dao.getActiveUserAccount()
    val schoolSettings: Flow<SchoolSettings?> = dao.getSchoolSettings()
    val allGuardians: Flow<List<GuardianProfile>> = guardianDao?.getAllGuardians() ?: emptyFlow()
    val allGrades: Flow<List<StudentGrade>> = gradeDao?.getAllGrades() ?: emptyFlow()
    val allLessonPlans: Flow<List<LessonPlan>> = lessonPlanDao?.getAllLessonPlans() ?: emptyFlow()
    val allPortalAccounts: Flow<List<UserPortalAccount>> = portalUserDao?.getAllAccounts() ?: emptyFlow()
    val activeLoggedInPortalAccount: Flow<UserPortalAccount?> = portalUserDao?.getActiveLoggedInAccount() ?: emptyFlow()
    val allSchoolEvents: Flow<List<SchoolEvent>> = schoolEventDao?.getAllEvents() ?: emptyFlow()
    val allTimetables: Flow<List<ClassTimetable>> = timetableDao?.getAllTimetables() ?: emptyFlow()
    val unsyncedAttendance: Flow<List<AttendanceRecord>> = dao.getUnsyncedAttendance()

    fun getNotificationsForRole(role: String): Flow<List<AppNotification>> =
        dao.getNotificationsForRole(role)

    fun getAllNotifications(): Flow<List<AppNotification>> =
        dao.getAllNotifications()

    fun getAttendanceForClass(className: String): Flow<List<AttendanceRecord>> =
        dao.getAttendanceForClass(className)

    val allDailyAttendance: Flow<List<DailyStudentAttendance>> =
        attendanceDao?.getAllDailyAttendance() ?: dao.getAllDailyAttendance()

    fun getDailyAttendanceByDate(date: String): Flow<List<DailyStudentAttendance>> =
        attendanceDao?.getAttendanceByDate(date) ?: dao.getDailyAttendanceByDate(date)

    fun getDailyAttendanceForStudent(studentId: Long): Flow<List<DailyStudentAttendance>> =
        attendanceDao?.getAttendanceForStudent(studentId) ?: dao.getDailyAttendanceForStudent(studentId)

    suspend fun saveDailyAttendance(attendance: DailyStudentAttendance): Long =
        attendanceDao?.insertAttendance(attendance) ?: dao.insertDailyAttendance(attendance)

    suspend fun updateDailyAttendance(attendance: DailyStudentAttendance) =
        attendanceDao?.updateAttendance(attendance) ?: dao.updateDailyAttendance(attendance)

    fun getStudentLedger(studentId: Long): Flow<StudentLedger?> =
        dao.getStudentLedgerById(studentId)

    fun getTransactionsForStudent(studentId: Long): Flow<List<FeeTransaction>> =
        dao.getTransactionsForStudent(studentId)

    // User Accounts Authentication & Switching
    suspend fun registerOrUpdateUser(
        fullName: String,
        email: String,
        phone: String,
        role: String,
        schoolName: String
    ): UserAccount {
        dao.clearLoggedInUsers()
        val existing = dao.getUserByEmail(email)
        val user = if (existing != null) {
            existing.copy(
                fullName = fullName,
                phone = phone,
                role = role,
                schoolName = schoolName,
                isLoggedIn = true
            )
        } else {
            UserAccount(
                fullName = fullName,
                email = email,
                phone = phone,
                role = role,
                schoolName = schoolName,
                isLoggedIn = true
            )
        }
        val id = dao.insertUserAccount(user)
        val savedUser = user.copy(id = if (existing != null) existing.id else id)

        // If Proprietor, also update global school settings
        if (role == "PROPRIETOR" && schoolName.isNotBlank()) {
            dao.saveSchoolSettings(SchoolSettings(id = 1, schoolName = schoolName))
        }

        return savedUser
    }

    suspend fun switchActiveUser(userAccount: UserAccount) {
        dao.clearLoggedInUsers()
        dao.updateUserAccount(userAccount.copy(isLoggedIn = true))
    }

    suspend fun logoutActiveUser() {
        dao.clearLoggedInUsers()
    }

    suspend fun updateSchoolName(newName: String) {
        if (newName.isNotBlank()) {
            dao.saveSchoolSettings(SchoolSettings(id = 1, schoolName = newName))
            // Also update any proprietor accounts
            val active = dao.getActiveUserAccount().first()
            if (active != null) {
                dao.updateUserAccount(active.copy(schoolName = newName))
            }
        }
    }

    // Notifications
    suspend fun addNotification(
        recipientRole: String,
        type: String,
        title: String,
        message: String
    ) {
        val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        val notification = AppNotification(
            recipientRole = recipientRole,
            type = type,
            title = title,
            message = message,
            timestampString = dateFormat.format(Date()),
            isRead = false
        )
        dao.insertNotification(notification)
    }

    suspend fun markNotificationRead(id: Long) {
        dao.markNotificationRead(id)
    }

    suspend fun markAllNotificationsRead(role: String) {
        dao.markAllNotificationsReadForRole(role)
    }

    suspend fun clearNotifications(role: String) {
        dao.clearNotificationsForRole(role)
    }

    suspend fun updateStaffPermission(staff: StaffMember) {
        dao.updateStaff(staff)
    }

    suspend fun addStaffMember(staff: StaffMember) {
        dao.insertStaff(staff)
    }

    suspend fun dropStaffMember(staffId: Long) {
        dao.deleteStaffMemberById(staffId)
    }

    suspend fun addStudentDirect(
        studentName: String,
        className: String,
        guardianPhone: String = "0244123456",
        totalFeesGhc: Double = 1200.0
    ): StudentLedger {
        val existingLedgers = dao.getAllStudentLedgers().first()
        val nextStudentId = if (existingLedgers.isNotEmpty()) (existingLedgers.maxOf { it.studentId } + 1) else 106L
        val indexCode = "AKM/2026/${String.format("%03d", nextStudentId)}"

        val newLedger = StudentLedger(
            studentId = nextStudentId,
            studentName = studentName,
            indexNumber = indexCode,
            className = className,
            guardianName = "Guardian of $studentName",
            guardianPhone = guardianPhone,
            termTuitionGhc = totalFeesGhc * 0.7,
            ptaLevyGhc = 100.0,
            ictLabFeeGhc = 100.0,
            feedingFeeGhc = (totalFeesGhc * 0.3 - 200.0).coerceAtLeast(0.0),
            totalFeesGhc = totalFeesGhc,
            paidFeesGhc = 0.0,
            balanceGhc = totalFeesGhc
        )
        dao.insertStudentLedger(newLedger)

        val newProfile = StudentProfile(
            id = nextStudentId,
            fullName = studentName,
            indexNumber = indexCode,
            className = className,
            guardianName = "Guardian of $studentName",
            guardianPhone = guardianPhone,
            admissionDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
            enrollmentStatus = "ACTIVE"
        )
        dao.insertStudentProfile(newProfile)

        return newLedger
    }

    suspend fun dropStudent(studentId: Long) {
        dao.deleteStudentLedgerById(studentId)
        val profile = dao.getStudentProfileById(studentId).first()
        if (profile != null) {
            dao.deleteStudentProfile(profile)
        }
    }

    suspend fun requestTeacherAddStudent(
        teacherName: String,
        studentName: String,
        className: String,
        guardianPhone: String = "0244123456",
        estimatedFeesGhc: Double = 1200.0,
        remarks: String = ""
    ): TransactionApproval {
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val approval = TransactionApproval(
            requestType = "STUDENT_REGISTRATION",
            requestorName = teacherName,
            requestorRole = "TEACHER",
            amountGhc = estimatedFeesGhc,
            reason = "Add Student Request: '$studentName' in class $className. Guardian Phone: $guardianPhone. $remarks",
            status = "PENDING",
            dateString = dateFormat.format(Date())
        )
        dao.insertApproval(approval)
        return approval
    }

    suspend fun updateApprovalStatus(approvalId: Long, newStatus: String, note: String) {
        val approvals = dao.getAllApprovals().first()
        val existing = approvals.find { it.id == approvalId }
        if (existing != null) {
            dao.updateApproval(
                existing.copy(
                    status = newStatus,
                    decisionNote = note
                )
            )
        }
    }

    suspend fun addTransactionApproval(approval: TransactionApproval) {
        dao.insertApproval(approval)
    }

    suspend fun logMessage(message: MessageLog) {
        dao.insertMessage(message)
    }

    suspend fun logClockIn(clockIn: ClockInLog) {
        dao.insertClockIn(clockIn)
    }

    suspend fun updateAttendanceRecord(record: AttendanceRecord, isOnline: Boolean = true) {
        val updated = record.copy(
            isSynced = isOnline,
            lastModifiedTimestamp = System.currentTimeMillis()
        )
        dao.updateAttendance(updated)
    }

    suspend fun syncPendingAttendanceRecords(): Int {
        val pendingList = dao.getUnsyncedAttendanceDirect()
        if (pendingList.isNotEmpty()) {
            dao.markAllAttendanceSynced()
        }
        return pendingList.size
    }

    suspend fun trackDailyAttendance(
        studentId: Long,
        studentName: String,
        className: String,
        dateString: String,
        status: String,
        remarks: String = ""
    ): DailyStudentAttendance {
        val upperStatus = status.uppercase(Locale.getDefault())
        val isPresent = upperStatus == "PRESENT"
        val isAbsent = upperStatus == "ABSENT"
        val isExcused = upperStatus == "EXCUSED"

        val record = DailyStudentAttendance(
            studentId = studentId,
            studentName = studentName,
            className = className,
            dateString = dateString,
            status = upperStatus,
            isPresent = isPresent,
            isAbsent = isAbsent,
            isExcused = isExcused,
            remarks = remarks
        )
        val id = dao.insertDailyAttendance(record)
        return record.copy(id = id)
    }

    suspend fun updateDailyAttendanceRecord(record: DailyStudentAttendance) {
        val upperStatus = record.status.uppercase(Locale.getDefault())
        val updated = record.copy(
            status = upperStatus,
            isPresent = upperStatus == "PRESENT",
            isAbsent = upperStatus == "ABSENT",
            isExcused = upperStatus == "EXCUSED"
        )
        dao.updateDailyAttendance(updated)
    }

    // --- Guardian Profile Repository Operations ---
    fun getGuardianById(id: Long): Flow<GuardianProfile?> =
        guardianDao?.getGuardianById(id) ?: emptyFlow()

    fun getGuardiansForStudent(studentId: Long): Flow<List<GuardianProfile>> =
        guardianDao?.getGuardiansForStudent(studentId.toString()) ?: emptyFlow()

    suspend fun saveGuardianProfile(guardian: GuardianProfile): Long {
        return guardianDao?.insertGuardian(guardian) ?: 0L
    }

    suspend fun updateGuardianProfile(guardian: GuardianProfile) {
        guardianDao?.updateGuardian(guardian)
    }

    suspend fun deleteGuardianProfile(guardian: GuardianProfile) {
        guardianDao?.deleteGuardian(guardian)
    }

    // --- Student Grade Repository Operations ---
    fun getGradesForStudent(studentId: Long): Flow<List<StudentGrade>> =
        gradeDao?.getGradesForStudent(studentId) ?: emptyFlow()

    fun getGradesByClassAndSubject(className: String, subject: String): Flow<List<StudentGrade>> =
        gradeDao?.getGradesByClassAndSubject(className, subject) ?: emptyFlow()

    fun getGradesForStudentInTerm(studentId: Long, term: String, year: String): Flow<List<StudentGrade>> =
        gradeDao?.getGradesForStudentInTerm(studentId, term, year) ?: emptyFlow()

    suspend fun saveStudentGrade(grade: StudentGrade): Long {
        return gradeDao?.insertGrade(grade) ?: 0L
    }

    suspend fun updateStudentGrade(grade: StudentGrade) {
        gradeDao?.updateGrade(grade)
    }

    suspend fun deleteStudentGrade(grade: StudentGrade) {
        gradeDao?.deleteGrade(grade)
    }

    // --- Daily Lesson Plan Operations ---
    fun getLessonPlansForTeacher(teacherId: Long): Flow<List<LessonPlan>> =
        lessonPlanDao?.getLessonPlansForTeacher(teacherId) ?: emptyFlow()

    fun getLessonPlansByClassAndSubject(className: String, subject: String): Flow<List<LessonPlan>> =
        lessonPlanDao?.getLessonPlansByClassAndSubject(className, subject) ?: emptyFlow()

    suspend fun saveLessonPlan(plan: LessonPlan): Long =
        lessonPlanDao?.insertLessonPlan(plan) ?: 0L

    suspend fun updateLessonPlanStatus(planId: Long, status: String, feedback: String) =
        lessonPlanDao?.updateLessonPlanStatus(planId, status, feedback)

    // --- Portal User Access Control Operations ---
    suspend fun registerPortalUserAccount(
        fullName: String,
        email: String,
        phone: String,
        role: String,
        schoolName: String = "St. Talafor Academy"
    ): UserPortalAccount {
        val upperRole = role.uppercase(Locale.getDefault())

        if (portalUserDao != null && upperRole == "PROPRIETOR") {
            val existingProprietorCount = portalUserDao.getProprietorCountForSchool(schoolName)
            if (existingProprietorCount >= 1) {
                throw IllegalStateException("A proprietor account already exists for $schoolName. Only one proprietor is permitted per school.")
            }
        }

        val account = UserPortalAccount(
            fullName = fullName,
            email = email,
            phone = phone,
            role = upperRole,
            schoolName = schoolName,
            isLoggedIn = true,
            isProprietor = (upperRole == "PROPRIETOR"),
            portalAccessRole = upperRole
        )

        val id = portalUserDao?.let { dao ->
            dao.logoutAllSessions()
            dao.insertAccount(account)
        } ?: 0L

        return account.copy(id = id)
    }

    suspend fun loginToSingleUserPortalSession(userId: Long) {
        portalUserDao?.loginAsSingleUser(userId)
    }

    suspend fun logoutPortalSession() {
        portalUserDao?.logoutAllSessions()
    }

    // --- School Event Repository Operations ---
    fun getSchoolEventsByAudience(audience: String): Flow<List<SchoolEvent>> =
        schoolEventDao?.getEventsByAudience(audience) ?: emptyFlow()

    fun getSchoolEventById(id: Long): Flow<SchoolEvent?> =
        schoolEventDao?.getEventById(id) ?: emptyFlow()

    suspend fun saveSchoolEvent(event: SchoolEvent): Long {
        return schoolEventDao?.insertEvent(event) ?: 0L
    }

    suspend fun updateSchoolEvent(event: SchoolEvent) {
        schoolEventDao?.updateEvent(event)
    }

    suspend fun deleteSchoolEvent(event: SchoolEvent) {
        schoolEventDao?.deleteEvent(event)
    }

    // --- Timetable Repository Operations ---
    fun getTimetableForClass(className: String): Flow<List<ClassTimetable>> =
        timetableDao?.getTimetableForClass(className) ?: emptyFlow()

    fun getTimetableForClassAndDay(className: String, dayOfWeek: String): Flow<List<ClassTimetable>> =
        timetableDao?.getTimetableForClassAndDay(className, dayOfWeek) ?: emptyFlow()

    fun getTimetableForTeacher(teacherName: String, teacherId: Long = 0): Flow<List<ClassTimetable>> =
        timetableDao?.getTimetableForTeacher(teacherName, teacherId) ?: emptyFlow()

    suspend fun saveTimetableSlot(slot: ClassTimetable): Long {
        return timetableDao?.insertTimetableSlot(slot) ?: 0L
    }

    suspend fun updateTimetableSlot(slot: ClassTimetable) {
        timetableDao?.updateTimetableSlot(slot)
    }

    suspend fun deleteTimetableSlot(slot: ClassTimetable) {
        timetableDao?.deleteTimetableSlot(slot)
    }

    // --- Student Profile Repository Operations ---
    fun getStudentProfileById(id: Long): Flow<StudentProfile?> = dao.getStudentProfileById(id)

    fun getStudentProfilesByClass(className: String): Flow<List<StudentProfile>> = dao.getStudentProfilesByClass(className)

    suspend fun saveStudentProfile(student: StudentProfile): Long {
        return dao.insertStudentProfile(student)
    }

    suspend fun updateStudentProfile(student: StudentProfile) {
        dao.updateStudentProfile(student)
    }

    suspend fun deleteStudentProfile(student: StudentProfile) {
        dao.deleteStudentProfile(student)
    }

    suspend fun processMomoPayment(
        studentId: Long,
        amountGhc: Double,
        method: String,
        phone: String,
        reference: String
    ): FeeTransaction {
        val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        val dateStr = dateFormat.format(Date())
        val txnRef = "MOMO-${System.currentTimeMillis().toString().takeLast(8)}"

        val transaction = FeeTransaction(
            studentId = studentId,
            transactionRef = txnRef,
            amountGhc = amountGhc,
            paymentMethod = method,
            channelNumber = phone,
            description = "MoMo Fee Payment - Ref: $reference",
            dateString = dateStr,
            status = "SUCCESS"
        )
        dao.insertFeeTransaction(transaction)

        // Update Student Ledger Balance
        val ledger = dao.getStudentLedgerById(studentId).first()
        if (ledger != null) {
            val newPaid = ledger.paidFeesGhc + amountGhc
            val newBalance = (ledger.totalFeesGhc - newPaid).coerceAtLeast(0.0)
            dao.updateStudentLedger(
                ledger.copy(
                    paidFeesGhc = newPaid,
                    balanceGhc = newBalance
                )
            )
        }

        return transaction
    }

    suspend fun seedInitialDataIfEmpty() {
        val existingStaff = dao.getAllStaff().first()
        if (existingStaff.isEmpty()) {
            val staffList = listOf(
                StaffMember(
                    id = 1,
                    name = "Mr. Kojo Mensah",
                    staffCode = "AK-T01",
                    primaryRole = "JHS 2 Class Master & Math Head",
                    isCanEditGrades = true,
                    isCanMarkAttendance = true,
                    isCanApproveOverrides = false,
                    isCanAccessFinancials = false,
                    isCanSendSms = true,
                    isCanManageRoles = false
                ),
                StaffMember(
                    id = 2,
                    name = "Mrs. Abena Osei",
                    staffCode = "AK-T02",
                    primaryRole = "Primary 6 Class Teacher",
                    isCanEditGrades = true,
                    isCanMarkAttendance = true,
                    isCanApproveOverrides = false,
                    isCanAccessFinancials = false,
                    isCanSendSms = true,
                    isCanManageRoles = false
                ),
                StaffMember(
                    id = 3,
                    name = "Kwame Boateng",
                    staffCode = "AK-A01",
                    primaryRole = "Chief Bursar & Accountant",
                    isCanEditGrades = false,
                    isCanMarkAttendance = false,
                    isCanApproveOverrides = true,
                    isCanAccessFinancials = true,
                    isCanSendSms = true,
                    isCanManageRoles = false
                ),
                StaffMember(
                    id = 4,
                    name = "Miss Akosua Addo",
                    staffCode = "AK-T03",
                    primaryRole = "English & Social Studies Lead",
                    isCanEditGrades = true,
                    isCanMarkAttendance = true,
                    isCanApproveOverrides = false,
                    isCanAccessFinancials = false,
                    isCanSendSms = false,
                    isCanManageRoles = false
                )
            )
            dao.insertAllStaff(staffList)

            val approvals = listOf(
                TransactionApproval(
                    id = 1,
                    requestorName = "Mr. Kojo Mensah",
                    requestorRole = "Senior Teacher",
                    requestType = "STAFF_LOAN",
                    amountGhc = 1200.0,
                    reason = "Emergency medical fee advance for dependent",
                    dateString = "20 Jul 2026",
                    status = "PENDING"
                ),
                TransactionApproval(
                    id = 2,
                    requestorName = "Mrs. Abena Osei",
                    requestorRole = "Primary Teacher",
                    requestType = "FEE_OVERRIDE",
                    amountGhc = 450.0,
                    reason = "Hardship fee waiver request for orphan student (Kofi Asante)",
                    dateString = "19 Jul 2026",
                    status = "PENDING"
                ),
                TransactionApproval(
                    id = 3,
                    requestorName = "Kwame Boateng",
                    requestorRole = "Bursar",
                    requestType = "EQUIPMENT_PURCHASE",
                    amountGhc = 2800.0,
                    reason = "ICT Computer Lab projector replacement",
                    dateString = "15 Jul 2026",
                    status = "APPROVED",
                    decisionNote = "Approved in Term 3 Capital Expenditure budget"
                ),
                TransactionApproval(
                    id = 4,
                    requestorName = "Miss Akosua Addo",
                    requestorRole = "Teacher",
                    requestType = "STAFF_LOAN",
                    amountGhc = 350.0,
                    reason = "BECE preparation supplementary book allowance",
                    dateString = "12 Jul 2026",
                    status = "REJECTED",
                    decisionNote = "Books already provided by Ministry of Education curriculum grant"
                )
            )
            dao.insertAllApprovals(approvals)

            val messages = listOf(
                MessageLog(
                    id = 1,
                    targetType = "ALL_PARENTS",
                    targetDetail = "All Primary & JHS Parents",
                    channel = "WHATSAPP",
                    messageText = "Dear Parents/Guardians, Akoma Academy Term 3 PTA Meeting is scheduled for Saturday at 10:00 AM in the Assembly Hall.",
                    recipientCount = 380,
                    estimatedCostGhc = 0.00,
                    timestampString = "18 Jul 2026, 09:30",
                    senderName = "Proprietor Office"
                ),
                MessageLog(
                    id = 2,
                    targetType = "CLASS_SPECIFIC",
                    targetDetail = "JHS 2 - Gold Class",
                    channel = "SMS",
                    messageText = "Notice: JHS 2 Science & ICT practical mock exams begin this Monday. Please ensure students come with geometry sets.",
                    recipientCount = 42,
                    estimatedCostGhc = 2.52,
                    timestampString = "16 Jul 2026, 14:15",
                    senderName = "Mr. Kojo Mensah"
                )
            )
            for (msg in messages) {
                dao.insertMessage(msg)
            }

            val attendanceList = listOf(
                AttendanceRecord(
                    id = 1,
                    studentId = 101,
                    studentName = "Kofi Addo",
                    className = "JHS 2 - Gold",
                    mondayStatus = "PRESENT",
                    tuesdayStatus = "PRESENT",
                    wednesdayStatus = "ABSENT",
                    thursdayStatus = "PRESENT",
                    fridayStatus = "PRESENT"
                ),
                AttendanceRecord(
                    id = 2,
                    studentId = 102,
                    studentName = "Ama Serwaa Mensah",
                    className = "JHS 2 - Gold",
                    mondayStatus = "PRESENT",
                    tuesdayStatus = "PRESENT",
                    wednesdayStatus = "PRESENT",
                    thursdayStatus = "PRESENT",
                    fridayStatus = "PRESENT"
                ),
                AttendanceRecord(
                    id = 3,
                    studentId = 103,
                    studentName = "Kwaku Baah",
                    className = "JHS 2 - Gold",
                    mondayStatus = "ABSENT",
                    tuesdayStatus = "ABSENT",
                    wednesdayStatus = "PRESENT",
                    thursdayStatus = "ABSENT",
                    fridayStatus = "PRESENT"
                ),
                AttendanceRecord(
                    id = 4,
                    studentId = 104,
                    studentName = "Yaa Asantewaa Osei",
                    className = "JHS 2 - Gold",
                    mondayStatus = "PRESENT",
                    tuesdayStatus = "PRESENT",
                    wednesdayStatus = "PRESENT",
                    thursdayStatus = "PRESENT",
                    fridayStatus = "LATE"
                ),
                AttendanceRecord(
                    id = 5,
                    studentId = 105,
                    studentName = "Kwame Nkrumah Baffour",
                    className = "JHS 2 - Gold",
                    mondayStatus = "PRESENT",
                    tuesdayStatus = "ABSENT",
                    wednesdayStatus = "ABSENT",
                    thursdayStatus = "ABSENT",
                    fridayStatus = "ABSENT"
                ),
                AttendanceRecord(
                    id = 6,
                    studentId = 106,
                    studentName = "Abena Kyeremaa",
                    className = "JHS 2 - Gold",
                    mondayStatus = "PRESENT",
                    tuesdayStatus = "PRESENT",
                    wednesdayStatus = "PRESENT",
                    thursdayStatus = "PRESENT",
                    fridayStatus = "PRESENT"
                ),
                AttendanceRecord(
                    id = 7,
                    studentId = 107,
                    studentName = "Emmanuel Tetteh",
                    className = "JHS 2 - Gold",
                    mondayStatus = "PRESENT",
                    tuesdayStatus = "PRESENT",
                    wednesdayStatus = "PRESENT",
                    thursdayStatus = "ABSENT",
                    fridayStatus = "PRESENT"
                )
            )
            dao.insertAllAttendance(attendanceList)

            val studentLedgers = listOf(
                StudentLedger(
                    studentId = 102,
                    studentName = "Ama Serwaa Mensah",
                    indexNumber = "AK2026-089",
                    className = "JHS 2 - Gold",
                    guardianName = "Mrs. Grace Mensah",
                    guardianPhone = "0244123456",
                    termTuitionGhc = 1200.0,
                    ptaLevyGhc = 150.0,
                    ictLabFeeGhc = 200.0,
                    feedingFeeGhc = 350.0,
                    totalFeesGhc = 1900.0,
                    paidFeesGhc = 1200.0,
                    balanceGhc = 700.0
                ),
                StudentLedger(
                    studentId = 201,
                    studentName = "Kojo Mensah Jr.",
                    indexNumber = "AK2026-142",
                    className = "Primary 4 - Harmony",
                    guardianName = "Mrs. Grace Mensah",
                    guardianPhone = "0244123456",
                    termTuitionGhc = 950.0,
                    ptaLevyGhc = 150.0,
                    ictLabFeeGhc = 0.0,
                    feedingFeeGhc = 300.0,
                    totalFeesGhc = 1400.0,
                    paidFeesGhc = 1400.0,
                    balanceGhc = 0.0
                )
            )
            dao.insertAllStudentLedgers(studentLedgers)

            val transactions = listOf(
                FeeTransaction(
                    id = 1,
                    studentId = 102,
                    transactionRef = "MOMO-88124910",
                    amountGhc = 800.0,
                    paymentMethod = "MTN MoMo",
                    channelNumber = "0244123456",
                    description = "Term 3 Tuition Deposit",
                    dateString = "10 Jul 2026, 11:20",
                    status = "SUCCESS"
                ),
                FeeTransaction(
                    id = 2,
                    studentId = 102,
                    transactionRef = "MOMO-44210982",
                    amountGhc = 400.0,
                    paymentMethod = "MTN MoMo",
                    channelNumber = "0244123456",
                    description = "PTA Levy & Feeding Fee Payment",
                    dateString = "15 Jun 2026, 09:45",
                    status = "SUCCESS"
                ),
                FeeTransaction(
                    id = 3,
                    studentId = 201,
                    transactionRef = "MOMO-10928374",
                    amountGhc = 1400.0,
                    paymentMethod = "Telecel Cash",
                    channelNumber = "0208991122",
                    description = "Primary 4 Full Term Fees",
                    dateString = "02 Jun 2026, 08:30",
                    status = "SUCCESS"
                )
            )
            dao.insertAllFeeTransactions(transactions)

            // Initial School Settings
            dao.saveSchoolSettings(SchoolSettings(id = 1, schoolName = "St. Talafor Academy", campusLocation = "Sibi, Oti Region, Ghana"))

            // Initial Accounts for instant demo setup
            val accounts = listOf(
                UserAccount(
                    fullName = "Dr. Kwabena Mensah",
                    email = "proprietor@sttalafor.edu.gh",
                    phone = "0244987654",
                    role = "PROPRIETOR",
                    schoolName = "St. Talafor Academy",
                    isLoggedIn = true
                ),
                UserAccount(
                    fullName = "Mr. Kojo Mensah",
                    email = "kojo.mensah@sttalafor.edu.gh",
                    phone = "0208112233",
                    role = "TEACHER",
                    schoolName = "St. Talafor Academy",
                    isLoggedIn = false
                ),
                UserAccount(
                    fullName = "Mrs. Grace Mensah",
                    email = "grace.mensah@gmail.com",
                    phone = "0244123456",
                    role = "GUARDIAN",
                    schoolName = "St. Talafor Academy",
                    isLoggedIn = false
                )
            )
            for (acc in accounts) {
                dao.insertUserAccount(acc)
            }

            // Initial Notifications
            val initialNotifications = listOf(
                AppNotification(
                    recipientRole = "PROPRIETOR",
                    type = "APPROVAL_REQUIRED",
                    title = "Transaction Approval Needed",
                    message = "Mr. Kojo Mensah submitted a staff loan request for GH₵ 1,200.00.",
                    timestampString = "21 Jul 2026, 14:10",
                    isRead = false
                ),
                AppNotification(
                    recipientRole = "PROPRIETOR",
                    type = "PAYMENT_SUCCESS",
                    title = "New Fee Payment Received",
                    message = "Received GH₵ 800.00 via MTN MoMo for Ama Serwaa Mensah.",
                    timestampString = "20 Jul 2026, 11:25",
                    isRead = true
                ),
                AppNotification(
                    recipientRole = "TEACHER",
                    type = "ATTENDANCE_ISSUE",
                    title = "New Attendance Issue Alert",
                    message = "Kwame Nkrumah Baffour (JHS 2 Gold) marked absent 4 days this week.",
                    timestampString = "21 Jul 2026, 09:15",
                    isRead = false
                ),
                AppNotification(
                    recipientRole = "TEACHER",
                    type = "BROADCAST",
                    title = "Staff Notice: Lesson Notes Submission",
                    message = "All JHS class teachers must upload Term 3 lesson notes by Friday.",
                    timestampString = "19 Jul 2026, 16:00",
                    isRead = true
                ),
                AppNotification(
                    recipientRole = "GUARDIAN",
                    type = "FEE_DUE",
                    title = "Fee Payment Due Reminder",
                    message = "Outstanding balance of GH₵ 700.00 due for Ama Serwaa Mensah (Term 3).",
                    timestampString = "21 Jul 2026, 08:00",
                    isRead = false
                ),
                AppNotification(
                    recipientRole = "GUARDIAN",
                    type = "BROADCAST",
                    title = "Term 3 PTA Assembly Notice",
                    message = "Akoma Academy Term 3 PTA Meeting is scheduled for Saturday at 10:00 AM.",
                    timestampString = "18 Jul 2026, 10:00",
                    isRead = true
                )
            )
            dao.insertAllNotifications(initialNotifications)

            // Seed initial daily student attendance
            val initialDailyAttendance = listOf(
                DailyStudentAttendance(
                    studentId = 102,
                    studentName = "Ama Serwaa Mensah",
                    className = "JHS 2 - Gold",
                    dateString = "2026-07-25",
                    status = "PRESENT",
                    isPresent = true,
                    isAbsent = false,
                    isExcused = false,
                    remarks = "Arrived at 07:35 AM. Present for Morning Assembly."
                ),
                DailyStudentAttendance(
                    studentId = 102,
                    studentName = "Ama Serwaa Mensah",
                    className = "JHS 2 - Gold",
                    dateString = "2026-07-24",
                    status = "PRESENT",
                    isPresent = true,
                    isAbsent = false,
                    isExcused = false,
                    remarks = "Full day attendance recorded."
                ),
                DailyStudentAttendance(
                    studentId = 102,
                    studentName = "Ama Serwaa Mensah",
                    className = "JHS 2 - Gold",
                    dateString = "2026-07-23",
                    status = "EXCUSED",
                    isPresent = false,
                    isAbsent = false,
                    isExcused = true,
                    remarks = "Excused absence - Dental clinic appointment."
                ),
                DailyStudentAttendance(
                    studentId = 102,
                    studentName = "Ama Serwaa Mensah",
                    className = "JHS 2 - Gold",
                    dateString = "2026-07-22",
                    status = "PRESENT",
                    isPresent = true,
                    isAbsent = false,
                    isExcused = false,
                    remarks = "Arrived on time for morning devotion."
                ),
                DailyStudentAttendance(
                    studentId = 102,
                    studentName = "Ama Serwaa Mensah",
                    className = "JHS 2 - Gold",
                    dateString = "2026-07-21",
                    status = "PRESENT",
                    isPresent = true,
                    isAbsent = false,
                    isExcused = false,
                    remarks = "Present."
                ),
                DailyStudentAttendance(
                    studentId = 101,
                    studentName = "Kofi Mensah",
                    className = "JHS 2 - Gold",
                    dateString = "2026-07-25",
                    status = "PRESENT",
                    isPresent = true,
                    isAbsent = false,
                    isExcused = false,
                    remarks = "Present on time."
                ),
                DailyStudentAttendance(
                    studentId = 101,
                    studentName = "Kofi Mensah",
                    className = "JHS 2 - Gold",
                    dateString = "2026-07-24",
                    status = "PRESENT",
                    isPresent = true,
                    isAbsent = false,
                    isExcused = false,
                    remarks = "Present."
                ),
                DailyStudentAttendance(
                    studentId = 103,
                    studentName = "Kwame Nkrumah Baffour",
                    className = "JHS 2 - Gold",
                    dateString = "2026-07-25",
                    status = "PRESENT",
                    isPresent = true,
                    isAbsent = false,
                    isExcused = false,
                    remarks = "Present."
                ),
                DailyStudentAttendance(
                    studentId = 103,
                    studentName = "Kwame Nkrumah Baffour",
                    className = "JHS 2 - Gold",
                    dateString = "2026-07-24",
                    status = "EXCUSED",
                    isPresent = false,
                    isAbsent = false,
                    isExcused = true,
                    remarks = "Excused absence - Family travel notice submitted."
                )
            )
            if (attendanceDao != null) {
                attendanceDao.insertAllAttendance(initialDailyAttendance)
            } else {
                dao.insertAllDailyAttendance(initialDailyAttendance)
            }

            // Seed Initial Guardian Profiles
            val initialGuardians = listOf(
                GuardianProfile(
                    fullName = "Abena Serwaa Mensah",
                    phoneNumber = "0244123456",
                    email = "abena.serwaa@example.com",
                    relationship = "Mother",
                    address = "Plot 14 Block B, Adum, Kumasi",
                    occupation = "Trader / Entrepreneur",
                    emergencyContactPhone = "0208123456",
                    linkedStudentIds = "101,102",
                    linkedStudentNames = "Ama Serwaa Mensah, Kwame Nkrumah Baffour"
                ),
                GuardianProfile(
                    fullName = "Kwadwo Baffour Senior",
                    phoneNumber = "0208987654",
                    email = "kwadwo.baffour@example.com",
                    relationship = "Father",
                    address = "House 89, Santasi, Kumasi",
                    occupation = "Civil Engineer",
                    emergencyContactPhone = "0244987654",
                    linkedStudentIds = "103",
                    linkedStudentNames = "Kofi Owusu-Ansa"
                ),
                GuardianProfile(
                    fullName = "Akosua Appiah",
                    phoneNumber = "0551239876",
                    email = "akosua.appiah@example.com",
                    relationship = "Guardian",
                    address = "Asokwa Residential Area, Kumasi",
                    occupation = "Teacher",
                    emergencyContactPhone = "0501239876",
                    linkedStudentIds = "104",
                    linkedStudentNames = "Yaa Asantewaa Appiah"
                )
            )
            guardianDao?.insertAllGuardians(initialGuardians)

            // Seed Initial Student Academic Grades across Primary & JHS
            val initialGrades = listOf(
                // Ama Serwaa Mensah (Student ID: 101) - JHS 2 - Gold
                StudentGrade(studentId = 101, studentName = "Ama Serwaa Mensah", className = "JHS 2 - Gold", subject = "Mathematics", academicTerm = "Term 1", academicYear = "2025/2026", classScore = 28.5, examScore = 64.0, totalScore = 92.5, gradeLetter = "A1", remarks = "Exceptional mastery in algebra and geometry."),
                StudentGrade(studentId = 101, studentName = "Ama Serwaa Mensah", className = "JHS 2 - Gold", subject = "Integrated Science", academicTerm = "Term 1", academicYear = "2025/2026", classScore = 26.0, examScore = 60.5, totalScore = 86.5, gradeLetter = "A1", remarks = "Strong understanding of scientific methods."),
                StudentGrade(studentId = 101, studentName = "Ama Serwaa Mensah", className = "JHS 2 - Gold", subject = "English Language", academicTerm = "Term 1", academicYear = "2025/2026", classScore = 27.0, examScore = 61.0, totalScore = 88.0, gradeLetter = "A1", remarks = "Fluent expression and great essay writing."),
                StudentGrade(studentId = 101, studentName = "Ama Serwaa Mensah", className = "JHS 2 - Gold", subject = "Mathematics", academicTerm = "Term 2", academicYear = "2025/2026", classScore = 29.0, examScore = 65.0, totalScore = 94.0, gradeLetter = "A1", remarks = "Top score in term examination."),
                StudentGrade(studentId = 101, studentName = "Ama Serwaa Mensah", className = "JHS 2 - Gold", subject = "Integrated Science", academicTerm = "Term 2", academicYear = "2025/2026", classScore = 27.5, examScore = 62.5, totalScore = 90.0, gradeLetter = "A1", remarks = "Excellent lab experiment writeups."),
                StudentGrade(studentId = 101, studentName = "Ama Serwaa Mensah", className = "JHS 2 - Gold", subject = "English Language", academicTerm = "Term 2", academicYear = "2025/2026", classScore = 26.5, examScore = 58.5, totalScore = 85.0, gradeLetter = "A1", remarks = "Consistent high performance."),
                StudentGrade(studentId = 101, studentName = "Ama Serwaa Mensah", className = "JHS 2 - Gold", subject = "Mathematics", academicTerm = "Term 3", academicYear = "2025/2026", classScore = 28.0, examScore = 63.0, totalScore = 91.0, gradeLetter = "A1", remarks = "Outstanding cumulative year results."),

                // Kwame Nkrumah Baffour (Student ID: 102) - JHS 2 - Gold
                StudentGrade(studentId = 102, studentName = "Kwame Nkrumah Baffour", className = "JHS 2 - Gold", subject = "Mathematics", academicTerm = "Term 1", academicYear = "2025/2026", classScore = 22.0, examScore = 52.0, totalScore = 74.0, gradeLetter = "B3", remarks = "Good performance, continue practicing problem solving."),
                StudentGrade(studentId = 102, studentName = "Kwame Nkrumah Baffour", className = "JHS 2 - Gold", subject = "Integrated Science", academicTerm = "Term 1", academicYear = "2025/2026", classScore = 24.0, examScore = 54.0, totalScore = 78.0, gradeLetter = "B2", remarks = "Solid grasp of core science concepts."),
                StudentGrade(studentId = 102, studentName = "Kwame Nkrumah Baffour", className = "JHS 2 - Gold", subject = "English Language", academicTerm = "Term 1", academicYear = "2025/2026", classScore = 25.0, examScore = 57.0, totalScore = 82.0, gradeLetter = "A1", remarks = "Very impressive grammar and vocabulary."),
                StudentGrade(studentId = 102, studentName = "Kwame Nkrumah Baffour", className = "JHS 2 - Gold", subject = "Mathematics", academicTerm = "Term 2", academicYear = "2025/2026", classScore = 24.5, examScore = 54.5, totalScore = 79.0, gradeLetter = "B2", remarks = "Steadily improving in problem solving."),
                StudentGrade(studentId = 102, studentName = "Kwame Nkrumah Baffour", className = "JHS 2 - Gold", subject = "Integrated Science", academicTerm = "Term 2", academicYear = "2025/2026", classScore = 25.5, examScore = 56.0, totalScore = 81.5, gradeLetter = "A1", remarks = "Great improvement in practical science."),

                // Kofi Addo (Student ID: 103) - JHS 2 - Gold
                StudentGrade(studentId = 103, studentName = "Kofi Addo", className = "JHS 2 - Gold", subject = "Mathematics", academicTerm = "Term 1", academicYear = "2025/2026", classScore = 20.0, examScore = 48.0, totalScore = 68.0, gradeLetter = "C4", remarks = "Pass with credit, extra revision advised."),
                StudentGrade(studentId = 103, studentName = "Kofi Addo", className = "JHS 2 - Gold", subject = "English Language", academicTerm = "Term 1", academicYear = "2025/2026", classScore = 22.0, examScore = 50.0, totalScore = 72.0, gradeLetter = "B3", remarks = "Good effort in term paper."),
                StudentGrade(studentId = 103, studentName = "Kofi Addo", className = "JHS 2 - Gold", subject = "Mathematics", academicTerm = "Term 2", academicYear = "2025/2026", classScore = 23.0, examScore = 51.0, totalScore = 74.0, gradeLetter = "B3", remarks = "Improved from Term 1."),

                // Yaa Asantewaa Osei (Student ID: 104) - JHS 2 - Gold
                StudentGrade(studentId = 104, studentName = "Yaa Asantewaa Osei", className = "JHS 2 - Gold", subject = "Mathematics", academicTerm = "Term 1", academicYear = "2025/2026", classScore = 27.0, examScore = 61.0, totalScore = 88.0, gradeLetter = "A1", remarks = "Excellent logical reasoning."),
                StudentGrade(studentId = 104, studentName = "Yaa Asantewaa Osei", className = "JHS 2 - Gold", subject = "Integrated Science", academicTerm = "Term 1", academicYear = "2025/2026", classScore = 28.0, examScore = 63.5, totalScore = 91.5, gradeLetter = "A1", remarks = "Outstanding performance."),
                StudentGrade(studentId = 104, studentName = "Yaa Asantewaa Osei", className = "JHS 2 - Gold", subject = "Mathematics", academicTerm = "Term 2", academicYear = "2025/2026", classScore = 26.0, examScore = 60.0, totalScore = 86.0, gradeLetter = "A1", remarks = "Maintained top grade."),

                // Kofi Owusu-Ansa (Student ID: 105) - Primary 4
                StudentGrade(studentId = 105, studentName = "Kofi Owusu-Ansa", className = "Primary 4", subject = "English Language", academicTerm = "Term 1", academicYear = "2025/2026", classScore = 25.0, examScore = 58.0, totalScore = 83.0, gradeLetter = "A1", remarks = "Excellent reading comprehension and grammar skills."),
                StudentGrade(studentId = 105, studentName = "Kofi Owusu-Ansa", className = "Primary 4", subject = "Mathematics", academicTerm = "Term 1", academicYear = "2025/2026", classScore = 24.0, examScore = 55.0, totalScore = 79.0, gradeLetter = "B2", remarks = "Strong arithmetic skills.")
            )
            gradeDao?.insertAllGrades(initialGrades)

            // Seed Initial Teacher Daily Lesson Plans
            val initialLessonPlans = listOf(
                LessonPlan(
                    teacherId = 1,
                    teacherName = "Mr. Kojo Mensah",
                    className = "JHS 2 - Gold",
                    subject = "Mathematics",
                    topic = "Algebraic Expressions & Factorization",
                    subTopic = "Simplifying Polynomials",
                    lessonDate = "2026-07-28",
                    durationMinutes = 60,
                    objectives = "Students will be able to expand binomials and factor quadratic expressions using grid methods.",
                    teachingMaterials = "Graph Board, Factoring Algebra Tiles, Worksheets",
                    procedureSteps = "1. Starter: Quick mental math review on prime factors (10 mins)\n2. Concept Introduction: Expanding (x+a)(x+b) using grid models (20 mins)\n3. Guided Practice: Solving board exercises in pairs (15 mins)\n4. Plenary & Assessment: Exit ticket exercise (15 mins)",
                    status = "APPROVED",
                    managementFeedback = "Excellent structured breakdown and active group exercise. Approved by Proprietor."
                ),
                LessonPlan(
                    teacherId = 1,
                    teacherName = "Mr. Kojo Mensah",
                    className = "JHS 2 - Gold",
                    subject = "Integrated Science",
                    topic = "Photosynthesis & Plant Transport Systems",
                    subTopic = "Role of Xylem & Phloem",
                    lessonDate = "2026-07-29",
                    durationMinutes = 60,
                    objectives = "Demonstrate water movement in plants using dyed celery stalks under magnification.",
                    teachingMaterials = "Celery stalks, Food coloring, Beakers, Scalpels",
                    procedureSteps = "1. Introduction to plant vascular systems (10 mins)\n2. Set up celery dye experiment (20 mins)\n3. Cross-section microscopic observation (20 mins)\n4. Conclusion and clean up (10 mins)",
                    status = "PENDING_REVIEW",
                    managementFeedback = ""
                ),
                LessonPlan(
                    teacherId = 1,
                    teacherName = "Mr. Kojo Mensah",
                    className = "Primary 6",
                    subject = "ICT",
                    topic = "Introduction to Computer Algorithms",
                    subTopic = "Flowcharts and Pseudocode",
                    lessonDate = "2026-07-30",
                    durationMinutes = 45,
                    objectives = "Students will draw simple decision flowcharts for daily activities.",
                    teachingMaterials = "ICT Lab Projector, Chart Papers, Markers",
                    procedureSteps = "1. Video presentation on algorithms in daily life (10 mins)\n2. Teacher demo: Drawing decision symbols (15 mins)\n3. Student exercise on chart paper (20 mins)",
                    status = "REVISION_REQUESTED",
                    managementFeedback = "Please include specific safety rules for computer lab work in procedure."
                )
            )
            lessonPlanDao?.insertAllLessonPlans(initialLessonPlans)

            // Seed Initial Single-Role Portal User Accounts
            val initialPortalAccounts = listOf(
                UserPortalAccount(
                    fullName = "Nana Yaw Otchere",
                    email = "proprietor@sttalafor.edu.gh",
                    phone = "0244001122",
                    role = "PROPRIETOR",
                    schoolName = "St. Talafor Academy",
                    isLoggedIn = true,
                    isProprietor = true,
                    portalAccessRole = "PROPRIETOR"
                ),
                UserPortalAccount(
                    fullName = "Mr. Emmanuel Mensah",
                    email = "mensah@sttalafor.edu.gh",
                    phone = "0244112233",
                    role = "TEACHER",
                    schoolName = "St. Talafor Academy",
                    isLoggedIn = false,
                    isProprietor = false,
                    portalAccessRole = "TEACHER"
                ),
                UserPortalAccount(
                    fullName = "Abena Serwaa Mensah",
                    email = "serwaa@example.com",
                    phone = "0244123456",
                    role = "GUARDIAN",
                    schoolName = "St. Talafor Academy",
                    isLoggedIn = false,
                    isProprietor = false,
                    portalAccessRole = "GUARDIAN"
                )
            )
            portalUserDao?.insertAllAccounts(initialPortalAccounts)

            // Seed Initial School Events
            val initialEvents = listOf(
                SchoolEvent(
                    title = "Parent-Teacher Association (PTA) General Meeting",
                    dateString = "2026-08-08",
                    timeString = "10:00 AM",
                    description = "Term 1 general assembly for all guardians and teaching staff to discuss infrastructure projects and academic performance.",
                    targetAudience = "PARENTS",
                    location = "School Main Auditorium",
                    organizer = "PTA Executive Committee",
                    isImportant = true
                ),
                SchoolEvent(
                    title = "Annual Science & ICT Exhibition",
                    dateString = "2026-08-15",
                    timeString = "08:30 AM",
                    description = "Student project showcase featuring robotics, renewable energy models, and coding demonstrations by JHS 1-3 students.",
                    targetAudience = "ALL",
                    location = "Science & Tech Complex",
                    organizer = "Department of Sciences",
                    isImportant = true
                ),
                SchoolEvent(
                    title = "Staff Pedagogical Workshop",
                    dateString = "2026-08-20",
                    timeString = "01:00 PM",
                    description = "In-service continuous professional development workshop on modern continuous assessment strategies.",
                    targetAudience = "TEACHERS",
                    location = "Staff Conference Room",
                    organizer = "Headmaster's Office",
                    isImportant = false
                ),
                SchoolEvent(
                    title = "Inter-House Sports & Athletic Competition",
                    dateString = "2026-08-28",
                    timeString = "08:00 AM",
                    description = "Annual track and field championship featuring football, netball, relay races, and field events across all houses.",
                    targetAudience = "STUDENTS",
                    location = "Akoma Sports Complex",
                    organizer = "Sports & Physical Education Dept",
                    isImportant = false
                )
            )
            schoolEventDao?.insertAllEvents(initialEvents)

            // Seed Initial Weekly Class Timetable Slots
            val initialTimetableSlots = listOf(
                ClassTimetable(
                    className = "JHS 2 - Gold",
                    dayOfWeek = "Monday",
                    periodNumber = 1,
                    startTime = "08:00 AM",
                    endTime = "08:45 AM",
                    subject = "Mathematics",
                    teacherName = "Mr. Emmanuel Mensah",
                    classroom = "Block J2-A"
                ),
                ClassTimetable(
                    className = "JHS 2 - Gold",
                    dayOfWeek = "Monday",
                    periodNumber = 2,
                    startTime = "08:45 AM",
                    endTime = "09:30 AM",
                    subject = "Integrated Science",
                    teacherName = "Mrs. Grace Appiah",
                    classroom = "Lab 1"
                ),
                ClassTimetable(
                    className = "JHS 2 - Gold",
                    dayOfWeek = "Tuesday",
                    periodNumber = 1,
                    startTime = "08:00 AM",
                    endTime = "08:45 AM",
                    subject = "English Language",
                    teacherName = "Mr. Francis Kwarteng",
                    classroom = "Block J2-A"
                ),
                ClassTimetable(
                    className = "JHS 2 - Gold",
                    dayOfWeek = "Tuesday",
                    periodNumber = 2,
                    startTime = "08:45 AM",
                    endTime = "09:30 AM",
                    subject = "ICT",
                    teacherName = "Ms. Janet Osei",
                    classroom = "Computer Lab"
                ),
                ClassTimetable(
                    className = "Primary 4",
                    dayOfWeek = "Monday",
                    periodNumber = 1,
                    startTime = "08:00 AM",
                    endTime = "08:45 AM",
                    subject = "English Language",
                    teacherName = "Mr. Francis Kwarteng",
                    classroom = "Room P4"
                ),
                ClassTimetable(
                    className = "Primary 4",
                    dayOfWeek = "Monday",
                    periodNumber = 2,
                    startTime = "08:45 AM",
                    endTime = "09:30 AM",
                    subject = "Mathematics",
                    teacherName = "Mr. Emmanuel Mensah",
                    classroom = "Room P4"
                )
            )
            timetableDao?.insertAllTimetables(initialTimetableSlots)

            // Seed Initial Student Profiles
            val initialStudentProfiles = listOf(
                StudentProfile(
                    fullName = "Kofi Mensah",
                    indexNumber = "AKM/2025/082",
                    className = "JHS 2 - Gold",
                    dateOfBirth = "2012-05-14",
                    gender = "Male",
                    guardianName = "Madam Serwaa Mensah",
                    guardianPhone = "0244111222",
                    guardianEmail = "parent.serwaa@gmail.com",
                    residentialAddress = "Asokwa, Kumasi",
                    enrollmentStatus = "ACTIVE"
                ),
                StudentProfile(
                    fullName = "Ama Osei",
                    indexNumber = "AKM/2025/083",
                    className = "JHS 2 - Gold",
                    dateOfBirth = "2012-08-22",
                    gender = "Female",
                    guardianName = "Mr. Yaw Osei",
                    guardianPhone = "0244333444",
                    guardianEmail = "yaw.osei@yahoo.com",
                    residentialAddress = "Nhyiaeso, Kumasi",
                    enrollmentStatus = "ACTIVE"
                ),
                StudentProfile(
                    fullName = "Kwame Appiah",
                    indexNumber = "AKM/2025/084",
                    className = "Primary 4",
                    dateOfBirth = "2014-03-10",
                    gender = "Male",
                    guardianName = "Mrs. Abena Appiah",
                    guardianPhone = "0244555666",
                    guardianEmail = "abena.appiah@gmail.com",
                    residentialAddress = "Bantama, Kumasi",
                    enrollmentStatus = "ACTIVE"
                )
            )
            dao.insertAllStudentProfiles(initialStudentProfiles)
        }
    }
}
