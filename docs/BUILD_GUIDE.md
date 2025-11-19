# 🚀 Guía de Instalación y Build

## Requisitos Previos

### 1. **Java Development Kit (JDK) 17 o superior**
```powershell
# Verificar instalación
java -version

# Descargar desde:
# https://adoptium.net/ (Temurin JDK recomendado)
# o
# https://www.oracle.com/java/technologies/downloads/
```

### 2. **Gradle 8.5+ (se instalará automáticamente con wrapper)**
El proyecto usa Gradle Wrapper, pero necesitas Gradle para inicializarlo por primera vez.

```powershell
# Instalar Gradle con Chocolatey (recomendado para Windows)
choco install gradle

# O descargar manualmente desde: https://gradle.org/install/
```

### 3. **VLC Media Player (OBLIGATORIO)**

MusicMusic usa **VLCJ** (Java bindings para libVLC) para reproducir audio. VLC debe estar instalado en el sistema.

#### 🪟 Windows

**Opción A: Instalador oficial (recomendado)**
1. Descargar desde https://www.videolan.org/vlc/download-windows.html
2. Instalar VLC 3.0.x (64-bit o 32-bit según tu sistema)
3. Usar la ruta de instalación por defecto: `C:\Program Files\VideoLAN\VLC\`

**Opción B: Chocolatey**
```powershell
choco install vlc
```

**Opción C: winget**
```powershell
winget install VideoLAN.VLC
```

**Verificar instalación:**
```powershell
# Verificar que VLC está en PATH
vlc --version

# O verificar manualmente la ruta
Test-Path "C:\Program Files\VideoLAN\VLC\libvlc.dll"
# Debe retornar: True
```

#### 🐧 Linux

**Ubuntu/Debian:**
```bash
sudo apt update
sudo apt install vlc libvlc-dev libvlccore-dev
```

**Fedora/RHEL:**
```bash
sudo dnf install vlc vlc-devel
```

**Arch Linux:**
```bash
sudo pacman -S vlc
```

**Verificar instalación:**
```bash
vlc --version
ldconfig -p | grep vlc  # Debe mostrar libvlc.so
```

#### 🍎 macOS

**Opción A: Homebrew (recomendado)**
```bash
brew install --cask vlc
```

**Opción B: Instalador oficial**
1. Descargar desde https://www.videolan.org/vlc/download-macosx.html
2. Arrastrar VLC.app a `/Applications/`

**Verificar instalación:**
```bash
/Applications/VLC.app/Contents/MacOS/VLC --version
```

#### ⚠️ Notas Importantes sobre VLC

1. **Versión compatible:** VLC 3.0.x (VLCJ 4.8.x no soporta VLC 4.0+ aún)
2. **Arquitectura:** La arquitectura de VLC debe coincidir con la de Java:
   - Java 64-bit → VLC 64-bit
   - Java 32-bit → VLC 32-bit
3. **Variables de entorno (opcional):**
   ```powershell
   # Windows - Si VLC no está en la ruta por defecto
   $env:VLCJ_PLUGIN_PATH="C:\Path\To\VLC\plugins"
   
   # Linux/macOS
   export VLCJ_PLUGIN_PATH="/usr/lib/vlc/plugins"
   ```

---

## 🔧 Setup del Proyecto

### Paso 1: Inicializar Gradle Wrapper
```powershell
cd c:\Users\LeSoviet\Documents\GitHub\MusicMusic
gradle wrapper --gradle-version 8.5
```

### Paso 2: Compilar el proyecto
```powershell
# Windows
.\gradlew build

# Linux/Mac
./gradlew build
```

### Paso 3: Ejecutar la aplicación
```powershell
# Windows
.\gradlew run

# Linux/Mac
./gradlew run
```

---

## 🏃 Comandos Útiles

### Compilar sin tests
```powershell
.\gradlew build -x test
```

### Limpiar y compilar
```powershell
.\gradlew clean build
```

### Ejecutar en modo debug
```powershell
.\gradlew run --debug
```

### Crear ejecutable nativo (distribución)
```powershell
# Windows .exe
.\gradlew packageDistributionForCurrentOS

# El instalador estará en:
# composeApp/build/compose/binaries/main/app/MusicMusic/
```

### Ver dependencias
```powershell
.\gradlew dependencies
```

---

## 📦 Estructura del Build

```
MusicMusic/
├── composeApp/
│   ├── build/
│   │   ├── classes/           # Clases compiladas
│   │   ├── compose/
│   │   │   └── binaries/      # Ejecutables nativos
│   │   └── libs/              # JARs generados
│   └── src/
├── build.gradle.kts           # Configuración raíz
├── settings.gradle.kts        # Settings de Gradle
└── gradle/
    └── wrapper/               # Gradle Wrapper files
