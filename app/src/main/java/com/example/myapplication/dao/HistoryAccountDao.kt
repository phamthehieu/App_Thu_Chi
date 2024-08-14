package com.example.myapplication.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.myapplication.data.HistoryAccountWithAccount
import com.example.myapplication.entity.HistoryAccount
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryAccountDao {

    @Transaction
    @Query("SELECT * FROM history_account_tablet")
    fun getAllHistoryAccount(): Flow<List<HistoryAccountWithAccount>>

    @Insert
    fun insert(historyAccount: HistoryAccount): Void

    @Delete
    fun deleteHistoryAccount(historyAccount: HistoryAccount): Void

}