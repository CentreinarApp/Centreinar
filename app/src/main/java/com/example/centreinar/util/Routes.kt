package com.example.centreinar.util

// Rotas de navegação centralizadas.
object Routes {

    // -------------------------------------------------------------------------
    // Fluxo de Classificação
    // -------------------------------------------------------------------------

    const val HOME                  = "home"
    const val GRAIN_SELECTION       = "grainSelection"
    const val GROUP_SELECTION       = "groupSelection"
    const val OFFICIAL_OR_NOT       = "officialOrNot"
    const val LIMIT_INPUT           = "limitInput"
    const val DISQUALIFICATION      = "disqualification"       // + ?classificationId={classificationId}
    const val CLASSIFICATION_INPUT  = "classification"
    const val CLASSIFICATION_RESULT = "classificationResult"

    // -------------------------------------------------------------------------
    // Fluxo de Descontos
    // -------------------------------------------------------------------------

    const val GRAIN_SELECTION_DISCOUNT  = "grainSelectionDiscount"
    const val GROUP_SELECTION_DISCOUNT  = "groupSelectionDiscount"
    const val OFFICIAL_OR_NOT_DISCOUNT  = "officialOrNotDiscount"
    const val DISCOUNT_LIMIT_INPUT      = "discountLimitInput"
    const val DISCOUNT_INPUT            = "discountInputScreen"  // + ?classificationId={classificationId}
    const val DISCOUNT_RESULTS          = "discountResultsScreen"
}