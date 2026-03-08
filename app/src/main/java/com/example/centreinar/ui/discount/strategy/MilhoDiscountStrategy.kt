package com.example.centreinar.ui.discount.strategy

import android.content.Context
import com.example.centreinar.data.local.entity.DiscountMilho
import com.example.centreinar.data.local.entity.InputDiscountMilho
import com.example.centreinar.data.local.entity.LimitMilho
import com.example.centreinar.data.repository.ClassificationRepositoryMilho
import com.example.centreinar.domain.repository.DiscountRepositoryMilho
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

class MilhoDiscountStrategy @Inject constructor(
    private val classificationRepo: ClassificationRepositoryMilho,
    private val discountRepo: DiscountRepositoryMilho,
    private val pdfExporter: PDFExporter
) : GrainDiscountStrategy {

    override val grainName: String = "Milho"

    private var lastDiscountId: Long = -1L

    override fun getLimitInputFields(classificationId: Int?): List<DiscountInputField> = listOf(
        DiscountInputField("umidade",     "Umidade da Amostra (%)"),
        DiscountInputField("impureza",    "Impurezas (%)"),
        DiscountInputField("ardidos",     "Ardidos (%)"),
        DiscountInputField("carunchados", "Carunchados (%)"),
        DiscountInputField("avariados",   "Total Avariados (%)"),
        DiscountInputField("quebrados",   "Quebrados (%)")
    )

    override fun getDefectInputFields(): List<DiscountInputField> = listOf(
        DiscountInputField("impureza",    "Mat. Estranha e Imp. (%)", tabGroup = 0),
        DiscountInputField("quebrados",   "Quebrados (%)",            tabGroup = 1),
        DiscountInputField("ardidos",     "Ardidos (%)",              tabGroup = 1),
        DiscountInputField("avariados",   "Total Avariados (%)",      tabGroup = 1),
        DiscountInputField("carunchados", "Carunchados (%)",          tabGroup = 1),
    )

    override fun createDefectsPayload(fieldValues: Map<String, Float>): DiscountDefectsPayload {
        return DiscountDefectsPayload.Milho(
            moisture   = fieldValues["umidade"]     ?: 0f,
            impurities = fieldValues["impureza"]    ?: 0f,
            broken     = fieldValues["quebrados"]   ?: 0f,
            ardido     = fieldValues["ardidos"]     ?: 0f,
            carunchado = fieldValues["carunchados"] ?: 0f,
            spoiled    = fieldValues["avariados"]   ?: 0f,
            mofados    = fieldValues["mofados"]     ?: 0f,
            fermented  = fieldValues["fermentados"] ?: 0f,
            germinated = fieldValues["germinados"]  ?: 0f,
            gessado    = fieldValues["gessados"]    ?: 0f
        )
    }

    override suspend fun getBaseLimits(group: Int): Map<String, Float>? {
        val limit = classificationRepo.getLimitsByGroup(grainName, group, 0)
            .firstOrNull() ?: return null
        return mapOf(
            "umidade"     to limit.moistureUpLim,
            "impureza"    to limit.impuritiesUpLim,
            "ardidos"     to limit.ardidoUpLim,
            "carunchados" to limit.carunchadoUpLim,
            "avariados"   to limit.spoiledTotalUpLim,
            "quebrados"   to limit.brokenUpLim
        )
    }

    override suspend fun getOfficialTableData(group: Int): Pair<Int, List<Pair<String, List<Float>>>> {
        val limits = classificationRepo.getLimitsByGroup(grainName, group, 0)
        if (limits.isEmpty()) return Pair(0, emptyList())
        return Pair(limits.size, listOf(
            "Umidade"          to limits.map { it.moistureUpLim },
            "Ardidos"          to limits.map { it.ardidoUpLim },
            "Avariados Total"  to limits.map { it.spoiledTotalUpLim },
            "Quebrados"        to limits.map { it.brokenUpLim },
            "Mat. Est. e Imp." to limits.map { it.impuritiesUpLim },
            "Carunchados"      to limits.map { it.carunchadoUpLim }
        ))
    }

    override suspend fun getOfficialLimitsList(group: Int): List<Any> =
        classificationRepo.getLimitsByGroup(grainName, group, 0)

    override suspend fun saveCustomLimitData(group: Int, fieldMap: Map<String, Float>) {
        classificationRepo.deleteCustomLimits()
        classificationRepo.setLimit(
            grain        = grainName,
            group        = group,
            tipo         = 1,
            impurities   = fieldMap["impureza"]    ?: 0f,
            moisture     = fieldMap["umidade"]     ?: 0f,
            broken       = fieldMap["quebrados"]   ?: 0f,
            ardido       = fieldMap["ardidos"]     ?: 0f,
            mofado       = 0f,
            spoiledTotal = fieldMap["avariados"]   ?: 0f,
            carunchado   = fieldMap["carunchados"] ?: 0f
        )
    }

    override suspend fun calculateDiscount(
        defectsPayload: DiscountDefectsPayload,
        financialPayload: FinancialDiscountPayload,
        isOfficial: Boolean
    ): DiscountResult {
        val payload = defectsPayload as? DiscountDefectsPayload.Milho
            ?: return DiscountResult(emptyList(), emptyList())

        val limitSource = if (!isOfficial) discountRepo.getLastLimitSource() else 0
        val lotPrice    = (financialPayload.lotWeight * financialPayload.priceBySack) / 60f

        val input = InputDiscountMilho(
            grain            = grainName,
            group            = financialPayload.group,
            limitSource      = limitSource,
            classificationId = financialPayload.sourceClassificationId, // ← vem do ViewModel
            daysOfStorage    = financialPayload.daysOfStorage,
            lotWeight        = financialPayload.lotWeight,
            lotPrice         = lotPrice,
            impurities       = payload.impurities,
            moisture         = payload.moisture,
            broken           = payload.broken,
            ardidos          = payload.ardido,
            mofados          = payload.mofados,
            carunchado       = payload.carunchado,
            spoiled          = payload.spoiled,
            deductionValue   = financialPayload.deductionValue
        )
        discountRepo.setInputDiscount(input)

        val savedInput = discountRepo.getLastInputDiscount()

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
            // Verifica se a classificação foi feita com limites oficiais
            val isOfficial = input.limitSource == 0

            val classificationPayload: ClassificationPdfPayload? =
                sourceClassificationId?.let { buildClassificationPayload(it, isOfficial) }

            val discountPayload = DiscountPdfPayload(
                grain        = grainName,
                summaryRows  = listOf(
                    PdfDiscountRow("Mat. Estranhas e Impurezas", discount.impuritiesLoss,        discount.impuritiesLossPrice),
                    PdfDiscountRow("Umidade",                    discount.humidityLoss,           discount.humidityLossPrice),
                    PdfDiscountRow("Desconto de Classificação",  discount.classificationDiscount, discount.classificationDiscountPrice),
                    PdfDiscountRow("Quebra Técnica",             discount.technicalLoss,          discount.technicalLossPrice),
                    PdfDiscountRow("Desconto Total",             discount.finalDiscount,          discount.finalDiscountPrice),
                    PdfDiscountRow("Lote Líquido Final",         discount.finalWeight,            discount.finalWeightPrice)
                ),
                detailRows   = listOf(
                    PdfDiscountRow("Mat. Estranhas e Impurezas", discount.impuritiesLoss,  discount.impuritiesLossPrice),
                    PdfDiscountRow("Umidade",                    discount.humidityLoss,    discount.humidityLossPrice),
                    PdfDiscountRow("Quebrados",                  discount.brokenLoss,      discount.brokenLossPrice),
                    PdfDiscountRow("Ardidos",                    discount.ardidoLoss,      discount.ardidoLossPrice),
                    PdfDiscountRow("Total de Avariados",         discount.spoiledLoss,     discount.spoiledLossPrice),
                    PdfDiscountRow("Carunchados",                discount.carunchadoLoss,  discount.carunchadoLossPrice)
                ),
                inputMoisture         = input.moisture,
                inputImpurities       = input.impurities,
                lotWeight             = input.lotWeight,
                lotPrice              = input.lotPrice,
                classificationPayload = classificationPayload
            )

            pdfExporter.exportDiscount(context, discountPayload)
        } catch (e: Exception) {
            android.util.Log.e("PDF_DISCOUNT_MILHO", "Erro ao exportar PDF: ${e.message}", e)
        }
    }

    private suspend fun buildClassificationPayload(classificationId: Int, isOfficial: Boolean): ClassificationPdfPayload? {
        val classification = classificationRepo.getClassification(classificationId) ?: return null
        val sample = classificationRepo.getSample(classification.sampleId) ?: return null
        val disqualification = classificationRepo.getDisqualificationByClassificationId(classificationId)
        val toxicSeeds = disqualification?.let { classificationRepo.getToxicSeedsByDisqualificationId(it.id) }

        // Busca a cor no banco de dados
        val colorData = classificationRepo.getColorClassification(classificationId.toLong())
        val detailText = if (colorData != null) {
            "Grupo: ${colorData.framingGroup} (Duro: %.1f%%) | Classe: ${colorData.framingClass} (Amarelo: %.1f%%)".format(
                colorData.duroPercentage,
                colorData.yellowPercentage
            )
        } else {
            null
        }

        val lastSource = classificationRepo.getLastLimitSource()
        val limit = classificationRepo.getLimit(grainName, sample.group, classification.finalType, lastSource)

        val limitesParaPdf = if (isOfficial) {
            classificationRepo.getLimitsByGroup(grainName, sample.group, 0).filterIsInstance<LimitMilho>()
        } else {
            limit?.let { listOf(it) } ?: emptyList()
        }

        val limitHeaders = listOf("Defeito") + limitesParaPdf.mapIndexed { i, _ ->
            if (i == 3) "Fora de Tipo" else "Tipo ${i + 1}"
        }

        val limitRows = listOf(
            "Ardidos"          to limitesParaPdf.map { "%.2f%%".format(it.ardidoUpLim) },
            "Mofados"          to limitesParaPdf.map { "%.2f%%".format(it.mofadoUpLim) },
            "Avariados Total"  to limitesParaPdf.map { "%.2f%%".format(it.spoiledTotalUpLim) },
            "Quebrados"        to limitesParaPdf.map { "%.2f%%".format(it.brokenUpLim) },
            "Carunchados"      to limitesParaPdf.map { "%.2f%%".format(it.carunchadoUpLim) },
            "Mat. Estranha"    to limitesParaPdf.map { "%.2f%%".format(it.impuritiesUpLim) }
        ).map { (label, values) -> PdfLimitRow(label, values) }

        val tableRows = listOf(
            PdfTableRow("Matérias Estranhas e Impurezas (%)", "%.2f".format(classification.impuritiesPercentage), getTypeLabel(classification.impuritiesType, sample.group)),
            PdfTableRow("Grãos Quebrados (%)", "%.2f".format(classification.brokenPercentage), getTypeLabel(classification.brokenType, sample.group)),
            PdfTableRow("Ardidos (%)", "%.2f".format(classification.ardidoPercentage), getTypeLabel(classification.ardidoType, sample.group)),

            // Não classificam tipo = -1
            PdfTableRow("Mofados (%)", "%.2f".format(classification.mofadoPercentage), getTypeLabel(classification.mofadoType, sample.group)),
            PdfTableRow("Fermentados (%)", "%.2f".format(classification.fermentedPercentage), getTypeLabel(classification.fermentedType, sample.group)),
            PdfTableRow("Germinados (%)", "%.2f".format(classification.germinatedPercentage), getTypeLabel(classification.germinatedType, sample.group)),
            PdfTableRow("Chochos e Imaturos (%)", "%.2f".format(classification.immaturePercentage), getTypeLabel(classification.immatureType, sample.group)),
            PdfTableRow("Gessados (%)", "%.2f".format(classification.gessadoPercentage), getTypeLabel(classification.gessadoType, sample.group)),

            PdfTableRow("Total de Avariados (%)", "%.2f".format(classification.spoiledTotalPercentage ?: 0f), getTypeLabel(classification.spoiledTotalType ?: 0, sample.group)),
            PdfTableRow("Carunchados (%)", "%.2f".format(classification.carunchadoPercentage), getTypeLabel(classification.carunchadoType, sample.group))
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
            moisturePercentage = classification.moisturePercentage,
            moistureLimit      = limitesParaPdf.firstOrNull()?.moistureUpLim ?: 14f,
            finalTypeLabel     = getTypeLabel(classification.finalType, sample.group),
            tableRows          = tableRows,

            // Injeta a label e a cor no PDF
            colorLabel         = "GRUPO E CLASSE DO MILHO",
            colorDetailText    = detailText,

            sample             = PdfSampleData(sample.lotWeight, sample.sampleWeight, sample.moisture),
            disqualification   = disqData,
            limitHeaders       = limitHeaders,
            limitRows          = limitRows,
            observation        = ""
        )
    }
}

