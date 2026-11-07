package com.example.onermcalculator.ui.historial

import com.example.onermcalculator.MainDispatcherRule
import com.example.onermcalculator.TestCalculoDataSource
import com.example.onermcalculator.data.local.entity.CalculoEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HistorialViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test fun `H01 historial vacio publica estado vacio`() = runTest {
        val viewModel = HistorialViewModel(TestCalculoDataSource())
        val colector = backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.registros.isEmpty())
        assertFalse(viewModel.uiState.value.cargando)
        colector.cancel()
    }

    @Test fun `H02 historial con registros publica datos correctos`() = runTest {
        val origen = TestCalculoDataSource().apply { historial.value = listOf(calculo(2), calculo(1)) }
        val viewModel = HistorialViewModel(origen)
        val colector = backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()
        assertEquals(listOf(2L, 1L), viewModel.uiState.value.registros.map { it.id })
        assertNull(viewModel.uiState.value.error)
        colector.cancel()
    }

    @Test fun `H03 insercion actualiza lista`() = runTest {
        val origen = TestCalculoDataSource()
        val viewModel = HistorialViewModel(origen)
        val colector = backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()
        origen.historial.value = listOf(calculo(1)); advanceUntilIdle()
        assertEquals(listOf(1L), viewModel.uiState.value.registros.map { it.id })
        colector.cancel()
    }

    @Test fun `H04 eliminar registro lo quita de la lista`() = runTest {
        val origen = TestCalculoDataSource().apply { historial.value = listOf(calculo(1), calculo(2)) }
        val viewModel = HistorialViewModel(origen)
        val colector = backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle(); var exito: Boolean? = null
        viewModel.eliminar(1) { exito = it }; advanceUntilIdle()
        assertEquals(true, exito); assertEquals(listOf(2L), viewModel.uiState.value.registros.map { it.id })
        colector.cancel()
    }

    @Test fun `H05 cancelar no invoca eliminacion`() = runTest {
        val origen = TestCalculoDataSource().apply { historial.value = listOf(calculo(1)) }
        val viewModel = HistorialViewModel(origen)
        val colector = backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()
        // Cancelar es una acción del diálogo: al no llamar eliminar, el origen queda intacto.
        assertEquals(listOf(1L), viewModel.uiState.value.registros.map { it.id })
        colector.cancel()
    }

    @Test fun `H06 eliminar todo deja lista vacia`() = runTest {
        val origen = TestCalculoDataSource().apply { historial.value = listOf(calculo(1), calculo(2)) }
        val viewModel = HistorialViewModel(origen)
        val colector = backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle(); viewModel.eliminarTodo { }; advanceUntilIdle()
        assertTrue(viewModel.uiState.value.registros.isEmpty())
        colector.cancel()
    }

    @Test fun `H07 fallo de eliminacion informa error y conserva datos`() = runTest {
        val origen = TestCalculoDataSource().apply {
            historial.value = listOf(calculo(1)); errorAlEliminar = IllegalStateException()
        }
        val viewModel = HistorialViewModel(origen)
        val colector = backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle(); var exito: Boolean? = null
        viewModel.eliminar(1) { exito = it }; advanceUntilIdle()
        assertEquals(false, exito)
        assertEquals("No fue posible eliminar el cálculo.", viewModel.uiState.value.error)
        assertEquals(listOf(1L), viewModel.uiState.value.registros.map { it.id })
        colector.cancel()
    }

    @Test fun `H08 conserva orden descendente entregado por el origen`() = runTest {
        val origen = TestCalculoDataSource().apply { historial.value = listOf(calculo(3, 30), calculo(2, 20), calculo(1, 20)) }
        val viewModel = HistorialViewModel(origen)
        val colector = backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()
        assertEquals(listOf(3L, 2L, 1L), viewModel.uiState.value.registros.map { it.id })
        colector.cancel()
    }

    @Test fun `V18 eliminar un id inexistente informa error y no confirma exito`() = runTest {
        val origen = TestCalculoDataSource().apply { historial.value = listOf(calculo(1)) }
        val viewModel = HistorialViewModel(origen)
        val colector = backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle(); var exito: Boolean? = null

        viewModel.eliminar(99) { exito = it }; advanceUntilIdle()

        assertEquals(false, exito)
        assertEquals("No fue posible eliminar el cálculo.", viewModel.uiState.value.error)
        assertEquals(listOf(1L), viewModel.uiState.value.registros.map { it.id })
        colector.cancel()
    }

    @Test fun `V17 fallo al cargar es distinguible de un historial vacio`() = runTest {
        val origen = TestCalculoDataSource().apply { errorAlCargar = IllegalStateException() }
        val viewModel = HistorialViewModel(origen)
        val colector = backgroundScope.launch { viewModel.uiState.collect {} }

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.registros.isEmpty())
        assertEquals("No fue posible cargar el historial.", viewModel.uiState.value.error)
        colector.cancel()
    }

    @Test fun `V19 fallo al eliminar todo conserva datos e informa error`() = runTest {
        val origen = TestCalculoDataSource().apply {
            historial.value = listOf(calculo(1))
            errorAlEliminar = IllegalStateException()
        }
        val viewModel = HistorialViewModel(origen)
        val colector = backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle(); var exito: Boolean? = null

        viewModel.eliminarTodo { exito = it }; advanceUntilIdle()

        assertEquals(false, exito)
        assertEquals("No fue posible eliminar el historial.", viewModel.uiState.value.error)
        assertEquals(listOf(1L), viewModel.uiState.value.registros.map { it.id })
        colector.cancel()
    }

    private fun calculo(id: Long, fecha: Long = id) = CalculoEntity(id, 80.0, 5, 93.3333, fecha)
}
