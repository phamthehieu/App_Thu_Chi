package com.example.myapplication.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(
    tableName = "budget_table",
    foreignKeys = [ForeignKey(
        entity = Category::class,
        parentColumns = ["id"],
        childColumns = ["categoryId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["categoryId"])])
data class Budget(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val budgetCost: String,
    val categoryId: Int,
) : Parcelable
