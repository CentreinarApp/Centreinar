package com.example.centreinar.data.local.dao

import androidx.room.*
import com.example.centreinar.ClassificationMilho
import kotlinx.coroutines.flow.Flow

@Dao
interface ClassificationMilhoDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(classification: ClassificationMilho): Long

    @Insert
    suspend fun insertAll(classifications: List<ClassificationMilho>): List<Long>

    @Update
    suspend fun update(classification: ClassificationMilho): Int

    @Delete
    suspend fun delete(classification: ClassificationMilho): Int

    @Query("DELETE FROM classification_milho")
    suspend fun deleteAll(): Int

    @Query("SELECT * FROM classification_milho")
    fun getAll(): Flow<List<ClassificationMilho>>

    @Query("SELECT * FROM classification_milho WHERE id = :id")
    suspend fun getById(id: Int): ClassificationMilho?

    @Query("SELECT * FROM classification_milho WHERE sample = :sampleId")
    fun getBySampleId(sampleId: Int): Flow<List<ClassificationMilho>>

    @Query("SELECT * FROM classification_milho WHERE `group` = :group")
    fun getByGroup(group: Int): Flow<List<ClassificationMilho>>

    @Query("SELECT * FROM classification_milho WHERE grain = :grainType")
    fun getByGrainType(grainType: String): Flow<List<ClassificationMilho>>

    @Query("SELECT * FROM classification_milho ORDER BY id DESC LIMIT 1")
    suspend fun getLastClassification(): ClassificationMilho
}
