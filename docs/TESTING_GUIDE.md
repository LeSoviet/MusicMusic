# 🎵 Guía de Testing Manual - Fase 2

## 🚀 Setup Inicial

### 1. Verificar Instalación de VLC
```powershell
# Windows - Verificar que VLC esté instalado
Get-Command vlc -ErrorAction SilentlyContinue

# O buscar en Program Files
Test-Path "C:\Program Files\VideoLAN\VLC\vlc.exe"
```

### 2. Preparar Archivos de Audio
Crea una carpeta con algunos archivos de audio de prueba:
```
C:\Users\TuUsuario\Music\Test\
├── song1.mp3
├── song2.flac
└── song3.m4a
```

---

## 🧪 Tests a Realizar

### Test 1: Compilación del Proyecto
```powershell
# Limpiar y compilar
.\gradlew clean
.\gradlew :composeApp:build
```

**Resultado esperado**: ✅ Compilación exitosa sin errores

---

### Test 2: Ejecutar la Aplicación
```powershell
.\gradlew :composeApp:run
```

**Resultado esperado**: 
- ✅ Se abre una ventana de 1200x800px
- ✅ Se muestra "🎵 MusicMusic"
- ✅ No hay errores en la consola

---

### Test 3: Escaneo de Biblioteca (Código de prueba)

Crea un archivo de prueba temporal en `composeApp/src/desktopMain/kotlin/com/musicmusic/TestScanner.kt`:

```kotlin
package com.musicmusic

import com.musicmusic.files.FileScanner
import com.musicmusic.files.MetadataReader
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    println("🎵 Test de FileScanner")
    println("=" * 50)
    
    val metadataReader = MetadataReader()
    val scanner = FileScanner(metadataReader)
    
    // Cambiar esto a tu carpeta de música
    val musicFolder = "C:\\Users\\TuUsuario\\Music\\Test"
    
    println("Escaneando: $musicFolder")
    
    scanner.scanDirectory(musicFolder).collect { progress ->
        println("Progreso: ${progress.percentage * 100}% - ${progress.currentFile}")
    }
    
    val audioFiles = scanner.findAudioFiles(java.io.File(musicFolder))
    println("\n✅ Archivos encontrados: ${audioFiles.size}")
    
    audioFiles.forEach { file ->
        val song = scanner.processSongFile(file)
        if (song != null) {
            println("\n🎵 ${song.title}")
            println("   Artista: ${song.artist}")
            println("   Álbum: ${song.album}")
            println("   Duración: ${song.getFormattedDuration()}")
            println("   Bitrate: ${song.bitrate} kbps")
        }
    }
}
```

**Ejecutar**: `.\gradlew :composeApp:run --args="TestScanner"`

**Resultado esperado**:
- ✅ Lista todos los archivos de audio
- ✅ Muestra metadatos correctos
- ✅ Duración formateada correctamente

---

### Test 4: Reproducción de Audio (Código de prueba)

Crea `TestPlayer.kt`:

```kotlin
package com.musicmusic

import com.musicmusic.audio.VlcjAudioPlayer
import com.musicmusic.domain.model.Song
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect

fun main() = runBlocking {
    println("🎵 Test de VlcjAudioPlayer")
    println("=" * 50)
    
    val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    val player = VlcjAudioPlayer(scope)
    
    // Crear una canción de prueba (cambiar path)
    val song = Song(
        id = "test1",
        title = "Test Song",
        artist = "Test Artist",
        album = "Test Album",
        duration = 180000,
        filePath = "C:\\Users\\TuUsuario\\Music\\Test\\song1.mp3"
    )
    
    // Observar estados
    launch {
        player.playbackState.collect { state ->
            println("Estado: $state")
        }
    }
    
    launch {
        player.currentPosition.collect { position ->
            if (position > 0) {
                val seconds = position / 1000
                print("\rPosición: ${seconds}s  ")
            }
        }
    }
    
    println("\n▶️  Reproduciendo...")
    player.play(song)
    
    delay(5000) // Reproducir 5 segundos
    
    println("\n⏸️  Pausando...")
    player.pause()
    
    delay(2000)
    
    println("▶️  Resumiendo...")
    player.resume()
    
    delay(3000)
    
    println("\n⏹️  Deteniendo...")
    player.stop()
    
    delay(1000)
    
    player.release()
    scope.cancel()
    
    println("\n✅ Test completado")
}
```

**Resultado esperado**:
- ✅ Reproduce el audio
- ✅ Pausa correctamente
- ✅ Resume la reproducción
- ✅ Actualiza la posición en tiempo real

---

