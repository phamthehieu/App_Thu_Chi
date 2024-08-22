package com.example.myapplication.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.HistoryAccountWithAccount
import com.example.myapplication.database.CategoryDatabase
import com.example.myapplication.entity.Account
import com.example.myapplication.entity.HistoryAccount
import com.example.myapplication.repository.HistoryAccountRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HistoryAccountViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: HistoryAccountRepository
    val allHistoryAccount: LiveData<List<HistoryAccountWithAccount>>

    init {
        val historyAccountDao = CategoryDatabase.getDatabase(application, viewModelScope).historyAccountDao()
        repository = HistoryAccountRepository(historyAccountDao)
        allHistoryAccount = repository.allHistoryAccounts.asLiveData()
    }

    fun insert(historyAccount: HistoryAccount): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.insert(historyAccount)
                result.postValue(true)
            } catch (e: Exception) {
                result.postValue(false)
            }
        }
         return result;
    }

    fun deleteHistoryAccount(historyAccount: HistoryAccount) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(historyAccount)
        }
    }

    fun updateHistoryAccount(historyAccount: HistoryAccount): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.updateHistoryAccount(historyAccount)
                result.postValue(true)
            } catch (e: Exception) {
                result.postValue(false)
            }
        }
        return result
    }

    fun getHistoryAccountWithSearch(note: String): LiveData<List<HistoryAccountWithAccount>> {
        return repository.getHistoryAccountWithSearch(note).asLiveData()
    }

    fun deleteAllHistoryAccount() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllHistoryAccount()
        }
    }

}

class HistoryAccountViewModelFactory(private val application: Application) :
    ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryAccountViewModel::class.java)) {
            return HistoryAccountViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}