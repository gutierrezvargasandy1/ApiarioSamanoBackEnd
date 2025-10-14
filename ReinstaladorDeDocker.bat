@echo off
setlocal enabledelayedexpansion

echo =====================================================
echo 🚀 INICIANDO PROCESO DE REINSTALACIÓN Y CREACIÓN DE IMÁGENES DOCKER
echo =====================================================

cd /d "C:\Users\Andy\Desktop\ApiarioSamanoBackEnd"

:: =============================
:: Lista de microservicios CON NOMBRES DE IMAGEN EN MINÚSCULAS
:: =============================
set servicios=MicroServiceUsuario:microserviceusuario MicroServiceProveedores:microserviceproveedores MicroServiceProduccion:microserviceproduccion MicroServiceNotificacionesGmail:microservicenotificacionesgmail MicroServiceGeneradorCodigo:microservicegeneradorcodigo MicroServiceAuth:microserviceauth MicroServiceApiarios:microserviceapiarios MicroServiceAlmacen:microservicealmacen

set reintentos=3

:: =============================
:: PROCESAR CADA MICROSERVICIO
:: =============================
for %%s in (%servicios%) do (
    for /f "tokens=1,2 delims=:" %%a in ("%%s") do (
        set carpeta=%%a
        set imagen=%%b
        
        echo.
        echo =====================================================
        echo 📦 PROCESANDO: !carpeta!
        echo =====================================================
        
        :: 1. ELIMINAR CARPETA TARGET
        if exist "!carpeta!\target" (
            echo 🗑 Eliminando carpeta target de !carpeta!...
            rmdir /s /q "!carpeta!\target" >nul 2>&1
        )
        
        :: 2. COMPILAR Y GENERAR TARGET
        echo 🔧 Compilando !carpeta!...
        cd "!carpeta!"
        call mvn clean package -DskipTests >nul 2>&1
        if !errorlevel! neq 0 (
            echo ❌ Error compilando !carpeta!
            cd ..
            goto :error
        )
        cd ..
        
        :: 3. CREAR IMAGEN DOCKER (con reintentos)
        set exitoso=0
        for /l %%i in (1,1,%reintentos%) do (
            if !exitoso! equ 0 (
                echo 🐳 Creando imagen Docker !imagen!:1.0 - Intento %%i de %reintentos%...
                cd "!carpeta!"
                call docker build -t !imagen!:1.0 . >nul 2>&1
                if !errorlevel! equ 0 (
                    set exitoso=1
                    echo ✅ !carpeta! completado exitosamente
                ) else (
                    if %%i lss %reintentos% (
                        echo ⏳ Esperando 5 segundos antes de reintentar...
                        timeout /t 5 /nobreak >nul
                    )
                )
                cd ..
            )
        )
        
        if !exitoso! equ 0 (
            echo ❌ Error creando imagen !imagen!:1.0 después de %reintentos% intentos
            goto :error
        )
    )
)

echo.
echo =====================================================
echo ✅ TODAS LAS IMÁGENES DOCKER HAN SIDO CREADAS
echo =====================================================
pause
exit /b 0

:error
echo.
echo ❌ PROCESO INTERRUMPIDO POR ERROR
echo =====================================================
echo.
echo 💡 Soluciones:
echo    1. Verifica tu conexión a internet
echo    2. Verifica que Docker Desktop esté ejecutándose
echo    3. Intenta nuevamente ejecutando el script
echo.
pause
exit /b 1