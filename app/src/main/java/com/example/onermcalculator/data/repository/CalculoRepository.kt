package com.example.onermcalculator.data.repository

import com.example.onermcalculator.data.local.dao.CalculoDao
import com.example.onermcalculator.data.local.entity.CalculoEntity
import com.example.onermcalculator.domain.calculator.Calculadora1RM
import com.example.onermcalculator.domain.model.Calculo1RM
import kotlinx.coroutines.flow.Flow
import kotlin.math.abs
import kotlin.math.max

/**
 * Punto de acceso único al historial local.
 * Valida la consistencia del cálculo antes de delegar la escritura y expone el Flow del DAO.
 */
class CalculoRepository(
    private val dao: CalculoDao,
    private val clock: () -> Long = { System.currentTimeMillis() },
) : CalculoDataSource {
    override suspend fun guardarCalculo(calculo: Calculo1RM): Long = guardarCalculo(
        pesoKg = calculo.pesoKg,
        repeticiones = calculo.repeticiones,
        resultado1RM = calculo.resultado1RM,
    )

    suspend fun guardarCalculo(pesoKg: Double, repeticiones: Int, resultado1RM: Double): Long {
        require(resultado1RM.isFinite() && resultado1RM > 0.0) {
            "El resultado del 1RM debe ser un número finito mayor que cero."
        }

        // La persistencia solo acepta un resultado coherente con la regla de cálculo del dominio.
        val esperado = Calculadora1RM.calcular1RM(pesoKg, repeticiones).resultado1RM
        require(resultadosCoinciden(resultado1RM, esperado)) {
            "El resultado del 1RM no corresponde al peso y las repeticiones indicados."
        }

        return dao.insertar(
            CalculoEntity(
                pesoKg = pesoKg,
                repeticiones = repeticiones,
                resultado1RM = resultado1RM,
                fechaRegistro = clock(),
            ),
        )
    }

    // Room publica mediante Flow cada actualización del historial almacenado.
    override fun obtenerHistorial(): Flow<List<CalculoEntity>> = dao.obtenerHistorial()

    suspend fun obtenerPorId(id: Long): CalculoEntity? = dao.obtenerPorId(id)

    override suspend fun eliminarCalculo(id: Long): Int = dao.eliminarPorId(id)

    override suspend fun eliminarHistorial(): Int = dao.eliminarTodos()

    private fun resultadosCoinciden(real: Double, esperado: Double): Boolean =
        abs(real - esperado) <= TOLERANCIA * max(1.0, abs(esperado))

    private companion object {
        const val TOLERANCIA = 1e-9
    }
}
