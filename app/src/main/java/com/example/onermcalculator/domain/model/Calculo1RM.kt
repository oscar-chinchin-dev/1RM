package com.example.onermcalculator.domain.model

/** Resultado sin redondear de una estimación de repetición máxima. */
data class Calculo1RM(
    val pesoKg: Double,
    val repeticiones: Int,
    val resultado1RM: Double,
)
