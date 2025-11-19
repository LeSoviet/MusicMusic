# 🎵 MusicMusic - Roadmap del Proyecto

## Descripción del Proyecto
Reproductor de música multiplataforma (Windows/Linux) desarrollado en Kotlin Multiplatform con Compose Desktop. Un cliente estilo Spotify pero para música local, con integración de 400+ radios por streaming.

## 🎯 Objetivos Principales
- ✅ Gestión completa de biblioteca de música local
- ✅ Reproductor con soporte para formatos comunes (MP3, FLAC, WAV, OGG, AAC, M4A)
- ✅ Streaming de 400+ radios online
- ✅ UI Premium minimalista con colores suaves
- ✅ Multiplataforma: Windows y Linux nativo

---

## 📚 Stack Tecnológico

### Core
- **Kotlin Multiplatform (KMP)**: Código compartido entre plataformas
- **Compose Multiplatform Desktop**: UI declarativa moderna
- **Gradle 8+**: Sistema de build

### Audio & Media
- **VLC Java Bindings (VLCJ)**: Motor de reproducción multiplataforma
  - Soporta todos los formatos de audio
  - Streaming de radios HTTP/HTTPS
  - Control avanzado de reproducción
  - Excelente para Windows/Linux

### Networking
- **Ktor Client**: HTTP client para streaming de radios
  - Multiplataforma
  - Coroutines integradas
  - Streaming eficiente

### Database
- **SQLDelight**: Base de datos SQL type-safe multiplataforma
  - Gestión de biblioteca de música
  - Playlists y favoritos
  - Historial de reproducción

### Serialización
- **Kotlinx Serialization**: Para metadatos y configuración JSON
  - Lista de radios
  - Configuración de usuario
  - Tags de audio

### Dependency Injection
- **Koin**: DI ligero y multiplataforma
  - Gestión de dependencias
  - ViewModel pattern

### File System
- **Okio / Kotlinx IO**: Operaciones de archivos multiplataforma
  - Escaneo de biblioteca
  - Lectura de metadatos

### Audio Metadata
- **JAudioTagger** o **Mp3agic**: Lectura de tags ID3
  - Título, artista, álbum
  - Carátulas
  - Metadatos extendidos

---

## 🏗️ Arquitectura del Proyecto

