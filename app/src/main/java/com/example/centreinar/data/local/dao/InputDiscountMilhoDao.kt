package com.example.centreinar.data.local.dao

import androidx.room.*
import com.example.centreinar.data.local.entity.InputDiscountMilho

@Dao
interface InputDiscountMilhoDao {
    @Insert
    suspend fun insert(input: InputDiscountMilho): Long

    @Update
    suspend fun update(input: InputDiscountMilho)

    @Delete
    suspend fun delete(input: InputDiscountMilho)

    @Query("SELECT * FROM input_discount_milho WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): InputDiscountMilho?

    @Query("SELECT * FROM input_discount_milho ORDER BY id DESC LIMIT 1")
    suspend fun getLastInputDiscount(): InputDiscountMilho
}

