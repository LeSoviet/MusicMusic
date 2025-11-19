package com.musicmusic.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.musicmusic.data.validation.UrlValidator
import com.musicmusic.data.validation.ValidationResult
import com.musicmusic.database.AppDatabase
import com.musicmusic.domain.error.AppError
import com.musicmusic.domain.error.ErrorHandler
import com.musicmusic.domain.model.Radio
import com.musicmusic.domain.model.RadioList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/**
 * Repositorio para gestionar las radios online.
 * 
 * Incluye validación de URLs para prevenir SSRF y otros ataques.
 */
class RadioRepository(
    private val database: AppDatabase,
    private val errorHandler: ErrorHandler? = null
) {
    private val json = Json { 
        ignoreUnknownKeys = true
        prettyPrint = true
    }
    
    private val queries = database.radioQueries
    
    /**
     * Flow de todas las radios desde la base de datos.
     */
    val radios: Flow<List<Radio>> = queries.selectAll()
        .asFlow()
        .mapToList(Dispatchers.IO)
        .map { entities ->
            entities.map { entity ->
                Radio(
                    id = entity.id,
                    name = entity.name,
                    url = entity.url,
                    genre = entity.genre,
                    country = entity.country,
                    logoUrl = entity.logoUrl,
                    description = entity.description,
                    bitrate = entity.bitrate?.toInt(),
                    isFavorite = entity.isFavorite == 1L,
                    tags = entity.tags.split(",").filter { it.isNotBlank() }
                )
            }
        }
    
    /**
     * Flow de radios favoritas.
     */
    val favoriteRadios: Flow<List<Radio>> = queries.selectFavorites()
        .asFlow()
        .mapToList(Dispatchers.IO)
        .map { entities ->
            entities.map { entity ->
                Radio(
                    id = entity.id,
                    name = entity.name,
                    url = entity.url,
                    genre = entity.genre,
                    country = entity.country,
                    logoUrl = entity.logoUrl,
                    description = entity.description,
                    bitrate = entity.bitrate?.toInt(),
                    isFavorite = true,
                    tags = entity.tags.split(",").filter { it.isNotBlank() }
                )
            }
        }
    
    /**
     * Carga las radios desde el archivo JSON e inserta en la base de datos.
     */
    suspend fun loadRadios() = withContext(Dispatchers.IO) {
        try {
            // Verificar si ya hay radios en la base de datos
            val existingRadios = queries.selectAll().executeAsList()
            if (existingRadios.isNotEmpty()) {
                println("Radios ya cargadas en la base de datos: ${existingRadios.size}")
                return@withContext
            }
            
            // Cargar desde recursos empaquetados
            val radioJson = this::class.java.classLoader
                ?.getResourceAsStream("radios.json")
                ?.bufferedReader()
                ?.use { it.readText() }
                ?: throw IllegalStateException("No se pudo cargar el archivo radios.json")
            
            val radioList = json.decodeFromString<RadioList>(radioJson)
            
            // Validar URLs antes de insertar
            val validRadios = mutableListOf<com.musicmusic.domain.model.Radio>()
            val invalidCount = radioList.radios.count { radio ->
                when (val result = UrlValidator.validateRadioUrl(radio.url)) {
                    is ValidationResult.Valid -> {
                        validRadios.add(radio)
                        false
                    }
                    is ValidationResult.Invalid -> {
                        println("⚠️ Radio '${radio.name}' tiene URL inválida: ${result.reason}")
                        true
                    }
                }
            }
            
            println("✅ URLs válidas: ${validRadios.size}, inválidas: $invalidCount")
            
            // Insertar solo radios válidas en la base de datos
            validRadios.forEach { radio ->
                queries.insertRadio(
                    id = radio.id,
                    name = radio.name,
                    url = radio.url,
                    genre = radio.genre,
                    country = radio.country,
                    logoUrl = radio.logoUrl,
                    description = radio.description,
                    bitrate = radio.bitrate?.toLong(),
                    isFavorite = if (radio.isFavorite) 1 else 0,
                    tags = radio.tags.joinToString(",")
                )
            }
            
            println("✅ ${validRadios.size} radios cargadas correctamente ($invalidCount URLs inválidas filtradas)")
        } catch (e: Exception) {
            println("❌ Error cargando radios: ${e.message}")
            e.printStackTrace()
        }
    }
    
    /**
     * Obtiene todas las radios.
     */
    suspend fun getAllRadios(): List<Radio> = withContext(Dispatchers.IO) {
        queries.selectAll().executeAsList().map { entity ->
            Radio(
                id = entity.id,
                name = entity.name,
                url = entity.url,
                genre = entity.genre,
                country = entity.country,
                logoUrl = entity.logoUrl,
                description = entity.description,
                bitrate = entity.bitrate?.toInt(),
                isFavorite = entity.isFavorite == 1L,
                tags = entity.tags.split(",").filter { it.isNotBlank() }
            )
        }
    }
    
    /**
     * Busca radios por nombre, género o país.
     */
    suspend fun searchRadios(query: String): List<Radio> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext getAllRadios()
        
        queries.searchRadios(query, query, query, query).executeAsList().map { entity ->
            Radio(
                id = entity.id,
                name = entity.name,
                url = entity.url,
                genre = entity.genre,
                country = entity.country,
                logoUrl = entity.logoUrl,
                description = entity.description,
                bitrate = entity.bitrate?.toInt(),
                isFavorite = entity.isFavorite == 1L,
                tags = entity.tags.split(",").filter { it.isNotBlank() }
            )
        }
    }
    
    /**
     * Filtra radios por género.
     */
    suspend fun getRadiosByGenre(genre: String): List<Radio> = withContext(Dispatchers.IO) {
        queries.selectByGenre(genre).executeAsList().map { entity ->
            Radio(
                id = entity.id,
                name = entity.name,
                url = entity.url,
                genre = entity.genre,
                country = entity.country,
                logoUrl = entity.logoUrl,
                description = entity.description,
                bitrate = entity.bitrate?.toInt(),
                isFavorite = entity.isFavorite == 1L,
                tags = entity.tags.split(",").filter { it.isNotBlank() }
            )
        }
    }
    
    /**
     * Filtra radios por país.
     */
    suspend fun getRadiosByCountry(country: String): List<Radio> = withContext(Dispatchers.IO) {
        queries.selectByCountry(country).executeAsList().map { entity ->
            Radio(
                id = entity.id,
                name = entity.name,
                url = entity.url,
                genre = entity.genre,
                country = entity.country,
                logoUrl = entity.logoUrl,
                description = entity.description,
                bitrate = entity.bitrate?.toInt(),
                isFavorite = entity.isFavorite == 1L,
                tags = entity.tags.split(",").filter { it.isNotBlank() }
            )
        }
    }
    
    /**
     * Obtiene todos los géneros únicos disponibles.
     */
    suspend fun getAvailableGenres(): List<String> = withContext(Dispatchers.IO) {
        queries.getAllGenres().executeAsList().mapNotNull { it }
    }
    
    /**
     * Obtiene todos los países únicos disponibles.
     */
    suspend fun getAvailableCountries(): List<String> = withContext(Dispatchers.IO) {
        queries.getAllCountries().executeAsList().mapNotNull { it }
    }
    
    /**
     * Marca/desmarca una radio como favorita.
     */
    suspend fun toggleFavorite(radioId: String) = withContext(Dispatchers.IO) {
        val radio = queries.selectById(radioId).executeAsOneOrNull()
        if (radio != null) {
            val newFavoriteState = if (radio.isFavorite == 1L) 0L else 1L
            queries.updateFavorite(newFavoriteState, radioId)
        }
    }
    
    /**
     * Obtiene las radios marcadas como favoritas.
     */
    suspend fun getFavoriteRadios(): List<Radio> = withContext(Dispatchers.IO) {
        queries.selectFavorites().executeAsList().map { entity ->
            Radio(
                id = entity.id,
                name = entity.name,
                url = entity.url,
                genre = entity.genre,
                country = entity.country,
                logoUrl = entity.logoUrl,
                description = entity.description,
                bitrate = entity.bitrate?.toInt(),
                isFavorite = true,
                tags = entity.tags.split(",").filter { it.isNotBlank() }
            )
        }
    }
    
    /**
     * Obtiene una radio por su ID.
     */
    suspend fun getRadioById(radioId: String): Radio? = withContext(Dispatchers.IO) {
        queries.selectById(radioId).executeAsOneOrNull()?.let { entity ->
            Radio(
                id = entity.id,
                name = entity.name,
                url = entity.url,
                genre = entity.genre,
                country = entity.country,
                logoUrl = entity.logoUrl,
                description = entity.description,
                bitrate = entity.bitrate?.toInt(),
                isFavorite = entity.isFavorite == 1L,
                tags = entity.tags.split(",").filter { it.isNotBlank() }
            )
        }
    }
    
    /**
     * Valida una URL de radio.
     * 
     * Verifica:
     * - Formato válido
     * - Esquema permitido (http/https)
     * - No es IP privada o localhost (prevención SSRF)
     * - No contiene extensiones sospechosas
     * 
     * @param url La URL a validar
     * @return ValidationResult con el estado de validación
     */
    suspend fun validateRadioUrl(url: String): ValidationResult = withContext(Dispatchers.IO) {
        UrlValidator.validateRadioUrl(url)
    }
    
    /**
     * Valida una lista de URLs.
     * 
     * @param urls Lista de URLs a validar
     * @return Par de listas: (URLs válidas, URLs inválidas con razón)
     */
    suspend fun validateBatchUrls(urls: List<String>): Pair<List<String>, List<Pair<String, String>>> = 
        withContext(Dispatchers.IO) {
            UrlValidator.validateBatch(urls)
        }
}
