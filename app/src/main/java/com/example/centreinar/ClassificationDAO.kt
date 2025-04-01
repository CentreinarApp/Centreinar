package com.example.centreinar

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ClassificationDao {

    // Insert operations
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(classification: Classification): Long

    @Insert
    suspend fun insertAll(classifications: List<Classification>): List<Long>

    // Update operation
    @Update
    suspend fun update(classification: Classification): Int

    // Delete operations
    @Delete
    suspend fun delete(classification: Classification): Int

    @Query("DELETE FROM classification")
    suspend fun deleteAll(): Int

    // Query operations
    @Query("SELECT * FROM classification")
    fun getAll(): Flow<List<Classification>>

    @Query("SELECT * FROM classification WHERE id = :id")
    suspend fun getById(id: Int): Classification?

    @Query("SELECT * FROM classification WHERE sample = :sampleId")
    fun getBySampleId(sampleId: Int): Flow<List<Classification>>

    @Query("SELECT * FROM classification WHERE `group` = :group")
    fun getByGroup(group: Int): Flow<List<Classification>>

    @Query("SELECT * FROM classification WHERE grain = :grainType")
    fun getByGrainType(grainType: String): Flow<List<Classification>>
}