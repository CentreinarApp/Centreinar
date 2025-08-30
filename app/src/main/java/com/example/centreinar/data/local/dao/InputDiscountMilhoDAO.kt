package com.example.centreinar.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.centreinar.data.local.entity.InputDiscountMilho

class InputDiscountMilhoDAO {
    @Dao
    interface InputDiscountMilhoDao {

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insert(inputDiscount: InputDiscountMilho): Long

        @Update
        suspend fun update(inputDiscount: InputDiscountMilho): Int
        @Delete
        suspend fun delete(inputDiscount: InputDiscountMilho): Int

        @Query("SELECT * FROM input_discount_milho ORDER BY id DESC LIMIT 1")
        suspend fun getLastInputDiscount(): InputDiscountMilho?

        @Query("SELECT * FROM input_discount_milho")
        suspend fun getAll(): List<InputDiscountMilho>

        @Query("SELECT * FROM input_discount_milho WHERE id = :id")
        suspend fun getById(id: Int): InputDiscountMilho?

        @Query("SELECT * FROM input_discount_milho WHERE classificationId = :classificationId")
        suspend fun getByClassificationId(classificationId: Int): List<InputDiscountMilho>

        @Query("SELECT * FROM input_discount_milho WHERE grain = :grain")
        suspend fun getByGrain(grain: String): List<InputDiscountMilho>

        @Query("SELECT * FROM input_discount_milho WHERE `group` = :group")
        suspend fun getByGroup(group: Int): List<InputDiscountMilho>
    }

}