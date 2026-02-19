package com.example.centreinar.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.centreinar.data.local.entity.ToxicSeedMilho

@Dao
interface ToxicSeedMilhoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(seeds: List<ToxicSeedMilho>)

    @Query("SELECT * FROM toxic_seed_milho WHERE disqualificationId = :disqualificationId")
    suspend fun getToxicSeedsByDisqualificationId(disqualificationId: Int): List<ToxicSeedMilho>
}