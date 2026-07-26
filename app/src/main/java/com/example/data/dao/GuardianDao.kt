package com.example.data.dao

import androidx.room.*
import com.example.data.model.GuardianProfile
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Guardian Profiles
 * Handles CRUD operations for guardian contact information and linked student records.
 */
@Dao
interface GuardianDao {
    @Query("SELECT * FROM guardian_profiles ORDER BY fullName ASC")
    fun getAllGuardians(): Flow<List<GuardianProfile>>

    @Query("SELECT * FROM guardian_profiles WHERE id = :id LIMIT 1")
    fun getGuardianById(id: Long): Flow<GuardianProfile?>

    @Query("SELECT * FROM guardian_profiles WHERE email = :email LIMIT 1")
    suspend fun getGuardianByEmail(email: String): GuardianProfile?

    @Query("SELECT * FROM guardian_profiles WHERE phoneNumber = :phone LIMIT 1")
    suspend fun getGuardianByPhone(phone: String): GuardianProfile?

    @Query("SELECT * FROM guardian_profiles WHERE linkedStudentIds LIKE '%' || :studentId || '%'")
    fun getGuardiansForStudent(studentId: String): Flow<List<GuardianProfile>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGuardian(guardian: GuardianProfile): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllGuardians(guardians: List<GuardianProfile>)

    @Update
    suspend fun updateGuardian(guardian: GuardianProfile)

    @Delete
    suspend fun deleteGuardian(guardian: GuardianProfile)
}
