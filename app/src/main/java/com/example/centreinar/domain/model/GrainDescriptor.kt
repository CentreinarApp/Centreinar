package com.example.centreinar.domain.model

// Descreve um grão para a UI — metadados de apresentação e estrutura visual.
data class GrainDescriptor(
    val name: String,               // chave do @StringKey no Hilt, ex: "Soja"
    val displayName: String,        // label exibido nos botões, ex: "Soja"
    val colorScheme: String,        // "primary", "secondary" ou "tertiary"
    val supportsGroups: Boolean,    // true = passa por GroupSelection; false = group fixo 1
    val supportsColorClass: Boolean = false, // true = exibe seção de classe de cor na ClassificationInputScreen
    val supportsCarunchado: Boolean = false  // true = exibe campos e seções de Milho na ClassificationInputScreen
)