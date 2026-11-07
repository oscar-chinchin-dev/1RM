package com.example.onermcalculator.data.repository

import com.example.onermcalculator.data.local.entity.CalculoEntity
import com.example.onermcalculator.domain.model.Calculo1RM
import kotlinx.coroutines.flow.Flow

/** Contrato que permite a las pantallas trabajar con Room o con un origen de prueba. */
interface CalculoDataSource {
    suspend fun guardarCalculo(calculo: Calculo1RM): Long
    fun obtenerHistorial(): Flow<List<CalculoEntity>>
    suspend fun eliminarCalculo(id: Long): Int
    suspend fun eliminarHistorial(): Int
}
