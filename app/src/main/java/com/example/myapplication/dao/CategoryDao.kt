package com.example.myapplication.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.myapplication.data.CategoryWithIcon
import com.example.myapplication.entity.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Transaction
    @Query("SELECT * FROM category_table")
    fun getAllCategory(): Flow<List<CategoryWithIcon>>

    @Insert
    fun insertAllCategory(category: List<Category>)

    @Delete
    fun deleteCategory(category: Category): Void

    @Update
    fun updateCategory(category: Category): Void
}