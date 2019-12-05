package com.gooldy.georeminder.dao.interfaces

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.gooldy.georeminder.dao.entites.Area
import java.util.*

@Dao
interface AreaDao {

    @Insert
    fun addArea(area: Area)

    @Update
    fun updateArea(area: Area)

    @Delete
    fun deleteArea(area: Area)

    @Query("SELECT * FROM areas")
    fun getAreas(): List<Area>

    @Query("SELECT * FROM areas WHERE id = :id")
    fun getArea(id: UUID): Area?

    @Query("SELECT * FROM areas WHERE reminderId = :id")
    fun getAreasByReminderId(id: UUID) : List<Area>

}
