package com.example.data.dao

import androidx.room.*
import com.example.data.model.ClassTimetable
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Class Timetables
 * Handles queries for viewing class schedules by class name, teacher, and day of week.
 */
@Dao
interface TimetableDao {

    @Query("SELECT * FROM class_timetables ORDER BY className ASC, dayOfWeek ASC, periodNumber ASC")
    fun getAllTimetables(): Flow<List<ClassTimetable>>

    @Query("SELECT * FROM class_timetables WHERE className = :className ORDER BY dayOfWeek ASC, periodNumber ASC")
    fun getTimetableForClass(className: String): Flow<List<ClassTimetable>>

    @Query("SELECT * FROM class_timetables WHERE className = :className AND dayOfWeek = :dayOfWeek ORDER BY periodNumber ASC")
    fun getTimetableForClassAndDay(className: String, dayOfWeek: String): Flow<List<ClassTimetable>>

    @Query("SELECT * FROM class_timetables WHERE teacherName = :teacherName OR (teacherId != 0 AND teacherId = :teacherId) ORDER BY dayOfWeek ASC, periodNumber ASC")
    fun getTimetableForTeacher(teacherName: String, teacherId: Long = 0): Flow<List<ClassTimetable>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimetableSlot(slot: ClassTimetable): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTimetables(slots: List<ClassTimetable>)

    @Update
    suspend fun updateTimetableSlot(slot: ClassTimetable)

    @Delete
    suspend fun deleteTimetableSlot(slot: ClassTimetable)
}
