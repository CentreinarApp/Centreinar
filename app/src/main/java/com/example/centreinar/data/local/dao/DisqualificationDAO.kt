package com.example.centreinar.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.centreinar.Disqualification

@Dao
interface DisqualificationDao {

    // Insert operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(disqualification: Disqualification): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(disqualifications: List<Disqualification>): List<Long>

    // Update operations
    @Update
    suspend fun update(disqualification: Disqualification)

    @Query("UPDATE Disqualification SET graveDefectSum = :newValue WHERE id = :id")
    suspend fun updateGraveDefectSum(id: Int, newValue: Int)

    @Query("UPDATE Disqualification SET classificationId = :classificationId WHERE id = :id")
    suspend fun updateClassificationId(id: Int, classificationId: Int)

    // Delete operations
    @Delete
    suspend fun delete(disqualification: Disqualification)

    @Query("DELETE FROM Disqualification")
    suspend fun deleteAll()

    // Query operations
    @Query("SELECT * FROM Disqualification")
    suspend fun getAll(): List<Disqualification>

    @Query("SELECT * FROM Disqualification WHERE id = :id")
    suspend fun getById(id: Int): Disqualification?

    @Query("SELECT * FROM Disqualification WHERE classificationId = :classificationId")
    suspend fun getByClassificationId(classificationId: Int): Disqualification

    @Query("SELECT * FROM Disqualification ORDER BY id DESC LIMIT 1")
    suspend fun getLastDisqualification(): Disqualification

    @Query("SELECT id FROM Disqualification ORDER BY id DESC LIMIT 1")
    suspend fun getLastDisqualificationId(): Int

}