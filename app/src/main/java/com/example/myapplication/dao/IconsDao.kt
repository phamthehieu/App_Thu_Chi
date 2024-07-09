package com.example.myapplication.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.myapplication.entity.Icon
import kotlinx.coroutines.flow.Flow

@Dao
interface IconsDao {

    @Query("SELECT * FROM icon_table")
    fun getAllIcons(): Flow<List<Icon>>

    @Insert
    fun insertAll(icons: List<Icon>)

}