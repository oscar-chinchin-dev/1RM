package com.example.onermcalculator.domain.calculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class Calculadora1RMTest {
    @Test fun `T01 calcula Epley para 80 kg y 5 repeticiones`() {
        val calculo = Calculadora1RM.calcular1RM(80.0, 5)
        assertEquals(80.0, calculo.pesoKg, TOLERANCIA)
        assertEquals(5, calculo.repeticiones)
        assertEquals(93.33333333333333, calculo.resultado1RM, TOLERANCIA)
    }

    @Test fun `T02 una repeticion devuelve el peso original`() =
        assertEquals(100.0, Calculadora1RM.calcular1RM(100.0, 1).resultado1RM, TOLERANCIA)

    @Test fun `T03 calcula Epley para 60 kg y 10 repeticiones`() =
        assertEquals(80.0, Calculadora1RM.calcular1RM(60.0, 10).resultado1RM, TOLERANCIA)

    @Test fun `T04 rechaza peso cero`() = assertArgumentoInvalido { Calculadora1RM.calcular1RM(0.0, 5) }
    @Test fun `T05 rechaza peso negativo`() = assertArgumentoInvalido { Calculadora1RM.calcular1RM(-20.0, 5) }
    @Test fun `T06 rechaza cero repeticiones`() = assertArgumentoInvalido { Calculadora1RM.calcular1RM(80.0, 0) }
    @Test fun `T07 rechaza once repeticiones`() = assertArgumentoInvalido { Calculadora1RM.calcular1RM(80.0, 11) }

    @Test fun `T08 conserva peso para una repeticion`() =
        assertEquals(80.0, Calculadora1RM.calcular1RM(80.0, 1).resultado1RM, TOLERANCIA)

    @Test fun `T09 calcula los seis porcentajes`() {
        val porcentajes = Calculadora1RM.calcularPorcentajes(100.0)
        assertEquals(mapOf(50 to 50.0, 60 to 60.0, 70 to 70.0, 80 to 80.0, 90 to 90.0, 100 to 100.0), porcentajes)
    }

    @Test fun `T10 rechaza 1RM cero`() = assertArgumentoInvalido { Calculadora1RM.calcularPorcentajes(0.0) }
    @Test fun `T11 rechaza peso NaN`() = assertArgumentoInvalido { Calculadora1RM.calcular1RM(Double.NaN, 5) }
    @Test fun `T12 rechaza peso infinito positivo`() = assertArgumentoInvalido { Calculadora1RM.calcular1RM(Double.POSITIVE_INFINITY, 5) }
    @Test fun `T13 rechaza 1RM NaN`() = assertArgumentoInvalido { Calculadora1RM.calcularPorcentajes(Double.NaN) }
    @Test fun `T14 rechaza 1RM infinito negativo`() = assertArgumentoInvalido { Calculadora1RM.calcularPorcentajes(Double.NEGATIVE_INFINITY) }
    @Test fun `T15 rechaza resultado desbordado`() = assertArgumentoInvalido { Calculadora1RM.calcular1RM(Double.MAX_VALUE, 10) }

    private fun assertArgumentoInvalido(bloque: () -> Unit) {
        assertThrows(IllegalArgumentException::class.java, bloque)
    }

    private companion object {
        const val TOLERANCIA = 0.0001
    }
}
