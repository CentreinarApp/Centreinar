package com.example.centreinar.ui.classificationProcess.strategy

import android.content.Context
import android.util.Log
import com.example.centreinar.ClassificationMilho
import com.example.centreinar.data.local.entity.DisqualificationMilho
import com.example.centreinar.data.local.entity.LimitMilho
import com.example.centreinar.data.local.entity.SampleMilho
import com.example.centreinar.data.local.entity.ToxicSeedMilho
import com.example.centreinar.data.repository.ClassificationRepositoryMilho
import com.example.centreinar.domain.model.GrainDescriptor
import com.example.centreinar.domain.model.LimitField
import com.example.centreinar.domain.usecase.ClassifyMilhoUseCase
import com.example.centreinar.util.ClassificationPdfPayload
import com.example.centreinar.util.FieldKeys
import com.example.centreinar.util.PDFExporter
import com.example.centreinar.util.PdfDisqualificationData
import com.example.centreinar.util.PdfLimitRow
import com.example.centreinar.util.PdfSampleData
import com.example.centreinar.util.PdfTableRow
import javax.inject.Inject

class MilhoClassificationStrategy @Inject constructor(
    private val repositoryMilho: ClassificationRepositoryMilho,
    private val classifyMilhoUseCase: ClassifyMilhoUseCase,
    private val pdfExporter: PDFExporter
) : GrainStrategy {

    override val descriptor = GrainDescriptor(
        name               = "Milho",
        displayName        = "Milho",
        colorScheme        = "secondary",
        supportsGroups     = false,  // Milho não passa por GroupSelection — group fixo 1
        supportsCarunchado = true
    )

    override fun buildPayload(state: ClassificationInputState): ClassificationPayload {
        val sample = SampleMilho(
            grain = grainName,
            group = state.group,
            lotWeight = state.lotWeight,
            sampleWeight = state.sampleWeight,
            moisture = state.moisture,
            cleanWeight = state.cleanSampleWeight,
            impurities = state.foreignMatters,
            broken = state.brokenCrackedDamaged,
            carunchado = state.carunchado,
            ardido = state.sour,
            mofado = state.moldy,
            fermented = state.fermented,
            germinated = state.germinated,
            immature = state.immature,
            gessado = state.gessado
        )
        return ClassificationPayload.Milho(
            sample            = sample,
            shouldDefineClass = state.shouldDefineClass,
            weightYellow      = state.weightYellow,
            weightWhite       = state.weightWhite,
            weightMixedColors = state.weightMixedColors,
            shouldDefineGroup = state.shouldDefineGroup,
            weightHard        = state.weightHard,
            weightDent        = state.weightDent,
            weightSemiHard    = state.weightSemiHard
        )
    }

    override fun getSampleInputRows(state: ClassificationUIState): List<SampleInputRow> {
        return state.sampleInputRows
    }

    override suspend fun getOfficialLimits(group: Int): List<Any> {
        return repositoryMilho.getLimitsByGroup(grainName, group, 0)
    }

    override suspend fun getBaseLimits(group: Int): Map<String, Float>? {
        return repositoryMilho.getLimitOfType1Official(group, grainName).takeIf { it.isNotEmpty() }
    }

    override suspend fun getUiStateData(id: Int): StrategyResultData {
        val classification = repositoryMilho.getClassification(id)
            ?: throw Exception("Classificação de Milho não encontrada")

        val sample = repositoryMilho.getSample(classification.sampleId)

        val disqualification = repositoryMilho.getDisqualificationByClassificationId(id)

        val lastSource = repositoryMilho.getLastLimitSource()
        val limit = repositoryMilho.getLimit(
            grain  = grainName,
            group  = classification.group,
            tipo   = classification.finalType,
            source = lastSource
        ) ?: throw Exception("Limites de referência não encontrados")

        val toxicSeeds = disqualification?.let {
            repositoryMilho.getToxicSeedsByDisqualificationId(it.id)
        } ?: emptyList()

        val rows = listOf(
            ClassificationRow("Matérias Estranhas e Impurezas", classification.impuritiesPercentage, classification.impuritiesType, limit.impuritiesUpLim),
            ClassificationRow("Ardidos", classification.ardidoPercentage, classification.ardidoType, limit.ardidoUpLim),
            ClassificationRow("Mofados", classification.mofadoPercentage, classification.mofadoType, limit.mofadoUpLim),
            ClassificationRow("Total de Avariados", classification.spoiledTotalPercentage ?: 0f, classification.spoiledTotalType ?: 0, limit.spoiledTotalUpLim),
            ClassificationRow("Quebrados", classification.brokenPercentage, classification.brokenType, limit.brokenUpLim),
            ClassificationRow("Carunchados", classification.carunchadoPercentage, classification.carunchadoType, limit.carunchadoUpLim)
        )

        return StrategyResultData(
            classification   = classification,
            disqualification = disqualification,
            limit            = limit,
            tableRows        = rows,
            toxicSeeds       = toxicSeeds.map { Pair(it.name, it.quantity) },
            cards            = emptyList(),
            sampleLotWeight  = sample?.lotWeight ?: 0f
        )
    }

    override fun getLimitFields(): List<LimitField> = listOf(
        LimitField(FieldKeys.ARDIDO,     "Ardidos (%)"),
        LimitField(FieldKeys.SPOILED,    "Total Avariados (%)"),
        LimitField(FieldKeys.BROKEN,     "Quebrados (%)"),
        LimitField(FieldKeys.IMPURITIES, "Matérias Estranhas e Impurezas (%)"),
        LimitField(FieldKeys.CARUNCHADO, "Carunchados (%)")
    )

    override suspend fun classify(payload: ClassificationPayload, isOfficial: Boolean): ClassificationUIState {
        require(payload is ClassificationPayload.Milho) { "Payload incorreto para a estratégia de Milho" }

        val result = classifyMilhoUseCase.execute(
            sample            = payload.sample,
            shouldDefineClass = payload.shouldDefineClass,
            weightYellow      = payload.weightYellow,
            weightWhite       = payload.weightWhite,
            weightMixedColors = payload.weightMixedColors,
            shouldDefineGroup = payload.shouldDefineGroup,
            weightHard        = payload.weightHard,
            weightDent        = payload.weightDent,
            weightSemiHard    = payload.weightSemiHard,
            isOfficial        = isOfficial
        )

        val mappedToxicSeeds = result.toxicSeeds.map { Pair(it.name, it.quantity) }
        val cards            = mutableListOf<ComplementaryCardData>()

        result.complementaryData?.let { data ->
            if (payload.shouldDefineClass && data.framingClass.isNotEmpty()) {
                cards.add(
                    ComplementaryCardData(
                        title    = data.framingClass,
                        subtitle = "Amarela: %.2f%% | Outras: %.2f%%".format(
                            data.yellowPercentage,
                            data.otherColorPercentage
                        ),
                        colorType = "tertiary"
                    )
                )
            }

            if (payload.shouldDefineGroup && data.framingGroup.isNotEmpty()) {
                cards.add(
                    ComplementaryCardData(
                        title    = data.framingGroup,
                        subtitle = "Duro: %.2f%% | Dentado: %.2f%% | Semiduro: %.2f%%".format(
                            data.duroPercentage,
                            data.dentadoPercentage,
                            data.semiDuroPercentage
                        ),
                        colorType = "secondary"
                    )
                )
            }
        }

        val classification = result.classification as ClassificationMilho
        val limit          = result.limitUsed as LimitMilho

        val rows = listOf(
            ClassificationRow("Matérias Estranhas e Impurezas", classification.impuritiesPercentage, classification.impuritiesType, limit.impuritiesUpLim),
            ClassificationRow("Quebrados", classification.brokenPercentage, classification.brokenType, limit.brokenUpLim),
            ClassificationRow("Ardidos", classification.ardidoPercentage, classification.ardidoType, limit.ardidoUpLim),
            ClassificationRow("Mofados", classification.mofadoPercentage, -1, limit.mofadoUpLim),
            ClassificationRow("Fermentados", classification.fermentedPercentage, -1, 0f),
            ClassificationRow("Germinados", classification.germinatedPercentage, -1, 0f),
            ClassificationRow("Chochos e Imaturos", classification.immaturePercentage, -1, 0f),
            ClassificationRow("Gessados", classification.gessadoPercentage, -1, 0f),
            ClassificationRow("Total de Avariados", classification.spoiledTotalPercentage ?: 0f, classification.spoiledTotalType ?: 0, limit.spoiledTotalUpLim),
            ClassificationRow("Carunchados", classification.carunchadoPercentage, classification.carunchadoType, limit.carunchadoUpLim)
        )

        val sampleInputRows = listOf(
            SampleInputRow("Peso do Lote",        "%.2f kg".format(payload.sample.lotWeight)),
            SampleInputRow("Peso da Amostra",     "%.2f g".format(payload.sample.sampleWeight)),
            SampleInputRow("Peso Limpo",          "%.2f g".format(
                if (payload.sample.cleanWeight > 0f) payload.sample.cleanWeight
                else payload.sample.sampleWeight - (payload.sample.impurities + payload.sample.broken)
            )),
            SampleInputRow("Matérias Estranhas e Impurezas",           "%.2f g".format(payload.sample.impurities)),
            SampleInputRow("Grãos Quebrados",           "%.2f g".format(payload.sample.broken)),
            SampleInputRow("Ardidos",             "%.2f g".format(payload.sample.ardido)),
            SampleInputRow("Mofados",             "%.2f g".format(payload.sample.mofado)),
            SampleInputRow("Fermentados",         "%.2f g".format(payload.sample.fermented)),
            SampleInputRow("Germinados",          "%.2f g".format(payload.sample.germinated)),
            SampleInputRow("Chochos e Imaturos",  "%.2f g".format(payload.sample.immature)),
            SampleInputRow("Gessados",            "%.2f g".format(payload.sample.gessado)),
            SampleInputRow("Avariados Total",     "%.2f g".format(
                payload.sample.ardido + payload.sample.mofado + payload.sample.fermented +
                        payload.sample.germinated + payload.sample.immature + payload.sample.gessado)),
            SampleInputRow("Carunchados",         "%.2f g".format(payload.sample.carunchado))
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
        val resolvedId = repositoryMilho.getLastClassificationId(grainName) ?: return

        val disqId = repositoryMilho.insertDisqualification(
            DisqualificationMilho(
                classificationId = resolvedId,
                badConservation  = badConservation,
                strangeSmell     = strangeSmell,
                insects          = insects,
                toxicGrains      = toxicGrains
            )
        )

        if (toxicGrains == 1 && toxicSeeds.isNotEmpty()) {
            val seedsToInsert = toxicSeeds.map { (name, qty) ->
                ToxicSeedMilho(
                    disqualificationId = disqId.toInt(),
                    name               = name,
                    quantity           = qty.toIntOrNull() ?: 0
                )
            }
            repositoryMilho.insertToxicSeeds(seedsToInsert)
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
            val classification = state.classification as? ClassificationMilho ?: return
            val limit          = state.limitUsed as? LimitMilho ?: return
            val sample         = repositoryMilho.getSample(classification.sampleId) ?: return
            val disqMilho      = repositoryMilho.getDisqualificationByClassificationId(classification.id)
            val colorData      = repositoryMilho.getColorClassification(classification.id.toLong())
            val toxicSeeds     = disqMilho?.let { repositoryMilho.getToxicSeedsByDisqualificationId(it.id) }

            val limitesParaPdf = if (isOfficial) limits.filterIsInstance<LimitMilho>() else listOf(limit)

            val limitHeaders = listOf("Defeito") + limitesParaPdf.mapIndexed { i, _ ->
                if (i == 3) "Fora de Tipo" else "Tipo ${i + 1}"
            }

            val limitRows = listOf(
                "Ardidos"                        to limitesParaPdf.map { "%.2f%%".format(it.ardidoUpLim) },
                "Avariados Total"                to limitesParaPdf.map { "%.2f%%".format(it.spoiledTotalUpLim) },
                "Quebrados"                      to limitesParaPdf.map { "%.2f%%".format(it.brokenUpLim) },
                "Matérias Estranhas e Impurezas" to limitesParaPdf.map { "%.2f%%".format(it.impuritiesUpLim) },
                "Carunchados"                    to limitesParaPdf.map { "%.2f%%".format(it.carunchadoUpLim) }
            ).map { (label, values) -> PdfLimitRow(label, values) }

            val detailText = if (colorData != null) {
                "Grupo: ${colorData.framingGroup} (Duro: %.1f%%) | Classe: ${colorData.framingClass} (Amarelo: %.1f%%)".format(
                    colorData.duroPercentage,
                    colorData.yellowPercentage
                )
            } else null

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
                colorLabel         = "CLASSE E COR DO MILHO",
                colorDefined       = colorData != null,
                colorDetailText    = detailText,
                sample             = PdfSampleData(sample.lotWeight, sample.sampleWeight, sample.moisture),
                sampleInputRows    = state.sampleInputRows
                    .filter { it.label != "Peso do Lote" && it.label != "Peso da Amostra" }
                    .map { it.label to it.value },
                disqualification   = disqMilho?.let { disq ->
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
                observation        = observation
            )

            pdfExporter.exportClassification(context, payload)
        } catch (e: Exception) {
            Log.e("PDF_EXPORT_MILHO", "Erro ao exportar PDF: ${e.message}", e)
        }
    }

    override suspend fun setCustomLimit(payload: CustomLimitPayload) {
        repositoryMilho.setLimit(
            grain        = grainName,
            group        = payload.group,
            tipo         = 1,
            impurities   = payload.impurities,
            moisture     = payload.moisture,
            broken       = payload.broken,
            ardido       = payload.ardido,
            mofado       = payload.moldy,
            spoiledTotal = payload.spoiled,
            carunchado   = payload.carunchado
        )
    }

    override suspend fun deleteCustomLimits() {
        repositoryMilho.deleteCustomLimits()
    }

    override fun getTypeLabel(finalType: Int, group: Int): String {
        return when (finalType) {
            -1   -> "-"
            0   -> "Desclassificada"
            7   -> "Fora de Tipo"
            else -> "Tipo $finalType"
        }
    }
}