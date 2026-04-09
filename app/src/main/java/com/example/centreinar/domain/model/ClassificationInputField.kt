package com.example.centreinar.domain.model

/**
 * Declara um campo de input para a tela de classificação.
 *
 * A Strategy retorna a lista completa via getClassificationInputFields(),
 * incluindo a aba onde o campo aparece (tabGroup) e seu tipo (fieldType)
 * para campos que precisam de tratamento especial na UI.
 *
 * tabGroup:
 *   0 = Aba "Informação Básica" (extra, após os campos fixos lotWeight/sampleWeight/moisture/impurities)
 *   1 = Aba "Avariados"
 *   2 = Aba "Defeitos Finais"
 *
 * fieldType:
 *   NUMERIC        = campo numérico simples (padrão)
 *   CALCULATED     = campo calculado/somente leitura
 *   COLOR_CLASS    = seção de classe de cor (Soja) — renderização especial
 *   MILHO_CLASSE   = seção de classe do milho — renderização especial
 *   MILHO_GRUPO    = seção de grupo do milho — renderização especial
 */
data class ClassificationInputField(
    val key: String,
    val label: String,
    val tabGroup: Int = 1,
    val fieldType: FieldType = FieldType.NUMERIC
) {
    enum class FieldType {
        NUMERIC,
        CALCULATED,
        COLOR_CLASS,
        MILHO_CLASSE,
        MILHO_GRUPO
    }
}