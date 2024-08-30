package com.example.myapplication.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_reminders")
data class DailyReminder (
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val frequency: String,
    val date: String,
    val hour: Int,
    val minute: Int,
    val note: String

    )