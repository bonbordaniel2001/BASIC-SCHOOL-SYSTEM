package com.example.data.repository

import com.example.data.dao.GuardianDao
import com.example.data.model.GuardianProfile
import kotlinx.coroutines.flow.Flow

/**
 * Guardian Repository
 * Abstracts data access operations for Guardian entities from ViewModels and UI layers.
 */
class GuardianRepository(private val guardianDao: GuardianDao) {

    val allGuardians: Flow<List<GuardianProfile>> = guardianDao.getAllGuardians()

    fun getGuardianById(id: Long): Flow<GuardianProfile?> = guardianDao.getGuardianById(id)

    suspend fun getGuardianByEmail(email: String): GuardianProfile? = guardianDao.getGuardianByEmail(email)

    suspend fun getGuardianByPhone(phone: String): GuardianProfile? = guardianDao.getGuardianByPhone(phone)

    fun getGuardiansForStudent(studentId: String): Flow<List<GuardianProfile>> = guardianDao.getGuardiansForStudent(studentId)

    suspend fun insertGuardian(guardian: GuardianProfile): Long = guardianDao.insertGuardian(guardian)

    suspend fun insertAllGuardians(guardians: List<GuardianProfile>) = guardianDao.insertAllGuardians(guardians)

    suspend fun updateGuardian(guardian: GuardianProfile) = guardianDao.updateGuardian(guardian)

    suspend fun deleteGuardian(guardian: GuardianProfile) = guardianDao.deleteGuardian(guardian)
}
