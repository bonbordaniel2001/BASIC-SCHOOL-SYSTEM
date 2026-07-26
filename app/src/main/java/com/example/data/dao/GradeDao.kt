package com.example.data.dao

import androidx.room.*
import com.example.data.model.StudentGrade
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Student Academic Grades
 * Handles CRUD queries for academic performance tracking across Primary & JHS subjects.
 */
@Dao
interface GradeDao {
    @Query("SELECT * FROM student_grades ORDER BY className ASC, studentName ASC, subject ASC")
    fun getAllGrades(): Flow<List<StudentGrade>>

    @Query("SELECT * FROM student_grades WHERE studentId = :studentId ORDER BY academicYear DESC, academicTerm DESC, subject ASC")
    fun getGradesForStudent(studentId: Long): Flow<List<StudentGrade>>

    @Query("SELECT * FROM student_grades WHERE className = :className AND subject = :subject ORDER BY studentName ASC")
    fun getGradesByClassAndSubject(className: String, subject: String): Flow<List<StudentGrade>>

    @Query("SELECT * FROM student_grades WHERE studentId = :studentId AND academicTerm = :term AND academicYear = :year ORDER BY subject ASC")
    fun getGradesForStudentInTerm(studentId: Long, term: String, year: String): Flow<List<StudentGrade>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrade(grade: StudentGrade): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllGrades(grades: List<StudentGrade>)

    @Update
    suspend fun updateGrade(grade: StudentGrade)

    @Delete
    suspend fun deleteGrade(grade: StudentGrade)
}
