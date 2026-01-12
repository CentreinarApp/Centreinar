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
import com.example.centreinar.data.local.entity.DiscountMilho
import com.example.centreinar.data.local.entity.InputDiscountMilho
import com.example.centreinar.data.local.entity.LimitMilho
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PDFExporterMilho @Inject constructor(
    @ApplicationContext private val appContext: Context
) {
    // Reutilizando a estrutura de Paints do Soja
    data class Paints(val titlePaint: Paint, val headerPaint: Paint, val cellPaint: Paint, val borderPaint: Paint)

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

    // --- FUNÇÃO AUXILIAR PARA DESENHAR TABELA (Implementada aqui para Milho) ---
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


    private fun createDiscountPage(document: PdfDocument, pageWidth: Int, pageHeight: Int, discount: DiscountMilho): PdfDocument.Page {
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
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
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 2).create()
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
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 3).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paints = setupPaints()

        var yStart = 60f
        canvas.drawText("LIMITES DE TOLERÂNCIA (MILHO)", pageWidth / 2f, yStart, paints.titlePaint)
        yStart += 40f

        // Dados para a tabela de limites
        val data = listOf(
            listOf("Defeito", "Limite Máx. (%)"),
            listOf("Matérias Estranhas", "%.2f".format(limits.impuritiesUpLim)),
            listOf("Quebrados", "%.2f".format(limits.brokenUpLim)),
            listOf("Ardidos", "%.2f".format(limits.ardidoUpLim)),
            listOf("Mofados", "%.2f".format(limits.mofadoUpLim)), // Mofados é o Total de Avariados
            listOf("Carunchados", "%.2f".format(limits.carunchadoUpLim)),
            listOf("Umidade", "%.2f".format(limits.moistureUpLim)),
        )

        // Desenha a tabela de limites
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
            val fileName = "milho_${System.currentTimeMillis()}.pdf"
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