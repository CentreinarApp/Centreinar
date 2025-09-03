package com.example.centreinar.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.centreinar.data.local.entity.SampleMilho
import kotlinx.coroutines.flow.Flow

    @Dao
    interface SampleMilhoDao {

        @Insert(onConflict = OnConflictStrategy.ABORT)
        suspend fun insert(sample: SampleMilho): Long

        @Insert
        suspend fun insertAll(samples: List<SampleMilho>): List<Long>

        @Update
        suspend fun update(sample: SampleMilho): Int
        @Delete
        suspend fun delete(sample: SampleMilho): Int

        @Query("DELETE FROM sample_milho")
        suspend fun deleteAll(): Int

        @Query("SELECT * FROM sample_milho")
        fun getAll(): Flow<List<SampleMilho>>

        @Query("SELECT * FROM sample_milho WHERE id = :id")
        suspend fun getById(id: Int): SampleMilho?

        @Query("SELECT * FROM sample_milho WHERE sampleWeight BETWEEN :minWeight AND :maxWeight")
        fun getByWeightRange(minWeight: Float, maxWeight: Float): Flow<List<SampleMilho>>

        @Query("SELECT * FROM sample_milho WHERE humidity > :minHumidity")
        fun getByMinimumHumidity(minHumidity: Float): Flow<List<SampleMilho>>

        @Query("SELECT * FROM sample_milho WHERE broken > :threshold")
        fun getWithDamageAbove(threshold: Float): Flow<List<SampleMilho>>

        @Query("SELECT * FROM sample_milho ORDER BY id DESC LIMIT 1")
        suspend fun getLatestSample(): SampleMilho?
    }

