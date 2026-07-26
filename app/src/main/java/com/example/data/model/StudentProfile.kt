package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Student Profile Entity
 * Represents comprehensive student information including personal details, class placement,
 * guardian relationship, index number, and enrollment status.
 */
@Entity(tableName = "student_profiles")
data class StudentProfile(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val indexNumber: String, // e.g. "AKM/2025/082"
    val className: String, // e.g. "JHS 2 - Gold", "Primary 4"
    val dateOfBirth: String = "2012-05-14",
    val gender: String = "Male", // "Male", "Female"
    val guardianName: String,
    val guardianPhone: String,
    val guardianEmail: String = "",
    val residentialAddress: String = "Sibi, Oti Region, Ghana",
    val enrollmentStatus: String = "ACTIVE", // "ACTIVE", "GRADUATED", "SUSPENDED", "TRANSFERRED"
    val admissionDate: String = "2023-09-10"
)
