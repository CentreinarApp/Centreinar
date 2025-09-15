package com.example.centreinar.data.local.dao

import androidx.room.*
import com.example.centreinar.ClassificationMilho

@Dao
interface ClassificationMilhoDao {
    @Insert
    suspend fun insert(classification: ClassificationMilho): Long

    @Update
    suspend fun update(classification: ClassificationMilho)

    @Delete
    suspend fun delete(classification: ClassificationMilho)

    @Query("SELECT * FROM classification_milho WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): ClassificationMilho?

    @Query("SELECT * FROM classification_milho ORDER BY id DESC LIMIT 1")
    suspend fun getLastClassification(): ClassificationMilho
}
