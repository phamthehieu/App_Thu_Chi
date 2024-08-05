package com.example.myapplication.dao
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.myapplication.data.AccountWithIcon
import com.example.myapplication.entity.Account
import com.example.myapplication.entity.IncomeExpenseList
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {

    @Transaction
    @Query("SELECT * FROM account_table")
    fun getAllAccounts(): Flow<List<AccountWithIcon>>

    @Insert
    fun insert(account: Account)
}