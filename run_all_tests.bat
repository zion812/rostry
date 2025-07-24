u@echo off
echo ========================================
echo Running Comprehensive UI Tests for Rostry App
echo ========================================

echo.
echo Building the app...
call gradlew assembleDebug
if %errorlevel% neq 0 (
    echo Build failed!
    pause
    exit /b 1
)

echo.
echo Building test APK...
call gradlew assembleDebugAndroidTest
if %errorlevel% neq 0 (
    echo Test build failed!
    pause
    exit /b 1
)

echo.
echo Installing app on device/emulator...
call gradlew installDebug
if %errorlevel% neq 0 (
    echo App installation failed!
    pause
    exit /b 1
)

echo.
echo Installing test APK on device/emulator...
call gradlew installDebugAndroidTest
if %errorlevel% neq 0 (
    echo Test APK installation failed!
    pause
    exit /b 1
)

echo.
echo ========================================
echo Running Comprehensive UI Tests...
echo ========================================

echo.
echo Running Navigation Tests...
call gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.rio.rostry.NavigationTest
if %errorlevel% neq 0 (
    echo Navigation tests failed!
)

echo.
echo Running Button Interaction Tests...
call gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.rio.rostry.ButtonInteractionTest
if %errorlevel% neq 0 (
    echo Button interaction tests failed!
)

echo.
echo Running Comprehensive UI Tests...
call gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.rio.rostry.ComprehensiveUITest
if %errorlevel% neq 0 (
    echo Comprehensive UI tests failed!
)

echo.
echo ========================================
echo Running All Tests Together...
echo ========================================
call gradlew connectedDebugAndroidTest
if %errorlevel% neq 0 (
    echo Some tests failed! Check the reports for details.
) else (
    echo All tests passed successfully!
)

echo.
echo ========================================
echo Test Results Location:
echo app\build\reports\androidTests\connected\index.html
echo ========================================

echo.
echo Opening test results...
start app\build\reports\androidTests\connected\index.html

pause