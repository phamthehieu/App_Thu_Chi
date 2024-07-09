package com.example.myapplication.data

import androidx.room.Embedded
import androidx.room.Relation
import com.example.myapplication.entity.Category
import com.example.myapplication.entity.IncomeExpenseList

data class CategoryWithIncomeExpenseList (
    @Embedded val incomeExpense: IncomeExpenseList,
    @Relation(
        parentColumn = "categoryId",
        entityColumn = "id"
    )
    val category: Category
)