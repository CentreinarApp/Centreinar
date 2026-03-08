package com.example.centreinar.ui.discount.strategy

import android.content.Context
import com.example.centreinar.DiscountSoja
import com.example.centreinar.InputDiscountSoja
import com.example.centreinar.LimitSoja
import com.example.centreinar.data.repository.ClassificationRepository
import com.example.centreinar.data.repository.DiscountRepository
import com.example.centreinar.util.ClassificationPdfPayload
import com.example.centreinar.util.DiscountPdfPayload
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
    private val pdfExporter: PDFExporter
) : GrainDiscountStrategy {

    override val grainName: String = "Soja"

    private var lastDiscountId: Long = -1L
    private var lastInputDiscountId: Long = -1L

    override fun getLimitInputFields(classificationId: Int?): List<DiscountInputField> = listOf(
        DiscountInputField("umidade",     "Umidade da Amostra (%)"),
        DiscountInputField("impureza",    "Impurezas (%)"),
        DiscountInputField("queimados",   "Queimados (%)"),
        DiscountInputField("ardidos",     "Ardidos e Queimados (%)"),
        DiscountInputField("mofados",     "Mofados (%)"),
        DiscountInputField("avariados",   "Total Avariados (%)"),
        DiscountInputField("esverdeados", "Esverdeados (%)"),
        DiscountInputField("quebrados",   "Quebrados/Amassados (%)")
    )

    override fun getDefectInputFields(): List<DiscountInputField> = listOf(
        DiscountInputField("impureza",    "Mat. Estranha e Imp. (%)",  tabGroup = 0),
        DiscountInputField("ardidos",     "Ardidos e Queimados (%)",    tabGroup = 1),
        DiscountInputField("queimados",   "Queimados (%)",              tabGroup = 1),
        DiscountInputField("mofados",     "Mofados (%)",                tabGroup = 1),
        DiscountInputField("avariados",   "Total Avariados (%)",        tabGroup = 1),
        DiscountInputField("esverdeados", "Esverdeados (%)",            tabGroup = 2),
        DiscountInputField("quebrados",   "Partidos/Quebrados (%)",     tabGroup = 2)
    )

    override fun createDefectsPayload(fieldValues: Map<String, Float>): DiscountDefectsPayload {
        return DiscountDefectsPayload.Soja(
            moisture    = fieldValues["umidade"]     ?: 0f,
            impurities  = fieldValues["impureza"]    ?: 0f,
            greenish    = fieldValues["esverdeados"] ?: 0f,
            broken      = fieldValues["quebrados"]   ?: 0f,
            spoiled     = fieldValues["avariados"]   ?: 0f,
            burnt       = fieldValues["queimados"]   ?: 0f,
            burntOrSour = fieldValues["ardidos"]     ?: 0f,
            moldy       = fieldValues["mofados"]     ?: 0f
        )
    }

    override suspend fun getBaseLimits(group: Int): Map<String, Float>? {
        val limit = classificationRepo.getLimitsByGroup(grainName, group, 0)
            .firstOrNull() ?: return null
        return mapOf(
            "umidade"     to limit.moistureUpLim,
            "impureza"    to limit.impuritiesUpLim,
            "queimados"   to limit.burntUpLim,
            "ardidos"     to limit.burntOrSourUpLim,
            "mofados"     to limit.moldyUpLim,
            "avariados"   to limit.spoiledTotalUpLim,
            "esverdeados" to limit.greenishUpLim,
            "quebrados"   to limit.brokenCrackedDamagedUpLim
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

    override suspend fun saveCustomLimitData(group: Int, fieldMap: Map<String, Float>) {
        classificationRepo.deleteCustomLimits()
        classificationRepo.setLimit(
            grain                = grainName,
            group                = group,
            type                 = 1,
            impurities           = fieldMap["impureza"]    ?: 0f,
            moisture             = fieldMap["umidade"]     ?: 0f,
            brokenCrackedDamaged = fieldMap["quebrados"]   ?: 0f,
            greenish             = fieldMap["esverdeados"] ?: 0f,
            burnt                = fieldMap["queimados"]   ?: 0f,
            burntOrSour          = fieldMap["ardidos"]     ?: 0f,
            moldy                = fieldMap["mofados"]     ?: 0f,
            spoiled              = fieldMap["avariados"]   ?: 0f
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
            classificationId            = financialPayload.sourceClassificationId, // ← vem do ViewModel
            daysOfStorage               = financialPayload.daysOfStorage,
            lotWeight                   = financialPayload.lotWeight,
            lotPrice                    = lotPrice,
            foreignMattersAndImpurities = payload.impurities,
            humidity                    = payload.moisture,
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

        val discountId = discountRepo.calculateDiscount(
            grain                  = grainName,
            group                  = savedInput.group,
            tipo                   = 1,
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

    override suspend fun exportDiscountToPdf(context: Context, sourceClassificationId: Int?) {
        try {
            if (lastDiscountId < 0) return
            val discount = discountRepo.getDiscountById(lastDiscountId) ?: return

            val input = discountRepo.getLastInputDiscount()
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
                inputMoisture         = input.humidity,
                inputImpurities       = input.foreignMattersAndImpurities,
                lotWeight             = input.lotWeight,
                lotPrice              = input.lotPrice,
                classificationPayload = classificationPayload
            )

            pdfExporter.exportDiscount(context, discountPayload)
        } catch (e: Exception) {
            android.util.Log.e("PDF_DISCOUNT_SOJA", "Erro ao exportar PDF: ${e.message}", e)
        }
    }

    private suspend fun buildClassificationPayload(classificationId: Int, isOfficial: Boolean): ClassificationPdfPayload? {
        val classification = classificationRepo.getClassification(classificationId) ?: return null
        val sample = classificationRepo.getSample(classification.sampleId) ?: return null
        val disqualification = classificationRepo.getDisqualificationByClassificationId(classificationId)
        val toxicSeeds = disqualification?.let { classificationRepo.getToxicSeedsByDisqualificationId(it.id) }

        val colorClassification = classificationRepo.getColorClassification(classificationId)

        val lastSource = classificationRepo.getLastLimitSource()
        val limit = classificationRepo.getLimit(grainName, sample.group, classification.finalType, lastSource)

        val limitesParaPdf = if (isOfficial) {
            classificationRepo.getLimitsByGroup(grainName, sample.group, 0).filterIsInstance<LimitSoja>()
        } else {
            limit?.let { listOf(it as LimitSoja) } ?: emptyList()
        }

        val limitHeaders = listOf("Defeito") + limitesParaPdf.mapIndexed { i, _ ->
            if (sample.group == 2) "Padrão Básico" else "Tipo ${i + 1}"
        }

        val limitRows = listOf(
            "Ardidos/Queim."   to limitesParaPdf.map { "%.2f%%".format(it.burntOrSourUpLim) },
            "Queimados"        to limitesParaPdf.map { "%.2f%%".format(it.burntUpLim) },
            "Mofados"          to limitesParaPdf.map { "%.2f%%".format(it.moldyUpLim) },
            "Avariados Total"  to limitesParaPdf.map { "%.2f%%".format(it.spoiledTotalUpLim) },
            "Esverdeados"      to limitesParaPdf.map { "%.2f%%".format(it.greenishUpLim) },
            "Partidos/Quebr."  to limitesParaPdf.map { "%.2f%%".format(it.brokenCrackedDamagedUpLim) },
            "Mat. Est./Imp."   to limitesParaPdf.map { "%.2f%%".format(it.impuritiesUpLim) }
        ).map { (label, values) -> PdfLimitRow(label, values) }

        val tableRows = listOf(
            PdfTableRow("Matérias Estranhas e Impurezas (%)", "%.2f".format(classification.impuritiesPercentage), getTypeLabel(classification.impuritiesType, sample.group)),

            // Defeitos específicos (Avariados e outros)
            PdfTableRow("Ardidos (%)", "%.2f".format(classification.sourPercentage ?: 0f), getTypeLabel( -1, sample.group)),

            PdfTableRow("Queimados (%)", "%.2f".format(classification.burntPercentage), getTypeLabel(classification.burntType, sample.group)),

            PdfTableRow("Total Ardidos + Queimados (%)", "%.2f".format(classification.burntOrSourPercentage), getTypeLabel(classification.burntOrSourType, sample.group)),

            PdfTableRow("Mofados (%)", "%.2f".format(classification.moldyPercentage), getTypeLabel(classification.moldyType, sample.group)),

            PdfTableRow("Fermentados (%)", "%.2f".format(classification.fermentedPercentage ?: 0f), getTypeLabel(-1, sample.group)),

            PdfTableRow("Germinados (%)", "%.2f".format(classification.germinatedPercentage ?: 0f), getTypeLabel(-1, sample.group)),

            PdfTableRow("Imaturos (%)", "%.2f".format(classification.immaturePercentage ?: 0f), getTypeLabel(-1, sample.group)),

            PdfTableRow("Chochos (%)", "%.2f".format(classification.shriveledPercentage ?: 0f), getTypeLabel(-1, sample.group)),

            PdfTableRow("Danificados (%)", "%.2f".format(classification.damagedPercentage ?: 0f), getTypeLabel(-1, sample.group)),

            // Totais e outros
            PdfTableRow("Total de Avariados (%)", "%.2f".format(classification.spoiledPercentage), getTypeLabel(classification.spoiledType, sample.group)),

            PdfTableRow("Esverdeados (%)", "%.2f".format(classification.greenishPercentage), getTypeLabel(classification.greenishType, sample.group)),

            PdfTableRow("Partidos, Quebrados e Amassados (%)", "%.2f".format(classification.brokenCrackedDamagedPercentage), getTypeLabel(classification.brokenCrackedDamagedType, sample.group))
        )

        val disqData = disqualification?.let { disq ->
            PdfDisqualificationData(
                badConservation = disq.badConservation,
                strangeSmell = disq.strangeSmell,
                insects = disq.insects,
                toxicGrains = disq.toxicGrains,
                toxicSeedDetails = toxicSeeds?.map { it.name to it.quantity } ?: emptyList()
            )
        }

        return ClassificationPdfPayload(
            grain              = grainName,
            group              = sample.group,
            isOfficial         = isOfficial,
            moisturePercentage = classification.moisturePercentage ?: 0f,
            moistureLimit      = limitesParaPdf.firstOrNull()?.moistureUpLim ?: 14f,
            finalTypeLabel     = getTypeLabel(classification.finalType ?: -1, sample.group),
            tableRows          = tableRows,

            // 👇 AGORA PEGAMOS DA ENTIDADE DE COR 👇
            colorLabel         = colorClassification?.framingClass ?: "CLASSE",
            colorYellow        = colorClassification?.yellowPercentage,
            colorOther         = colorClassification?.otherColorPercentage,
            // 👆 ================================= 👆

            sample             = PdfSampleData(sample.lotWeight, sample.sampleWeight, sample.moisture),
            disqualification   = disqData,
            limitHeaders       = limitHeaders,
            limitRows          = limitRows,
            observation        = ""
        )
    }
}

private fun DiscountSoja.toDiscountResult(
    limitRows: List<Pair<String, List<Float>>>,
    limitHeaders: List<String>
) = DiscountResult(
    summaryRows = listOf(
        DiscountResultRow("Desc. por Matérias Estranhas e Impurezas", impuritiesLoss,        impuritiesLossPrice),
        DiscountResultRow("Desc. por Umidade",                        humidityLoss,          humidityLossPrice),
        DiscountResultRow("Quebra Classificação",                     classificationDiscount, classificationDiscountPrice),
        DiscountResultRow("Quebra Técnica",                           technicalLoss,         technicalLossPrice),
        DiscountResultRow("Desconto Total",                           finalDiscount,         finalDiscountPrice),
        DiscountResultRow("Lote Líquido Final",                       finalWeight,           finalWeightPrice)
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