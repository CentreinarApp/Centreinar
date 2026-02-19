package com.example.centreinar.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.centreinar.DisqualificationSoja

@Entity(
    tableName = "toxic_seed_soja",
    foreignKeys = [
        ForeignKey(
            entity = DisqualificationSoja::class,
            parentColumns = ["id"],
            childColumns = ["disqualificationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["disqualificationId"])]
)
data class ToxicSeedSoja(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val disqualificationId: Int, // O Vínculo com a tabela de Desclassificação
    val name: String,
    val quantity: Int
)