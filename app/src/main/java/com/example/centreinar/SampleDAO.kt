package com.example.centreinar

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SampleDao {

    // Insert operations
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(sample: Sample): Long

    @Insert
    suspend fun insertAll(samples: List<Sample>): List<Long>

    // Update operation
    @Update
    suspend fun update(sample: Sample): Int

    // Delete operations
    @Delete
    suspend fun delete(sample: Sample): Int

    @Query("DELETE FROM sample")
    suspend fun deleteAll(): Int

    // Query operations
    @Query("SELECT * FROM sample")
    fun getAll(): Flow<List<Sample>>

    @Query("SELECT * FROM sample WHERE id = :id")
    suspend fun getById(id: Int): Sample?

    // Example filter queries
    @Query("SELECT * FROM sample WHERE weight BETWEEN :minWeight AND :maxWeight")
    fun getByWeightRange(minWeight: Float, maxWeight: Float): Flow<List<Sample>>

    @Query("SELECT * FROM sample WHERE humidity > :minHumidity")
    fun getByMinimumHumidity(minHumidity: Float): Flow<List<Sample>>

    @Query("SELECT * FROM sample WHERE brokenCrackedDamaged > :threshold")
    fun getWithDamageAbove(threshold: Float): Flow<List<Sample>>

    @Query("SELECT * FROM sample ORDER BY id DESC LIMIT 1")
    suspend fun getLatestSample(): Sample?
}