package com.gooldy.georeminder.data

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.util.*

@Entity data class Area(@PrimaryKey @NonNull val id: UUID, var latitude: Double, var longitude: Double, var radius: Double, var streetName: String)
    : Serializable
