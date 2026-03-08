package com.example.centreinar.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

// =============================================================================
// MODELOS GENÉRICOS
// =============================================================================

data class PdfTableRow(val parameter: String, val percentage: String, val typeLabel: String)
data class PdfSampleData(val lotWeight: Float, val sampleWeight: Float, val moisture: Float)
data class PdfDisqualificationData(
    val badConservation: Int, val strangeSmell: Int, val insects: Int, val toxicGrains: Int,
    val toxicSeedDetails: List<Pair<String, Int>> = emptyList()
)
data class PdfLimitRow(val label: String, val valuesByType: List<String>)
data class PdfDiscountRow(val label: String, val massKg: Float, val valueRS: Float)

data class ClassificationPdfPayload(
    val grain: String,
    val group: Int,
    val isOfficial: Boolean,
    val moisturePercentage: Float,
    val moistureLimit: Float,
    val finalTypeLabel: String,
    val tableRows: List<PdfTableRow>,
    val colorLabel: String? = null,
    val colorYellow: Float? = null,
    val colorOther: Float? = null,
    val colorDetailText: String? = null,
    val sample: PdfSampleData? = null,
    val disqualification: PdfDisqualificationData? = null,
    val limitHeaders: List<String> = emptyList(),
    val limitRows: List<PdfLimitRow> = emptyList(),
    val observation: String? = null
)

/**
 * Quando [classificationPayload] não é nulo, o PDF gerado pela tela de descontos
 * terá a seguinte ordem de páginas:
 * Pág 1 — Resultado da Classificação
 * Pág 2 — Resultado dos Descontos
 * Pág 3 — Dados Complementares e Limites de Referência
 */
data class DiscountPdfPayload(
    val grain: String,
    val summaryRows: List<PdfDiscountRow>,
    val detailRows: List<PdfDiscountRow> = emptyList(),
    val inputMoisture: Float? = null,
    val inputImpurities: Float? = null,
    val lotWeight: Float? = null,
    val lotPrice: Float? = null,
    val classificationPayload: ClassificationPdfPayload? = null
)

// =============================================================================
// EXPORTER
// =============================================================================

