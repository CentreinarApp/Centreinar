package com.example.centreinar.data.local.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.example.centreinar.data.local.entity.DisqualificationMilho
import com.example.centreinar.data.local.entity.ToxicSeedMilho

data class DisqualificationMilhoWithToxicSeeds(
    @Embedded val disqualification: DisqualificationMilho,

    @Relation(
        parentColumn = "id",                // O ID da DisqualificationMilho
        entityColumn = "disqualificationId" // O ID na ToxicSeedSoja que aponta para o pai
    )
    val toxicSeeds: List<ToxicSeedMilho>
)