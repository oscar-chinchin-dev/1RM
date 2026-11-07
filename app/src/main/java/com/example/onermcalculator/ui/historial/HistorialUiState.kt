package com.example.onermcalculator.ui.historial

import com.example.onermcalculator.data.local.entity.CalculoEntity

data class HistorialUiState(
    val registros: List<CalculoEntity> = emptyList(),
    val cargando: Boolean = true,
    val operacionEnCurso: Boolean = false,
    val error: String? = null,
)
