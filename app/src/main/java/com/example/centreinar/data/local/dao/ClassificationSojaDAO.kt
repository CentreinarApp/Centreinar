package com.example.centreinar.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.centreinar.ClassificationSoja
import kotlinx.coroutines.flow.Flow

class ClassificationSojaDAO {
@Dao
interface ClassificationSojaDao {

    // Insert operations
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(classification: ClassificationSoja): Long

    @Insert
    suspend fun insertAll(classifications: List<ClassificationSoja>): List<Long>

    // Update operation
    @Update
    suspend fun update(classification: ClassificationSoja): Int

    // Delete operations
    @Delete
    suspend fun delete(classification: ClassificationSoja): Int

    @Query("DELETE FROM classification_soja")
    suspend fun deleteAll(): Int

    // Query operations
    @Query("SELECT * FROM classification_soja")
    fun getAll(): Flow<List<ClassificationSoja>>

    @Query("SELECT * FROM classification_soja WHERE id = :id")
    suspend fun getById(id: Int): ClassificationSoja?

    @Query("SELECT * FROM classification_soja WHERE sample = :sampleId")
    fun getBySampleId(sampleId: Int): Flow<List<ClassificationSoja>>

    @Query("SELECT * FROM classification_soja WHERE `group` = :group")
    fun getByGroup(group: Int): Flow<List<ClassificationSoja>>

    @Query("SELECT * FROM classification_soja WHERE grain = :grainType")
    fun getByGrainType(grainType: String): Flow<List<ClassificationSoja>>

    @Query("SELECT * FROM classification_soja ORDER BY id DESC LIMIT 1")
    suspend fun getLastClassification(): ClassificationSoja
}


}
