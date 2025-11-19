"""
Quick Start: Generate a Simple Working Fall Detection Model

This creates a basic TFLite model that can be used immediately for testing.
The model is simple and won't be very accurate, but it allows you to test
the ML pipeline while you collect real data for proper training.

Usage: python generate_simple_model.py
Output: simple_fall_model.tflite (copy to app/src/main/assets/model.tflite)
"""

import numpy as np
import tensorflow as tf
from tensorflow import keras
from tensorflow.keras import layers

# Must match FallInferenceManager settings
SEQ_LEN = 800
CHANNELS = 3
NUM_CLASSES = 10  # Number of labels in labels.txt

def create_simple_model():
    """Create a simple CNN model architecture"""
    model = keras.Sequential([
        layers.Input(shape=(SEQ_LEN, CHANNELS)),

        # Simple convolutional layers
        layers.Conv1D(16, 5, activation='relu', padding='same'),
        layers.MaxPooling1D(4),
        layers.Dropout(0.3),

        layers.Conv1D(32, 3, activation='relu', padding='same'),
        layers.MaxPooling1D(4),
        layers.Dropout(0.3),

        layers.Conv1D(64, 3, activation='relu', padding='same'),
        layers.GlobalAveragePooling1D(),

        # Classification head
        layers.Dense(32, activation='relu'),
        layers.Dropout(0.4),
        layers.Dense(NUM_CLASSES, activation='softmax')
    ])

    model.compile(
        optimizer='adam',
        loss='sparse_categorical_crossentropy',
        metrics=['accuracy']
    )

    return model

def generate_dummy_data():
    """Generate some dummy data to initialize weights"""
    # Create synthetic data for each class
    X = []
    y = []

    for class_id in range(NUM_CLASSES):
        for _ in range(10):
            # Generate random accelerometer data
            # Add slight class-specific patterns (for initialization)
            data = np.random.randn(SEQ_LEN, CHANNELS) * (0.5 + class_id * 0.1)
            X.append(data)
            y.append(class_id)

    return np.array(X, dtype=np.float32), np.array(y, dtype=np.int32)

def convert_to_tflite(model, output_path='simple_fall_model.tflite'):
    """Convert to TensorFlow Lite format"""
    converter = tf.lite.TFLiteConverter.from_keras_model(model)
    converter.optimizations = [tf.lite.Optimize.DEFAULT]
    tflite_model = converter.convert()

    with open(output_path, 'wb') as f:
        f.write(tflite_model)

    file_size = len(tflite_model) / 1024
    print(f"✅ TFLite model saved: {output_path} ({file_size:.1f} KB)")

def main():
    print("=" * 60)
    print("Generating Simple Fall Detection Model")
    print("=" * 60)

    print("\n⚠️  NOTE: This is a PLACEHOLDER model for testing only!")
    print("For accurate fall detection, you need to train with real data.")
    print()

    # Create model
    print("Creating model architecture...")
    model = create_model()

    # Generate dummy data
    print("Generating initialization data...")
    X_dummy, y_dummy = generate_dummy_data()

    # Quick training (just to initialize weights properly)
    print("Initializing model weights...")
    model.fit(X_dummy, y_dummy, epochs=5, batch_size=16, verbose=0)

    print("✅ Model created successfully")

    # Show model summary
    print("\nModel Summary:")
    model.summary()

    # Convert to TFLite
    print("\nConverting to TensorFlow Lite...")
    convert_to_tflite(model, 'simple_fall_model.tflite')

    print("\n" + "=" * 60)
    print("✅ COMPLETE!")
    print("=" * 60)
    print("\nNext steps:")
    print("1. Copy 'simple_fall_model.tflite' to:")
    print("   app/src/main/assets/model.tflite")
    print()
    print("2. Rebuild and install the app:")
    print("   ./gradlew assembleDebug")
    print()
    print("3. Test the ML pipeline:")
    print("   - Drop your phone (onto soft surface)")
    print("   - Check logcat: adb logcat | grep FallInference")
    print("   - You should see inference results")
    print()
    print("4. Collect real data and train a proper model:")
    print("   - Use the app to collect 50+ fall events")
    print("   - Run: python train_fall_model.py")
    print("   - Replace with the trained model")
    print()
    print("⚠️  Remember: This model won't be accurate - it's just for testing!")


if __name__ == '__main__':
    try:
        main()
    except Exception as e:
        print(f"\n❌ ERROR: {e}")
        print("\nMake sure you have TensorFlow installed:")
        print("  pip install tensorflow numpy")

