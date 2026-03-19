package com.example.centreinar.domain.model

/**
 * Descreve um campo de limite de tolerância para a UI.
 *
 * Usado tanto no fluxo de Classificação quanto no fluxo de Desconto.
 * Cada Strategy declara quais campos ela precisa via getLimitFields() —
 * a tela renderiza dinamicamente sem saber nada sobre o grão.
 *
 * @param key      Chave do campo — deve ser idêntica às chaves de FieldKeys.*
 * @param label    Label exibido no campo de input
 */
data class LimitField(
    val key: String,
    val label: String
)