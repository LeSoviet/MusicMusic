# 🎵 MusicMusic

**Un reproductor de música moderno y elegante para Windows y Linux**

MusicMusic es un reproductor de música multiplataforma desarrollado con Kotlin Multiplatform y Compose Desktop. Gestiona tu biblioteca local de música con una interfaz premium minimalista y disfruta de más de 400 estaciones de radio online.

---

## ✨ Features

### 🎧 Reproducción de Audio
- ✅ Soporte para formatos populares: MP3, FLAC, WAV, OGG, AAC, M4A
- ✅ Motor de audio VLCJ de alto rendimiento
- ✅ Control completo: play, pause, seek, volumen
- ✅ Cola de reproducción con shuffle y repeat
- ✅ Ecualizador y efectos de audio

### 📚 Biblioteca Local
- ✅ Escaneo automático de carpetas
- ✅ Detección de metadatos (título, artista, álbum, año)
- ✅ Extracción de carátulas embebidas
- ✅ Organización por álbumes, artistas y canciones
- ✅ Búsqueda instantánea en toda la biblioteca
- ✅ Filtros y ordenamiento avanzado

### 📻 Radios Online ✨ **NUEVO**
- ✅ 400+ estaciones de radio de todo el mundo
- ✅ Categorización por género (Jazz, Rock, Electronic, Classical, etc.)
- ✅ Filtros por país
- ✅ Búsqueda en tiempo real
- ✅ Sistema de favoritos persistente
- ✅ Streaming estable con buffering automático
- ✅ Metadatos de bitrate y ubicación

### 🎨 Interfaz Premium
- ✅ Diseño minimalista moderno
- ✅ Colores suaves y tipografía elegante
- ✅ Modo oscuro y claro
- ✅ Animaciones fluidas
- ✅ Efectos glassmorphism
- ✅ Navigation Rail sidebar
- ✅ Estados de carga elegantes

### 🎹 Playlists
- ✅ Crear y gestionar playlists personalizadas
- ✅ Agregar/quitar canciones fácilmente
- ✅ Ordenar canciones con drag & drop
- ✅ Playlists inteligentes

---

## 📸 Screenshots

_Screenshots próximamente_

---

## 🚀 Instalación

