package com.example.centreinar.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.example.centreinar.ClassificationSoja
import com.example.centreinar.ColorClassificationSoja
import com.example.centreinar.DiscountSoja
import com.example.centreinar.InputDiscountSoja
import com.example.centreinar.LimitSoja
import com.example.centreinar.SampleSoja
import com.example.centreinar.utils.PdfPaints
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class PDFExporterSoja @Inject constructor() {

    data class RowData(
        val label: String,
        val weight: Float,
        val percentage: Float,
        val type: String
    )

    fun exportClassificationToPdf(
        context: Context,
        classification: ClassificationSoja,
        sample: SampleSoja,
        colorClassification: ColorClassificationSoja?,
        observation: String?,
        limit: LimitSoja?,
    ) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val paints = PdfPaints()
        var xStart = 50f
        var yStart = 80f

        // 🔹 Cabeçalho
        canvas.drawText("Relatório de Classificação - Soja", xStart, yStart, paints.titlePaint)
        yStart += 40f

        // 🔹 Dados gerais de classificação
        canvas.drawText("Classificação: ${classification.finalType}", xStart, yStart, paints.cellPaint)
        yStart += 20f

        canvas.drawText("Classe de Cor: ${colorClassification?.framingClass ?: "N/A"}", xStart, yStart, paints.cellPaint)
        yStart += 20f

        canvas.drawText("Limite: ${limit?.id ?: "N/A"}", xStart, yStart, paints.cellPaint)
        yStart += 20f

        canvas.drawText("Observações: ${observation ?: "Nenhuma"}", xStart, yStart, paints.cellPaint)
        yStart += 40f

        // 🔹 Dados da amostra
        canvas.drawText("Grão: ${sample.grain}", xStart, yStart, paints.cellPaint)
        yStart += 20f
        canvas.drawText("Grupo: ${sample.group}", xStart, yStart, paints.cellPaint)
        yStart += 20f
        canvas.drawText("Peso do Lote: ${sample.lotWeight} kg", xStart, yStart, paints.cellPaint)
        yStart += 20f
        canvas.drawText("Peso da Amostra: ${sample.sampleWeight} kg", xStart, yStart, paints.cellPaint)
        yStart += 20f
        canvas.drawText("Peso Limpo: ${sample.cleanWeight} kg", xStart, yStart, paints.cellPaint)
        yStart += 40f

        // 🔹 Características da classificação
        canvas.drawText("Umidade: ${sample.humidity}%", xStart, yStart, paints.cellPaint)
        yStart += 20f
        canvas.drawText("Impurezas: ${sample.foreignMattersAndImpurities}%", xStart, yStart, paints.cellPaint)
        yStart += 20f
        canvas.drawText("Partidos e Danificados: ${sample.brokenCrackedDamaged}%", xStart, yStart, paints.cellPaint)
        yStart += 20f
        canvas.drawText("Verdes (Greenish): ${sample.greenish}%", xStart, yStart, paints.cellPaint)
        yStart += 20f
        canvas.drawText("Queimados: ${sample.burnt}%", xStart, yStart, paints.cellPaint)
        yStart += 20f
        canvas.drawText("Mofados: ${sample.moldy}%", xStart, yStart, paints.cellPaint)
        yStart += 20f
        canvas.drawText("Ardidos (Sour): ${sample.sour}%", xStart, yStart, paints.cellPaint)
        yStart += 20f
        canvas.drawText("Fermentados: ${sample.fermented}%", xStart, yStart, paints.cellPaint)
        yStart += 20f
        canvas.drawText("Germinados: ${sample.germinated}%", xStart, yStart, paints.cellPaint)
        yStart += 20f
        canvas.drawText("Imaturos: ${sample.immature}%", xStart, yStart, paints.cellPaint)
        yStart += 20f
        canvas.drawText("Enrugados (Shriveled): ${sample.shriveled}%", xStart, yStart, paints.cellPaint)
        yStart += 30f

        // 🔹 Rodapé
        canvas.drawText("Relatório gerado automaticamente pelo sistema", xStart, yStart, paints.footerPaint)
        yStart += 15f
        canvas.drawText(
            "Data: ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(java.util.Date())}",
            xStart, yStart, paints.footerPaint
        )

        // Finaliza a página e grava o PDF
        document.finishPage(page)

        // Salva e compartilha
        saveAndShareDocument(context, document)
    }


    // 🔹 Função para exportar descontos e limites
    fun exportDiscountToPdf(
        context: Context,
        discount: DiscountSoja,
        sample: InputDiscountSoja,
        defectLimits: LimitSoja? = null,
        classification: ClassificationSoja? = null,
        sampleClassification: SampleSoja? = null,
    ) {
        val document = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842

        val page1 = createDiscountPage(document, pageWidth, pageHeight, discount)
        document.finishPage(page1)

        val page2 = createInputDiscountPage(document, pageWidth, pageHeight, sample)
        document.finishPage(page2)

        sampleClassification?.let { originalSample ->
            classification?.let { classi ->
                val page3 = createClassificationPage(document, pageWidth, pageHeight, classi, sampleClassification)
                document.finishPage(page3)
            }
        }

        defectLimits?.let { limits ->
            val page4 = createDefectLimitsPage(document, pageWidth, pageHeight, limits)
            document.finishPage(page4)
        }

        saveAndShareDocument(context, document)
    }

    // --- Métodos auxiliares ---

    private fun createDiscountPage(document: PdfDocument, pageWidth: Int, pageHeight: Int, discount: DiscountSoja): PdfDocument.Page {
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paints = setupPaints()

        var yStart = 60f
        val xStart = 40f
        val tableWidth = 515f
        val rowHeight = 30f
        val colWidths = listOf(0.5f, 0.25f, 0.25f).map { it * tableWidth }

        canvas.drawText("RESULTADO DOS DESCONTOS (SOJA)", pageWidth / 2f, yStart, paints.titlePaint)
        yStart += 40f

        val headers = listOf("TIPO DE QUEBRA", "QUANTIA (KG)", "VALOR (R$)")
        var x = xStart
        headers.forEachIndexed { i, text ->
            canvas.drawRect(x, yStart, x + colWidths[i], yStart + rowHeight, paints.borderPaint)
            canvas.drawText(text, x + 10f, yStart + 20f, paints.headerPaint)
            x += colWidths[i]
        }
        yStart += rowHeight

        val rows = listOf(
            Triple("Impurezas e Umidade", discount.humidityAndImpuritiesDiscount, discount.humidityAndImpuritiesDiscountPrice),
            Triple("Quebra Técnica", discount.technicalLoss, discount.technicalLossPrice),
            Triple("Classificação (sem deságio)", discount.classificationDiscount, discount.classificationDiscountPrice),
            Triple("Classificação (com deságio)", discount.deduction, discount.deductionValue),
            Triple("Total", discount.finalDiscount, discount.finalDiscountPrice),
            Triple("Lote Final", discount.finalWeight, discount.finalWeightPrice)
        )

        rows.forEach { row ->
            x = xStart
            val data = listOf("%.2f".format(row.second), "%.2f".format(row.third))
            canvas.drawText(row.first, x + 10f, yStart + 20f, paints.cellPaint); x += colWidths[0]
            data.forEachIndexed { i, text ->
                canvas.drawRect(x, yStart, x + colWidths[i + 1], yStart + rowHeight, paints.borderPaint)
                canvas.drawText(text, x + 10f, yStart + 20f, paints.cellPaint)
                x += colWidths[i + 1]
            }
            yStart += rowHeight
        }

        return page
    }

    private fun createInputDiscountPage(document: PdfDocument, pageWidth: Int, pageHeight: Int, inputDiscount: InputDiscountSoja): PdfDocument.Page {
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 2).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paints = setupPaints()

        var yStart = 60f
        val xStart = 40f

        canvas.drawText("DADOS DA AMOSTRA (SOJA)", pageWidth / 2f, yStart, paints.titlePaint)
        yStart += 40f
        canvas.drawText("Umidade: ${inputDiscount.humidity}%", xStart, yStart, paints.cellPaint); yStart += 20f
        canvas.drawText("Impurezas: ${inputDiscount.foreignMattersAndImpurities}%", xStart, yStart, paints.cellPaint)

        return page
    }

    private fun createClassificationPage(document: PdfDocument, pageWidth: Int, pageHeight: Int, classification: ClassificationSoja, sample: SampleSoja): PdfDocument.Page {
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 3).create()
        return document.startPage(pageInfo)
    }

    private fun createDefectLimitsPage(document: PdfDocument, pageWidth: Int, pageHeight: Int, limits: LimitSoja): PdfDocument.Page {
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 4).create()
        return document.startPage(pageInfo)
    }

    private fun setupPaints(): Paints {
        return Paints(
            Paint().apply { textSize = 18f; isFakeBoldText = true; color = Color.BLACK; textAlign = Paint.Align.CENTER },
            Paint().apply { textSize = 14f; isFakeBoldText = true; color = Color.DKGRAY },
            Paint().apply { textSize = 12f; color = Color.BLACK },
            Paint().apply { style = Paint.Style.STROKE; strokeWidth = 1f; color = Color.LTGRAY }
        )
    }

    private fun saveAndShareDocument(context: Context, document: PdfDocument) {
        val fileName = "soja_${System.currentTimeMillis()}.pdf"
        val fileDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val file = File(fileDir, fileName)
        FileOutputStream(file).use { stream -> document.writeTo(stream) }
        document.close()
        val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Compartilhar PDF via"))
    }

    data class Paints(
        val titlePaint: Paint,
        val headerPaint: Paint,
        val cellPaint: Paint,
        val borderPaint: Paint
    )
}
