package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.AttendanceDao
import com.example.data.dao.GradeDao
import com.example.data.dao.GuardianDao
import com.example.data.dao.LessonPlanDao
import com.example.data.dao.PortalUserDao
import com.example.data.dao.SchoolDao
import com.example.data.dao.SchoolEventDao
import com.example.data.dao.TimetableDao
import com.example.data.model.*

@Database(
    entities = [
        StaffMember::class,
        TransactionApproval::class,
        MessageLog::class,
        AttendanceRecord::class,
        ClockInLog::class,
        StudentLedger::class,
        FeeTransaction::class,
        UserAccount::class,
        AppNotification::class,
        SchoolSettings::class,
        DailyStudentAttendance::class,
        GuardianProfile::class,
        StudentGrade::class,
        UserPortalAccount::class,
        SchoolEvent::class,
        ClassTimetable::class,
        StudentProfile::class,
        LessonPlan::class,
        StudentAddRequest::class
    ],
    version = 12,
    exportSchema = false
)
abstract class SchoolDatabase : RoomDatabase() {

    abstract fun schoolDao(): SchoolDao
    abstract fun guardianDao(): GuardianDao
    abstract fun gradeDao(): GradeDao
    abstract fun portalUserDao(): PortalUserDao
    abstract fun schoolEventDao(): SchoolEventDao
    abstract fun timetableDao(): TimetableDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun lessonPlanDao(): LessonPlanDao

    companion object {
        @Volatile
        private var INSTANCE: SchoolDatabase? = null

        fun getDatabase(context: Context): SchoolDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SchoolDatabase::class.java,
                    "akoma_school_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
