package com.example.centreinar.ui.classificationProcess.strategy

import android.content.Context
import android.util.Log
import com.example.centreinar.ClassificationSoja
import com.example.centreinar.DisqualificationSoja
import com.example.centreinar.LimitSoja
import com.example.centreinar.SampleSoja
import com.example.centreinar.data.local.entities.ToxicSeedSoja
import com.example.centreinar.data.repository.ClassificationRepository
import com.example.centreinar.domain.model.GrainDescriptor
import com.example.centreinar.domain.model.LimitField
import com.example.centreinar.domain.usecase.ClassifySojaUseCase
import com.example.centreinar.util.ClassificationPdfPayload
import com.example.centreinar.util.FieldKeys
import com.example.centreinar.util.PDFExporter
import com.example.centreinar.util.PdfDisqualificationData
import com.example.centreinar.util.PdfLimitRow
import com.example.centreinar.util.PdfSampleData
import com.example.centreinar.util.PdfTableRow
import javax.inject.Inject

class SojaClassificationStrategy @Inject constructor(
    private val repositorySoja: ClassificationRepository,
    private val classifySojaUseCase: ClassifySojaUseCase,
    private val pdfExporter: PDFExporter
) : GrainStrategy {

    override val descriptor = GrainDescriptor(
        name               = "Soja",
        displayName        = "Soja",
        colorScheme        = "primary",
        supportsGroups     = true,
        supportsColorClass = true
    )

    override fun buildPayload(state: ClassificationInputState): ClassificationPayload {
        val sample = SampleSoja(
            grain = grainName,
            group = state.group,
            lotWeight = state.lotWeight,
            sampleWeight = state.sampleWeight,
            moisture = state.moisture,
            foreignMattersAndImpurities = state.foreignMatters,
            greenish = state.greenish,
            brokenCrackedDamaged = state.brokenCrackedDamaged,
            damaged = state.damaged,
            burnt = state.burnt,
            sour = state.sour,
            moldy = state.moldy,
            fermented = state.fermented,
            germinated = state.germinated,
            immature = state.immature,
            shriveled = state.shriveled
        )
        return ClassificationPayload.Soja(
            sample            = sample,
            otherColorsWeight = state.otherColorsWeight,
            baseWeightCor     = state.baseWeightForColor,
            isColorDefined    = state.isColorDefined
        )
    }

    override fun getSampleInputRows(state: ClassificationUIState): List<SampleInputRow> {
        return state.sampleInputRows
    }

    override suspend fun getOfficialLimits(group: Int): List<Any> {
        return repositorySoja.getLimitsByGroup(grainName, group, 0)
    }

    override suspend fun getBaseLimits(group: Int): Map<String, Float>? {
        return repositorySoja.getLimitOfType1Official(group, grainName).takeIf { it.isNotEmpty() }
    }

    override suspend fun getUiStateData(id: Int): StrategyResultData {
        val classification = repositorySoja.getClassification(id)
            ?: throw Exception("Classificação de Soja não encontrada")

        val sample = repositorySoja.getSample(classification.sampleId)

        val disqualification = repositorySoja.getDisqualificationByClassificationId(id)

        val lastSource = repositorySoja.getLastLimitSource()
        val limits = repositorySoja.getLimit(
            grain  = "Soja",
            group  = classification.group,
            tipo   = classification.finalType,
            source = lastSource
        ) ?: throw Exception("Limites de referência não encontrados")

        val toxicSeeds = disqualification?.let {
            repositorySoja.getToxicSeedsByDisqualificationId(it.id)
        } ?: emptyList()

        val rows = listOf(
            ClassificationRow("Matérias Estranhas e Impurezas", classification.impuritiesPercentage, classification.impuritiesType, limits.impuritiesUpLim),
            ClassificationRow("Ardidos", classification.sourPercentage, 0, 0f),
            ClassificationRow("Queimados", classification.burntPercentage, classification.burntType, limits.burntUpLim),
            ClassificationRow("Total de Ardidos e Queimados", classification.burntOrSourPercentage, classification.burntOrSourType, limits.burntOrSourUpLim),
            ClassificationRow("Mofados", classification.moldyPercentage, classification.moldyType, limits.moldyUpLim),
            ClassificationRow("Fermentados", classification.fermentedPercentage, 0, 0f),
            ClassificationRow("Germinados", classification.germinatedPercentage, 0, 0f),
            ClassificationRow("Imaturos", classification.immaturePercentage, 0, 0f),
            ClassificationRow("Chochos", classification.shriveledPercentage, 0, 0f),
            ClassificationRow("Danificados", classification.damagedPercentage, 0, 0f),
            ClassificationRow("Total de Avariados", classification.spoiledPercentage, classification.spoiledType, limits.spoiledTotalUpLim),
            ClassificationRow("Esverdeados", classification.greenishPercentage, classification.greenishType, limits.greenishUpLim),
            ClassificationRow("Partidos/Quebrados", classification.brokenCrackedDamagedPercentage, classification.brokenCrackedDamagedType, limits.brokenCrackedDamagedUpLim)
        )

        val colorClass = repositorySoja.getColorClassificationBySample(id)
        val cards = mutableListOf<ComplementaryCardData>()
        colorClass?.let {
            cards.add(
                ComplementaryCardData(
                    title    = it.framingClass,
                    subtitle = "Amarela: %.2f%% | Outras Cores: %.2f%%".format(it.yellowPercentage, it.otherColorPercentage),
                    colorType = "tertiary"
                )
            )
        }

        return StrategyResultData(
            classification   = classification,
            disqualification = disqualification,
            limit            = limits,
            tableRows        = rows,
            toxicSeeds       = toxicSeeds.map { it.name to it.quantity },
            cards            = cards,
            sampleLotWeight  = sample?.lotWeight ?: 0f
        )
    }

    override fun getLimitFields(): List<LimitField> = listOf(
        LimitField(FieldKeys.BURNT_OR_SOUR, "Ardidos e Queimados (%)"),
        LimitField(FieldKeys.BURNT,         "Queimados (%)"),
        LimitField(FieldKeys.MOLDY,         "Mofados (%)"),
        LimitField(FieldKeys.SPOILED,       "Total Avariados (%)"),
        LimitField(FieldKeys.GREENISH,      "Esverdeados (%)"),
        LimitField(FieldKeys.BROKEN,        "Partidos, Quebrados e Amassados (%)"),
        LimitField(FieldKeys.IMPURITIES, "Matérias Estranhas e Impurezas (%)")
    )

    override suspend fun classify(payload: ClassificationPayload, isOfficial: Boolean): ClassificationUIState {
        require(payload is ClassificationPayload.Soja) { "Payload incorreto para a estratégia de Soja" }

        val result = classifySojaUseCase.execute(
            sample            = payload.sample,
            otherColorsWeight = payload.otherColorsWeight,
            baseWeightCor     = payload.baseWeightCor,
            isColorDefined    = payload.isColorDefined,
            isOfficial        = isOfficial
        )

        val mappedToxicSeeds = result.toxicSeeds.map { seed -> Pair(seed.name, seed.quantity) }

        val cards = mutableListOf<ComplementaryCardData>()
        result.colorClassification?.let { colorInfo ->
            cards.add(
                ComplementaryCardData(
                    title    = colorInfo.framingClass,
                    subtitle = "Amarela: %.2f%% | Outras Cores: %.2f%%".format(
                        colorInfo.yellowPercentage,
                        colorInfo.otherColorPercentage
                    ),
                    colorType = "tertiary"
                )
            )
        }

        val classification = result.classification as ClassificationSoja
        val limits         = result.limitUsed as LimitSoja

        val rows = listOf(
            ClassificationRow("Matérias Estranhas e Impurezas", classification.impuritiesPercentage, classification.impuritiesType, limits.impuritiesUpLim),
            ClassificationRow("Ardidos", classification.sourPercentage, -1, 0f),
            ClassificationRow("Queimados", classification.burntPercentage, classification.burntType, limits.burntUpLim),
            ClassificationRow("Total de Ardidos e Queimados", classification.burntOrSourPercentage, classification.burntOrSourType, limits.burntOrSourUpLim),
            ClassificationRow("Mofados", classification.moldyPercentage, classification.moldyType, limits.moldyUpLim),
            ClassificationRow("Fermentados", classification.fermentedPercentage, -1, 0f),
            ClassificationRow("Germinados", classification.germinatedPercentage, -1, 0f),
            ClassificationRow("Imaturos", classification.immaturePercentage, -1, 0f),
            ClassificationRow("Chochos", classification.shriveledPercentage, -1, 0f),
            ClassificationRow("Danificados", classification.damagedPercentage, -1, 0f),
            ClassificationRow("Total de Avariados", classification.spoiledPercentage, classification.spoiledType, limits.spoiledTotalUpLim),
            ClassificationRow("Esverdeados", classification.greenishPercentage, classification.greenishType, limits.greenishUpLim),
            ClassificationRow("Partidos, Quebrados e Amassados", classification.brokenCrackedDamagedPercentage, classification.brokenCrackedDamagedType, limits.brokenCrackedDamagedUpLim)
        )

        val sampleInputRows = listOf(
            SampleInputRow("Peso do Lote",           "%.2f kg".format(payload.sample.lotWeight)),
            SampleInputRow("Peso da Amostra",        "%.2f g".format(payload.sample.sampleWeight)),
            SampleInputRow("Peso Limpo",             "%.2f g".format(
                if (payload.sample.cleanWeight > 0f) payload.sample.cleanWeight
                else payload.sample.sampleWeight - payload.sample.foreignMattersAndImpurities
            )),
            SampleInputRow("Matérias Estranhas e Impurezas",              "%.2f g".format(payload.sample.foreignMattersAndImpurities)),
            SampleInputRow("Ardidos",                "%.2f g".format(payload.sample.sour)),
            SampleInputRow("Queimados",              "%.2f g".format(payload.sample.burnt)),
            SampleInputRow("Mofados",                "%.2f g".format(payload.sample.moldy)),
            SampleInputRow("Fermentados",            "%.2f g".format(payload.sample.fermented)),
            SampleInputRow("Germinados",             "%.2f g".format(payload.sample.germinated)),
            SampleInputRow("Imaturos",               "%.2f g".format(payload.sample.immature)),
            SampleInputRow("Chochos",                "%.2f g".format(payload.sample.shriveled)),
            SampleInputRow("Danificados Total",            "%.2f g".format(payload.sample.damaged)),
            SampleInputRow("Avariados Total",        "%.2f g".format(
                payload.sample.moldy + payload.sample.fermented + payload.sample.sour +
                        payload.sample.burnt + payload.sample.germinated + payload.sample.immature +
                        payload.sample.shriveled + payload.sample.damaged)),
            SampleInputRow("Esverdeados",            "%.2f g".format(payload.sample.greenish)),
            SampleInputRow("Partidos, Quebrados e Amassados",     "%.2f g".format(payload.sample.brokenCrackedDamaged))
        )

        return ClassificationUIState(
            limitUsed          = result.limitUsed,
            classification     = result.classification,
            disqualification   = result.disqualification,
            toxicSeeds         = mappedToxicSeeds,
            complementaryCards = cards,
            tableRows          = rows,
            sampleLotWeight    = payload.sample.lotWeight,
            sampleInputRows    = sampleInputRows
        )
    }

    override suspend fun saveDisqualificationData(
        classificationId: Int,
        badConservation: Int,
        strangeSmell: Int,
        insects: Int,
        toxicGrains: Int,
        toxicSeeds: List<Pair<String, String>>
    ) {
        val disqualification = DisqualificationSoja(
            classificationId = null,
            badConservation  = badConservation,
            graveDefectSum   = 0,
            strangeSmell     = strangeSmell,
            insects          = insects,
            toxicGrains      = toxicGrains
        )

        val newDisqualificationId = repositorySoja.insertDisqualification(disqualification).toInt()

        if (toxicGrains == 1 && toxicSeeds.isNotEmpty()) {
            val seedsToInsert = toxicSeeds.map { pair ->
                ToxicSeedSoja(
                    disqualificationId = newDisqualificationId,
                    name               = pair.first,
                    quantity           = pair.second.toIntOrNull() ?: 0
                )
            }
            repositorySoja.insertToxicSeeds(seedsToInsert)
        }
    }

    override suspend fun exportClassificationToPdf(
        context: Context,
        state: ClassificationUIState,
        limits: List<Any>,
        observation: String?,
        isOfficial: Boolean
    ) {
        try {
            val classification = state.classification as? ClassificationSoja ?: return
            val limit          = state.limitUsed as? LimitSoja ?: return
            val sample         = repositorySoja.getSample(classification.sampleId) ?: return
            val colorClass     = repositorySoja.getLastColorClass()
            val disqSoja       = repositorySoja.getDisqualificationByClassificationId(classification.id)
            val toxicSeeds     = disqSoja?.let { repositorySoja.getToxicSeedsByDisqualificationId(it.id) }

            val doesDefineColorClass = state.complementaryCards.isNotEmpty()
            val obs = if (doesDefineColorClass) {
                repositorySoja.getObservations(classification.id, colorClass)
            } else {
                repositorySoja.getObservations(idClassification = classification.id)
            }

            val limitesParaPdf = if (isOfficial) limits.filterIsInstance<LimitSoja>() else listOf(limit)

            val limitHeaders = listOf("Defeito") + limitesParaPdf.mapIndexed { i, item ->
                if (item.group == 2) "Padrão Básico"
                else if (item.group == 1 && (i + 1) == 4) "Fora de Tipo"
                else "Tipo ${i + 1}"
            }

            val limitRows = listOf(
                "Ardidos e Queimados"                    to limitesParaPdf.map { "%.2f%%".format(it.burntOrSourUpLim) },
                "Queimados"                              to limitesParaPdf.map { "%.2f%%".format(it.burntUpLim) },
                "Mofados"                                to limitesParaPdf.map { "%.2f%%".format(it.moldyUpLim) },
                "Avariados Total"                        to limitesParaPdf.map { "%.2f%%".format(it.spoiledTotalUpLim) },
                "Esverdeados"                            to limitesParaPdf.map { "%.2f%%".format(it.greenishUpLim) },
                "Partidos, Quebrados e Amassados"        to limitesParaPdf.map { "%.2f%%".format(it.brokenCrackedDamagedUpLim) },
                "Matérias Estranhas e Impurezas"         to limitesParaPdf.map { "%.2f%%".format(it.impuritiesUpLim) }
            ).map { (label, values) -> PdfLimitRow(label, values) }

            val payload = ClassificationPdfPayload(
                grain              = grainName,
                group              = sample.group,
                isOfficial         = isOfficial,
                moisturePercentage = classification.moisturePercentage,
                moistureLimit      = limitesParaPdf.firstOrNull()?.moistureUpLim ?: 14f,
                finalTypeLabel     = getTypeLabel(classification.finalType, sample.group),
                tableRows          = state.tableRows.map { row ->
                    PdfTableRow(row.label, "%.2f".format(row.percentage), getTypeLabel(row.typeCode, sample.group))
                },
                colorLabel         = colorClass?.framingClass ?: "CLASSE DE COR DA SOJA",
                colorDefined       = colorClass != null,
                colorYellow        = colorClass?.yellowPercentage,
                colorOther         = colorClass?.otherColorPercentage,
                sample             = PdfSampleData(sample.lotWeight, sample.sampleWeight, sample.moisture),
                sampleInputRows    = state.sampleInputRows
                    .filter { it.label != "Peso do Lote" && it.label != "Peso da Amostra" }
                    .map { it.label to it.value },
                disqualification   = disqSoja?.let { disq ->
                    PdfDisqualificationData(
                        badConservation  = disq.badConservation,
                        strangeSmell     = disq.strangeSmell,
                        insects          = disq.insects,
                        toxicGrains      = disq.toxicGrains,
                        toxicSeedDetails = toxicSeeds?.map { it.name to it.quantity } ?: emptyList()
                    )
                },
                limitHeaders       = limitHeaders,
                limitRows          = limitRows,
                observation        = obs
            )

            pdfExporter.exportClassification(context, payload)
        } catch (e: Exception) {
            Log.e("PDF_EXPORT_SOJA", "Erro ao exportar PDF: ${e.message}", e)
        }
    }

    override suspend fun setCustomLimit(payload: CustomLimitPayload) {
        repositorySoja.setLimit(
            grain                = grainName,
            group                = payload.group,
            type                 = 1,
            impurities           = payload.impurities,
            moisture             = payload.moisture,
            brokenCrackedDamaged = payload.broken,
            greenish             = payload.greenish,
            burnt                = payload.burnt,
            burntOrSour          = payload.burntOrSour,
            moldy                = payload.moldy,
            spoiled              = payload.spoiled
        )
    }

    override suspend fun deleteCustomLimits() {
        repositorySoja.deleteCustomLimits()
    }

    override suspend fun saveColorClass(
        classificationId: Int,
        totalWeight: Float,
        otherColorsWeight: Float
    ) {
        repositorySoja.setClass(grainName, classificationId, totalWeight, otherColorsWeight)
    }

    override fun getTypeLabel(finalType: Int, group: Int): String {
        if (finalType == -1) return "-"
        if (finalType == 0) return "Desclassificada"
        if (finalType == 7) return "Fora de Tipo"

        return if (group == 2) {
            "Padrão Básico"
        } else when (finalType) {
            1    -> "Tipo 1"
            2    -> "Tipo 2"
            3    -> "Tipo 3"
            else -> "Tipo $finalType"
        }
    }
}