### Windows
1. Descarga el instalador `.exe` desde [Releases](https://github.com/yourusername/MusicMusic/releases)
2. Ejecuta el instalador
3. Sigue las instrucciones del asistente
4. ¡Listo! MusicMusic se abrirá automáticamente

### Linux

#### Debian/Ubuntu (.deb)
```bash
sudo dpkg -i MusicMusic-0.1.0.deb
sudo apt-get install -f  # Instalar dependencias si es necesario
```

#### Otras distribuciones
```bash
# Descargar y descomprimir el archivo
tar -xzf MusicMusic-0.1.0-linux.tar.gz

# Ejecutar
cd MusicMusic-0.1.0
./MusicMusic
```

---

## 🛠️ Desarrollo

### Requisitos
- **JDK 17** o superior
- **Gradle 8.5+** (incluido via wrapper)
- **VLC Media Player** instalado en el sistema
- Git

### Clonar el Repositorio
```bash
git clone https://github.com/yourusername/MusicMusic.git
cd MusicMusic
```

### Compilar y Ejecutar

#### Windows
```powershell
.\gradlew :composeApp:run
```

#### Linux/macOS
```bash
./gradlew :composeApp:run
```

### Crear Distribución

#### Todas las plataformas
```bash
./gradlew :composeApp:packageDistributionForCurrentOS
```

Los archivos se generarán en:
```
composeApp/build/compose/binaries/main/
```

---

## 🏗️ Arquitectura

MusicMusic está construido siguiendo principios de Clean Architecture:

```
┌─────────────────────────────────────────┐
│        UI Layer (Compose)               │
│  Screens │ ViewModels │ Components      │
└─────────────┬───────────────────────────┘
              │
┌─────────────▼───────────────────────────┐
│         Domain Layer                    │
│  Models │ UseCases │ Repositories       │
└─────────────┬───────────────────────────┘
              │
┌─────────────▼───────────────────────────┐
│          Data Layer                     │
│  Repository Impl │ DataSources          │
└─────────────┬───────────────────────────┘
              │
    ┌─────────┴─────────┐
    │                   │
┌───▼────┐      ┌──────▼──────┐
│SQLite  │      │VLCJ Audio   │
│Database│      │Player       │
└────────┘      └─────────────┘
```

### Tecnologías Principales
- **Kotlin Multiplatform**: Código compartido
- **Compose Multiplatform**: UI moderna y declarativa
- **VLCJ**: Motor de reproducción de audio
- **SQLDelight**: Base de datos type-safe
- **Ktor Client**: Streaming de radios
- **Koin**: Inyección de dependencias
- **Coroutines & Flow**: Programación asíncrona

Para más detalles, consulta:
- [ROADMAP.md](ROADMAP.md) - Plan de desarrollo completo
- [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md) - Estructura detallada del proyecto

---

## 📋 Roadmap## 📋 Roadmap

### ✅ Fase 1: Setup & Fundamentos (COMPLETADA)
- [x] Proyecto Kotlin Multiplatform configurado
- [x] Arquitectura base con MVVM
- [x] Sistema de temas premium
- [x] Navegación entre pantallas

### ✅ Fase 2: Motor de Audio (COMPLETADA)
- [x] Integración VLCJ
- [x] Reproducción de audio local
- [x] Lectura de metadatos (JAudioTagger)
- [x] Scanner de biblioteca

### ✅ Fase 3: UI de Reproducción (COMPLETADA)
- [x] Now Playing Screen
- [x] Player Bar global
- [x] Cola de reproducción
- [x] Controles de reproducción

### ✅ Fase 4: Biblioteca y Organización (COMPLETADA)
- [x] Home Screen con álbumes
- [x] Vista de artistas y canciones
- [x] Sistema de búsqueda
- [x] Playlists básicas

### ✅ Fase 5: Streaming de Radios (COMPLETADA) 🆕
- [x] Modelo de datos Radio
- [x] Base de datos SQLDelight
- [x] Repositorio con 20+ radios de ejemplo
- [x] UI de radios con búsqueda y filtros
- [x] Sistema de favoritos
- [x] Integración con AudioPlayer
- [x] Navegación con sidebar

### 🔄 Fase 6: Polish & Features Avanzados (PRÓXIMA)
- [ ] Animaciones y transiciones
- [ ] Modo oscuro completo
- [ ] Atajos de teclado
- [ ] Configuración avanzada
- [ ] Optimizaciones de rendimiento

### 📅 Fase 7: Testing & Distribución
- [ ] Tests unitarios
- [ ] Empaquetado para Windows/Linux
- [ ] Documentación completa
- [ ] Release v1.0.0

Ver [ROADMAP.md](docs/ROADMAP.md) para el plan detallado completo.

---

## 📊 Estado del Proyecto

**Versión Actual**: 0.5.0 (Fase 5 Completada)  
**Estado**: 🚀 En Desarrollo Activo  
**Progreso General**: ████████░░ 75%

### Fases Completadas
- ✅ Fase 1: Setup & Fundamentos
- ✅ Fase 2: Motor de Audio
- ✅ Fase 3: UI de Reproducción
- ✅ Fase 4: Biblioteca y Organización
- ✅ Fase 5: Streaming de Radios

### Documentación
- 📄 [ROADMAP.md](docs/ROADMAP.md) - Plan completo del proyecto
- 📄 [PHASE_1_COMPLETED.md](docs/PHASE_1_COMPLETED.md) - Resumen Fase 1
- 📄 [PHASE_2_COMPLETED.md](docs/PHASE_2_COMPLETED.md) - Resumen Fase 2
- 📄 [PHASE_3_COMPLETED.md](docs/PHASE_3_COMPLETED.md) - Resumen Fase 3
- 📄 [PHASE_4_COMPLETED.md](docs/PHASE_4_COMPLETED.md) - Resumen Fase 4
- 📄 [PHASE_5_COMPLETED.md](docs/PHASE_5_COMPLETED.md) - Resumen Fase 5 ✨
- 📄 [BUILD_GUIDE.md](docs/BUILD_GUIDE.md) - Guía de compilación
- 📄 [TESTING_GUIDE.md](docs/TESTING_GUIDE.md) - Guía de testing

---

### ✅ v0.1.0 (Actual)
- [x] Setup inicial del proyecto
- [x] Configuración Gradle KMP
- [ ] Reproducción de audio local
- [ ] Interfaz básica

### 🚧 v0.2.0 (En desarrollo)
- [ ] Gestión completa de biblioteca
- [ ] Búsqueda avanzada
- [ ] Sistema de playlists

### 🔮 v0.3.0 (Planeado)
- [ ] Integración de 400 radios
- [ ] Modo oscuro completo
- [ ] Atajos de teclado

### 📅 v1.0.0 (Objetivo)
- [ ] Aplicación estable y pulida
- [ ] Testing completo
- [ ] Instaladores para Windows y Linux
- [ ] Documentación completa

Ver [ROADMAP.md](ROADMAP.md) para el plan completo.

---

## 🤝 Contribuir

¡Las contribuciones son bienvenidas! Por favor:

1. Fork el proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

### Guías de Estilo
- Sigue las convenciones de Kotlin
- Documenta funciones públicas
- Escribe tests para nueva funcionalidad
- Mantén los commits atómicos y descriptivos

---

## 🐛 Reportar Bugs

Si encuentras un bug, por favor abre un [Issue](https://github.com/yourusername/MusicMusic/issues) con:
- Descripción del problema
- Pasos para reproducir
- Comportamiento esperado vs actual
- Screenshots (si aplica)
- Sistema operativo y versión

---

## 📄 Licencia

Este proyecto está licenciado bajo la licencia MIT - ver el archivo [LICENSE](LICENSE) para más detalles.

---

## 🙏 Agradecimientos

- [JetBrains](https://www.jetbrains.com/) por Kotlin y Compose Multiplatform
- [VideoLAN](https://www.videolan.org/) por VLC y VLCJ
- [Cash App](https://cashapp.github.io/sqldelight/) por SQLDelight
- Comunidad de Kotlin por el excelente ecosistema

---

## 📬 Contacto

- **GitHub**: [@yourusername](https://github.com/yourusername)
- **Email**: contact@musicmusic.dev
- **Website**: [musicmusic.dev](https://musicmusic.dev)

---

## ⭐ Star History

Si te gusta el proyecto, ¡dale una estrella! ⭐

---

**Hecho con ❤️ usando Kotlin Multiplatform**
