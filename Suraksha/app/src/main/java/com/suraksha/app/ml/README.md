# Machine Learning Package

This package contains TensorFlow Lite inference components for fall detection classification.

## Files

### 1. `TFLiteModel.kt`
Basic TensorFlow Lite wrapper for running inference.

**Key features:**
- Loads `model.tflite` from assets
- Expects input shape: `[1, SEQ_LEN, 3]` (batch, timesteps, channels)
- Returns output: `FloatArray(10)` with class probabilities

**Usage:**
```kotlin
val model = TFLiteModel(context)

// Prepare input [1, 800, 3]
val input = Array(1) { Array(800) { FloatArray(3) } }

// Fill with normalized sensor data
for (i in 0 until 800) {
    input[0][i][0] = accelX[i] / 9.81f  // x-axis in g-units
    input[0][i][1] = accelY[i] / 9.81f  // y-axis
    input[0][i][2] = accelZ[i] / 9.81f  // z-axis
}

// Run inference
val probabilities = model.predict(input)

// Get top prediction
val maxIdx = probabilities.indices.maxByOrNull { probabilities[it] } ?: 0
val confidence = probabilities[maxIdx]
Log.d("ML", "Predicted class: $maxIdx with ${(confidence * 100).toInt()}% confidence")

// Clean up
model.close()
```

### 2. `FallInferenceManager.kt`
Advanced inference manager with label support and data preprocessing.

**Key features:**
- Loads `fall_cnn.tflite` and `labels.txt` from assets
- Accepts `List<Triple<Float,Float,Float>>` (sensor data)
- Automatic padding/trimming to SEQ_LEN
- Returns human-readable labels with confidence

**Usage:**
```kotlin
val manager = FallInferenceManager(context)

// Prepare sensor data
val accWindow = listOf(
    Triple(0.1f, 0.2f, 9.8f),
    // ... more samples
)

// Run inference
val (label, confidence) = manager.runInference(accWindow)
Log.i(TAG, "Prediction: $label with ${(confidence * 100).toInt()}% confidence")

// Clean up
manager.close()
```

### 3. `FallDetectionExample.kt`
Complete example showing how to integrate with FallDetectorService.

**Key features:**
- Demonstrates real-world integration
- Shows how to extract ACC data from SensorRow list
- Includes dummy inference for testing
- Logs top 3 predictions for debugging

**Usage:**
```kotlin
val example = FallDetectionExample(context)

// With real sensor data
val (label, confidence) = example.classifyFallEvent(sensorRows)

// With dummy data (for testing)
example.runDummyInference()

// Clean up
example.close()
```

## Model Requirements

### Input Shape
`[1, SEQ_LEN, 3]`
- Batch size: 1
- SEQ_LEN: 800 (default, adjust to your model)
- Channels: 3 (X, Y, Z accelerometer)

### Output Shape
`[1, 10]`
- 10 classes corresponding to `labels.txt`

### Normalization
Accelerometer data should be normalized to g-units:
```kotlin
normalizedValue = rawValue / 9.81f
```

## Labels (from assets/labels.txt)

0. drop_pickup_1s
1. drop_pickup_2s
2. drop_pickup_3s
3. drop_pickup_4s
4. drop_pickup_5s
5. drop_left
6. sim_fall
7. real_fall
8. no_fall
9. phone_drop

## Integration with FallDetectorService

```kotlin
// In FallDetectorService, after collecting candidate data:

private val inferenceManager by lazy { FallInferenceManager(applicationContext) }

private fun finishAndSaveCandidate() {
    // ... existing code to collect sensor data
    
    // Extract accelerometer data
    val accWindow = fullWindow
        .filter { it.type == "ACC" }
        .map { Triple(it.x.toFloat(), it.y.toFloat(), it.z.toFloat()) }
    
    // Run inference
    val (label, confidence) = inferenceManager.runInference(accWindow)
    
    Log.i(TAG, "ML Prediction: $label (${(confidence * 100).toInt()}%)")
    
    // Trigger SOS only for real falls with high confidence
    if ((label.contains("real_fall") || label.contains("sim_fall")) && confidence > 0.75f) {
        // Trigger SOS alert
        triggerSOS()
    }
    
    // ... existing code to save CSV
}
```

## Notes

- TensorFlow Lite errors will resolve after Gradle sync
- Replace placeholder model files in `assets/` with your trained model
- Update `SEQ_LEN` constant to match your model's input shape
- All warnings about "never used" are normal until you integrate these classes

## Dependencies

```gradle
implementation("org.tensorflow:tensorflow-lite:2.15.0")
```

