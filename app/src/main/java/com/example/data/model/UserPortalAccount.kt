package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Single-Role User Account Entity for Dashboard Authentication & Access Control.
 * Guarantees that:
 * 1. An individual logs in as only ONE user/role at a time.
 * 2. After creation, account access is strictly locked to the assigned role portal.
 * 3. A school allows ONLY ONE active Proprietor account.
 */
@Entity(
    tableName = "portal_user_accounts",
    indices = [
        Index(value = ["email"], unique = true)
    ]
)
data class UserPortalAccount(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val email: String,
    val phone: String,
    val role: String, // "PROPRIETOR", "TEACHER", "GUARDIAN"
    val schoolName: String = "St. Talafor Primary & JHS",
    val isLoggedIn: Boolean = false,
    val isProprietor: Boolean = (role == "PROPRIETOR"),
    val portalAccessRole: String = role, // Locked portal access role
    val createdTimestamp: Long = System.currentTimeMillis()
)
