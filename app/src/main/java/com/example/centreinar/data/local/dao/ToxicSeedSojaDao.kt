package com.example.centreinar.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.centreinar.data.local.entities.ToxicSeedSoja

@Dao
interface ToxicSeedSojaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(seeds: List<ToxicSeedSoja>)

    @Query("SELECT * FROM toxic_seed_soja WHERE disqualificationId = :disqualificationId")
    suspend fun getSeedsByDisqualificationId(disqualificationId: Int): List<ToxicSeedSoja>

    @Query("DELETE FROM toxic_seed_soja WHERE disqualificationId = :disqualificationId")
    suspend fun deleteSeedsByDisqualificationId(disqualificationId: Int)
}