package com.example.centreinar.data.local.dao

import androidx.room.*
import com.example.centreinar.ClassificationMilho
import com.example.centreinar.data.local.entity.DiscountMilho
import kotlinx.coroutines.flow.Flow

class DiscountMilhoDAO {
    @Dao
    interface DiscountMilhoDao {

        @Insert suspend fun insert(discount: DiscountMilho): Long
        @Update suspend fun update(discount: DiscountMilho)
        @Delete suspend fun delete(discount: DiscountMilho)

        @Query("SELECT * FROM discount_milho ORDER BY id DESC")
        fun getAllDiscounts(): Flow<List<DiscountMilho>>

        @Query("SELECT * FROM discount_milho WHERE id = :id")
        suspend fun getDiscountById(id: Int): DiscountMilho?

        @Query("SELECT * FROM discount_milho ORDER BY id DESC LIMIT 1")
        suspend fun getLastDiscount(): DiscountMilho?
    }

}