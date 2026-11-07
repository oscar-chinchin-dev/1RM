package com.example.onermcalculator.domain.calculator

import com.example.onermcalculator.domain.model.Calculo1RM

/**
 * Centraliza la estimación de 1RM y las cargas de entrenamiento derivadas.
 * La regla de negocio no depende de la interfaz ni de la persistencia.
 */
object Calculadora1RM {
    private const val REPETICIONES_MINIMAS = 1
    private const val REPETICIONES_MAXIMAS = 10
    private val porcentajes = listOf(50, 60, 70, 80, 90, 100)

    fun calcular1RM(pesoKg: Double, repeticiones: Int): Calculo1RM {
        // Se rechazan entradas fuera del dominio antes de aplicar la fórmula.
        require(pesoKg.isFinite() && pesoKg > 0.0) {
            "El peso debe ser un número finito mayor que cero."
        }
        require(repeticiones in REPETICIONES_MINIMAS..REPETICIONES_MAXIMAS) {
            "Las repeticiones deben estar entre 1 y 10."
        }

        val resultado1RM = if (repeticiones == 1) {
            // Una repetición ya representa el máximo levantado; no se estima un valor distinto.
            pesoKg
        } else {
            // Fórmula de Epley: peso × (1 + repeticiones / 30).
            pesoKg * (1.0 + repeticiones / 30.0)
        }

        require(resultado1RM.isFinite()) {
            "El resultado del 1RM debe ser un número finito."
        }

        return Calculo1RM(pesoKg, repeticiones, resultado1RM)
    }

    fun calcularPorcentajes(resultado1RM: Double): Map<Int, Double> {
        require(resultado1RM.isFinite() && resultado1RM > 0.0) {
            "El 1RM debe ser un número finito mayor que cero."
        }

        // Cada porcentaje expresa una carga objetivo a partir del 1RM ya validado.
        return porcentajes.associateWith { porcentaje ->
            val carga = resultado1RM * porcentaje / 100.0
            require(carga.isFinite()) {
                "La carga calculada debe ser un número finito."
            }
            carga
        }
    }
}
