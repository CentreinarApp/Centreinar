package com.example.centreinar.domain.usecase

import com.example.centreinar.ClassificationMilho
import com.example.centreinar.data.local.entity.ColorClassificationMilho
import com.example.centreinar.data.local.entity.DisqualificationMilho
import com.example.centreinar.data.local.entity.LimitMilho
import com.example.centreinar.data.local.entity.SampleMilho
import com.example.centreinar.data.local.entity.ToxicSeedMilho
import com.example.centreinar.data.repository.ClassificationRepositoryMilho
import com.example.centreinar.domain.rules.ClassMilhoResult
import com.example.centreinar.domain.rules.GroupMilhoResult
import com.example.centreinar.domain.rules.MilhoRules
import javax.inject.Inject

data class ClassificationResult(
    val resultId: Long,
    val limitUsed: LimitMilho?,
    val classification: ClassificationMilho?,
    val disqualification: DisqualificationMilho?,
    val toxicSeeds: List<ToxicSeedMilho>,
    val complementaryData: ColorClassificationMilho?
)

class ClassifyMilhoUseCase @Inject constructor (
    private val repositoryMilho: ClassificationRepositoryMilho,
    private val milhoRules: MilhoRules
) {
    suspend fun execute(
        sample: SampleMilho,
        shouldDefineClass: Boolean,
        shouldDefineGroup: Boolean,
        weightYellow: Float,
        weightWhite: Float,
        weightMixedColors: Float,
        weightHard: Float,
        weightDent: Float,
        weightSemiHard: Float,
        isOfficial: Boolean
    ): ClassificationResult {
        // Busca os limites
        val limitSource =
            if (!isOfficial) repositoryMilho.getLastLimitSource() else 0
        val limitUsed =
            repositoryMilho.getLimit(sample.grain, sample.group, 1, limitSource)

        // Busca a desclassificação anterior
        val lastDisqId = repositoryMilho.getLastDisqualificationId()
        val lastDisq = repositoryMilho.getLastDisqualification()

        val safeDisq = lastDisq ?: DisqualificationMilho(
            classificationId = 0,
            badConservation = 0,
            strangeSmell = 0,
            insects = 0,
            toxicGrains = 0
        )

        // Salva a classificação
        val resultId =
            repositoryMilho.classifySample(sample, limitSource, safeDisq)

        // Atualiza o ID da classificação na desclassificação
        lastDisqId?.let {
            repositoryMilho.updateClassificationIdOnDisqualification(it, resultId.toInt())
        }

        // Busca os resultados salvos
        val classification =
            repositoryMilho.getClassification(resultId.toInt())
        val disqMilho =
            repositoryMilho.getDisqualificationByClassificationId(resultId.toInt())
        val toxicSeeds =
            disqMilho?.let {
                repositoryMilho.getToxicSeedsByDisqualificationId(it.id)
            } ?: emptyList()

        // Aplica regras de classe e grupo e salva (se necessário)
        val classResult =
            if (shouldDefineClass)
                milhoRules.calculateClass(
                    weightYellow,
                    weightWhite,
                    weightMixedColors
                )
            else null

        val groupResult =
            if (shouldDefineGroup)
                milhoRules.calculateGroup(
                    weightHard,
                    weightDent,
                    weightSemiHard
                )
            else null

        val complementaryData =
            if (shouldDefineClass || shouldDefineGroup) {

                ColorClassificationMilho(
                    classificationId = resultId,
                    yellowPercentage = classResult?.yellowPct ?: 0f,
                    otherColorPercentage =
                        classResult?.let { 100f - it.yellowPct } ?: 0f,
                    framingClass = classResult?.finalClass?.name ?: "",
                    duroPercentage = groupResult?.hardPct ?: 0f,
                    dentadoPercentage = groupResult?.dentPct ?: 0f,
                    semiDuroPercentage = groupResult?.semiDuroPct ?: 0f,
                    framingGroup = groupResult?.finalGroup?.name ?: ""
                ).also {
                    repositoryMilho.insertColorClassificationMilho(it)
                }

            } else null

        // Retorna o pacote completo para a UI
        return ClassificationResult(
            resultId = resultId,
            limitUsed = limitUsed,
            classification = classification,
            disqualification = disqMilho,
            toxicSeeds = toxicSeeds,
            complementaryData = complementaryData
        )
    }
}