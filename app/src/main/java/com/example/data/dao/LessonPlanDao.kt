package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.LessonPlan
import kotlinx.coroutines.flow.Flow

@Dao
interface LessonPlanDao {
    @Query("SELECT * FROM lesson_plans ORDER BY createdAt DESC")
    fun getAllLessonPlans(): Flow<List<LessonPlan>>

    @Query("SELECT * FROM lesson_plans WHERE teacherId = :teacherId ORDER BY createdAt DESC")
    fun getLessonPlansForTeacher(teacherId: Long): Flow<List<LessonPlan>>

    @Query("SELECT * FROM lesson_plans WHERE className = :className AND subject = :subject ORDER BY createdAt DESC")
    fun getLessonPlansByClassAndSubject(className: String, subject: String): Flow<List<LessonPlan>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLessonPlan(plan: LessonPlan): Long

    @Update
    suspend fun updateLessonPlan(plan: LessonPlan)

    @Query("UPDATE lesson_plans SET status = :status, managementFeedback = :feedback WHERE id = :id")
    suspend fun updateLessonPlanStatus(id: Long, status: String, feedback: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllLessonPlans(plans: List<LessonPlan>)
}
