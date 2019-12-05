package com.gooldy.georeminder.dao.entites

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.gooldy.georeminder.dao.converters.Converter
import java.io.Serializable
import java.util.*

@Entity(foreignKeys = [ForeignKey(
    entity = Reminder::class,
    parentColumns = ["id"],
    childColumns = ["reminderId"],
    onDelete = CASCADE)], tableName = "areas")
data class Area(
    @PrimaryKey
    @NonNull
    @TypeConverters(Converter::class)
    var id: UUID,

    @NonNull
    var latitude: Double,

    @NonNull
    var longitude: Double,

    @NonNull
    var radius: Double,

    var streetName: String,

    @TypeConverters(Converter::class)
    @NonNull var reminderId: UUID?
) : Serializable
