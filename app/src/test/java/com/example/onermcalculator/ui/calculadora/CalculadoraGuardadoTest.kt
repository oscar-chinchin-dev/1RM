package com.example.onermcalculator.ui.calculadora

import com.example.onermcalculator.MainDispatcherRule
import com.example.onermcalculator.TestCalculoDataSource
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CalculadoraGuardadoTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test fun `G01 calculo valido guarda exactamente un registro`() = runTest {
        val origen = TestCalculoDataSource()
        val viewModel = CalculadoraViewModel(origen).conEntradasValidas()

        viewModel.calcular(); advanceUntilIdle()

        assertEquals(1, origen.guardados.size)
        assertEquals(80.0, origen.guardados.single().pesoKg, 0.0)
        assertEquals(5, origen.guardados.single().repeticiones)
    }

    @Test fun `G02 peso invalido no guarda`() = runTest {
        val origen = TestCalculoDataSource()
        val viewModel = CalculadoraViewModel(origen)
        viewModel.actualizarPeso("0"); viewModel.actualizarRepeticiones("5")

        viewModel.calcular(); advanceUntilIdle()

        assertTrue(origen.guardados.isEmpty())
    }

    @Test fun `G03 repeticiones invalidas no guarda`() = runTest {
        val origen = TestCalculoDataSource()
        val viewModel = CalculadoraViewModel(origen)
        viewModel.actualizarPeso("80"); viewModel.actualizarRepeticiones("11")

        viewModel.calcular(); advanceUntilIdle()

        assertTrue(origen.guardados.isEmpty())
    }

    @Test fun `G04 fallo del repositorio informa error sin confirmacion`() = runTest {
        val origen = TestCalculoDataSource().apply { errorAlGuardar = IllegalStateException() }
        val viewModel = CalculadoraViewModel(origen).conEntradasValidas()

        viewModel.calcular(); advanceUntilIdle()

        assertEquals("No fue posible guardar el cálculo.", viewModel.uiState.value.errorGuardado)
        assertNull(viewModel.uiState.value.mensajeGuardado)
        assertFalse(viewModel.uiState.value.guardando)
    }

    @Test fun `G05 dos pulsaciones durante guardado insertan una vez`() = runTest {
        val puerta = CompletableDeferred<Unit>()
        val origen = TestCalculoDataSource().apply { puertaGuardado = puerta }
        val viewModel = CalculadoraViewModel(origen).conEntradasValidas()

        viewModel.calcular(); viewModel.calcular()
        assertTrue(viewModel.uiState.value.guardando)
        assertTrue(origen.guardados.isEmpty())
        puerta.complete(Unit); advanceUntilIdle()

        assertEquals(1, origen.guardados.size)
    }

    @Test fun `G06 cambiar entradas invalida resultado anterior`() = runTest {
        val origen = TestCalculoDataSource()
        val viewModel = CalculadoraViewModel(origen).conEntradasValidas()

        viewModel.calcular(); advanceUntilIdle(); viewModel.actualizarPeso("81")

        assertNull(viewModel.uiState.value.resultado1RM)
        assertTrue(viewModel.uiState.value.porcentajes.isEmpty())
        assertNull(viewModel.uiState.value.mensajeGuardado)
    }

    @Test fun `G07 navegar sin calcular no guarda`() = runTest {
        val origen = TestCalculoDataSource()
        val viewModel = CalculadoraViewModel(origen)

        // La navegación no invoca calcular; el ViewModel no inicia escrituras por sí mismo.
        advanceUntilIdle()

        assertTrue(origen.guardados.isEmpty())
        assertFalse(viewModel.uiState.value.guardando)
    }

    @Test fun `G08 confirmacion aparece solo tras completar guardado`() = runTest {
        val puerta = CompletableDeferred<Unit>()
        val origen = TestCalculoDataSource().apply { puertaGuardado = puerta }
        val viewModel = CalculadoraViewModel(origen).conEntradasValidas()

        viewModel.calcular()
        assertNull(viewModel.uiState.value.mensajeGuardado)
        assertTrue(viewModel.uiState.value.guardando)
        puerta.complete(Unit); advanceUntilIdle()

        assertEquals("Cálculo guardado en el historial.", viewModel.uiState.value.mensajeGuardado)
    }

    @Test fun `V20 cancelacion no se muestra como error de guardado y libera el estado`() = runTest {
        val origen = TestCalculoDataSource().apply { puertaGuardado = CompletableDeferred() }
        val viewModel = CalculadoraViewModel(origen).conEntradasValidas()

        viewModel.calcular(); runCurrent()
        ViewModel::class.java.declaredMethods
            .single { it.name.startsWith("clear") && it.parameterCount == 0 }
            .apply { isAccessible = true }
            .invoke(viewModel)
        runCurrent()

        assertFalse(viewModel.uiState.value.guardando)
        assertNull(viewModel.uiState.value.errorGuardado)
        assertNull(viewModel.uiState.value.mensajeGuardado)
    }

    private fun CalculadoraViewModel.conEntradasValidas(): CalculadoraViewModel = apply {
        actualizarPeso("80"); actualizarRepeticiones("5")
    }
}
