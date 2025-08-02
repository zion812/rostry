@echo off
echo ========================================
echo ROSTRY COMPILATION VERIFICATION
echo ========================================
echo.

echo [1/4] Cleaning project...
call gradlew clean

echo.
echo [2/4] Compiling Kotlin sources...
call gradlew compileDebugKotlin --continue

echo.
echo [3/4] Checking for specific errors...
call gradlew compileDebugKotlin 2>&1 | findstr /C:"error:" /C:"Unresolved reference" /C:"Cannot find"

echo.
echo [4/4] Build summary...
call gradlew compileDebugKotlin 2>&1 | findstr /C:"BUILD SUCCESSFUL" /C:"BUILD FAILED"

echo.
echo ========================================
echo VERIFICATION COMPLETE
echo ========================================
pause