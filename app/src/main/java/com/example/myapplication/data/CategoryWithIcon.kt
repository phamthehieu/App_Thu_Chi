package com.example.myapplication.data

import androidx.room.Embedded
import androidx.room.Relation
import com.example.myapplication.entity.Category
import com.example.myapplication.entity.Icon

data class CategoryWithIcon(
    @Embedded val category: Category,
    @Relation(
        parentColumn = "icon",
        entityColumn = "id"
    )
    val icon: Icon
)

