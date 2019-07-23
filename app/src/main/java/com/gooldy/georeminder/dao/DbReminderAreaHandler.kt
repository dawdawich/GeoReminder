package com.gooldy.georeminder.dao

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.gooldy.georeminder.constants.DATABASE_NAME
import com.gooldy.georeminder.constants.DATABASE_VERSION
import com.gooldy.georeminder.dao.DbAreaHandler.Companion.TABLE_AREA
import com.gooldy.georeminder.dao.DbReminderHandler.Companion.TABLE_REMINDER
import com.gooldy.georeminder.dao.interfaces.IDbReminderAreaHandler
import com.gooldy.georeminder.data.Area
import java.util.*

class DbReminderAreaHandler(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION),
    IDbReminderAreaHandler {

    companion object {
        const val TABLE_REMINDER_AREA = "reminder_area"

        const val KEY_REMINDER_ID = "rId"
        const val KEY_AREA_ID = "aId"
    }

    val reminderForeignIdKey = DbReminderHandler.KEY_REMINDER_ID
    val areaForeignIdKey = DbAreaHandler.KEY_AREA_ID

    override fun onCreate(db: SQLiteDatabase?) {
        val createAreasTableSql = """CREATE TABLE $TABLE_REMINDER_AREA (
            |$KEY_REMINDER_ID    TEXT, 
            |$KEY_AREA_ID        TEXT,
            |FOREIGN KEY($KEY_REMINDER_ID) REFERENCES $TABLE_REMINDER($reminderForeignIdKey),
            |FOREIGN KEY($KEY_AREA_ID) REFERENCES $TABLE_AREA($areaForeignIdKey)
            |)""".trimMargin()
        db?.execSQL(createAreasTableSql)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_REMINDER_AREA")

        onCreate(db)
    }

    override fun addRelation(aId: UUID, rId: UUID) {
        val values = ContentValues().apply {
            put(KEY_REMINDER_ID, rId.toString())
            put(KEY_AREA_ID, aId.toString())
        }

        writableDatabase.use { it.insert(TABLE_REMINDER_AREA, null, values) }
    }

    override fun getAreaIds(rId: UUID): Set<String> {
        readableDatabase.use {
            it.query(TABLE_REMINDER_AREA, arrayOf(KEY_AREA_ID), "$KEY_REMINDER_ID=?", arrayOf(rId.toString()),
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
        }
        return emptySet()
    }

    override fun deleteRelations(rId: UUID) {
        writableDatabase.use { it.delete(TABLE_REMINDER_AREA, "$KEY_REMINDER_ID=?", arrayOf(rId.toString())) }
    }

    override fun deleteRelation(rId: UUID, aId: UUID) {
        writableDatabase.use { it.delete(TABLE_REMINDER_AREA, "$KEY_REMINDER_ID=? AND $KEY_AREA_ID=?", arrayOf(rId.toString(), aId.toString())) }
    }

    private fun getArea(id: String): Area {
        readableDatabase.use {
            it.query(TABLE_AREA, arrayOf(DbAreaHandler.KEY_AREA_ID, DbAreaHandler.KEY_AREA_LATITUDE, DbAreaHandler.KEY_AREA_LONGITUDE, DbAreaHandler.KEY_AREA_RADIUS, DbAreaHandler.KEY_AREA_STREET_NAME),
                "${DbAreaHandler.KEY_AREA_ID}=?", arrayOf(id), null, null, null).use { cursor ->
                cursor.moveToFirst()
                return Area(UUID.fromString(cursor.getString(1)), cursor.getDouble(2),
                    cursor.getDouble(3), cursor.getDouble(4), cursor.getString(5))
            }
        }
    }
}