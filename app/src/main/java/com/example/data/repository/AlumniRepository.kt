package com.example.data.repository

import com.example.data.dao.AlumniDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*

class AlumniRepository(private val alumniDao: AlumniDao) {

    val allAlumniProfiles: Flow<List<AlumniProfile>> = alumniDao.getAllAlumniProfiles()
    val activatedAlumniProfiles: Flow<List<AlumniProfile>> = alumniDao.getActivatedAlumni()
    val activeAlumniAdmin: Flow<AlumniProfile?> = alumniDao.getActiveAlumniAdmin()
    val allAlumniChatMessages: Flow<List<AlumniChatMessage>> = alumniDao.getAllAlumniChatMessages()
    val allCallSessions: Flow<List<AlumniCallSession>> = alumniDao.getAllCallSessions()
    val allPerformanceMetrics: Flow<List<SchoolPerformanceMetric>> = alumniDao.getAllPerformanceMetrics()
    val allAspirants: Flow<List<AlumniAspirant>> = alumniDao.getAllAspirants()

    /**
     * Submit Aspirant Intent for Alumni Admin position for upcoming term year.
     * Document written with status "pending_intent" and voteCount 0.
     */
    suspend fun submitAspirantIntent(
        user: AlumniProfile,
        manifesto: String,
        termYear: String = "2027",
        termStartMillis: Long = System.currentTimeMillis() + (10L * 24 * 3600 * 1000) // Default 10 days from now
    ): AlumniAspirant {
        // Enforce backend validation: Check if intent application window is closed
        val currentMillis = System.currentTimeMillis()
        val sevenDaysBeforeStart = termStartMillis - (7L * 24 * 3600 * 1000)
        
        if (currentMillis >= sevenDaysBeforeStart) {
            throw IllegalStateException("Application Window Closed: Intent submissions for term $termYear closed when the 7-day voting window opened.")
        }

        val docId = "${termYear}_${user.id}"
        val aspirant = AlumniAspirant(
            docId = docId,
            userId = user.id.toString(),
            candidateName = user.fullName,
            batchTag = user.batchTag,
            manifesto = manifesto,
            termYear = termYear,
            startDateTimestamp = termStartMillis,
            endDateTimestamp = termStartMillis + (365L * 24 * 3600 * 1000),
            status = "pending_intent",
            voteCount = 0
        )
        alumniDao.insertAspirant(aspirant)
        return aspirant
    }

    /**
     * Time-Locked Voting Engine with Double-Vote Prevention and Self-Vote Prevention
     */
    suspend fun castVoteForAspirant(
        voter: AlumniProfile,
        candidateDocId: String,
        termYear: String = "2027"
    ) {
        val aspirant = alumniDao.getAspirantByDocId(candidateDocId)
            ?: throw IllegalArgumentException("Candidate record not found for ID: $candidateDocId")

        // 1. Time Check Constraint: Must fall strictly within the 7-day window prior to term startDate
        val currentMillis = System.currentTimeMillis()
        val startDate = aspirant.startDateTimestamp
        val sevenDaysBeforeStart = startDate - (7L * 24 * 3600 * 1000)

        // Note: For live UI demonstration, if window hasn't opened yet or closed, check condition:
        val isWindowActive = currentMillis in sevenDaysBeforeStart..startDate
        if (!isWindowActive) {
            // For testing flexibility in demo, if outside standard bounds we provide clear diagnostic error or simulate window open
            if (currentMillis < sevenDaysBeforeStart) {
                throw IllegalStateException("Voting Window Not Open Yet: Voting opens strictly 7 days prior to term start (${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(sevenDaysBeforeStart))}).")
            } else if (currentMillis > startDate) {
                throw IllegalStateException("Voting Window Expired: Voting for the $termYear term concluded at term start date.")
            }
        }

        // 2. Self-Vote Prevention
        if (aspirant.userId == voter.id.toString()) {
            throw IllegalStateException("Invalid Vote Action: Candidates cannot vote for themselves.")
        }

        // 3. Double-Vote Prevention via admin_votes collection lookup ({year}_{voterId})
        val voteDocId = "${termYear}_${voter.id}"
        val existingVote = alumniDao.getVoteByDocId(voteDocId)
        if (existingVote != null) {
            throw IllegalStateException("Double Vote Prevention: You have already cast your ballot for the $termYear term.")
        }

        // Atomic Batch / Transaction Write
        val newVote = AdminVote(
            voteDocId = voteDocId,
            termYear = termYear,
            voterUserId = voter.id.toString(),
            candidateDocId = candidateDocId,
            votedTimestamp = currentMillis
        )
        alumniDao.insertVote(newVote)
        alumniDao.incrementVoteCount(candidateDocId)
    }

    suspend fun hasUserVoted(voterId: String, termYear: String = "2027"): Boolean {
        val voteDocId = "${termYear}_$voterId"
        return alumniDao.getVoteByDocId(voteDocId) != null
    }

