package com.example.centreinar.util

/**
 * Chaves canônicas compartilhadas entre os fluxos de Classificação e Desconto.
 *
 * Regra: toda vez que um Map<String, Float> transitar entre Strategy, ViewModel
 * ou tela carregando dados de defeitos ou limites, use estas constantes como chave.
 */
object FieldKeys {

    // Informações básicas da amostra
    const val MOISTURE    = "moisture"    // Umidade (%)
    const val IMPURITIES  = "impurities"  // Matéria Estranha e Impurezas (%)
    const val BROKEN      = "broken"      // Quebrados / Partidos / Amassados (%)

    // Defeitos específicos da Soja
    const val BURNT          = "burnt"       // Queimados (%)
    const val BURNT_OR_SOUR  = "burntOrSour" // Ardidos + Queimados (%)
    const val MOLDY          = "moldy"       // Mofados (%)
    const val SPOILED        = "spoiled"     // Total de Avariados (%)
    const val GREENISH       = "greenish"    // Esverdeados (%)

    // Defeitos específicos do Milho
    const val ARDIDO     = "ardido"     // Ardidos (%) — exclusivo Milho
    const val CARUNCHADO = "carunchado" // Carunchados (%) — exclusivo Milho

    // Defeitos comuns (presentes nos dois grãos, sem peso no tipo final)
    const val FERMENTED  = "fermented"  // Fermentados (%)
    const val GERMINATED = "germinated" // Germinados (%)
    const val IMMATURE   = "immature"   // Imaturos / Chochos e Imaturos (%)
    const val SHRIVELED  = "shriveled"  // Chochos (%) — exclusivo Soja
    const val DAMAGED    = "damaged"    // Total de Danificados (%) — exclusivo Soja
    const val GESSADO    = "gessado"    // Gessados (%) — exclusivo Milho

    // Campos financeiros — usados pelo fluxo de Desconto
    const val LOT_WEIGHT = "lotWeight" // Peso do lote (kg)
    const val LOT_PRICE  = "lotPrice"  // Valor total do lote (R$)
}