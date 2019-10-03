package com.gooldy.georeminder.dao.interfaces

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.gooldy.georeminder.data.Area
import java.util.*

@Dao
interface IDbAreaHandler {

    @Insert
    fun addArea(area: Area)
    @Query("SELECT * FROM reminder WHERE id = :id")
    fun getArea(id: UUID): Area?
    @Query("SELECT * FROM reminder")
    fun getAllAreas(): Set<Area>
    @Update
    fun updateArea(area: Area)
    @Delete
    fun deleteArea(id: UUID)
    fun deleteAll()

}