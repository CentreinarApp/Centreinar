package com.example.centreinar.data.local.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.example.centreinar.data.local.entity.DisqualificationMilho
import com.example.centreinar.data.local.entity.ToxicSeedMilho

data class DisqualificationMilhoWithToxicSeeds(
    @Embedded val disqualification: DisqualificationMilho,

    @Relation(
        parentColumn = "id",
        entityColumn = "disqualificationId"
    )
    val toxicSeeds: List<ToxicSeedMilho>
)