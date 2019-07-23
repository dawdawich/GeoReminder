package com.gooldy.georeminder.dao.interfaces

import com.gooldy.georeminder.data.Area
import java.util.*

interface IDbAreaHandler {

    fun addArea(area: Area)
    fun getArea(id: UUID): Area?
    fun getAllAreas(): Set<Area>
    fun updateArea(area: Area)
    fun deleteArea(id: UUID)
    fun deleteAll()

}