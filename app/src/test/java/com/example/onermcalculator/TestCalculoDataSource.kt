package com.example.onermcalculator

import com.example.onermcalculator.data.local.entity.CalculoEntity
import com.example.onermcalculator.data.repository.CalculoDataSource
import com.example.onermcalculator.domain.model.Calculo1RM
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow

/** Origen controlable para probar ViewModels sin Room. */
class TestCalculoDataSource : CalculoDataSource {
    val historial = MutableStateFlow<List<CalculoEntity>>(emptyList())
    val guardados = mutableListOf<Calculo1RM>()
    var errorAlGuardar: Throwable? = null
    var errorAlCargar: Throwable? = null
    var errorAlEliminar: Throwable? = null
    var puertaGuardado: CompletableDeferred<Unit>? = null
    var puertaEliminacion: CompletableDeferred<Unit>? = null

    override suspend fun guardarCalculo(calculo: Calculo1RM): Long {
        puertaGuardado?.await()
        errorAlGuardar?.let { throw it }
        guardados += calculo
        return guardados.size.toLong()
    }

    override fun obtenerHistorial(): Flow<List<CalculoEntity>> = errorAlCargar?.let { error ->
        flow { throw error }
    } ?: historial

    override suspend fun eliminarCalculo(id: Long): Int {
        puertaEliminacion?.await()
        errorAlEliminar?.let { throw it }
        val anterior = historial.value
        historial.value = anterior.filterNot { it.id == id }
        return anterior.size - historial.value.size
    }

    override suspend fun eliminarHistorial(): Int {
        puertaEliminacion?.await()
        errorAlEliminar?.let { throw it }
        val cantidad = historial.value.size
        historial.value = emptyList()
        return cantidad
    }
}