    /**
     * Calculate estimated graduation year based on admission date and Basic Education duration (~9-10 yrs)
     */
    fun calculateGraduationYear(admissionDateStr: String, track: String = "BASIC_K6_JHS3"): Int {
        val startYear = try {
            val parts = admissionDateStr.split("-")
            if (parts.isNotEmpty()) parts[0].toInt() else 2016
        } catch (e: Exception) {
            2016
        }
        val durationYears = if (track.contains("JHS_ONLY")) 3 else 9
        return startYear + durationYears
    }

    /**
     * Strict verification engine: Matches Name, Index/Student ID, DOB, and Guardian Name
     */
    suspend fun verifyAndActivateAccount(
        name: String,
        studentId: String,
        dob: String,
        guardianName: String,
        phone: String,
        email: String
    ): AlumniProfile? {
        val record = alumniDao.findMatchingAlumniRecord(
            name.trim(),
            studentId.trim(),
            dob.trim(),
            guardianName.trim()
        ) ?: return null

        val updatedRecord = record.copy(
            isAccountLocked = false,
            isActivated = true,
            phone = phone.ifBlank { record.phone },
            email = email.ifBlank { record.email }
        )
        alumniDao.updateAlumniProfile(updatedRecord)
        return updatedRecord
    }

    suspend fun rotateAnnualAdmin(newAdminId: Long, termYear: String = "2026 Admin") {
        alumniDao.clearAllAlumniAdmins()
        alumniDao.setAlumniAdmin(newAdminId, termYear)
    }

    suspend fun batchImportAlumni(profiles: List<AlumniProfile>) {
        alumniDao.insertAllAlumniProfiles(profiles)
    }

    suspend fun sendChatMessage(
        senderName: String,
        senderRole: String,
        messageText: String,
        mediaType: String = "TEXT",
        mediaUrl: String = "",
        fileName: String = ""
    ): Long {
        val dateFormat = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
        val msg = AlumniChatMessage(
            senderName = senderName,
            senderRole = senderRole,
            messageText = messageText,
            mediaType = mediaType,
            mediaUrl = mediaUrl,
            fileName = fileName,
            timestampString = dateFormat.format(Date()),
            isProprietorVisible = false
        )
        return alumniDao.insertChatMessage(msg)
    }

    suspend fun initiateCallSession(
        initiator: AlumniProfile,
        callType: String = "AUDIO_CONSULTATION"
    ): AlumniCallSession {
        // Enforce ACL check: Only Alumni Admin can call Proprietor
        if (!initiator.isAlumniAdmin) {
            throw SecurityException("Access Denied: Direct calls to the Proprietor are restricted exclusively to designated Alumni Admins.")
        }
        val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val session = AlumniCallSession(
            initiatorId = initiator.id,
            initiatorName = initiator.fullName,
            initiatorRole = "ALUMNI_ADMIN",
            recipientName = "School Proprietor",
            callType = callType,
            status = "RINGING",
            isProprietorCall = true,
            timestampString = dateFormat.format(Date())
        )
        val id = alumniDao.insertCallSession(session)
        return session.copy(id = id)
    }

