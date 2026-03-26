@echo off
REM Journex Launcher for Windows
REM This script will launch Journex with the bundled or system Java

setlocal

REM Check if Java is available
where java >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo Java is not installed or not in PATH.
    echo Please install Java 17 or higher from: https://adoptium.net/
    echo.
    pause
    exit /b 1
)

REM Check Java version
for /f "tokens=3" %%g in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    set JAVA_VERSION=%%g
)
set JAVA_VERSION=%JAVA_VERSION:"=%
for /f "delims=. tokens=1-3" %%v in ("%JAVA_VERSION%") do (
    set MAJOR=%%v
)

if %MAJOR% LSS 17 (
    echo Java version %JAVA_VERSION% is too old.
    echo Journex requires Java 17 or higher.
    echo Please update Java from: https://adoptium.net/
    echo.
    pause
    exit /b 1
)

REM Launch Journex
echo Starting Journex...
start javaw -jar "%~dp0Journex.jar"
exit
