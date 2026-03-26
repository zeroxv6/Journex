@echo off
REM Journex Desktop - Package Builder Script for Windows

echo ========================================
echo    Journex Desktop Package Builder
echo ========================================
echo.

echo Select build option:
echo 1) Build all packages for Windows
echo 2) Build EXE installer
echo 3) Build MSI installer
echo 4) Just run the app (no packaging)
echo.

set /p choice="Enter choice [1-4]: "

if "%choice%"=="1" (
    echo Building all Windows packages...
    call gradlew.bat :desktop:packageDistributionForCurrentOS
) else if "%choice%"=="2" (
    echo Building EXE installer...
    call gradlew.bat :desktop:packageExe
) else if "%choice%"=="3" (
    echo Building MSI installer...
    call gradlew.bat :desktop:packageMsi
) else if "%choice%"=="4" (
    echo Running application...
    call gradlew.bat :desktop:run
) else (
    echo Invalid choice
    exit /b 1
)

echo.
echo ========================================
echo           Build Complete!
echo ========================================
echo.
echo Output location: desktop\build\compose\binaries\main\
echo.
pause
