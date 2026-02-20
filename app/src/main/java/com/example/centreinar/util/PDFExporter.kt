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
import com.example.centreinar.DisqualificationSoja
import com.example.centreinar.data.local.entities.ToxicSeedSoja

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
            val classPage = createClassificationPage(document, pageWidth, pageHeight, classObj, sampleClassification ?: SampleSoja())
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
        disqualification: DisqualificationSoja?,
        toxicSeeds: List<ToxicSeedSoja>?,
        observation: String?,
        limits: List<LimitSoja>?
    ) {
        val document = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val paints = setupPaints()

        // --- PÁGINA 1: RESULTADO PRINCIPAL (UMIDADE + TABELA + COR) ---
        val pageInfo1 = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page1 = document.startPage(pageInfo1)
        val canvas1 = page1.canvas
        var y = 60f

        canvas1.drawText("RESULTADO DA CLASSIFICAÇÃO (SOJA)", pageWidth / 2f, y, paints.titlePaint)
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
        val translate = { typeCode: Int -> getFinalTypeLabel(typeCode, sample.group) }

        val tableData = listOf(
            listOf("Matéria Estranha/Imp (%)", "%.2f".format(classification.impuritiesPercentage), translate(classification.impuritiesType)),
            listOf("Ardidos (%)", "%.2f".format(classification.sourPercentage), "--"),
            listOf("Queimados (%)", "%.2f".format(classification.burntPercentage), translate(classification.burntType)),
            listOf("Total Ardidos + Queimados (%)", "%.2f".format(classification.burntOrSourPercentage), translate(classification.burntOrSourType)),
            listOf("Mofados (%)", "%.2f".format(classification.moldyPercentage), translate(classification.moldyType)),
            listOf("Fermentados (%)", "%.2f".format(classification.fermentedPercentage), "--"),
            listOf("Germinados (%)", "%.2f".format(classification.germinatedPercentage), "--"),
            listOf("Imaturos (%)", "%.2f".format(classification.immaturePercentage), "--"),
            listOf("Chochos (%)", "%.2f".format(classification.shriveledPercentage), "--"),
            listOf("Danificados (%)", "%.2f".format(classification.damagedPercentage), "--"),
            listOf("Total de Avariados (%)", "%.2f".format(classification.spoiledPercentage), translate(classification.spoiledType)),
            listOf("Esverdeados (%)", "%.2f".format(classification.greenishPercentage), translate(classification.greenishType)),
            listOf("Partidos/Quebrados (%)", "%.2f".format(classification.brokenCrackedDamagedPercentage), translate(classification.brokenCrackedDamagedType)),
            listOf(" ", " ", " "),
            listOf("TIPO FINAL", "---", translate(classification.finalType))
        )

        y = drawClassificationTable(canvas1, y, pageWidth, listOf("PARÂMETRO", "PERCENTUAL", "TIPO"), tableData, paints)
        y += 20f

        // Card de Cor (Se houver)
        colorClassification?.let { color ->
            canvas1.drawRect(50f, y, pageWidth - 50f, y + 45f, paints.borderPaint)
            canvas1.drawText(color.framingClass.uppercase(), 60f, y + 18f, paints.headerPaint)
            canvas1.drawText(
                "Amarela: %.2f%% | Outras Cores: %.2f%%".format(color.yellowPercentage, color.otherColorPercentage),
                60f, y + 36f, paints.cellPaint
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
        canvas2.drawText("Peso da Amostra: ${sample.sampleWeight}g", 60f, y, paints.cellPaint); y += 15f
        canvas2.drawText("Peso do Lote: ${sample.lotWeight}kg", 60f, y, paints.cellPaint); y += 30f

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

        // --- PÁGINA 3: LIMITES DE REFERÊNCIA ---
        val pageInfo3 = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 3).create()
        val page3 = document.startPage(pageInfo3)
        val canvas3 = page3.canvas
        y = 60f

        limits?.takeIf { it.isNotEmpty() }?.let { dataList ->
            canvas3.drawText("LIMITES DE REFERÊNCIA UTILIZADOS", pageWidth / 2f, y, paints.titlePaint)
            y += 40f

            val labels = listOf(
                "Ardidos e Queimados",
                "Queimados",
                "Mofados",
                "Avariados Total",
                "Esverdeados",
                "Partidos/Quebrados",
                "Matérias Estranhas"
            )

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

            // Mapear as linhas de dados cruzando labels com os valores de cada limite na lista
            val tableData = labels.mapIndexed { rowIndex, label ->
                val row = mutableListOf(label)
                dataList.forEach { item ->
                    val value = when (rowIndex) {
                        0 -> item.burntOrSourUpLim
                        1 -> item.burntUpLim
                        2 -> item.moldyUpLim
                        3 -> item.spoiledTotalUpLim
                        4 -> item.greenishUpLim
                        5 -> item.brokenCrackedDamagedUpLim
                        6 -> item.impuritiesUpLim
                        else -> 0f
                    }
                    row.add("%.2f%%".format(value))
                }
                row
            }

            // Desenha a tabela com colunas dinâmicas
            y = drawClassificationTable(
                canvas = canvas3,
                yStart = y,
                pageWidth = pageWidth,
                headers = headers,
                data = tableData,
                paints = paints
            )

            y += 40f
        }

        // Observações logo abaixo da tabela de limites
        observation?.takeIf { it.isNotBlank() }?.let { obs ->
            canvas3.drawText("OBSERVAÇÕES", 50f, y, paints.headerPaint)
            y += 15f
            drawMultilineText(canvas3, obs, 50f, y, paints.cellPaint, pageWidth - 100)
        }

        document.finishPage(page3)

        saveAndShareDocument(context, document)
    }

    // Funções de Criação de Página (Com Tabela)
    private fun createClassificationPage(document: PdfDocument, pageWidth: Int, pageHeight: Int, classification: ClassificationSoja, sample: SampleSoja): PdfDocument.Page {
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 4).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paints = setupPaints()
        var yStart = 60f

        canvas.drawText("RESULTADO DA CLASSIFICAÇÃO", pageWidth / 2f, yStart, paints.titlePaint)
        yStart += 40f

        // Dados para a tabela
        val data = listOf(
            listOf("TIPO FINAL", "---", getFinalTypeLabel(classification.finalType, sample.group)),
            listOf("-------------------", "-------------------", "------"),
            listOf("Impurezas (%)", "%.2f".format(classification.impuritiesPercentage), getFinalTypeLabel(classification.impuritiesType, sample.group)),
            listOf("Partidos/Quebrados (%)", "%.2f".format(classification.brokenCrackedDamagedPercentage), getFinalTypeLabel(classification.brokenCrackedDamagedType, sample.group)),
            listOf("Esverdeados (%)", "%.2f".format(classification.greenishPercentage), getFinalTypeLabel(classification.greenishType, sample.group)),
            listOf("Ardidos + Queimados (%)", "%.2f".format(classification.burntOrSourPercentage), getFinalTypeLabel(classification.burntOrSourType, sample.group)),
            listOf("Mofados (%)", "%.2f".format(classification.moldyPercentage), getFinalTypeLabel(classification.moldyType, sample.group)),
            listOf("Total Avariados (%)", "%.2f".format(classification.spoiledPercentage), getFinalTypeLabel(classification.spoiledType, sample.group)),
            listOf("Queimados Máx (%)", "%.2f".format(classification.burntPercentage), getFinalTypeLabel(classification.burntType, sample.group)),
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
    private fun drawClassificationTable(canvas: Canvas, yStart: Float, pageWidth: Int, headers: List<String>, data: List<List<String>>, paints: Paints): Float {
        val margin = 50f
        val columnWidth = (pageWidth - 2 * margin) / headers.size.toFloat()
        val rowHeight = 25f
        var y = yStart

        // Headers
        canvas.drawRect(margin, y, margin + columnWidth * headers.size, y + rowHeight, paints.borderPaint)
        headers.forEachIndexed { i, h -> canvas.drawText(h, margin + (i * columnWidth) + 5f, y + 17f, paints.headerPaint) }
        y += rowHeight

        // Rows
        data.forEach { row ->
            canvas.drawRect(margin, y, margin + columnWidth * headers.size, y + rowHeight, paints.borderPaint)
            row.forEachIndexed { i, cell -> canvas.drawText(cell, margin + (i * columnWidth) + 5f, y + 17f, paints.cellPaint) }
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

    private fun setupPaints() = Paints(
        titlePaint = Paint().apply { textSize = 18f; isFakeBoldText = true; textAlign = Paint.Align.CENTER },
        headerPaint = Paint().apply { textSize = 12f; isFakeBoldText = true; color = Color.DKGRAY },
        cellPaint = Paint().apply { textSize = 11f; color = Color.BLACK },
        borderPaint = Paint().apply { style = Paint.Style.STROKE; strokeWidth = 1f; color = Color.LTGRAY }
    )

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

private fun getFinalTypeLabel(finalType: Int, group: Int): String {
    if (finalType == 0) return "Desclassificada"
    if (finalType == 7) return "Fora de Tipo"

    return if (group == 2) {
        // No Grupo 2
        "Padrão Básico"
    } else {
        // No Grupo 1
        when (finalType) {
            1 -> "Tipo 1"
            2 -> "Tipo 2"
            else -> "Tipo $finalType"
        }
    }
}