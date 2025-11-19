# Quick Fix Checklist ✓

## ⚡ Do These 5 Things Right Now:

### ☑️ 1. Enable Places API
- Go to: https://console.cloud.google.com/
- Navigate to: **APIs & Services** → **Library**
- Search: **"Places API (New)"**
- Click: **ENABLE**

### ☑️ 2. Enable Billing
- In Google Cloud Console → **Billing**
- Link a credit card (required, but you get $200 FREE per month)
- This is **REQUIRED** for Places API to work

### ☑️ 3. Check API Key
- Go to: **APIs & Services** → **Credentials**
- Find your key: `AIzaSyB_zzihs-ogi-GK5WMUeILm7D3r1TmyQRU`
- Make sure these are enabled:
  - ✅ Maps SDK for Android
  - ✅ Places API (New)

### ☑️ 4. Rebuild App
```bash
# In Android Studio:
Build → Rebuild Project
```

### ☑️ 5. Test
- Uninstall old app
- Install and run new build
- Grant location permission
- Open Map screen
- Wait 10 seconds

## 🔍 Check if It Works:

### In Logcat (Filter: "MapViewModel"):
**GOOD** ✅:
```
D/MapViewModel: Search response received... Total places: 15
D/MapViewModel: Added 15 new unique havens
```

**BAD** ❌:
```
E/MapViewModel: This API key is not authorized
E/MapViewModel: REQUEST_DENIED: Billing not enabled
```

### On Screen:
- **Working**: "Found: 8 Police Stations, 12 Hospitals (within 5km)"
- **Not Working**: "Found: 0 Police Stations, 0 Hospitals (within 5km)"

## 📞 Still Not Working?

Read the detailed guides:
1. **PLACES_API_SETUP.md** - Detailed setup instructions
2. **FIX_SUMMARY.md** - Complete troubleshooting guide

---

**Most Common Issue**: Places API (New) not enabled + No billing ← Fix this first!

