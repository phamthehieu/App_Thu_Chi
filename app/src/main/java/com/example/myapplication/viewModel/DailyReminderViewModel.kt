package com.example.myapplication.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplication.database.CategoryDatabase
import com.example.myapplication.entity.DailyReminder
import com.example.myapplication.repository.DailyReminderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DailyReminderViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: DailyReminderRepository
    val allDailyReminders: LiveData<List<DailyReminder>>

    init {
        val dailyReminderDao = CategoryDatabase.getDatabase(application, viewModelScope).dailyReminderDao()
        repository = DailyReminderRepository(dailyReminderDao)
        allDailyReminders = repository.allDailyReminders.asLiveData()
    }

    fun insertDailyReminder(dailyReminder: DailyReminder) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertDailyReminder(dailyReminder)
        }
    }

    fun updateDailyReminder(dailyReminder: DailyReminder) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateDailyReminder(dailyReminder)
        }
    }

    fun deleteDailyReminder(dailyReminder: DailyReminder) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteDailyReminder(dailyReminder)
        }
    }
}

class DailyReminderViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DailyReminderViewModel::class.java)) {
            return DailyReminderViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}