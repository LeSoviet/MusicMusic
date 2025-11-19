# MusicMusic - Script de Limpieza
# Limpia el proyecto y regenera todo

Write-Host "🧹 Limpiando proyecto..." -ForegroundColor Magenta
Write-Host ""

# Limpiar build
.\gradlew.bat clean

# Limpiar base de datos (opcional)
$dbPath = "$env:USERPROFILE\.musicmusic\musicmusic.db"
if (Test-Path $dbPath) {
    $response = Read-Host "¿Eliminar base de datos local? (S/N)"
    if ($response -eq 'S' -or $response -eq 's') {
        Remove-Item $dbPath -Force
        Write-Host "✅ Base de datos eliminada" -ForegroundColor Green
    }
}

Write-Host ""
Write-Host "✅ Limpieza completada!" -ForegroundColor Green
