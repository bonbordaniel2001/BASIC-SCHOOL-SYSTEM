package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * School Event Entity
 * Manages school calendar events, announcements, and activities for students, teachers, and parents.
 */
@Entity(tableName = "school_events")
data class SchoolEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val dateString: String, // e.g. "2026-08-05"
    val timeString: String = "09:00 AM",
    val description: String,
    val targetAudience: String = "ALL", // "ALL", "STUDENTS", "TEACHERS", "PARENTS"
    val location: String = "School Assembly Hall",
    val organizer: String = "School Administration",
    val isImportant: Boolean = false
)