private fun DiscountMilho.toDiscountResult(
    limitRows: List<Pair<String, List<Float>>>,
    limitHeaders: List<String>
) = DiscountResult(
    summaryRows = listOf(
        DiscountResultRow("Desc. por Matérias Estranhas e Impurezas", impuritiesLoss,        impuritiesLossPrice),
        DiscountResultRow("Desc. por Umidade",                        humidityLoss,          humidityLossPrice),
        DiscountResultRow("Desconto de Classificação",                classificationDiscount, classificationDiscountPrice),
        DiscountResultRow("Quebra Técnica",                           technicalLoss,         technicalLossPrice),
        DiscountResultRow("Desconto Total",                           finalDiscount,         finalDiscountPrice),
        DiscountResultRow("Lote Líquido Final",                       finalWeight,           finalWeightPrice)
    ),
    detailRows = listOf(
        DiscountResultRow("Mat. Estranha e Impurezas", impuritiesLoss,  impuritiesLossPrice),
        DiscountResultRow("Umidade",                   humidityLoss,    humidityLossPrice),
        DiscountResultRow("Quebrados",                 brokenLoss,      brokenLossPrice),
        DiscountResultRow("Ardidos",                   ardidoLoss,      ardidoLossPrice),
        DiscountResultRow("Total de Avariados",        spoiledLoss,     spoiledLossPrice),
        DiscountResultRow("Carunchados",               carunchadoLoss,  carunchadoLossPrice)
    ),
    limitRows    = limitRows,
    limitHeaders = limitHeaders
)

private fun List<LimitMilho>.toLimitRows() = listOf(
    "Umidade"          to map { it.moistureUpLim },
    "Ardidos"          to map { it.ardidoUpLim },
    "Avariados Total"  to map { it.spoiledTotalUpLim },
    "Quebrados"        to map { it.brokenUpLim },
    "Mat. Est. e Imp." to map { it.impuritiesUpLim },
    "Carunchados"      to map { it.carunchadoUpLim }
)