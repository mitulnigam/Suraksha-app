# Fall Detection ML Integration - Setup Instructions

## ✅ What's Been Completed

The following files have been created/updated for ML-based fall detection:

1. **FallInferenceManager.kt** - TensorFlow Lite inference engine
2. **FallDetectorService.kt** - Fully integrated service with ML detection
3. **ConfirmationActivity.kt** - 15-second countdown UI before SOS
4. **activity_confirmation.xml** - Layout for confirmation screen
5. **SosReceiver.kt** - Broadcast receiver to trigger SOS
6. **AndroidManifest.xml** - Updated with all necessary declarations

## 📋 What You Need to Do Next

### 1. Add Model Files to Assets

Create the `assets` folder if it doesn't exist, then add:

**Location:** `app/src/main/assets/`

**Files needed:**
- `model.tflite` - Your trained TensorFlow Lite model
- `labels.txt` - One label per line matching your model's output

**Example `labels.txt`:**
```
drop_pickup_1s
drop_pickup_2s
drop_pickup_3s
drop_pickup_4s
drop_pickup_5s
drop_left
sim_fall
real_fall
no_fall
phone_drop
```

### 2. Start the Fall Detection Service

Add this code wherever you want to start fall detection (e.g., in `MainActivity` or settings toggle):

```kotlin
val intent = Intent(this, FallDetectorService::class.java)
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    startForegroundService(intent)
} else {
    startService(intent)
}
```

### 3. Stop the Service

```kotlin
val intent = Intent(this, FallDetectorService::class.java)
stopService(intent)
```

## 🔧 How It Works

### Detection Flow:
1. **Sensors Active** - Service continuously monitors accelerometer/gyroscope at 50 Hz
2. **Free-fall Detection** - VM < 1.6 m/s² triggers free-fall state
3. **Impact Detection** - Peak VM > 16.0 m/s² within 2 seconds = candidate event
4. **Post-Collection** - Records 12 seconds after impact
5. **ML Inference** - Runs TensorFlow Lite model on collected data
6. **Decision** - If confidence > 70% and label contains "fall":
   - Shows **ConfirmationActivity** with 15-second countdown
   - User can tap CANCEL to stop
   - After 15s, broadcasts SOS trigger

### Thresholds (tunable):
- **Free-fall:** 1.6 m/s²
- **Impact:** 16.0 m/s²
- **Pickup:** 15.0 m/s²
- **Confidence:** 70%

## 📁 File Locations

```
app/src/main/
├── assets/
│   ├── model.tflite          ← ADD THIS
│   └── labels.txt             ← ADD THIS
├── java/com/suraksha/app/
│   ├── ml/
│   │   ├── FallInferenceManager.kt  ✅
│   │   └── FallDetectionExample.kt  (existing)
│   ├── services/
│   │   └── FallDetectorService.kt   ✅
│   ├── ui/
│   │   └── ConfirmationActivity.kt  ✅
│   └── sos/
│       └── SosReceiver.kt           ✅
├── res/layout/
│   └── activity_confirmation.xml    ✅
└── AndroidManifest.xml              ✅
```

## 🧪 Testing

1. **Install app** with model files in assets
2. **Start service** from your UI
3. **Simulate fall:**
   - Drop phone from ~1 meter onto soft surface (bed/couch)
   - Should detect: free-fall → impact → pickup
4. **Check logs:**
   ```
   adb logcat | grep FallDetector
   ```
5. **If fall detected:**
   - Confirmation screen appears
   - 15-second countdown
   - Tap CANCEL to stop, or wait for SOS

## 🔍 Debug Data

All detected events are saved to:
```
Android/data/com.suraksha.app/files/candidates/candidate_*.csv
```

Format:
```csv
label,real_fall
impact_ts,1700000000000
pickup_detected,false
pickup_time_sec,-1
sample_hz,50

ts_ms,ts_nano,type,x,y,z,vm
1700000000000,123456789,ACC,0.1,0.2,9.8,9.82
...
```

## ⚙️ Customization

### Adjust Thresholds
Edit `FallDetectorService.kt`:
```kotlin
private val freeFallThreshold = 1.6    // lower = more sensitive
private val impactThreshold = 16.0     // lower = more sensitive
private val pickupThreshold = 15.0
```

### Adjust Confidence
```kotlin
val threshold = 0.70f  // 70% confidence minimum
```

### Adjust Countdown Time
Edit `ConfirmationActivity.kt`:
```kotlin
private val totalMs = 15000L // 15 seconds
```

## 🚨 Integration with Existing SOS

The `SosReceiver` sends an Intent to your existing `SurakshaService`:
```kotlin
val sosIntent = Intent(context, SurakshaService::class.java).apply {
    action = "TRIGGER_SOS"
}
context.startService(sosIntent)
```

Make sure your `SurakshaService` handles the "TRIGGER_SOS" action.

## ✨ Next Steps

1. ✅ Code is complete
2. 📦 Add `model.tflite` and `labels.txt` to assets
3. 🔌 Add service start/stop buttons in settings
4. 🧪 Test with real drops
5. 🎯 Fine-tune thresholds based on testing
6. 🚀 Deploy!

## 📞 Support

If you encounter issues:
- Check logcat: `adb logcat | grep -E "FallDetector|FallInference"`
- Verify model files exist in assets
- Ensure service is started
- Check manifest declarations

---
**Status:** ✅ Complete - Ready for model files and testing

