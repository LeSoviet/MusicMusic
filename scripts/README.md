# 🎵 Scripts de MusicMusic

Scripts de PowerShell para facilitar el desarrollo y ejecución del proyecto.

## 📋 Scripts Disponibles

### 🚀 `run.ps1`
Ejecuta la aplicación MusicMusic directamente.

```powershell
.\scripts\run.ps1
```

### 🔨 `build.ps1`
Limpia y compila el proyecto completo.

```powershell
.\scripts\build.ps1
```

### 🧹 `clean.ps1`
Limpia el proyecto y opcionalmente elimina la base de datos local.

```powershell
.\scripts\clean.ps1
```

### 📦 `package.ps1`
Genera el paquete de distribución para Windows.

```powershell
.\scripts\package.ps1
```

El paquete se generará en: `composeApp\build\compose\binaries\main\`

### 🚀 `dev.ps1`
Comando todo-en-uno: limpia, compila y ejecuta (ideal para desarrollo).

```powershell
.\scripts\dev.ps1
```

## 🎯 VS Code Integration

También puedes ejecutar estos comandos desde VS Code:

1. Presiona `Ctrl+Shift+P` (o `Cmd+Shift+P` en Mac)
2. Escribe "Run Task"
3. Selecciona una de las tareas disponibles:
   - 🚀 Run MusicMusic
   - 🔨 Build Project
   - 🧹 Clean Build
   - 🔄 Refresh Dependencies
   - 📦 Package Distribution
   - 🗑️ Clean Project
   - 🔍 Generate SQLDelight Code
   - 🧪 Run Tests

## ⚡ Atajos Rápidos

### Ejecutar desde cualquier lugar en VS Code:
- `Ctrl+Shift+B` - Ejecuta la tarea por defecto (Run MusicMusic)
- `F5` - Lanza el depurador

## 📝 Notas

- Todos los scripts deben ejecutarse desde la raíz del proyecto
- Los scripts asumen que tienes PowerShell 5.1 o superior
- Se requiere Java 17+ instalado
- La primera ejecución puede tardar mientras Gradle descarga dependencias
