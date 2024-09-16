package com.example.myapplication.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.myapplication.data.HistoryAccountWithAccount
import com.example.myapplication.entity.HistoryAccount
import com.example.myapplication.entity.IncomeExpenseList
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryAccountDao {

    @Transaction
    @Query("SELECT * FROM history_account_tablet")
    fun getAllHistoryAccount(): Flow<List<HistoryAccountWithAccount>>

    @Query("SELECT * FROM history_account_tablet ")
    fun allHistoryAccount(): Flow<List<HistoryAccount>>

    @Insert
    fun insertList(historyAccounts: List<HistoryAccount>): Void

    @Insert
    fun insert(historyAccount: HistoryAccount): Void

    @Delete
    fun deleteHistoryAccount(historyAccount: HistoryAccount): Void

    @Update
    fun updateHistoryAccount(historyAccount: HistoryAccount): Void

    @Query("SELECT * FROM history_account_tablet WHERE (:note = '' OR note LIKE '%' || :note || '%')")
    fun getHistoryAccountWithSearch(note: String): Flow<List<HistoryAccountWithAccount>>

    @Query("DELETE FROM history_account_tablet")
    fun deleteAllHistoryAccount(): Void

    @Query("SELECT * FROM history_account_tablet WHERE date BETWEEN :startDate AND :endDate")
    fun getHistoryAccountListByDateRange(startDate: String, endDate: String): Flow<List<HistoryAccountWithAccount>>
}