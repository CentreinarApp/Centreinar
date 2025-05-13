package com.example.centreinar
import androidx.room.*
import com.example.centreinar.Limit
import com.example.centreinar.LimitCategory

@Dao
interface LimitDao{

    @Query("SELECT * FROM limits")
    suspend fun getAllLimits(): List<Limit>

    @Insert
    suspend fun insertLimit(limit: Limit)

    @Delete
    suspend fun deleteLimit(limit: Limit)

    @Update
    suspend fun updateLimit(limit: Limit)

    @Query("SELECT * FROM limits WHERE id = :id")
    suspend fun getLimitById(id:Long): List<Limit>

    @Query("SELECT * FROM limits WHERE grain = :grain")
    suspend fun getLimitsByGrain(grain:String): List<Limit>

    @Query("SELECT * FROM limits WHERE grain = :grain AND source = :limitSource AND `group` = :group")
    suspend fun getLimitsBySource(grain:String,group: Int,limitSource:Int): List<Limit>

    @Query("SELECT * FROM limits WHERE grain = :grain AND type = :tipo AND `group` = :group AND source = :limitSource")
    suspend fun getLimitsByType(grain:String,group: Int,tipo:Int, limitSource: Int): Limit

    @Query("SELECT type,impuritiesLowerLim AS lowerL , impuritiesUpLim AS upperL FROM limits WHERE grain = :grain AND `group` = :group AND source = :limitSource")
    suspend fun getLimitsForImpurities(grain:String,group:Int,limitSource: Int):List<LimitCategory>

    @Query("SELECT type,brokenCrackedDamagedLowerLim AS lowerL ,brokenCrackedDamagedUpLim AS upperL FROM limits WHERE grain = :grain AND `group` = :group AND source = :limitSource")
    suspend fun getLimitsForBrokenCrackedDamaged(grain:String,group:Int,limitSource: Int):List<LimitCategory>

    @Query("SELECT type,greenishLowerLim AS lowerL,greenishUpLim AS upperL FROM limits WHERE grain = :grain AND `group` = :group AND source = :limitSource")
    suspend fun getLimitsForGreenish(grain:String,group:Int,limitSource: Int):List<LimitCategory>

    @Query("SELECT type, burntLowerLim AS lowerL, burntUpLim  AS upperL FROM limits WHERE grain = :grain AND `group` = :group AND source = :limitSource")
    suspend fun getLimitsForBurnt(grain:String,group:Int,limitSource: Int):List<LimitCategory>

    @Query("SELECT type,moldyLowerLim AS lowerL ,moldyUpLim AS upperL FROM limits WHERE grain = :grain AND `group` = :group AND source = :limitSource")
    suspend fun getLimitsForMoldy(grain:String,group:Int,limitSource: Int):List<LimitCategory>

    @Query("SELECT type, burntOrSourLowerLim AS lowerL, burntOrSourUpLim AS upperL FROM limits WHERE grain = :grain AND `group` = :group AND source = :limitSource")
    suspend fun getLimitsForBurntOrSour(grain:String,group:Int,limitSource: Int):List<LimitCategory>

    @Query("SELECT type,spoiledTotalLowerLim AS lowerL ,spoiledTotalUpLim AS upperL FROM limits WHERE grain = :grain AND `group` = :group AND source = :limitSource")
    suspend fun getLimitsForSpoiledTotal(grain:String,group:Int,limitSource: Int):List<LimitCategory>

}