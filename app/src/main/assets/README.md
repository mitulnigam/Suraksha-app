# Assets Folder - TensorFlow Lite Model Files

## Files Required

### 1. fall_cnn.tflite
**Status:** ⚠️ PLACEHOLDER - Replace with your trained model

This is your trained TensorFlow Lite model that classifies fall events.

**To replace:**
1. Train your CNN model using the candidate CSV files from: 
   `Android/data/com.suraksha.app/files/candidates/`
2. Convert your trained model to TensorFlow Lite format (.tflite)
3. Replace the placeholder `fall_cnn.tflite` file with your trained model
4. Ensure the model input/output shapes match your preprocessing code

**Expected model specs:**
- Input: Preprocessed sensor data (ACC + GYR, 3s pre + impact window)
- Output: Class probabilities for each label in labels.txt
- Format: TensorFlow Lite (.tflite)

### 2. labels.txt
**Status:** ✅ Created with sample labels

Contains one label per line corresponding to your model's output classes.

**Current labels (example):**
- drop_pickup_1s
- drop_pickup_2s
- drop_pickup_3s
- drop_pickup_4s
- drop_pickup_5s
- drop_left
- sim_fall
- real_fall
- no_fall
- phone_drop

**To customize:**
Edit this file to match your model's exact training labels. Order matters - line N corresponds to output index N.

## Integration Steps

1. **Collect training data** using FallDetectorService
2. **Train your model** on the candidate CSVs
3. **Convert to TFLite** format
4. **Replace** fall_cnn.tflite with your trained model
5. **Update** labels.txt to match your model's classes
6. **Build and test** the app with inference enabled

## Model Training Workflow

```
Candidate CSVs → Feature Engineering → Train CNN → Convert to TFLite → Deploy to assets/
```

See FallDetectorService for candidate collection and labeling logic.

## FallInferenceManager Integration

The `FallInferenceManager` class (located at `app/src/main/java/com/suraksha/app/ml/FallInferenceManager.kt`) handles:

- **Model Loading**: Loads `fall_cnn.tflite` from assets on initialization
- **Label Loading**: Reads `labels.txt` for class names
- **Inference**: Runs predictions on sensor data windows
- **Normalization**: Converts accelerometer data from m/s² to g-units (÷9.81)

### Key Parameters

```kotlin
SEQ_LEN = 800      // Must match your model's expected sequence length
CHANNELS = 3       // X, Y, Z accelerometer axes
```

⚠️ **Important**: Update `SEQ_LEN` in `FallInferenceManager.kt` to match your trained model's input shape.

### Usage Example

```kotlin
val inferenceManager = FallInferenceManager(context)

// Prepare sensor data as List<Triple<Float,Float,Float>>
val accWindow = listOf(
    Triple(0.1f, 0.2f, 9.8f),  // x, y, z in m/s²
    // ... more samples
)

// Run inference
val (label, confidence) = inferenceManager.runInference(accWindow)
Log.i(TAG, "Predicted: $label with ${(confidence*100).toInt()}% confidence")

// Clean up when done
inferenceManager.close()
```

### Integration with FallDetectorService

To enable real-time fall classification:
1. Create `FallInferenceManager` instance in service
2. After collecting candidate window, extract ACC data
3. Call `runInference()` with the window
4. Use prediction to determine if SOS should trigger

See integration examples in service implementation docs.
