package com.example.data.model

/**
 * Official Transaction Receipt Model
 * Represents a printable, downloadable digital receipt generated after every
 * school financial transaction (Student Fee Payment, Proprietor Direct Receipt, Staff Salary Disbursement).
 */
data class OfficialReceipt(
    val receiptNumber: String,
    val title: String = "OFFICIAL TRANSACTION RECEIPT",
    val schoolName: String = "St. Talafor Primary & JHS",
    val recipientName: String, // Student Name or Staff Member Name
    val subDetail: String, // e.g. "Class: JHS 2 - Gold" or "Designation: Senior JHS Teacher"
    val payerOrGuardian: String, // e.g. "Guardian: Madam Serwaa Mensah" or "Paid by: Proprietor Administration"
    val amountGhc: Double,
    val paymentMethod: String, // "MTN MoMo", "Telecel Cash", "Bank Deposit", "Cash at Bursar"
    val transactionRef: String,
    val paymentDate: String,
    val feeCategoryOrMemo: String, // "Tuition & School Fees", "Term 3 Feeding", "Monthly Salary Payout"
    val remainingBalanceGhc: Double? = null,
    val authorizedBy: String = "Dr. Kwabena Mensah (School Proprietor)",
    val notes: String = "",
    val isSalaryPayment: Boolean = false
) {
    /**
     * Formats receipt as plain text suitable for saving to storage or sharing via WhatsApp / SMS / Email
     */
    fun toFormattedReceiptText(): String {
        return buildString {
            appendLine("========================================")
            appendLine("       $schoolName       ")
            appendLine("   Excellence in Education • Sibi, Ghana")
            appendLine("========================================")
            appendLine("RECEIPT NO: $receiptNumber")
            appendLine("TRANSACTION REF: $transactionRef")
            appendLine("DATE: $paymentDate")
            appendLine("----------------------------------------")
            appendLine("TYPE: $title")
            appendLine("NAME: $recipientName")
            appendLine("DETAILS: $subDetail")
            appendLine("PARTY: $payerOrGuardian")
            appendLine("CATEGORY: $feeCategoryOrMemo")
            appendLine("METHOD: $paymentMethod")
            appendLine("----------------------------------------")
            appendLine("AMOUNT: GH₵ ${String.format("%.2f", amountGhc)}")
            if (remainingBalanceGhc != null) {
                appendLine("OUTSTANDING BALANCE: GH₵ ${String.format("%.2f", remainingBalanceGhc)}")
            }
            if (notes.isNotBlank()) {
                appendLine("MEMO / NOTES: $notes")
            }
            appendLine("----------------------------------------")
            appendLine("AUTHORIZED BY: $authorizedBy")
            appendLine("STATUS: VERIFIED & OFFICIALLY RECORDED")
            appendLine("========================================")
            appendLine("Thank you for partnering with St. Talafor!")
        }
    }
}
