package com.example.centreinar.data.local.dao


import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.centreinar.data.local.entity.ColorClassificationMilho

class ColorClassificationMilhoDAO {

    @Dao
    interface ColorClassificationMilhoDao {

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insert(color: ColorClassificationMilho): Long

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertAll(colors: List<ColorClassificationMilho>): List<Long>

        @Update suspend fun update(color: ColorClassificationMilho)
        @Delete suspend fun delete(color: ColorClassificationMilho)

        @Query("DELETE FROM color_classification_milho")
        suspend fun deleteAll()

        @Query("SELECT * FROM color_classification_milho")
        suspend fun getAll(): List<ColorClassificationMilho>

        @Query("SELECT * FROM color_classification_milho WHERE id = :id")
        suspend fun getById(id: Int): ColorClassificationMilho?

        @Query("SELECT * FROM color_classification_milho WHERE classificationId = :classificationId")
        suspend fun getByClassificationId(classificationId: Int): ColorClassificationMilho?

        @Query("SELECT * FROM color_classification_milho WHERE `class` = :framingClass")
        suspend fun getByFramingClass(framingClass: String): List<ColorClassificationMilho>

        @Query("SELECT * FROM color_classification_milho ORDER BY id DESC LIMIT 1")
        suspend fun getLastColorClass(): ColorClassificationMilho?
    }

}