package com.example.onermcalculator.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import java.util.Locale

/** Presenta las cargas de entrenamiento previamente calculadas a partir del 1RM. */
@Composable
fun TablaPorcentajes(porcentajes: Map<Int, Double>, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Cargas de entrenamiento",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            porcentajes.toSortedMap().forEach { (porcentaje, carga) ->
                FilaPorcentaje(porcentaje = porcentaje, carga = carga)
                if (porcentaje != porcentajes.keys.maxOrNull()) {
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun FilaPorcentaje(porcentaje: Int, carga: Double) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("$porcentaje%", style = MaterialTheme.typography.bodyLarge)
        Text(
            text = "${formatearKilogramos(carga)} kg",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
        )
    }
}

private fun formatearKilogramos(valor: Double): String =
    NumberFormat.getNumberInstance(Locale.forLanguageTag("es-ES")).apply {
        minimumFractionDigits = 1
        maximumFractionDigits = 1
    }.format(valor)
