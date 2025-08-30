package com.example.centreinar.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.centreinar.SampleSoja
import kotlinx.coroutines.flow.Flow

@Dao
interface SampleSojaDao {

    // Insert operations
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(sample: SampleSoja): Long

    @Insert
    suspend fun insertAll(samples: List<SampleSoja>): List<Long>

    // Update operation
    @Update
    suspend fun update(sample: SampleSoja): Int

    // Delete operations
    @Delete
    suspend fun delete(sample: SampleSoja): Int

    @Query("DELETE FROM sample_soja")
    suspend fun deleteAll(): Int

    // Query operations
    @Query("SELECT * FROM sample_soja")
    fun getAll(): Flow<List<SampleSoja>>

    @Query("SELECT * FROM sample_soja WHERE id = :id")
    suspend fun getById(id: Int): SampleSoja?

    // Example filter queries
    @Query("SELECT * FROM sample_soja WHERE sampleWeight BETWEEN :minWeight AND :maxWeight")
    fun getByWeightRange(minWeight: Float, maxWeight: Float): Flow<List<SampleSoja>>

    @Query("SELECT * FROM sample_soja WHERE humidity > :minHumidity")
    fun getByMinimumHumidity(minHumidity: Float): Flow<List<SampleSoja>>

    @Query("SELECT * FROM sample_soja WHERE brokenCrackedDamaged > :threshold")
    fun getWithDamageAbove(threshold: Float): Flow<List<SampleSoja>>

    @Query("SELECT * FROM sample_soja ORDER BY id DESC LIMIT 1")
    suspend fun getLatestSample(): SampleSoja?
}

