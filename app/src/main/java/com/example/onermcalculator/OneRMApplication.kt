package com.example.onermcalculator

import android.app.Application
import com.example.onermcalculator.data.local.database.OneRMDatabase
import com.example.onermcalculator.data.repository.CalculoRepository

class OneRMApplication : Application() {
    // La creación diferida reutiliza una sola cadena de dependencias durante la vida de la aplicación.
    val calculoRepository: CalculoRepository by lazy {
        CalculoRepository(OneRMDatabase.getInstance(applicationContext).calculoDao())
    }
}
