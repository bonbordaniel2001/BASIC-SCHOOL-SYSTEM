package com.example.data.dao

import androidx.room.*
import com.example.data.model.DailyStudentAttendance
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for Daily Student Attendance operations.
 */
@Dao
interface AttendanceDao {

    @Query("SELECT * FROM daily_student_attendance ORDER BY dateString DESC, studentName ASC")
    fun getAllDailyAttendance(): Flow<List<DailyStudentAttendance>>

    @Query("SELECT * FROM daily_student_attendance WHERE studentId = :studentId ORDER BY dateString DESC")
    fun getAttendanceForStudent(studentId: Long): Flow<List<DailyStudentAttendance>>

    @Query("SELECT * FROM daily_student_attendance WHERE dateString = :date ORDER BY className ASC, studentName ASC")
    fun getAttendanceByDate(date: String): Flow<List<DailyStudentAttendance>>

    @Query("SELECT * FROM daily_student_attendance WHERE className = :className AND dateString = :date ORDER BY studentName ASC")
    fun getAttendanceForClassAndDate(className: String, date: String): Flow<List<DailyStudentAttendance>>

    @Query("SELECT COUNT(*) FROM daily_student_attendance WHERE studentId = :studentId AND isPresent = 1")
    fun getPresentCountForStudent(studentId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM daily_student_attendance WHERE studentId = :studentId AND isAbsent = 1")
    fun getAbsentCountForStudent(studentId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM daily_student_attendance WHERE studentId = :studentId AND isExcused = 1")
    fun getExcusedCountForStudent(studentId: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: DailyStudentAttendance): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllAttendance(records: List<DailyStudentAttendance>)

    @Update
    suspend fun updateAttendance(attendance: DailyStudentAttendance)

    @Delete
    suspend fun deleteAttendance(attendance: DailyStudentAttendance)
}
