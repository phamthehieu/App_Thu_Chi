package com.example.myapplication.viewModel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.database.CategoryDatabase
import com.example.myapplication.entity.IncomeExpenseList
import com.example.myapplication.repository.IncomeExpenseListRepository
import kotlinx.coroutines.launch

class IncomeExpenseListModel(application: Context) : AndroidViewModel(application as Application) {
    private val repository: IncomeExpenseListRepository

    init {
        val incomeExpenseDao = CategoryDatabase.getDatabase(application, viewModelScope).incomeExpenseListDao()
        repository = IncomeExpenseListRepository(incomeExpenseDao)
    }

    fun insert(incomeExpenseList: IncomeExpenseList) {
        viewModelScope.launch {
            repository.insert(incomeExpenseList)
        }
    }

}

class IncomeExpenseListFactory(private val application: Context) : ViewModelProvider.Factory{
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(IncomeExpenseListModel::class.java)) {
            return IncomeExpenseListModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}