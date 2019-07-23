package com.gooldy.georeminder.dao

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Build
import androidx.annotation.RequiresApi
import com.gooldy.georeminder.constants.DATABASE_NAME
import com.gooldy.georeminder.constants.DATABASE_VERSION
import com.gooldy.georeminder.dao.interfaces.IDbReminderHandler
import com.gooldy.georeminder.data.Reminder
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.*

class DbReminderHandler(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION), IDbReminderHandler {

    companion object {
        const val TABLE_REMINDER = "reminders"

        const val KEY_REMINDER_ID = "id"
        const val KEY_REMINDER_NAME = "name"
        const val KEY_REMINDER_TEXT = "text"
        const val KEY_REMINDER_CREATED = "created"
        const val KEY_REMINDER_MODIFIED = "modified"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createAreasTableSql = """CREATE TABLE $TABLE_REMINDER (
            |$KEY_REMINDER_ID             TEXT PRIMARY KEY, 
            |$KEY_REMINDER_NAME           TEXT NOT NULL, 
            |$KEY_REMINDER_TEXT           TEXT NOT NULL, 
            |$KEY_REMINDER_CREATED        INTEGER NOT NULL, 
            |$KEY_REMINDER_MODIFIED       INTEGER NOT NULL
            |)""".trimMargin()
        db?.execSQL(createAreasTableSql)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_REMINDER")

        onCreate(db)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getAllReminders(): Set<Reminder> {
        readableDatabase.use {
            it.query(TABLE_REMINDER, arrayOf(KEY_REMINDER_ID, KEY_REMINDER_NAME, KEY_REMINDER_TEXT, KEY_REMINDER_CREATED, KEY_REMINDER_MODIFIED),
                null, null, null, null, null).use { cursor ->
                if (cursor.moveToFirst()) {
                    val areas = mutableSetOf<Reminder>()
                    do {
                        areas.add(Reminder(UUID.fromString(cursor.getString(1)), cursor.getString(2),
                            cursor.getString(3), emptySet(),
                            LocalDateTime.ofInstant(Instant.ofEpochSecond(cursor.getInt(4).toLong()), ZoneId.systemDefault()),
                            LocalDateTime.ofInstant(Instant.ofEpochSecond(cursor.getInt(5).toLong()), ZoneId.systemDefault())))
                    } while (cursor.moveToNext())
                    return areas
                }
            }
        }
        return emptySet()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getReminder(id: UUID): Reminder? {
        readableDatabase.use {
            it.query(TABLE_REMINDER, arrayOf(KEY_REMINDER_ID, KEY_REMINDER_NAME, KEY_REMINDER_TEXT, KEY_REMINDER_CREATED, KEY_REMINDER_MODIFIED),
                "$KEY_REMINDER_ID=?", arrayOf(id.toString()), null, null, null).use { cursor ->
                if (cursor.moveToFirst()) {
                    return Reminder(UUID.fromString(cursor.getString(1)), cursor.getString(2),
                        cursor.getString(3), emptySet(),
                        LocalDateTime.ofInstant(Instant.ofEpochSecond(cursor.getInt(4).toLong()), ZoneId.systemDefault()),
                        LocalDateTime.ofInstant(Instant.ofEpochSecond(cursor.getInt(5).toLong()), ZoneId.systemDefault()))
                }
            }
        }
        return null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun addReminder(reminder: Reminder) {
        val values = ContentValues().apply {
            put(KEY_REMINDER_ID, reminder.id.toString())
            put(KEY_REMINDER_NAME, reminder.reminderName)
            put(KEY_REMINDER_TEXT, reminder.reminderText)
            put(KEY_REMINDER_CREATED, LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
            put(KEY_REMINDER_MODIFIED, LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
        }

        writableDatabase.use { it.insert(TABLE_REMINDER, null, values) }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun updateReminder(reminder: Reminder) {
        val values = ContentValues().apply {
            put(KEY_REMINDER_NAME, reminder.reminderName)
            put(KEY_REMINDER_TEXT, reminder.reminderText)
            put(KEY_REMINDER_MODIFIED, LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
        }

        writableDatabase.use { it.update(TABLE_REMINDER, values, "$KEY_REMINDER_ID=?", arrayOf(reminder.id.toString())) }
    }

    override fun deleteReminder(id: UUID) {
        writableDatabase.use { it.delete(TABLE_REMINDER, "$KEY_REMINDER_ID=?", arrayOf(id.toString())) }
    }

    override fun deleteAll() {
        writableDatabase.use { it.delete(TABLE_REMINDER, null, null) }
    }

}
