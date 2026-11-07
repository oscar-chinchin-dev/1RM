package com.example.onermcalculator.ui.historial

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.onermcalculator.data.local.entity.CalculoEntity
import com.example.onermcalculator.data.repository.CalculoDataSource
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistorialScreen(repositorio: CalculoDataSource, alVolver: () -> Unit) {
    // La pantalla observa el historial y mantiene únicamente el estado local de los diálogos.
    val viewModel: HistorialViewModel = viewModel(factory = HistorialViewModel.factory(repositorio))
    val estado by viewModel.uiState.collectAsState()
    var seleccionado by remember { mutableStateOf<CalculoEntity?>(null) }
    var confirmarTodo by remember { mutableStateOf(false) }

    HistorialContent(estado, alVolver, { seleccionado = it }, { confirmarTodo = true })
    seleccionado?.let { calculo ->
        // La eliminación individual requiere confirmación explícita del usuario.
        AlertDialog(
            onDismissRequest = { if (!estado.operacionEnCurso) seleccionado = null },
            title = { Text("¿Eliminar este cálculo?") },
            text = { Text("Esta acción quitará el registro del historial y no se puede deshacer.") },
            confirmButton = { Button(enabled = !estado.operacionEnCurso, modifier = Modifier.testTag("confirmar_eliminar"), onClick = { viewModel.eliminar(calculo.id) { if (it) seleccionado = null } }) { Text("Eliminar") } },
            dismissButton = { OutlinedButton(enabled = !estado.operacionEnCurso, modifier = Modifier.testTag("cancelar_eliminar"), onClick = { seleccionado = null }) { Text("Cancelar") } },
        )
    }
    if (confirmarTodo) {
        // La acción masiva se confirma por separado porque afecta a todo el historial.
        AlertDialog(
            onDismissRequest = { confirmarTodo = false },
            title = { Text("¿Eliminar todo el historial?") },
            text = { Text("Se eliminarán permanentemente todos los cálculos guardados.") },
            confirmButton = { Button(enabled = !estado.operacionEnCurso, modifier = Modifier.testTag("confirmar_eliminar_todo"), onClick = { viewModel.eliminarTodo { if (it) confirmarTodo = false } }) { Text("Eliminar todo") } },
            dismissButton = { OutlinedButton(enabled = !estado.operacionEnCurso, onClick = { confirmarTodo = false }) { Text("Cancelar") } },
        )
    }
}

@Composable
internal fun HistorialContent(estado: HistorialUiState, alVolver: () -> Unit, alEliminar: (CalculoEntity) -> Unit, alEliminarTodo: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Historial de cálculos", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        OutlinedButton(onClick = alVolver) { Text("Volver a la calculadora") }
        estado.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        if (!estado.cargando && estado.registros.isEmpty()) {
            // El estado vacío orienta al usuario cuando Room no entrega registros.
            Text("Todavía no tienes cálculos guardados.", style = MaterialTheme.typography.titleMedium)
            Text("Realiza tu primer cálculo para verlo aquí.")
        } else {
            // La lista utiliza claves estables para representar cada registro persistido.
            if (estado.registros.isNotEmpty()) Button(onClick = alEliminarTodo, modifier = Modifier.fillMaxWidth().testTag("eliminar_historial")) { Text("Eliminar historial") }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(estado.registros, key = { it.id }) { CalculoCard(it, { alEliminar(it) }) }
            }
        }
    }
}

@Composable
private fun CalculoCard(calculo: CalculoEntity, alEliminar: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(SimpleDateFormat("dd/MM/yyyy · HH:mm", Locale.getDefault()).format(Date(calculo.fechaRegistro)))
            Fila("Peso:", "${formatear(calculo.pesoKg)} kg")
            Fila("Repeticiones:", calculo.repeticiones.toString())
            Fila("1RM estimado:", "${formatear(calculo.resultado1RM)} kg")
            OutlinedButton(modifier = Modifier.testTag("eliminar_${calculo.id}"), onClick = alEliminar) { Text("Eliminar") }
        }
    }
}

@Composable private fun Fila(etiqueta: String, valor: String) = Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(etiqueta); Text(valor, fontWeight = FontWeight.Medium) }
private fun formatear(valor: Double) = NumberFormat.getNumberInstance(Locale.forLanguageTag("es-ES")).apply { minimumFractionDigits = 1; maximumFractionDigits = 1 }.format(valor)
