package com.example.centreinar.data.local.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.example.centreinar.DisqualificationSoja
import com.example.centreinar.data.local.entities.ToxicSeedSoja

data class DisqualificationWithToxicSeeds(
    @Embedded val disqualification: DisqualificationSoja,

    @Relation(
        parentColumn = "id", // O ID da DisqualificationSoja
        entityColumn = "disqualificationId" // O ID na ToxicSeedSoja que aponta para o pai
    )
    val toxicSeeds: List<ToxicSeedSoja>
)