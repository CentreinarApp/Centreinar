package com.example.centreinar.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
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
import com.example.centreinar.ColorClassificationSoja
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
            val limitPage = createDefectLimitsPage(document, pageWidth, pageHeight, limits)
            document.finishPage(limitPage)
        }

        // Salva e compartilha o PDF
        saveAndShareDocument(context, document)
    }


    fun exportClassificationToPdf(
        context: Context,
        classification: ClassificationSoja,
        sample: SampleSoja,
        colorClassification: ColorClassificationSoja?,
        observation: String?,
        limit: LimitSoja?
    ) {
        val document = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842

        // Página 1 - Dados da classificação (Com Tabela)
        val classPage = createClassificationPage(document, pageWidth, pageHeight, classification)
        document.finishPage(classPage)

        // Página 2 - Dados da amostra (Sem alteração)
        val samplePage = createSampleClassificationPage(document, pageWidth, pageHeight, sample)
        document.finishPage(samplePage)

        // Página 3 - Limites (Com Tabela)
        limit?.let { limitData ->
            val limitPage = createDefectLimitsPage(document, pageWidth, pageHeight, limitData)
            document.finishPage(limitPage)
        }

        // Página 4 - Classificação de cor (se houver)
        colorClassification?.let { colorClass ->
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 4).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas
            val paints = setupPaints()

            var yStart = 60f
            canvas.drawText("CLASSIFICAÇÃO DE COR", pageWidth / 2f, yStart, paints.titlePaint)
            yStart += 36f
            drawMultilineText(canvas, colorClass.toString(), 50f, yStart, paints.cellPaint, pageWidth - 100)

            document.finishPage(page)
        }

        // Página 5 - Observações (se houver)
        observation?.takeIf { it.isNotBlank() }?.let { obs ->
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 5).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas
            val paints = setupPaints()

            var yStart = 60f
            canvas.drawText("OBSERVAÇÕES", pageWidth / 2f, yStart, paints.titlePaint)
            yStart += 36f
            drawMultilineText(canvas, obs, 50f, yStart, paints.cellPaint, pageWidth - 100)

            document.finishPage(page)
        }

        saveAndShareDocument(context, document)
    }

    // Funções de Criação de Página (Com Tabela)

    private fun createClassificationPage(document: PdfDocument, pageWidth: Int, pageHeight: Int, classification: ClassificationSoja): PdfDocument.Page {
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 4).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paints = setupPaints()
        var yStart = 60f

        canvas.drawText("RESULTADO DA CLASSIFICAÇÃO", pageWidth / 2f, yStart, paints.titlePaint)
        yStart += 40f

        // Dados para a tabela (Parameter, Percentage, Type Code)
        val data = listOf(
            listOf("Tipo Final", classification.finalType.toString(), ""),
            listOf("-------------------", "-------------------", "------"),
            listOf("Impurezas (%)", "%.2f".format(classification.impuritiesPercentage), classification.impuritiesType.toString()),
            listOf("Partidos/Quebrados (%)", "%.2f".format(classification.brokenCrackedDamagedPercentage), classification.brokenCrackedDamagedType.toString()),
            listOf("Esverdeados (%)", "%.2f".format(classification.greenishPercentage), classification.greenishType.toString()),
            listOf("Ardidos + Queimados (%)", "%.2f".format(classification.burntOrSourPercentage), classification.burntOrSourType.toString()),
            listOf("Mofados (%)", "%.2f".format(classification.moldyPercentage), classification.moldyType.toString()),
            listOf("Total Avariados (%)", "%.2f".format(classification.spoiledPercentage), classification.spoiledType.toString()),
            listOf("Queimados Máx (%)", "%.2f".format(classification.burntPercentage), classification.burntType.toString()),
        )

        // Desenha a tabela de classificação
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

    private fun createDefectLimitsPage(document: PdfDocument, pageWidth: Int, pageHeight: Int, limits: LimitSoja): PdfDocument.Page {
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 5).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paints = setupPaints()
        var yStart = 60f

        canvas.drawText("LIMITES DE TOLERÂNCIA (SOJA)", pageWidth / 2f, yStart, paints.titlePaint)
        yStart += 40f

        // Dados para a tabela de limites
        val data = listOf(
            listOf("Defeito", "Limite Mín.", "Limite Máx."),
            listOf("Matérias Estranhas", "%.2f".format(limits.impuritiesLowerLim), "%.2f".format(limits.impuritiesUpLim)),
            listOf("Partidos/Quebrados", "%.2f".format(limits.brokenCrackedDamagedLowerLim), "%.2f".format(limits.brokenCrackedDamagedUpLim)),
            listOf("Esverdeados", "%.2f".format(limits.greenishLowerLim), "%.2f".format(limits.greenishUpLim)),
            listOf("Ardidos + Queimados", "%.2f".format(limits.burntOrSourLowerLim), "%.2f".format(limits.burntOrSourUpLim)),
            listOf("Mofados", "%.2f".format(limits.moldyLowerLim), "%.2f".format(limits.moldyUpLim)),
            listOf("Total Avariados", "%.2f".format(limits.spoiledTotalLowerLim), "%.2f".format(limits.spoiledTotalUpLim)),
        )

        // Desenha a tabela de limites
        drawClassificationTable(
            canvas = canvas,
            yStart = yStart,
            pageWidth = pageWidth,
            headers = listOf("DEFEITO", "MÍN. (%)", "MÁX. (%)"),
            data = data,
            paints = paints
        )

        return page
    }

    // --- FUNÇÃO AUXILIAR PARA DESENHAR TABELA (NOVO) ---
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


    // --- FUNÇÕES DE LAYOUT (Mantidas) ---

    private fun createDiscountPage(document: PdfDocument, pageWidth: Int, pageHeight: Int, discount: DiscountSoja): PdfDocument.Page {
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paints = setupPaints()

        var yStart = 60f
        canvas.drawText("RESULTADO DOS DESCONTOS (SOJA)", pageWidth / 2f, yStart, paints.titlePaint)
        yStart += 40f
        // Simplesmente listamos os dados importantes, você pode adaptar para tabela se quiser
        canvas.drawText("Desconto total (Peso): ${"%.2f".format(discount.finalDiscount)} kg", 50f, yStart, paints.cellPaint)
        yStart += 20f
        canvas.drawText("Desconto total (Preço): ${"%.2f".format(discount.finalDiscountPrice)} R$", 50f, yStart, paints.cellPaint)
        yStart += 20f
        canvas.drawText("Quebra Impurezas: ${"%.2f".format(discount.impuritiesLoss)} kg", 50f, yStart, paints.cellPaint)

        return page
    }

    private fun createInputDiscountPage(document: PdfDocument, pageWidth: Int, pageHeight: Int, inputDiscount: InputDiscountSoja): PdfDocument.Page {
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 2).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paints = setupPaints()

        var yStart = 60f
        canvas.drawText("DADOS DA AMOSTRA (INPUT DESCONTO)", pageWidth / 2f, yStart, paints.titlePaint)
        yStart += 40f
        canvas.drawText("Umidade: ${inputDiscount.humidity}%", 50f, yStart, paints.cellPaint)
        yStart += 20f
        canvas.drawText("Impurezas: ${inputDiscount.foreignMattersAndImpurities}%", 50f, yStart, paints.cellPaint)
        yStart += 20f
        canvas.drawText("Preço/Saca: ${inputDiscount.lotPrice / (inputDiscount.lotWeight / 60f)} R$", 50f, yStart, paints.cellPaint)


        return page
    }

    private fun createSampleClassificationPage(document: PdfDocument, pageWidth: Int, pageHeight: Int, sampleClassification: SampleSoja): PdfDocument.Page {
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 5).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paints = setupPaints()

        var yStart = 60f
        canvas.drawText("AMOSTRA (CLASSIFICAÇÃO)", pageWidth / 2f, yStart, paints.titlePaint)
        yStart += 36f

        // Removido o toString() e usamos uma lista estruturada para evitar texto quebrado
        val text = "Peso da Amostra: ${sampleClassification.sampleWeight}g\n" +
                "Peso do Lote: ${sampleClassification.lotWeight}kg\n" +
                "Umidade: ${sampleClassification.humidity}%"
        drawMultilineText(canvas, text, 50f, yStart, paints.cellPaint, pageWidth - 100)

        return page
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
        // ... (o restante da função permanece o mesmo) ...
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