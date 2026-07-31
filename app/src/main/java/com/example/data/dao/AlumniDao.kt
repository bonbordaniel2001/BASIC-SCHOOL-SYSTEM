package com.example.data.dao

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AlumniDao {
    @Query("SELECT * FROM alumni_profiles ORDER BY batchTag DESC, fullName ASC")
    fun getAllAlumniProfiles(): Flow<List<AlumniProfile>>

    @Query("SELECT * FROM alumni_profiles WHERE isActivated = 1 ORDER BY fullName ASC")
    fun getActivatedAlumni(): Flow<List<AlumniProfile>>

    @Query("SELECT * FROM alumni_profiles WHERE isAlumniAdmin = 1 LIMIT 1")
    fun getActiveAlumniAdmin(): Flow<AlumniProfile?>

    @Query("SELECT * FROM alumni_profiles WHERE fullName = :name AND indexNumber = :studentId AND dateOfBirth = :dob AND guardianName = :guardian LIMIT 1")
    suspend fun findMatchingAlumniRecord(
        name: String,
        studentId: String,
        dob: String,
        guardian: String
    ): AlumniProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlumniProfile(profile: AlumniProfile): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllAlumniProfiles(profiles: List<AlumniProfile>)

    @Update
    suspend fun updateAlumniProfile(profile: AlumniProfile)

    @Query("UPDATE alumni_profiles SET isAlumniAdmin = 0")
    suspend fun clearAllAlumniAdmins()

    @Query("UPDATE alumni_profiles SET isAlumniAdmin = 1, adminTermYear = :termYear WHERE id = :alumniId")
    suspend fun setAlumniAdmin(alumniId: Long, termYear: String)

    // --- Chat Messages ---
    @Query("SELECT * FROM alumni_chat_messages ORDER BY id ASC")
    fun getAllAlumniChatMessages(): Flow<List<AlumniChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: AlumniChatMessage): Long

    // --- Call Sessions ---
    @Query("SELECT * FROM alumni_call_sessions ORDER BY id DESC")
    fun getAllCallSessions(): Flow<List<AlumniCallSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCallSession(session: AlumniCallSession): Long

    @Query("UPDATE alumni_call_sessions SET status = :status WHERE id = :sessionId")
    suspend fun updateCallSessionStatus(sessionId: Long, status: String)

    // --- Performance Metrics ---
    @Query("SELECT * FROM school_performance_metrics ORDER BY yearLabel DESC")
    fun getAllPerformanceMetrics(): Flow<List<SchoolPerformanceMetric>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPerformanceMetric(metric: SchoolPerformanceMetric): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPerformanceMetrics(metrics: List<SchoolPerformanceMetric>)

    // --- Aspirant Intent & Voting ---
    @Query("SELECT * FROM alumni_aspirants WHERE termYear = :termYear ORDER BY voteCount DESC")
    fun getAspirantsForTerm(termYear: String): Flow<List<AlumniAspirant>>

    @Query("SELECT * FROM alumni_aspirants ORDER BY voteCount DESC")
    fun getAllAspirants(): Flow<List<AlumniAspirant>>

    @Query("SELECT * FROM alumni_aspirants WHERE docId = :docId LIMIT 1")
    suspend fun getAspirantByDocId(docId: String): AlumniAspirant?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAspirant(aspirant: AlumniAspirant)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllAspirants(aspirants: List<AlumniAspirant>)

    @Query("UPDATE alumni_aspirants SET voteCount = voteCount + 1 WHERE docId = :candidateDocId")
    suspend fun incrementVoteCount(candidateDocId: String)

    @Query("SELECT * FROM admin_votes WHERE voteDocId = :voteDocId LIMIT 1")
    suspend fun getVoteByDocId(voteDocId: String): AdminVote?

    @Query("SELECT * FROM admin_votes WHERE voterUserId = :voterUserId AND termYear = :termYear LIMIT 1")
    suspend fun getUserVoteForTerm(voterUserId: String, termYear: String): AdminVote?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVote(vote: AdminVote)
}
