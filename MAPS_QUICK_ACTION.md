# Google Maps SHA1 Setup - Quick Action Guide 🗺️

## ✅ **WHAT WAS DONE**

### **1. Documented Your Credentials**
```
SHA1 Fingerprint: C6:0D:64:7B:6B:9E:08:CB:92:6E:64:2A:62:E0:4C:4B:21:FF:5D:EC
API Key: AIzaSyAr_FI6_F4nRAyVzzQzuZ3jm7EBjnob3GI
Package Name: com.suraksha.app
```

### **2. Updated local.properties**
✅ Added SHA1 documentation for reference

### **3. Verified App Configuration**
✅ API key already configured in `build.gradle.kts`  
✅ Manifest already has meta-data tag  
✅ Maps Compose dependencies present  
✅ Location permissions configured  

### **4. Created Comprehensive Setup Guide**
✅ Step-by-step Google Cloud Console configuration  
✅ Troubleshooting for white screen issue  
✅ Testing procedures  
✅ Debugging commands  

### **5. Built APK Successfully**
✅ BUILD SUCCESSFUL in 1 second  
✅ APK ready at: `app/build/outputs/apk/debug/app-debug.apk`

---

## 🚨 **CRITICAL ACTION REQUIRED!**

### **YOU MUST DO THIS IN GOOGLE CLOUD CONSOLE:**

1. **Go to:** https://console.cloud.google.com/

2. **Navigate to:** APIs & Services → Credentials

3. **Find your API Key:** `AIzaSyAr_FI6_F4nRAyVzzQzuZ3jm7EBjnob3GI`

4. **Click Edit API Key**

5. **Under "Application restrictions":**
   - Select: ✅ **Android apps**
   - Click: **+ ADD AN ITEM**

6. **Enter EXACTLY:**
   ```
   Package name: com.suraksha.app
   SHA-1 fingerprint: C6:0D:64:7B:6B:9E:08:CB:92:6E:64:2A:62:E0:4C:4B:21:FF:5D:EC
   ```

7. **Under "API restrictions":**
   - Select: ✅ **Restrict key**
   - Check: ✅ **Maps SDK for Android**
   - Check: ✅ **Places API**

8. **Click SAVE**

9. **⏳ WAIT 5-10 MINUTES** for changes to propagate

10. **Reinstall app:**
    ```bash
    adb install -r "app\build\outputs\apk\debug\app-debug.apk"
    ```

---

## 🎯 **WHY MAP IS WHITE/BLANK**

### **Most Common Causes:**

#### **1. SHA1 Not Added (90% of cases)**
```
❌ Problem: API key doesn't recognize your app signature
✅ Solution: Add SHA1 to Google Cloud Console (steps above)
```

#### **2. Maps SDK Not Enabled**
```
❌ Problem: API not turned on
✅ Solution: 
   1. Go to: APIs & Services → Library
   2. Search: "Maps SDK for Android"
   3. Click ENABLE
```

#### **3. Wrong Package Name**
```
❌ Problem: Package name mismatch
✅ Solution: Must be exactly: com.suraksha.app
```

#### **4. Changes Not Propagated**
```
❌ Problem: Just made changes in console
✅ Solution: Wait 5-10 minutes, then reinstall app
```

---

## 📱 **TESTING AFTER SETUP**

### **Step 1: Install Fresh APK**
```bash
cd "C:\Users\rogue\OneDrive\Documents\MATLAB\Suraksha new - 2\Suraksha"
adb install -r "app\build\outputs\apk\debug\app-debug.apk"
```

### **Step 2: Open App & Grant Permissions**
```
1. Open Suraksha app
2. When prompted, grant Location permission
3. Navigate to Map screen (bottom navigation)
```

### **Step 3: Watch LogCat**
```bash
adb logcat | grep -E "MapScreen|Maps|Google"
```

**Look for:**
```
✅ "Location permission granted"
✅ "Starting search for safe havens"
✅ "Map ready"

❌ "Authentication failed" → SHA1 issue
❌ "API key not found" → Configuration issue
❌ "Maps SDK not enabled" → Enable in console
```

### **Step 4: Verify Map Loads**
```
After 3-5 seconds, you should see:
✅ Dark themed map
✅ Cyan marker (your location)
✅ Red markers (police stations)
✅ Green markers (hospitals)
✅ Zoom controls
```

---

## 🔍 **QUICK DIAGNOSTIC**

### **If map still white after 10 minutes:**

