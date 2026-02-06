package com.example.centreinar.data.local.dao

import androidx.room.*
import com.example.centreinar.data.local.entity.LimitMilho

@Dao
interface LimitMilhoDao {
    @Query("SELECT * FROM limits_milho")
    suspend fun getAllLimits(): List<LimitMilho>

    @Insert
    suspend fun insertLimit(limit: LimitMilho): Long

    @Delete
    suspend fun deleteLimit(limit: LimitMilho)

    @Update
    suspend fun updateLimit(limit: LimitMilho)

    @Query("SELECT * FROM limits_milho WHERE id = :id")
    suspend fun getLimitById(id: Long): List<LimitMilho>

    @Query("SELECT * FROM limits_milho WHERE grain = :grain")
    suspend fun getLimitsByGrain(grain: String): List<LimitMilho>

    // Esta função retorna uma lista. O Repositório usará .firstOrNull() para segurança.
    @Query("SELECT * FROM limits_milho WHERE grain = :grain AND source = :limitSource AND `group` = :group")
    suspend fun getLimitsBySource(grain: String, limitSource: Int, group: Int): List<LimitMilho>

    // Adiciono a função getLimitsByType, caso você a use em outra parte (semelhante ao DAO de Soja),
    // mas corrigida para retornar uma lista que pode ser vazia.
    @Query("SELECT * FROM limits_milho WHERE grain = :grain AND type = :tipo AND `group` = :group AND source = :limitSource")
    suspend fun getLimitsByType(grain: String, group: Int, tipo: Int, limitSource: Int): List<LimitMilho>

    @Query("SELECT source FROM limits_milho ORDER BY source DESC LIMIT 1")
    suspend fun getLastSource(): Int

    @Query("SELECT * FROM limits_milho WHERE grain = :grain AND `group` = :group AND source = :limitSource ORDER BY type ASC")
    suspend fun getLimitsForTable(grain: String, group: Int, limitSource: Int): List<LimitMilho>

    @Query("DELETE FROM limits_milho WHERE source != 0")
    suspend fun deleteCustomLimits()
}