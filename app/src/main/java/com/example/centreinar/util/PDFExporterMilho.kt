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
            val page3 = createDefectLimitsPage(document, pageWidth, pageHeight, limits)
            document.finishPage(page3)
        }

        saveAndShareDocument(context, document)
    }

    private fun createDiscountPage(document: PdfDocument, pageWidth: Int, pageHeight: Int, discount: DiscountMilho): PdfDocument.Page {
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paints = setupPaints()

        var yStart = 60f
        // título centralizado
        canvas.drawText("RESULTADO DOS DESCONTOS (MILHO)", pageWidth / 2f, yStart, paints.titlePaint)
        yStart += 40f
        canvas.drawText("Desconto total: ${discount.finalDiscount}", 50f, yStart, paints.cellPaint)

        return page
    }

    private fun createInputDiscountPage(document: PdfDocument, pageWidth: Int, pageHeight: Int, inputDiscount: InputDiscountMilho): PdfDocument.Page {
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 2).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paints = setupPaints()

        var yStart = 60f
        canvas.drawText("DADOS DA AMOSTRA (MILHO)", pageWidth / 2f, yStart, paints.titlePaint)
        yStart += 40f
        canvas.drawText("Umidade: ${inputDiscount.humidity}%", 50f, yStart, paints.cellPaint)
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
        canvas.drawText("LIMITES DE DEFEITOS (MILHO)", pageWidth / 2f, yStart, paints.titlePaint)
        yStart += 36f
        canvas.drawText("Impurezas (máx): ${limits.impuritiesUpLim}%", 50f, yStart, paints.cellPaint)
        yStart += 20f
        canvas.drawText("Partidos (máx): ${limits.brokenUpLim}%", 50f, yStart, paints.cellPaint)
        yStart += 20f
        canvas.drawText("Ardidos (máx): ${limits.ardidoUpLim}%", 50f, yStart, paints.cellPaint)
        yStart += 20f
        canvas.drawText("Mofados (máx): ${limits.mofadoUpLim}%", 50f, yStart, paints.cellPaint)
        yStart += 20f
        canvas.drawText("Carunchado (máx): ${limits.carunchadoUpLim}%", 50f, yStart, paints.cellPaint)

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

            // Start chooser safely (se estiver chamando do non-activity context, pode precisar definir FLAG_ACTIVITY_NEW_TASK)
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

    data class Paints(val titlePaint: Paint, val headerPaint: Paint, val cellPaint: Paint, val borderPaint: Paint)
}
