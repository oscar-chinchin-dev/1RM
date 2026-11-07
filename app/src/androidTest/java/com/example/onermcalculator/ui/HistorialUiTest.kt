package com.example.onermcalculator.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.onermcalculator.TestCalculoDataSource
import com.example.onermcalculator.data.local.entity.CalculoEntity
import com.example.onermcalculator.ui.calculadora.CalculadoraScreen
import com.example.onermcalculator.ui.historial.HistorialScreen
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class HistorialUiTest {
    @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test fun UI01_abrirHistorial_muestraPantalla() {
        val origen = TestCalculoDataSource()
        composeRule.setContent {
            var historial by remember { mutableStateOf(false) }
            if (historial) HistorialScreen(origen) { historial = false }
            else CalculadoraScreen(origen) { historial = true }
        }
        composeRule.onNodeWithTag("ver_historial").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Historial de cálculos").assertExists()
    }

    @Test fun UI02_historialVacio_muestraMensaje() {
        composeRule.setContent { HistorialScreen(TestCalculoDataSource()) {} }
        composeRule.onNodeWithText("Todavía no tienes cálculos guardados.").assertExists()
    }

    @Test fun UI03_calcularYGuardar_permiteVerRegistro() {
        val origen = TestCalculoDataSource()
        composeRule.setContent {
            var historial by remember { mutableStateOf(false) }
            if (historial) HistorialScreen(origen) { historial = false }
            else CalculadoraScreen(origen) { historial = true }
        }
        composeRule.onNodeWithTag("peso").performTextInput("80")
        composeRule.onNodeWithTag("repeticiones").performTextInput("5")
        composeRule.onNodeWithTag("calcular").performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) { origen.guardados.size == 1 }
        composeRule.onNodeWithTag("ver_historial").performClick()
        esperarNodo("eliminar_1")
        composeRule.onNodeWithText("1RM estimado:").assertExists()
    }

    @Test fun UI04_cancelarEliminacion_conservaRegistro() {
        val origen = TestCalculoDataSource().apply { historial.value = listOf(calculo()) }
        composeRule.setContent { HistorialScreen(origen) {} }
        esperarNodo("eliminar_1")
        composeRule.onNodeWithTag("eliminar_1").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Cancelar").performClick()
        composeRule.onNodeWithText("1RM estimado:").assertExists()
        assertEquals(1, origen.historial.value.size)
    }

    @Test fun UI05_confirmarEliminacion_quitaRegistro() {
        val origen = TestCalculoDataSource().apply { historial.value = listOf(calculo()) }
        composeRule.setContent { HistorialScreen(origen) {} }
        esperarNodo("eliminar_1")
        composeRule.onNodeWithTag("eliminar_1").performClick()
        esperarNodo("confirmar_eliminar")
        composeRule.onNodeWithTag("confirmar_eliminar").performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) { origen.historial.value.isEmpty() }
        composeRule.onNodeWithText("Todavía no tienes cálculos guardados.").assertExists()
    }

    @Test fun UI06_eliminarTodo_dejaHistorialVacio() {
        val origen = TestCalculoDataSource().apply { historial.value = listOf(calculo(1), calculo(2)) }
        composeRule.setContent { HistorialScreen(origen) {} }
        esperarNodo("eliminar_historial")
        composeRule.onNodeWithTag("eliminar_historial").performClick()
        composeRule.waitForIdle()
        esperarNodo("confirmar_eliminar_todo")
        composeRule.onNodeWithTag("confirmar_eliminar_todo").performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) { origen.historial.value.isEmpty() }
        composeRule.onNodeWithText("Todavía no tienes cálculos guardados.").assertExists()
    }

    @Test fun UI07_volverACalculadora_restauraPantalla() {
        composeRule.setContent {
            var historial by remember { mutableStateOf(true) }
            if (historial) HistorialScreen(TestCalculoDataSource()) { historial = false }
            else CalculadoraScreen(TestCalculoDataSource()) { historial = true }
        }
        composeRule.onNodeWithText("Volver a la calculadora").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("1RM Calculator").assertExists()
    }

    private fun calculo(id: Long = 1) = CalculoEntity(id, 80.0, 5, 93.3333, id)

    private fun esperarNodo(tag: String) {
        composeRule.waitUntil(timeoutMillis = 5_000) {
            try {
                composeRule.onNodeWithTag(tag).assertExists()
                true
            } catch (_: AssertionError) {
                false
            }
        }
    }
}
