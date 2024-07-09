package com.example.myapplication.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CombinedCategoryIcon(
    val idCategory: Int,
    val categoryName: String,
    val categoryType: String,
    val iconResource: Int,
    val iconType: String,
    val source:String,
    val icon: Int
) : Parcelable
