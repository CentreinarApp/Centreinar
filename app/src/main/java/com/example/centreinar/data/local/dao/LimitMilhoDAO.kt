package com.example.centreinar.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.centreinar.data.local.entity.LimitMilho

class LimitMilhoDAO {
    @Dao
    interface LimitMilhoDao {

        @Query("SELECT * FROM limits_milho")
        suspend fun getAllLimits(): List<LimitMilho>

        @Insert suspend fun insertLimit(limit: LimitMilho): Long
        @Delete
        suspend fun deleteLimit(limit: LimitMilho)
        @Update
        suspend fun updateLimit(limit: LimitMilho)

        @Query("SELECT * FROM limits_milho WHERE id = :id")
        suspend fun getLimitById(id: Long): List<LimitMilho>

        @Query("SELECT * FROM limits_milho WHERE grain = :grain")
        suspend fun getLimitsByGrain(grain: String): List<LimitMilho>

        @Query("SELECT * FROM limits_milho WHERE grain = :grain AND source = :limitSource AND `group` = :group")
        suspend fun getLimitsBySource(grain: String, group: Int, limitSource: Int): List<LimitMilho>

        @Query("SELECT source FROM limits_milho ORDER BY source DESC LIMIT 1")
        suspend fun getLastSource(): Int
    }

}