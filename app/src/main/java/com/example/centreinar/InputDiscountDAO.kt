package com.example.centreinar

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface InputDiscountDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(inputDiscount: InputDiscount): Long

    @Update
    suspend fun update(inputDiscount: InputDiscount): Int

    @Delete
    suspend fun delete(inputDiscount: InputDiscount): Int

    @Query("SELECT * FROM InputDiscount")
    suspend fun getAll(): List<InputDiscount>

    @Query("SELECT * FROM InputDiscount WHERE id = :id")
    suspend fun getById(id: Int): InputDiscount?

    @Query("SELECT * FROM InputDiscount WHERE classificationId = :classificationId")
    suspend fun getByClassificationId(classificationId: Int): List<InputDiscount>

    @Query("SELECT * FROM InputDiscount WHERE grain = :grain")
    suspend fun getByGrain(grain: String): List<InputDiscount>

    @Query("SELECT * FROM InputDiscount WHERE `group` = :group")
    suspend fun getByGroup(group: Int): List<InputDiscount>
}