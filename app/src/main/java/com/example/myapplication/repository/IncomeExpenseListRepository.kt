package com.example.myapplication.repository

import androidx.annotation.WorkerThread
import com.example.myapplication.dao.IncomeExpenseListDao
import com.example.myapplication.entity.IncomeExpenseList
import kotlinx.coroutines.flow.Flow

class IncomeExpenseListRepository(private val incomeExpenseListDao: IncomeExpenseListDao) {
    val allIncomeExpenseList: Flow<List<IncomeExpenseList>> = incomeExpenseListDao.getAllIncomeExpenseList()

    @WorkerThread
    fun insert(incomeExpenseList: IncomeExpenseList) {
        incomeExpenseListDao.insert(incomeExpenseList)
    }

}