# MusicMusic - Script de Desarrollo
# Limpia, compila y ejecuta en un solo comando

Write-Host "🚀 Preparando MusicMusic para desarrollo..." -ForegroundColor Cyan
Write-Host ""

# Limpiar
Write-Host "1️⃣  Limpiando..." -ForegroundColor Yellow
.\gradlew.bat clean

# Compilar
Write-Host ""
Write-Host "2️⃣  Compilando..." -ForegroundColor Yellow
.\gradlew.bat build

if ($LASTEXITCODE -eq 0) {
    # Ejecutar
    Write-Host ""
    Write-Host "3️⃣  Ejecutando..." -ForegroundColor Yellow
    Write-Host ""
    .\gradlew.bat :composeApp:run
} else {
    Write-Host ""
    Write-Host "❌ Error en la compilación, no se puede ejecutar" -ForegroundColor Red
}
