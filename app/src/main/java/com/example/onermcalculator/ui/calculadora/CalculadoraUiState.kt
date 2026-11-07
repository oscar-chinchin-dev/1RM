package com.example.onermcalculator.ui.calculadora

/** Estado observable e inmutable de la pantalla de cálculo. */
data class CalculadoraUiState(
    val pesoTexto: String = "",
    val repeticionesTexto: String = "",
    val resultado1RM: Double? = null,
    val porcentajes: Map<Int, Double> = emptyMap(),
    val mensajeError: String? = null,
    val guardando: Boolean = false,
    val mensajeGuardado: String? = null,
    val errorGuardado: String? = null,
)
