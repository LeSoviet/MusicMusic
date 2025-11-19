package com.musicmusic.data.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.musicmusic.database.AppDatabase
import java.io.File

/**
 * Factory para crear la base de datos SQLDelight.
 */
object DatabaseDriverFactory {
    
    /**
     * Crea el driver de la base de datos.
     */
    fun createDriver(): SqlDriver {
        // Directorio para la base de datos
        val databaseDir = File(System.getProperty("user.home"), ".musicmusic")
        if (!databaseDir.exists()) {
            databaseDir.mkdirs()
        }
        
        val databasePath = File(databaseDir, "musicmusic.db")
        val driver = JdbcSqliteDriver("jdbc:sqlite:${databasePath.absolutePath}")
        
        // Crear las tablas si no existen
        try {
            println("📂 Base de datos en: ${databasePath.absolutePath}")
            AppDatabase.Schema.create(driver)
            println("✅ Esquema de base de datos creado correctamente")
        } catch (e: Exception) {
            println("⚠️ Error al crear esquema (puede que ya exista): ${e.message}")
        }
        
        return driver
    }
    
    /**
     * Crea una instancia de la base de datos.
     */
    fun createDatabase(): AppDatabase {
        return AppDatabase(createDriver())
    }
}
