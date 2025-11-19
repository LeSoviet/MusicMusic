package com.musicmusic.data.validation

import java.net.InetAddress
import java.net.URI
import java.net.URL

/**
 * Validador de URLs para streaming de radios.
 * 
 * Proporciona validación de:
 * - Formato de URL válido
 * - Esquemas permitidos (http, https)
 * - Prevención de SSRF (Server-Side Request Forgery)
 * - Prevención de URLs locales/privadas
 */
object UrlValidator {
    
    // Esquemas permitidos para streaming
    private val ALLOWED_SCHEMES = setOf("http", "https")
    
    // Rangos de IP privadas (RFC 1918)
    private val PRIVATE_IP_RANGES = listOf(
        "10.0.0.0" to "10.255.255.255",
        "172.16.0.0" to "172.31.255.255",
        "192.168.0.0" to "192.168.255.255",
        "127.0.0.0" to "127.255.255.255",  // Loopback
        "169.254.0.0" to "169.254.255.255", // Link-local
        "0.0.0.0" to "0.255.255.255"        // Reserved
    )
    
    /**
     * Valida una URL de radio.
     * 
     * @param urlString La URL a validar
     * @return ValidationResult con el estado de validación
     */
    fun validateRadioUrl(urlString: String): ValidationResult {
        // 1. Verificar que no esté vacía
        if (urlString.isBlank()) {
            return ValidationResult.Invalid("URL cannot be empty")
        }
        
        // 2. Verificar longitud razonable (evitar DoS)
        if (urlString.length > 2048) {
            return ValidationResult.Invalid("URL too long (max 2048 characters)")
        }
        
        // 3. Parse URL
        val url = try {
            URL(urlString)
        } catch (e: Exception) {
            return ValidationResult.Invalid("Invalid URL format: ${e.message}")
        }
        
        // 4. Verificar esquema permitido
        if (url.protocol.lowercase() !in ALLOWED_SCHEMES) {
            return ValidationResult.Invalid(
                "Invalid scheme '${url.protocol}'. Only HTTP and HTTPS are allowed"
            )
        }
        
        // 5. Verificar que tenga host
        if (url.host.isNullOrBlank()) {
            return ValidationResult.Invalid("URL must have a host")
        }
        
        // 6. Prevenir SSRF - verificar que no sea IP privada o localhost
        if (isPrivateOrLocalhost(url.host)) {
            return ValidationResult.Invalid(
                "Private or localhost URLs are not allowed for security reasons"
            )
        }
        
        // 7. Verificar extensiones sospechosas (opcional - lista blanca)
        val path = url.path.lowercase()
        val suspiciousExtensions = listOf(".exe", ".bat", ".sh", ".ps1", ".cmd")
        if (suspiciousExtensions.any { path.endsWith(it) }) {
            return ValidationResult.Invalid("URL contains suspicious file extension")
        }
        
        return ValidationResult.Valid(urlString)
    }
    
    /**
     * Valida una lista de URLs y retorna solo las válidas.
     * 
     * @param urls Lista de URLs a validar
     * @return Par de listas: (URLs válidas, URLs inválidas con razón)
     */
    fun validateBatch(urls: List<String>): Pair<List<String>, List<Pair<String, String>>> {
        val valid = mutableListOf<String>()
        val invalid = mutableListOf<Pair<String, String>>()
        
        urls.forEach { url ->
            when (val result = validateRadioUrl(url)) {
                is ValidationResult.Valid -> valid.add(result.url)
                is ValidationResult.Invalid -> invalid.add(url to result.reason)
            }
        }
        
        return valid to invalid
    }
    
    /**
     * Verifica si un host es una IP privada o localhost.
     */
    private fun isPrivateOrLocalhost(host: String): Boolean {
        // Verificar nombres de localhost comunes
        val localhostNames = setOf("localhost", "127.0.0.1", "::1", "0.0.0.0")
        if (host.lowercase() in localhostNames) {
            return true
        }
        
        // Intentar resolver el host a IP
        val ipAddress = try {
            InetAddress.getByName(host).hostAddress
        } catch (e: Exception) {
            // Si no se puede resolver, asumimos que es válido (puede ser un DNS válido)
            return false
        }
        
        // Verificar si la IP está en rangos privados
        return isIpInPrivateRange(ipAddress)
    }
    
    /**
     * Verifica si una IP está en un rango privado.
     */
    private fun isIpInPrivateRange(ip: String): Boolean {
        // Convertir IP a long para comparación
        val ipLong = ipToLong(ip) ?: return false
        
        return PRIVATE_IP_RANGES.any { (start, end) ->
            val startLong = ipToLong(start) ?: return@any false
            val endLong = ipToLong(end) ?: return@any false
            ipLong in startLong..endLong
        }
    }
    
    /**
     * Convierte una dirección IP a Long para comparación numérica.
     */
    private fun ipToLong(ip: String): Long? {
        return try {
            val parts = ip.split(".")
            if (parts.size != 4) return null
            
            parts.map { it.toLongOrNull() ?: return null }
                .fold(0L) { acc, part -> (acc shl 8) + part }
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * Resultado de validación de URL.
 */
sealed class ValidationResult {
    data class Valid(val url: String) : ValidationResult()
    data class Invalid(val reason: String) : ValidationResult()
    
    fun isValid(): Boolean = this is Valid
    fun isInvalid(): Boolean = this is Invalid
}
