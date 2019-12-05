package com.gooldy.georeminder.dao.converters

import androidx.room.TypeConverter
import java.time.Instant
import java.util.*

class Converter {

    @TypeConverter
    fun fromString(uuid: String?) : UUID? {
        return uuid?.let { UUID.fromString(it) }
    }

    @TypeConverter
    fun toString(uuid: UUID?) : String? {
        return uuid?.toString()
    }

    @TypeConverter
    fun fromTimestamp(value: Long?): Instant? {
        return if (value == null) null else Instant.ofEpochMilli(value)
    }

    @TypeConverter
    fun dateToTimestamp(date: Instant?): Long? {
        return date?.toEpochMilli()
    }

}