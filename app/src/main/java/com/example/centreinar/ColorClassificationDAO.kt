package com.example.centreinar

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface ColorClassificationDao {

    // Insert operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(colorClassification: ColorClassification): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(colorClassifications: List<ColorClassification>): List<Long>

    // Update operation
    @Update
    suspend fun update(colorClassification: ColorClassification)

    // Delete operations
    @Delete
    suspend fun delete(colorClassification: ColorClassification)

    @Query("DELETE FROM ColorClassification")
    suspend fun deleteAll()

    // Query operations
    @Query("SELECT * FROM ColorClassification")
    suspend fun getAll(): List<ColorClassification>

    @Query("SELECT * FROM ColorClassification WHERE id = :id")
    suspend fun getById(id: Int): ColorClassification?

    @Query("SELECT * FROM ColorClassification WHERE classificationId = :classificationId")
    suspend fun getByClassificationId(classificationId: Int): ColorClassification

    @Query("SELECT * FROM ColorClassification WHERE `class` = :framingClass")
    suspend fun getByFramingClass(framingClass: String): List<ColorClassification>
}