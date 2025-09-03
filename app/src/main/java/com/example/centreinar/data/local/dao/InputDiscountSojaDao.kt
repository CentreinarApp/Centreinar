package com.example.centreinar.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.centreinar.InputDiscountSoja


@Dao
interface InputDiscountSojaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(inputDiscount: InputDiscountSoja): Long

    @Update
    suspend fun update(inputDiscount: InputDiscountSoja): Int

    @Delete
    suspend fun delete(inputDiscount: InputDiscountSoja): Int

    @Query("SELECT * FROM input_discount_soja ORDER BY id DESC LIMIT 1")
    suspend fun getLastInputDiscount(): InputDiscountSoja

    @Query("SELECT * FROM input_discount_soja")
    suspend fun getAll(): List<InputDiscountSoja>

    @Query("SELECT * FROM input_discount_soja WHERE id = :id")
    suspend fun getById(id: Int): InputDiscountSoja?

    @Query("SELECT * FROM input_discount_soja WHERE classificationId = :classificationId")
    suspend fun getByClassificationId(classificationId: Int): List<InputDiscountSoja>

    @Query("SELECT * FROM input_discount_soja WHERE grain = :grain")
    suspend fun getByGrain(grain: String): List<InputDiscountSoja>

    @Query("SELECT * FROM input_discount_soja WHERE `group` = :group")
    suspend fun getByGroup(group: Int): List<InputDiscountSoja>
}

