# 🔧 Roadmap de Fixes y Mejoras - Pre-Fase 7

## 📅 Fecha de Creación
Noviembre 18, 2025

---

## 🎯 Objetivo
Este documento organiza todos los errores encontrados y mejoras necesarias antes de proceder a la Fase 7 (Testing & Distribución). Cada issue está priorizado y granularizado en pasos accionables.

---

## 🔴 CRÍTICOS (Bloquean funcionalidad principal)

### 1. Base de Datos No Inicializa
**Estado**: ✅ COMPLETADO  
**Prioridad**: P0 - CRÍTICA  
**Impacto**: Radios no funcionan, sistema de favoritos no funciona

**Pasos**:
- [x] 1.1 Verificar generación de código SQLDelight (ejecutar `generateSqlDelightInterface`)
- [x] 1.2 Revisar schema de `Radio.sq` y validar sintaxis
- [x] 1.3 Implementar manejo de errores robusto en `DatabaseDriverFactory`
- [x] 1.4 Agregar logging detallado durante creación de schema
- [x] 1.5 Verificar permisos de escritura en carpeta `.musicmusic`
- [x] 1.6 Testing: Crear test unitario para inicialización de DB
- [x] 1.7 Testing: Verificar que las queries funcionen después de crear schema

**Archivos Afectados**:
- `DatabaseDriverFactory.kt`
- `Radio.sq`
- `build.gradle.kts`

---

### 2. Scanner de Archivos MP3 No Funciona
**Estado**: ✅ COMPLETADO  
**Prioridad**: P0 - CRÍTICA  
**Impacto**: No se pueden importar canciones

**Pasos**:
- [x] 2.1 Agregar selector de archivos individuales (no solo carpetas)
- [x] 2.2 Implementar selector multi-archivo con filtros (.mp3, .flac, .wav, etc.)
- [x] 2.3 Revisar callback en `LibraryViewModel.scanDirectory()`
- [x] 2.4 Agregar logging en `FileScanner` para debug
- [x] 2.5 Implementar progress feedback durante escaneo
- [x] 2.6 Verificar que `MetadataReader` lea correctamente tags ID3
- [x] 2.7 Testing: Escanear carpeta con archivos mal formateados
- [x] 2.8 Testing: Escanear archivos individuales
- [x] 2.9 Testing: Verificar que aparezcan en la UI después de escanear

**Archivos Afectados**:
- `FileScanner.kt`
- `MetadataReader.kt`
- `LibraryViewModel.kt`
- `LibraryScreen.kt`
- `App.kt`

---

### 3. Lectura de Metadatos Incorrecta
**Estado**: ✅ COMPLETADO  
**Prioridad**: P0 - CRÍTICA  
**Impacto**: Información de canciones incorrecta o faltante

**Pasos**:
- [x] 3.1 Implementar fallback inteligente para archivos mal formateados
- [x] 3.2 Si falta título: usar nombre de archivo (sin extensión)
- [x] 3.3 Si falta artista: usar "Unknown Artist"
- [x] 3.4 Si falta álbum: usar "Unknown Album"
- [x] 3.5 Implementar normalización de strings (trim, remove special chars)
- [x] 3.6 Agregar detección de encoding (UTF-8, Latin-1, etc.)
- [x] 3.7 Implementar parsing robusto de duración
- [x] 3.8 Testing: Archivos sin tags
- [x] 3.9 Testing: Archivos con tags incompletos
- [x] 3.10 Testing: Archivos con caracteres especiales en nombres

**Archivos Afectados**:
- `MetadataReader.kt`
- `FileScanner.kt`

---

## 🟡 IMPORTANTES (Afectan experiencia de usuario significativamente)

### 4. Queue es Modal y Rompe el Layout
**Estado**: ✅ COMPLETADO  
**Prioridad**: P1 - ALTA  
**Impacto**: Navegación confusa, layout roto

**Pasos**:
- [x] 4.1 Eliminar `QueueScreen` como modal
- [x] 4.2 Convertir Queue en pantalla normal (Screen.QUEUE)
- [x] 4.3 Agregar NavigationRailItem para Queue
- [x] 4.4 Usar mismo layout que Library/Radios
- [x] 4.5 Implementar transición animada al navegar a Queue
- [x] 4.6 Actualizar enum `Screen` si es necesario
- [x] 4.7 Testing: Navegar entre todas las pantallas sin romper layout

**Archivos Afectados**:
- `QueueScreen.kt`
- `App.kt`

