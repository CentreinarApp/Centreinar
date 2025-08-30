package com.example.centreinar.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.centreinar.LimitSoja
import com.example.centreinar.domain.model.LimitCategory

@Dao
interface LimitSojaDao {

    @Query("SELECT * FROM limits_soja")
    suspend fun getAllLimits(): List<LimitSoja>

    @Insert
    suspend fun insertLimit(limit: LimitSoja): Long

    @Delete
    suspend fun deleteLimit(limit: LimitSoja)

    @Update
    suspend fun updateLimit(limit: LimitSoja)

    @Query("SELECT * FROM limits_soja WHERE id = :id")
    suspend fun getLimitById(id: Long): List<LimitSoja>

    @Query("SELECT * FROM limits_soja WHERE grain = :grain")
    suspend fun getLimitsByGrain(grain: String): List<LimitSoja>

    @Query("SELECT * FROM limits_soja WHERE grain = :grain AND source = :limitSource AND `group` = :group")
    suspend fun getLimitsBySource(grain: String, group: Int, limitSource: Int): List<LimitSoja>

    @Query("SELECT * FROM limits_soja WHERE grain = :grain AND type = :tipo AND `group` = :group AND source = :limitSource")
    suspend fun getLimitsByType(grain: String, group: Int, tipo: Int, limitSource: Int): LimitSoja

    @Query("SELECT type, impuritiesLowerLim AS lowerL, impuritiesUpLim AS upperL FROM limits_soja WHERE grain = :grain AND `group` = :group AND source = :limitSource")
    suspend fun getLimitsForImpurities(grain: String, group: Int, limitSource: Int): List<LimitCategory>

    @Query("SELECT type, brokenCrackedDamagedLowerLim AS lowerL, brokenCrackedDamagedUpLim AS upperL FROM limits_soja WHERE grain = :grain AND `group` = :group AND source = :limitSource")
    suspend fun getLimitsForBrokenCrackedDamaged(grain: String, group: Int, limitSource: Int): List<LimitCategory>

    @Query("SELECT type, greenishLowerLim AS lowerL, greenishUpLim AS upperL FROM limits_soja WHERE grain = :grain AND `group` = :group AND source = :limitSource")
    suspend fun getLimitsForGreenish(grain: String, group: Int, limitSource: Int): List<LimitCategory>

    @Query("SELECT type, burntLowerLim AS lowerL, burntUpLim AS upperL FROM limits_soja WHERE grain = :grain AND `group` = :group AND source = :limitSource")
    suspend fun getLimitsForBurnt(grain: String, group: Int, limitSource: Int): List<LimitCategory>

    @Query("SELECT type, moldyLowerLim AS lowerL, moldyUpLim AS upperL FROM limits_soja WHERE grain = :grain AND `group` = :group AND source = :limitSource")
    suspend fun getLimitsForMoldy(grain: String, group: Int, limitSource: Int): List<LimitCategory>

    @Query("SELECT type, burntOrSourLowerLim AS lowerL, burntOrSourUpLim AS upperL FROM limits_soja WHERE grain = :grain AND `group` = :group AND source = :limitSource")
    suspend fun getLimitsForBurntOrSour(grain: String, group: Int, limitSource: Int): List<LimitCategory>

    @Query("SELECT type, spoiledTotalLowerLim AS lowerL, spoiledTotalUpLim AS upperL FROM limits_soja WHERE grain = :grain AND `group` = :group AND source = :limitSource")
    suspend fun getLimitsForSpoiledTotal(grain: String, group: Int, limitSource: Int): List<LimitCategory>

    @Query("SELECT source FROM limits_soja ORDER BY source DESC LIMIT 1")
    suspend fun getLastSource(): Int
}
