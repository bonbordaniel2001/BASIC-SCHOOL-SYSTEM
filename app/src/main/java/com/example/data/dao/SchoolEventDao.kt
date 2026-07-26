package com.example.data.dao

import androidx.room.*
import com.example.data.model.SchoolEvent
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for School Events
 * Handles CRUD operations and filtering for school activities, meetings, and academic milestones.
 */
@Dao
interface SchoolEventDao {

    @Query("SELECT * FROM school_events ORDER BY dateString ASC, timeString ASC")
    fun getAllEvents(): Flow<List<SchoolEvent>>

    @Query("SELECT * FROM school_events WHERE id = :id LIMIT 1")
    fun getEventById(id: Long): Flow<SchoolEvent?>

    @Query("SELECT * FROM school_events WHERE targetAudience = :audience OR targetAudience = 'ALL' ORDER BY dateString ASC")
    fun getEventsByAudience(audience: String): Flow<List<SchoolEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: SchoolEvent): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllEvents(events: List<SchoolEvent>)

    @Update
    suspend fun updateEvent(event: SchoolEvent)

    @Delete
    suspend fun deleteEvent(event: SchoolEvent)
}
