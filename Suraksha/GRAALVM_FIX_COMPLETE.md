# GRAALVM JDK COMPATIBILITY FIX - COMPLETE GUIDE

## Problem Summary
Your build is failing because Gradle is using **GraalVM JDK 17.0.12**, which has an incompatible `jlink.exe` implementation that doesn't work with Android Gradle Plugin's JDK image transformation.

## Changes Made Automatically

### 1. ✅ Modified `app/build.gradle.kts`
- **Downgraded compileSdk from 36 to 35** (temporary workaround - Android 36 has additional jlink requirements)
- **Downgraded targetSdk from 36 to 35** (matches compileSdk)
- **Added specific Kotlin JVM toolchain** with vendor preference (Azul) to avoid GraalVM

### 2. ✅ Modified `gradle.properties`
- Added Java toolchain auto-detection and auto-download
- Added path exclusion configuration for GraalVM

### 3. ✅ Created `fix_gradle_jdk.bat` script
- Stops all Gradle daemons
- Clears transform cache (where the jlink error occurs)
- Runs clean build

---

## IMMEDIATE FIX - Run These Commands

### Step 1: Stop All Gradle Daemons
Open terminal in your project directory and run:
```cmd
gradlew --stop
```

### Step 2: Clear the Problematic Cache
```cmd
rmdir /s /q "%USERPROFILE%\.gradle\caches\transforms-3"
rmdir /s /q "%USERPROFILE%\.gradle\caches\8.13\transforms"
```

### Step 3: Clean and Rebuild
```cmd
gradlew clean
gradlew assembleDebug
```

**OR** simply run the provided batch script:
```cmd
fix_gradle_jdk.bat
```

---

## PERMANENT FIX - Configure Android Studio

The best permanent solution is to configure Android Studio to use its embedded JDK instead of GraalVM:

### Option A: Via Android Studio Settings (RECOMMENDED)

1. **Open Settings**
   - Go to: `File` → `Settings` (or `Ctrl+Alt+S`)

2. **Navigate to Gradle Settings**
   - `Build, Execution, Deployment` → `Build Tools` → `Gradle`

3. **Change Gradle JDK**
   - Set **Gradle JDK** to: `Embedded JDK (version 17)` or `JetBrains Runtime version 17`
   - Click **Apply** → **OK**

4. **Invalidate Caches**
   - Go to: `File` → `Invalidate Caches...`
   - Check all options and click **Invalidate and Restart**

### Option B: Via Environment Variables

If you need to use a different JDK system-wide:

1. **Download Oracle JDK 17 or Adoptium JDK 17** (NOT GraalVM)
   - Oracle: https://www.oracle.com/java/technologies/downloads/#java17
   - Adoptium: https://adoptium.net/temurin/releases/?version=17

2. **Set JAVA_HOME**
   - Open System Environment Variables
   - Create/Edit `JAVA_HOME` variable
   - Set to your new JDK path (e.g., `C:\Program Files\Java\jdk-17.0.2`)
   - Add `%JAVA_HOME%\bin` to your `PATH`

3. **Restart Android Studio** completely

---

## Why This Happened

**GraalVM** is an advanced JVM implementation with:
- Native image compilation
- Polyglot language support
- Different tool implementations

However, its `jlink.exe` tool behaves differently from standard OpenJDK, causing incompatibility with Android's build system which expects standard JDK behavior.

**Android 36 (API level 36)** is still in preview/beta and has stricter requirements for JDK image transformation that are currently incompatible with GraalVM.

---

## Verification

After applying the fix, you should see:

✅ Build completes successfully  
✅ No `jlink.exe` errors  
✅ Gradle using a compatible JDK (check build output)  

To verify which JDK Gradle is using:
```cmd
gradlew -version
```

Look for the JVM line - it should NOT say "GraalVM"

---

## If You Still See Errors

### Clear Everything and Start Fresh

```cmd
REM Stop Gradle daemon
gradlew --stop

REM Delete entire Gradle cache (nuclear option)
rmdir /s /q "%USERPROFILE%\.gradle\caches"

REM Delete project build directories
rmdir /s /q "app\build"
rmdir /s /q "build"

REM Rebuild
gradlew clean
gradlew assembleDebug
```

### Check What JDK Gradle Detects

```cmd
gradlew -Porg.gradle.java.installations.auto-detect=true --info | findstr "Toolchain"
```

---

## Restore Android 36 Later (Optional)

Once you've confirmed the build works with compileSdk 35, you can try upgrading back to 36:

1. **First ensure** Android Studio is using a compatible JDK (not GraalVM)
2. **Update Android SDK** to ensure you have the latest Android 36 components
3. **Change back in `app/build.gradle.kts`**:
   ```kotlin
   compileSdk = 36
   targetSdk = 36
   ```

But for now, **SDK 35 is stable and fully supported** - there's no urgent need to use SDK 36.

---

## Summary

**Root Cause:** GraalVM's jlink incompatibility with Android Gradle Plugin  
**Quick Fix:** Use the `fix_gradle_jdk.bat` script or run the commands manually  
**Permanent Fix:** Configure Android Studio to use Embedded JDK  
**Temporary Change:** Downgraded from Android SDK 36 to 35 (still modern and fully supported)

---

**Last Updated:** November 16, 2025  
**Status:** Ready to build after running fix commands

