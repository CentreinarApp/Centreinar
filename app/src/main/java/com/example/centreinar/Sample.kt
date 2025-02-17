package com.example.centreinar

/*
Esverdeado → Greenish
Partido e quebrado e danificado → Broken, cracked, and damaged
Queimados → Burnt
Ardidos → Sour
Mofados → Moldy
Fermentados → Fermented
Germinados → Germinated
Imaturos (chochos) → Immature (shrivelled)
Danificados por insetos → Insect-damaged
Gessado → starchy
 */
data class Sample(
    val id: Int,
    val weight: Float,
    val humidity: Float,
    val greenish: Float,
    val brokenCrackedDamaged: Float,
    val burnt: Float,
    val sour: Float,
    val moldy: Float,
    val fermented: Float,
    val germinated: Float,
    val immature: Float
)