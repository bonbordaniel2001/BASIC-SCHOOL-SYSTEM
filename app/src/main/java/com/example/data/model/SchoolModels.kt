package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Staff Member Entity with customizable role delegation permissions
 */
@Entity(tableName = "staff_members")
data class StaffMember(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val staffCode: String,
    val primaryRole: String, // e.g. "Senior Teacher", "Head of JHS", "Bursar"
    val isCanEditGrades: Boolean = true,
    val isCanMarkAttendance: Boolean = true,
    val isCanApproveOverrides: Boolean = false,
    val isCanAccessFinancials: Boolean = false,
    val isCanSendSms: Boolean = true,
    val isCanManageRoles: Boolean = false
)

/**
 * Transaction Approval Request Entity (Loan Requests & Fee Overrides)
 */
@Entity(tableName = "transaction_approvals")
data class TransactionApproval(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val requestorName: String,
    val requestorRole: String,
    val requestType: String, // "STAFF_LOAN", "FEE_OVERRIDE", "EQUIPMENT_PURCHASE"
    val amountGhc: Double,
    val reason: String,
    val dateString: String,
    val status: String = "PENDING", // "PENDING", "APPROVED", "REJECTED"
    val decisionNote: String = ""
)

/**
 * Broadcast Messaging Log (SMS & WhatsApp)
 */
@Entity(tableName = "message_logs")
data class MessageLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val targetType: String, // "ALL_PARENTS", "CLASS_SPECIFIC", "INDIVIDUAL"
    val targetDetail: String, // e.g. "All Primary & JHS", "JHS 2 Gold", "Mr. Kofi Mensah"
    val channel: String, // "SMS", "WHATSAPP"
    val messageText: String,
    val recipientCount: Int,
    val estimatedCostGhc: Double,
    val timestampString: String,
    val senderName: String
)

/**
 * Student Attendance Entity (Weekly Overview)
 * Supports local offline storage with sync state tracking flags.
 */
@Entity(tableName = "attendance_records")
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val studentName: String,
    val className: String, // e.g., "JHS 2 - Gold", "Primary 4"
    val mondayStatus: String = "PRESENT", // "PRESENT", "ABSENT", "LATE"
    val tuesdayStatus: String = "PRESENT",
    val wednesdayStatus: String = "PRESENT",
    val thursdayStatus: String = "PRESENT",
    val fridayStatus: String = "PRESENT",
    val isSynced: Boolean = true, // Offline sync state tracking flag
    val lastModifiedTimestamp: Long = System.currentTimeMillis()
)

/**
 * Daily Student Attendance Entity
 * Tracks daily status for individual students with status fields for Present, Absent, and Excused.
 */
@Entity(tableName = "daily_student_attendance")
data class DailyStudentAttendance(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val studentName: String,
    val className: String,
    val dateString: String, // e.g. "2026-07-22"
    val status: String = "PRESENT", // "PRESENT", "ABSENT", "EXCUSED"
    val isPresent: Boolean = true,
    val isAbsent: Boolean = false,
    val isExcused: Boolean = false,
    val remarks: String = "",
    val isSynced: Boolean = true,
    val lastModifiedTimestamp: Long = System.currentTimeMillis()
)

/**
 * Teacher Geofenced Clock-In Record
 */
@Entity(tableName = "clock_in_logs")
data class ClockInLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val teacherName: String,
    val timestampString: String,
    val latitude: Double,
    val longitude: Double,
    val distanceMeters: Float,
    val isWithinGeofence: Boolean,
    val actionType: String // "CLOCK_IN", "CLOCK_OUT"
)

/**
 * Student Ledger Entity (Guardian View)
 */
@Entity(tableName = "student_ledgers")
data class StudentLedger(
    @PrimaryKey(autoGenerate = true) val studentId: Long = 0,
    val studentName: String,
    val indexNumber: String,
    val className: String,
    val guardianName: String,
    val guardianPhone: String,
    val termTuitionGhc: Double,
    val ptaLevyGhc: Double,
    val ictLabFeeGhc: Double,
    val feedingFeeGhc: Double,
    val totalFeesGhc: Double,
    val paidFeesGhc: Double,
    val balanceGhc: Double
)

/**
 * Fee Transactions (Mini-Statement items)
 */
@Entity(tableName = "fee_transactions")
data class FeeTransaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val transactionRef: String,
    val amountGhc: Double,
    val paymentMethod: String, // "MTN MoMo", "Telecel Cash", "AT Money", "Cash at Bursar"
    val channelNumber: String,
    val description: String,
    val dateString: String,
    val status: String = "SUCCESS"
)

/**
 * User Account Entity for Authentication & Role Management
 */
@Entity(tableName = "user_accounts")
data class UserAccount(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val email: String,
    val phone: String,
    val role: String, // "PROPRIETOR", "TEACHER", "GUARDIAN"
    val schoolName: String = "Akoma Primary & JHS",
    val isLoggedIn: Boolean = false
)

/**
 * Real-Time Notification Entity
 */
@Entity(tableName = "app_notifications")
data class AppNotification(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recipientRole: String, // "PROPRIETOR", "TEACHER", "GUARDIAN", "ALL"
    val type: String, // "APPROVAL_REQUIRED", "ATTENDANCE_ISSUE", "FEE_DUE", "PAYMENT_SUCCESS", "BROADCAST"
    val title: String,
    val message: String,
    val timestampString: String,
    val isRead: Boolean = false
)

/**
 * Student Registration Request submitted by Teacher for Proprietor Approval
 */
@Entity(tableName = "student_add_requests")
data class StudentAddRequest(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentName: String,
    val className: String,
    val guardianName: String = "",
    val guardianPhone: String = "0244123456",
    val estimatedFeesGhc: Double = 1200.0,
    val reason: String = "",
    val requestedByTeacher: String = "Class Teacher",
    val status: String = "PENDING", // "PENDING", "APPROVED", "REJECTED"
    val requestDate: String = "2026-07-26"
)

/**
 * School Global Settings Entity
 */
@Entity(tableName = "school_settings")
data class SchoolSettings(
    @PrimaryKey val id: Long = 1,
    val schoolName: String = "Akoma Primary & JHS",
    val campusLocation: String = "Sibi, Oti Region, Ghana"
)

/**
 * Chart Data Models for Proprietor Dashboard Data Visualizations
 */
data class MonthlyFeeStat(
    val monthLabel: String,
    val targetGhc: Double,
    val collectedGhc: Double
)

data class EnrollmentTrendPoint(
    val periodLabel: String,
    val totalStudents: Int,
    val jhsStudents: Int,
    val primaryStudents: Int
)

