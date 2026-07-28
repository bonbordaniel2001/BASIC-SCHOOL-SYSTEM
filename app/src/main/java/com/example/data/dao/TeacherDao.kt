package com.example.data.dao

import androidx.room.*
import com.example.data.model.TeacherProfile
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Teacher Profiles
 */
@Dao
interface TeacherDao {
    @Query("SELECT * FROM teacher_profiles ORDER BY fullName ASC")
    fun getAllTeachers(): Flow<List<TeacherProfile>>

    @Query("SELECT * FROM teacher_profiles WHERE id = :id LIMIT 1")
    fun getTeacherById(id: Long): Flow<TeacherProfile?>

    @Query("SELECT * FROM teacher_profiles WHERE assignedClass = :className LIMIT 1")
    fun getTeacherByClass(className: String): Flow<TeacherProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeacher(teacher: TeacherProfile): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTeachers(teachers: List<TeacherProfile>)

    @Update
    suspend fun updateTeacher(teacher: TeacherProfile)

    @Delete
    suspend fun deleteTeacher(teacher: TeacherProfile)
}
