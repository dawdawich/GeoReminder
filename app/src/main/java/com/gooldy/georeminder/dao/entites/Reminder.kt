package com.gooldy.georeminder.dao.entites

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.time.Instant
import java.util.*

@Entity(tableName = "reminders") data class Reminder(@PrimaryKey @NonNull var id: UUID = UUID.randomUUID(),
                                                     @NonNull var reminderName: String? = null,
                                                     var reminderText: String? = null,
                                                     @NonNull var createDate: Instant? = null,
                                                     @NonNull var modifyTime: Instant? = null,
                                                     @NonNull var repeatable: Boolean? = null,
                                                     @NonNull var active: Boolean? = null,
                                                     @NonNull var notified: Boolean = false,
                                                     @Transient var areas: MutableSet<Area>? = null
) : Serializable {

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