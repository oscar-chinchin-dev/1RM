package com.example.onermcalculator.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Representa una fila de historial persistida por Room para una estimación de repetición máxima. */
@Entity(tableName = "calculos_1rm")
data class CalculoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val pesoKg: Double,
    val repeticiones: Int,
    val resultado1RM: Double,
    val fechaRegistro: Long,
)
