package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Class Timetable Entity
 * Manages weekly class schedules, period timings, subjects, and assigned teacher information
 * for Primary and JHS classes.
 */
@Entity(tableName = "class_timetables")
data class ClassTimetable(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val className: String, // e.g., "JHS 2 - Gold", "Primary 4"
    val dayOfWeek: String, // e.g., "Monday", "Tuesday", "Wednesday", "Thursday", "Friday"
    val periodNumber: Int, // 1, 2, 3, 4, 5, 6, 7
    val startTime: String, // e.g., "08:00 AM"
    val endTime: String, // e.g., "08:45 AM"
    val subject: String, // e.g., "Mathematics", "English Language", "Integrated Science"
    val teacherName: String, // e.g., "Mr. Emmanuel Mensah", "Mrs. Grace Appiah"
    val teacherId: Long = 0,
    val classroom: String = "Room B12",
    val academicTerm: String = "Term 1",
    val academicYear: String = "2025/2026"
)