```
MusicMusic/
│
├── composeApp/                    # Aplicación Compose Desktop
│   ├── src/
│   │   ├── commonMain/           # Código compartido
│   │   │   ├── kotlin/
│   │   │   │   ├── ui/          # Componentes UI
│   │   │   │   │   ├── theme/   # Tema premium minimalista
│   │   │   │   │   ├── screens/ # Pantallas principales
│   │   │   │   │   └── components/ # Componentes reutilizables
│   │   │   │   ├── domain/      # Lógica de negocio
│   │   │   │   ├── data/        # Repositorios y sources
│   │   │   │   └── di/          # Koin modules
│   │   │   └── resources/       # Assets compartidos
│   │   │
│   │   ├── desktopMain/         # Código específico desktop
│   │   │   └── kotlin/
│   │   │       ├── audio/       # Implementación VLCJ
│   │   │       ├── files/       # Sistema de archivos nativo
│   │   │       └── platform/    # APIs específicas de plataforma
│   │   │
│   │   ├── windowsMain/         # Específico Windows (opcional)
│   │   └── linuxMain/           # Específico Linux (opcional)
│   │
│   └── build.gradle.kts
│
├── shared/                       # Lógica compartida pura (opcional)
│   ├── src/
│   │   └── commonMain/
│   │       ├── kotlin/
│   │       │   ├── models/      # Data classes
│   │       │   ├── utils/       # Utilidades
│   │       │   └── constants/   # Constantes
│   └── build.gradle.kts
│
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

---

## 🎨 Diseño UI Premium Minimalista

### Principios de Diseño
- **Minimalismo**: Espacios amplios, elementos esenciales
- **Colores Suaves**: Paleta pastel/neutral
  - Fondo: Gris muy claro (#F5F5F7) / Gris oscuro (#1C1C1E) modo oscuro
  - Acentos: Lavanda suave (#B4A7D6), Verde menta (#A8E6CF)
  - Texto: Gris carbón (#2C2C2E) / Blanco (#FFFFFF)
- **Tipografía**: Inter o SF Pro Display
- **Animaciones**: Sutiles y fluidas (easing natural)
- **Glassmorphism**: Efectos de vidrio esmerilado para capas

### Pantallas Principales
1. **Home**: Biblioteca principal con grid de álbumes
2. **Now Playing**: Vista de reproducción actual con carátula grande
3. **Radios**: Lista categorizada de 400 radios
4. **Playlists**: Gestión de playlists personalizadas
5. **Search**: Búsqueda en biblioteca y radios
6. **Settings**: Configuración de la app

---

## 📋 Fases de Desarrollo

### 🟢 Fase 1: Setup & Fundamentos (Semana 1-2)
**Objetivo**: Configurar proyecto base funcional

#### Tareas:
1. **Setup Inicial**
   - [ ] Crear proyecto Kotlin Multiplatform con Compose Desktop
   - [ ] Configurar Gradle con todas las dependencias
   - [ ] Setup SQLDelight para base de datos local
   - [ ] Configurar Koin para DI

2. **Arquitectura Base**
   - [ ] Definir modelos de datos (Song, Album, Artist, Radio, Playlist)
   - [ ] Implementar capa de repositorio
   - [ ] Crear ViewModels básicos
   - [ ] Setup de navegación entre pantallas

3. **UI Base**
   - [ ] Diseñar sistema de tema (colores, tipografía, shapes)
   - [ ] Crear layout principal con sidebar
   - [ ] Implementar componentes básicos (Button, Card, List)

**Entregable**: App que compila y muestra UI básica con navegación

---

### 🟡 Fase 2: Motor de Audio (Semana 3-4)
**Objetivo**: Integrar reproducción de audio local

#### Tareas:
1. **Integración VLCJ**
   - [ ] Integrar VLCJ en el proyecto
   - [ ] Crear `AudioPlayer` service con VLCJ
   - [ ] Implementar controles: play, pause, stop, seek
   - [ ] Gestión de volumen y equalizer

2. **Lectura de Metadatos**
   - [ ] Integrar JAudioTagger
   - [ ] Extraer metadatos de archivos (título, artista, álbum, año)
   - [ ] Cargar carátulas embebidas
   - [ ] Calcular duración de tracks

3. **Biblioteca Local**
   - [ ] Implementar scanner de carpetas recursivo
   - [ ] Indexar archivos de música en SQLDelight
   - [ ] Crear sistema de caché de metadatos
   - [ ] Watcher de carpetas para cambios automáticos

**Entregable**: Reproductor funcional que lee y reproduce música local

---

### 🟠 Fase 3: UI de Reproducción (Semana 5-6)
**Objetivo**: Crear interfaz premium de reproducción

#### Tareas:
1. **Now Playing Screen**
   - [ ] Vista de carátula grande con blur background
   - [ ] Barra de progreso interactiva
   - [ ] Controles de reproducción (anterior, play/pause, siguiente)
   - [ ] Display de metadatos (título, artista, álbum)
   - [ ] Botón de favoritos y agregar a playlist

2. **Player Bar Global**
   - [ ] Mini-player persistente en la parte inferior
   - [ ] Carátula miniatura
   - [ ] Controles básicos siempre visibles
   - [ ] Animación de expansión a Now Playing

3. **Cola de Reproducción**
   - [ ] Vista de cola actual
   - [ ] Drag & drop para reordenar
   - [ ] Opciones de modo (normal, repeat, shuffle)

**Entregable**: Experiencia de reproducción completa y fluida

---

### 🔵 Fase 4: Biblioteca y Organización (Semana 7-8)
**Objetivo**: Sistema completo de gestión de música

#### Tareas:
1. **Home Screen**
   - [ ] Grid de álbumes con carátulas
   - [ ] Vista de artistas
   - [ ] Lista de canciones
   - [ ] Filtros y ordenamiento

2. **Búsqueda**
   - [ ] Búsqueda en tiempo real (debounced)
   - [ ] Resultados por categoría (canciones, álbumes, artistas)
   - [ ] Historial de búsquedas

3. **Playlists**
   - [ ] Crear/editar/eliminar playlists
   - [ ] Agregar/quitar canciones
   - [ ] Ordenar canciones en playlist
   - [ ] Playlists inteligentes (más reproducidas, favoritas)

4. **Detalles de Álbum/Artista**
   - [ ] Vista detallada de álbum con tracklist
   - [ ] Vista de artista con discografía
   - [ ] Estadísticas (reproducciones, duración total)

**Entregable**: Biblioteca completa con gestión profesional

---

### 🟣 Fase 5: Streaming de Radios (Semana 9-10) ✅ **COMPLETADA**
**Objetivo**: Integrar 400 radios online

#### Tareas:
1. **Integración de Radios**
   - [x] Crear modelo Radio (nombre, URL, género, país, logo)
   - [x] Parser JSON con 400 radios
   - [x] Almacenar en SQLDelight
   - [x] Streaming con Ktor + VLCJ

2. **UI de Radios**
   - [x] Lista categorizada por género
   - [x] Filtro por país
   - [x] Búsqueda de radios
   - [x] Cards con logo de radio

3. **Reproducción de Radio**
   - [x] Detectar cambio entre local/radio
   - [x] Display de metadata de stream (si disponible)
   - [x] Indicador de buffering
   - [x] Favoritos de radios

4. **Gestión de URLs**
   - [x] Validación de URLs activas
   - [ ] Retry automático en caso de error
   - [ ] Timeout configurable

**Entregable**: Sistema completo de radios integrado ✅

---

### 🟤 Fase 6: Polish & Features Avanzados (Semana 11-12)
**Objetivo**: Pulir experiencia y agregar features premium

#### Tareas:
1. **Animaciones y Transiciones**
   - [ ] Transiciones suaves entre pantallas
   - [ ] Animación de carátulas
   - [ ] Loading states elegantes
   - [ ] Feedback visual premium

2. **Temas**
   - [ ] Modo oscuro completo
   - [ ] Transición suave entre temas
   - [ ] Persistencia de preferencia

3. **Atajos de Teclado**
   - [ ] Space: play/pause
   - [ ] Flechas: siguiente/anterior/seek
   - [ ] Ctrl+F: búsqueda
   - [ ] Media keys support

4. **Configuración**
   - [ ] Selección de carpetas de música
   - [ ] Configuración de audio (buffer, output device)
   - [ ] Preferencias de UI
   - [ ] Exportar/importar configuración

5. **Optimizaciones**
   - [ ] Lazy loading de imágenes
   - [ ] Virtual scrolling para listas grandes
   - [ ] Cache de carátulas
   - [ ] Reducir consumo de memoria

**Entregable**: App lista para uso real con UX pulida

---

### 🔴 Fase 7: Testing & Distribución (Semana 13-14)
**Objetivo**: Preparar para release

#### Tareas:
1. **Testing**
   - [ ] Tests unitarios de lógica de negocio
   - [ ] Tests de reproducción
   - [ ] Tests de base de datos
   - [ ] Testing manual en Windows y Linux

2. **Empaquetado**
   - [ ] Configurar Gradle packaging task
   - [ ] Crear instalador Windows (.exe con Inno Setup)
   - [ ] Crear package Linux (.deb/.AppImage)
   - [ ] Incluir dependencias de VLC

3. **Documentación**
   - [ ] README completo
   - [ ] Guía de usuario
   - [ ] Troubleshooting
   - [ ] Licencias de componentes

4. **Release**
   - [ ] Versioning (semantic versioning)
   - [ ] Changelog
   - [ ] Assets de release (capturas, logo)
   - [ ] Publicar en GitHub Releases

**Entregable**: Aplicación distribuible lista para usuarios

---

## 🔧 Dependencias Principales

### build.gradle.kts (Proyecto)
```kotlin
plugins {
    kotlin("multiplatform") version "2.0.0"
    kotlin("plugin.serialization") version "2.0.0"
    id("org.jetbrains.compose") version "1.6.10"
    id("app.cash.sqldelight") version "2.0.0"
}
```

### Dependencias Clave
```kotlin
// Compose Multiplatform Desktop
implementation(compose.desktop.currentOs)
implementation(compose.material3)
implementation(compose.materialIconsExtended)

