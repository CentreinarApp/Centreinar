package com.example.centreinar.ui.discount.strategy

import android.content.Context
import com.example.centreinar.DiscountSoja
import com.example.centreinar.InputDiscountSoja
import com.example.centreinar.LimitSoja
import com.example.centreinar.data.repository.ClassificationRepository
import com.example.centreinar.data.repository.DiscountRepository
import com.example.centreinar.domain.model.GrainDescriptor
import com.example.centreinar.domain.usecase.CalculateDiscountSojaUseCase
import com.example.centreinar.util.ClassificationPdfPayload
import com.example.centreinar.util.DiscountPdfPayload
import com.example.centreinar.util.FieldKeys
import com.example.centreinar.util.PDFExporter
import com.example.centreinar.util.PdfDiscountRow
import com.example.centreinar.util.PdfDisqualificationData
import com.example.centreinar.util.PdfLimitRow
import com.example.centreinar.util.PdfSampleData
import com.example.centreinar.util.PdfTableRow
import com.example.centreinar.util.getTypeLabel
import javax.inject.Inject

class SojaDiscountStrategy @Inject constructor(
    private val classificationRepo: ClassificationRepository,
    private val discountRepo: DiscountRepository,
    private val calculateDiscountUseCase: CalculateDiscountSojaUseCase,
    private val pdfExporter: PDFExporter
) : GrainDiscountStrategy {

    override val descriptor = GrainDescriptor(
        name           = "Soja",
        displayName    = "Soja",
        colorScheme    = "primary",
        supportsGroups = true
    )

    private var lastDiscountId: Long = -1L
    private var lastInputDiscountId: Long = -1L

    // -------------------------------------------------------------------------
    // Campos de input — TODAS AS CHAVES USAM FieldKeys.*
    //
    // Contrato obrigatório: as chaves em getLimitInputFields, getDefectInputFields,
    // createDefectsPayload, getBaseLimits e saveCustomLimitData devem ser
    // IDÊNTICAS às chaves retornadas por ClassificationSoja.toDefectsMap().
    // Isso garante que o prefill da classificação preencha os campos corretamente.
    // -------------------------------------------------------------------------

    override fun getDiscountInputRows(
        prefill: com.example.centreinar.ui.discount.viewmodel.ClassificationPrefill?,
        financial: FinancialDiscountPayload
    ): List<DiscountInputRow> = listOf(
        DiscountInputRow("Peso do Lote",          "%.2f kg".format(financial.lotWeight)),
        DiscountInputRow("Preço por Saca",        "R\$ %.2f".format(financial.priceBySack)),
        DiscountInputRow("Umidade",               "%.2f %%".format(prefill?.moisture ?: 0f)),
        DiscountInputRow("Matérias Estranhas e Impurezas",             "%.2f %%".format(prefill?.defects?.get(com.example.centreinar.util.FieldKeys.IMPURITIES) ?: 0f)),
        DiscountInputRow("Ardidos e Queimados",   "%.2f %%".format(prefill?.defects?.get(com.example.centreinar.util.FieldKeys.BURNT_OR_SOUR) ?: 0f)),
        DiscountInputRow("Queimados",             "%.2f %%".format(prefill?.defects?.get(com.example.centreinar.util.FieldKeys.BURNT) ?: 0f)),
        DiscountInputRow("Mofados",               "%.2f %%".format(prefill?.defects?.get(com.example.centreinar.util.FieldKeys.MOLDY) ?: 0f)),
        DiscountInputRow("Avariados Total",       "%.2f %%".format(prefill?.defects?.get(com.example.centreinar.util.FieldKeys.SPOILED) ?: 0f)),
        DiscountInputRow("Esverdeados",           "%.2f %%".format(prefill?.defects?.get(com.example.centreinar.util.FieldKeys.GREENISH) ?: 0f)),
        DiscountInputRow("Partidos, Quebrados e Amassados",    "%.2f %%".format(prefill?.defects?.get(com.example.centreinar.util.FieldKeys.BROKEN) ?: 0f))
    )

    override fun getLimitInputFields(classificationId: Int?): List<DiscountInputField> = listOf(
        DiscountInputField(FieldKeys.MOISTURE,      "Umidade da Amostra (%)"),
        DiscountInputField(FieldKeys.IMPURITIES,    "Impurezas (%)"),
        DiscountInputField(FieldKeys.BURNT,         "Queimados (%)"),
        DiscountInputField(FieldKeys.BURNT_OR_SOUR, "Ardidos e Queimados (%)"),
        DiscountInputField(FieldKeys.MOLDY,         "Mofados (%)"),
        DiscountInputField(FieldKeys.SPOILED,       "Total Avariados (%)"),
        DiscountInputField(FieldKeys.GREENISH,      "Esverdeados (%)"),
        DiscountInputField(FieldKeys.BROKEN,        "Quebrados/Amassados (%)")
    )

    override fun getDefectInputFields(): List<DiscountInputField> = listOf(
        DiscountInputField(FieldKeys.IMPURITIES,    "Mat. Estranha e Imp. (%)",  tabGroup = 0),
        DiscountInputField(FieldKeys.BURNT_OR_SOUR, "Ardidos e Queimados (%)",   tabGroup = 1),
        DiscountInputField(FieldKeys.BURNT,         "Queimados (%)",             tabGroup = 1),
        DiscountInputField(FieldKeys.MOLDY,         "Mofados (%)",               tabGroup = 1),
        DiscountInputField(FieldKeys.SPOILED,       "Total Avariados (%)",       tabGroup = 1),
        DiscountInputField(FieldKeys.GREENISH,      "Esverdeados (%)",           tabGroup = 2),
        DiscountInputField(FieldKeys.BROKEN,        "Partidos/Quebrados (%)",    tabGroup = 2)
    )

    override fun createDefectsPayload(fieldValues: Map<String, Float>): DiscountDefectsPayload {
        return DiscountDefectsPayload.Soja(
            moisture    = fieldValues[FieldKeys.MOISTURE]      ?: 0f,
            impurities  = fieldValues[FieldKeys.IMPURITIES]    ?: 0f,
            greenish    = fieldValues[FieldKeys.GREENISH]      ?: 0f,
            broken      = fieldValues[FieldKeys.BROKEN]        ?: 0f,
            spoiled     = fieldValues[FieldKeys.SPOILED]       ?: 0f,
            burnt       = fieldValues[FieldKeys.BURNT]         ?: 0f,
            burntOrSour = fieldValues[FieldKeys.BURNT_OR_SOUR] ?: 0f,
            moldy       = fieldValues[FieldKeys.MOLDY]         ?: 0f
        )
    }

    override suspend fun getBaseLimits(group: Int): Map<String, Float>? {
        val limit = classificationRepo.getLimitsByGroup(grainName, group, 0)
            .firstOrNull() ?: return null
        return mapOf(
            FieldKeys.MOISTURE      to limit.moistureUpLim,
            FieldKeys.IMPURITIES    to limit.impuritiesUpLim,
            FieldKeys.BURNT         to limit.burntUpLim,
            FieldKeys.BURNT_OR_SOUR to limit.burntOrSourUpLim,
            FieldKeys.MOLDY         to limit.moldyUpLim,
            FieldKeys.SPOILED       to limit.spoiledTotalUpLim,
            FieldKeys.GREENISH      to limit.greenishUpLim,
            FieldKeys.BROKEN        to limit.brokenCrackedDamagedUpLim
        )
    }

    override suspend fun getOfficialTableData(group: Int): Pair<Int, List<Pair<String, List<Float>>>> {
        val limits = classificationRepo.getLimitsByGroup(grainName, group, 0)
        if (limits.isEmpty()) return Pair(0, emptyList())
        return Pair(limits.size, listOf(
            "Umidade"          to limits.map { it.moistureUpLim },
            "Ardidos/Queim."   to limits.map { it.burntOrSourUpLim },
            "Queimados"        to limits.map { it.burntUpLim },
            "Mofados"          to limits.map { it.moldyUpLim },
            "Avariados Total"  to limits.map { it.spoiledTotalUpLim },
            "Esverdeados"      to limits.map { it.greenishUpLim },
            "Partidos/Quebr."  to limits.map { it.brokenCrackedDamagedUpLim },
            "Mat. Est. e Imp." to limits.map { it.impuritiesUpLim }
        ))
    }

    override suspend fun getOfficialLimitsList(group: Int): List<Any> =
        classificationRepo.getLimitsByGroup(grainName, group, 0)

    override fun getLimitFields(): List<com.example.centreinar.domain.model.LimitField> = listOf(
        com.example.centreinar.domain.model.LimitField(FieldKeys.BURNT_OR_SOUR, "Ardidos e Queimados (%)"),
        com.example.centreinar.domain.model.LimitField(FieldKeys.BURNT,         "Queimados (%)"),
        com.example.centreinar.domain.model.LimitField(FieldKeys.MOLDY,         "Mofados (%)"),
        com.example.centreinar.domain.model.LimitField(FieldKeys.SPOILED,       "Total Avariados (%)"),
        com.example.centreinar.domain.model.LimitField(FieldKeys.GREENISH,      "Esverdeados (%)"),
        com.example.centreinar.domain.model.LimitField(FieldKeys.BROKEN,        "Partidos, Quebrados e Amassados (%)"),
        com.example.centreinar.domain.model.LimitField(FieldKeys.IMPURITIES,    "Matérias Estranhas e Impurezas (%)"),
    )

    override suspend fun saveCustomLimitData(group: Int, fieldMap: Map<String, Float>) {
        classificationRepo.deleteCustomLimits()
        classificationRepo.setLimit(
            grain                = grainName,
            group                = group,
            type                 = 1,
            impurities           = fieldMap[FieldKeys.IMPURITIES]    ?: 0f,
            moisture             = fieldMap[FieldKeys.MOISTURE]       ?: 0f,
            brokenCrackedDamaged = fieldMap[FieldKeys.BROKEN]         ?: 0f,
            greenish             = fieldMap[FieldKeys.GREENISH]       ?: 0f,
            burnt                = fieldMap[FieldKeys.BURNT]          ?: 0f,
            burntOrSour          = fieldMap[FieldKeys.BURNT_OR_SOUR]  ?: 0f,
            moldy                = fieldMap[FieldKeys.MOLDY]          ?: 0f,
            spoiled              = fieldMap[FieldKeys.SPOILED]        ?: 0f
        )
    }

    override suspend fun calculateDiscount(
        defectsPayload: DiscountDefectsPayload,
        financialPayload: FinancialDiscountPayload,
        isOfficial: Boolean
    ): DiscountResult {
        val payload = defectsPayload as? DiscountDefectsPayload.Soja
            ?: return DiscountResult(emptyList(), emptyList())

        val limitSource = if (!isOfficial) discountRepo.getLastLimitSource() else 0
        val lotPrice    = (financialPayload.lotWeight * financialPayload.priceBySack) / 60f

        val input = InputDiscountSoja(
            grain                       = grainName,
            group                       = financialPayload.group,
            limitSource                 = limitSource,
            classificationId            = financialPayload.sourceClassificationId,
            daysOfStorage               = financialPayload.daysOfStorage,
            lotWeight                   = financialPayload.lotWeight,
            lotPrice                    = lotPrice,
            foreignMattersAndImpurities = payload.impurities,
            moisture                    = payload.moisture,
            burnt                       = payload.burnt,
            burntOrSour                 = payload.burntOrSour,
            moldy                       = payload.moldy,
            spoiled                     = payload.spoiled,
            greenish                    = payload.greenish,
            brokenCrackedDamaged        = payload.broken,
            deductionValue              = financialPayload.deductionValue
        )
        discountRepo.setInputDiscount(input)

        val savedInput = discountRepo.getLastInputDiscount()
        lastInputDiscountId = savedInput.id.toLong()

        val discountId = calculateDiscountUseCase.execute(
            sample                 = savedInput,
            doesTechnicalLoss      = financialPayload.doesTechnicalLoss,
            doesClassificationLoss = true,
            doesDeduction          = financialPayload.doesDeduction
        )
        lastDiscountId = discountId

        val discount = discountRepo.getDiscountById(discountId)
            ?: return DiscountResult(emptyList(), emptyList())

        val limitsForDisplay = classificationRepo.getLimitsByGroup(grainName, savedInput.group, limitSource)
        val limitHeaders     = limitsForDisplay.map { "Tipo ${it.type}" }
        val limitRows        = limitsForDisplay.toLimitRows()

        return discount.toDiscountResult(limitRows, limitHeaders)
    }

    private fun buildSojaDiscountInputRows(input: InputDiscountSoja): List<Pair<String, String>> =
        listOf(
            "Umidade"             to "%.2f %%".format(input.moisture),
            "Matérias Estranhas e Impurezas"           to "%.2f %%".format(input.foreignMattersAndImpurities),
            "Ardidos e Queimados" to "%.2f %%".format(input.burntOrSour),
            "Queimados"           to "%.2f %%".format(input.burnt),
            "Mofados"             to "%.2f %%".format(input.moldy),
            "Avariados Total"     to "%.2f %%".format(input.spoiled),
            "Esverdeados"         to "%.2f %%".format(input.greenish),
            "Partidos, Quebrados e Amassados"  to "%.2f %%".format(input.brokenCrackedDamaged)
        )

    override suspend fun exportDiscountToPdf(context: Context, sourceClassificationId: Int?) {
        try {
            if (lastDiscountId < 0) return
            val discount = discountRepo.getDiscountById(lastDiscountId) ?: return

            val input      = discountRepo.getLastInputDiscount()
            val isOfficial = input.limitSource == 0

            val classificationPayload: ClassificationPdfPayload? =
                sourceClassificationId?.let { buildClassificationPayload(it, isOfficial) }

            val discountPayload = DiscountPdfPayload(
                grain        = grainName,
                summaryRows  = listOf(
                    PdfDiscountRow("Mat. Estranhas e Impurezas", discount.impuritiesLoss,        discount.impuritiesLossPrice),
                    PdfDiscountRow("Umidade",                    discount.humidityLoss,           discount.humidityLossPrice),
                    PdfDiscountRow("Quebra Classificação",       discount.classificationDiscount, discount.classificationDiscountPrice),
                    PdfDiscountRow("Quebra Técnica",             discount.technicalLoss,          discount.technicalLossPrice),
                    PdfDiscountRow("Desconto Total",             discount.finalDiscount,          discount.finalDiscountPrice),
                    PdfDiscountRow("Lote Líquido Final",         discount.finalWeight,            discount.finalWeightPrice)
                ),
                detailRows   = listOf(
                    PdfDiscountRow("Mat. Estranhas e Impurezas", discount.impuritiesLoss,   discount.impuritiesLossPrice),
                    PdfDiscountRow("Umidade",                    discount.humidityLoss,      discount.humidityLossPrice),
                    PdfDiscountRow("Ardidos e Queimados",        discount.burntOrSourLoss,   discount.burntOrSourLossPrice),
                    PdfDiscountRow("Queimados",                  discount.burntLoss,         discount.burntLossPrice),
                    PdfDiscountRow("Mofados",                    discount.moldyLoss,         discount.moldyLossPrice),
                    PdfDiscountRow("Total de Avariados",         discount.spoiledLoss,       discount.spoiledLossPrice),
                    PdfDiscountRow("Esverdeados",                discount.greenishLoss,      discount.greenishLossPrice),
                    PdfDiscountRow("Partidos/Quebrados",         discount.brokenLoss,        discount.brokenLossPrice)
                ),
                inputMoisture         = input.moisture,
                inputImpurities       = input.foreignMattersAndImpurities,
                lotWeight             = input.lotWeight,
                lotPrice              = input.lotPrice,
                classificationPayload = classificationPayload,
                discountInputRows     = buildSojaDiscountInputRows(input)
            )

            pdfExporter.exportDiscount(context, discountPayload)
        } catch (e: Exception) {
            android.util.Log.e("PDF_DISCOUNT_SOJA", "Erro ao exportar PDF: ${e.message}", e)
        }
    }

    private suspend fun buildClassificationPayload(classificationId: Int, isOfficial: Boolean): ClassificationPdfPayload? {
        val classification   = classificationRepo.getClassification(classificationId) ?: return null
        val sample           = classificationRepo.getSample(classification.sampleId)  ?: return null
        val disqualification = classificationRepo.getDisqualificationByClassificationId(classificationId)
        val toxicSeeds       = disqualification?.let { classificationRepo.getToxicSeedsByDisqualificationId(it.id) }
        val colorClassification = classificationRepo.getColorClassification(classificationId)
        val lastSource       = classificationRepo.getLastLimitSource()
        val limit            = classificationRepo.getLimit(grainName, sample.group, classification.finalType, lastSource)

        val limitesParaPdf = if (isOfficial) {
            classificationRepo.getLimitsByGroup(grainName, sample.group, 0).filterIsInstance<LimitSoja>()
        } else {
            limit?.let { listOf(it) } ?: emptyList()
        }

        val limitHeaders = listOf("Defeito") + limitesParaPdf.mapIndexed { i, _ ->
            if (sample.group == 2) "Padrão Básico" else "Tipo ${i + 1}"
        }

        val limitRows = listOf(
            "Ardidos e Queimados"  to limitesParaPdf.map { "%.2f%%".format(it.burntOrSourUpLim) },
            "Queimados"       to limitesParaPdf.map { "%.2f%%".format(it.burntUpLim) },
            "Mofados"         to limitesParaPdf.map { "%.2f%%".format(it.moldyUpLim) },
            "Avariados Total" to limitesParaPdf.map { "%.2f%%".format(it.spoiledTotalUpLim) },
            "Esverdeados"     to limitesParaPdf.map { "%.2f%%".format(it.greenishUpLim) },
            "Partidos, Quebrados e Amassados" to limitesParaPdf.map { "%.2f%%".format(it.brokenCrackedDamagedUpLim) },
            "Matérias Estranhas e Impurezas"  to limitesParaPdf.map { "%.2f%%".format(it.impuritiesUpLim) }
        ).map { (label, values) -> PdfLimitRow(label, values) }

        val tableRows = listOf(
            PdfTableRow("Matérias Estranhas e Impurezas", "%.2f".format(classification.impuritiesPercentage), getTypeLabel(classification.impuritiesType, sample.group)),
            PdfTableRow("Ardidos", "%.2f".format(classification.sourPercentage), getTypeLabel(-1, sample.group)),
            PdfTableRow("Queimados", "%.2f".format(classification.burntPercentage), getTypeLabel(classification.burntType, sample.group)),
            PdfTableRow("Total Ardidos e Queimados", "%.2f".format(classification.burntOrSourPercentage), getTypeLabel(classification.burntOrSourType, sample.group)),
            PdfTableRow("Mofados", "%.2f".format(classification.moldyPercentage), getTypeLabel(classification.moldyType, sample.group)),
            PdfTableRow("Fermentados", "%.2f".format(classification.fermentedPercentage), getTypeLabel(-1, sample.group)),
            PdfTableRow("Germinados", "%.2f".format(classification.germinatedPercentage), getTypeLabel(-1, sample.group)),
            PdfTableRow("Imaturos", "%.2f".format(classification.immaturePercentage), getTypeLabel(-1, sample.group)),
            PdfTableRow("Chochos", "%.2f".format(classification.shriveledPercentage), getTypeLabel(-1, sample.group)),
            PdfTableRow("Danificados", "%.2f".format(classification.damagedPercentage), getTypeLabel(-1, sample.group)),
            PdfTableRow("Total de Avariados", "%.2f".format(classification.spoiledPercentage), getTypeLabel(classification.spoiledType, sample.group)),
            PdfTableRow("Esverdeados", "%.2f".format(classification.greenishPercentage), getTypeLabel(classification.greenishType, sample.group)),
            PdfTableRow("Partidos, Quebrados e Amassados", "%.2f".format(classification.brokenCrackedDamagedPercentage), getTypeLabel(classification.brokenCrackedDamagedType, sample.group))
        )

        val disqData = disqualification?.let { disq ->
            PdfDisqualificationData(
                badConservation  = disq.badConservation,
                strangeSmell     = disq.strangeSmell,
                insects          = disq.insects,
                toxicGrains      = disq.toxicGrains,
                toxicSeedDetails = toxicSeeds?.map { it.name to it.quantity } ?: emptyList()
            )
        }

        val sampleInputRows = listOf(
            "Matérias Estranhas e Impurezas"           to "%.2f g".format(sample.foreignMattersAndImpurities),
            "Ardidos"             to "%.2f g".format(sample.sour),
            "Queimados"           to "%.2f g".format(sample.burnt),
            "Mofados"             to "%.2f g".format(sample.moldy),
            "Fermentados"         to "%.2f g".format(sample.fermented),
            "Germinados"          to "%.2f g".format(sample.germinated),
            "Imaturos"            to "%.2f g".format(sample.immature),
            "Chochos"             to "%.2f g".format(sample.shriveled),
            "Danificados"         to "%.2f g".format(sample.damaged),
            "Avariados Total"     to "%.2f g".format(
                sample.moldy + sample.fermented + sample.sour + sample.burnt +
                        sample.germinated + sample.immature + sample.shriveled + sample.damaged),
            "Esverdeados"         to "%.2f g".format(sample.greenish),
            "Partidos, Quebrados e Amassados"  to "%.2f g".format(sample.brokenCrackedDamaged)
        )

        return ClassificationPdfPayload(
            grain              = grainName,
            group              = sample.group,
            isOfficial         = isOfficial,
            moisturePercentage = classification.moisturePercentage,
            moistureLimit      = limitesParaPdf.firstOrNull()?.moistureUpLim ?: 14f,
            finalTypeLabel     = getTypeLabel(classification.finalType, sample.group),
            tableRows          = tableRows,
            colorLabel         = colorClassification?.framingClass ?: "CLASSE DE COR DA SOJA",
            colorDefined       = colorClassification != null,
            colorYellow        = colorClassification?.yellowPercentage,
            colorOther         = colorClassification?.otherColorPercentage,
            sample             = PdfSampleData(sample.lotWeight, sample.sampleWeight, sample.moisture),
            disqualification   = disqData,
            limitHeaders       = limitHeaders,
            limitRows          = limitRows,
            observation        = "",
            sampleInputRows    = sampleInputRows
        )
    }
}

