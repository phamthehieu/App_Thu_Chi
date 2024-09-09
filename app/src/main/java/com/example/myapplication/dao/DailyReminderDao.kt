package com.example.myapplication.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.myapplication.entity.DailyReminder
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyReminderDao {

    @Transaction
    @Query("SELECT * FROM daily_reminders")
    fun getAllDailyReminder(): Flow<List<DailyReminder>>

    @Insert
    fun insertDailyReminder(dailyReminder: DailyReminder)

    @Update
    fun updateDailyReminder(dailyReminder: DailyReminder)

    @Delete
    fun deleteDailyReminder(dailyReminder: DailyReminder)
}