package com.example.myapplication.data

import androidx.room.Embedded
import androidx.room.Relation
import com.example.myapplication.entity.Account
import com.example.myapplication.entity.Icon

data class AccountWithIcon(
    @Embedded
    val account: Account,
    @Relation(
        parentColumn = "icon",
        entityColumn = "id"
    )
    val icon: Icon
)