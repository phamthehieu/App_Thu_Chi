package com.example.myapplication.repository

import androidx.annotation.WorkerThread
import com.example.myapplication.dao.IncomeExpenseListDao
import com.example.myapplication.entity.IncomeExpenseList

class IncomeExpenseListRepository(private val incomeExpenseListDao: IncomeExpenseListDao) {

    @WorkerThread
    fun insert(incomeExpenseList: IncomeExpenseList) {
        incomeExpenseListDao.insert(incomeExpenseList)
    }

}