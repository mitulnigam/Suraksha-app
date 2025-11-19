@echo off
echo ========================================
echo EMERGENCY FIX - Forcing JDK Change
echo ========================================
echo.

echo Step 1: Killing ALL Gradle and Java processes...
taskkill /F /IM java.exe 2>nul
taskkill /F /IM javaw.exe 2>nul
timeout /t 2 /nobreak >nul
echo.

echo Step 2: Stopping Gradle daemon...
call gradlew --stop
echo.

echo Step 3: Deleting Gradle daemon directory (forces fresh start)...
rmdir /s /q "%USERPROFILE%\.gradle\daemon" 2>nul
echo.

echo Step 4: Clearing ALL transform caches...
rmdir /s /q "%USERPROFILE%\.gradle\caches\transforms-3" 2>nul
rmdir /s /q "%USERPROFILE%\.gradle\caches\8.13\transforms" 2>nul
rmdir /s /q "%USERPROFILE%\.gradle\caches\modules-2" 2>nul
echo.

echo Step 5: Clearing project build directories...
rmdir /s /q "app\build" 2>nul
rmdir /s /q "build" 2>nul
rmdir /s /q ".gradle" 2>nul
echo.

echo Step 6: Cleaning project...
call gradlew clean
echo.

echo Step 7: Building with new JDK (will auto-download if needed)...
echo This may take 5-10 minutes on first run...
echo.
call gradlew assembleDebug --no-daemon --info
echo.

echo ========================================
echo Fix Complete!
echo ========================================
pause

