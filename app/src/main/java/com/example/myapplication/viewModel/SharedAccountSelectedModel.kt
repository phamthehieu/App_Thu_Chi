package com.example.myapplication.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.data.AccountIconFormat

class SharedAccountSelectedModel: ViewModel() {
    private val _selectedAccount = MutableLiveData<AccountIconFormat>()
    val selectedAccount: LiveData<AccountIconFormat> get() = _selectedAccount

    fun selectAccount(account: AccountIconFormat) {
        _selectedAccount.value = account
    }

}