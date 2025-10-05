package com.example.centreinar.utils

import android.graphics.Paint
import android.graphics.Typeface

class PdfPaints {

    // Título
    val titlePaint = Paint().apply {
        color = android.graphics.Color.BLACK
        textSize = 20f
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
    }

    // Texto normal (usado nas linhas de dados)
    val cellPaint = Paint().apply {
        color = android.graphics.Color.BLACK
        textSize = 12f
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
    }

    // Rodapé ou observações
    val footerPaint = Paint().apply {
        color = android.graphics.Color.DKGRAY
        textSize = 10f
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.ITALIC)
    }
}