#### **Test 1: Check API Key Works**
```bash
curl "https://maps.googleapis.com/maps/api/geocode/json?address=Delhi&key=AIzaSyAr_FI6_F4nRAyVzzQzuZ3jm7EBjnob3GI"
```

**Should return:** `"status": "OK"`  
**If returns:** `"REQUEST_DENIED"` → API not enabled

#### **Test 2: Temporarily Remove Restrictions**
```
1. Edit API key in Google Cloud Console
2. Set restrictions to: None
3. Save
4. Wait 5 minutes
5. Test app
6. If works → Problem is with restrictions
7. Re-add restrictions correctly
```

#### **Test 3: Check LogCat for Specific Error**
```bash
adb logcat *:E | grep Maps
```

---

## 📋 **CHECKLIST**

### **In Google Cloud Console:**
- [ ] Project selected/created
- [ ] Maps SDK for Android **ENABLED**
- [ ] Places API **ENABLED**
- [ ] API key restrictions configured:
  - [ ] Android apps restriction
  - [ ] Package: `com.suraksha.app`
  - [ ] SHA1: `C6:0D:64:7B:6B:9E:08:CB:92:6E:64:2A:62:E0:4C:4B:21:FF:5D:EC`
- [ ] API restrictions configured:
  - [ ] Maps SDK for Android
  - [ ] Places API
- [ ] Saved changes
- [ ] Waited 10 minutes

### **On Device:**
- [ ] Fresh APK installed
- [ ] Location permission granted
- [ ] Internet connected (WiFi or mobile data)
- [ ] Waited 5 seconds on Map screen

---

## 🎯 **EXPECTED RESULT**

```
┌────────────────────────────────┐
│         Map Screen             │
│      Safe Havens               │
│                                │
│  Found: 3 Police, 2 Hospitals │
│  Legend: 🔵You 🔴Police 🟢Hospital │
│                                │
│  ╔══════════════════════════╗ │
│  ║   GOOGLE MAP LOADED      ║ │
│  ║   (Dark Theme)           ║ │
│  ║                          ║ │
│  ║   📍 Your Location       ║ │
│  ║   🚓 Police Stations     ║ │
│  ║   🏥 Hospitals           ║ │
│  ║                          ║ │
│  ╚══════════════════════════╝ │
└────────────────────────────────┘
```

---

## 📞 **STILL HAVING ISSUES?**

### **Run full diagnostic:**
```bash
# 1. Check app logs
adb logcat -c  # Clear logs
adb logcat | grep -E "MapScreen|MapViewModel|Places"

# 2. Check for authentication errors
adb logcat | grep Authentication

# 3. Check for API errors
adb logcat | grep "API"
```

### **Common Error Messages:**

**"PERMISSION_DENIED"**
```
→ SHA1 fingerprint not added or incorrect
→ Package name mismatch
```

**"API_KEY_INVALID"**
```
→ API key typo
→ API key deleted
```

**"REQUEST_DENIED"**
```
→ Maps SDK for Android not enabled
→ API restrictions too strict
```

---

## 🎉 **SUMMARY**

### **App Configuration:**
✅ **COMPLETE** - API key configured  
✅ **COMPLETE** - Manifest configured  
✅ **COMPLETE** - Dependencies added  
✅ **COMPLETE** - Build successful  

### **Your Action Required:**
⚠️ **PENDING** - Add SHA1 to Google Cloud Console  
⚠️ **PENDING** - Enable Maps SDK for Android  
⚠️ **PENDING** - Wait 10 minutes after changes  
⚠️ **PENDING** - Test map loading  

---

## 🔑 **QUICK COPY-PASTE**

**For Google Cloud Console:**
```
Package name: com.suraksha.app
SHA-1: C6:0D:64:7B:6B:9E:08:CB:92:6E:64:2A:62:E0:4C:4B:21:FF:5D:EC
API Key: AIzaSyAr_FI6_F4nRAyVzzQzuZ3jm7EBjnob3GI
```

**For Testing:**
```bash
# Install
adb install -r "app\build\outputs\apk\debug\app-debug.apk"

# Monitor
adb logcat | grep -E "MapScreen|Maps"
```

---

**🎯 NEXT STEPS:**

1. ✅ Read full guide: `GOOGLE_MAPS_SHA1_SETUP.md`
2. 🚨 Configure SHA1 in Google Cloud Console
3. ⏳ Wait 10 minutes
4. 📱 Install APK and test
5. 🗺️ Map should load successfully!

---

**BUILD SUCCESSFUL - APP READY - JUST NEED TO CONFIGURE GOOGLE CLOUD CONSOLE!** ✅🗺️

