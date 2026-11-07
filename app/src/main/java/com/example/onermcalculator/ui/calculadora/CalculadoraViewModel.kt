package com.example.onermcalculator.ui.calculadora

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.onermcalculator.data.repository.CalculoDataSource
import com.example.onermcalculator.domain.calculator.Calculadora1RM
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/** Coordina las entradas, el cálculo del 1RM y el guardado de resultados válidos. */
class CalculadoraViewModel(private val repositorio: CalculoDataSource? = null) : ViewModel() {
    private val _uiState = MutableStateFlow(CalculadoraUiState())
    val uiState: StateFlow<CalculadoraUiState> = _uiState.asStateFlow()

    fun actualizarPeso(texto: String) = actualizarEntrada { copy(pesoTexto = texto) }

    fun actualizarRepeticiones(texto: String) = actualizarEntrada { copy(repeticionesTexto = texto) }

    fun calcular() {
        // Impide que una segunda pulsación inicie otro guardado mientras el primero sigue activo.
        if (_uiState.value.guardando) return
        val estado = _uiState.value
        val peso = estado.pesoTexto.trim().replace(',', '.').toDoubleOrNull()
        val error = validarPeso(estado.pesoTexto, peso)
            ?: validarRepeticiones(estado.repeticionesTexto)

        if (error != null) {
            // Las entradas inválidas no producen resultados ni intentos de persistencia.
            _uiState.update { it.copy(resultado1RM = null, porcentajes = emptyMap(), mensajeError = error, mensajeGuardado = null, errorGuardado = null) }
            return
        }

        val repeticiones = estado.repeticionesTexto.trim().toInt()
        try {
            val calculo = Calculadora1RM.calcular1RM(peso!!, repeticiones)
            val porcentajes = Calculadora1RM.calcularPorcentajes(calculo.resultado1RM)
            _uiState.update {
                it.copy(resultado1RM = calculo.resultado1RM, porcentajes = porcentajes, mensajeError = null, mensajeGuardado = null, errorGuardado = null)
            }
            // La instancia sin repositorio se conserva para las pruebas unitarias aprobadas de la etapa 05.
            val origen = repositorio ?: return
            // El estado se actualiza antes de lanzar la coroutine para prevenir guardados duplicados.
            _uiState.update { it.copy(guardando = true) }
            viewModelScope.launch {
                try {
                    origen.guardarCalculo(calculo)
                    _uiState.update { actual ->
                        if (actual.pesoTexto == estado.pesoTexto && actual.repeticionesTexto == estado.repeticionesTexto)
                            actual.copy(mensajeGuardado = "Cálculo guardado en el historial.", errorGuardado = null)
                        else actual
                    }
                } catch (exception: CancellationException) {
                    // La cancelación pertenece al ciclo de vida del ViewModel y no es un error de usuario.
                    throw exception
                } catch (_: Exception) {
                    _uiState.update { it.copy(errorGuardado = "No fue posible guardar el cálculo.", mensajeGuardado = null) }
                } finally {
                    _uiState.update { it.copy(guardando = false) }
                }
            }
        } catch (exception: IllegalArgumentException) {
            _uiState.update {
                it.copy(resultado1RM = null, porcentajes = emptyMap(), mensajeError = "No fue posible calcular un 1RM válido.")
            }
        }
    }

    private fun actualizarEntrada(cambio: CalculadoraUiState.() -> CalculadoraUiState) {
        // Cualquier edición invalida el resultado calculado con las entradas anteriores.
        _uiState.update {
            it.cambio().copy(resultado1RM = null, porcentajes = emptyMap(), mensajeError = null, mensajeGuardado = null, errorGuardado = null)
        }
    }

    private fun validarPeso(texto: String, peso: Double?): String? = when {
        texto.trim().isEmpty() -> "Ingresa el peso levantado."
        peso == null || !peso.isFinite() -> "Ingresa un peso válido."
        peso == 0.0 -> "El peso debe ser mayor que cero."
        peso < 0.0 -> "El peso ingresado no es válido."
        else -> null
    }

    private fun validarRepeticiones(texto: String): String? = when {
        texto.trim().isEmpty() -> "Ingresa las repeticiones."
        !texto.trim().matches(Regex("[0-9]+")) -> "Ingresa un número entero válido de repeticiones."
        else -> {
            val repeticiones = texto.trim().toIntOrNull()
                ?: return "Ingresa un número entero válido de repeticiones."
            if (repeticiones !in 1..10) "Las repeticiones deben estar entre 1 y 10." else null
        }
    }

    companion object {
        fun factory(repositorio: CalculoDataSource): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                check(modelClass.isAssignableFrom(CalculadoraViewModel::class.java))
                @Suppress("UNCHECKED_CAST") return CalculadoraViewModel(repositorio) as T
            }
        }
    }
}
