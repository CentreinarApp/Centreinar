package com.example.centreinar.data.local.dao

import androidx.room.*
import com.example.centreinar.data.local.entity.ColorClassificationMilho

@Dao
interface ColorClassificationMilhoDao {
    @Insert
    suspend fun insert(color: ColorClassificationMilho): Long

    @Update
    suspend fun update(color: ColorClassificationMilho)

    @Delete
    suspend fun delete(color: ColorClassificationMilho)

    @Query("SELECT * FROM color_group_classification_milho WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): ColorClassificationMilho?

    @Query("SELECT * FROM color_group_classification_milho ORDER BY id DESC LIMIT 1")
    suspend fun getLastColorClass(): ColorClassificationMilho?
}

