package com.example.myapplication.dao
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
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

    @Query("SELECT * FROM account_table WHERE id = :idAccount")
    fun getAccountsByAccountId(idAccount: Int): AccountWithIcon

    @Delete
    fun deleteAccount(account: Account): Void

    @Update
    fun updateAccount(account: Account): Void
}