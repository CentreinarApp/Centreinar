package com.example.centreinar.ui.classificationProcess.strategy

data class ClassificationInputState(
    val group: Int = 1,

    // Informação básica (Tab 0)
    val lotWeight: Float = 0f,
    val sampleWeight: Float = 0f,
    val moisture: Float = 0f,
    val foreignMatters: Float = 0f,    // Matéria estranha e impurezas (g)
    val cleanSampleWeight: Float = 0f, // Calculado: sampleWeight - foreignMatters

    // Avariados (Tab 1)
    val sour: Float = 0f,              // Ardidos (Soja) / Ardidos (Milho)
    val burnt: Float = 0f,             // Queimados — exclusivo Soja
    val moldy: Float = 0f,
    val fermented: Float = 0f,
    val germinated: Float = 0f,
    val immature: Float = 0f,          // "Imaturos" Soja / "Chochos e Imaturos" Milho
    val shriveled: Float = 0f,         // Chochos — exclusivo Soja
    val damaged: Float = 0f,           // Total danificados — exclusivo Soja
    val gessado: Float = 0f,           // Gessados — exclusivo Milho

    // Defeitos finais (Tab 2)
    val brokenCrackedDamaged: Float = 0f, // PQA Soja / Quebrados Milho (Tab 0)
    val greenish: Float = 0f,          // Esverdeados — exclusivo Soja
    val carunchado: Float = 0f,        // Carunchados — exclusivo Milho

    // Classe de cor Soja (Tab 2, opcional)
    val isColorDefined: Boolean = false,
    val otherColorsWeight: Float = 0f,
    val baseWeightForColor: Float = 0f, // peso base para cálculo de cor

    // Classe e Grupo do Milho (Tab 2, opcionais)
    val shouldDefineClass: Boolean = false,
    val weightYellow: Float = 0f,
    val weightWhite: Float = 0f,
    val weightMixedColors: Float = 0f,

    val shouldDefineGroup: Boolean = false,
    val weightHard: Float = 0f,
    val weightDent: Float = 0f,
    val weightSemiHard: Float = 0f
)