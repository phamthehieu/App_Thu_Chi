package com.example.myapplication.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(
    tableName = "category_table",
    foreignKeys = [ForeignKey(
        entity = Icon::class,
        parentColumns = ["id"],
        childColumns = ["icon"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["icon"])]
)
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val icon: Int,
    val type: String,
    val source: String,
    val budget: String
) : Parcelable