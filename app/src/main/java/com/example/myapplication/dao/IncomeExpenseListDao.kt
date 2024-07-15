package com.example.myapplication.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.myapplication.entity.IncomeExpenseList
import kotlinx.coroutines.flow.Flow

@Dao
interface IncomeExpenseListDao {

    @Insert
    fun insert(incomeExpenseList: IncomeExpenseList)

    @Transaction
    @Query("SELECT * FROM income_expense_list_table")
    fun getAllIncomeExpenseList(): Flow<List<IncomeExpenseList>>

}