private fun DiscountSoja.toDiscountResult(
    limitRows: List<Pair<String, List<Float>>>,
    limitHeaders: List<String>
) = DiscountResult(
    summaryRows = listOf(
        DiscountResultRow("Desc. por Matérias Estranhas e Impurezas", impuritiesLoss,         impuritiesLossPrice),
        DiscountResultRow("Desc. por Umidade",                        humidityLoss,           humidityLossPrice),
        DiscountResultRow("Quebra Classificação",                     classificationDiscount, classificationDiscountPrice),
        DiscountResultRow("Quebra Técnica",                           technicalLoss,          technicalLossPrice),
        DiscountResultRow("Desconto Total",                           finalDiscount,          finalDiscountPrice),
        DiscountResultRow("Lote Líquido Final",                       finalWeight,            finalWeightPrice)
    ),
    detailRows = listOf(
        DiscountResultRow("Matérias Estranhas e Impurezas",  impuritiesLoss,   impuritiesLossPrice),
        DiscountResultRow("Umidade",                         humidityLoss,     humidityLossPrice),
        DiscountResultRow("Ardidos e Queimados",             burntOrSourLoss,  burntOrSourLossPrice),
        DiscountResultRow("Queimados",                       burntLoss,        burntLossPrice),
        DiscountResultRow("Mofados",                         moldyLoss,        moldyLossPrice),
        DiscountResultRow("Total de Avariados",              spoiledLoss,      spoiledLossPrice),
        DiscountResultRow("Esverdeados",                     greenishLoss,     greenishLossPrice),
        DiscountResultRow("Partidos, Quebrados e Amassados", brokenLoss,       brokenLossPrice)
    ),
    limitRows    = limitRows,
    limitHeaders = limitHeaders
)

private fun List<LimitSoja>.toLimitRows() = listOf(
    "Umidade"          to map { it.moistureUpLim },
    "Ardidos/Queim."   to map { it.burntOrSourUpLim },
    "Queimados"        to map { it.burntUpLim },
    "Mofados"          to map { it.moldyUpLim },
    "Avariados Total"  to map { it.spoiledTotalUpLim },
    "Esverdeados"      to map { it.greenishUpLim },
    "Partidos/Quebr."  to map { it.brokenCrackedDamagedUpLim },
    "Mat. Est. e Imp." to map { it.impuritiesUpLim }
)