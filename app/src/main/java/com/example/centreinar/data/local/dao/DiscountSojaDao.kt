package com.example.centreinar.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.centreinar.DiscountSoja
import kotlinx.coroutines.flow.Flow


@Dao
interface DiscountSojaDao {

    @Insert
    suspend fun insert(discount: DiscountSoja): Long

    @Update
    suspend fun update(discount: DiscountSoja)

    @Delete
    suspend fun delete(discount: DiscountSoja)

    @Query("SELECT * FROM discount_soja ORDER BY id DESC")
    fun getAllDiscounts(): Flow<List<DiscountSoja>>

    @Query("SELECT * FROM discount_soja WHERE id = :id")
    suspend fun getDiscountById(id: Int): DiscountSoja?
}

