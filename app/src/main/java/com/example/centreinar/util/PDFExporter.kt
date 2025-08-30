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
import com.example.centreinar.Classification
import com.example.centreinar.ColorClassificationSoja
import com.example.centreinar.DiscountSoja
import com.example.centreinar.InputDiscount
import com.example.centreinar.Limit
import com.example.centreinar.Sample
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class PDFExporter @Inject constructor() {

    data class RowData(
        val label: String,
        val weight: Float,
        val percentage: Float,
        val type: String
    )

    fun exportClassificationToPdf(
        context: Context,
        classification: Classification,
        sample: Sample,
        colorClassification: ColorClassificationSoja?,  // Make nullable
        observation: String?,                      // Make nullable
        defectLimits: Limit?                       // Make nullable
    ) {
        val document = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842

        // Page 1: Classification Results
        val page1 = createClassificationPage(document, pageWidth, pageHeight, classification, sample)
        document.finishPage(page1)

        // Page 2: Color Classification and Observations (only if available)
        if (colorClassification != null || !observation.isNullOrBlank()) {
            val page2 = createColorAndObservationsPage(
                document,
                pageWidth,
                pageHeight,
                colorClassification,
                observation
            )
            document.finishPage(page2)
        }

        // Page 3: Defect Limits (only if available)
        defectLimits?.let { limits ->
            val page3 = createDefectLimitsPage(document, pageWidth, pageHeight, limits)
            document.finishPage(page3)
        }

        // Save and share PDF
        saveAndShareDocument(context, document)
    }

    private fun createClassificationPage(
        document: PdfDocument,
        pageWidth: Int,
        pageHeight: Int,
        classification: Classification,
        sample: Sample
    ): PdfDocument.Page {
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paints = setupPaints()
        var yStart = 60f
        val xStart = 40f
        val tableWidth = 515f
        val rowHeight = 30f
        val colWidths = listOf(0.4f, 0.2f, 0.2f, 0.2f).map { it * tableWidth }

        // Title
        canvas.drawText("RESULTADO DA CLASSIFICAÇÃO", pageWidth / 2f, yStart, paints.titlePaint)
        yStart += 40f

        // Header
        val headers = listOf("DEFEITO", "Peso", "%", "TIPO")
        var x = xStart
        headers.forEachIndexed { i, text ->
            canvas.drawRect(x, yStart, x + colWidths[i], yStart + rowHeight, paints.borderPaint)
            canvas.drawText(text, x + 10f, yStart + 20f, paints.headerPaint)
            x += colWidths[i]
        }
        yStart += rowHeight

        // Data Rows
        val rows = listOf(
            RowData("Matéria Estranha e Impurezas", sample.foreignMattersAndImpurities, classification.foreignMattersPercentage, typeNumberToString(classification.foreignMatters)),
            RowData("Queimados", sample.burnt, classification.burntPercentage, typeNumberToString(classification.burnt)),
            RowData("Ardidos e Queimados", sample.burnt + sample.sour, classification.burntOrSourPercentage, typeNumberToString(classification.burntOrSour)),
            RowData("Mofados", sample.moldy, classification.moldyPercentage, typeNumberToString(classification.moldy)),
            RowData("Fermentados", sample.fermented, classification.fermentedPercentage, "-"),
            RowData("Germinados", sample.germinated, classification.germinatedPercentage, "-"),
            RowData("Imaturos", sample.immature, classification.immaturePercentage, "-"),
            RowData("Chochos", sample.shriveled, classification.shriveledPercentage, "-"),
            RowData("Danificados", sample.damaged, classification.damagedPercentage, "-"),
            RowData("Total de Avariados", sample.fermented + sample.germinated + sample.immature + sample.shriveled + sample.damaged, classification.spoiledPercentage, typeNumberToString(classification.spoiled)),
            RowData("Esverdeados", sample.greenish, classification.greenishPercentage, typeNumberToString(classification.greenish)),
            RowData("Partidos, Quebrados e Amassados", sample.brokenCrackedDamaged, classification.brokenCrackedDamagedPercentage, typeNumberToString(classification.brokenCrackedDamaged))
        )

        rows.forEach { row ->
            x = xStart
            val data = listOf(
                row.label,
                "%.2f".format(row.weight),
                "%.2f%%".format(row.percentage),
                row.type
            )
            data.forEachIndexed { i, text ->
                canvas.drawRect(x, yStart, x + colWidths[i], yStart + rowHeight, paints.borderPaint)
                canvas.drawText(text, x + 10f, yStart + 20f, paints.cellPaint)
                x += colWidths[i]
            }
            yStart += rowHeight
        }

        // Final Type
        yStart += 30f
        canvas.drawText("Tipo Final: ${typeNumberToString(classification.finalType)}", pageWidth / 2f, yStart, paints.titlePaint)

        return page
    }

    private fun createColorAndObservationsPage(
        document: PdfDocument,
        pageWidth: Int,
        pageHeight: Int,
        colorClassification: ColorClassificationSoja?,
        observation: String?
    ): PdfDocument.Page {
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 2).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paints = setupPaints()
        var yStart = 60f
        val xStart = 40f
        val contentWidth = 515f

        // Color Classification Section (if available)
        colorClassification?.let { colorClass ->
            canvas.drawText("Classe", pageWidth / 2f, yStart, paints.titlePaint)
            yStart += 40f

            // Draw color table
            yStart = drawColorTable(canvas, xStart, yStart, contentWidth, colorClass, paints)
            yStart += 30f
        }

        // Observations Section (if available)
        if (!observation.isNullOrBlank()) {
            canvas.drawText("OBSERVAÇÕES", pageWidth / 2f, yStart, paints.titlePaint)
            yStart += 40f

            val observationLines = wrapText(
                observation,
                paints.cellPaint,
                contentWidth - 20f
            )

            observationLines.forEach { line ->
                if (yStart < pageHeight - 50) {
                    canvas.drawText(line, xStart + 10f, yStart + 20f, paints.cellPaint)
                    yStart += 30f
                }
            }
        }

        return page
    }

    private fun drawColorTable(
        canvas: Canvas,
        xStart: Float,
        yStart: Float,
        tableWidth: Float,
        colorClass: ColorClassificationSoja,
        paints: Paints
    ): Float {
        var currentY = yStart
        val rowHeight = 30f
        val colWidths = listOf(0.7f * tableWidth, 0.3f * tableWidth)

        // Table Header
        var x = xStart
        listOf("", "%").forEachIndexed { i, text ->
            canvas.drawRect(x, currentY, x + colWidths[i], currentY + rowHeight, paints.borderPaint)
            if (i == 1) {
                canvas.drawText(
                    text,
                    x + colWidths[i]/2,
                    currentY + 20f,
                    paints.headerPaint.apply { textAlign = Paint.Align.CENTER }
                )
            }
            x += colWidths[i]
        }
        currentY += rowHeight

        // Table Rows
        listOf(
            "Amarela" to colorClass.yellowPercentage,
            "Outras Cores" to colorClass.otherColorPercentage
        ).forEach { (label, percentage) ->
            x = xStart
            listOf(label, "%.2f%%".format(percentage)).forEachIndexed { i, text ->
                canvas.drawRect(x, currentY, x + colWidths[i], currentY + rowHeight, paints.borderPaint)
                val align = if (i == 0) Paint.Align.LEFT else Paint.Align.CENTER
                canvas.drawText(
                    text,
                    x + if (i == 0) 10f else colWidths[i]/2,
                    currentY + 20f,
                    paints.cellPaint.apply { textAlign = align }
                )
                x += colWidths[i]
            }
            currentY += rowHeight
        }

        // Final Class
        currentY += 30f
        canvas.drawText(
            "Classe Final: ${colorClass.framingClass}",
            tableWidth / 2f,
            currentY,
            paints.titlePaint
        )

        return currentY + 40f
    }

    private fun createDefectLimitsPage(
        document: PdfDocument,
        pageWidth: Int,
        pageHeight: Int,
        defectLimits: Limit
    ): PdfDocument.Page {
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 3).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paints = setupPaints()
        var yStart = 60f
        val xStart = 40f
        val tableWidth = 515f
        val rowHeight = 30f
        val colWidths = listOf(0.7f * tableWidth, 0.3f * tableWidth)

        // Limits Title
        canvas.drawText("LIMITES UTILIZADOS", pageWidth / 2f, yStart, paints.titlePaint)
        yStart += 40f

        // Limits Header
        var x = xStart
        val limitHeaders = listOf("Defeito", "Limite de Tolerância (%)")
        limitHeaders.forEachIndexed { i, text ->
            canvas.drawRect(x, yStart, x + colWidths[i], yStart + rowHeight, paints.borderPaint)
            canvas.drawText(text, x + 10f, yStart + 20f, paints.headerPaint)
            x += colWidths[i]
        }
        yStart += rowHeight

        // Limits Data
        val defectPairs = listOf(
            "Impurezas" to Pair(defectLimits.impuritiesLowerLim, defectLimits.impuritiesUpLim),
            "Quebrados, Partidos e Amassados" to Pair(defectLimits.brokenCrackedDamagedLowerLim, defectLimits.brokenCrackedDamagedUpLim),
            "Esverdeados" to Pair(defectLimits.greenishLowerLim, defectLimits.greenishUpLim),
            "Queimados" to Pair(defectLimits.burntLowerLim, defectLimits.burntUpLim),
            "Mofados" to Pair(defectLimits.moldyLowerLim, defectLimits.moldyUpLim),
            "Ardidos e Queimados" to Pair(defectLimits.burntOrSourLowerLim, defectLimits.burntOrSourUpLim),
            "Total de Avariados" to Pair(defectLimits.spoiledTotalLowerLim, defectLimits.spoiledTotalUpLim)
        )

        defectPairs.forEach { (defect, limits) ->
            x = xStart
            val data = listOf(
                defect,
                "${"%.2f".format(limits.first)} - ${"%.2f".format(limits.second)}"
            )
            data.forEachIndexed { i, text ->
                canvas.drawRect(x, yStart, x + colWidths[i], yStart + rowHeight, paints.borderPaint)
                canvas.drawText(text, x + 10f, yStart + 20f, paints.cellPaint)
                x += colWidths[i]
            }
            yStart += rowHeight
        }

        return page
    }

    private fun setupPaints(): Paints {
        return Paints(
            titlePaint = Paint().apply {
                textSize = 18f
                isFakeBoldText = true
                color = Color.BLACK
                textAlign = Paint.Align.CENTER
            },
            headerPaint = Paint().apply {
                textSize = 14f
                isFakeBoldText = true
                color = Color.DKGRAY
            },
            cellPaint = Paint().apply {
                textSize = 12f
                color = Color.BLACK
            },
            borderPaint = Paint().apply {
                style = Paint.Style.STROKE
                strokeWidth = 1f
                color = Color.LTGRAY
            }
        )
    }
    private fun saveAndShareDocument(context: Context, document: PdfDocument) {
        val fileName = "classificacao_${System.currentTimeMillis()}.pdf"
        val fileDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val file = File(fileDir, fileName)

        try {
            FileOutputStream(file).use { stream ->
                document.writeTo(stream)
            }
            sharePdf(context, file)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            document.close()
        }
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val lines = mutableListOf<String>()
        var currentLine = ""

        for (word in text.split(" ")) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (paint.measureText(testLine) <= maxWidth) {
                currentLine = testLine
            } else {
                lines.add(currentLine)
                currentLine = word
            }
        }
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine)
        }
        return lines
    }

    fun exportDiscountToPdf(
        context: Context,
        discount: DiscountSoja,
        sample:InputDiscount,
        defectLimits: Limit? = null,
        classification: Classification? = null,
        sampleClassification: Sample? = null,
    ) {
        val document = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842

        // Page 1: Discount Results
        val page1 = createDiscountPage(document, pageWidth, pageHeight, discount)
        document.finishPage(page1)

        // Page 2: inputDiscount (if available)
        val page2 = createInputDiscountPage(document,pageWidth,pageHeight,sample)
        document.finishPage(page2)


        //Page 3:classification (if available)
        sampleClassification?.let{ originalSample ->
            classification?.let{ classi ->
                Log.e("PDFEXporter","classification started")
                val page3 = createClassificationPage(document, pageWidth, pageHeight, classification,sampleClassification)
                Log.e("PDFEXporter","classification finish ")
                document.finishPage(page3)
            }
        }

        // Page 4: Defect Limits (if available)
        defectLimits?.let { limits ->
            val page4 = createDefectLimitsPage(document, pageWidth, pageHeight, limits)
            document.finishPage(page4)
        }


        // Save and share PDF
        saveAndShareDocument(context, document)
    }
    private fun createInputDiscountPage(
        document: PdfDocument,
        pageWidth: Int,
        pageHeight: Int,
        inputDiscount: InputDiscount
    ): PdfDocument.Page{
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paints = setupPaints()
        var yStart = 60f
        val xStart = 40f
        val tableWidth = 515f
        val rowHeight = 30f
        val colWidths = listOf(0.5f, 0.25f, 0.25f).map { it * tableWidth }
        canvas.drawText("Dados da Amostra", pageWidth / 2f, yStart, paints.titlePaint)
        yStart += 40f
        // Header
        val headers = listOf("Defeitos", "%")
        var x = xStart
        headers.forEachIndexed { i, text ->
            canvas.drawRect(x, yStart, x + colWidths[i], yStart + rowHeight, paints.borderPaint)
            canvas.drawText(text, x + 10f, yStart + 20f, paints.headerPaint)
            x += colWidths[i]
        }
        yStart += rowHeight

        // Data Rows
        val rows = listOf(
            Pair("Matéria estranha e Impureza", inputDiscount.foreignMattersAndImpurities),
            Pair("Umidade", inputDiscount.humidity),
            Pair("Queimados", inputDiscount.burnt),
            Pair("Ardidos e Queimados", inputDiscount.burntOrSour),
            Pair("Mofados", inputDiscount.moldy),
            Pair("Ardidos", inputDiscount.spoiled),
            Pair("Esverdeados", inputDiscount.greenish),
            Pair("Partidos, Quebrados e Danificados", inputDiscount.brokenCrackedDamaged)
        )

        rows.forEach { row ->
            x = xStart
            val data = listOf(
                row.first,
                "%.2f".format(row.second),
            )
            data.forEachIndexed { i, text ->
                canvas.drawRect(x, yStart, x + colWidths[i], yStart + rowHeight, paints.borderPaint)
                canvas.drawText(text, x + 10f, yStart + 20f, paints.cellPaint)
                x += colWidths[i]
            }
            yStart += rowHeight

            // Add separator except after last row
            if (rows.last() != row) {
                canvas.drawLine(
                    xStart, yStart,
                    xStart + tableWidth, yStart,
                    paints.borderPaint
                )
                yStart += 5f
            }
        }

        return page
    }

    private fun createDiscountPage(
        document: PdfDocument,
        pageWidth: Int,
        pageHeight: Int,
        discount: DiscountSoja
    ): PdfDocument.Page {
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paints = setupPaints()
        var yStart = 60f
        val xStart = 40f
        val tableWidth = 515f
        val rowHeight = 30f
        val colWidths = listOf(0.5f, 0.25f, 0.25f).map { it * tableWidth }

        // Title
        canvas.drawText("RESULTADO DOS DESCONTOS", pageWidth / 2f, yStart, paints.titlePaint)
        yStart += 40f

        // Header
        val headers = listOf("TIPO DE QUEBRA", "QUANTIA (KG)", "VALOR (R$)")
        var x = xStart
        headers.forEachIndexed { i, text ->
            canvas.drawRect(x, yStart, x + colWidths[i], yStart + rowHeight, paints.borderPaint)
            canvas.drawText(text, x + 10f, yStart + 20f, paints.headerPaint)
            x += colWidths[i]
        }
        yStart += rowHeight

        // Simplified
        val rows = listOf(
            Triple("Desconto por Impurezas e Umidade", discount.humidityAndImpuritiesDiscount, discount.humidityAndImpuritiesDiscountPrice),
            Triple("Quebra técnica", discount.technicalLoss, discount.technicalLossPrice),
            Triple("Quebra da Classificação (sem deságio)", discount.classificationDiscount, discount.classificationDiscountPrice),
            Triple("Quebra da Classificação (com deságio)", discount.deduction, discount.deductionValue),
            Triple("Desconto Total", discount.finalDiscount, discount.finalDiscountPrice),
            Triple("Lote Líquido Final", discount.finalWeight, discount.finalWeightPrice),
            Triple("Matéria Estranha e Impurezas", discount.impuritiesLoss,discount.impuritiesLossPrice),
            Triple("Umidade", discount.humidityLoss,discount.humidityLossPrice),
            Triple("Queimados", discount.burntLoss, discount.burntLossPrice),
            Triple("Ardidos e Queimados", discount.burntOrSourLoss, discount.burntOrSourLossPrice),
            Triple("Mofados", discount.moldyLoss, discount.moldyLossPrice),
            Triple("Total de Avariados", discount.spoiledLoss,discount.spoiledLossPrice),
            Triple("Esverdeados", discount.greenishLoss,discount.greenishLossPrice),
            Triple("Partidos, Quebrados e Amassados", discount.brokenLoss,discount.brokenLossPrice)
        )


        rows.forEach { row ->
            x = xStart
            val data = listOf(
                row.first,
                "%.2f".format(row.second),
                "%.2f".format(row.third)
            )
            data.forEachIndexed { i, text ->
                canvas.drawRect(x, yStart, x + colWidths[i], yStart + rowHeight, paints.borderPaint)
                canvas.drawText(text, x + 10f, yStart + 20f, paints.cellPaint)
                x += colWidths[i]
            }
            yStart += rowHeight

            // Add separator except after last row
            if (rows.last() != row) {
                canvas.drawLine(
                    xStart, yStart,
                    xStart + tableWidth, yStart,
                    paints.borderPaint
                )
                yStart += 5f
            }
        }

        return page
    }

    private fun sharePdf(context: Context, file: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "Compartilhar PDF via"))
    }

    fun typeNumberToString(typeNum: Int): String {
        return when (typeNum) {
            0 -> "Desclassificado"
            7 -> "Fora de Tipo"
            else -> typeNum.toString()
        }
    }

    data class Paints(
        val titlePaint: Paint,
        val headerPaint: Paint,
        val cellPaint: Paint,
        val borderPaint: Paint
    )
}
