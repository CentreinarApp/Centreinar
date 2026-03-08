package com.example.centreinar.domain.usecase

import com.example.centreinar.ClassificationSoja
import com.example.centreinar.ColorClassificationSoja
import com.example.centreinar.DisqualificationSoja
import com.example.centreinar.LimitSoja
import com.example.centreinar.SampleSoja
import com.example.centreinar.data.local.entities.ToxicSeedSoja
import com.example.centreinar.data.repository.ClassificationRepository // Interface do repositório da Soja
import com.example.centreinar.domain.rules.SojaRules
import javax.inject.Inject

data class ClassificationSojaResult(
    val resultId: Long,
    val limitUsed: LimitSoja?,
    val classification: ClassificationSoja?,
    val disqualification: DisqualificationSoja?,
    val toxicSeeds: List<ToxicSeedSoja>,
    val colorClassification: ColorClassificationSoja?
)

class ClassifySojaUseCase @Inject constructor(
    private val repositorySoja: ClassificationRepository,
    private val sojaRules: SojaRules
) {
    suspend fun execute(
        sample: SampleSoja,
        otherColorsWeight: Float,
        baseWeightCor: Float,
        isColorDefined: Boolean,
        isOfficial: Boolean
    ): ClassificationSojaResult {
        // Busca os Limites
        val source = if (!isOfficial) repositorySoja.getLastLimitSource() else 0
        val limitUsed = repositorySoja.getLimit(sample.grain, sample.group, 1, source)

        // Busca a Desclassificação Anterior
        val lastDisqId = repositorySoja.getLastDisqualificationId()
        val lastDisq = repositorySoja.getLastDisqualification()

        requireNotNull(lastDisq) { "Nenhuma desclassificação de Soja encontrada para vincular à amostra." }

        // Salva a Classificação
        val resultId = repositorySoja.classifySample(sample, source, lastDisq)

        // Atualiza o ID da Classificação na Desclassificação
        lastDisqId?.let {
            repositorySoja.updateClassificationIdOnDisqualification(it, resultId.toInt())
        }

        // Busca os Resultados Salvos
        val classification = repositorySoja.getClassification(resultId.toInt())
        val disqSoja = repositorySoja.getDisqualificationByClassificationId(resultId.toInt())
        val toxicSeeds = disqSoja?.let {
            repositorySoja.getToxicSeedsByDisqualificationId(it.id)
        } ?: emptyList()

        // Aplica Regras de Cor e Salva (se necessário)
        var colorClassification: ColorClassificationSoja? = null

        if (isColorDefined) {
            val colorResult = sojaRules.calculateColor(otherColorsWeight, baseWeightCor)

            if (colorResult != null) {
                colorClassification = ColorClassificationSoja(
                    grain = "Soja",
                    classificationId = resultId.toInt(),
                    yellowPercentage = colorResult.yellowPct,
                    otherColorPercentage = colorResult.otherColorPct,
                    framingClass = colorResult.framingClass
                ).also {
                    repositorySoja.insertColorClassification(it)
                }
            }
        }

        // Retorna o Pacote Completo para a UI
        return ClassificationSojaResult(
            resultId = resultId,
            limitUsed = limitUsed,
            classification = classification,
            disqualification = disqSoja,
            toxicSeeds = toxicSeeds,
            colorClassification = colorClassification
        )
    }
}