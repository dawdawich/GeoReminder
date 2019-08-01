package com.gooldy.georeminder.dao

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.gooldy.georeminder.dao.DatabaseFactory.Companion.KEY_AREA_ID
import com.gooldy.georeminder.dao.DatabaseFactory.Companion.KEY_AREA_LATITUDE
import com.gooldy.georeminder.dao.DatabaseFactory.Companion.KEY_AREA_LONGITUDE
import com.gooldy.georeminder.dao.DatabaseFactory.Companion.KEY_AREA_RADIUS
import com.gooldy.georeminder.dao.DatabaseFactory.Companion.KEY_AREA_STREET_NAME
import com.gooldy.georeminder.dao.DatabaseFactory.Companion.TABLE_AREA
import com.gooldy.georeminder.dao.interfaces.IDbAreaHandler
import com.gooldy.georeminder.data.Area
import java.util.*

class DbAreaHandler constructor(private val wDb: () -> SQLiteDatabase, private val rDb: () -> SQLiteDatabase) : IDbAreaHandler {

    override fun addArea(area: Area) {
        val values = ContentValues().apply {
            put(KEY_AREA_ID, area.id.toString())
            put(KEY_AREA_LATITUDE, area.latitude)
            put(KEY_AREA_LONGITUDE, area.longitude)
            put(KEY_AREA_RADIUS, area.radius)
            put(KEY_AREA_STREET_NAME, area.streetName)
        }

        wDb().insert(TABLE_AREA, null, values)
    }

    override fun getArea(id: UUID): Area? {
        rDb().query(TABLE_AREA, arrayOf(KEY_AREA_ID, KEY_AREA_LATITUDE, KEY_AREA_LONGITUDE, KEY_AREA_RADIUS, KEY_AREA_STREET_NAME),
            "$KEY_AREA_ID=?", arrayOf(id.toString()), null, null, null).use { cursor ->
            if (cursor.moveToFirst()) {
                return Area(UUID.fromString(cursor.getString(0)), cursor.getDouble(1),
                    cursor.getDouble(2), cursor.getDouble(3), cursor.getString(4))
            }
        }

        return null
    }

    override fun getAllAreas(): Set<Area> {
        rDb().query(TABLE_AREA, arrayOf(KEY_AREA_ID, KEY_AREA_LATITUDE, KEY_AREA_LONGITUDE, KEY_AREA_RADIUS, KEY_AREA_STREET_NAME),
            null, null, null, null, null).use { cursor ->
            if (cursor.moveToFirst()) {
                val areas = mutableSetOf<Area>()
                do {
                    areas.add(Area(UUID.fromString(cursor.getString(0)), cursor.getDouble(1),
                        cursor.getDouble(2), cursor.getDouble(3), cursor.getString(4)))
                } while (cursor.moveToNext())
                return areas
            }
        }
        return emptySet()
    }

    override fun updateArea(area: Area) {
        val values = ContentValues().apply {
            put(KEY_AREA_LATITUDE, area.latitude)
            put(KEY_AREA_LONGITUDE, area.longitude)
            put(KEY_AREA_RADIUS, area.radius)
            put(KEY_AREA_STREET_NAME, area.streetName)
        }

        wDb().update(TABLE_AREA, values, "$KEY_AREA_ID=?", arrayOf(area.id.toString()))
    }

    override fun deleteArea(id: UUID) {
        wDb().delete(TABLE_AREA, "$KEY_AREA_ID=?", arrayOf(id.toString()))
    }

    override fun deleteAll() {
        wDb().delete(TABLE_AREA, null, null)
    }

}