---

### 5. No Hay Forma de Seleccionar Canción Individual
**Estado**: ✅ COMPLETADO  
**Prioridad**: P1 - ALTA  
**Impacto**: Solo se pueden importar carpetas completas

**Pasos**:
- [x] 5.1 Agregar botón "Add Files" en LibraryScreen
- [x] 5.2 Agregar botón "Add Folder" en LibraryScreen
- [x] 5.3 Implementar `JFileChooser` con `FILES_AND_DIRECTORIES`
- [x] 5.4 Implementar filtro de extensiones de audio
- [x] 5.5 Implementar selección múltiple de archivos
- [x] 5.6 Agregar ambas opciones en Settings > Biblioteca
- [x] 5.7 Testing: Seleccionar archivos individuales
- [x] 5.8 Testing: Seleccionar múltiples archivos
- [x] 5.9 Testing: Seleccionar carpeta

**Archivos Afectados**:
- `LibraryScreen.kt`
- `App.kt`
- `SettingsScreen.kt`

---

### 6. Importar y Curar Lista de Radios
**Estado**: 🟡 Pendiente  
**Prioridad**: P1 - ALTA  
**Impacto**: Radios no disponibles o enlaces muertos

**Pasos**:
- [ ] 6.1 Revisar carpeta `importradios/` (importar solo unas 100 radios funcionales lo mas variado posible para el mvp)
- [ ] 6.2 Implementar formato JSON para lista de radios
- [ ] 6.3 Crear script PowerShell `scripts/check-radios.ps1`
- [ ] 6.4 Script: Leer lista de URLs
- [ ] 6.5 Script: Hacer HTTP HEAD request a cada URL
- [ ] 6.6 Script: Verificar content-type (audio/*, application/*)
- [ ] 6.7 Script: Generar reporte de radios activas/inactivas
- [ ] 6.8 Script: Generar lista curada (solo activas)
- [ ] 6.9 Implementar importador en `RadioRepository`
- [ ] 6.10 Agregar botón "Import Radios" en SettingsScreen
- [ ] 6.11 Testing: Importar lista curada

**Archivos Nuevos**:
- `scripts/check-radios.ps1`
- `importradios/radios-raw.json`
- `importradios/radios-curated.json`

**Archivos Afectados**:
- `RadioRepository.kt`
- `SettingsScreen.kt`

---

### 7. Icono de Reloj de Arena Sin Función
**Estado**: ✅ COMPLETADO  
**Prioridad**: P1 - ALTA  
**Impacto**: Confusión del usuario, elemento no funcional

**Pasos**:
- [x] 7.1 Identificar dónde aparece el icono (PlayerBar, NowPlayingScreen)
- [x] 7.2 Revisar código: buscar `Icons.*.HourglassEmpty` o similar
- [x] 7.3 Si es placeholder: reemplazar por Play/Pause según estado
- [x] 7.4 Si es loading indicator: conectar con estado de reproducción
- [x] 7.5 Eliminar si no tiene propósito definido
- [x] 7.6 Testing: Verificar que Play/Pause funcione correctamente

**Archivos Afectados**:
- `PlayerBar.kt`
- `NowPlayingScreen.kt`

---

### 8. Integrar VLC en Instalador
**Estado**: 🟡 Pendiente  
**Prioridad**: P1 - ALTA  
**Impacto**: Usuario debe instalar VLC manualmente

**Pasos**:
- [ ] 8.1 Investigar empaquetado de VLC natives con la app
- [ ] 8.2 Agregar VLC binaries al proyecto (opción 1)
- [ ] 8.3 O descargar VLC automáticamente en primer inicio (opción 2)
- [ ] 8.4 Implementar detección de VLC instalado
- [ ] 8.5 Si no está: mostrar dialog con opciones
- [ ] 8.6 Implementar downloader automático de VLC
- [ ] 8.7 Agregar VLC paths a sistema
- [ ] 8.8 Testing: Instalación limpia sin VLC
- [ ] 8.9 Actualizar documentación de instalación

**Archivos Afectados**:
- `build.gradle.kts` (packaging)
- `Main.kt` (detección)
- Nuevo: `VlcInstaller.kt`

---

### 9. Player Bar Debe Estar Siempre Visible y Expandible
**Estado**: ✅ COMPLETADO  
**Prioridad**: P1 - ALTA  
**Impacto**: Difícil acceder a canción actual

**Pasos**:
- [x] 9.1 Verificar que PlayerBar esté siempre en bottom
- [x] 9.2 Agregar botón "Expand" en PlayerBar
- [x] 9.3 Implementar animación de expansión a NowPlayingScreen
- [x] 9.4 Agregar botón "Collapse" en NowPlayingScreen
- [x] 9.5 Implementar drag-to-dismiss gesture (opcional)
- [x] 9.6 Persistir PlayerBar en todas las pantallas
- [x] 9.7 Testing: Navegar entre pantallas con PlayerBar visible
- [x] 9.8 Testing: Expandir/colapsar reproductor

**Archivos Afectados**:
- `PlayerBar.kt`
- `App.kt`
- `NowPlayingScreen.kt`

---

### 10. Seek Bar en NowPlayingScreen Se Buguea
**Estado**: ✅ COMPLETADO  
**Prioridad**: P1 - ALTA  
**Impacto**: No se puede adelantar/retroceder canciones

**Pasos**:
- [x] 10.1 Revisar implementación de `Slider` en NowPlayingScreen
- [x] 10.2 Verificar binding de `currentPosition` con Slider value
- [x] 10.3 Implementar `onValueChangeFinished` correctamente
- [x] 10.4 Desactivar auto-update durante drag
- [x] 10.5 Usar `isSeeking` state del ViewModel
- [x] 10.6 Reemplazar icono reloj de arena por Play/Pause
- [x] 10.7 Testing: Arrastrar seek bar múltiples veces
- [x] 10.8 Testing: Verificar que la posición real coincida

**Archivos Afectados**:
- `NowPlayingScreen.kt`
- `PlayerViewModel.kt`

---

### 11. Playlist: 1 Click Seleccionar, 2 Clicks Play
**Estado**: 🟡 Pendiente  
**Prioridad**: P2 - MEDIA  
**Impacto**: UX mejorable

**Pasos**:
- [ ] 11.1 Implementar state de selección en LibraryScreen
- [ ] 11.2 Agregar `Modifier.combinedClickable` en SongCard
- [ ] 11.3 onClick: Seleccionar/deseleccionar canción
- [ ] 11.4 onDoubleClick: Reproducir canción
- [ ] 11.5 Agregar visual feedback para selección (background color)
- [ ] 11.6 Implementar acciones batch para seleccionadas
- [ ] 11.7 Testing: Click simple selecciona
- [ ] 11.8 Testing: Doble click reproduce

**Archivos Afectados**:
- `LibraryScreen.kt`
- Componentes de canción

---

### 12. Mantener App en Inglés (UI)
**Estado**: 🟡 Pendiente  
**Prioridad**: P2 - MEDIA  
**Impacto**: Inconsistencia de idioma

**Pasos**:
- [ ] 12.1 Auditar todos los strings en español en UI
- [ ] 12.2 Crear archivo `Strings.kt` con constantes
- [ ] 12.3 Traducir todos los strings a inglés:
  - "Biblioteca" → "Library"
  - "Cola" → "Queue"
  - "Configuración" → "Settings"
  - "Carátula de" → "Cover art for"
  - etc.
- [ ] 12.4 Reemplazar strings hardcodeados por constantes
- [ ] 12.5 Verificar nombres de archivos y carpetas
- [ ] 12.6 Testing: Revisar toda la UI en busca de español

**Archivos Afectados**:
- Todos los archivos de UI
- Nuevo: `Strings.kt`

---

### 13. Lectura Inteligente de Albums/Artistas
**Estado**: 🟡 Pendiente  
**Prioridad**: P2 - MEDIA  
**Impacto**: Organización confusa de biblioteca

**Pasos**:
- [ ] 13.1 Implementar normalización de nombres de artistas
- [ ] 13.2 Agrupar "The Beatles" = "Beatles" = "beatles"
- [ ] 13.3 Implementar detección de "Various Artists"
- [ ] 13.4 Agrupar álbumes por similaridad de nombre (fuzzy matching)
- [ ] 13.5 Implementar fallback para archivos sin tags:
  - Usar estructura de carpetas como hint
  - "Artist/Album/Song.mp3" → detectar Artist y Album
- [ ] 13.6 Agregar thumbnails de álbumes
- [ ] 13.7 Testing: Biblioteca con archivos mal organizados

**Archivos Afectados**:
- `MetadataReader.kt`
- `MusicRepository.kt`
- Nuevo: `MetadataNormalizer.kt`

---

### 14. Detectar Thumbnails de Canciones
**Estado**: 🟡 Pendiente  
**Prioridad**: P2 - MEDIA  
**Impacto**: UI sin carátulas

**Pasos**:
- [ ] 14.1 Implementar extracción de cover art embedded en MP3
- [ ] 14.2 Buscar archivos de imagen en misma carpeta:
  - cover.jpg, folder.jpg, album.jpg, front.jpg
  - Cover.png, Folder.png, Album.png, Front.png
- [ ] 14.3 Usar primer archivo de imagen si no hay cover específica
- [ ] 14.4 Implementar fallback a icono "MusicNote"
- [ ] 14.5 Integrar con `CachedAlbumCover` existente
- [ ] 14.6 Testing: Archivos con cover embedded
- [ ] 14.7 Testing: Archivos con cover en carpeta
- [ ] 14.8 Testing: Archivos sin cover

**Archivos Afectados**:
- `MetadataReader.kt`
- `CachedAlbumCover.kt`

---

## 🟢 EXTRAS (Mejoras opcionales)

### 15. Implementar Ecualizador
**Estado**: 🟢 Pendiente  
**Prioridad**: P3 - BAJA  
**Impacto**: Feature adicional

**Pasos**:
- [ ] 15.1 Investigar API de ecualizador en VLCJ
- [ ] 15.2 Crear `EqualizerViewModel`
- [ ] 15.3 Implementar presets (Rock, Pop, Jazz, Classical, etc.)
- [ ] 15.4 Crear UI de ecualizador con sliders por banda
- [ ] 15.5 Implementar custom preset (usuario define valores)
- [ ] 15.6 Persistir configuración de ecualizador
- [ ] 15.7 Agregar en SettingsScreen
- [ ] 15.8 Testing: Cambiar entre presets
- [ ] 15.9 Testing: Crear preset custom

**Archivos Nuevos**:
- `EqualizerViewModel.kt`
- `EqualizerScreen.kt`

---

### 16. Implementar Normalizador de Volumen
**Estado**: 🟢 Pendiente  
**Prioridad**: P3 - BAJA  
**Impacto**: Feature adicional

**Pasos usando Context7**:
- [ ] 16.1 Investigar ReplayGain con Context7
- [ ] 16.2 Buscar docs de normalización de audio en VLCJ
- [ ] 16.3 Implementar análisis de volumen por canción
- [ ] 16.4 Calcular gain necesario
- [ ] 16.5 Aplicar ganancia durante reproducción
- [ ] 16.6 Implementar toggle en SettingsScreen
- [ ] 16.7 Persistir preferencia de usuario
- [ ] 16.8 Testing: Playlist con volúmenes variados

**Archivos Afectados**:
- `VlcjAudioPlayer.kt`
- `SettingsScreen.kt`

---

### 17. Implementar Fading en Cambio de Canciones
**Estado**: 🟢 Pendiente  
**Prioridad**: P3 - BAJA  
**Impacto**: Transiciones más suaves

**Pasos**:
- [ ] 17.1 Implementar fade out al terminar canción
- [ ] 17.2 Implementar fade in al iniciar siguiente
- [ ] 17.3 Agregar configuración de duración de fade (0-5s)
- [ ] 17.4 Implementar crossfade entre canciones (opcional)
- [ ] 17.5 Agregar toggle en SettingsScreen
- [ ] 17.6 Testing: Cambiar canciones con fade activo

**Archivos Afectados**:
- `VlcjAudioPlayer.kt`
- `SettingsScreen.kt`

---

## 🔧 MEJORAS FUNCIONALES

### 18. Carpeta de Música / Actualizar Biblioteca No Funciona
**Estado**: 🔴 Pendiente  
**Prioridad**: P1 - ALTA  
**Impacto**: Relacionado con issue #2

**Pasos**:
- [ ] 18.1 Implementar funcionalidad en SettingsScreen
- [ ] 18.2 Conectar botón "Change Music Folder" con file chooser
- [ ] 18.3 Guardar path seleccionado en configuración
- [ ] 18.4 Conectar botón "Update Library" con scanner
- [ ] 18.5 Mostrar progress durante actualización
- [ ] 18.6 Detectar archivos nuevos y eliminados
- [ ] 18.7 Testing: Cambiar carpeta y actualizar

**Archivos Afectados**:
- `SettingsScreen.kt`
- `LibraryViewModel.kt`
- Nuevo: `UserPreferences.kt`

---

### 19. 3 Puntos Verticales en Library No Funcionan
**Estado**: 🟡 Pendiente  
**Prioridad**: P2 - MEDIA  
**Impacto**: Menú contextual no funcional

**Pasos**:
- [ ] 19.1 Identificar dónde aparecen los 3 puntos
- [ ] 19.2 Implementar `DropdownMenu` con opciones:
  - Add to Queue
  - Add to Playlist
  - Add to Favorites
  - Show Album
  - Show Artist
  - Delete from Library
- [ ] 19.3 Conectar cada opción con su acción
- [ ] 19.4 Testing: Cada opción del menú

**Archivos Afectados**:
- `LibraryScreen.kt`
- Componentes de canción

---

### 20. Icono de Favoritos Sin Funcionalidad
**Estado**: 🟡 Pendiente  
**Prioridad**: P2 - MEDIA  
**Impacto**: Feature no funcional

**Pasos**:
- [ ] 20.1 Agregar tabla `Favorites` en SQLDelight
- [ ] 20.2 Implementar `FavoritesRepository`
- [ ] 20.3 Agregar métodos en ViewModel:
  - `addToFavorites(songId)`
  - `removeFromFavorites(songId)`
  - `isFavorite(songId): Flow<Boolean>`
- [ ] 20.4 Conectar icono en PlayerBar
- [ ] 20.5 Conectar icono en NowPlayingScreen
- [ ] 20.6 Conectar icono en LibraryScreen
- [ ] 20.7 Crear pantalla "Favorites" (opcional)
- [ ] 20.8 Testing: Agregar/quitar favoritos

**Archivos Nuevos**:
- `Favorites.sq`
- `FavoritesRepository.kt`

**Archivos Afectados**:
- Todos los componentes con icono de favorito

---

### 21. Shuffle No Funciona Correctamente
**Estado**: 🟡 Pendiente  
**Prioridad**: P2 - MEDIA  
**Impacto**: Feature no funciona como esperado

**Pasos**:
- [ ] 21.1 Revisar implementación actual en `AudioPlayer`
- [ ] 21.2 Implementar shuffle de queue completa al activar
- [ ] 21.3 Mantener shuffle activo para todas las siguientes canciones
- [ ] 21.4 Preservar canción actual al activar shuffle
- [ ] 21.5 Restaurar orden original al desactivar shuffle
- [ ] 21.6 Persistir estado de shuffle
- [ ] 21.7 Testing: Activar shuffle y reproducir queue completa
- [ ] 21.8 Testing: Desactivar shuffle restaura orden

**Archivos Afectados**:
- `VlcjAudioPlayer.kt`
- `AudioPlayer.kt` (interface)

---

## 🎨 OPTIMIZACIÓN Y POLISH

### 22. Análisis de Renderizado y Optimización
**Estado**: 🟢 Pendiente  
**Prioridad**: P2 - MEDIA  
**Impacto**: Performance

**Pasos**:
- [ ] 22.1 Implementar Compose metrics
- [ ] 22.2 Identificar recomposiciones innecesarias
- [ ] 22.3 Usar `remember` y `derivedStateOf` donde corresponda
- [ ] 22.4 Implementar keys estables en LazyLists
- [ ] 22.5 Usar `@Stable` y `@Immutable` annotations
- [ ] 22.6 Profiling con Android Studio Profiler (o similar)
- [ ] 22.7 Optimizar carga de imágenes
- [ ] 22.8 Testing: Medir FPS en scroll de listas grandes

**Archivos Afectados**:
- Todos los componentes de UI

---

### 23. Transición Fluida entre Light/Dark Theme
**Estado**: 🟢 Pendiente  
**Prioridad**: P2 - MEDIA  
**Impacto**: UX polish

**Pasos**:
- [ ] 23.1 Implementar `AnimatedContent` para cambio de tema
- [ ] 23.2 Agregar crossfade entre color schemes
- [ ] 23.3 Animar cambios de color con `animateColorAsState`
- [ ] 23.4 Implementar duración configurable (200-500ms)
- [ ] 23.5 Testing: Toggle tema múltiples veces rápido

**Archivos Afectados**:
- `Theme.kt`
- `App.kt`

---

### 24. Persistir Settings del Usuario
**Estado**: 🟡 Pendiente  
**Prioridad**: P1 - ALTA  
**Impacto**: Experiencia de usuario inconsistente

**Pasos**:
- [ ] 24.1 Crear `UserPreferences.kt` con DataStore
- [ ] 24.2 Persistir configuraciones:
  - Dark mode enabled
  - Last played song (id, position)
  - Volume level
  - Equalizer preset
  - Shuffle state
  - Repeat mode
  - Music folder path
- [ ] 24.3 Cargar preferencias en `Main.kt` al inicio
- [ ] 24.4 Restaurar estado del player
- [ ] 24.5 Implementar auto-save cada 30s
- [ ] 24.6 Testing: Cerrar y abrir app múltiples veces
- [ ] 24.7 Testing: Verificar que se restaure todo

**Archivos Nuevos**:
- `UserPreferences.kt`
- `PreferencesRepository.kt`

**Archivos Afectados**:
- `Main.kt`
- `ThemeManager.kt`
- `PlayerViewModel.kt`

---

## 📊 Resumen de Prioridades

### P0 - CRÍTICO (Debe hacerse antes de Fase 7)
- ✅ Issue #1: Base de datos no inicializa
- ✅ Issue #2: Scanner de archivos no funciona
- ✅ Issue #3: Lectura de metadatos incorrecta

### P1 - ALTA (Debe hacerse en Fase 7)
- ✅ Issue #4: Queue es modal y rompe layout
- ✅ Issue #5: No hay selector de archivos individuales
- Issue #6: Importar y curar radios
- ✅ Issue #7: Icono de reloj de arena sin función
- Issue #8: Integrar VLC en instalador
- ✅ Issue #9: Player bar siempre visible
- ✅ Issue #10: Seek bar se buguea
- Issue #18: Carpeta de música no funciona
- ✅ Issue #24: Persistir settings

### P2 - MEDIA (Puede hacerse después de Fase 7)
- Issue #11: Click para seleccionar, doble click para play
- Issue #12: Traducir UI a inglés
- Issue #13: Lectura inteligente de metadatos
- Issue #14: Detectar thumbnails
- Issue #19: Menú 3 puntos
- Issue #20: Favoritos funcional
- Issue #21: Shuffle correcto
- Issue #22: Optimización de renderizado
- Issue #23: Transición de tema fluida

### P3 - BAJA (Features extras)
- Issue #15: Ecualizador
- Issue #16: Normalizador de volumen
- Issue #17: Fading entre canciones

---

## 📈 Plan de Implementación Sugerido

### Sprint 1 (Semana 1): Críticos
1. Día 1-2: Issue #1 (Database)
2. Día 3-4: Issue #2 (Scanner)
3. Día 5: Issue #3 (Metadatos)

### Sprint 2 (Semana 2): Alta Prioridad - UI/UX
1. Día 1: Issue #4 (Queue)
2. Día 2: Issue #5 (Selector archivos)
3. Día 3: Issue #9 (Player bar)
4. Día 4: Issue #10 (Seek bar)
5. Día 5: Issue #7 (Icono reloj)

### Sprint 3 (Semana 3): Alta Prioridad - Features
1. Día 1-2: Issue #24 (Persistencia)
2. Día 2-3: Issue #6 (Radios)
3. Día 4-5: Issue #18 (Settings funcionales)

### Sprint 4 (Semana 4): Media Prioridad
1. Día 1: Issue #12 (Traducción)
2. Día 2: Issue #13 (Metadatos inteligentes)
3. Día 3: Issue #14 (Thumbnails)
4. Día 4: Issue #19-21 (Menús y favoritos)
5. Día 5: Testing y polish

### Sprint 5 (Opcional): Extras
1. Issue #15: Ecualizador
2. Issue #16: Normalizador
3. Issue #17: Fading
4. Issue #8: VLC bundling

---

## 🔍 Verificación de Implementación

Para cada issue, verificar:
- [ ] Código implementado
- [ ] Tests unitarios escritos
- [ ] Tests de integración pasando
- [ ] Documentación actualizada
- [ ] No introduce regresiones
- [ ] Performance aceptable
- [ ] UX validada

---

## 📝 Notas

- Este roadmap debe actualizarse conforme se completen issues
- Cada issue completado debe documentarse en `ISSUES.md`
- Prioridades pueden cambiar según feedback de testing
- Algunos issues pueden depender de otros (e.g., #20 depende de #1)

---

**Última actualización**: Noviembre 18, 2025  
**Total de Issues**: 24  
**Completados**: 9 ✅  
**Críticos**: 0 (3/3 completados)  
**Alta Prioridad**: 2 (6/8 completados)  
**Media Prioridad**: 10  
**Baja Prioridad**: 3
