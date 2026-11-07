package com.example.onermcalculator

import com.example.onermcalculator.data.local.entity.CalculoEntity
import com.example.onermcalculator.data.repository.CalculoDataSource
import com.example.onermcalculator.domain.model.Calculo1RM
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** Origen aislado para pruebas Compose; no abre la base de datos de la aplicación. */
class TestCalculoDataSource : CalculoDataSource {
    val historial = MutableStateFlow<List<CalculoEntity>>(emptyList())
    val guardados = mutableListOf<Calculo1RM>()

    override suspend fun guardarCalculo(calculo: Calculo1RM): Long {
        guardados += calculo
        val id = (historial.value.maxOfOrNull { it.id } ?: 0) + 1
        historial.value = listOf(
            CalculoEntity(id, calculo.pesoKg, calculo.repeticiones, calculo.resultado1RM, System.currentTimeMillis()),
        ) + historial.value
        return id
    }

    override fun obtenerHistorial(): Flow<List<CalculoEntity>> = historial

    override suspend fun eliminarCalculo(id: Long): Int {
        val antes = historial.value
        historial.value = antes.filterNot { it.id == id }
        return antes.size - historial.value.size
    }

    override suspend fun eliminarHistorial(): Int {
        val cantidad = historial.value.size
        historial.value = emptyList()
        return cantidad
    }
}
