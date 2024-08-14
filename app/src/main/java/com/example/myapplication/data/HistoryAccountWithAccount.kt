package com.example.myapplication.data

import androidx.room.Embedded
import androidx.room.Relation
import com.example.myapplication.entity.Account
import com.example.myapplication.entity.HistoryAccount

data class HistoryAccountWithAccount (
    @Embedded val historyAccount: HistoryAccount,
    @Relation(
        parentColumn = "idAccountTransfer",
        entityColumn = "id"
    )
    val accountTransfer: Account,
    @Relation(
        parentColumn = "idAccountReceive",
        entityColumn = "id"
    )
    val accountReceive: Account

)