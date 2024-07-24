package com.example.myapplication.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.CategoryWithIncomeExpenseList
import com.example.myapplication.database.CategoryDatabase
import com.example.myapplication.entity.IncomeExpenseList
import com.example.myapplication.repository.IncomeExpenseListRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class IncomeExpenseListModel(application: Application) : AndroidViewModel(application) {
    private val repository: IncomeExpenseListRepository
    val allIncomeExpense: LiveData<List<IncomeExpenseList>>
    val filteredIncomeExpenseList: LiveData<List<IncomeExpenseList>> = MutableLiveData()

    init {
        val incomeExpenseDao =
            CategoryDatabase.getDatabase(application, viewModelScope).incomeExpenseListDao()
        repository = IncomeExpenseListRepository(incomeExpenseDao)
        allIncomeExpense = repository.allIncomeExpenseList.asLiveData()
    }

    fun insert(incomeExpenseList: IncomeExpenseList): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.insert(incomeExpenseList)
                result.postValue(true)
            } catch (e: Exception) {
                result.postValue(false)
            }
        }
        return result
    }

    fun deleteIncomeExpenseListModel(incomeExpenseList: IncomeExpenseList) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteIncomeExpenseList(incomeExpenseList)
        }
    }

    fun updateIncomeExpenseListModel(incomeExpenseList: IncomeExpenseList): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.updateIncomeExpenseList(incomeExpenseList)
                result.postValue(true)
            } catch (e: Exception) {
                result.postValue(false)
            }
        }
        return result
    }

    fun getIncomeExpenseListByMonthYear(
        year: String,
        month: String
    ): LiveData<List<CategoryWithIncomeExpenseList>> {
        return repository.getIncomeExpenseList(year, month).asLiveData()
    }

    fun getIncomeExpenseListByMonthYearIdCategory(
        year: String,
        month: String,
        categoryId: Int
    ): LiveData<List<CategoryWithIncomeExpenseList>> {
        return repository.getIncomeExpenseList(year, month, categoryId).asLiveData()
    }
}

class IncomeExpenseListFactory(private val application: Application) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(IncomeExpenseListModel::class.java)) {
            return IncomeExpenseListModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
