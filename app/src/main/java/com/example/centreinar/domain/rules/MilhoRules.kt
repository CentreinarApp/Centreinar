package com.example.centreinar.domain.rules

import javax.inject.Inject


enum class GroupMilho {
    DURO,
    DENTADO,
    SEMIDURO,
    NAO_DEFINIDO
}

enum class ClassMilho {
    AMARELA,
    BRANCA,
    CORES,
    NAO_DEFINIDA
}

data class GroupMilhoResult(
    val hardPct: Float,
    val dentPct: Float,
    val semiDuroPct: Float,
    val finalGroup: GroupMilho
)

data class ClassMilhoResult(
    val yellowPct: Float,
    val finalClass: ClassMilho
)

class MilhoRules @Inject constructor() {
    fun calculateClass(
        weightYellow: Float,
        weightWhite: Float,
        weightMixedColors: Float
    ): ClassMilhoResult {
        val total = weightYellow + weightWhite + weightMixedColors

        if (total == 0f) {
            return ClassMilhoResult(
                0f,
                ClassMilho.NAO_DEFINIDA
            )
        }

        val yellowPct = (weightYellow / total) * 100f
        val whitePct = (weightWhite / total) * 100f

        val result = when {
            yellowPct >= 95f -> ClassMilho.AMARELA
            whitePct >= 95f -> ClassMilho.BRANCA
            else -> ClassMilho.CORES
        }

        return ClassMilhoResult(
            yellowPct = yellowPct,
            finalClass = result
        )
    }

    fun calculateGroup(
        weightHard: Float,
        weightDent: Float,
        weightSemiHard: Float
    ): GroupMilhoResult {
        val totalGroupWeight = weightHard + weightDent + weightSemiHard

        if (totalGroupWeight == 0f) {
            return GroupMilhoResult(
                hardPct = 0f,
                dentPct = 0f,
                semiDuroPct = 0f,
                finalGroup = GroupMilho.NAO_DEFINIDO
            )
        }

        val calculatedHardPct = (weightHard / totalGroupWeight) * 100f
        val calculatedDentPct = (weightDent / totalGroupWeight) * 100f
        val calculatedSemiDuroPct = (weightSemiHard / totalGroupWeight) * 100f

        val finalGroupResult = when {
            calculatedHardPct >= 85f -> GroupMilho.DURO
            calculatedDentPct >= 85f -> GroupMilho.DENTADO
            else -> GroupMilho.SEMIDURO
        }

        return GroupMilhoResult(
            calculatedHardPct,
            calculatedDentPct,
            calculatedSemiDuroPct,
            finalGroupResult
        )
    }
}