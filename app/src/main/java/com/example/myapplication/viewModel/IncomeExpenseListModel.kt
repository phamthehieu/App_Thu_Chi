package com.example.myapplication.viewModel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplication.database.CategoryDatabase
import com.example.myapplication.entity.IncomeExpenseList
import com.example.myapplication.repository.IncomeExpenseListRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class IncomeExpenseListModel(application: Application) : AndroidViewModel(application) {
    private val repository: IncomeExpenseListRepository
    val allIncomeExpense: LiveData<List<IncomeExpenseList>>

    init {
        val incomeExpenseDao = CategoryDatabase.getDatabase(application, viewModelScope).incomeExpenseListDao()
        repository = IncomeExpenseListRepository(incomeExpenseDao)
        allIncomeExpense = repository.allIncomeExpenseList.asLiveData()
    }

    fun insert(incomeExpenseList: IncomeExpenseList): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.insert(incomeExpenseList)
                result.postValue(true)  // Chèn thành công
            } catch (e: Exception) {
                result.postValue(false) // Chèn không thành công
            }
        }
        return result
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
