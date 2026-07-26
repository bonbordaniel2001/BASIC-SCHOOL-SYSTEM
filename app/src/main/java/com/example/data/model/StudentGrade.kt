package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Student Grade & Academic Performance Entity
 * Tracks student academic scores, grades, and remarks across Primary & JHS subjects.
 */
@Entity(tableName = "student_grades")
data class StudentGrade(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val studentName: String,
    val className: String, // e.g. "JHS 2 - Gold", "Primary 4"
    val subject: String, // e.g. "Mathematics", "English Language", "Integrated Science", "Social Studies", "ICT", "RME"
    val academicTerm: String = "Term 1", // "Term 1", "Term 2", "Term 3"
    val academicYear: String = "2025/2026",
    val classScore: Double = 0.0, // Continuous assessment (out of 30 or 40)
    val examScore: Double = 0.0, // Final exam score (out of 70 or 60)
    val totalScore: Double = 0.0, // Aggregate score out of 100
    val gradeLetter: String = "A1", // e.g. "A1", "B2", "B3", "C4", "C5", "C6", "D7", "E8", "F9"
    val remarks: String = "" // Teacher remarks e.g. "Excellent mastery of algebraic concepts"
)
