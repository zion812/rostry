@echo off
echo Starting Rostry MVP Application...
echo.

echo Building the application...
call gradlew assembleDebug

if %ERRORLEVEL% EQU 0 (
    echo.
    echo Build successful! Installing on connected device...
    call gradlew installDebug
    
    if %ERRORLEVEL% EQU 0 (
        echo.
        echo App installed successfully!
        echo You can now launch "Rostry" from your device.
    ) else (
        echo.
        echo Installation failed. Make sure a device is connected and USB debugging is enabled.
    )
) else (
    echo.
    echo Build failed. Please check the error messages above.
)

echo.
pause