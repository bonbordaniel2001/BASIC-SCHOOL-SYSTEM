package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Guardian Profile Entity
 * Stores guardian contact information and linked student records.
 */
@Entity(tableName = "guardian_profiles")
data class GuardianProfile(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val phoneNumber: String,
    val email: String,
    val relationship: String, // e.g., "Mother", "Father", "Guardian"
    val address: String,
    val occupation: String = "",
    val emergencyContactPhone: String = "",
    val linkedStudentIds: String = "", // Comma-separated student IDs e.g. "101,102"
    val linkedStudentNames: String = "" // Comma-separated student names e.g. "Ama Serwaa Mensah, Kwame Nkrumah Baffour"
)
