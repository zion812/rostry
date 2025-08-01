@echo off
echo ========================================
echo ROSTRY DEPLOYMENT SCRIPT
echo ========================================
echo.

echo [1/5] Cleaning previous builds...
call gradlew clean

echo.
echo [2/5] Running tests...
call gradlew test

echo.
echo [3/5] Building debug APK...
call gradlew assembleDebug

echo.
echo [4/5] Building release APK...
call gradlew assembleRelease

echo.
echo [5/5] Generating build report...
echo Build completed at: %date% %time% > build_report.txt
echo Debug APK: app/build/outputs/apk/debug/app-debug.apk >> build_report.txt
echo Release APK: app/build/outputs/apk/release/app-release.apk >> build_report.txt

echo.
echo ========================================
echo DEPLOYMENT COMPLETE!
echo ========================================
echo Debug APK: app/build/outputs/apk/debug/app-debug.apk
echo Release APK: app/build/outputs/apk/release/app-release.apk
echo Build Report: build_report.txt
echo ========================================

pause