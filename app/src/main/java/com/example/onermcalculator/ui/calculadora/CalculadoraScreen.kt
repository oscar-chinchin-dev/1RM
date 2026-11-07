package com.example.onermcalculator.ui.calculadora

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.onermcalculator.data.repository.CalculoDataSource
import com.example.onermcalculator.ui.components.TablaPorcentajes
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CalculadoraScreen(repositorio: CalculoDataSource, alVerHistorial: () -> Unit) {
    // La pantalla obtiene el estado observable y delega las acciones al ViewModel.
    val viewModel: CalculadoraViewModel = viewModel(factory = CalculadoraViewModel.factory(repositorio))
    val estado by viewModel.uiState.collectAsState()
    CalculadoraContent(
        estado = estado,
        alCambiarPeso = viewModel::actualizarPeso,
        alCambiarRepeticiones = viewModel::actualizarRepeticiones,
        alCalcular = viewModel::calcular,
        alVerHistorial = alVerHistorial,
    )
}

@Composable
private fun CalculadoraContent(
    estado: CalculadoraUiState,
    alCambiarPeso: (String) -> Unit,
    alCambiarRepeticiones: (String) -> Unit,
    alCalcular: () -> Unit,
    alVerHistorial: () -> Unit,
) {
    // Esta composición presenta entradas, mensajes y resultado sin contener lógica de negocio.
    Column(
        modifier = Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("1RM Calculator", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Estima tu repetición máxima.", style = MaterialTheme.typography.bodyLarge)

        OutlinedTextField(
            value = estado.pesoTexto,
            onValueChange = alCambiarPeso,
            modifier = Modifier.fillMaxWidth().testTag("peso"),
            label = { Text("Peso levantado (kg)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
        )
        OutlinedTextField(
            value = estado.repeticionesTexto,
            onValueChange = alCambiarRepeticiones,
            modifier = Modifier.fillMaxWidth().testTag("repeticiones"),
            label = { Text("Repeticiones realizadas") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
        )
        Button(onClick = alCalcular, enabled = !estado.guardando, modifier = Modifier.fillMaxWidth().testTag("calcular")) {
            Text(if (estado.guardando) "Guardando…" else "Calcular 1RM")
        }
        OutlinedButton(onClick = alVerHistorial, modifier = Modifier.fillMaxWidth().testTag("ver_historial")) { Text("Ver historial") }

        estado.mensajeError?.let { mensaje ->
            Text(mensaje, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }
        estado.mensajeGuardado?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
        estado.errorGuardado?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        estado.resultado1RM?.let { resultado ->
            // El resultado solo se muestra después de una validación y cálculo correctos.
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Tu 1RM estimado", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${formatearKilogramos(resultado)} kg",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "Este resultado es una estimación basada en la fórmula de Epley.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
        if (estado.resultado1RM != null && estado.porcentajes.isNotEmpty()) {
            // Las cargas provienen del mismo 1RM mostrado al usuario.
            TablaPorcentajes(porcentajes = estado.porcentajes)
        }
    }
}

private fun formatearKilogramos(valor: Double): String =
    NumberFormat.getNumberInstance(Locale.forLanguageTag("es-ES")).apply {
        minimumFractionDigits = 1
        maximumFractionDigits = 1
    }.format(valor)
