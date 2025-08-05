@echo off
echo.
echo ========================================
echo    ROSTRY - Quick Start Script
echo ========================================
echo.

echo Checking for connected Android devices...
adb devices

echo.
echo Building and installing Rostry app...
echo.

call gradlew installDebug

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo   SUCCESS! Rostry app installed
    echo ========================================
    echo.
    echo The app should now be available on your device.
    echo Look for "Rostry" in your app drawer.
    echo.
    echo To launch the app via ADB:
    echo adb shell am start -n com.rio.rostry/.MainActivity
    echo.
) else (
    echo.
    echo ========================================
    echo   BUILD FAILED
    echo ========================================
    echo.
    echo Please check the error messages above.
    echo Make sure you have:
    echo 1. Android device connected via USB
    echo 2. USB debugging enabled
    echo 3. Device authorized for debugging
    echo.
)

pause