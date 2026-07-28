package com.example.data.dao

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SchoolDao {

    // --- Staff & Roles ---
    @Query("SELECT * FROM staff_members ORDER BY id ASC")
    fun getAllStaff(): Flow<List<StaffMember>>

    @Delete
    suspend fun deleteStaffMember(staff: StaffMember)

    @Query("DELETE FROM staff_members WHERE id = :staffId")
    suspend fun deleteStaffMemberById(staffId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStaff(staff: StaffMember)

    @Update
    suspend fun updateStaff(staff: StaffMember)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllStaff(staffList: List<StaffMember>)

    // --- Transaction Approvals ---
    @Query("SELECT * FROM transaction_approvals ORDER BY id DESC")
    fun getAllApprovals(): Flow<List<TransactionApproval>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApproval(approval: TransactionApproval)

    @Update
    suspend fun updateApproval(approval: TransactionApproval)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllApprovals(approvals: List<TransactionApproval>)

    // --- Messaging ---
    @Query("SELECT * FROM message_logs ORDER BY id DESC")
    fun getAllMessages(): Flow<List<MessageLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageLog)

    // --- Attendance ---
    @Query("SELECT * FROM attendance_records WHERE className = :className ORDER BY studentName ASC")
    fun getAttendanceForClass(className: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records ORDER BY className, studentName ASC")
    fun getAllAttendance(): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE isSynced = 0")
    fun getUnsyncedAttendance(): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE isSynced = 0")
    suspend fun getUnsyncedAttendanceDirect(): List<AttendanceRecord>

    @Query("UPDATE attendance_records SET isSynced = 1 WHERE isSynced = 0")
    suspend fun markAllAttendanceSynced()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: AttendanceRecord)

    @Update
    suspend fun updateAttendance(attendance: AttendanceRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllAttendance(records: List<AttendanceRecord>)

    // --- Daily Student Attendance ---
    @Query("SELECT * FROM daily_student_attendance ORDER BY dateString DESC, studentName ASC")
    fun getAllDailyAttendance(): Flow<List<DailyStudentAttendance>>

    @Query("SELECT * FROM daily_student_attendance WHERE dateString = :date ORDER BY studentName ASC")
    fun getDailyAttendanceByDate(date: String): Flow<List<DailyStudentAttendance>>

    @Query("SELECT * FROM daily_student_attendance WHERE studentId = :studentId ORDER BY dateString DESC")
    fun getDailyAttendanceForStudent(studentId: Long): Flow<List<DailyStudentAttendance>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyAttendance(attendance: DailyStudentAttendance): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllDailyAttendance(records: List<DailyStudentAttendance>)

    @Update
    suspend fun updateDailyAttendance(attendance: DailyStudentAttendance)

    // --- Clock-In Logs ---
    @Query("SELECT * FROM clock_in_logs ORDER BY id DESC")
    fun getAllClockInLogs(): Flow<List<ClockInLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClockIn(log: ClockInLog)

    // --- Student Profiles ---
    @Query("DELETE FROM student_profiles WHERE id = :studentId")
    suspend fun deleteStudentProfileById(studentId: Long)

    @Query("DELETE FROM attendance_records WHERE studentId = :studentId")
    suspend fun deleteAttendanceByStudentId(studentId: Long)

    @Query("SELECT * FROM student_profiles ORDER BY className ASC, fullName ASC")
    fun getAllStudentProfiles(): Flow<List<StudentProfile>>

    @Query("SELECT * FROM student_profiles WHERE id = :id LIMIT 1")
    fun getStudentProfileById(id: Long): Flow<StudentProfile?>

    @Query("SELECT * FROM student_profiles WHERE className = :className ORDER BY fullName ASC")
    fun getStudentProfilesByClass(className: String): Flow<List<StudentProfile>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudentProfile(student: StudentProfile): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllStudentProfiles(students: List<StudentProfile>)

    @Update
    suspend fun updateStudentProfile(student: StudentProfile)

    @Delete
    suspend fun deleteStudentProfile(student: StudentProfile)

    // --- Student Ledgers & Fee Transactions ---
    @Query("SELECT * FROM student_ledgers ORDER BY studentName ASC")
    fun getAllStudentLedgers(): Flow<List<StudentLedger>>

    @Query("SELECT * FROM student_ledgers WHERE studentId = :studentId")
    fun getStudentLedgerById(studentId: Long): Flow<StudentLedger?>

    @Query("DELETE FROM student_ledgers WHERE studentId = :studentId")
    suspend fun deleteStudentLedgerById(studentId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudentLedger(ledger: StudentLedger)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllStudentLedgers(ledgers: List<StudentLedger>)

    @Update
    suspend fun updateStudentLedger(ledger: StudentLedger)

    @Query("SELECT * FROM fee_transactions ORDER BY id DESC")
    fun getAllFeeTransactions(): Flow<List<FeeTransaction>>

    @Query("SELECT * FROM fee_transactions WHERE studentId = :studentId ORDER BY id DESC")
    fun getTransactionsForStudent(studentId: Long): Flow<List<FeeTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeeTransaction(transaction: FeeTransaction)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllFeeTransactions(transactions: List<FeeTransaction>)

    // --- Student Fee Payments ---
    @Query("SELECT * FROM student_fee_payments ORDER BY id DESC")
    fun getAllFeePayments(): Flow<List<StudentFeePayment>>

    @Query("SELECT * FROM student_fee_payments WHERE studentId = :studentId ORDER BY id DESC")
    fun getFeePaymentsForStudent(studentId: Long): Flow<List<StudentFeePayment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeePayment(payment: StudentFeePayment)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllFeePayments(payments: List<StudentFeePayment>)

    // --- User Accounts (Authentication & Role System) ---
    @Query("SELECT * FROM user_accounts ORDER BY id ASC")
    fun getAllUserAccounts(): Flow<List<UserAccount>>

    @Query("SELECT * FROM user_accounts WHERE isApproved = 0 ORDER BY id DESC")
    fun getPendingUserAccounts(): Flow<List<UserAccount>>

    @Query("SELECT * FROM user_accounts WHERE isLoggedIn = 1 LIMIT 1")
    fun getActiveUserAccount(): Flow<UserAccount?>

    @Query("SELECT * FROM user_accounts WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserAccount?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserAccount(user: UserAccount): Long

    @Update
    suspend fun updateUserAccount(user: UserAccount)

    @Query("UPDATE user_accounts SET isApproved = :isApproved WHERE id = :userId")
    suspend fun updateUserApproval(userId: Long, isApproved: Boolean)

    @Query("DELETE FROM user_accounts WHERE id = :userId")
    suspend fun deleteUserAccountById(userId: Long)

    @Query("UPDATE user_accounts SET isLoggedIn = 0")
    suspend fun clearLoggedInUsers()

    // --- Notifications ---
    @Query("SELECT * FROM app_notifications WHERE recipientRole = :role OR recipientRole = 'ALL' ORDER BY id DESC")
    fun getNotificationsForRole(role: String): Flow<List<AppNotification>>

    @Query("SELECT * FROM app_notifications ORDER BY id DESC")
    fun getAllNotifications(): Flow<List<AppNotification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: AppNotification)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllNotifications(notifications: List<AppNotification>)

    @Query("UPDATE app_notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationRead(id: Long)

    @Query("UPDATE app_notifications SET isRead = 1 WHERE recipientRole = :role OR recipientRole = 'ALL'")
    suspend fun markAllNotificationsReadForRole(role: String)

    @Query("DELETE FROM app_notifications WHERE recipientRole = :role OR recipientRole = 'ALL'")
    suspend fun clearNotificationsForRole(role: String)

    // --- Student Add Requests (Teacher -> Proprietor Approval) ---
    @Query("SELECT * FROM student_add_requests ORDER BY id DESC")
    fun getAllStudentAddRequests(): Flow<List<StudentAddRequest>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudentAddRequest(request: StudentAddRequest): Long

    @Update
    suspend fun updateStudentAddRequest(request: StudentAddRequest)

    // --- Direct Messages (Guardian-Teacher Communication) ---
    @Query("SELECT * FROM direct_messages ORDER BY id DESC")
    fun getAllDirectMessages(): Flow<List<DirectMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDirectMessage(message: DirectMessage): Long

    @Query("UPDATE direct_messages SET isRead = 1 WHERE id = :id")
    suspend fun markDirectMessageAsRead(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllDirectMessages(messages: List<DirectMessage>)

    // --- School Settings ---
    @Query("SELECT * FROM school_settings WHERE id = 1 LIMIT 1")
    fun getSchoolSettings(): Flow<SchoolSettings?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSchoolSettings(settings: SchoolSettings)
}
