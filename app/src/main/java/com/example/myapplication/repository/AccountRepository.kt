package com.example.myapplication.repository

import androidx.annotation.WorkerThread
import com.example.myapplication.dao.AccountDao
import com.example.myapplication.data.AccountWithIcon
import com.example.myapplication.entity.Account
import com.example.myapplication.entity.IncomeExpenseList
import kotlinx.coroutines.flow.Flow

class AccountRepository(private val accountDao: AccountDao) {
    val allAccounts: Flow<List<AccountWithIcon>> = accountDao.getAllAccounts()

    @WorkerThread
    fun insert(account: Account) {
        accountDao.insert(account)
    }

    @WorkerThread
    fun getAccountsByAccountIdRepository(accountId: Int): AccountWithIcon {
        return accountDao.getAccountsByAccountId(accountId)
    }

    @WorkerThread
    fun deleteAccount(account: Account) {
        accountDao.deleteAccount(account)
    }

    @WorkerThread
    fun updateAccount(account: Account) {
        accountDao.updateAccount(account)
    }

}