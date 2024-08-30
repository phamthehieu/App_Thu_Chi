package com.example.myapplication.repository

import androidx.annotation.WorkerThread
import com.example.myapplication.dao.DailyReminderDao
import com.example.myapplication.entity.DailyReminder
import kotlinx.coroutines.flow.Flow

class DailyReminderRepository(private val dailyReminderDao: DailyReminderDao) {
    val allDailyReminders: Flow<List<DailyReminder>> = dailyReminderDao.getAllDailyReminder()

    @WorkerThread
    fun insertDailyReminder(dailyReminder: DailyReminder) {
        dailyReminderDao.insertDailyReminder(dailyReminder)
    }

}