### Test 5: Control de Volumen

Agregar a `TestPlayer.kt`:

```kotlin
println("\n🔊 Probando volumen...")

println("Volumen 100%")
player.setVolume(1.0f)
delay(2000)

println("Volumen 50%")
player.setVolume(0.5f)
delay(2000)

println("Volumen 10%")
player.setVolume(0.1f)
delay(2000)
```

**Resultado esperado**:
- ✅ El volumen cambia audiblemente

---

### Test 6: Cola de Reproducción

```kotlin
println("\n📝 Probando cola de reproducción...")

val songs = listOf(
    Song(id = "1", title = "Song 1", artist = "Artist", album = "Album", 
         duration = 0, filePath = "path/to/song1.mp3"),
    Song(id = "2", title = "Song 2", artist = "Artist", album = "Album", 
         duration = 0, filePath = "path/to/song2.mp3"),
    Song(id = "3", title = "Song 3", artist = "Artist", album = "Album", 
         duration = 0, filePath = "path/to/song3.mp3")
)

player.playQueue(songs, 0)

delay(5000)

println("⏭️  Siguiente canción")
player.next()

delay(5000)

println("⏮️  Canción anterior")
player.previous()
```

**Resultado esperado**:
- ✅ Reproduce la primera canción
- ✅ Salta a la siguiente
- ✅ Vuelve a la anterior

---

### Test 7: Shuffle y Repeat

```kotlin
println("\n🔀 Activando shuffle...")
player.setShuffle(true)

delay(1000)

println("🔁 Activando repeat ALL...")
player.setRepeatMode(RepeatMode.ALL)
```

**Resultado esperado**:
- ✅ Estados se actualizan correctamente

---

## 📊 Checklist de Validación

### Compilación
- [ ] El proyecto compila sin errores
- [ ] No hay warnings críticos
- [ ] Las dependencias se resuelven correctamente

### Funcionalidad de Audio
- [ ] VLC se inicializa correctamente
- [ ] Reproduce archivos MP3
- [ ] Reproduce archivos FLAC
- [ ] Reproduce archivos M4A
- [ ] Pausa funciona
- [ ] Resume funciona
- [ ] Stop funciona
- [ ] Next funciona
- [ ] Previous funciona

### Control de Posición
- [ ] Seek to position funciona
- [ ] Seek forward funciona
- [ ] Seek backward funciona
- [ ] La posición se actualiza en tiempo real

### Control de Volumen
- [ ] Set volume funciona
- [ ] Increase volume funciona
- [ ] Decrease volume funciona
- [ ] Mute funciona

### Metadatos
- [ ] Lee título correctamente
- [ ] Lee artista correctamente
- [ ] Lee álbum correctamente
- [ ] Lee duración correctamente
- [ ] Lee bitrate correctamente
- [ ] Extrae carátula correctamente

### Escaneo de Archivos
- [ ] Encuentra archivos recursivamente
- [ ] Filtra solo archivos de audio
- [ ] Procesa múltiples formatos
- [ ] Genera IDs únicos
- [ ] Guarda carátulas en disco

---

## 🐛 Troubleshooting

### Error: "Unable to load library 'libvlc'"
**Solución**:
```powershell
# Verificar instalación de VLC
vlc --version

# Reinstalar VLC si es necesario
# Descargar desde: https://www.videolan.org/vlc/
```

### Error: "Could not find or load main class"
**Solución**:
```powershell
.\gradlew clean
.\gradlew :composeApp:build
```

### Error al leer metadatos
**Solución**:
- Verificar que el archivo tenga tags ID3
- Probar con otro archivo
- Verificar permisos de lectura

### No se escucha audio
**Solución**:
- Verificar que VLC esté instalado
- Verificar que el archivo existe
- Verificar que no esté en mute
- Probar con otro archivo

---

## 📝 Reporte de Resultados

Crea un archivo `TEST_RESULTS.md` con:

```markdown
# Resultados de Testing - Fase 2

Fecha: [TU_FECHA]
Sistema: Windows/Linux
JDK: [VERSION]
VLC: [VERSION]

## Tests Ejecutados

### ✅ Test 1: Compilación
- Estado: PASS/FAIL
- Notas: [Observaciones]

### ✅ Test 2: Ejecución
- Estado: PASS/FAIL
- Notas: [Observaciones]

[Continuar para todos los tests...]

## Problemas Encontrados

1. [Descripción del problema]
   - Solución: [Cómo se resolvió]

## Conclusión

[Resumen general del estado de la Fase 2]
```

---

**¡Con estos tests validarás que la Fase 2 funciona correctamente! 🎉**
