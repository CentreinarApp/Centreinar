package com.example.centreinar.data.local.dao

import androidx.room.*
import com.example.centreinar.data.local.entity.DisqualificationMilho

@Dao
interface DisqualificationMilhoDao {
    @Insert
    suspend fun insert(disqualification: DisqualificationMilho): Long

    @Update
    suspend fun update(disqualification: DisqualificationMilho)

    @Delete
    suspend fun delete(disqualification: DisqualificationMilho)

    @Query("SELECT * FROM disqualification_milho WHERE classificationId = :classificationId LIMIT 1")
    suspend fun getByClassificationId(classificationId: Int): DisqualificationMilho?

    @Query("SELECT * FROM disqualification_milho ORDER BY id DESC LIMIT 1")
    suspend fun getLastDisqualification(): DisqualificationMilho?

    @Query("SELECT id FROM disqualification_milho ORDER BY id DESC LIMIT 1")
    suspend fun getLastDisqualificationId(): Int?

    @Query("UPDATE disqualification_milho SET classificationId = :classificationId WHERE id = :id")
    suspend fun updateClassificationId(id: Int, classificationId: Int)

    @Query("UPDATE disqualification_milho SET graveDefectSum = :graveDefectSum WHERE id = :id")
    suspend fun updateGraveDefectSum(id: Int, graveDefectSum: Int)
}

