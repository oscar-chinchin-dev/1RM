package com.example.onermcalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.onermcalculator.ui.calculadora.CalculadoraScreen
import com.example.onermcalculator.ui.historial.HistorialScreen
import com.example.onermcalculator.ui.theme.OneRmCalculatorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // La actividad conserva la pantalla actual y proporciona el repositorio compartido.
            OneRmCalculatorTheme {
                var mostrandoHistorial by remember { mutableStateOf(false) }
                if (mostrandoHistorial) {
                    // El gesto Atrás vuelve a la calculadora cuando el historial está visible.
                    BackHandler { mostrandoHistorial = false }
                    HistorialScreen(
                        repositorio = (application as OneRMApplication).calculoRepository,
                        alVolver = { mostrandoHistorial = false },
                    )
                } else {
                    CalculadoraScreen(
                        repositorio = (application as OneRMApplication).calculoRepository,
                        alVerHistorial = { mostrandoHistorial = true },
                    )
                }
            }
        }
    }
}
