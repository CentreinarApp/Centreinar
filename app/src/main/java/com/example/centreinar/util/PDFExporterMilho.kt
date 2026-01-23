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
import com.example.centreinar.data.local.entity.DiscountMilho
import com.example.centreinar.data.local.entity.InputDiscountMilho
import com.example.centreinar.data.local.entity.LimitMilho
import com.example.centreinar.data.local.entity.SampleMilho
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
        limit: LimitMilho?
    ) {
        val document = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842

        // Página 1: Resultado da Classificação (Tabela com defeitos encontrados)
        val classPage = createClassificationPage(document, pageWidth, pageHeight, classification)
        document.finishPage(classPage)

        // Página 2: Dados da Amostra
        val samplePage = createSamplePage(document, pageWidth, pageHeight, sample)
        document.finishPage(samplePage)

        // Página 3: Limites Utilizados
        limit?.let { limits ->
            val limitPage = createDefectLimitsPage(document, pageWidth, pageHeight, limits)
            document.finishPage(limitPage)
        }

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