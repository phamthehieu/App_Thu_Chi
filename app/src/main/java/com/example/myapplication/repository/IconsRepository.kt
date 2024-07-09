package com.example.myapplication.repository

import androidx.annotation.WorkerThread
import com.example.myapplication.dao.IconsDao
import com.example.myapplication.entity.Icon
import kotlinx.coroutines.flow.Flow

class IconsRepository(private val iconsDao: IconsDao) {
    val allIcons: Flow<List<Icon>> = iconsDao.getAllIcons()

    @WorkerThread
    suspend fun insertAll(icons: List<Icon>) {
        iconsDao.insertAll(icons)
    }
}