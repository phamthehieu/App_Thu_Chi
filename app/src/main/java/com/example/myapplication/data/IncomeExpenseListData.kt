package com.example.myapplication.data

data class IncomeExpenseListData(
    val id: Int,
    val note: String,
    val amount: String,
    val date: String,
    val categoryId: Int,
    val type: String,
    val image: String,
    val categoryName: String,
    val iconResource: Int,
    val idIcon: Int,
)