// Ktor Client (streaming radios)
implementation("io.ktor:ktor-client-core:2.3.7")
implementation("io.ktor:ktor-client-cio:2.3.7")

// VLCJ (audio playback)
implementation("uk.co.caprica:vlcj:4.8.2")

// SQLDelight (database)
implementation("app.cash.sqldelight:sqlite-driver:2.0.0")
implementation("app.cash.sqldelight:coroutines-extensions:2.0.0")

// Koin (DI)
implementation("io.insert-koin:koin-core:3.5.0")

// Kotlinx Serialization
implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

// JAudioTagger (metadata)
implementation("net.jthink:jaudiotagger:3.0.1")

// Coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.7.3")

// Kotlinx DateTime
implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")
```

---

## 📊 Métricas de Éxito

### Técnicas
- ✅ Reproducción sin lag de archivos locales
- ✅ Streaming estable de radios (< 2s buffering inicial)
- ✅ Base de datos indexa 10,000+ canciones en < 5s
- ✅ Uso de RAM < 250MB en reposo
- ✅ Startup time < 3s

### UX
- ✅ Tiempo de búsqueda < 200ms
- ✅ Transiciones fluidas 60fps
- ✅ Zero crashes en uso normal
- ✅ UI responsive (sin freeze)

---

## 🎯 Features Futuras (Post v1.0)

### Fase 8+
- [ ] Sincronización con servicios cloud
- [ ] Scrobbling a Last.fm
- [ ] Ecualizador visual
- [ ] Letras de canciones
- [ ] Visualizador de espectro
- [ ] Soporte para podcasts
- [ ] Control remoto (móvil como remote)
- [ ] Soporte para macOS
- [ ] Discord Rich Presence
- [ ] Importar playlists de Spotify
- [ ] Mini-mode (ventana compacta)
- [ ] Crossfade entre canciones

---

## 🚨 Riesgos y Mitigaciones

| Riesgo | Probabilidad | Impacto | Mitigación |
|--------|--------------|---------|------------|
| VLC no disponible en sistema | Media | Alto | Bundlear VLC libs con instalador |
| URLs de radios caídas | Alta | Medio | Sistema de validación y fallback |
| Performance con 10k+ canciones | Media | Medio | Virtual scrolling, lazy loading |
| Compatibilidad Linux (distros) | Media | Medio | Testing en Ubuntu, Fedora, Arch |
| Codecs propietarios (AAC) | Baja | Medio | VLC incluye decoders necesarios |

---

## 📝 Notas de Implementación

### Gestión de 400 Radios
Estructura JSON sugerida:
```json
{
  "radios": [
    {
      "id": "radio-001",
      "name": "Jazz FM",
      "url": "https://example.com/stream",
      "genre": "Jazz",
      "country": "US",
      "logoUrl": "https://example.com/logo.png",
      "description": "The best jazz music 24/7",
      "bitrate": 128
    }
  ]
}
```

Cargar en base de datos al primer inicio, permitir actualización manual.

### Escaneo de Biblioteca
- Usar `kotlinx.coroutines.flow` para progreso en tiempo real
- Procesar en background thread
- Caché de checksums para evitar re-scan innecesario
- Soporte para múltiples carpetas raíz

### UI Performance
- `LazyColumn` para listas grandes
- `AsyncImage` con Coil/Kamel para carátulas
- `remember` y `derivedStateOf` para evitar recomposiciones
- `key()` en loops para estabilidad

---

## 🤝 Contribuciones (Futuro)

Este proyecto será de código abierto. Guías de contribución a definir en v1.0.

---

## 📄 Licencia

A definir - Sugerencia: MIT o Apache 2.0

---

**Última actualización**: Noviembre 2025  
**Versión del Roadmap**: 1.0  
**Estado del Proyecto**: 🚀 Planning
