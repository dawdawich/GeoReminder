package com.gooldy.georeminder.data

import java.io.Serializable
import java.time.Instant
import java.util.*

data class Reminder(val id: UUID, var reminderName: String, var reminderText: String, var reminderAreas: MutableSet<Area>,
                    var createDate: Instant?, var modifyTime: Instant?, var repeatable: Boolean, var isActive: Boolean,
                    var notified: Boolean = false) : Serializable {
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