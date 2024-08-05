package com.example.myapplication.utilities

import com.example.myapplication.data.CategoryWithIncomeExpenseList
import com.example.myapplication.data.IncomeExpenseListData

fun convertToIncomeExpenseListData(categoryWithIncomeExpenseList: CategoryWithIncomeExpenseList): IncomeExpenseListData {
    val incomeExpense = categoryWithIncomeExpenseList.incomeExpense
    val category = categoryWithIncomeExpenseList.category

    return IncomeExpenseListData(
        id = incomeExpense.id,
        note = incomeExpense.note,
        amount = incomeExpense.amount,
        date = incomeExpense.date,
        categoryId = incomeExpense.categoryId,
        type = incomeExpense.type,
        image = incomeExpense.image,
        categoryName = incomeExpense.categoryName,
        iconResource = incomeExpense.iconResource,
        idIcon = category.icon,
        accountId = incomeExpense.accountId
    )
}