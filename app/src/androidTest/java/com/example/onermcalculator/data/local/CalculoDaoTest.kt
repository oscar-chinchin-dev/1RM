package com.example.onermcalculator.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.onermcalculator.data.local.dao.CalculoDao
import com.example.onermcalculator.data.local.database.OneRMDatabase
import com.example.onermcalculator.data.local.entity.CalculoEntity
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class CalculoDaoTest {
    private lateinit var database: OneRMDatabase
    private lateinit var dao: CalculoDao

    @Before fun crearBase() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            OneRMDatabase::class.java,
        ).build()
        dao = database.calculoDao()
    }

    @After fun cerrarBase() = database.close()

    @Test fun DB01_baseIniciaVacia() = runBlocking {
        assertEquals(emptyList<CalculoEntity>(), dao.obtenerHistorial().first())
    }

    @Test fun DB02_insertaYGeneraId() = runBlocking {
        assertEquals(1L, dao.insertar(calculo(fecha = 1)))
    }

    @Test fun DB03_obtienePorId() = runBlocking {
        val id = dao.insertar(calculo(fecha = 1))
        assertEquals(calculo(id = id, fecha = 1), dao.obtenerPorId(id))
    }

    @Test fun DB04_insertaVariosRegistros() = runBlocking {
        dao.insertar(calculo(fecha = 1)); dao.insertar(calculo(fecha = 2))
        assertEquals(2, dao.obtenerHistorial().first().size)
    }

    @Test fun DB05_ordenaPorFechaDescendente() = runBlocking {
        dao.insertar(calculo(fecha = 10)); dao.insertar(calculo(peso = 70.0, fecha = 20))
        assertEquals(70.0, dao.obtenerHistorial().first().first().pesoKg, 0.0)
    }

    @Test fun DB06_desempataPorIdDescendente() = runBlocking {
        val primero = dao.insertar(calculo(fecha = 10)); val segundo = dao.insertar(calculo(peso = 70.0, fecha = 10))
        assertEquals(listOf(segundo, primero), dao.obtenerHistorial().first().map { it.id })
    }

    @Test fun DB07_eliminaIdExistente() = runBlocking {
        val id = dao.insertar(calculo(fecha = 1))
        assertEquals(1, dao.eliminarPorId(id)); assertNull(dao.obtenerPorId(id))
    }

    @Test fun DB08_eliminarIdInexistenteDevuelveCero() = runBlocking {
        assertEquals(0, dao.eliminarPorId(999))
    }

    @Test fun DB09_eliminaHistorialCompleto() = runBlocking {
        dao.insertar(calculo(fecha = 1)); dao.insertar(calculo(fecha = 2))
        assertEquals(2, dao.eliminarTodos()); assertEquals(emptyList<CalculoEntity>(), dao.obtenerHistorial().first())
    }

    @Test fun DB10_flowEmiteInsercionYEliminacion() = runBlocking {
        val emisiones = Channel<List<CalculoEntity>>(Channel.UNLIMITED)
        val colector = launch { dao.obtenerHistorial().collect { emisiones.send(it) } }

        try {
            assertEquals(emptyList<CalculoEntity>(), emisiones.receive())

            val id = dao.insertar(calculo(fecha = 1))
            assertEquals(1, emisiones.receive().size)

            dao.eliminarPorId(id)
            assertEquals(emptyList<CalculoEntity>(), emisiones.receive())
        } finally {
            colector.cancel()
            emisiones.close()
        }
    }

    @Test fun UI08_reabrirBaseTemporalEnDiscoConservaRegistros() = runBlocking {
        val contexto = ApplicationProvider.getApplicationContext<android.content.Context>()
        val nombre = "persistencia-ui08.db"
        contexto.deleteDatabase(nombre)
        val primera = Room.databaseBuilder(contexto, OneRMDatabase::class.java, nombre).build()
        primera.calculoDao().insertar(calculo(fecha = 99))
        primera.close()

        val reabierta = Room.databaseBuilder(contexto, OneRMDatabase::class.java, nombre).build()
        try {
            assertEquals(1, reabierta.calculoDao().obtenerHistorial().first().size)
        } finally {
            reabierta.close()
            contexto.deleteDatabase(nombre)
        }
    }

    private fun calculo(id: Long = 0, peso: Double = 60.0, fecha: Long) = CalculoEntity(
        id = id, pesoKg = peso, repeticiones = 1, resultado1RM = peso, fechaRegistro = fecha,
    )
}
