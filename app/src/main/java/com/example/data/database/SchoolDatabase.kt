package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.dao.AttendanceDao
import com.example.data.dao.GradeDao
import com.example.data.dao.GuardianDao
import com.example.data.dao.LessonPlanDao
import com.example.data.dao.PortalUserDao
import com.example.data.dao.SchoolDao
import com.example.data.dao.SchoolEventDao
import com.example.data.dao.AlumniDao
import com.example.data.dao.StudentDao
import com.example.data.dao.TeacherDao
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
        TeacherProfile::class,
        StudentGrade::class,
        UserPortalAccount::class,
        SchoolEvent::class,
        ClassTimetable::class,
        StudentProfile::class,
        LessonPlan::class,
        StudentAddRequest::class,
        StudentFeePayment::class,
        DirectMessage::class,
        TeacherLoanRequest::class,
        DigitalResource::class,
        ClassAssignment::class,
        AlumniProfile::class,
        AlumniChatMessage::class,
        AlumniCallSession::class,
        SchoolPerformanceMetric::class,
        YearlyAdminRegistryEntry::class,
        AlumniAspirant::class,
        AdminVote::class
    ],
    version = 22,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SchoolDatabase : RoomDatabase() {

    abstract fun schoolDao(): SchoolDao
    abstract fun studentDao(): StudentDao
    abstract fun teacherDao(): TeacherDao
    abstract fun guardianDao(): GuardianDao
    abstract fun gradeDao(): GradeDao
    abstract fun portalUserDao(): PortalUserDao
    abstract fun schoolEventDao(): SchoolEventDao
    abstract fun timetableDao(): TimetableDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun lessonPlanDao(): LessonPlanDao
    abstract fun alumniDao(): AlumniDao

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
