package com.example.myapplication.dao

import androidx.room.Dao
import androidx.room.Insert
import com.example.myapplication.entity.IncomeExpenseList

@Dao
interface IncomeExpenseListDao {

    @Insert
    fun insert(incomeExpenseList: IncomeExpenseList)

}