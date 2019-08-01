package com.gooldy.georeminder.dao

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import androidx.annotation.RequiresApi
import com.gooldy.georeminder.dao.DatabaseFactory.Companion.KEY_REMINDER_CREATED
import com.gooldy.georeminder.dao.DatabaseFactory.Companion.KEY_REMINDER_ID
import com.gooldy.georeminder.dao.DatabaseFactory.Companion.KEY_REMINDER_MODIFIED
import com.gooldy.georeminder.dao.DatabaseFactory.Companion.KEY_REMINDER_NAME
import com.gooldy.georeminder.dao.DatabaseFactory.Companion.KEY_REMINDER_NOTIFIED
import com.gooldy.georeminder.dao.DatabaseFactory.Companion.KEY_REMINDER_TEXT
import com.gooldy.georeminder.dao.DatabaseFactory.Companion.TABLE_REMINDER
import com.gooldy.georeminder.dao.interfaces.IDbReminderHandler
import com.gooldy.georeminder.data.Reminder
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.*

class DbReminderHandler constructor(private val wDb: () -> SQLiteDatabase, private val rDb: () -> SQLiteDatabase) : IDbReminderHandler {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getAllReminders(): Set<Reminder> {
        rDb().query(TABLE_REMINDER, arrayOf(KEY_REMINDER_ID, KEY_REMINDER_NAME, KEY_REMINDER_TEXT, KEY_REMINDER_CREATED,
            KEY_REMINDER_MODIFIED, KEY_REMINDER_NOTIFIED),
            null, null, null, null, null).use { cursor ->
            if (cursor.moveToFirst()) {
                val reminders = mutableSetOf<Reminder>()
                do {
                    reminders.add(Reminder(UUID.fromString(cursor.getString(0)), cursor.getString(1),
                        cursor.getString(2), emptySet(),
                        LocalDateTime.ofInstant(Instant.ofEpochSecond(cursor.getInt(3).toLong()), ZoneId.systemDefault()),
                        LocalDateTime.ofInstant(Instant.ofEpochSecond(cursor.getInt(4).toLong()), ZoneId.systemDefault()),
                        cursor.getInt(5) > 0))
                } while (cursor.moveToNext())
                return reminders
            }
        }
        return emptySet()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getReminder(id: UUID): Reminder? {
        rDb().query(TABLE_REMINDER, arrayOf(KEY_REMINDER_ID, KEY_REMINDER_NAME, KEY_REMINDER_TEXT, KEY_REMINDER_CREATED,
            KEY_REMINDER_MODIFIED, KEY_REMINDER_NOTIFIED),
            "$KEY_REMINDER_ID=?", arrayOf(id.toString()), null, null, null).use { cursor ->
            if (cursor.moveToFirst()) {
                return Reminder(UUID.fromString(cursor.getString(0)), cursor.getString(1),
                    cursor.getString(2), emptySet(),
                    LocalDateTime.ofInstant(Instant.ofEpochSecond(cursor.getInt(3).toLong()), ZoneId.systemDefault()),
                    LocalDateTime.ofInstant(Instant.ofEpochSecond(cursor.getInt(4).toLong()), ZoneId.systemDefault()),
                    cursor.getInt(5) > 0)
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
            put(KEY_REMINDER_NOTIFIED, if (!reminder.notified) 0 else 1)
        }

        wDb().insert(TABLE_REMINDER, null, values)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun updateReminder(reminder: Reminder) {
        val values = ContentValues().apply {
            put(KEY_REMINDER_NAME, reminder.reminderName)
            put(KEY_REMINDER_TEXT, reminder.reminderText)
            put(KEY_REMINDER_MODIFIED, LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
            put(KEY_REMINDER_NOTIFIED, if (!reminder.notified) 0 else 1)
        }

        wDb().update(TABLE_REMINDER, values, "$KEY_REMINDER_ID=?", arrayOf(reminder.id.toString()))
    }

    override fun deleteReminder(id: UUID) {
        wDb().delete(TABLE_REMINDER, "$KEY_REMINDER_ID=?", arrayOf(id.toString()))
    }

    override fun deleteAll() {
        wDb().delete(TABLE_REMINDER, null, null)
    }

}
