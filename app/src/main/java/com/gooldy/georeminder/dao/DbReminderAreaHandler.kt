package com.gooldy.georeminder.dao

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.gooldy.georeminder.dao.DatabaseFactory.Companion.KEY_AREA_ID
import com.gooldy.georeminder.dao.DatabaseFactory.Companion.KEY_AREA_LATITUDE
import com.gooldy.georeminder.dao.DatabaseFactory.Companion.KEY_AREA_LONGITUDE
import com.gooldy.georeminder.dao.DatabaseFactory.Companion.KEY_AREA_RADIUS
import com.gooldy.georeminder.dao.DatabaseFactory.Companion.KEY_AREA_STREET_NAME
import com.gooldy.georeminder.dao.DatabaseFactory.Companion.KEY_BOUND_AREA_ID
import com.gooldy.georeminder.dao.DatabaseFactory.Companion.KEY_BOUND_REMINDER_ID
import com.gooldy.georeminder.dao.DatabaseFactory.Companion.TABLE_AREA
import com.gooldy.georeminder.dao.DatabaseFactory.Companion.TABLE_REMINDER_AREA
import com.gooldy.georeminder.dao.interfaces.IDbReminderAreaHandler
import com.gooldy.georeminder.data.Area
import java.util.*

class DbReminderAreaHandler constructor(private val wDb: () -> SQLiteDatabase, private val rDb: () -> SQLiteDatabase) : IDbReminderAreaHandler {

    override fun addRelation(aId: UUID, rId: UUID) {
        val values = ContentValues().apply {
            put(KEY_BOUND_REMINDER_ID, rId.toString())
            put(KEY_BOUND_AREA_ID, aId.toString())
        }

        wDb().insert(TABLE_REMINDER_AREA, null, values)
    }

    override fun getAreaIds(rId: UUID): Set<String> {
        rDb().query(TABLE_REMINDER_AREA, arrayOf(KEY_BOUND_AREA_ID), "$KEY_BOUND_REMINDER_ID=?", arrayOf(rId.toString()),
            null, null, null).use { cursor ->
            if (cursor.moveToFirst()) {
                val areas = mutableSetOf<String>()
                do {
                    val areaId = cursor.getString(1)
                    areas.add(cursor.getString(1))
                } while (cursor.moveToNext())
                return areas
            }
        }
        return emptySet()
    }

    override fun deleteRelations(rId: UUID) {
        wDb().delete(TABLE_REMINDER_AREA, "$KEY_BOUND_REMINDER_ID=?", arrayOf(rId.toString()))
    }

    override fun deleteRelation(rId: UUID, aId: UUID) {
        wDb().delete(TABLE_REMINDER_AREA, "$KEY_BOUND_REMINDER_ID=? AND $KEY_BOUND_AREA_ID=?", arrayOf(rId.toString(), aId.toString()))
    }

    private fun getArea(id: String): Area {
        rDb().query(TABLE_AREA, arrayOf(KEY_AREA_ID, KEY_AREA_LATITUDE, KEY_AREA_LONGITUDE, KEY_AREA_RADIUS, KEY_AREA_STREET_NAME),
            "$KEY_AREA_ID=?", arrayOf(id), null, null, null).use { cursor ->
            cursor.moveToFirst()
            return Area(UUID.fromString(cursor.getString(1)), cursor.getDouble(2),
                cursor.getDouble(3), cursor.getDouble(4), cursor.getString(5))
        }
    }
}
