package com.example.centreinar.ui.classificationProcess.strategy

data class ClassificationRow(
    val label: String,
    val percentage: Float,
    val typeCode: Int,
    val limit: Float = 0f,
    val shouldHighlight: Boolean = true
)

// Contrato para os resultados numéricos da classificação
interface BaseClassification {
    val id: Int
    val moisturePercentage: Float
    val finalType: Int
}

// Contrato para os fatores de desclassificação
interface BaseDisqualification {
    val badConservation: Int
    val strangeSmell: Int
    val insects: Int
    val toxicGrains: Int
}

// Contrato para os limites de tolerância
interface BaseLimit {
    val moistureUpLim: Float
    val group: Int
}