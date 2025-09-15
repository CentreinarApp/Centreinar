package com.example.centreinar.data.repository

import com.example.centreinar.ClassificationMilho
import com.example.centreinar.data.local.dao.ClassificationMilhoDao
import com.example.centreinar.data.local.dao.LimitMilhoDao
import com.example.centreinar.data.local.dao.SampleMilhoDao
import com.example.centreinar.data.local.entity.ClassificationMilho
import com.example.centreinar.data.local.entity.LimitMilho
import com.example.centreinar.data.local.entity.SampleMilho
import com.example.centreinar.util.Utilities
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

@Singleton
class ClassificationRepositoryMilhoImpl @Inject constructor(
    private val limitDao: LimitMilhoDao,
    private val classificationDao: ClassificationMilhoDao,
    private val sampleDao: SampleMilhoDao,
    private val tools: Utilities
) : ClassificationRepositoryMilho {

    /**
     * Classifica a amostra de milho.
     *
     * Lógica (assunções):
     * - Calcula porcentagens dos defeitos usando sample.sampleWeight.
     * - Obtém os limites (por tipo) via limitDao.getLimitsBySource(grain, group, limitSource).
     * - Se ardido + mofado > DESCLASS_LIMIT (assunção: 12% para grupo 1, 40% para grupo 2 — como soja),
     *   marca finalType = 0 (desclassificado).
     * - Se qualquer defeito exceder o limite do tipo 2 (ou do tipo Padrão) marca como Fora de Tipo (finalType = 2).
     * - Caso contrário finalType = 1 (dentro do tipo).
     *
     * Esses valores e regras são facilmente alteráveis no código.
     */
    override suspend fun classifySample(sample: SampleMilho, limitSource: Int): Long {
        // 1) persiste amostra e pega id
        val sampleId = setSample(sample) // retorna id inserido pelo DAO
        val savedSampleId = sampleId.toInt()
        // atualizar sampleId na classificação futura
        val sampleForCalc = sample.copy() // imutabilidade: usar os valores que vieram

        // 2) calcular porcentagens (por 100)
        // se sampleWeight == 0 evitamos div/0
        val denom = if (sampleForCalc.sampleWeight > 0f) sampleForCalc.sampleWeight else 1f

        val impuritiesPerc = tools.calculateDefectPercentage(sampleForCalc.impurities, denom)
        val brokenPerc = tools.calculateDefectPercentage(sampleForCalc.broken, denom)
        val ardidoPerc = tools.calculateDefectPercentage(sampleForCalc.ardido, denom)
        val mofadoPerc = tools.calculateDefectPercentage(sampleForCalc.mofado, denom)
        val carunchadoPerc = tools.calculateDefectPercentage(sampleForCalc.carunchado, denom)

        // 3) buscar limites (lista de LimitMilho por tipos)
        val limitsList = limitDao.getLimitsBySource(sampleForCalc.grain, sampleForCalc.group, limitSource)
        // converter em mapa por type para fácil lookup
        val limitsByType: Map<Int, LimitMilho> = limitsList.associateBy { it.type }

        // função auxiliar para pegar limiteUp de um campo para um determinado type (se existir)
        fun getLimitUp(type: Int, selector: (LimitMilho) -> Float, default: Float = Float.MAX_VALUE): Float {
            val limit = limitsByType[type]
            return limit?.let(selector) ?: default
        }

        // 4) Decisão de desclassificação por defeitos graves
        // Assumimos regra análoga à soja: ardido + mofado > X => desclassificado
        val desclassLimit = if (sampleForCalc.group == 1) 12f else 40f
        if (ardidoPerc + mofadoPerc > desclassLimit) {
            // grava classificação com finalType = 0
            val classification = ClassificationMilho(
                grain = sampleForCalc.grain,
                group = sampleForCalc.group,
                sampleId = savedSampleId,
                impuritiesPercentage = impuritiesPerc,
                brokenPercentage = brokenPerc,
                ardidoPercentage = ardidoPerc,
                mofadoPercentage = mofadoPerc,
                carunchadoPercentage = carunchadoPerc,
                finalType = 0
            )
            return classificationDao.insert(classification)
        }

        // 5) Verificar se ultrapassa qualquer limite relevante (Fora de Tipo)
        // Aqui, usamos TYPE = 2 as "limites mais restritivos" (ajustável).
        // Se qualquer percentual exceder o limiteUp do tipo 2 então será Fora de Tipo (finalType = 2)
        val typeToCheck = 2
        val impuritiesLimit = getLimitUp(typeToCheck) { it.impuritiesUpLim }
        val brokenLimit = getLimitUp(typeToCheck) { it.brokenUpLim }
        val ardidoLimit = getLimitUp(typeToCheck) { it.ardidoUpLim }
        val mofadoLimit = getLimitUp(typeToCheck) { it.mofadoUpLim }
        val carunchadoLimit = getLimitUp(typeToCheck) { it.carunchadoUpLim }

        val exceedsAny = (impuritiesPerc > impuritiesLimit) ||
                (brokenPerc > brokenLimit) ||
                (ardidoPerc > ardidoLimit) ||
                (mofadoPerc > mofadoLimit) ||
                (carunchadoPerc > carunchadoLimit)

        val finalType = if (exceedsAny) 2 else 1

        val classification = ClassificationMilho(
            grain = sampleForCalc.grain,
            group = sampleForCalc.group,
            sampleId = savedSampleId,
            impuritiesPercentage = impuritiesPerc,
            brokenPercentage = brokenPerc,
            ardidoPercentage = ardidoPerc,
            mofadoPercentage = mofadoPerc,
            carunchadoPercentage = carunchadoPerc,
            finalType = finalType
        )

        return classificationDao.insert(classification)
    }

    override suspend fun getSample(id: Int): SampleMilho? {
        return sampleDao.getById(id)
    }

    override suspend fun setSample(sample: SampleMilho): Long {
        // salva via DAO e retorna id
        return sampleDao.insert(sample)
    }

    override suspend fun getClassification(id: Int): ClassificationMilho? {
        return classificationDao.getById(id)
    }

    override suspend fun getLimitsForGrain(grain: String, group: Int, limitSource: Int): LimitMilho {
        // pegar o limite "tipo 1" por padrão (padrão básico) — ou ajustar conforme necessário
        return limitDao.getLimitsBySource(grain, group, limitSource)
            .firstOrNull() ?: throw IllegalStateException("No limits found for $grain group=$group source=$limitSource")
    }
}
