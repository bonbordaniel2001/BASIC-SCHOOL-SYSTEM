package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Teacher Profile Entity
 * Represents teacher staff member details, assigned class, subjects, qualification and contact.
 */
@Entity(tableName = "teacher_profiles")
data class TeacherProfile(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val teacherCode: String, // e.g. "TCH/2025/001"
    val assignedClass: String, // e.g. "JHS 2 - Gold"
    val subjectSpecialization: String, // e.g. "Mathematics & Integrated Science"
    val phoneNumber: String,
    val email: String = "",
    val qualification: String = "B.Ed Basic Education",
    val employmentStatus: String = "FULL_TIME", // "FULL_TIME", "PART_TIME"
    val joiningDate: String = "2022-09-01"
)
