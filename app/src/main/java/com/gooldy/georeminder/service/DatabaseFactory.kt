package com.gooldy.georeminder.service

import android.content.Context
import com.gooldy.georeminder.dao.DbAreaHandler
import com.gooldy.georeminder.dao.DbReminderAreaHandler
import com.gooldy.georeminder.dao.DbReminderHandler
import com.gooldy.georeminder.dao.interfaces.IDbAreaHandler
import com.gooldy.georeminder.dao.interfaces.IDbReminderAreaHandler
import com.gooldy.georeminder.dao.interfaces.IDbReminderHandler

class DatabaseFactory private constructor() {

    lateinit var daoArea: IDbAreaHandler
        private set
    lateinit var daoReminder: IDbReminderHandler
        private set
    lateinit var daoReminderArea: IDbReminderAreaHandler
        private set

    fun getInstance(context: Context): DatabaseFactory {
        return DatabaseFactory().apply {
            daoArea = DbAreaHandler(context)
            daoReminder = DbReminderHandler(context)
            daoReminderArea = DbReminderAreaHandler(context)
        }
    }

}