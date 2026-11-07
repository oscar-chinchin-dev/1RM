package com.example.onermcalculator.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.onermcalculator.data.local.entity.CalculoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CalculoDao {
    /** Inserta un cálculo y devuelve el identificador generado por Room. */
    @Insert
    suspend fun insertar(calculo: CalculoEntity): Long

    @Query("SELECT * FROM calculos_1rm ORDER BY fechaRegistro DESC, id DESC")
    /** Expone cambios del historial para que la interfaz se actualice de forma reactiva. */
    fun obtenerHistorial(): Flow<List<CalculoEntity>>

    @Query("DELETE FROM calculos_1rm WHERE id = :id")
    /** Elimina un registro concreto e informa cuántas filas fueron afectadas. */
    suspend fun eliminarPorId(id: Long): Int

    @Query("DELETE FROM calculos_1rm")
    /** Elimina todos los registros almacenados en el historial. */
    suspend fun eliminarTodos(): Int

    @Query("SELECT * FROM calculos_1rm WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Long): CalculoEntity?
}
