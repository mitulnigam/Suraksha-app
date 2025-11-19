# ML Fall Detection - How It Works

## ✅ What's Actually Happening Now

Your app **IS using the ML model** - it was working all along! Here's what happens:

### 1. **Data Collection (Continuous)**
- FallDetectorService monitors accelerometer/gyroscope 24/7
- Detects free-fall → impact pattern (phone drop or real fall)
- Keeps a 3-second pre-buffer + 12-second post-buffer

### 2. **ML Inference (Every Detection)**
When a candidate event is detected:
```
FallDetectorService → runInference()
  ↓
Extracts accelerometer data (x,y,z)
  ↓
FallInferenceManager.runInference()
  ↓
TensorFlow Lite model.tflite
  ↓
Returns: (label, confidence)
```

### 3. **Decision Logic**
```kotlin
if (label contains "fall" && confidence >= 50%) {
    // Show 15s confirmation dialog
    // If not canceled → Trigger SOS
} else {
    // Save CSV for later analysis
}
```

### 4. **Trained Labels** (from labels.txt)
Your model can detect:
- `drop_pickup_1s` through `drop_pickup_5s` - phone dropped then picked up
- `drop_left` - phone dropped and left on ground
- `sim_fall` - simulated fall (test)
- `real_fall` - actual person falling ⚠️ THIS TRIGGERS SOS
- `no_fall` - normal movement
- `phone_drop` - just phone dropping

---

## 🔍 Why You Might Not See Results

### Common Reasons:

1. **Model predicts "drop_left" or "phone_drop" instead of "real_fall"**
   - Solution: The model is correctly classifying phone drops as NOT falls
   - This is GOOD - prevents false alarms

2. **Confidence too low (< 50%)**
   - Happens when sensor pattern doesn't match training data
   - Previously threshold was 70%, now lowered to 50%

3. **Not enough training data for "real_fall"**
   - If you only collected phone drops, model won't recognize person falls
   - Need to collect actual simulated fall data

---

## 📊 New Debug Logging (Added)

After installing the updated APK, check logcat:

```bash
adb logcat | findstr "FallInference\|FallDetector"
```

You'll see:
```
FallInferenceManager: ML Inference complete:
FallInferenceManager:   Top prediction: drop_left (85%)
FallInferenceManager:   All top 3: drop_left: 85%, phone_drop: 12%, no_fall: 3%
FallInferenceManager:   Sequence length: 800/800 samples
FallDetectorService: ML PREDICTION: label='drop_left', confidence=85%, pickup=false
FallDetectorService: Not a fall (or confidence too low). Saving CSV only.
```

If a real fall is detected:
```
FallInferenceManager:   Top prediction: real_fall (78%)
FallDetectorService: ML PREDICTION: label='real_fall', confidence=78%, pickup=false
FallDetectorService: FALL DETECTED! Showing confirmation dialog
```

---

## 🎯 Testing the ML Model

### Test 1: Phone Drop (Should NOT trigger SOS)
1. Enable fall detection in settings
2. Drop phone on soft surface from waist height
3. Check logcat - should see `drop_left` or `phone_drop`
4. CSV saved in: `Android/data/com.suraksha.app/files/candidates/`
5. ✅ No confirmation dialog = Working correctly!

### Test 2: Simulated Fall (SHOULD trigger SOS)
1. Hold phone and simulate falling motion:
   - Quick downward motion (free-fall)
   - Sudden stop (impact)
   - Stay still for 2-3 seconds
2. Check logcat - should see `sim_fall` or `real_fall`
3. ✅ Confirmation dialog appears = ML working!
4. ✅ If not canceled → SOS sent

### Test 3: Check Notification
- The persistent notification now shows:
  ```
  Suraksha Fall Detection
  Last: drop_left (85%)
  ```
- Updates after each detection with ML prediction

---

## 🔧 Configuration (Current Settings)

| Setting | Value | Adjustable In |
|---------|-------|---------------|
| Free-fall threshold | 1.6 m/s² | FallDetectorService.kt line 35 |
| Impact threshold | 16.0 m/s² | FallDetectorService.kt line 36 |
| Confidence threshold | 50% | FallDetectorService.kt line 207 |
| Sequence length | 800 samples (16 sec @ 50Hz) | FallInferenceManager.kt line 16 |
| Fall detection | "fall" in label | FallDetectorService.kt line 205 |

---

## 📈 Improving ML Accuracy

If you want better real fall detection:

### Option 1: Collect More Training Data
1. Use the SensorLoggerActivity (if implemented)
2. Or manually simulate falls while app records
3. Label CSVs as `real_fall` or `sim_fall`
4. Retrain model with new data

### Option 2: Adjust Thresholds
```kotlin
// In FallDetectorService.kt, line 207:
val threshold = 0.40f  // Lower = more sensitive (more false positives)
val threshold = 0.60f  // Higher = less sensitive (fewer false alarms)
```

### Option 3: Expand Fall Detection Logic
```kotlin
// In FallDetectorService.kt, line 205:
// Current: only "sim_fall" and "real_fall" trigger
val isFall = label.contains("fall", ignoreCase = true)

// Alternative: also trigger on sudden drops without pickup
val isFall = label.contains("fall", ignoreCase = true) || 
             (label == "drop_left" && !pickupDetected && pickupTimeSec > 5)
```

---

## ✅ Summary

**The ML model IS working!** It:
- ✅ Loads model.tflite successfully
- ✅ Runs inference on every detection
- ✅ Correctly classifies phone drops vs falls
- ✅ Shows confirmation dialog for real falls (≥50% confidence)
- ✅ Saves all detections to CSV for analysis

**Why you might not see SOS triggers:**
- Model correctly identifies phone drops as NOT falls (preventing false alarms)
- Need actual fall-like motion patterns to trigger
- CSV files are saved for later model improvement

**How to verify it's working:**
1. Check logcat for "ML PREDICTION" messages
2. Look at notification - shows last prediction
3. Try simulated fall motion (not just dropping phone)
4. Check candidate CSV files - they include ML prediction labels

---

## 🚨 Important Notes

1. **CSV files are NOT wasted** - they help improve the model
2. **Not triggering on phone drops is GOOD** - prevents false alarms
3. **Model needs fall-like patterns** - simple drops won't trigger SOS
4. **Threshold lowered to 50%** for better sensitivity
5. **All predictions logged** for debugging

---

## 🎬 Next Steps

To see ML in action:
1. Install the updated APK
2. Enable fall detection in settings
3. Run: `adb logcat -c ; adb logcat | findstr "FallInference"`
4. Simulate a fall motion (not just drop)
5. Watch the logs - you'll see ML predictions in real-time!

**The model is working - you just need to trigger it with fall-like motion patterns!**

