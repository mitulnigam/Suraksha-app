# Suraksha Setup Guide

## Quick Start

### 1. Configure API Keys

Add these to `local.properties` in the project root:

```properties
# Google Maps API Key (for location features)
MAPS_API_KEY=YOUR_GOOGLE_MAPS_API_KEY

# Picovoice Access Key (for hotword detection)
PICOVOICE_ACCESS_KEY=YOUR_PICOVOICE_ACCESS_KEY
```

**Get API Keys:**
- **Maps API**: https://console.cloud.google.com/apis/credentials
- **Picovoice**: https://console.picovoice.ai (Free tier available)

---

### 2. Build & Install

```powershell
# Clean build
.\gradlew.bat :app:clean :app:assembleDebug

# Install to connected device
.\gradlew.bat :app:installDebug
```

**If build fails with "25.0.1" error:**
- Open Android Studio
- Go to SDK Manager
- Install:
  - Android SDK Platform 36
  - Android SDK Build-Tools 36.x
  - Android SDK Platform-Tools (latest)

---

### 3. Grant Permissions on First Launch

The app will request these permissions:
- ✅ **SMS** - Send emergency alerts
- ✅ **Location** - Include GPS in alerts
- ✅ **Microphone** - Hotword detection
- ✅ **Notifications** - Show service status
- ✅ **Foreground Services** - Background monitoring

**Important**: Grant "Allow all the time" for Location to ensure alerts work in background.

---

### 4. Add Trusted Contacts

1. Open app → Navigate to **Contacts** tab
2. Tap **Add Contact** button
3. Select contact from phone or enter manually:
   - Name: Contact's name
   - Phone: Full number with country code (+1234567890)
4. Repeat for all emergency contacts

---

### 5. Enable Safety Features

Go to **Settings** → **Triggers**:

#### Shake Detection
- Toggle **ON**
- Triggers on 3 rapid back-and-forth shakes
- Test: Shake phone firmly 3 times

#### Hotword Detection  
- Toggle **ON**
- Default hotword: "**bumblebee**"
- Test: Say "bumblebee" clearly
- Note: Requires PICOVOICE_ACCESS_KEY

#### AI Fall Detection
- Toggle **ON**
- Triggers on sudden drop followed by impact
- Test: Simulate fall motion (carefully!)

---

### 6. Test the System

#### Manual SOS (Button)
1. On Home screen, tap the large **SOS** button
2. You'll see a 10-second countdown
3. After countdown, SMS is sent to all contacts
4. Tap **CANCEL** to abort before sending

#### Shake Test
1. Ensure "Shake Detection" is ON
2. Hold phone firmly
3. Shake back-and-forth rapidly 3 times (like shaking a bottle)
4. Should see popup: "SOS triggered (Shake Gesture)"
5. SMS sent after cooldown

#### Hotword Test
1. Ensure "Hotword Detection" is ON
2. Say clearly: "**bumblebee**"
3. Should see popup: "SOS triggered (Hotword)"
4. SMS sent after cooldown

#### Expected SMS Format
```
HELP! This is an emergency alert from Suraksha. 
My current location is: http://maps.google.com?q=LAT,LON
```

---

## Feature Toggles

All triggers can be turned ON/OFF independently:
- **Home Screen**: Shows status of each feature (ON/OFF)
- **Settings Screen**: Full control with toggle switches
- **Service**: Automatically starts/stops based on toggles

---

## Troubleshooting

### "Hotword disabled: missing access key"
**Solution**: Add `PICOVOICE_ACCESS_KEY` to `local.properties`

### "SOS not triggering on shake"
**Check**:
- Is "Shake Detection" toggle ON in Settings?
- Is the foreground service notification showing?
- Try shaking harder/faster with clear back-and-forth motion
- Check logcat for "ShakeDetector" messages

### "Hotword not responding"
**Check**:
- Is "Hotword Detection" toggle ON?
- Is microphone permission granted?
- Speak clearly and say "bumblebee" (not "help me")
- Reduce background noise
- Check logcat for "HotwordDetector" messages

### "SMS not sent"
**Check**:
- SMS permission granted?
- Trusted contacts added with valid phone numbers?
- Phone has SMS capability (not Wi-Fi only tablet)?
- Check logcat for "AlertManager" messages

