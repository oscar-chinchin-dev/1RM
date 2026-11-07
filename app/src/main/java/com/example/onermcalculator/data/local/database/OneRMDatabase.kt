package com.example.onermcalculator.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.onermcalculator.data.local.dao.CalculoDao
import com.example.onermcalculator.data.local.entity.CalculoEntity

@Database(entities = [CalculoEntity::class], version = 1, exportSchema = true)
abstract class OneRMDatabase : RoomDatabase() {
    abstract fun calculoDao(): CalculoDao

    companion object {
        @Volatile
        private var instance: OneRMDatabase? = null

        /** Crea una única base Room para el proceso y reutiliza la instancia ya construida. */
        fun getInstance(context: Context): OneRMDatabase = instance ?: synchronized(this) {
            // La sincronización evita crear más de una base cuando hay accesos concurrentes.
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                OneRMDatabase::class.java,
                "onerm_database",
            ).build().also { instance = it }
        }
    }
}
