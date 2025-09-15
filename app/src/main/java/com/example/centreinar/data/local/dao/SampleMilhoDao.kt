package com.example.centreinar.data.local.dao


import androidx.room.*
import com.example.centreinar.data.local.entity.SampleMilho

@Dao
interface SampleMilhoDao {
    @Insert
    suspend fun insert(sample: SampleMilho): Long

    @Update
    suspend fun update(sample: SampleMilho)

    @Delete
    suspend fun delete(sample: SampleMilho)

    @Query("SELECT * FROM samples_milho WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): SampleMilho?

    @Query("SELECT * FROM samples_milho ORDER BY id DESC LIMIT 1")
    suspend fun getLastSample(): SampleMilho
}
