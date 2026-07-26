package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lesson_plans")
data class LessonPlan(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val teacherId: Long = 1,
    val teacherName: String = "Mr. Kojo Mensah",
    val className: String = "JHS 2 - Gold",
    val subject: String = "Mathematics",
    val topic: String,
    val subTopic: String = "",
    val lessonDate: String,
    val durationMinutes: Int = 60,
    val objectives: String,
    val teachingMaterials: String = "Textbook, Whiteboard, Flashcards",
    val procedureSteps: String = "",
    val status: String = "PENDING_REVIEW", // PENDING_REVIEW, APPROVED, REVISION_REQUESTED
    val managementFeedback: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
