package com.example.centreinar.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "toxic_seed_milho",
    foreignKeys = [
        ForeignKey(
            entity = DisqualificationMilho::class,
            parentColumns = ["id"],
            childColumns = ["disqualificationId"],
            onDelete = ForeignKey.CASCADE // Apaga a semente se a desclassificação for apagada
        )
    ]
)
data class ToxicSeedMilho(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val disqualificationId: Int, // Chave que vincula à DisqualificationMilho
    val name: String,
    val quantity: Int
)