@echo off
echo ========================================
echo ROSTRY QUICK TEST SCRIPT
echo ========================================
echo.

echo [1/3] Checking compilation...
call gradlew compileDebugKotlin --continue

echo.
echo [2/3] Running unit tests...
call gradlew testDebugUnitTest --continue

echo.
echo [3/3] Building debug APK...
call gradlew assembleDebug --continue

echo.
echo ========================================
echo QUICK TEST COMPLETE!
echo ========================================

if exist "app\build\outputs\apk\debug\app-debug.apk" (
    echo ✅ SUCCESS: Debug APK generated successfully!
    echo Location: app\build\outputs\apk\debug\app-debug.apk
) else (
    echo ❌ FAILED: Debug APK not generated
)

echo ========================================
pause