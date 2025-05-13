package com.example.centreinar

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

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
    suspend fun updateGraveDefectSum(id: Int, newValue: Boolean)

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
    suspend fun getByClassificationId(classificationId: Int): List<Disqualification>

    // Additional query examples
    @Query("SELECT * FROM Disqualification WHERE toxicGrains = :toxic")
    suspend fun getByToxicGrains(toxic: Boolean): List<Disqualification>
}