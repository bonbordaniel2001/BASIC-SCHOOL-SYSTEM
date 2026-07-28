package com.example.data.database

import androidx.room.TypeConverter
import java.util.Date

/**
 * Type Converters for Room Database
 * Enables Room to persist custom/complex data types (e.g., Date, List<String>, List<Long>, List<Int>)
 * by mapping them to SQLite-supported primitive types (Long, String).
 */
class Converters {

    // --- Date Converters ---
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    // --- String List Converters ---
    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        if (value == null) return null
        return value.joinToString(separator = "|||")
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        if (value == null) return null
        if (value.isEmpty()) return emptyList()
        return value.split("|||")
    }

    // --- Long List Converters ---
    @TypeConverter
    fun fromLongList(value: List<Long>?): String? {
        if (value == null) return null
        return value.joinToString(separator = ",")
    }

    @TypeConverter
    fun toLongList(value: String?): List<Long>? {
        if (value.isNullOrBlank()) return emptyList()
        return value.split(",").mapNotNull { it.trim().toLongOrNull() }
    }

    // --- Int List Converters ---
    @TypeConverter
    fun fromIntList(value: List<Int>?): String? {
        if (value == null) return null
        return value.joinToString(separator = ",")
    }

    @TypeConverter
    fun toIntList(value: String?): List<Int>? {
        if (value.isNullOrBlank()) return emptyList()
        return value.split(",").mapNotNull { it.trim().toIntOrNull() }
    }
}
