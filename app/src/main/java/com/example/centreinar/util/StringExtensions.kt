package com.example.centreinar.util

// Converte a string para Float, aceitando vírgula como separador decimal. Retorna 0f se inválido.
fun String.toFloatOrZero(): Float = replace(",", ".").toFloatOrNull() ?: 0f

// Converte a string para Int. Retorna 0 se inválido.
fun String.toIntOrZero(): Int = toIntOrNull() ?: 0