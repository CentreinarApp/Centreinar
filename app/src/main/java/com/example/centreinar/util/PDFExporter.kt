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
import com.example.centreinar.Classification
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject


class PDFExporter @Inject constructor() {

    fun exportClassificationToPdf(context: Context, classification: Classification) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val titlePaint = Paint().apply {
            textSize = 18f
            isFakeBoldText = true
            color = Color.BLACK
            textAlign = Paint.Align.CENTER
        }

        val headerPaint = Paint().apply {
            textSize = 14f
            isFakeBoldText = true
            color = Color.DKGRAY
        }

        val cellPaint = Paint().apply {
            textSize = 12f
            color = Color.BLACK
        }

        val borderPaint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = 1f
            color = Color.LTGRAY
        }

        var xStart = 40f
        var yStart = 60f
        val tableWidth = 515f
        val rowHeight = 30f
        val colWidths = listOf(0.5f, 0.25f, 0.25f).map { it * tableWidth }

        // Title
        canvas.drawText("RESULTADO DA CLASSIFICAÇÃO", pageInfo.pageWidth / 2f, yStart, titlePaint)
        yStart += 40f

        // Header
        val headers = listOf("DEFEITO", "%", "TIPO")
        var x = xStart
        headers.forEachIndexed { i, text ->
            canvas.drawRect(x, yStart, x + colWidths[i], yStart + rowHeight, borderPaint)
            canvas.drawText(text, x + 10f, yStart + 20f, headerPaint)
            x += colWidths[i]
        }
        yStart += rowHeight

        // Rows
        val rows = listOf(
            Triple("Matéria Estranha e Impurezas", classification.foreignMattersPercentage, typeNumberToString(classification.foreignMatters)),
            Triple("Queimados", classification.burntPercentage, typeNumberToString(classification.burnt)),
            Triple("Ardidos e Queimados", classification.burntOrSourPercentage, typeNumberToString(classification.burntOrSour)),
            Triple("Mofados", classification.moldyPercentage, typeNumberToString(classification.moldy)),
            Triple("Fermentados", classification.fermentedPercentage, "-"),
            Triple("Germinados", classification.germinatedPercentage, "-"),
            Triple("Imaturos", classification.immaturePercentage, "-"),
            Triple("Chochos", classification.shriveledPercentage, "-"),
            Triple("Fermentados", classification.fermentedPercentage, "-"),
            Triple("Total de Avariados", classification.spoiledPercentage, typeNumberToString(classification.spoiled)),
            Triple("Esverdeados", classification.greenishPercentage, typeNumberToString(classification.greenish)),
            Triple("Partidos, Quebrados e Amassados", classification.brokenCrackedDamagedPercentage, typeNumberToString(classification.brokenCrackedDamaged))
        )

        rows.forEach { (label, percentage, type) ->
            x = xStart
            val data = listOf(label, "%.2f%%".format(percentage), type)
            data.forEachIndexed { i, text ->
                canvas.drawRect(x, yStart, x + colWidths[i], yStart + rowHeight, borderPaint)
                canvas.drawText(text, x + 10f, yStart + 20f, cellPaint)
                x += colWidths[i]
            }
            yStart += rowHeight
        }

        // Final Type
        yStart += 30f
        canvas.drawText("Tipo Final: ${typeNumberToString(classification.finalType)}", pageInfo.pageWidth / 2f, yStart, titlePaint)

        document.finishPage(page)

        // Save PDF
        val fileName = "classificacao.pdf"
        val fileDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val file = File(fileDir, fileName)
        try {
            document.writeTo(FileOutputStream(file))
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            document.close()
        }

        // Share the PDF
        sharePdf(context, file)
    }

    private fun sharePdf(context: Context, file: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider", // Use your real package name here!
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "Compartilhar PDF via"))
    }
    fun typeNumberToString(typeNum:Int):String{
        if(typeNum == 0){
            return "Desclassificado"
        }
        if(typeNum == 7){
            return "Fora de Tipo"
        }
        return typeNum.toString()
    }
}
