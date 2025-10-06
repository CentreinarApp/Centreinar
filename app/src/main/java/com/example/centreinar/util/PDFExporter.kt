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
import com.example.centreinar.ClassificationSoja
import com.example.centreinar.DiscountSoja
import com.example.centreinar.InputDiscountSoja
import com.example.centreinar.LimitSoja
import com.example.centreinar.SampleSoja
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PDFExporterSoja @Inject constructor(
    @ApplicationContext private val appContext: Context
) {

    fun exportDiscountToPdf(
        context: Context,
        discount: DiscountSoja,
        sample: InputDiscountSoja,
        defectLimits: LimitSoja? = null,
        classification: ClassificationSoja? = null,
        sampleClassification: SampleSoja? = null
    ) {
        val document = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842

        // Página 1: resultado
        val page1 = createDiscountPage(document, pageWidth, pageHeight, discount)
        document.finishPage(page1)

        // Página 2: dados da amostra/input
        val page2 = createInputDiscountPage(document, pageWidth, pageHeight, sample)
        document.finishPage(page2)

        // Página 3: classificação (se existir)
        classification?.let { classObj ->
            val classPage = createClassificationPage(document, pageWidth, pageHeight, classObj)
            document.finishPage(classPage)
        }

        // Página 4: amostra de classificação (se existir)
        sampleClassification?.let { sampleClassObj ->
            val sampleClassPage = createSampleClassificationPage(document, pageWidth, pageHeight, sampleClassObj)
            document.finishPage(sampleClassPage)
        }

        // Página 5: limites de defeitos (se existir)
        defectLimits?.let { limits ->
            val page3 = createDefectLimitsPage(document, pageWidth, pageHeight, limits)
            document.finishPage(page3)
        }

        // Salva e compartilha o PDF
        saveAndShareDocument(context, document)
    }


    private fun createDiscountPage(document: PdfDocument, pageWidth: Int, pageHeight: Int, discount: DiscountSoja): PdfDocument.Page {
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paints = setupPaints()

        var yStart = 60f
        canvas.drawText("RESULTADO DOS DESCONTOS (SOJA)", pageWidth / 2f, yStart, paints.titlePaint)
        yStart += 40f
        canvas.drawText("Desconto total: ${discount.finalDiscount}", 50f, yStart, paints.cellPaint)

        return page
    }

    private fun createInputDiscountPage(document: PdfDocument, pageWidth: Int, pageHeight: Int, inputDiscount: InputDiscountSoja): PdfDocument.Page {
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 2).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paints = setupPaints()

        var yStart = 60f
        canvas.drawText("DADOS DA AMOSTRA (SOJA)", pageWidth / 2f, yStart, paints.titlePaint)
        yStart += 40f
        canvas.drawText("Umidade: ${inputDiscount.humidity}%", 50f, yStart, paints.cellPaint)
        yStart += 20f
        canvas.drawText("Impurezas: ${inputDiscount.foreignMattersAndImpurities}%", 50f, yStart, paints.cellPaint)

        return page
    }

    /**
     * Página que imprime o objeto classification (usa toString() para segurança).
     */
    private fun createClassificationPage(
        document: PdfDocument,
        pageWidth: Int,
        pageHeight: Int,
        classification: ClassificationSoja
    ): PdfDocument.Page {
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 4).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paints = setupPaints()

        var yStart = 60f
        canvas.drawText("DADOS DA CLASSIFICAÇÃO", pageWidth / 2f, yStart, paints.titlePaint)
        yStart += 36f

        val text = classification.toString()
        drawMultilineText(canvas, text, 50f, yStart, paints.cellPaint, pageWidth - 100)

        return page
    }

    /**
     * Página que imprime os dados da amostra vinculada à classificação (usa toString()).
     */
    private fun createSampleClassificationPage(
        document: PdfDocument,
        pageWidth: Int,
        pageHeight: Int,
        sampleClassification: SampleSoja
    ): PdfDocument.Page {
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 5).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paints = setupPaints()

        var yStart = 60f
        canvas.drawText("AMOSTRA (CLASSIFICAÇÃO)", pageWidth / 2f, yStart, paints.titlePaint)
        yStart += 36f

        val text = sampleClassification.toString()
        drawMultilineText(canvas, text, 50f, yStart, paints.cellPaint, pageWidth - 100)

        return page
    }

    private fun createDefectLimitsPage(document: PdfDocument, pageWidth: Int, pageHeight: Int, limits: LimitSoja): PdfDocument.Page {
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 3).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paints = setupPaints()

        var yStart = 60f
        canvas.drawText("LIMITES DE DEFEITOS (SOJA)", pageWidth / 2f, yStart, paints.titlePaint)
        yStart += 36f
        canvas.drawText("Impurezas (máx): ${limits.impuritiesUpLim}%", 50f, yStart, paints.cellPaint)
        yStart += 20f
        canvas.drawText("Partidos (máx): ${limits.brokenCrackedDamagedUpLim}%", 50f, yStart, paints.cellPaint)
        yStart += 20f
        canvas.drawText("Ardidos (máx): ${limits.burntUpLim}%", 50f, yStart, paints.cellPaint)
        yStart += 20f
        canvas.drawText("Mofados (máx): ${limits.moldyUpLim}%", 50f, yStart, paints.cellPaint)
        yStart += 20f
        canvas.drawText("Carunchado (máx): ${limits.spoiledTotalUpLim}%", 50f, yStart, paints.cellPaint)

        return page
    }

    /**
     * Quebra texto longo em várias linhas para caber no PDF.
     */
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
            val fileName = "soja_${System.currentTimeMillis()}.pdf"
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

            Log.i("PDFExporterSoja", "PDF salvo e compartilhado: ${file.absolutePath}")
        } catch (e: Exception) {
            Log.e("PDFExporterSoja", "Erro ao salvar/compartilhar PDF: ${e.message}", e)
            try {
                document.close()
            } catch (_: Exception) { /* ignore */ }
        }
    }

    data class Paints(val titlePaint: Paint, val headerPaint: Paint, val cellPaint: Paint, val borderPaint: Paint)
}
