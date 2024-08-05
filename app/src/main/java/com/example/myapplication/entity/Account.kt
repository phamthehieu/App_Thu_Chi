package com.example.myapplication.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "account_table",
    foreignKeys = [ForeignKey(
        entity = Icon::class,
        parentColumns = ["id"],
        childColumns = ["icon"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["icon"])])
data class Account(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nameAccount: String,
    val typeAccount: Int,
    val amountAccount: String,
    val icon: Int,
    val note: String
) : Parcelable
