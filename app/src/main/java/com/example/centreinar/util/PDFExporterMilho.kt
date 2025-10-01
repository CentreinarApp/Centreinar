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
import com.example.centreinar.data.local.entity.DiscountMilho
import com.example.centreinar.data.local.entity.InputDiscountMilho
import com.example.centreinar.data.local.entity.LimitMilho
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class PDFExporterMilho @Inject constructor() {

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
        val fileName = "milho_${System.currentTimeMillis()}.pdf"
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

    data class Paints(val titlePaint: Paint, val headerPaint: Paint, val cellPaint: Paint, val borderPaint: Paint)
}
