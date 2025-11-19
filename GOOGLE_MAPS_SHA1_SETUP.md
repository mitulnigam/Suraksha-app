# Google Maps Setup Guide - SHA1 Configuration ✅

## 📋 **Your Configuration Details**

**SHA1 Fingerprint:**
```
C6:0D:64:7B:6B:9E:08:CB:92:6E:64:2A:62:E0:4C:4B:21:FF:5D:EC
```

**Maps API Key:**
```
AIzaSyAr_FI6_F4nRAyVzzQzuZ3jm7EBjnob3GI
```

**Package Name:**
```
com.suraksha.app
```

---

## ✅ **STEP-BY-STEP SETUP**

### **Step 1: Enable Google Maps SDK for Android**

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Select your project (or create a new one)
3. Navigate to: **APIs & Services** → **Library**
4. Search for: **"Maps SDK for Android"**
5. Click **ENABLE**

6. Also enable these APIs:
   - ✅ **Maps SDK for Android**
   - ✅ **Places API** (for safe havens search)
   - ✅ **Geocoding API** (optional, for address lookup)

---

### **Step 2: Configure API Key with SHA1**

1. Go to: **APIs & Services** → **Credentials**

2. Find your API Key: `AIzaSyAr_FI6_F4nRAyVzzQzuZ3jm7EBjnob3GI`

3. Click **Edit** (pencil icon)

4. Under **Application restrictions**:
   - Select: ✅ **Android apps**

5. Click **+ ADD AN ITEM**

6. Enter:
   ```
   Package name: com.suraksha.app
   SHA-1 certificate fingerprint: C6:0D:64:7B:6B:9E:08:CB:92:6E:64:2A:62:E0:4C:4B:21:FF:5D:EC
   ```

7. Under **API restrictions**:
   - Select: ✅ **Restrict key**
   - Choose these APIs:
     - ✅ Maps SDK for Android
     - ✅ Places API
     - ✅ Geocoding API (if enabled)

8. Click **SAVE**

⚠️ **IMPORTANT:** Changes may take 5-10 minutes to propagate!

---

### **Step 3: Verify Configuration in Your App**

#### **✅ Already Configured:**

**File: `app/build.gradle.kts`**
```kotlin
val mapsApiKey = "AIzaSyAr_FI6_F4nRAyVzzQzuZ3jm7EBjnob3GI"

defaultConfig {
    ...
    resValue("string", "google_maps_key", mapsApiKey)
    manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
}
```

**File: `AndroidManifest.xml`**
```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="${MAPS_API_KEY}" />
```

**File: `local.properties`**
```properties
# SHA1 Fingerprint: C6:0D:64:7B:6B:9E:08:CB:92:6E:64:2A:62:E0:4C:4B:21:FF:5D:EC
# Maps API Key is configured in app/build.gradle.kts
```

---

## 🔧 **TROUBLESHOOTING WHITE SCREEN**

### **Issue: Map Shows White/Blank Screen**

#### **Cause 1: API Key Not Enabled**
```
Solution:
1. Enable "Maps SDK for Android" in Google Cloud Console
2. Wait 5-10 minutes for changes to propagate
3. Reinstall app
```

#### **Cause 2: SHA1 Not Added**
```
Solution:
1. Add SHA1 fingerprint to API key restrictions
2. Package name: com.suraksha.app
3. SHA1: C6:0D:64:7B:6B:9E:08:CB:92:6E:64:2A:62:E0:4C:4B:21:FF:5D:EC
4. Wait 5-10 minutes
5. Reinstall app
```

#### **Cause 3: API Key Restrictions Too Strict**
```
Solution:
1. Temporarily set API key to "None" (unrestricted)
2. Test if map loads
3. If it works, re-add restrictions correctly
```

#### **Cause 4: Location Permission Denied**
```
Solution:
1. Grant location permissions in app settings
2. Or enable from app when prompted
```

#### **Cause 5: Internet Connection**
```
Solution:
1. Check internet connectivity
2. Try on WiFi if cellular data not working
```

---

## 🧪 **TESTING PROCEDURE**

### **Step 1: Check LogCat for Errors**

```bash
adb logcat | grep -E "Maps|Google|API"
```

**Look for these messages:**

**❌ Bad/Error:**
```
API key not found
Authentication failed
Maps initialization failed
```

**✅ Good:**
```
Successfully loaded Google Maps
Map ready
Location permission granted
```

---

### **Step 2: Test Map Loading**

1. Install APK:
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

2. Open app → Navigate to **Map** screen

3. Grant location permissions when prompted

4. Wait 3-5 seconds for map to load

5. You should see:
   - ✅ Map tiles loaded (dark themed)
   - ✅ Your current location (cyan marker)
   - ✅ Nearby police stations (red markers)
   - ✅ Nearby hospitals (green markers)
   - ✅ Zoom controls visible

---

### **Step 3: Verify API Key is Working**

#### **Quick Test:**
```bash
# Test API key directly
curl "https://maps.googleapis.com/maps/api/geocode/json?address=Mumbai&key=AIzaSyAr_FI6_F4nRAyVzzQzuZ3jm7EBjnob3GI"
```

**Expected Response:**
```json
{
  "results": [ ... ],
  "status": "OK"
}
```

**If you get error:**
```json
{
  "error_message": "This API key is not authorized to use this service or API.",
  "status": "REQUEST_DENIED"
}
```
→ API key not enabled for Maps SDK

---

## 📱 **EXPECTED BEHAVIOR**

### **When Map Loads Successfully:**

```
┌──────────────────────────────────┐
│           Map                    │
│       Safe Havens                │
│                                  │
│  Found: 3 Police, 2 Hospitals   │
│  [Cyan] You  [Red] Police        │
│  [Green] Hospital                │
│                                  │
│  ┌────────────────────────────┐ │
│  │        MAP VIEW            │ │
│  │   (Dark themed Google Map) │ │
│  │                            │ │
│  │   📍 Your Location         │ │
│  │   🚓 Police Stations       │ │
│  │   🏥 Hospitals             │ │
│  │                            │ │
│  │   [+] [-] Zoom controls    │ │
│  └────────────────────────────┘ │
└──────────────────────────────────┘
```

### **Features:**
- ✅ Dark theme map (matches app theme)
- ✅ Current location marker (cyan)
- ✅ Police station markers (red)
- ✅ Hospital markers (green)
- ✅ Tap markers to see name & address
- ✅ Zoom and pan controls
- ✅ Smooth camera animations

---

## 🔍 **DEBUGGING COMMANDS**

### **Check if Maps SDK is loaded:**
```bash
adb logcat | grep "MapsInitializer"
```

### **Check for API authentication errors:**
```bash
adb logcat | grep "Authentication"
```

### **Check for location updates:**
```bash
adb logcat | grep "MapScreen"
```

### **Monitor Places API calls:**
```bash
adb logcat | grep "Places"
```

---

## 🎯 **COMMON ERROR MESSAGES & FIXES**

### **Error: "API key not found"**
```
Cause: API key not in AndroidManifest.xml
Fix: Already configured in your app ✅
```

### **Error: "Authentication failed"**
```
Cause: SHA1 fingerprint doesn't match
Fix: Add your SHA1 to API key restrictions
SHA1: C6:0D:64:7B:6B:9E:08:CB:92:6E:64:2A:62:E0:4C:4B:21:FF:5D:EC
```

### **Error: "Maps SDK for Android is not enabled"**
```
Cause: API not enabled in Google Cloud Console
Fix: Go to APIs & Services → Enable Maps SDK for Android
```

### **Error: "This API key is not authorized"**
```
Cause: API restrictions too strict or wrong package name
Fix: 
1. Check package name: com.suraksha.app
2. Check SHA1 fingerprint matches
3. Temporarily remove restrictions to test
```

---

## 📋 **CHECKLIST**

### **Google Cloud Console:**
- ✅ Maps SDK for Android **ENABLED**
- ✅ Places API **ENABLED** (for safe havens)
- ✅ API Key created
- ✅ Android app restriction added
- ✅ Package name: `com.suraksha.app`
- ✅ SHA1 fingerprint: `C6:0D:64:7B:6B:9E:08:CB:92:6E:64:2A:62:E0:4C:4B:21:FF:5D:EC`
- ✅ API restrictions configured (Maps SDK + Places API)

### **App Configuration:**
- ✅ API key in `build.gradle.kts`
- ✅ API key in `AndroidManifest.xml`
- ✅ Maps Compose dependency added
- ✅ Location permissions in manifest
- ✅ Internet permission in manifest

### **Testing:**
- ✅ Build successful
- ✅ APK installed
- ✅ Location permission granted
- ✅ Internet connected
- ✅ Waited 5-10 min after API config changes

---

## 🚀 **BUILD & INSTALL**

### **Build APK:**
```bash
cd "C:\Users\rogue\OneDrive\Documents\MATLAB\Suraksha new - 2\Suraksha"
.\gradlew.bat assembleDebug
```

### **Install APK:**
```bash
adb install -r "app\build\outputs\apk\debug\app-debug.apk"
```

### **Test Map:**
```
1. Open app
2. Grant location permission
3. Navigate to Map screen (bottom nav)
4. Wait 3-5 seconds
5. Map should load with markers
```

---

## 🎯 **KEY POINTS**

### **SHA1 Fingerprint:**
```
C6:0D:64:7B:6B:9E:08:CB:92:6E:64:2A:62:E0:4C:4B:21:FF:5D:EC
```
**This MUST be added to your API key restrictions in Google Cloud Console!**

### **Package Name:**
```
com.suraksha.app
```
**This MUST match in Google Cloud Console restrictions!**

### **API Key:**
```
AIzaSyAr_FI6_F4nRAyVzzQzuZ3jm7EBjnob3GI
```
**This is already configured in your app ✅**

### **Wait Time:**
```
⚠️ After making changes in Google Cloud Console:
- Wait 5-10 minutes for changes to propagate
- Then rebuild and reinstall app
```

---

## 📞 **STILL NOT WORKING?**

### **Try this diagnostic sequence:**

1. **Remove all restrictions temporarily:**
   - Edit API key
   - Set Application restrictions: **None**
   - Set API restrictions: **Don't restrict key**
   - Save and wait 10 minutes
   - Test app

2. **If it works now:**
   - ✅ Problem is with restrictions
   - Add restrictions back one by one:
     1. First add Android app restriction (package + SHA1)
     2. Test
     3. Then add API restrictions
     4. Test

3. **If still doesn't work:**
   - Check LogCat for specific error messages
   - Verify internet connection
   - Verify location permissions granted
   - Try on different device/emulator

---

## 🎉 **EXPECTED RESULT**

After following all steps:
- ✅ Map loads with dark theme
- ✅ Your location shows as cyan marker
- ✅ Police stations show as red markers
- ✅ Hospitals show as green markers
- ✅ Can zoom and pan smoothly
- ✅ Can tap markers for details
- ✅ No white/blank screen

---

**🎯 YOUR SHA1 KEY IS NOW DOCUMENTED AND READY TO BE ADDED TO GOOGLE CLOUD CONSOLE!**

**Add it to your API key restrictions, wait 10 minutes, reinstall app, and the map should load!** 🗺️✅

