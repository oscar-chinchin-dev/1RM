package com.example.onermcalculator.ui.calculadora

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculadoraViewModelTest {
    @Test fun `UI01 estado inicial vacio`() {
        val estado = CalculadoraViewModel().uiState.value
        assertEquals("", estado.pesoTexto); assertEquals("", estado.repeticionesTexto)
        assertNull(estado.resultado1RM); assertNull(estado.mensajeError)
        assertTrue(estado.porcentajes.isEmpty())
    }

    @Test fun `UI02 calcula 80 kg y 5 repeticiones`() = verificarCalculo("80", "5", 93.33333333333333)
    @Test fun `UI03 una repeticion conserva el peso`() = verificarCalculo("100", "1", 100.0)
    @Test fun `UI04 acepta coma decimal`() = verificarCalculo("80,5", "5", 93.91666666666667)
    @Test fun `UI05 peso vacio muestra error`() = verificarError("", "5", "Ingresa el peso levantado.")
    @Test fun `UI06 peso cero muestra error`() = verificarError("0", "5", "El peso debe ser mayor que cero.")
    @Test fun `UI07 peso negativo muestra error`() = verificarError("-10", "5", "El peso ingresado no es válido.")
    @Test fun `UI08 repeticiones vacias muestra error`() = verificarError("80", "", "Ingresa las repeticiones.")
    @Test fun `UI09 repeticiones cero muestra error`() = verificarError("80", "0", "Las repeticiones deben estar entre 1 y 10.")
    @Test fun `UI10 repeticiones once muestra error`() = verificarError("80", "11", "Las repeticiones deben estar entre 1 y 10.")
    @Test fun `UI11 repeticiones decimales muestra error`() = verificarError("80", "5.5", "Ingresa un número entero válido de repeticiones.")
    @Test fun `UI14 peso no numerico muestra error`() = verificarError("peso", "5", "Ingresa un peso válido.")
    @Test fun `UI15 peso enorme muestra error controlado`() = verificarError("1e309", "5", "Ingresa un peso válido.")

    @Test fun `UI12 cambio de entrada invalida resultado`() {
        val viewModel = CalculadoraViewModel()
        viewModel.actualizarPeso("80"); viewModel.actualizarRepeticiones("5"); viewModel.calcular()
        viewModel.actualizarPeso("81")
        assertNull(viewModel.uiState.value.resultado1RM)
        assertTrue(viewModel.uiState.value.porcentajes.isEmpty())
    }

    @Test fun `UI13 corregir entrada limpia error`() {
        val viewModel = CalculadoraViewModel()
        viewModel.actualizarPeso("0"); viewModel.actualizarRepeticiones("5"); viewModel.calcular()
        viewModel.actualizarPeso("80")
        assertNull(viewModel.uiState.value.mensajeError)
    }

    @Test fun `P02 calcula los seis porcentajes para un 1RM de 100 kg`() {
        val viewModel = CalculadoraViewModel()
        viewModel.actualizarPeso("100"); viewModel.actualizarRepeticiones("1"); viewModel.calcular()

        val porcentajes = viewModel.uiState.value.porcentajes
        assertEquals(listOf(50, 60, 70, 80, 90, 100), porcentajes.keys.toList())
        assertEquals(50.0, porcentajes.getValue(50), 0.0001)
        assertEquals(100.0, porcentajes.getValue(100), 0.0001)
        assertEquals(6, porcentajes.size)
    }

    @Test fun `P03 calcula porcentajes con precision interna`() {
        val viewModel = CalculadoraViewModel()
        viewModel.actualizarPeso("80"); viewModel.actualizarRepeticiones("5"); viewModel.calcular()

        val porcentajes = viewModel.uiState.value.porcentajes
        assertEquals(46.666666666666664, porcentajes.getValue(50), 0.0001)
        assertEquals(93.33333333333333, porcentajes.getValue(100), 0.0001)
    }

    @Test fun `P05 cambiar repeticiones invalida resultado y porcentajes`() {
        val viewModel = CalculadoraViewModel()
        viewModel.actualizarPeso("80"); viewModel.actualizarRepeticiones("5"); viewModel.calcular()
        viewModel.actualizarRepeticiones("6")

        assertNull(viewModel.uiState.value.resultado1RM)
        assertTrue(viewModel.uiState.value.porcentajes.isEmpty())
    }

    @Test fun `P06 calculo invalido no muestra porcentajes`() {
        val viewModel = CalculadoraViewModel()
        viewModel.actualizarPeso("0"); viewModel.actualizarRepeticiones("5"); viewModel.calcular()

        assertTrue(viewModel.uiState.value.porcentajes.isEmpty())
    }

    @Test fun `P07 nuevo calculo reemplaza los porcentajes anteriores`() {
        val viewModel = CalculadoraViewModel()
        viewModel.actualizarPeso("100"); viewModel.actualizarRepeticiones("1"); viewModel.calcular()
        viewModel.actualizarPeso("80"); viewModel.actualizarRepeticiones("5"); viewModel.calcular()

        assertEquals(46.666666666666664, viewModel.uiState.value.porcentajes.getValue(50), 0.0001)
        assertEquals(93.33333333333333, viewModel.uiState.value.resultado1RM!!, 0.0001)
    }

    private fun verificarCalculo(peso: String, repeticiones: String, esperado: Double) {
        val viewModel = CalculadoraViewModel()
        viewModel.actualizarPeso(peso); viewModel.actualizarRepeticiones(repeticiones); viewModel.calcular()
        assertEquals(esperado, viewModel.uiState.value.resultado1RM!!, 0.0001)
        assertNull(viewModel.uiState.value.mensajeError)
    }

    private fun verificarError(peso: String, repeticiones: String, esperado: String) {
        val viewModel = CalculadoraViewModel()
        viewModel.actualizarPeso(peso); viewModel.actualizarRepeticiones(repeticiones); viewModel.calcular()
        assertEquals(esperado, viewModel.uiState.value.mensajeError)
        assertNull(viewModel.uiState.value.resultado1RM)
    }
}
