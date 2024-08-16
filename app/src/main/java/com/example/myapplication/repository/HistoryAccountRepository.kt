package com.example.myapplication.repository

import androidx.annotation.WorkerThread
import com.example.myapplication.dao.HistoryAccountDao
import com.example.myapplication.data.HistoryAccountWithAccount
import com.example.myapplication.entity.HistoryAccount
import kotlinx.coroutines.flow.Flow

class HistoryAccountRepository(private val historyAccountDao: HistoryAccountDao) {
    val allHistoryAccounts: Flow<List<HistoryAccountWithAccount>> = historyAccountDao.getAllHistoryAccount()

    @WorkerThread
    fun insert(historyAccount: HistoryAccount) {
        historyAccountDao.insert(historyAccount)
    }

    @WorkerThread
    fun delete(historyAccount: HistoryAccount) {
        historyAccountDao.deleteHistoryAccount(historyAccount)
    }

    @WorkerThread
    fun updateHistoryAccount(historyAccount: HistoryAccount) {
        historyAccountDao.updateHistoryAccount(historyAccount)

    }

}