```

---

## 🐛 Troubleshooting

### Error: "Cannot find Java"
- Instala JDK 17+ y configura `JAVA_HOME`:
```powershell
$env:JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-17.0.9.9-hotspot"
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
```

### Error: "VLC not found" o "Unable to load library 'libvlc'"

**Causa:** VLCJ no puede encontrar las bibliotecas de VLC.

**Soluciones:**

#### Windows:
1. **Verificar que VLC está instalado:**
   ```powershell
   Test-Path "C:\Program Files\VideoLAN\VLC\libvlc.dll"
   ```

2. **Agregar VLC al PATH del sistema:**
   ```powershell
   # Temporal (solo sesión actual)
   $env:PATH += ";C:\Program Files\VideoLAN\VLC"
   
   # Permanente (requiere reiniciar)
   [Environment]::SetEnvironmentVariable("Path", $env:Path + ";C:\Program Files\VideoLAN\VLC", "Machine")
   ```

3. **Especificar ruta manualmente en código (last resort):**
   Agregar antes de `startKoin`:
   ```kotlin
   System.setProperty("jna.library.path", "C:\\Program Files\\VideoLAN\\VLC")
   ```

#### Linux:
1. **Verificar libvlc:**
   ```bash
   ldconfig -p | grep libvlc
   # Debe mostrar: libvlc.so.5 -> /usr/lib/x86_64-linux-gnu/libvlc.so.5
   ```

2. **Instalar paquetes de desarrollo:**
   ```bash
   # Ubuntu/Debian
   sudo apt install libvlc-dev libvlccore-dev
   
   # Fedora
   sudo dnf install vlc-devel
   ```

3. **Configurar LD_LIBRARY_PATH:**
   ```bash
   export LD_LIBRARY_PATH=/usr/lib/vlc:$LD_LIBRARY_PATH
   ```

#### macOS:
1. **Verificar VLC.app:**
   ```bash
   ls /Applications/VLC.app/Contents/MacOS/lib/
   # Debe contener: libvlc.dylib, libvlccore.dylib
   ```

2. **Configurar DYLD_LIBRARY_PATH:**
   ```bash
   export DYLD_LIBRARY_PATH=/Applications/VLC.app/Contents/MacOS/lib:$DYLD_LIBRARY_PATH
   ```

### Error: "Failed to initialize libvlc"

**Causas comunes:**
1. **Arquitectura incompatible:** Java 64-bit con VLC 32-bit (o viceversa)
   ```powershell
   # Verificar arquitectura de Java
   java -version
   # Buscar "64-Bit" en la salida
   
   # Reinstalar VLC con la arquitectura correcta
   ```

2. **VLC 4.0 instalado:** VLCJ 4.8.x solo soporta VLC 3.0.x
   ```powershell
   # Desinstalar VLC 4.0
   # Instalar VLC 3.0.x desde: https://www.videolan.org/vlc/releases/3.0.20.html
   ```

3. **Plugins de VLC corruptos:**
   ```powershell
   # Windows - Eliminar caché de plugins
   Remove-Item "$env:APPDATA\vlc\plugins-cache" -Force
   
   # Linux
   rm -rf ~/.cache/vlc
   
   # Reiniciar VLC una vez para regenerar caché
   vlc --reset-plugins-cache
   ```

### Error: "No audio output" o "Audio crackling"

**Soluciones:**
1. **Cambiar módulo de salida de audio en VLC:**
   ```
   VLC > Tools > Preferences > Audio > Output
   - Windows: DirectSound o WaveOut
   - Linux: ALSA o PulseAudio
   - macOS: CoreAudio
   ```

2. **Verificar dispositivos de audio:**
   ```kotlin
   // En VlcjAudioPlayer, agregar logging:
   println("Available audio outputs: ${mediaPlayer.audio().outputDevices()}")
   ```

### Error: "Gradle version incompatible"
- Usa Gradle 8.5+:
```powershell
gradle wrapper --gradle-version 8.5
```

### Build muy lento
- Habilita Gradle daemon:
```properties
# En gradle.properties
org.gradle.daemon=true
org.gradle.parallel=true
org.gradle.caching=true
```

### Error: "Out of memory" durante build
```properties
# En gradle.properties
org.gradle.jvmargs=-Xmx4g -XX:MaxMetaspaceSize=512m
```

---

## 🎯 Próximos Pasos

1. **Instalar JDK 17+**
2. **Instalar Gradle** (o usar IntelliJ IDEA que lo incluye)
3. **Ejecutar `gradle wrapper`**
4. **Ejecutar `.\gradlew run`**
5. **Probar la aplicación** 🎉

---

## 📚 Alternativa: Usar IntelliJ IDEA

Si tienes IntelliJ IDEA instalado:

1. **Abrir proyecto**: `File > Open > seleccionar carpeta MusicMusic`
2. **Esperar a que importe**: IntelliJ configurará Gradle automáticamente
3. **Ejecutar**: Click derecho en `Main.kt` > Run 'MainKt'

IntelliJ IDEA Community Edition es gratuito: https://www.jetbrains.com/idea/download/

---

## ✅ Verificación Rápida

Una vez que tengas Java, Gradle y VLC instalados:

```powershell
# 1. Verificar Java
java -version
# Output esperado: openjdk version "17.x.x" o superior

# 2. Verificar Gradle
gradle --version
# Output esperado: Gradle 8.5 o superior

# 3. Verificar VLC
vlc --version
# Output esperado: VLC media player 3.0.x

# 4. Verificar libvlc (Windows)
Test-Path "C:\Program Files\VideoLAN\VLC\libvlc.dll"
# Output esperado: True

# 5. Inicializar wrapper
gradle wrapper

# 6. Compilar
.\gradlew build

# 7. Ejecutar
.\gradlew run
```

Si todos los pasos funcionan, ¡ya puedes desarrollar! 🚀

---

## 🎵 Primera Ejecución

Al ejecutar MusicMusic por primera vez:

1. **Selecciona tu carpeta de música:**
   - Ir a Settings (icono de engranaje)
   - Click en "Select Music Folder"
   - Elegir carpeta con archivos MP3/FLAC/etc.

2. **Espera el escaneo inicial:**
   - La app escaneará todos los archivos de audio
   - Extraerá metadata (título, artista, álbum, etc.)
   - Puede tomar unos minutos dependiendo del tamaño de tu biblioteca

3. **Disfruta tu música:**
   - Las canciones aparecerán en Library
   - Puedes buscar, filtrar por artista/álbum
   - Reproducir, pausar, cambiar volumen, etc.

---