package com.gooldy.georeminder.dao

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.gooldy.georeminder.constants.DATABASE_NAME
import com.gooldy.georeminder.dao.converters.Converter
import com.gooldy.georeminder.dao.entites.Area
import com.gooldy.georeminder.dao.entites.Reminder
import com.gooldy.georeminder.dao.interfaces.AreaDao
import com.gooldy.georeminder.dao.interfaces.ReminderDao

@Database(entities = [Area::class, Reminder::class], version = 1)
@TypeConverters(Converter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun areaDao() : AreaDao
    abstract fun reminderDao() : ReminderDao

    companion object {
        var INSTANCE : AppDatabase? = null

        fun getAppDatabase(context: Context) : AppDatabase {
            if (INSTANCE == null) {
                synchronized(AppDatabase::class) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                        AppDatabase::class.java, DATABASE_NAME).build()
                }
            }
            return INSTANCE!!
        }

        fun destroyDatabase() {
            INSTANCE = null
        }
    }
}
