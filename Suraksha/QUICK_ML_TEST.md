# Quick ML Fall Detection Test

## Install Updated APK
```powershell
adb install -r "C:\Users\rogue\OneDrive\Documents\MATLAB\Suraksha new - 2\Suraksha\app\build\outputs\apk\debug\app-debug.apk"
```

## Start Logging
```powershell
adb logcat -c
adb logcat | Select-String -Pattern "FallInference|FallDetector|Suraksha"
```

## Enable Fall Detection
1. Open app → Settings
2. Enable "Fall Detection"
3. You should see notification: "Suraksha Fall Detection: Fall detection active"

## Test Scenarios

### ❌ Test 1: Phone Drop (Should NOT trigger SOS)
**Action:**
- Drop phone on soft surface from waist height

**Expected Logs:**
```
FallDetectorService: FREE-FALL detected at [timestamp] vm=1.88
FallDetectorService: IMPACT detected peak=18.5 at [timestamp]
FallInferenceManager: Top prediction: drop_left (85%)
FallDetectorService: ML PREDICTION: label='drop_left', confidence=85%, pickup=false
FallDetectorService: Not a fall (or confidence too low). Saving CSV only.
```

**Expected Result:** ✅ No confirmation dialog (correct - prevents false alarm!)

---

### ✅ Test 2: Simulated Fall (SHOULD trigger SOS)
**Action:**
1. Hold phone in hand
2. Quickly move phone downward (simulate falling)
3. Sudden stop (simulate hitting ground)
4. Keep still for 2-3 seconds

**Expected Logs:**
```
FallDetectorService: FREE-FALL detected
FallDetectorService: IMPACT detected
FallInferenceManager: Top prediction: sim_fall (72%)
FallDetectorService: ML PREDICTION: label='sim_fall', confidence=72%, pickup=false
FallDetectorService: FALL DETECTED! Showing confirmation dialog
```

**Expected Result:** ✅ Confirmation dialog appears with 15s countdown

---

### 🔍 Test 3: Check What Model Sees
**Action:**
- Any detection event

**Expected Logs:**
```
FallInferenceManager: ML Inference complete:
FallInferenceManager:   Top prediction: drop_left (85%)
FallInferenceManager:   All top 3: drop_left: 85%, phone_drop: 12%, no_fall: 3%
FallInferenceManager:   Sequence length: 800/800 samples
```

**What This Shows:**
- Model ran successfully
- Analyzed 800 samples (16 seconds @ 50Hz)
- Top 3 predictions with confidence scores
- Model is actively classifying!

---

## Check Saved Data

### View Candidate CSV Files
```powershell
adb shell "ls -lh /sdcard/Android/data/com.suraksha.app/files/candidates/"
```

### Pull Latest CSV
```powershell
adb shell "ls -t /sdcard/Android/data/com.suraksha.app/files/candidates/*.csv | head -1" | ForEach-Object { adb pull $_ . }
```

### CSV Format
Each file contains:
```csv
label,drop_left
impact_ts,1731722345123
pickup_detected,false
pickup_time_sec,-1
sample_hz,50
rows,800

ts_ms,ts_nano,type,x,y,z,vm
1731722340000,12345678,ACC,0.12,9.81,0.05,9.82
...
```

**The label field is the ML prediction!**

---

## Troubleshooting

### "ML not available" in logs
**Cause:** Model failed to load
**Fix:** Check assets folder has model.tflite and labels.txt

### "Inference error" in logs
**Cause:** Model input shape mismatch
**Fix:** Verify SEQ_LEN=800 matches your model

### Only seeing "drop_left" predictions
**Cause:** Model correctly identifies phone drops
**Fix:** This is CORRECT behavior! Try simulated fall motion instead

### No logs appearing
**Cause:** Service not running
**Fix:** 
```powershell
adb shell "am start-foreground-service -n com.suraksha.app/.services.FallDetectorService"
```

---

## Understanding Predictions

| Label | Meaning | SOS Trigger? |
|-------|---------|--------------|
| `real_fall` | Person falling | ✅ YES (if conf ≥50%) |
| `sim_fall` | Simulated fall | ✅ YES (if conf ≥50%) |
| `drop_left` | Phone dropped, not picked up | ❌ No |
| `drop_pickup_3s` | Phone dropped, picked up in 3s | ❌ No |
| `phone_drop` | Simple phone drop | ❌ No |
| `no_fall` | Normal movement | ❌ No |

**Only labels containing "fall" trigger SOS!**

---

## Adjust Sensitivity

If you want MORE sensitive detection (more false alarms):

**Option 1: Lower confidence threshold**
Edit `FallDetectorService.kt` line 207:
```kotlin
val threshold = 0.30f  // was 0.50f
```

**Option 2: Expand triggering labels**
Edit `FallDetectorService.kt` line 205:
```kotlin
val isFall = label.contains("fall", ignoreCase = true) || 
             label == "drop_left"  // Also trigger on drops left behind
```

Then rebuild:
```powershell
.\gradlew.bat assembleDebug
```

---

## Success Criteria

✅ **ML is working if you see:**
1. Logs show "ML Inference complete"
2. Logs show "Top prediction: [label] ([confidence]%)"
3. Notification updates with predictions
4. CSV files have ML-predicted labels

✅ **SOS trigger is working if:**
1. Simulated fall motion triggers confirmation dialog
2. Phone drops do NOT trigger (prevents false alarms)
3. Logs show "FALL DETECTED! Showing confirmation dialog"

---

## Real-Time Monitoring Command
```powershell
adb logcat -c ; adb logcat | Select-String -Pattern "FallInference|ML PREDICTION|FALL DETECTED"
```

This shows only the important ML prediction logs!

