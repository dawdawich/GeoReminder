package com.gooldy.georeminder.data

import java.io.Serializable
import java.util.*

data class Area(val id: UUID, val latitude: Double, val longitude: Double, val radius: Double, val streetName: String) :
    Serializable
