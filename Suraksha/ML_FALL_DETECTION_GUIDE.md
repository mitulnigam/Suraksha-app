# 🚀 ML Fall Detection - Quick Start Guide

## Current Status

Your app has the ML fall detection infrastructure ready, but needs actual model files.

## Option 1: Quick Test (Immediate - Use Existing Code)

Since model training requires data collection first, I've configured your app to work in "data collection mode":

### What's Working NOW:
✅ FallDetectorService collects sensor data
✅ Detects potential falls (free-fall + impact)
✅ Saves CSV files with all sensor data
✅ Logs events for later training

### CSV Files Location:
```
Android/data/com.suraksha.app/files/candidates/
```

Each file contains:
- 3 seconds of data BEFORE the fall
- 12 seconds of data AFTER the fall
- Metadata (impact time, pickup detection, etc.)

---

## Option 2: Enable Full ML Detection (Recommended Workflow)

### Step 1: Collect Training Data (1-2 weeks)

**Install and use the app:**
1. Install current version
2. Enable fall detection in settings
3. Collect various scenarios:
   - Real falls (safely on soft surface)
   - Phone drops (from different heights)
   - Picking up phone after drop
   - False positives (sitting down, etc.)
   - Target: 50-100 events per class

### Step 2: Train Your Model

**Prerequisites:**
```bash
pip install tensorflow numpy pandas scikit-learn
```

**Training:**
```bash
# 1. Copy CSV files from phone to computer
adb pull /sdcard/Android/data/com.suraksha.app/files/candidates/ ./training_data/

# 2. Run training script
python train_fall_model.py

# Output: fall_model.tflite
```

### Step 3: Deploy Model

```bash
# Copy trained model to assets
cp fall_model.tflite app/src/main/assets/model.tflite

# Rebuild app
./gradlew assembleDebug

# Install
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Step 4: Test ML Detection

- Drop phone
- Check logcat: `adb logcat | grep FallInference`
- Should see: "Result: real_fall (89%)"
- ConfirmationActivity appears if confidence > 70%

---

## Option 3: Use Pre-trained Model (Advanced)

If you have access to a pre-trained fall detection model:

1. Convert to TFLite format
2. Ensure input shape: `[1, 800, 3]` (batch, seq_len, channels)
3. Copy to `app/src/main/assets/model.tflite`
4. Update `labels.txt` with your classes
5. Rebuild and test

---

## Current App Behavior (Without ML Model)

### ✅ What Works:
- Fall detection (rule-based: free-fall + impact)
- Sensor data logging (50 Hz)
- CSV file generation
- All app features

### ⏸️ What's Disabled:
- ML classification
- Confidence scoring
- Automatic fall vs phone-drop distinction

### Detection Flow:
```
Free-fall detected (VM < 1.6 m/s²)
    ↓
Impact detected (VM > 16.0 m/s²)
    ↓
Data collected (3s pre + 12s post)
    ↓
❌ ML inference skipped (no model)
    ↓
✅ CSV saved for training: candidate_*.csv
```

---

## Quick Test Without ML

Want to test the system now? Here's what to do:

### 1. Enable Fall Detection
- Open app → Settings
- Enable "Fall Detection" toggle

### 2. Test Detection
- Drop phone from ~1 meter onto bed/couch
- Service will detect and log the event

### 3. Check Results
```bash
# View logs
adb logcat | grep FallDetector

# Expected output:
# FallDetectorService: FREE FALL detected @1700000000000
# FallDetectorService: IMPACT detected peak=28.5 at 1700000001200
# FallDetectorService: Saved CSV: .../candidate_20251116_153045.csv
# FallInferenceManager: ML not available - data logging only
```

### 4. Collect Data
Repeat different scenarios to build your training dataset.

---

## Training Data Collection Tips

### Good Practices:
✅ Drop from various heights (0.5m - 1.5m)
✅ Different surfaces (carpet, hardwood, tile)
✅ Different phone orientations
✅ Include false positives (running, jumping, etc.)
✅ Label events immediately after recording
✅ Aim for balanced classes (similar number per type)

### Labeling Strategy:
The CSV files auto-label based on pickup time:
- `drop_pickup_1s` - Phone picked up ~1 second after
- `drop_pickup_2s` - Picked up ~2 seconds after
- `drop_left` - Phone left on ground
- You can manually relabel files for training

---

## Model Training Parameters

### Recommended Settings (in train_fall_model.py):

```python
SEQ_LEN = 800          # Sequence length (matches app)
CHANNELS = 3           # x, y, z accelerometer
BATCH_SIZE = 32
EPOCHS = 50
LEARNING_RATE = 0.001
```

### Expected Results:
- Training time: 10-30 minutes (CPU)
- Accuracy target: >85% on test set
- Model size: ~500 KB - 2 MB

---

## Troubleshooting

### "Model not found" in logs
✅ **Expected** - Continue using data collection mode

### No CSV files generated
- Check if fall detection enabled in settings
- Verify service is running (notification should show)
- Try dropping from higher (1m+)
- Check thresholds in FallDetectorService.kt

### Can't find CSV files
```bash
# List files
adb shell ls /sdcard/Android/data/com.suraksha.app/files/candidates/

# Pull all files
adb pull /sdcard/Android/data/com.suraksha.app/files/candidates/ ./
```

---

## Current Configuration

### Your App Status:
- ✅ ML infrastructure: **Ready**
- ✅ Data collection: **Active**
- ✅ Service running: **Yes**
- ⏸️ ML model: **Not yet trained**

### Next Action:
**Collect data first, then train model**

This is the recommended approach - you'll get a model specifically trained for your phone and usage patterns!

---

## Alternative: Skip ML (Use Rule-Based Only)

If you don't want ML-based classification, you can:

1. Modify FallDetectorService to trigger SOS directly on impact
2. Remove ML inference call
3. Use simple threshold-based detection

Let me know if you want me to configure this option instead!

---

## Summary

**Status:** ML detection is ready to go once you have a trained model

**Current Mode:** Data collection & rule-based detection

**Recommended Path:**
1. Use app for 1-2 weeks collecting data
2. Train model with collected CSV files
3. Deploy trained model
4. Full ML detection enabled!

**Quick Test Path (Today):**
- Generate simple model with Python script
- Test ML pipeline
- Replace with real model later

---

**Need Help?**
- Training script: `train_fall_model.py`
- Quick test script: `generate_simple_model.py`
- Both scripts are in your project root

**Questions?** Let me know and I'll help you get ML detection fully operational!

