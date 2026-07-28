package com.example.data.repository

import com.example.data.dao.StudentDao
import com.example.data.model.StudentProfile
import kotlinx.coroutines.flow.Flow

/**
 * Student Repository
 * Abstracts data access operations for Student entities from ViewModels and UI layers.
 */
class StudentRepository(private val studentDao: StudentDao) {

    val allStudents: Flow<List<StudentProfile>> = studentDao.getAllStudents()

    fun getStudentById(id: Long): Flow<StudentProfile?> = studentDao.getStudentById(id)

    fun getStudentsByClass(className: String): Flow<List<StudentProfile>> = studentDao.getStudentsByClass(className)

    suspend fun insertStudent(student: StudentProfile): Long = studentDao.insertStudent(student)

    suspend fun insertAllStudents(students: List<StudentProfile>) = studentDao.insertAllStudents(students)

    suspend fun updateStudent(student: StudentProfile) = studentDao.updateStudent(student)

    suspend fun deleteStudent(student: StudentProfile) = studentDao.deleteStudent(student)

    suspend fun deleteStudentById(studentId: Long) = studentDao.deleteStudentById(studentId)
}
