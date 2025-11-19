package com.musicmusic.data.validation

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests para UrlValidator.
 * 
 * Verifica la validación de URLs de radios y prevención de SSRF.
 */
class UrlValidatorTest {
    
    // ========== Tests de URLs válidas ==========
    
    @Test
    fun `validates valid HTTP URLs`() {
        val validUrls = listOf(
            "http://stream.example.com:8000/radio",
            "http://radio.example.com/stream.mp3",
            "http://example.com/live/station1",
            "http://192.0.2.1:8080/stream"  // IP pública válida (TEST-NET-1)
        )
        
        validUrls.forEach { url ->
            val result = UrlValidator.validateRadioUrl(url)
            assertTrue(result.isValid(), "Expected $url to be valid")
        }
    }
    
    @Test
    fun `validates valid HTTPS URLs`() {
        val validUrls = listOf(
            "https://stream.example.com:8000/radio",
            "https://radio.example.com/stream.mp3",
            "https://example.com/live/station1"
        )
        
        validUrls.forEach { url ->
            val result = UrlValidator.validateRadioUrl(url)
            assertTrue(result.isValid(), "Expected $url to be valid")
        }
    }
    
    // ========== Tests de esquemas inválidos ==========
    
    @Test
    fun `rejects file scheme URLs`() {
        val invalidUrls = listOf(
            "file:///etc/passwd",
            "file://C:/Windows/System32/config/sam"
        )
        
        invalidUrls.forEach { url ->
            val result = UrlValidator.validateRadioUrl(url)
            assertTrue(result.isInvalid(), "Expected $url to be invalid")
            if (result is ValidationResult.Invalid) {
                assertTrue(result.reason.contains("scheme", ignoreCase = true))
            }
        }
    }
    
    @Test
    fun `rejects FTP URLs`() {
        val url = "ftp://ftp.example.com/file.mp3"
        val result = UrlValidator.validateRadioUrl(url)
        
        assertTrue(result.isInvalid())
        if (result is ValidationResult.Invalid) {
            assertTrue(result.reason.contains("scheme", ignoreCase = true))
        }
    }
    
    @Test
    fun `rejects data URLs`() {
        val url = "data:text/html,<script>alert('XSS')</script>"
        val result = UrlValidator.validateRadioUrl(url)
        
        assertTrue(result.isInvalid())
    }
    
    // ========== Tests de prevención SSRF ==========
    
    @Test
    fun `rejects localhost URLs`() {
        val localhostUrls = listOf(
            "http://localhost:8080/admin",
            "http://127.0.0.1:8080/admin",
            "http://127.0.0.2:8080/internal",
            "http://127.255.255.255/test"
        )
        
        localhostUrls.forEach { url ->
            val result = UrlValidator.validateRadioUrl(url)
            assertTrue(result.isInvalid(), "Expected $url to be rejected (localhost)")
            if (result is ValidationResult.Invalid) {
                assertTrue(
                    result.reason.contains("private", ignoreCase = true) ||
                    result.reason.contains("localhost", ignoreCase = true),
                    "Expected reason to mention private/localhost, got: ${result.reason}"
                )
            }
        }
    }
    
    @Test
    fun `rejects private IP ranges - 10 dot x`() {
        val privateUrls = listOf(
            "http://10.0.0.1/admin",
            "http://10.10.10.10/internal",
            "http://10.255.255.255/secret"
        )
        
        privateUrls.forEach { url ->
            val result = UrlValidator.validateRadioUrl(url)
            assertTrue(result.isInvalid(), "Expected $url to be rejected (private IP)")
        }
    }
    
    @Test
    fun `rejects private IP ranges - 192 dot 168`() {
        val privateUrls = listOf(
            "http://192.168.0.1/admin",
            "http://192.168.1.1/router",
            "http://192.168.255.255/internal"
        )
        
        privateUrls.forEach { url ->
            val result = UrlValidator.validateRadioUrl(url)
            assertTrue(result.isInvalid(), "Expected $url to be rejected (private IP)")
        }
    }
    
    @Test
    fun `rejects private IP ranges - 172 dot 16 to 31`() {
        val privateUrls = listOf(
            "http://172.16.0.1/admin",
            "http://172.20.10.5/internal",
            "http://172.31.255.255/secret"
        )
        
        privateUrls.forEach { url ->
            val result = UrlValidator.validateRadioUrl(url)
            assertTrue(result.isInvalid(), "Expected $url to be rejected (private IP)")
        }
    }
    
    @Test
    fun `rejects link-local addresses`() {
        val linkLocalUrls = listOf(
            "http://169.254.0.1/admin",
            "http://169.254.169.254/metadata"  // AWS metadata endpoint
        )
        
        linkLocalUrls.forEach { url ->
            val result = UrlValidator.validateRadioUrl(url)
            assertTrue(result.isInvalid(), "Expected $url to be rejected (link-local)")
        }
    }
    
    // ========== Tests de extensiones sospechosas ==========
    
