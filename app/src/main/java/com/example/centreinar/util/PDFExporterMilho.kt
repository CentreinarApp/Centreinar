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
import com.example.centreinar.ClassificationMilho
import com.example.centreinar.data.local.entity.ColorClassificationMilho
import com.example.centreinar.data.local.entity.DiscountMilho
import com.example.centreinar.data.local.entity.DisqualificationMilho
import com.example.centreinar.data.local.entity.InputDiscountMilho
import com.example.centreinar.data.local.entity.LimitMilho
import com.example.centreinar.data.local.entity.SampleMilho
import com.example.centreinar.data.local.entity.ToxicSeedMilho
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PDFExporterMilho @Inject constructor(
    @ApplicationContext private val appContext: Context
) {
    // Definição das styles
    data class Paints(val titlePaint: Paint, val headerPaint: Paint, val cellPaint: Paint, val borderPaint: Paint)

    // EXPORTAR CLASSIFICAÇÃO
    fun exportClassificationToPdf(
        context: Context,
        classification: ClassificationMilho,
        sample: SampleMilho,
        colorClassification: ColorClassificationMilho?,
        disqualification: DisqualificationMilho?,
        toxicSeeds: List<ToxicSeedMilho>?,
        observation: String?,
        limits: List<LimitMilho>?
    ) {
        val document = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val paints = setupPaints()

        // --- PÁGINA 1: RESULTADO (UMIDADE + TABELA + COMPLEMENTARES) ---
        val pageInfo1 = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page1 = document.startPage(pageInfo1)
        val canvas1 = page1.canvas
        var y = 60f

        canvas1.drawText("RESULTADO DA CLASSIFICAÇÃO (MILHO)", pageWidth / 2f, y, paints.titlePaint)
        y += 40f

        // Box de Umidade (Comparação com Limite)
        val moistureLimit = limits?.firstOrNull()?.moistureUpLim ?: 14.0f
        canvas1.drawRect(50f, y, pageWidth - 50f, y + 45f, paints.borderPaint)
        canvas1.drawText("DADOS DE UMIDADE", 60f, y + 18f, paints.headerPaint)
        canvas1.drawText(
            "Umidade da Amostra: %.1f%% | Limite de Referência: %.1f%%".format(classification.moisturePercentage, moistureLimit),
            60f, y + 36f, paints.cellPaint
        )
        y += 65f

        // Tabela de Resultados
        val formatType = { typeCode: Int -> if (typeCode == 0) "--" else getFinalTypeLabel(typeCode) }

        val tableData = listOf(
            listOf("Matéria Estranha/Imp (%)", "%.2f".format(classification.impuritiesPercentage), formatType(classification.impuritiesType)),
            listOf("Quebrados (%)", "%.2f".format(classification.brokenPercentage), formatType(classification.brokenType)),
            listOf("Ardidos (%)", "%.2f".format(classification.ardidoPercentage), formatType(classification.ardidoType)),
            listOf("Mofados (%)", "%.2f".format(classification.mofadoPercentage), formatType(classification.mofadoType)),
            listOf("Fermentados (%)", "%.2f".format(classification.fermentedPercentage), "--"),
            listOf("Germinados (%)", "%.2f".format(classification.germinatedPercentage), "--"),
            listOf("Chochos/Imaturos (%)", "%.2f".format(classification.immaturePercentage), "--"),
            listOf("Gessados (%)", "%.2f".format(classification.gessadoPercentage), "--"),
            listOf("Total Avariados (%)", "%.2f".format(classification.spoiledTotalPercentage), formatType(classification.spoiledTotalType)),
            listOf("Carunchados (%)", "%.2f".format(classification.carunchadoPercentage), formatType(classification.carunchadoType)),
            listOf("-------------------", "-----------", "-------"),
            listOf("TIPO FINAL", "---", getFinalTypeLabel(classification.finalType))
        )

        y = drawClassificationTable(canvas1, y, pageWidth, listOf("PARÂMETRO", "PERCENTUAL", "TIPO"), tableData, paints)
        y += 25f

        // Classe e Grupo (Se houver)
        colorClassification?.let { comp ->
            canvas1.drawRect(50f, y, pageWidth - 50f, y + 55f, paints.borderPaint)
            canvas1.drawText("CLASSE: ${comp.framingClass} | GRUPO: ${comp.framingGroup}", 60f, y + 18f, paints.headerPaint)
            canvas1.drawText(
                "Amarela: %.1f%% | Duro: %.1f%% | Dentado: %.1f%%".format(comp.yellowPercentage, comp.duroPercentage, comp.dentadoPercentage),
                60f, y + 40f, paints.cellPaint
            )
        }
        document.finishPage(page1)

        // --- PÁGINA 2: AMOSTRA E DESCLASSIFICAÇÃO ---
        val pageInfo2 = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 2).create()
        val page2 = document.startPage(pageInfo2)
        val canvas2 = page2.canvas
        y = 60f

        canvas2.drawText("DADOS COMPLEMENTARES", pageWidth / 2f, y, paints.titlePaint)
        y += 40f

        // Dados da Amostra
        canvas2.drawText("AMOSTRA:", 50f, y, paints.headerPaint); y += 20f
        val sampleText = "Peso Amostra: ${sample.sampleWeight}g\nPeso Lote: ${sample.lotWeight}kg\nUmidade: ${sample.moisture}%"
        drawMultilineText(canvas2, sampleText, 60f, y, paints.cellPaint, pageWidth - 100)
        y += 60f

        // Desclassificação
        disqualification?.let { disq ->
            canvas2.drawText("MOTIVOS DE DESCLASSIFICAÇÃO:", 50f, y, paints.headerPaint); y += 20f
            val status = { v: Int -> if (v == 1) "SIM" else "NÃO" }
            canvas2.drawText("- Mau estado: ${status(disq.badConservation)}", 60f, y, paints.cellPaint); y += 15f
            canvas2.drawText("- Odor estranho: ${status(disq.strangeSmell)}", 60f, y, paints.cellPaint); y += 15f
            canvas2.drawText("- Insetos vivos: ${status(disq.insects)}", 60f, y, paints.cellPaint); y += 15f

            if (disq.toxicGrains == 1) {
                canvas2.drawText("- Sementes Tóxicas: SIM", 60f, y, paints.cellPaint); y += 15f
                toxicSeeds?.forEach { seed ->
                    canvas2.drawText("  • ${seed.name}: ${seed.quantity}", 75f, y, paints.cellPaint); y += 15f
                }
            }
        }
        document.finishPage(page2)

        // --- PÁGINA 3: TABELA DE REFERÊNCIA OFICIAL (Estilo OfficialReferenceTable) ---
        val pageInfo3 = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 3).create()
        val page3 = document.startPage(pageInfo3)
        val canvas3 = page3.canvas
        y = 60f

        limits?.takeIf { it.isNotEmpty() }?.let { dataList ->
            canvas3.drawText("LIMITES DE REFERÊNCIA UTILIZADOS", pageWidth / 2f, y, paints.titlePaint)
            y += 40f

            val labels = listOf("Ardidos", "Avariados Total", "Quebrados", "Matérias Estranhas", "Carunchados")
            // Criar o cabeçalho dinâmico (Tipo 1, Tipo 2, Tipo 3, Fora de Tipo...)
            val headers = mutableListOf("Defeito")
            dataList.forEachIndexed { index, item ->
                val headerText = if (item.group == 2) {
                    "Padrão Básico"
                } else if (item.group == 1 && (index + 1) == 4) {
                    "Fora de Tipo"
                } else {
                    "Tipo ${index + 1}"
                }
                headers.add(headerText)
            }

            val limitTableData = labels.mapIndexed { rowIndex, label ->
                val row = mutableListOf(label)
                dataList.forEach { item ->
                    val value = when (rowIndex) {
                        0 -> item.ardidoUpLim
                        1 -> item.spoiledTotalUpLim
                        2 -> item.brokenUpLim
                        3 -> item.impuritiesUpLim
                        4 -> item.carunchadoUpLim
                        else -> 0f
                    }
                    row.add("%.1f%%".format(value))
                }
                row
            }

            y = drawClassificationTable(canvas3, y, pageWidth, headers, limitTableData, paints)
            y += 40f
        }

        observation?.takeIf { it.isNotBlank() }?.let { obs ->
            canvas3.drawText("OBSERVAÇÕES", 50f, y, paints.headerPaint); y += 15f
            drawMultilineText(canvas3, obs, 50f, y, paints.cellPaint, pageWidth - 100)
        }
        document.finishPage(page3)

        saveAndShareDocument(context, document)
    }


    // EXPORTAR DESCONTO
    fun exportDiscountToPdf(
        context: Context,
        discount: DiscountMilho,
        sample: InputDiscountMilho,
        defectLimits: LimitMilho? = null
    ) {
        val document = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842

        val page1 = createDiscountPage(document, pageWidth, pageHeight, discount)
        document.finishPage(page1)

        val page2 = createInputDiscountPage(document, pageWidth, pageHeight, sample)
        document.finishPage(page2)

        defectLimits?.let { limits ->
            val limitPage = createDefectLimitsPage(document, pageWidth, pageHeight, limits)
            document.finishPage(limitPage)
        }

        saveAndShareDocument(context, document)
    }


    // CRIAÇÃO DE PÁGINAS
    private fun createClassificationPage(document: PdfDocument, pageWidth: Int, pageHeight: Int, classification: ClassificationMilho): PdfDocument.Page {
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paints = setupPaints()
        var yStart = 60f

        canvas.drawText("RESULTADO DA CLASSIFICAÇÃO (MILHO)", pageWidth / 2f, yStart, paints.titlePaint)
        yStart += 40f

        // Montando os dados para a tabela
        val data = listOf(
            listOf("Tipo Final", classification.finalType.toString(), ""),
            listOf("-------------------", "-------------------", "------"),
            listOf("Umidade (%)", "%.2f".format(classification.moisturePercentage), "-"),
            listOf("Matéria Estranha (%)", "%.2f".format(classification.impuritiesPercentage), classification.impuritiesType.toString()),
            listOf("Quebrados (%)", "%.2f".format(classification.brokenPercentage), classification.brokenType.toString()),
            listOf("Total Avariados (%)", "%.2f".format(classification.spoiledTotalPercentage), classification.spoiledTotalType.toString()),
            listOf("Ardidos (%)", "%.2f".format(classification.ardidoPercentage), classification.ardidoType.toString()),
            listOf("Mofados (%)", "%.2f".format(classification.mofadoPercentage), classification.mofadoType.toString()),
            listOf("Carunchados (%)", "%.2f".format(classification.carunchadoPercentage), classification.carunchadoType.toString()),
            listOf("Fermentados (%)", "%.2f".format(classification.fermentedPercentage), "-"),
            listOf("Germinados (%)", "%.2f".format(classification.germinatedPercentage), "-"),
            listOf("Chochos/Imaturos (%)", "%.2f".format(classification.immaturePercentage), "-"),
            listOf("Gessados (%)", "%.2f".format(classification.gessadoPercentage), "-")
        )

        drawClassificationTable(
            canvas = canvas,
            yStart = yStart,
            pageWidth = pageWidth,
            headers = listOf("PARÂMETRO", "PERCENTUAL", "TIPO"),
            data = data,
            paints = paints
        )

        return page
    }

    private fun createSamplePage(document: PdfDocument, pageWidth: Int, pageHeight: Int, sample: SampleMilho): PdfDocument.Page {
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 2).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paints = setupPaints()

        var yStart = 60f
        canvas.drawText("DADOS DA AMOSTRA", pageWidth / 2f, yStart, paints.titlePaint)
        yStart += 40f

        val text = "Peso da Amostra: ${sample.sampleWeight}g\n" +
                "Peso do Lote: ${sample.lotWeight}kg\n" +
                "Umidade: ${sample.moisture}%"

        drawMultilineText(canvas, text, 50f, yStart, paints.cellPaint, pageWidth - 100)

        return page
    }

    private fun createDiscountPage(document: PdfDocument, pageWidth: Int, pageHeight: Int, discount: DiscountMilho): PdfDocument.Page {
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 3).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paints = setupPaints()

        var yStart = 60f
        canvas.drawText("RESULTADO DOS DESCONTOS (MILHO)", pageWidth / 2f, yStart, paints.titlePaint)
        yStart += 40f
        canvas.drawText("Desconto total (Peso): ${"%.2f".format(discount.finalDiscount)} kg", 50f, yStart, paints.cellPaint)
        yStart += 20f
        canvas.drawText("Quebra Umidade: ${"%.2f".format(discount.humidityLoss)} kg", 50f, yStart, paints.cellPaint)

        return page
    }

    private fun createInputDiscountPage(document: PdfDocument, pageWidth: Int, pageHeight: Int, inputDiscount: InputDiscountMilho): PdfDocument.Page {
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 4).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paints = setupPaints()

        var yStart = 60f
        canvas.drawText("DADOS DA AMOSTRA (INPUT DESCONTO)", pageWidth / 2f, yStart, paints.titlePaint)
        yStart += 40f
        canvas.drawText("Umidade: ${inputDiscount.moisture}%", 50f, yStart, paints.cellPaint)
        yStart += 20f
        canvas.drawText("Impurezas: ${inputDiscount.impurities}%", 50f, yStart, paints.cellPaint)

        return page
    }

    private fun createDefectLimitsPage(document: PdfDocument, pageWidth: Int, pageHeight: Int, limits: LimitMilho): PdfDocument.Page {
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 5).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paints = setupPaints()

        var yStart = 60f
        canvas.drawText("LIMITES DE TOLERÂNCIA (MILHO)", pageWidth / 2f, yStart, paints.titlePaint)
        yStart += 40f

        val data = listOf(
            listOf("Defeito", "Limite Máx. (%)"),
            listOf("Matérias Estranhas", "%.2f".format(limits.impuritiesUpLim)),
            listOf("Quebrados", "%.2f".format(limits.brokenUpLim)),
            listOf("Ardidos", "%.2f".format(limits.ardidoUpLim)),
            listOf("Carunchados", "%.2f".format(limits.carunchadoUpLim)),
            listOf("Umidade", "%.2f".format(limits.moistureUpLim)),
            listOf("Total Avariados", "%.2f".format(limits.spoiledTotalUpLim))
        )

        drawClassificationTable(
            canvas = canvas,
            yStart = yStart,
            pageWidth = pageWidth,
            headers = listOf("PARÂMETRO", "MÁX. (%)"),
            data = data,
            paints = paints
        )

        return page
    }


    // UTILITÁRIOS GRÁFICOS
    private fun drawClassificationTable(
        canvas: Canvas,
        yStart: Float,
        pageWidth: Int,
        headers: List<String>,
        data: List<List<String>>,
        paints: Paints
    ): Float {
        val margin = 50f
        val columnWidth = (pageWidth - 2 * margin) / headers.size.toFloat()
        val rowHeight = 30f
        var y = yStart
        var x = margin

        // Desenhar Cabeçalhos
        canvas.drawRect(x, y, x + columnWidth * headers.size, y + rowHeight, paints.borderPaint)
        for ((index, header) in headers.withIndex()) {
            val cellX = x + index * columnWidth
            canvas.drawText(header, cellX + 5f, y + rowHeight / 2f + 5f, paints.headerPaint)
        }
        y += rowHeight

        // Desenhar Dados
        for (row in data) {
            canvas.drawRect(x, y, x + columnWidth * headers.size, y + rowHeight, paints.borderPaint)
            for ((index, cell) in row.withIndex()) {
                val cellX = x + index * columnWidth
                canvas.drawText(cell, cellX + 5f, y + rowHeight / 2f + 5f, paints.cellPaint)
            }
            y += rowHeight
        }
        return y
    }

    private fun drawMultilineText(
        canvas: Canvas,
        text: String,
        x: Float,
        startY: Float,
        paint: Paint,
        maxWidth: Int
    ) {
        val words = text.split("\\s+".toRegex())
        var y = startY
        var line = StringBuilder()

        // Verifica se há quebras de linha explícitas (\n) no texto
        val lines = text.split("\n")

        // Se houver quebras manuais, desenha linha por linha
        if (lines.size > 1) {
            for (l in lines) {
                canvas.drawText(l, x, y, paint)
                y += paint.textSize + 6f
            }
            return
        }

        // Lógica de quebra automática
        for (w in words) {
            val candidate = if (line.isEmpty()) w else line.toString() + " " + w
            if (paint.measureText(candidate) > maxWidth) {
                canvas.drawText(line.toString(), x, y, paint)
                line = StringBuilder(w)
                y += paint.textSize + 6f
            } else {
                if (line.isNotEmpty()) line.append(" ")
                line.append(w)
            }
        }
        if (line.isNotEmpty()) {
            canvas.drawText(line.toString(), x, y, paint)
        }
    }

    private fun setupPaints(): Paints {
        return Paints(
            Paint().apply { textSize = 18f; isFakeBoldText = true; color = Color.BLACK; textAlign = Paint.Align.CENTER },
            Paint().apply { textSize = 14f; isFakeBoldText = true; color = Color.DKGRAY; textAlign = Paint.Align.LEFT },
            Paint().apply { textSize = 12f; color = Color.BLACK; textAlign = Paint.Align.LEFT },
            Paint().apply { style = Paint.Style.STROKE; strokeWidth = 1f; color = Color.LTGRAY }
        )
    }

    private fun saveAndShareDocument(context: Context, document: PdfDocument) {
        try {
            val fileName = "milho_classificacao_${System.currentTimeMillis()}.pdf"
            val fileDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: appContext.filesDir
            val file = File(fileDir, fileName)
            FileOutputStream(file).use { stream -> document.writeTo(stream) }
            document.close()

            val authority = "${context.packageName}.fileprovider"
            val uri: Uri = FileProvider.getUriForFile(context, authority, file)

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(intent, "Compartilhar PDF via")
            if (context !is android.app.Activity) chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)

            Log.i("PDFExporterMilho", "PDF salvo e pronto para compartilhar: ${file.absolutePath}")
        } catch (e: Exception) {
            Log.e("PDFExporterMilho", "Erro ao salvar/compartilhar PDF: ${e.message}", e)
            try {
                document.close()
            } catch (_: Exception) { /* ignore */ }
        }
    }
}

private fun getFinalTypeLabel(type: Int): String = when (type) {
    0 -> "Desclassificada"
    7 -> "Fora de Tipo"
    else -> "Tipo $type"
}