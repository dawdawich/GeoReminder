package com.gooldy.georeminder.service

import android.content.Context
import androidx.room.Transaction
import com.gooldy.georeminder.dao.AppDatabase
import com.gooldy.georeminder.dao.entites.Area
import com.gooldy.georeminder.dao.entites.Reminder
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*

class MainService(context: Context) {

    private val db: AppDatabase = AppDatabase.getAppDatabase(context)

    private val reminderDao = db.reminderDao()
    private val areaDao = db.areaDao()

    companion object {
        fun <T> observeOn(observer: () -> Observable<T>) {
            observer().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()
        }
    }

    @Transaction
    fun saveReminderWithAreas(reminder: Reminder, areas: Set<Area>) {
        reminderDao.insertReminder(reminder)
        areas.forEach {
            areaDao.addArea(it)
        }
    }

    fun updateReminder(reminder: Reminder) {
        reminderDao.updateReminder(reminder)
    }

    fun getAllReminders(): Set<Reminder> {
        return reminderDao.getReminders().toHashSet()
    }

    fun getAllActiveReminders(): Set<Reminder> {
        return reminderDao.getAllActiveReminders().toHashSet()
    }

    fun removeReminder(reminder: Reminder) {
        reminderDao.deleteReminder(reminder)
    }

    fun getAreas(reminderId: UUID) : Set<Area> {
        return areaDao.getAreasByReminderId(reminderId).toHashSet()
    }

    fun getAreasByReminderIds(reminderIds: List<UUID>): Set<Area> {
        return areaDao.getAreasByReminderIds(reminderIds).toHashSet()
    }
}
