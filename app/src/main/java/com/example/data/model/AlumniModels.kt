package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Alumni Profile Entity representing pre-created locked student records,
 * graduation status, and alumni network membership credentials.
 */
@Entity(tableName = "alumni_profiles")
data class AlumniProfile(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val indexNumber: String, // Student ID / Index Number e.g. "AKM/2016/001"
    val dateOfBirth: String, // YYYY-MM-DD
    val guardianName: String,
    val guardianPhone: String = "",
    val admissionDate: String, // YYYY-MM-DD
    val educationTrack: String = "BASIC_K6_JHS3", // Span: KG + Primary + JHS (~9-10 yrs)
    val estimatedGraduationYear: Int = 2025,
    val actualGraduationYear: Int = 2025,
    val enrollmentStatus: String = "GRADUATED", // "GRADUATED", "PRE_GRADUATED_LOCKED", "ACTIVE"
    val isAccountLocked: Boolean = true,
    val isActivated: Boolean = false,
    val phone: String = "",
    val email: String = "",
    val isAlumniAdmin: Boolean = false,
    val adminTermYear: String = "2026 Admin",
    val batchTag: String = "Class of 2025",
    val currentOccupation: String = "Tertiary Student",
    val residentialCity: String = "Accra, Ghana"
)

/**
 * Alumni Group Chat & Media Sharing Message Entity.
 * Proprietors cannot view these unless explicitly invited.
 */
@Entity(tableName = "alumni_chat_messages")
data class AlumniChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val senderId: Long = 0,
    val senderName: String,
    val senderRole: String = "ALUMNI_MEMBER", // "ALUMNI_ADMIN", "ALUMNI_MEMBER", "PROPRIETOR_GUEST"
    val messageText: String,
    val mediaType: String = "TEXT", // "TEXT", "IMAGE", "VIDEO", "DOCUMENT"
    val mediaUrl: String = "",
    val fileName: String = "",
    val timestampString: String,
    val isProprietorVisible: Boolean = false
)

/**
 * Real-Time Call Session Entity for Peer-to-Peer or Admin-Proprietor Consultations.
 */
@Entity(tableName = "alumni_call_sessions")
data class AlumniCallSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val initiatorId: Long,
    val initiatorName: String,
    val initiatorRole: String, // "ALUMNI_ADMIN", "ALUMNI_MEMBER"
    val recipientName: String = "School Proprietor",
    val callType: String = "AUDIO_CONSULTATION", // "AUDIO_CONSULTATION", "VIDEO_CONSULTATION", "PEER_CALL"
    val status: String = "RINGING", // "RINGING", "CONNECTED", "ENDED", "REJECTED"
    val isProprietorCall: Boolean = true,
    val timestampString: String
)

/**
 * Yearly Admin Registry Entity mirroring the Firestore yearly_admin_registry collection.
 * Document ID structure: {year}_{admin_user_id} e.g. "2026_usr12345"
 */
@Entity(tableName = "yearly_admin_registry")
data class YearlyAdminRegistryEntry(
    @PrimaryKey val docId: String, // e.g. "2026_usr12345"
    val userId: String,
    val role: String = "Alumni Admin",
    val startDateTimestamp: Long,
    val endDateTimestamp: Long,
    val status: String = "active" // "active" | "expired"
)

/**
 * Aspirant Intent entity for Alumni Admin position submissions in yearly_admin_registry.
 * Document ID structure: {year}_{admin_user_id} e.g. "2027_usr102"
 */
@Entity(tableName = "alumni_aspirants")
data class AlumniAspirant(
    @PrimaryKey val docId: String, // "{year}_{admin_user_id}"
    val userId: String,
    val candidateName: String,
    val batchTag: String = "Class of 2025",
    val manifesto: String = "",
    val termYear: String = "2027",
    val startDateTimestamp: Long, // Start of term timestamp
    val endDateTimestamp: Long,   // End of term timestamp
    val status: String = "pending_intent", // "pending_intent" | "approved" | "elected"
    val voteCount: Int = 0
)

/**
 * Time-locked member vote tracking entity to enforce double-vote prevention.
 * Document ID structure in admin_votes: {year}_{voterId} e.g. "2027_usr205"
 */
@Entity(tableName = "admin_votes")
data class AdminVote(
    @PrimaryKey val voteDocId: String, // "{year}_{voterId}"
    val termYear: String,
    val voterUserId: String,
    val candidateDocId: String,
    val votedTimestamp: Long
)

/**
 * School Performance & Growth Metric Entity accessible in read-only mode to all verified alumni.
 */
@Entity(tableName = "school_performance_metrics")
data class SchoolPerformanceMetric(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val yearLabel: String, // e.g. "2025 Academic Year"
    val becePassRatePercentage: Double = 98.5,
    val overallPassRatePercentage: Double = 96.0,
    val topSubject: String = "Integrated Science & ICT",
    val totalGraduates: Int = 45,
    val milestoneDescription: String = "Constructed 2 new modern ICT and Science Laboratories",
    val infrastructureProjects: String = "Solar Power Installation & Digital Library Integration"
)
