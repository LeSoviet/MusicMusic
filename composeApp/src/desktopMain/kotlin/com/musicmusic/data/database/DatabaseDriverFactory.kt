package com.musicmusic.data.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.musicmusic.database.AppDatabase
import java.io.File

/**
 * Factory para crear la base de datos SQLDelight.
 */
object DatabaseDriverFactory {
    
    private var cachedDatabase: AppDatabase? = null
    
    /**
     * Crea el driver de la base de datos.
     */
    private fun createDriver(): SqlDriver {
        // Directorio para la base de datos
        val databaseDir = File(System.getProperty("user.home"), ".musicmusic")
        if (!databaseDir.exists()) {
            val created = databaseDir.mkdirs()
            println("📁 Directorio de base de datos creado: $created")
        }
        
        val databasePath = File(databaseDir, "musicmusic.db")
        println("📂 Database path: ${databasePath.absolutePath}")
        
        // Crear driver con JDBC
        val driver: SqlDriver = JdbcSqliteDriver("jdbc:sqlite:${databasePath.absolutePath}")
        
        // Verificar si las tablas ya existen
        val tablesExist = try {
            driver.executeQuery(
                null,
                "SELECT name FROM sqlite_master WHERE type='table' AND name='RadioEntity'",
                { cursor ->
                    cursor.next()
                },
                0
            ).value
        } catch (e: Exception) {
            false
        }
        
        if (!tablesExist) {
            println("🔨 Creando schema de base de datos...")
            try {
                AppDatabase.Schema.create(driver)
                println("✅ Schema creado exitosamente")
                
                // Verificar que se creó
                val verification = driver.executeQuery(
                    null,
                    "SELECT name FROM sqlite_master WHERE type='table' AND name='RadioEntity'",
                    { cursor ->
                        cursor.next()
                    },
                    0
                ).value
                
                if (verification) {
                    println("✅ Tabla RadioEntity verificada")
                } else {
                    println("❌ ERROR: Tabla RadioEntity NO se creó")
                }
            } catch (e: Exception) {
                println("❌ Error al crear schema: ${e.message}")
                e.printStackTrace()
                throw e
            }
        } else {
            println("✅ Tablas ya existen en la base de datos")
        }
        
        return driver
    }
    
    /**
     * Crea una instancia de la base de datos.
     */
    fun createDatabase(): AppDatabase {
        if (cachedDatabase == null) {
            println("🔄 Inicializando base de datos...")
            cachedDatabase = AppDatabase(createDriver())
            println("✅ Base de datos inicializada")
        }
        return cachedDatabase!!
    }
    
    /**
     * Limpia el caché de la base de datos (útil para testing)
     */
    fun clearCache() {
        cachedDatabase = null
    }
}
