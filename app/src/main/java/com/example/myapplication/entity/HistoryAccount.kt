package com.example.myapplication.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(
    tableName = "history_account_tablet",
    foreignKeys = [
        ForeignKey(
            entity = Account::class,
            parentColumns = ["id"],
            childColumns = ["idAccountTransfer"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Account::class,
            parentColumns = ["id"],
            childColumns = ["idAccountReceive"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["idAccountTransfer"]), Index(value = ["idAccountReceive"])]
)
data class HistoryAccount(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val idAccountTransfer: Int,
    val nameAccountTransfer: String,
    val idAccountReceive: Int,
    val nameAccountReceive: String,
    val transferAmount: String,
    val date: String,
    val image: String,
    val note: String,
    val icon: Int,
    val type: String
)
