package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Student Fee Payment Record
 * Tracks all fee transactions, payments, receipts, and outstanding balance snapshots.
 */
@Entity(tableName = "student_fee_payments")
data class StudentFeePayment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val studentName: String,
    val className: String,
    val guardianName: String,
    val amountPaidGhc: Double,
    val paymentDate: String, // e.g. "2026-07-27, 10:15"
    val paymentMethod: String, // "MTN MoMo", "Telecel Cash", "AT Money", "Cash at Bursar", "Bank Deposit"
    val feeCategory: String, // "Tuition", "Feeding", "PTA Levy", "ICT Lab", "Examination"
    val academicTerm: String = "Term 3",
    val transactionRef: String,
    val receiptNumber: String,
    val remainingBalanceGhc: Double,
    val recordedBy: String = "Guardian MoMo",
    val notes: String = ""
)
