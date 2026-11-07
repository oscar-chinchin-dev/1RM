package com.example.onermcalculator.ui.historial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.onermcalculator.data.repository.CalculoDataSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Observa el historial persistente y coordina sus operaciones de eliminación. */
class HistorialViewModel(private val repositorio: CalculoDataSource) : ViewModel() {
    private val operacionEnCurso = MutableStateFlow(false)
    private val errorOperacion = MutableStateFlow<String?>(null)
    val uiState: StateFlow<HistorialUiState> = repositorio.obtenerHistorial()
        // Cada emisión de Room se convierte en el estado que consume la pantalla.
        .map { HistorialUiState(registros = it, cargando = false) }
        .catch { emit(HistorialUiState(cargando = false, error = "No fue posible cargar el historial.")) }
        .combine(operacionEnCurso) { estado, procesando -> estado.copy(operacionEnCurso = procesando) }
        .combine(errorOperacion) { estado, error -> estado.copy(error = error ?: estado.error) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistorialUiState())

    fun eliminar(id: Long, alFinalizar: (Boolean) -> Unit) {
        // Solo se permite una eliminación a la vez para evitar operaciones solapadas.
        if (operacionEnCurso.value) return
        ejecutar("No fue posible eliminar el cálculo.", alFinalizar, { filasAfectadas -> filasAfectadas > 0 }) {
            repositorio.eliminarCalculo(id)
        }
    }

    fun eliminarTodo(alFinalizar: (Boolean) -> Unit) {
        if (operacionEnCurso.value) return
        ejecutar("No fue posible eliminar el historial.", alFinalizar, { true }) { repositorio.eliminarHistorial() }
    }

    private fun ejecutar(
        mensajeError: String,
        alFinalizar: (Boolean) -> Unit,
        fueExitosa: (Int) -> Boolean,
        bloque: suspend () -> Int,
    ) {
        // Se marca antes de lanzar la coroutine para que dos pulsaciones consecutivas no encolen borrados.
        operacionEnCurso.value = true
        viewModelScope.launch {
            // El Flow conserva los registros visibles mientras se elimina.
            errorOperacion.value = null
            try {
                if (fueExitosa(bloque())) {
                    alFinalizar(true)
                } else {
                    errorOperacion.value = mensajeError
                    alFinalizar(false)
                }
            } catch (exception: CancellationException) {
                // La cancelación se propaga para respetar el ciclo de vida del ViewModel.
                throw exception
            } catch (_: Exception) {
                errorOperacion.value = mensajeError
                alFinalizar(false)
            } finally {
                operacionEnCurso.value = false
            }
        }
    }

    companion object {
        fun factory(repositorio: CalculoDataSource): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                check(modelClass.isAssignableFrom(HistorialViewModel::class.java))
                @Suppress("UNCHECKED_CAST") return HistorialViewModel(repositorio) as T
            }
        }
    }
}
