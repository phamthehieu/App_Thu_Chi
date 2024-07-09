package com.example.myapplication.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

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
data class IncomeExpenseList (
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val note: String,
    val amount: Int,
    val date: String,
    val categoryId: Int,
    val type: String,
    val image: String
)