@Singleton
class PDFExporter @Inject constructor(
    @ApplicationContext private val appContext: Context
) {
    private val pageWidth  = 595
    private val pageHeight = 842

    // -------------------------------------------------------------------------
    // Tela de classificação
    // -------------------------------------------------------------------------
    fun exportClassification(context: Context, payload: ClassificationPdfPayload) {
        val document = PdfDocument()
        val paints   = setupPaints()
        addClassificationPages(document, paints, payload)
        saveAndShare(context, document, payload.grain)
    }

    // -------------------------------------------------------------------------
    // Tela de descontos
    // -------------------------------------------------------------------------
    fun exportDiscount(context: Context, discount: DiscountPdfPayload) {
        val document = PdfDocument()
        val paints   = setupPaints()

        val classif = discount.classificationPayload
        if (classif != null) {
            // Pág 1: resultado principal da classificação
            addClassificationResultPage(document, paints, classif)
            // Pág 2: descontos
            addDiscountPages(document, paints, discount)
            // Pág 3: dados complementares da classificação e limites
            addClassificationComplementaryPage(document, paints, classif)
        } else {
            // Desconto avulso — apenas a página de descontos
            addDiscountPages(document, paints, discount)
        }

        saveAndShare(context, document, discount.grain)
    }

    // =========================================================================
    // PÁGINAS DE CLASSIFICAÇÃO
    // =========================================================================

    private fun addClassificationPages(
        document: PdfDocument,
        paints: Paints,
        payload: ClassificationPdfPayload
    ) {
        addClassificationResultPage(document, paints, payload)
        addClassificationComplementaryPage(document, paints, payload)
    }

    // -------------------------------------------------------------------------
    // Página de resultado principal da classificação
    // -------------------------------------------------------------------------
    private fun addClassificationResultPage(
        document: PdfDocument,
        paints: Paints,
        payload: ClassificationPdfPayload
    ) {
        val pageNum  = document.pages.size + 1
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create()
        val page     = document.startPage(pageInfo)
        val canvas   = page.canvas
        var y        = 60f

        canvas.drawText(
            "RESULTADO DA CLASSIFICAÇÃO (${payload.grain.uppercase()})",
            pageWidth / 2f, y, paints.titlePaint
        )
        y += 40f

        canvas.drawRect(50f, y, pageWidth - 50f, y + 45f, paints.borderPaint)
        canvas.drawText("DADOS DE UMIDADE", 60f, y + 18f, paints.headerPaint)
        canvas.drawText(
            "Umidade da Amostra: %.1f%% | Limite de Referência: %.1f%%"
                .format(payload.moisturePercentage, payload.moistureLimit),
            60f, y + 36f, paints.cellPaint
        )
        y += 65f

        val tableData = payload.tableRows.map { listOf(it.parameter, it.percentage, it.typeLabel) } +
                listOf(listOf(" ", " ", " "), listOf("TIPO FINAL", "---", payload.finalTypeLabel))
        y = drawTable(canvas, y, listOf("PARÂMETRO", "PERCENTUAL", "TIPO"), tableData, paints)
        y += 20f

        // NOVO BLOCO GENÉRICO (Suporta Milho, Soja e Futuros Grãos)
        if (payload.colorLabel != null) {
            canvas.drawRect(50f, y, pageWidth - 50f, y + 45f, paints.borderPaint)
            canvas.drawText(payload.colorLabel.uppercase(), 60f, y + 18f, paints.headerPaint)

            val details = if (payload.colorDetailText != null) {
                payload.colorDetailText
            } else if (payload.colorYellow != null && payload.colorOther != null) {
                "Amarela: %.2f%% | Outras Cores: %.2f%%".format(payload.colorYellow, payload.colorOther)
            } else {
                ""
            }

            canvas.drawText(details, 60f, y + 36f, paints.cellPaint)
        }

        document.finishPage(page)
    }

    // -------------------------------------------------------------------------
    // Página de dados complementares e limites da classificação
    // -------------------------------------------------------------------------
    private fun addClassificationComplementaryPage(
        document: PdfDocument,
        paints: Paints,
        payload: ClassificationPdfPayload
    ) {
        val pageNum  = document.pages.size + 1
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create()
        val page     = document.startPage(pageInfo)
        val canvas   = page.canvas
        var y        = 60f

        // DADOS COMPLEMENTARES ---
        canvas.drawText("DADOS COMPLEMENTARES", pageWidth / 2f, y, paints.titlePaint)
        y += 40f

        payload.sample?.let { s ->
            canvas.drawText("AMOSTRA:", 50f, y, paints.headerPaint);            y += 20f
            canvas.drawText("Peso da Amostra: ${s.sampleWeight}g", 60f, y, paints.cellPaint); y += 15f
            canvas.drawText("Peso do Lote: ${s.lotWeight}kg",      60f, y, paints.cellPaint); y += 30f
        }

        payload.disqualification?.let { disq ->
            canvas.drawText("MOTIVOS DE DESCLASSIFICAÇÃO:", 50f, y, paints.headerPaint); y += 20f
            val status = { v: Int -> if (v == 1) "SIM" else "NÃO" }
            canvas.drawText("- Mau estado: ${status(disq.badConservation)}",  60f, y, paints.cellPaint); y += 15f
            canvas.drawText("- Odor estranho: ${status(disq.strangeSmell)}", 60f, y, paints.cellPaint); y += 15f
            canvas.drawText("- Insetos vivos: ${status(disq.insects)}",      60f, y, paints.cellPaint); y += 15f
            if (disq.toxicGrains == 1) {
                canvas.drawText("- Sementes Tóxicas: SIM", 60f, y, paints.cellPaint); y += 15f
                disq.toxicSeedDetails.forEach { (name, qty) ->
                    canvas.drawText("  • $name: $qty", 75f, y, paints.cellPaint); y += 15f
                }
            }
        }

        // LIMITES DE REFERÊNCIA ---
        y += 40f // Dá um espaço antes de iniciar a seção de limites
        canvas.drawText("LIMITES DE REFERÊNCIA UTILIZADOS", pageWidth / 2f, y, paints.titlePaint)
        y += 40f

        if (payload.limitRows.isNotEmpty() && payload.limitHeaders.isNotEmpty()) {
            val tableData = payload.limitRows.map { row -> listOf(row.label) + row.valuesByType }
            y = drawTable(canvas, y, payload.limitHeaders, tableData, paints)
            y += 40f
        }

        // OBSERVAÇÕES ---
        payload.observation?.takeIf { it.isNotBlank() }?.let { obs ->
            canvas.drawText("OBSERVAÇÕES", 50f, y, paints.headerPaint); y += 15f
            drawMultilineText(canvas, obs, 50f, y, paints.cellPaint, pageWidth - 100)
        }

        document.finishPage(page)
    }

    // =========================================================================
    // PÁGINA DE DESCONTO
    // =========================================================================

    private fun addDiscountPages(
        document: PdfDocument,
        paints: Paints,
        payload: DiscountPdfPayload
    ) {
        val pageNum  = document.pages.size + 1
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create()
        val page     = document.startPage(pageInfo)
        val canvas   = page.canvas
        var y        = 60f

        canvas.drawText(
            "RESULTADO DOS DESCONTOS (${payload.grain.uppercase()})",
            pageWidth / 2f, y, paints.titlePaint
        )
        y += 40f

        payload.inputMoisture?.let {
            canvas.drawRect(50f, y, pageWidth - 50f, y + 45f, paints.borderPaint)
            canvas.drawText("DADOS DE ENTRADA", 60f, y + 18f, paints.headerPaint)
            canvas.drawText(
                "Umidade: %.1f%% | Impurezas: %.2f%%".format(it, payload.inputImpurities ?: 0f),
                60f, y + 36f, paints.cellPaint
            )
            y += 65f
        }

        canvas.drawText("RESUMO", 50f, y, paints.headerPaint); y += 10f
        val summaryData = payload.summaryRows.map { row ->
            listOf(row.label, "%.2f kg".format(row.massKg), "R$ %.2f".format(row.valueRS))
        }
        y = drawTable(canvas, y, listOf("TIPO DE QUEBRA", "QTD (kg)", "VALOR (R$)"), summaryData, paints)
        y += 20f

        if (payload.detailRows.isNotEmpty()) {
            canvas.drawText("DETALHES POR DEFEITO", 50f, y, paints.headerPaint); y += 10f
            val detailData = payload.detailRows.map { row ->
                listOf(row.label, "%.2f kg".format(row.massKg), "R$ %.2f".format(row.valueRS))
            }
            drawTable(canvas, y, listOf("DEFEITO", "QTD (kg)", "VALOR (R$)"), detailData, paints)
        }

        document.finishPage(page)
    }

    // =========================================================================
    // UTILITÁRIOS
    // =========================================================================

    private fun drawTable(
        canvas: Canvas, yStart: Float, headers: List<String>,
        data: List<List<String>>, paints: Paints
    ): Float {
        val margin      = 50f
        val columnWidth = (pageWidth - 2 * margin) / headers.size.toFloat()
        val textPadding = 5f
        val maxTextWidth = columnWidth - (textPadding * 2) // Largura útil da célula
        val baseRowHeight = 25f
        val lineHeight  = paints.cellPaint.textSize + 6f
        var y           = yStart

        // Função auxiliar interna para quebrar o texto em várias linhas
        fun getLines(text: String, paint: Paint): List<String> {
            val words = text.split("\\s+".toRegex())
            val lines = mutableListOf<String>()
            var currentLine = StringBuilder()

            for (word in words) {
                val candidate = if (currentLine.isEmpty()) word else "$currentLine $word"
                if (paint.measureText(candidate) > maxTextWidth) {
                    if (currentLine.isNotEmpty()) lines.add(currentLine.toString())
                    currentLine = StringBuilder(word)
                } else {
                    currentLine = StringBuilder(candidate)
                }
            }
            if (currentLine.isNotEmpty()) lines.add(currentLine.toString())
            return if (lines.isEmpty()) listOf("") else lines
        }

        // Desenhar Cabeçalhos
        val headerLines = headers.map { getLines(it, paints.headerPaint) }
        val maxHeaderLines = headerLines.maxOfOrNull { it.size } ?: 1
        val headerRowHeight = maxOf(baseRowHeight, (maxHeaderLines * lineHeight) + 10f)

        canvas.drawRect(margin, y, margin + columnWidth * headers.size, y + headerRowHeight, paints.borderPaint)
        headers.forEachIndexed { i, _ ->
            val lines = headerLines[i]
            lines.forEachIndexed { j, line ->
                canvas.drawText(
                    line,
                    margin + (i * columnWidth) + textPadding,
                    y + 17f + (j * lineHeight),
                    paints.headerPaint
                )
            }
        }
        y += headerRowHeight

        // Desenhar Linhas de Dados
        data.forEach { row ->
            val cellLines = row.map { getLines(it, paints.cellPaint) }
            val maxLines = cellLines.maxOfOrNull { it.size } ?: 1
            val rowHeight = maxOf(baseRowHeight, (maxLines * lineHeight) + 10f)

            canvas.drawRect(margin, y, margin + columnWidth * headers.size, y + rowHeight, paints.borderPaint)
            row.forEachIndexed { i, _ ->
                val lines = cellLines[i]
                lines.forEachIndexed { j, line ->
                    canvas.drawText(
                        line,
                        margin + (i * columnWidth) + textPadding,
                        y + 17f + (j * lineHeight),
                        paints.cellPaint
                    )
                }
            }
            y += rowHeight
        }

        return y
    }

    private fun drawMultilineText(
        canvas: Canvas, text: String, x: Float, startY: Float, paint: Paint, maxWidth: Int
    ) {
        val words = text.split("\\s+".toRegex())
        var y     = startY
        var line  = StringBuilder()
        for (w in words) {
            val candidate = if (line.isEmpty()) w else "$line $w"
            if (paint.measureText(candidate) > maxWidth) {
                canvas.drawText(line.toString(), x, y, paint)
                line = StringBuilder(w)
                y += paint.textSize + 6f
            } else {
                if (line.isNotEmpty()) line.append(" ")
                line.append(w)
            }
        }
        if (line.isNotEmpty()) canvas.drawText(line.toString(), x, y, paint)
    }

    private fun setupPaints() = Paints(
        titlePaint  = Paint().apply { textSize = 18f; isFakeBoldText = true; textAlign = Paint.Align.CENTER },
        headerPaint = Paint().apply { textSize = 12f; isFakeBoldText = true; color = Color.DKGRAY },
        cellPaint   = Paint().apply { textSize = 11f; color = Color.BLACK },
        borderPaint = Paint().apply { style = Paint.Style.STROKE; strokeWidth = 1f; color = Color.LTGRAY }
    )

    private fun saveAndShare(context: Context, document: PdfDocument, grain: String) {
        try {
            val fileName = "${grain.lowercase()}_${System.currentTimeMillis()}.pdf"
            val fileDir  = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: appContext.filesDir
            val file     = File(fileDir, fileName)
            FileOutputStream(file).use { document.writeTo(it) }
            document.close()

            val authority = "${context.packageName}.fileprovider"
            val uri: Uri  = FileProvider.getUriForFile(context, authority, file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(intent, "Compartilhar PDF via")
            if (context !is android.app.Activity) chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)

            Log.i("PDFExporter", "PDF salvo: ${file.absolutePath}")
        } catch (e: Exception) {
            Log.e("PDFExporter", "Erro ao salvar PDF: ${e.message}", e)
            try { document.close() } catch (_: Exception) {}
        }
    }

    data class Paints(
        val titlePaint: Paint, val headerPaint: Paint,
        val cellPaint: Paint,  val borderPaint: Paint
    )
}

// =============================================================================
// HELPER GLOBAL
// =============================================================================

fun getTypeLabel(finalType: Int, group: Int): String {
    if (finalType == -1) return "---"
    if (finalType == 0) return "Desclassificada"
    if (finalType == 7) return "Fora de Tipo"

    return if (group == 2) {
        // Grupo 2 só tem um limite de referência (padrão básico)
        "Padrão Básico"
    } else when (finalType) {
        // Grupo 1 tem 3 limites de referência
        1    -> "Tipo 1"
        2    -> "Tipo 2"
        3    -> "Tipo 3"
        else -> "Tipo $finalType"
    }
}