package com.gooldy.georeminder.data

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.PrimaryKey
import java.io.Serializable
import java.util.*

@Entity(foreignKeys = [ForeignKey(
    entity = Reminder::class,
    parentColumns = ["id"],
    childColumns = ["reminderId"],
    onDelete = CASCADE)])
data class Area(@PrimaryKey @NonNull val id: UUID,
                @NonNull var latitude: Double,
                @NonNull var longitude: Double, @NonNull var radius: Double,
                var streetName: String,
                @NonNull val reminderId: UUID
) : Serializable
