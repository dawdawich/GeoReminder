package com.gooldy.georeminder.data

import java.io.Serializable
import java.util.*

data class Area(val id: UUID, var latitude: Double, var longitude: Double, var radius: Double, var streetName: String) :
    Serializable
