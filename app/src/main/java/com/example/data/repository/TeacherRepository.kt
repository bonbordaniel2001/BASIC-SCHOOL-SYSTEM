package com.example.data.repository

import com.example.data.dao.TeacherDao
import com.example.data.model.TeacherProfile
import kotlinx.coroutines.flow.Flow

/**
 * Teacher Repository
 * Abstracts data access operations for Teacher entities from ViewModels and UI layers.
 */
class TeacherRepository(private val teacherDao: TeacherDao) {

    val allTeachers: Flow<List<TeacherProfile>> = teacherDao.getAllTeachers()

    fun getTeacherById(id: Long): Flow<TeacherProfile?> = teacherDao.getTeacherById(id)

    fun getTeacherByClass(className: String): Flow<TeacherProfile?> = teacherDao.getTeacherByClass(className)

    suspend fun insertTeacher(teacher: TeacherProfile): Long = teacherDao.insertTeacher(teacher)

    suspend fun insertAllTeachers(teachers: List<TeacherProfile>) = teacherDao.insertAllTeachers(teachers)

    suspend fun updateTeacher(teacher: TeacherProfile) = teacherDao.updateTeacher(teacher)

    suspend fun deleteTeacher(teacher: TeacherProfile) = teacherDao.deleteTeacher(teacher)
}
