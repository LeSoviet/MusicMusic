# MusicMusic - Script de Build
# Compila el proyecto completo

Write-Host "🔨 Compilando MusicMusic..." -ForegroundColor Yellow
Write-Host ""

.\gradlew.bat clean build

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "✅ Compilación exitosa!" -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "❌ Error en la compilación" -ForegroundColor Red
}
