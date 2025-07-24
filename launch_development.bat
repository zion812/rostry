@echo off
title Rostry Development Environment

echo.
echo ==========================================
echo    ROSTRY - Development Environment
echo ==========================================
echo.

echo Starting Android Studio with Rostry project...
echo.

REM Try to find Android Studio installation
set ANDROID_STUDIO_PATH=""

REM Common Android Studio installation paths
if exist "C:\Program Files\Android\Android Studio\bin\studio64.exe" (
    set ANDROID_STUDIO_PATH="C:\Program Files\Android\Android Studio\bin\studio64.exe"
) else if exist "C:\Users\%USERNAME%\AppData\Local\Android\Sdk\tools\android.bat" (
    echo Android SDK found, but Android Studio not found in default location.
) else (
    echo Please install Android Studio or update the path in this script.
)

if not %ANDROID_STUDIO_PATH%=="" (
    echo Launching Android Studio...
    start "" %ANDROID_STUDIO_PATH% "%~dp0"
) else (
    echo.
    echo Android Studio not found in default locations.
    echo Please manually open Android Studio and load this project:
    echo %~dp0
    echo.
    echo Or update the ANDROID_STUDIO_PATH in this script.
)

echo.
echo Development Tips:
echo ================
echo 1. Enable USB Debugging on your Android device
echo 2. Connect device via USB
echo 3. Run the app using Ctrl+R in Android Studio
echo 4. Use quick_start.bat for command-line builds
echo.
echo Project Structure:
echo ==================
echo - app/src/main/java/com/rio/rostry/ - Main source code
echo - app/src/main/res/ - Resources (layouts, images, etc.)
echo - app/build.gradle.kts - App-level build configuration
echo - build.gradle.kts - Project-level build configuration
echo.

pause