package com.example.myapplication.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.myapplication.data.CategoryWithIncomeExpenseList
import com.example.myapplication.entity.IncomeExpenseList
import kotlinx.coroutines.flow.Flow

@Dao
interface IncomeExpenseListDao {

    @Insert
    fun insertList(incomeExpenseList: List<IncomeExpenseList>)

    @Insert
    fun insert(incomeExpenseList: IncomeExpenseList)

    @Transaction
    @Query("SELECT * FROM income_expense_list_table")
    fun getAllIncomeExpenseList(): Flow<List<IncomeExpenseList>>

    @Query("SELECT * FROM income_expense_list_table WHERE strftime('%Y', date) = :year AND strftime('%m', date) = :month")
    fun getIncomeExpenseListByMonthYear(
        year: String,
        month: String
    ): Flow<List<CategoryWithIncomeExpenseList>>

    @Query("SELECT * FROM income_expense_list_table WHERE strftime('%Y', date) = :year AND strftime('%m', date) = :month AND categoryId = :categoryId")
    fun getIncomeExpenseListByMonthYearIdCategory(
        year: String,
        month: String,
        categoryId: Int
    ): Flow<List<CategoryWithIncomeExpenseList>>

    @Query("SELECT * FROM income_expense_list_table WHERE strftime('%Y', date) = :year")
    fun getIncomeExpenseListByYear(year: String): Flow<List<CategoryWithIncomeExpenseList>>

    @Query("SELECT * FROM income_expense_list_table WHERE strftime('%Y', date) = :year AND categoryId = :categoryId")
    fun getIncomeExpenseListByYearAndIdCategory(
        year: String,
        categoryId: Int
    ): Flow<List<CategoryWithIncomeExpenseList>>

    @Delete
    fun delete(incomeExpenseList: IncomeExpenseList): Void

    @Update
    fun update(incomeExpenseList: IncomeExpenseList): Void

    @Query("SELECT * FROM income_expense_list_table WHERE accountId = :accountId AND strftime('%Y', date) = :year AND strftime('%m', date) = :month")
    fun getListIncomeExpenseWithAccount(
        accountId: String,
        year: String,
        month: String
    ): Flow<List<CategoryWithIncomeExpenseList>>

    @Query("""
    SELECT * FROM income_expense_list_table 
    WHERE (:type = '' OR type = :type) 
    AND (:note = '' OR note LIKE '%' || :note || '%') 
    AND (:categoryIdListSize = 0 OR categoryId IN (:categoryIdList))
""")
    fun getListIncomeExpenseWithSearch(
        type: String,
        note: String,
        categoryIdList: List<Int>,
        categoryIdListSize: Int
    ): Flow<List<CategoryWithIncomeExpenseList>>


    @Query("DELETE FROM income_expense_list_table")
    fun deleteAllTable(): Void

    @Query("SELECT * FROM income_expense_list_table WHERE date BETWEEN :startDate AND :endDate")
    fun getIncomeExpenseListByDateRange(
        startDate: String,
        endDate: String
    ): Flow<List<CategoryWithIncomeExpenseList>>

}