    @Test
    fun `rejects URLs with executable extensions`() {
        val suspiciousUrls = listOf(
            "http://example.com/malware.exe",
            "http://example.com/script.bat",
            "http://example.com/payload.sh",
            "http://example.com/command.ps1"
        )
        
        suspiciousUrls.forEach { url ->
            val result = UrlValidator.validateRadioUrl(url)
            assertTrue(result.isInvalid(), "Expected $url to be rejected (suspicious extension)")
            if (result is ValidationResult.Invalid) {
                assertTrue(result.reason.contains("suspicious", ignoreCase = true))
            }
        }
    }
    
    // ========== Tests de formato inválido ==========
    
    @Test
    fun `rejects empty URLs`() {
        val result = UrlValidator.validateRadioUrl("")
        assertTrue(result.isInvalid())
        if (result is ValidationResult.Invalid) {
            assertTrue(result.reason.contains("empty", ignoreCase = true))
        }
    }
    
    @Test
    fun `rejects blank URLs`() {
        val result = UrlValidator.validateRadioUrl("   ")
        assertTrue(result.isInvalid())
    }
    
    @Test
    fun `rejects malformed URLs`() {
        val malformedUrls = listOf(
            "not-a-url",
            "http://",
            "://example.com",
            "example.com",  // Missing scheme
            "http:/example.com",  // Missing slash
            "http:///example.com"  // Missing host
        )
        
        malformedUrls.forEach { url ->
            val result = UrlValidator.validateRadioUrl(url)
            assertTrue(result.isInvalid(), "Expected $url to be invalid (malformed)")
        }
    }
    
    @Test
    fun `rejects extremely long URLs`() {
        val longUrl = "http://example.com/" + "a".repeat(3000)
        val result = UrlValidator.validateRadioUrl(longUrl)
        
        assertTrue(result.isInvalid())
        if (result is ValidationResult.Invalid) {
            assertTrue(result.reason.contains("too long", ignoreCase = true))
        }
    }
    
    @Test
    fun `rejects URLs without host`() {
        val url = "http://:8080/path"
        val result = UrlValidator.validateRadioUrl(url)
        
        assertTrue(result.isInvalid())
    }
    
    // ========== Tests de validación en lote ==========
    
    @Test
    fun `batch validation separates valid and invalid URLs`() {
        val urls = listOf(
            "http://example.com/valid1",
            "http://localhost/invalid",
            "https://example.com/valid2",
            "ftp://example.com/invalid",
            "http://192.168.1.1/invalid",
            "http://stream.radio.com:8000/valid3"
        )
        
        val (valid, invalid) = UrlValidator.validateBatch(urls)
        
        assertEquals(3, valid.size, "Expected 3 valid URLs")
        assertEquals(3, invalid.size, "Expected 3 invalid URLs")
        
        assertTrue("http://example.com/valid1" in valid)
        assertTrue("https://example.com/valid2" in valid)
        assertTrue("http://stream.radio.com:8000/valid3" in valid)
        
        val invalidUrls = invalid.map { it.first }
        assertTrue("http://localhost/invalid" in invalidUrls)
        assertTrue("ftp://example.com/invalid" in invalidUrls)
        assertTrue("http://192.168.1.1/invalid" in invalidUrls)
    }
    
    @Test
    fun `batch validation handles empty list`() {
        val (valid, invalid) = UrlValidator.validateBatch(emptyList())
        
        assertTrue(valid.isEmpty())
        assertTrue(invalid.isEmpty())
    }
    
    @Test
    fun `batch validation provides reasons for invalid URLs`() {
        val urls = listOf(
            "http://localhost/admin",
            "ftp://example.com/file"
        )
        
        val (_, invalid) = UrlValidator.validateBatch(urls)
        
        assertEquals(2, invalid.size)
        invalid.forEach { (url, reason) ->
            assertFalse(reason.isBlank(), "Reason should not be blank for $url")
        }
    }
    
    // ========== Tests de edge cases ==========
    
    @Test
    fun `accepts URLs with ports`() {
        val urlsWithPorts = listOf(
            "http://example.com:8000/stream",
            "https://example.com:443/radio",
            "http://example.com:80/live"
        )
        
        urlsWithPorts.forEach { url ->
            val result = UrlValidator.validateRadioUrl(url)
            assertTrue(result.isValid(), "Expected $url with port to be valid")
        }
    }
    
    @Test
    fun `accepts URLs with query parameters`() {
        val url = "http://example.com/stream?bitrate=128&format=mp3"
        val result = UrlValidator.validateRadioUrl(url)
        
        assertTrue(result.isValid())
    }
    
    @Test
    fun `accepts URLs with common streaming paths`() {
        val streamingUrls = listOf(
            "http://example.com/stream.mp3",
            "http://example.com/radio.m3u",
            "http://example.com/live.pls",
            "http://example.com/station.aac"
        )
        
        streamingUrls.forEach { url ->
            val result = UrlValidator.validateRadioUrl(url)
            assertTrue(result.isValid(), "Expected $url to be valid")
        }
    }
    
    @Test
    fun `accepts URLs with authentication (if public)`() {
        val url = "http://user:pass@example.com/stream"
        val result = UrlValidator.validateRadioUrl(url)
        
        // Should be valid as long as host is not private
        assertTrue(result.isValid())
    }
}
