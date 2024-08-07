package com.example.myapplication.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(
    tableName = "income_expense_list_table",
    foreignKeys = [ForeignKey(
        entity = Category::class,
        parentColumns = ["id"],
        childColumns = ["categoryId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["categoryId"])]
)
data class IncomeExpenseList(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val note: String,
    val amount: String,
    val date: String,
    val categoryId: Int,
    val type: String,
    val image: String,
    val categoryName: String,
    val iconResource: Int,
    val accountId: String
) : Parcelable
