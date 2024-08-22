package com.example.myapplication.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.AccountWithIcon
import com.example.myapplication.database.CategoryDatabase
import com.example.myapplication.entity.Account
import com.example.myapplication.repository.AccountRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AccountViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AccountRepository
    val allAccounts: LiveData<List<AccountWithIcon>>

    init {
        val accountsDao = CategoryDatabase.getDatabase(application, viewModelScope).accountDao()
        repository = AccountRepository(accountsDao)
        allAccounts = repository.allAccounts.asLiveData()
    }

    fun insert(account: Account): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.insert(account)
                result.postValue(true)
            } catch (e: Exception) {
                result.postValue(false)
            }
        }
        return result
    }

    fun getAccountsByAccountId(accountId: Int): LiveData<AccountWithIcon> {
        val accountData = MutableLiveData<AccountWithIcon>()
        viewModelScope.launch(Dispatchers.IO) {
            val account = repository.getAccountsByAccountIdRepository(accountId)
            accountData.postValue(account)
        }
        return accountData
    }

    fun deleteAccount(account: Account) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAccount(account)
        }
    }

    fun updateAccount(account: Account): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.updateAccount(account)
                result.postValue(true)
            } catch (e: Exception) {
                result.postValue(false)
            }
        }
        return result
    }

    fun updateListAccounts(accounts: List<Account>): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.updateListAccounts(accounts)
                result.postValue(true)
            } catch (e: Exception) {
                result.postValue(false)
            }
        }
        return result
    }

    fun getAccountsByTwoIds(id1: Int, id2: Int): LiveData<List<Account>> {
        return repository.getAccountsByTwoIds(id1, id2).asLiveData()
    }

    fun deleteAllAccount() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getDeleteAll()
        }
    }
}

    class AccountViewModelFactory(private val application: Application) :
        ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AccountViewModel::class.java)) {
                return AccountViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }