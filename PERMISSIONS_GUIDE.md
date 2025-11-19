# Suraksha App - Permissions Guide

## Overview
This document lists all permissions required by the Suraksha safety app and explains why each permission is needed for the features to function properly.

---

## Manifest Permissions (AndroidManifest.xml)

### Core Safety Features

#### 1. **SEND_SMS**
- **Purpose**: Send emergency SMS alerts to trusted contacts
- **When Used**: When SOS is triggered by shake, hotword, fall detection, or manual button press
- **Justification**: Core emergency alert functionality
- **Runtime Permission**: Yes (Dangerous permission)

#### 2. **ACCESS_FINE_LOCATION**
- **Purpose**: Get precise GPS coordinates for emergency alerts
- **When Used**: Continuously when service is active to include location in SOS messages
- **Justification**: Emergency responders need accurate location information
- **Runtime Permission**: Yes (Dangerous permission)

#### 3. **ACCESS_COARSE_LOCATION**
- **Purpose**: Fallback for approximate location if fine location unavailable
- **When Used**: When GPS is not available but network location is
- **Justification**: Backup location method for emergency situations
- **Runtime Permission**: Yes (Dangerous permission)

#### 4. **RECORD_AUDIO**
- **Purpose**: Enable hotword detection ("bumblebee" or custom wake word)
- **When Used**: When Hotword Detection toggle is enabled in Settings
- **Justification**: On-device voice recognition for hands-free SOS triggering
- **Runtime Permission**: Yes (Dangerous permission)

#### 5. **POST_NOTIFICATIONS** (Android 13+)
- **Purpose**: Show foreground service notification and SOS trigger alerts
- **When Used**: When the protection service is running
- **Justification**: Required for foreground services and user notifications
- **Runtime Permission**: Yes on Android 13+ (Dangerous permission)

### Foreground Service Permissions

#### 6. **FOREGROUND_SERVICE**
- **Purpose**: Run background protection service that monitors sensors and hotword
- **When Used**: When any trigger (shake/hotword/fall) is enabled
- **Justification**: Keep protection active even when app is in background
- **Runtime Permission**: No (Normal permission on API 26+)

#### 7. **FOREGROUND_SERVICE_LOCATION** (Android 10+)
- **Purpose**: Allow foreground service to access location for emergency alerts
- **When Used**: When protection service is running
- **Justification**: Required to track location while service runs in background
- **Runtime Permission**: Yes on Android 14+ (Special permission)

#### 8. **FOREGROUND_SERVICE_MICROPHONE** (Android 14+)
- **Purpose**: Allow foreground service to use microphone for hotword detection
- **When Used**: When Hotword Detection is enabled
- **Justification**: Required for continuous voice monitoring in background
- **Runtime Permission**: Yes on Android 14+ (Special permission)

### Network & Connectivity

#### 9. **ACCESS_NETWORK_STATE**
- **Purpose**: Check network connectivity for Firebase and location services
- **When Used**: Throughout app usage
- **Justification**: Optimize location and authentication performance
- **Runtime Permission**: No (Normal permission)

---

## Hardware Features (Optional)

### 1. **android.hardware.telephony**
- **Purpose**: SMS sending capability
- **Required**: No (app will install on tablets without cellular)
- **Fallback**: App functions but SMS alerts won't work

### 2. **android.hardware.microphone**
- **Purpose**: Hotword detection
- **Required**: No (app will install without microphone)
- **Fallback**: Hotword detection feature will be unavailable

### 3. **android.hardware.sensor.accelerometer**
- **Purpose**: Shake detection and fall detection
- **Required**: No (app will install without accelerometer)
- **Fallback**: Shake and fall detection features will be unavailable

---

## Feature-to-Permission Mapping

### Triple Shake Detection (Gesture-based SOS)
- **Required Permissions**:
  - ✅ FOREGROUND_SERVICE
  - ✅ FOREGROUND_SERVICE_LOCATION (Android 10+)
  - ✅ ACCESS_FINE_LOCATION or ACCESS_COARSE_LOCATION
  - ✅ SEND_SMS
- **Optional Hardware**:
  - android.hardware.sensor.accelerometer

### Hotword Detection (Voice-activated SOS)
- **Required Permissions**:
  - ✅ RECORD_AUDIO
  - ✅ FOREGROUND_SERVICE
  - ✅ FOREGROUND_SERVICE_MICROPHONE (Android 14+)
  - ✅ ACCESS_FINE_LOCATION or ACCESS_COARSE_LOCATION
  - ✅ SEND_SMS
- **Optional Hardware**:
  - android.hardware.microphone
- **Additional Requirement**:
  - Picovoice access key in local.properties

### AI Fall Detection
- **Required Permissions**:
  - ✅ FOREGROUND_SERVICE
  - ✅ FOREGROUND_SERVICE_LOCATION (Android 10+)
  - ✅ ACCESS_FINE_LOCATION or ACCESS_COARSE_LOCATION
  - ✅ SEND_SMS
- **Optional Hardware**:
  - android.hardware.sensor.accelerometer

### Manual SOS Button
- **Required Permissions**:
  - ✅ ACCESS_FINE_LOCATION or ACCESS_COARSE_LOCATION
  - ✅ SEND_SMS

---

## Runtime Permission Flow

### Android 6.0+ (API 23+)
The app requests the following dangerous permissions at runtime:
1. **On App Launch** (MainActivity):
   - SEND_SMS
   - ACCESS_FINE_LOCATION
   - ACCESS_COARSE_LOCATION
   - RECORD_AUDIO
   - POST_NOTIFICATIONS (Android 13+)
   - FOREGROUND_SERVICE_LOCATION (Android 10+)
   - FOREGROUND_SERVICE_MICROPHONE (Android 14+)

### Permission Rationale
If a user denies a permission, the following features will be affected:
- **SMS Denied**: Cannot send emergency alerts
- **Location Denied**: Alerts sent without location information
- **Microphone Denied**: Hotword detection unavailable
- **Notifications Denied**: Service runs silently, SOS trigger popups won't show

---

## Service Configuration

### SurakshaService
- **Type**: Foreground Service
- **Foreground Types**: `location | microphone`
- **Notification Channel**: "Suraksha Service Channel"
- **Behavior**: Runs continuously when any trigger is enabled

---

## Privacy & Security Notes

1. **On-Device Processing**:
   - Hotword detection uses Picovoice Porcupine (fully on-device)
   - No audio data is transmitted to servers
   - Voice processing happens locally

2. **Location Data**:
   - Only accessed when SOS is triggered
   - Sent only to user-configured trusted contacts via SMS
   - Not stored or transmitted to external services

3. **Background Service**:
   - Only runs when user explicitly enables triggers
   - Transparent with persistent notification
   - Can be stopped anytime from Settings

4. **Sensor Data**:
   - Accelerometer data processed in real-time
   - No sensor data is logged or stored
   - Used only for gesture/fall detection

---

## Testing Permissions

### Manual Testing Checklist
1. ✅ Install app and grant all permissions
2. ✅ Toggle each trigger ON in Settings
3. ✅ Test shake detection (3 back-and-forth gestures)
4. ✅ Test hotword (say "bumblebee")
5. ✅ Test fall detection (simulate fall motion)
6. ✅ Test manual SOS button
7. ✅ Verify SMS sent with location link
8. ✅ Verify mini popup appears on trigger

### Permission Denial Testing
1. Deny SMS → Verify graceful failure message
2. Deny Location → Verify SMS sent without coordinates
3. Deny Microphone → Verify hotword toggle shows error
4. Deny Notifications → Verify service still works

---

## Troubleshooting

### "Permission Denied" Errors

**SMS Permission**:
```
Settings → Apps → Suraksha → Permissions → SMS → Allow
```

**Location Permission**:
```
Settings → Apps → Suraksha → Permissions → Location → Allow all the time
```

**Microphone Permission**:
```
Settings → Apps → Suraksha → Permissions → Microphone → Allow
```

**Notification Permission** (Android 13+):
```
Settings → Apps → Suraksha → Permissions → Notifications → Allow
```

### "Hotword disabled: missing access key"
Add to `local.properties`:
```
PICOVOICE_ACCESS_KEY=YOUR_ACCESS_KEY_HERE
```
Get free key from: https://console.picovoice.ai

---

## Compliance & Store Requirements

### Google Play Store
- ✅ Prominent disclosure of SMS usage in app description
- ✅ Privacy policy explaining location and microphone usage
- ✅ Foreground service types properly declared
- ✅ SMS permission used only for core safety functionality

### Regulatory Compliance
- GDPR: Location data not stored/processed beyond SMS transmission
- COPPA: No data collection from children
- Accessibility: Works without requiring specific hardware features

---

## Version History

### Current Version (1.0)
- Shake detection (3x back-and-forth gesture)
- Hotword detection (Porcupine-based, on-device)
- AI fall detection
- Manual SOS button
- Location-aware SMS alerts
- All necessary permissions configured

---

## Contact & Support
For permission-related issues or questions, please refer to:
- App Settings → About Us
- Privacy Policy (link in app)

