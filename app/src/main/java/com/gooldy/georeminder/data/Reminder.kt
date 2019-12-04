package com.gooldy.georeminder.data

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.time.Instant
import java.util.*

@Entity
data class Reminder(@PrimaryKey val id: UUID,
                    @NonNull var reminderName: String,
                    var reminderText: String,
                    var reminderAreas: MutableSet<Area>,
                    @NonNull var createDate: Instant?,
                    @NonNull var modifyTime: Instant?,
                    @NonNull var repeatable: Boolean,
                    @NonNull var isActive: Boolean,
                    @NonNull var notified: Boolean = false
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