package com.example.centreinar.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.centreinar.data.local.entity.DisqualificationMilho

class DisqualificationMilhoDAO {
    @Dao
    interface DisqualificationMilhoDao {

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insert(disqualification: DisqualificationMilho): Long

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertAll(disqualifications: List<DisqualificationMilho>): List<Long>

        @Update
        suspend fun update(disqualification: DisqualificationMilho)

        @Delete
        suspend fun delete(disqualification: DisqualificationMilho)

        @Query("DELETE FROM disqualification_milho")
        suspend fun deleteAll()

        @Query("SELECT * FROM disqualification_milho")
        suspend fun getAll(): List<DisqualificationMilho>

        @Query("SELECT * FROM disqualification_milho WHERE id = :id")
        suspend fun getById(id: Int): DisqualificationMilho?

        @Query("SELECT * FROM disqualification_milho WHERE classificationId = :classificationId")
        suspend fun getByClassificationId(classificationId: Int): DisqualificationMilho?

        @Query("SELECT * FROM disqualification_milho ORDER BY id DESC LIMIT 1")
        suspend fun getLastDisqualification(): DisqualificationMilho?
    }

}