package com.example.bookclub.data.db

import androidx.room.TypeConverter
import com.example.bookclub.data.model.ClubStatus
import java.time.Instant

// TypeConverters Room: transformari pentru Instant si ClubStatus
// convertori folositi de Room pentru tipuri non-primitive
class Converters {

    // Instant ↔ Long
    @TypeConverter
    fun fromInstant(value: Instant?): Long? = value?.toEpochMilli()

    @TypeConverter
    fun toInstant(value: Long?): Instant? =
        value?.let { Instant.ofEpochMilli(it) }

    // ClubStatus ↔ String
    @TypeConverter
    fun fromStatus(status: ClubStatus?): String? = status?.name

    @TypeConverter
    fun toStatus(value: String?): ClubStatus? =
        value?.let { ClubStatus.valueOf(it) }
}
