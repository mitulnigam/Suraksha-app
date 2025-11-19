@echo off
echo ========================================
echo Fixing Gradle JDK Issue
echo ========================================
echo.

echo Step 1: Stopping all Gradle daemons...
call gradlew --stop
echo.

echo Step 2: Clearing Gradle transform cache...
rmdir /s /q "%USERPROFILE%\.gradle\caches\transforms-3" 2>nul
rmdir /s /q "%USERPROFILE%\.gradle\caches\8.13\transforms" 2>nul
echo.

echo Step 3: Clearing build cache...
call gradlew clean
echo.

echo Step 4: Building with auto-download enabled...
echo Note: Gradle will automatically download a compatible JDK if needed.
echo This may take a few minutes on first run.
call gradlew assembleDebug
echo.

echo ========================================
echo Fix complete!
echo ========================================
echo.
pause

