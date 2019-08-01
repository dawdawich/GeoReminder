package com.gooldy.georeminder.dao

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.gooldy.georeminder.constants.DATABASE_NAME
import com.gooldy.georeminder.constants.DATABASE_VERSION
import com.gooldy.georeminder.dao.interfaces.IDbAreaHandler
import com.gooldy.georeminder.dao.interfaces.IDbReminderAreaHandler
import com.gooldy.georeminder.dao.interfaces.IDbReminderHandler

class DatabaseFactory private constructor(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        // -------- Area ---------
        const val TABLE_AREA = "areas"

        const val KEY_AREA_ID = "id"
        const val KEY_AREA_LATITUDE = "latitude"
        const val KEY_AREA_LONGITUDE = "longitude"
        const val KEY_AREA_RADIUS = "radius"
        const val KEY_AREA_STREET_NAME = "street_name"
        // -----------------------

        // -------- Reminder -----
        const val TABLE_REMINDER = "reminders"

        const val KEY_REMINDER_ID = "id"
        const val KEY_REMINDER_NAME = "name"
        const val KEY_REMINDER_TEXT = "text"
        const val KEY_REMINDER_CREATED = "created"
        const val KEY_REMINDER_MODIFIED = "modified"
        const val KEY_REMINDER_NOTIFIED = "notified"
        // -----------------------


        // -------- ReminderArea -
        const val TABLE_REMINDER_AREA = "reminder_area"

        const val KEY_BOUND_REMINDER_ID = "rId"
        const val KEY_BOUND_AREA_ID = "aId"
        // -----------------------


        fun getInstance(context: Context): DatabaseFactory {
            return DatabaseFactory(context)
        }
    }

    private val daoArea: IDbAreaHandler
    private val daoReminder: IDbReminderHandler
    private val daoReminderArea: IDbReminderAreaHandler

    private var wDb: SQLiteDatabase? = null
    private var rDb: SQLiteDatabase? = null

    init {
        daoArea = DbAreaHandler({ wDb!! }, { rDb!! })
        daoReminder = DbReminderHandler({ wDb!! }, { rDb!! })
        daoReminderArea = DbReminderAreaHandler({ wDb!! }, { rDb!! })
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createAreasTableSql = """CREATE TABLE $TABLE_AREA (
            |$KEY_AREA_ID             TEXT PRIMARY KEY, 
            |$KEY_AREA_LATITUDE       DOUBLE NOT NULL, 
            |$KEY_AREA_LONGITUDE      DOUBLE NOT NULL, 
            |$KEY_AREA_RADIUS         DOUBLE NOT NULL, 
            |$KEY_AREA_STREET_NAME    TEXT NOT NULL
            |);
            |""".trimMargin()
        val createReminderTableSql = """CREATE TABLE $TABLE_REMINDER (
            |$KEY_REMINDER_ID             TEXT PRIMARY KEY, 
            |$KEY_REMINDER_NAME           TEXT NOT NULL, 
            |$KEY_REMINDER_TEXT           TEXT NOT NULL, 
            |$KEY_REMINDER_CREATED        INTEGER NOT NULL, 
            |$KEY_REMINDER_MODIFIED       INTEGER NOT NULL,
            |$KEY_REMINDER_NOTIFIED       INTEGER NOT NULL
            |);
            |""".trimMargin()
        val createReminderAreaTableSql = """CREATE TABLE $TABLE_REMINDER_AREA (
            |$KEY_BOUND_REMINDER_ID    TEXT, 
            |$KEY_BOUND_AREA_ID        TEXT,
            |FOREIGN KEY($KEY_BOUND_REMINDER_ID) REFERENCES $TABLE_REMINDER($KEY_REMINDER_ID),
            |FOREIGN KEY($KEY_BOUND_AREA_ID) REFERENCES $TABLE_AREA($KEY_AREA_ID)
            |);
            |""".trimMargin()

        db?.execSQL(createAreasTableSql)
        db?.execSQL(createReminderTableSql)
        db?.execSQL(createReminderAreaTableSql)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        val dropAreaReminder = "DROP TABLE IF EXISTS $TABLE_REMINDER_AREA"
        val dropReminder = "DROP TABLE IF EXISTS $TABLE_REMINDER"
        val dropArea = "DROP TABLE IF EXISTS $TABLE_AREA"
        db?.execSQL(dropAreaReminder)
        db?.execSQL(dropReminder)
        db?.execSQL(dropArea)

        onCreate(db)
    }

    fun getAreaDao(areaDao: (IDbAreaHandler) -> Unit) {
        wDb = writableDatabase
        rDb = readableDatabase
        areaDao(daoArea)
        wDb?.close()
        rDb?.close()
    }

    fun getReminderDao(reminderDao: (IDbReminderHandler) -> Unit) {
        wDb = writableDatabase
        rDb = readableDatabase
        reminderDao(daoReminder)
        wDb?.close()
        rDb?.close()
    }

    fun getReminderAreaDao(reminderAreaDao: (IDbReminderAreaHandler) -> Unit) {
        wDb = writableDatabase
        rDb = readableDatabase
        reminderAreaDao(daoReminderArea)
        wDb?.close()
        rDb?.close()
    }

    fun inTransaction(doInTransaction: (IDbAreaHandler, IDbReminderHandler, IDbReminderAreaHandler) -> Unit) {
        wDb = writableDatabase
        rDb = readableDatabase
        writableDatabase.beginTransaction()
        try {
            doInTransaction(daoArea, daoReminder, daoReminderArea)
            writableDatabase.setTransactionSuccessful()
        } finally {
            writableDatabase.endTransaction()
            writableDatabase.close()
        }
    }
}
