# Script para compilar MusicMusic y generar el instalador de Windows (.exe y .msi)
# Ejecución: .\build-installer.ps1

Write-Host "================================" -ForegroundColor Cyan
Write-Host "MusicMusic - Windows Installer Builder" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""

# Verificar que estamos en el directorio correcto
if (-not (Test-Path "build.gradle.kts")) {
    Write-Host "Error: No se encontró build.gradle.kts" -ForegroundColor Red
    Write-Host "Asegúrate de ejecutar este script desde la raíz del proyecto" -ForegroundColor Red
    exit 1
}

Write-Host "Paso 1: Limpiando builds anteriores..." -ForegroundColor Yellow
./gradlew clean

if ($LASTEXITCODE -ne 0) {
    Write-Host "Error durante la limpieza" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "Paso 2: Compilando MusicMusic..." -ForegroundColor Yellow
./gradlew packageDistributionForCurrentOS

if ($LASTEXITCODE -ne 0) {
    Write-Host "Error durante la compilación" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "Paso 3: Buscando archivos del instalador..." -ForegroundColor Yellow

$msiFile = Get-ChildItem -Path "composeApp/build/compose/binaries/main/msi/*.msi" -ErrorAction SilentlyContinue
$exeFile = Get-ChildItem -Path "composeApp/build/compose/binaries/main/exe/*.exe" -ErrorAction SilentlyContinue

if ($msiFile) {
    Write-Host "✓ Instalador MSI encontrado:" -ForegroundColor Green
    Write-Host "  $($msiFile.FullName)" -ForegroundColor Green
    Write-Host ""
}

if ($exeFile) {
    Write-Host "✓ Instalador EXE encontrado:" -ForegroundColor Green
    Write-Host "  $($exeFile.FullName)" -ForegroundColor Green
    Write-Host ""
}

Write-Host "================================" -ForegroundColor Cyan
Write-Host "¡Compilación completada!" -ForegroundColor Green
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Próximos pasos:" -ForegroundColor Yellow
Write-Host "1. Los instaladores están en: composeApp/build/compose/binaries/main/" -ForegroundColor White
Write-Host "2. Puedes distribuir el archivo .msi o .exe a otros usuarios" -ForegroundColor White
Write-Host "3. Los usuarios pueden ejecutar el instalador para instalar MusicMusic" -ForegroundColor White
Write-Host ""
