package com.gooldy.georeminder.dao.interfaces

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gooldy.georeminder.dao.entites.Reminder
import java.util.*

@Dao
interface ReminderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertReminder(reminder: Reminder)

    @Update
    fun updateReminder(reminder: Reminder)

    @Delete
    fun deleteReminder(reminder: Reminder)

    @Query("SELECT * FROM reminders")
    fun getReminders() : List<Reminder>

    @Query("SELECT * FROM reminders WHERE active > 0")
    fun getAllActiveReminders() : List<Reminder>

    @Query("SELECT * FROM reminders WHERE id = :id")
    fun getReminder(id: UUID) : Reminder
}