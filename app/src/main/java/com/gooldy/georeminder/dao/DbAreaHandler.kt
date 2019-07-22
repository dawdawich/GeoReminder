package com.gooldy.georeminder.dao

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.gooldy.georeminder.data.Area
import java.util.*

class DbAreaHandler(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION),
    IDbAreaHandler {

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "GeoReminderDb"
        const val TABLE_AREA = "areas"

        const val KEY_ID = "id"

        const val KEY_LATITUDE = "latitude"
        const val KEY_LONGITUDE = "longitude"
        const val KEY_RADIUS = "radius"
        const val KEY_STREET_NAME = "street_name"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createAreasTableSql = """CREATE TABLE $TABLE_AREA (
            |$KEY_ID             TEXT PRIMARY KEY, 
            |$KEY_LATITUDE       DOUBLE NOT NULL, 
            |$KEY_LONGITUDE      DOUBLE NOT NULL, 
            |$KEY_RADIUS         DOUBLE NOT NULL, 
            |$KEY_STREET_NAME    TEXT NOT NULL
            |)""".trimMargin()
        db?.execSQL(createAreasTableSql)
    }


    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_AREA")

        onCreate(db)
    }

    override fun addArea(area: Area) {
        val values = ContentValues().apply {
            put(KEY_ID, area.id.toString())
            put(KEY_LATITUDE, area.latitude)
            put(KEY_LONGITUDE, area.longitude)
            put(KEY_RADIUS, area.radius)
            put(KEY_STREET_NAME, area.streetName)
        }

        writableDatabase.use { it.insert(TABLE_AREA, null, values) }
    }

    override fun getArea(id: UUID): Area {
        readableDatabase.use {
            it.query(
                TABLE_AREA, arrayOf(KEY_ID, KEY_LATITUDE, KEY_LONGITUDE, KEY_RADIUS, KEY_STREET_NAME),
                "$KEY_ID=?", arrayOf(id.toString()), null, null, null
            ).use { cursor ->
                cursor.moveToFirst()
                return Area(
                    UUID.fromString(cursor.getString(1)), cursor.getDouble(2),
                    cursor.getDouble(3), cursor.getDouble(4), cursor.getString(5)
                )
            }
        }
    }

    override fun getAllAreas(): Set<Area> {
        readableDatabase.use {
            it.query(
                TABLE_AREA, arrayOf(KEY_ID, KEY_LATITUDE, KEY_LONGITUDE, KEY_RADIUS, KEY_STREET_NAME),
                null, null, null, null, null
            ).use { cursor ->
                if (cursor.moveToFirst()) {
                    val areas = mutableSetOf<Area>()
                    do {
                        areas.add(
                            Area(
                                UUID.fromString(cursor.getString(1)), cursor.getDouble(2),
                                cursor.getDouble(3), cursor.getDouble(4), cursor.getString(5)
                            )
                        )
                    } while (cursor.moveToNext())
                    return areas
                }
            }
        }
        return emptySet()
    }

    override fun updateArea(area: Area) {
        val values = ContentValues().apply {
            put(KEY_LATITUDE, area.latitude)
            put(KEY_LONGITUDE, area.longitude)
            put(KEY_RADIUS, area.radius)
            put(KEY_STREET_NAME, area.streetName)
        }

        writableDatabase.use { it.update(TABLE_AREA, values, "$KEY_ID=?", arrayOf(area.id.toString())) }
    }

    override fun deleteArea(id: UUID) {
        writableDatabase.use { it.delete(TABLE_AREA, "$KEY_ID=?", arrayOf(id.toString())) }
    }

    override fun deleteAll() {
        writableDatabase.use { it.delete(TABLE_AREA, null, null) }
    }

}