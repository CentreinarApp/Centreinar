package com.example.centreinar.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.centreinar.ColorClassificationSoja


@Dao
interface ColorClassificationSojaDao {

    // Insert operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(colorClassification: ColorClassificationSoja): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(colorClassifications: List<ColorClassificationSoja>): List<Long>

    // Update operation
    @Update
    suspend fun update(colorClassification: ColorClassificationSoja)

    // Delete operations
    @Delete
    suspend fun delete(colorClassification: ColorClassificationSoja)

    @Query("DELETE FROM ColorClassificationSoja")
    suspend fun deleteAll()

    // Query operations
    @Query("SELECT * FROM ColorClassificationSoja")
    suspend fun getAll(): List<ColorClassificationSoja>

    @Query("SELECT * FROM ColorClassificationSoja WHERE id = :id")
    suspend fun getById(id: Int): ColorClassificationSoja?

    @Query("SELECT * FROM ColorClassificationSoja WHERE classificationId = :classificationId")
    suspend fun getByClassificationId(classificationId: Int): ColorClassificationSoja

    @Query("SELECT * FROM ColorClassificationSoja WHERE `class` = :framingClass")
    suspend fun getByFramingClass(framingClass: String): List<ColorClassificationSoja>

    @Query("SELECT * FROM ColorClassificationSoja ORDER BY id DESC LIMIT 1")
    suspend fun getLastColorClass(): ColorClassificationSoja?
}
