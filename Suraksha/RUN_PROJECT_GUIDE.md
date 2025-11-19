# Suraksha Project - Run Guide

## ✅ Project Configuration Status

Your project is now fully configured and ready to run! All necessary settings have been applied.

## 📋 What Has Been Configured

1. **✅ Maps API Key**: Added to `local.properties` and configured in `build.gradle.kts`
2. **✅ Firebase**: `google-services.json` is present and configured
3. **✅ Gradle**: Version 8.13 with proper JVM settings
4. **✅ AndroidManifest.xml**: All permissions and services configured
5. **✅ Dependencies**: All required libraries are included

## 🚀 How to Run the Project

### Step 1: Sync Gradle Files
1. In Android Studio, click **File** → **Sync Project with Gradle Files**
2. Wait for the sync to complete (this may take a few minutes the first time)

### Step 2: Set Up a Device

**Option A: Using an Android Emulator**
1. Click **Tools** → **Device Manager**
2. Create a new Virtual Device if you don't have one:
   - Click **Create Device**
   - Select a device (e.g., Pixel 6)
   - Select a system image (API 26 or higher, recommended: API 34+)
   - Click **Finish**
3. Start the emulator by clicking the ▶️ button next to the device

**Option B: Using a Physical Device**
1. Enable **Developer Options** on your Android device:
   - Go to **Settings** → **About Phone**
   - Tap **Build Number** 7 times
2. Enable **USB Debugging**:
   - Go to **Settings** → **Developer Options**
   - Enable **USB Debugging**
3. Connect your device via USB
4. Accept the USB debugging prompt on your device

### Step 3: Run the App
1. Select your target device from the device dropdown in the toolbar
2. Click the green **Run** ▶️ button in the toolbar
3. Or press **Shift + F10**

## 📱 App Features

The Suraksha app includes:
- 🗺️ **Google Maps Integration**: Real-time location tracking
- 🚨 **SOS Emergency System**: Quick emergency alerts
- 📍 **Location Services**: Background location tracking
- 🔊 **Voice Features**: Hotword detection and audio monitoring
- 🤸 **Fall Detection**: Accelerometer-based fall detection
- 📱 **SMS Alerts**: Send emergency SMS to contacts
- 🔔 **Notifications**: Foreground service notifications

## 🔑 Required Permissions

The app will request the following permissions at runtime:
- Location (Fine & Coarse)
- SMS
- Microphone
- Notifications (Android 13+)

Grant all permissions for full functionality.

## 🛠️ Troubleshooting

### Build Fails
- **Clean and Rebuild**: **Build** → **Clean Project**, then **Build** → **Rebuild Project**
- **Invalidate Caches**: **File** → **Invalidate Caches / Restart**

### Gradle Sync Issues
- Check your internet connection (Gradle needs to download dependencies)
- Ensure you have enough disk space
- Check that your JDK is properly configured (should use JDK 17)

### App Crashes on Launch
- Check Logcat for error messages
- Ensure all permissions are granted
- Verify that Firebase is properly configured

### Maps Not Showing
- Verify `MAPS_API_KEY` is in `local.properties`
- Check that the API key is enabled in Google Cloud Console
- Enable **Maps SDK for Android** in your Google Cloud project

## 📊 Project Structure

```
Suraksha/
├── app/
│   ├── src/main/
│   │   ├── java/com/suraksha/app/
│   │   │   ├── MainActivity.kt
│   │   │   ├── SurakshaApp.kt
│   │   │   ├── services/        # Background services
│   │   │   ├── sensors/         # Sensor handling
│   │   │   ├── ui/              # UI components
│   │   │   ├── sos/             # SOS functionality
│   │   │   └── debug/           # Debug tools
│   │   ├── res/                 # Resources
│   │   └── AndroidManifest.xml
│   ├── build.gradle.kts
│   └── google-services.json
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── local.properties             # Your API keys (not in version control)
```

## 🔐 API Keys Configuration

Your API keys are stored in `local.properties`:
```properties
MAPS_API_KEY=AIzaSyAr_FI6_F4nRAyVzzQzuZ3jm7EBjnob3GI
```

**Important**: Never commit `local.properties` to version control!

## 📝 Additional Notes

- **Minimum SDK**: API 26 (Android 8.0)
- **Target SDK**: API 36 (Android 14+)
- **Kotlin Version**: 2.0.21
- **Gradle Version**: 8.13
- **JDK Required**: JDK 17

## ✨ You're Ready!

Your project is fully configured and ready to run. Click the **Run** button and start developing! 🎉

