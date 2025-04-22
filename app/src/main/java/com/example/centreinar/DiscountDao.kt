package com.example.centreinar

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DiscountDao {
    @Insert
    suspend fun insert(discount: Discount): Long

    @Update
    suspend fun update(discount: Discount)

    @Delete
    suspend fun delete(discount: Discount)

    @Query("SELECT * FROM discount ORDER BY id DESC")
    fun getAllDiscounts(): Flow<List<Discount>>

    @Query("SELECT * FROM discount WHERE id = :id")
    suspend fun getDiscountById(id: Int): Discount?
}