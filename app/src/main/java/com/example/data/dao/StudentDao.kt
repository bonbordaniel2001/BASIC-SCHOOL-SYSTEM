package com.example.data.dao

import androidx.room.*
import com.example.data.model.StudentProfile
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Student Profiles
 */
@Dao
interface StudentDao {
    @Query("SELECT * FROM student_profiles ORDER BY className ASC, fullName ASC")
    fun getAllStudents(): Flow<List<StudentProfile>>

    @Query("SELECT * FROM student_profiles WHERE id = :id LIMIT 1")
    fun getStudentById(id: Long): Flow<StudentProfile?>

    @Query("SELECT * FROM student_profiles WHERE className = :className ORDER BY fullName ASC")
    fun getStudentsByClass(className: String): Flow<List<StudentProfile>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentProfile): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllStudents(students: List<StudentProfile>)

    @Update
    suspend fun updateStudent(student: StudentProfile)

    @Delete
    suspend fun deleteStudent(student: StudentProfile)

    @Query("DELETE FROM student_profiles WHERE id = :studentId")
    suspend fun deleteStudentById(studentId: Long)
}
