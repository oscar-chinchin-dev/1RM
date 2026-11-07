package com.example.onermcalculator.data.repository

import com.example.onermcalculator.data.local.dao.CalculoDao
import com.example.onermcalculator.data.local.entity.CalculoEntity
import com.example.onermcalculator.domain.calculator.Calculadora1RM
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class CalculoRepositoryTest {
    @Test fun `RP01 guarda calculo valido con fecha del reloj`() = runBlocking {
        val dao = DaoFalso()
        val repositorio = CalculoRepository(dao) { 1_000L }
        val calculo = Calculadora1RM.calcular1RM(80.0, 5)

        val id = repositorio.guardarCalculo(calculo)

        assertEquals(1L, id)
        assertEquals(CalculoEntity(1, 80.0, 5, calculo.resultado1RM, 1_000L), dao.obtenerPorId(id))
    }

    @Test fun `RP02 rechaza peso no positivo`() {
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { CalculoRepository(DaoFalso()).guardarCalculo(0.0, 5, 1.0) }
        }
        Unit
    }

    @Test fun `RP03 rechaza repeticiones invalidas`() {
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { CalculoRepository(DaoFalso()).guardarCalculo(80.0, 11, 100.0) }
        }
        Unit
    }

    @Test fun `RP04 rechaza resultado no finito`() {
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { CalculoRepository(DaoFalso()).guardarCalculo(80.0, 5, Double.NaN) }
        }
        Unit
    }

    @Test fun `RP05 rechaza resultado inconsistente`() {
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { CalculoRepository(DaoFalso()).guardarCalculo(80.0, 5, 90.0) }
        }
        Unit
    }

    @Test fun `RP06 expone historial del DAO`() = runBlocking {
        val dao = DaoFalso()
        dao.insertar(CalculoEntity(pesoKg = 60.0, repeticiones = 1, resultado1RM = 60.0, fechaRegistro = 1))
        assertEquals(1, CalculoRepository(dao).obtenerHistorial().first().size)
    }

    @Test fun `RP07 delega eliminacion individual`() = runBlocking {
        val dao = DaoFalso()
        dao.insertar(CalculoEntity(pesoKg = 60.0, repeticiones = 1, resultado1RM = 60.0, fechaRegistro = 1))
        val repositorio = CalculoRepository(dao)
        assertEquals(1, repositorio.eliminarCalculo(1))
        assertNull(dao.obtenerPorId(1))
    }

    @Test fun `RP08 delega eliminacion completa`() = runBlocking {
        val dao = DaoFalso()
        dao.insertar(CalculoEntity(pesoKg = 60.0, repeticiones = 1, resultado1RM = 60.0, fechaRegistro = 1))
        assertEquals(1, CalculoRepository(dao).eliminarHistorial())
        assertEquals(emptyList<CalculoEntity>(), dao.obtenerHistorial().first())
    }

    private class DaoFalso : CalculoDao {
        private val registros = mutableListOf<CalculoEntity>()
        private val historial = MutableStateFlow<List<CalculoEntity>>(emptyList())

        override suspend fun insertar(calculo: CalculoEntity): Long {
            val id = if (calculo.id == 0L) (registros.maxOfOrNull { it.id } ?: 0L) + 1 else calculo.id
            registros += calculo.copy(id = id)
            publicar()
            return id
        }

        override fun obtenerHistorial(): Flow<List<CalculoEntity>> = historial

        override suspend fun eliminarPorId(id: Long): Int {
            val eliminado = registros.removeAll { it.id == id }
            if (eliminado) publicar()
            return if (eliminado) 1 else 0
        }

        override suspend fun eliminarTodos(): Int {
            val cantidad = registros.size
            registros.clear()
            publicar()
            return cantidad
        }

        override suspend fun obtenerPorId(id: Long): CalculoEntity? = registros.firstOrNull { it.id == id }

        private fun publicar() {
            historial.value = registros.sortedWith(compareByDescending<CalculoEntity> { it.fechaRegistro }.thenByDescending { it.id })
        }
    }
}