### "No location in SMS"
**Check**:
- Location permission granted as "Allow all the time"?
- GPS/Location services enabled on device?
- Device has GPS capability?
- May take 10-20 seconds to acquire GPS lock

### Service not running
**Check**:
- Any trigger (Shake/Hotword/Fall) must be ON
- Look for "Suraksha is Active" notification
- If missing, toggle a trigger OFF then ON again
- Check Settings → Apps → Suraksha → Battery → Unrestricted

---

## Default Settings

- **Shake Detection**: ON
- **Hotword Detection**: ON (disabled if no API key)
- **Fall Detection**: OFF (more prone to false positives)
- **Cooldown Period**: 10 seconds between triggers
- **Countdown**: 10 seconds for manual SOS button

---

## Customization Options

### Shake Sensitivity
Edit `ShakeDetector.kt`:
```kotlin
private const val THRESHOLD_M_S2 = 8.5f  // Lower = more sensitive
private const val REQUIRED_ALTERNATIONS = 6  // Change shake pattern
```

### Hotword Keyword
Default is "bumblebee". To use custom phrase:
1. Generate .ppn model at https://console.picovoice.ai
2. Add .ppn file to `app/src/main/assets/`
3. Update `HotwordDetector.kt` to load custom model

### Cooldown Period
Edit `SurakshaService.kt`:
```kotlin
private const val COOLDOWN_PERIOD_MS = 10000  // milliseconds
```

---

## Background Service Behavior

**When Enabled**:
- Persistent notification: "Suraksha is Active - You are protected"
- Continuously monitors enabled triggers
- Low battery impact (sensor-based, no polling)

**When All Triggers OFF**:
- Service stops automatically
- Notification disappears
- No background activity

**Battery Optimization**:
- For best reliability: Settings → Apps → Suraksha → Battery → Unrestricted
- This prevents Android from killing the protection service

---

## Privacy & Data

**What's Collected**:
- None. All processing is on-device.

**What's Transmitted**:
- Only SMS to your configured contacts when SOS triggers
- SMS contains your location link (GPS coordinates)

**What's Stored**:
- Trusted contact list (locally on device)
- Feature toggle preferences (locally)
- Firebase authentication (if using sign-in)

**Microphone Usage**:
- Hotword detection is 100% on-device (Porcupine)
- No audio leaves your phone
- No recordings stored

---

## Advanced Configuration

### Custom Hotword Model

1. Sign up at https://console.picovoice.ai
2. Create custom wake word (e.g., "help me")
3. Download .ppn model file
4. Add to `app/src/main/assets/keywords/`
5. Update `HotwordDetector.kt`:
```kotlin
// Instead of:
.setKeyword(keyword)

// Use:
.setKeywordPath("keywords/help-me.ppn")
```

### Multiple Keywords

Edit `HotwordDetector.kt` to support multiple wake words:
```kotlin
.setKeywordPaths(arrayOf(
    "keywords/help-me.ppn",
    "keywords/emergency.ppn"
))
```

---

## Release Checklist

Before publishing:
- [ ] Add privacy policy URL
- [ ] Add app description mentioning SMS usage
- [ ] Test on multiple Android versions (26+)
- [ ] Test with/without permissions granted
- [ ] Test on devices without GPS/microphone
- [ ] Add app icon and branding
- [ ] Remove debug logs for production
- [ ] Enable ProGuard for release build
- [ ] Generate signed APK/AAB

---

## Support Resources

- **Permissions Guide**: See `PERMISSIONS_GUIDE.md`
- **Picovoice Docs**: https://picovoice.ai/docs/
- **Android Sensors**: https://developer.android.com/guide/topics/sensors
- **Firebase Setup**: https://firebase.google.com/docs/android/setup

---

## Version Info

**Current Version**: 1.0  
**Min SDK**: 26 (Android 8.0)  
**Target SDK**: 36 (Android 15)  
**Build Tools**: 36.x

**Features**:
- ✅ Triple shake gesture detection
- ✅ On-device hotword recognition
- ✅ AI-based fall detection
- ✅ Manual SOS button with countdown
- ✅ GPS location in emergency SMS
- ✅ Trusted contacts management
- ✅ Background foreground service
- ✅ Multi-trigger cooldown system

---

Need help? Check logs:
```powershell
# View all app logs
adb logcat -s SurakshaService ShakeDetector HotwordDetector AlertManager

# View specific feature
adb logcat -s ShakeDetector:V *:S
```

