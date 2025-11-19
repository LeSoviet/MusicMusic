# MusicMusic - Script de Empaquetado
# Genera el paquete de distribución para la plataforma actual

Write-Host "📦 Empaquetando MusicMusic..." -ForegroundColor Blue
Write-Host ""

.\gradlew.bat :composeApp:packageDistributionForCurrentOS

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "✅ Empaquetado exitoso!" -ForegroundColor Green
    Write-Host "📁 Busca el paquete en: composeApp\build\compose\binaries\main\" -ForegroundColor Cyan
} else {
    Write-Host ""
    Write-Host "❌ Error en el empaquetado" -ForegroundColor Red
}