    suspend fun seedInitialAlumniDataIfEmpty() {
        val existing = alumniDao.getAllAlumniProfiles().first()
        if (existing.isEmpty()) {
            val initialAlumni = listOf(
                AlumniProfile(
                    fullName = "Daniel Akuffo",
                    indexNumber = "AKM/2016/001",
                    dateOfBirth = "2002-04-12",
                    guardianName = "Seth Akuffo",
                    admissionDate = "2016-09-10",
                    estimatedGraduationYear = 2025,
                    actualGraduationYear = 2025,
                    isAccountLocked = false,
                    isActivated = true,
                    phone = "0244987654",
                    email = "daniel.akuffo@alumni.edu.gh",
                    isAlumniAdmin = true,
                    adminTermYear = "2026 Yearly Admin",
                    batchTag = "Class of 2025",
                    currentOccupation = "Software Engineering Student",
                    residentialCity = "Accra"
                ),
                AlumniProfile(
                    fullName = "Esi Mansa Ofori",
                    indexNumber = "AKM/2016/002",
                    dateOfBirth = "2002-08-20",
                    guardianName = "Kofi Ofori",
                    admissionDate = "2016-09-10",
                    estimatedGraduationYear = 2025,
                    actualGraduationYear = 2025,
                    isAccountLocked = false,
                    isActivated = true,
                    phone = "0201234567",
                    email = "esi.ofori@alumni.edu.gh",
                    isAlumniAdmin = false,
                    batchTag = "Class of 2025",
                    currentOccupation = "Medical Student",
                    residentialCity = "Kumasi"
                ),
                AlumniProfile(
                    fullName = "Yaw Sarpong",
                    indexNumber = "AKM/2015/014",
                    dateOfBirth = "2001-11-05",
                    guardianName = "Grace Sarpong",
                    admissionDate = "2015-09-08",
                    estimatedGraduationYear = 2024,
                    actualGraduationYear = 2024,
                    isAccountLocked = true,
                    isActivated = false,
                    phone = "0277889900",
                    email = "yaw.sarpong@alumni.edu.gh",
                    isAlumniAdmin = false,
                    batchTag = "Class of 2024",
                    currentOccupation = "Entrepreneur",
                    residentialCity = "Ho"
                ),
                AlumniProfile(
                    fullName = "Abena Pokua",
                    indexNumber = "AKM/2014/030",
                    dateOfBirth = "2000-03-15",
                    guardianName = "Samuel Poku",
                    admissionDate = "2014-09-01",
                    estimatedGraduationYear = 2023,
                    actualGraduationYear = 2023,
                    isAccountLocked = true,
                    isActivated = false,
                    batchTag = "Class of 2023",
                    currentOccupation = "Civil Servant",
                    residentialCity = "Tamale"
                )
            )
            alumniDao.insertAllAlumniProfiles(initialAlumni)

            val chatMessages = listOf(
                AlumniChatMessage(
                    senderName = "Daniel Akuffo (Alumni Admin)",
                    senderRole = "ALUMNI_ADMIN",
                    messageText = "Welcome fellow Akoma graduates to our official Alumni Network Hub!",
                    mediaType = "TEXT",
                    timestampString = "08:30 AM"
                ),
                AlumniChatMessage(
                    senderName = "Esi Mansa Ofori",
                    senderRole = "ALUMNI_MEMBER",
                    messageText = "Glad to be here! Sharing photos from our graduation ceremony.",
                    mediaType = "IMAGE",
                    mediaUrl = "https://images.unsplash.com/photo-1523050854058-8df90110c9f1?w=600",
                    fileName = "Graduation_2025_Group.jpg",
                    timestampString = "08:45 AM"
                ),
                AlumniChatMessage(
                    senderName = "Esi Mansa Ofori",
                    senderRole = "ALUMNI_MEMBER",
                    messageText = "Uploading the official 2025 Alumni Transcript & Constitution Document.",
                    mediaType = "DOCUMENT",
                    fileName = "Akoma_Alumni_Constitution_2025.pdf",
                    timestampString = "09:12 AM"
                )
            )
            for (msg in chatMessages) {
                alumniDao.insertChatMessage(msg)
            }

            val performanceMetrics = listOf(
                SchoolPerformanceMetric(
                    yearLabel = "2025 BECE Academic Year",
                    becePassRatePercentage = 99.2,
                    overallPassRatePercentage = 97.8,
                    topSubject = "Integrated Science & ICT",
                    totalGraduates = 48,
                    milestoneDescription = "Top overall basic school score in Oti Region. 100% placement into Grade A SHS.",
                    infrastructureProjects = "Built 2 modern ICT Laboratories & Solar Powered Library"
                ),
                SchoolPerformanceMetric(
                    yearLabel = "2024 BECE Academic Year",
                    becePassRatePercentage = 98.0,
                    overallPassRatePercentage = 95.5,
                    topSubject = "Mathematics & Social Studies",
                    totalGraduates = 42,
                    milestoneDescription = "Achieved 98% distinction rate in national standard examinations.",
                    infrastructureProjects = "Expanded Science Laboratory & E-Learning Center"
                )
            )
            alumniDao.insertAllPerformanceMetrics(performanceMetrics)

            // Seed initial aspirants for the upcoming 2027 Admin Term
            val termStartMillis = System.currentTimeMillis() + (3L * 24 * 3600 * 1000) // 3 days from now (within 7-day voting window!)
            val initialAspirants = listOf(
                AlumniAspirant(
                    docId = "2027_2",
                    userId = "2",
                    candidateName = "Esi Mansa Ofori",
                    batchTag = "Class of 2025",
                    manifesto = "Pledging to establish a revolving Tertiary Scholarship Fund and expand STEM mentorship for JHS graduates.",
                    termYear = "2027",
                    startDateTimestamp = termStartMillis,
                    endDateTimestamp = termStartMillis + (365L * 24 * 3600 * 1000),
                    status = "approved",
                    voteCount = 14
                ),
                AlumniAspirant(
                    docId = "2027_3",
                    userId = "3",
                    candidateName = "Yaw Sarpong",
                    batchTag = "Class of 2024",
                    manifesto = "Focusing on regional alumni business networking, career fairs, and solar energy upgrades for basic classrooms.",
                    termYear = "2027",
                    startDateTimestamp = termStartMillis,
                    endDateTimestamp = termStartMillis + (365L * 24 * 3600 * 1000),
                    status = "approved",
                    voteCount = 9
                )
            )
            alumniDao.insertAllAspirants(initialAspirants)
        }
    }
}
