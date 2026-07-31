package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Student Profile Entity
 * Represents comprehensive student information including personal details, class placement,
 * guardian relationship, index number, and enrollment status.
 */
@Entity(tableName = "student_profiles")
data class StudentProfile(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val indexNumber: String, // e.g. "AKM/2025/082"
    val className: String, // e.g. "JHS 2 - Gold", "Primary 4"
    val dateOfBirth: String = "2012-05-14",
    val gender: String = "Male", // "Male", "Female"
    val guardianName: String,
    val guardianPhone: String,
    val guardianEmail: String = "",
    val residentialAddress: String = "Sibi, Oti Region, Ghana",
    val enrollmentStatus: String = "ACTIVE", // "ACTIVE", "GRADUATED", "SUSPENDED", "TRANSFERRED"
    val admissionDate: String = "2023-09-10"
) {
    /**
     * Converts entity to Firestore Document Map format
     */
    fun toFirestoreMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "fullName" to fullName,
            "indexNumber" to indexNumber,
            "className" to className,
            "dateOfBirth" to dateOfBirth,
            "gender" to gender,
            "guardianName" to guardianName,
            "guardianPhone" to guardianPhone,
            "guardianEmail" to guardianEmail,
            "residentialAddress" to residentialAddress,
            "enrollmentStatus" to enrollmentStatus,
            "admissionDate" to admissionDate
        )
    }

    companion object {
        /**
         * Creates StudentProfile instance from Firestore Document Map data
         */
        fun fromFirestoreMap(map: Map<String, Any?>, fallbackId: Long = 0L): StudentProfile {
            return StudentProfile(
                id = (map["id"] as? Number)?.toLong() ?: fallbackId,
                fullName = map["fullName"] as? String ?: "Unknown Student",
                indexNumber = map["indexNumber"] as? String ?: "N/A",
                className = map["className"] as? String ?: "Unassigned",
                dateOfBirth = map["dateOfBirth"] as? String ?: "2012-05-14",
                gender = map["gender"] as? String ?: "Male",
                guardianName = map["guardianName"] as? String ?: "N/A",
                guardianPhone = map["guardianPhone"] as? String ?: "N/A",
                guardianEmail = map["guardianEmail"] as? String ?: "",
                residentialAddress = map["residentialAddress"] as? String ?: "Ghana",
                enrollmentStatus = map["enrollmentStatus"] as? String ?: "ACTIVE",
                admissionDate = map["admissionDate"] as? String ?: "2023-09-10"
            )
        }
    }
}
