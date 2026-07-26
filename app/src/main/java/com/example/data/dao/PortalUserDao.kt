package com.example.data.dao

import androidx.room.*
import com.example.data.model.UserPortalAccount
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for User Portal Accounts & Dashboard Access Control
 * Enforces single-user active session, strict single-role portal access,
 * and single-proprietor validation per school.
 */
@Dao
interface PortalUserDao {

    @Query("SELECT * FROM portal_user_accounts ORDER BY fullName ASC")
    fun getAllAccounts(): Flow<List<UserPortalAccount>>

    @Query("SELECT * FROM portal_user_accounts WHERE isLoggedIn = 1 LIMIT 1")
    fun getActiveLoggedInAccount(): Flow<UserPortalAccount?>

    @Query("SELECT * FROM portal_user_accounts WHERE id = :id LIMIT 1")
    fun getAccountById(id: Long): Flow<UserPortalAccount?>

    @Query("SELECT * FROM portal_user_accounts WHERE email = :email LIMIT 1")
    suspend fun getAccountByEmail(email: String): UserPortalAccount?

    @Query("SELECT * FROM portal_user_accounts WHERE schoolName = :schoolName AND role = 'PROPRIETOR' LIMIT 1")
    fun getProprietorForSchool(schoolName: String): Flow<UserPortalAccount?>

    @Query("SELECT COUNT(*) FROM portal_user_accounts WHERE schoolName = :schoolName AND role = 'PROPRIETOR'")
    suspend fun getProprietorCountForSchool(schoolName: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: UserPortalAccount): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllAccounts(accounts: List<UserPortalAccount>)

    @Update
    suspend fun updateAccount(account: UserPortalAccount)

    @Delete
    suspend fun deleteAccount(account: UserPortalAccount)

    @Query("UPDATE portal_user_accounts SET isLoggedIn = 0")
    suspend fun logoutAllSessions()

    @Transaction
    suspend fun loginAsSingleUser(userId: Long) {
        logoutAllSessions()
        val account = getAccountByIdDirect(userId)
        if (account != null) {
            updateAccount(account.copy(isLoggedIn = true))
        }
    }

    @Query("SELECT * FROM portal_user_accounts WHERE id = :id LIMIT 1")
    suspend fun getAccountByIdDirect(id: Long): UserPortalAccount?
}
