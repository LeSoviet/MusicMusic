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

### 3. **VLC Media Player (solo para desarrollo)**
```powershell
# Descargar desde: https://www.videolan.org/vlc/
# La versión runtime se incluirá en el build final
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

### Error: "VLC not found"
- Instala VLC Media Player 3.x
- Asegúrate que esté en PATH o en `C:\Program Files\VideoLAN\VLC\`

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

Una vez que tengas Java y Gradle instalados:

```powershell
# 1. Verificar Java
java -version
# Output esperado: openjdk version "17.x.x" o superior

# 2. Verificar Gradle
gradle --version
# Output esperado: Gradle 8.5 o superior

# 3. Inicializar wrapper
gradle wrapper

# 4. Compilar
.\gradlew build

# 5. Ejecutar
.\gradlew run
```

Si todos los pasos funcionan, ¡ya puedes desarrollar! 🚀
