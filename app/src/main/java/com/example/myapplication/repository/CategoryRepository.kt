package com.example.myapplication.repository

import androidx.annotation.WorkerThread
import com.example.myapplication.dao.CategoryDao
import com.example.myapplication.data.CategoryWithIcon
import com.example.myapplication.entity.Category
import kotlinx.coroutines.flow.Flow

class CategoryRepository(private val categoryDao: CategoryDao) {
    val allCategories: Flow<List<CategoryWithIcon>> = categoryDao.getAllCategory()

    @WorkerThread
    fun insertAll(category: List<Category>) {
        categoryDao.insertAllCategory(category)
    }

    @WorkerThread
    suspend fun deleteCategory(category: Category) {
        categoryDao.deleteCategory(category)
    }

    @WorkerThread
    suspend fun updateCategory(category: Category) {
        categoryDao.updateCategory(category)
    }

    @WorkerThread
    fun deleteAllCategory() {
        categoryDao.deleteAllCategoryType()
    }

}