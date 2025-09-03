package com.example.centreinar.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.centreinar.DisqualificationSoja


@Dao
interface DisqualificationSojaDao {

    // Insert operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(disqualification: DisqualificationSoja): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(disqualifications: List<DisqualificationSoja>): List<Long>

    // Update operations
    @Update
    suspend fun update(disqualification: DisqualificationSoja)

    @Query("UPDATE disqualification_soja SET graveDefectSum = :newValue WHERE id = :id")
    suspend fun updateGraveDefectSum(id: Int, newValue: Int)

    @Query("UPDATE disqualification_soja SET classificationId = :classificationId WHERE id = :id")
    suspend fun updateClassificationId(id: Int, classificationId: Int)

    // Delete operations
    @Delete
    suspend fun delete(disqualification: DisqualificationSoja)

    @Query("DELETE FROM disqualification_soja")
    suspend fun deleteAll()

    // Query operations
    @Query("SELECT * FROM disqualification_soja")
    suspend fun getAll(): List<DisqualificationSoja>

    @Query("SELECT * FROM disqualification_soja WHERE id = :id")
    suspend fun getById(id: Int): DisqualificationSoja?

    @Query("SELECT * FROM disqualification_soja WHERE classificationId = :classificationId")
    suspend fun getByClassificationId(classificationId: Int): DisqualificationSoja

    @Query("SELECT * FROM disqualification_soja ORDER BY id DESC LIMIT 1")
    suspend fun getLastDisqualification(): DisqualificationSoja

    @Query("SELECT id FROM disqualification_soja ORDER BY id DESC LIMIT 1")
    suspend fun getLastDisqualificationId(): Int
}
