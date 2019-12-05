package com.gooldy.georeminder.dao.entites

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.gooldy.georeminder.dao.converters.Converter
import java.io.Serializable
import java.time.Instant
import java.util.*

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey
    @NonNull
    @TypeConverters(Converter::class)
    val id: UUID,

    @NonNull
    var reminderName: String,

    var reminderText: String,

    @NonNull
    val createDate: Instant,

    @NonNull
    var modifyTime: Instant,

    @NonNull
    var repeatable: Boolean,

    @NonNull
    var active: Boolean,

    @NonNull
    var notified: Boolean
) : Serializable {

    @Transient
    var areas: MutableSet<Area> = mutableSetOf()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Reminder

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}