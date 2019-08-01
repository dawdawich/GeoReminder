package com.gooldy.georeminder.dao

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.gooldy.georeminder.dao.DatabaseFactory.Companion.KEY_BOUND_AREA_ID
import com.gooldy.georeminder.dao.DatabaseFactory.Companion.KEY_BOUND_REMINDER_ID
import com.gooldy.georeminder.dao.DatabaseFactory.Companion.TABLE_REMINDER_AREA
import com.gooldy.georeminder.dao.interfaces.IDbReminderAreaHandler
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
                    areas.add(cursor.getString(0))
                } while (cursor.moveToNext())
                return areas
            }
        }
        return emptySet()
    }

    override fun getRemindersByAreaId(aId: UUID): Set<String> {
        rDb().query(TABLE_REMINDER_AREA, arrayOf(KEY_BOUND_REMINDER_ID), "$KEY_BOUND_AREA_ID=?", arrayOf(aId.toString()),
            null, null, null).use { cursor ->
            if (cursor.moveToFirst()) {
                val reminders = mutableSetOf<String>()
                do {
                    reminders.add(cursor.getString(0))
                } while (cursor.moveToNext())
                return reminders
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

    override fun getAllAreaIds(): Set<String> {
        rDb().query(TABLE_REMINDER_AREA, arrayOf(KEY_BOUND_AREA_ID), null, null, null,
            null, null).use { cursor ->
            if (cursor.moveToFirst()) {
                val ids = mutableSetOf<String>()
                do {
                    ids.add(cursor.getString(0))
                } while (cursor.moveToNext())
                return ids
            }
        }
        return emptySet()
    }
}
