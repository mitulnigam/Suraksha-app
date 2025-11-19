"""
Simple Fall Detection Model Training Script
Uses TensorFlow to train a CNN model for fall classification

Requirements:
pip install tensorflow numpy pandas scikit-learn

Usage:
1. Collect sensor data using the FallDetectorService (generates CSV files)
2. Copy CSV files from Android/data/com.suraksha.app/files/candidates/ to ./training_data/
3. Run: python train_fall_model.py
4. Output: fall_model.tflite (copy to app/src/main/assets/model.tflite)
"""

import numpy as np
import pandas as pd
import tensorflow as tf
from tensorflow import keras
from tensorflow.keras import layers
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import LabelEncoder
import os
import glob

# Configuration
SEQ_LEN = 800  # Must match FallInferenceManager.SEQ_LEN
CHANNELS = 3   # x, y, z accelerometer
BATCH_SIZE = 32
EPOCHS = 50
LEARNING_RATE = 0.001

# Labels (must match labels.txt)
LABELS = [
    'drop_pickup_1s',
    'drop_pickup_2s',
    'drop_pickup_3s',
    'drop_pickup_4s',
    'drop_pickup_5s',
    'drop_left',
    'sim_fall',
    'real_fall',
    'no_fall',
    'phone_drop'
]

def load_csv_data(csv_path):
    """Load sensor data from CSV file"""
    df = pd.read_csv(csv_path, skiprows=8)  # Skip metadata rows

    # Extract metadata
    with open(csv_path, 'r') as f:
        lines = f.readlines()
        label = lines[0].split(',')[1].strip()

    # Extract accelerometer data only
    acc_data = df[df['type'] == 'ACC'][['x', 'y', 'z']].values

    return acc_data, label

def preprocess_data(acc_data):
    """Normalize and pad/truncate to SEQ_LEN"""
    # Normalize to g-units
    acc_data = acc_data / 9.81

    # Pad or truncate to SEQ_LEN
    if len(acc_data) < SEQ_LEN:
        # Pad with zeros
        padding = np.zeros((SEQ_LEN - len(acc_data), CHANNELS))
        acc_data = np.vstack([acc_data, padding])
    elif len(acc_data) > SEQ_LEN:
        # Take last SEQ_LEN samples
        acc_data = acc_data[-SEQ_LEN:]

    return acc_data

def load_dataset(data_dir='./training_data'):
    """Load all CSV files from directory"""
    csv_files = glob.glob(os.path.join(data_dir, 'candidate_*.csv'))

    X = []
    y = []

    print(f"Loading {len(csv_files)} CSV files...")

    for csv_file in csv_files:
        try:
            acc_data, label = load_csv_data(csv_file)
            acc_data = preprocess_data(acc_data)
            X.append(acc_data)
            y.append(label)
        except Exception as e:
            print(f"Error loading {csv_file}: {e}")

    X = np.array(X)
    y = np.array(y)

    print(f"Loaded {len(X)} samples")
    print(f"Unique labels: {np.unique(y)}")

    return X, y

def create_model(num_classes):
    """Create a simple CNN model for fall classification"""
    model = keras.Sequential([
        # Input shape: (SEQ_LEN, CHANNELS)
        layers.Input(shape=(SEQ_LEN, CHANNELS)),

        # Conv1D layers
        layers.Conv1D(32, 3, activation='relu', padding='same'),
        layers.BatchNormalization(),
        layers.MaxPooling1D(2),
        layers.Dropout(0.2),

        layers.Conv1D(64, 3, activation='relu', padding='same'),
        layers.BatchNormalization(),
        layers.MaxPooling1D(2),
        layers.Dropout(0.2),

        layers.Conv1D(128, 3, activation='relu', padding='same'),
        layers.BatchNormalization(),
        layers.MaxPooling1D(2),
        layers.Dropout(0.3),

        # Global pooling
        layers.GlobalAveragePooling1D(),

        # Dense layers
        layers.Dense(128, activation='relu'),
        layers.Dropout(0.4),
        layers.Dense(64, activation='relu'),
        layers.Dropout(0.3),

        # Output layer
        layers.Dense(num_classes, activation='softmax')
    ])

    model.compile(
        optimizer=keras.optimizers.Adam(learning_rate=LEARNING_RATE),
        loss='sparse_categorical_crossentropy',
        metrics=['accuracy']
    )

    return model

def convert_to_tflite(model, output_path='fall_model.tflite'):
    """Convert Keras model to TensorFlow Lite"""
    converter = tf.lite.TFLiteConverter.from_keras_model(model)
    converter.optimizations = [tf.lite.Optimize.DEFAULT]
    tflite_model = converter.convert()

    with open(output_path, 'wb') as f:
        f.write(tflite_model)

    print(f"TFLite model saved to: {output_path}")

def main():
    print("=" * 60)
    print("Fall Detection Model Training")
    print("=" * 60)

    # Load data
    if not os.path.exists('./training_data'):
        print("ERROR: ./training_data directory not found!")
        print("Please create it and add your candidate_*.csv files")
        return

    X, y = load_dataset('./training_data')

    if len(X) == 0:
        print("ERROR: No data loaded!")
        return

    # Encode labels
    label_encoder = LabelEncoder()
    y_encoded = label_encoder.fit_transform(y)
    num_classes = len(label_encoder.classes_)

    print(f"\nClasses ({num_classes}):")
    for i, label in enumerate(label_encoder.classes_):
        print(f"  {i}: {label}")

    # Split data
    X_train, X_test, y_train, y_test = train_test_split(
        X, y_encoded, test_size=0.2, random_state=42, stratify=y_encoded
    )

    print(f"\nTrain samples: {len(X_train)}")
    print(f"Test samples: {len(X_test)}")

    # Create and train model
    print("\nCreating model...")
    model = create_model(num_classes)
    model.summary()

    print("\nTraining model...")
    history = model.fit(
        X_train, y_train,
        batch_size=BATCH_SIZE,
        epochs=EPOCHS,
        validation_split=0.2,
        callbacks=[
            keras.callbacks.EarlyStopping(
                monitor='val_loss',
                patience=10,
                restore_best_weights=True
            ),
            keras.callbacks.ReduceLROnPlateau(
                monitor='val_loss',
                factor=0.5,
                patience=5,
                min_lr=1e-6
            )
        ],
        verbose=1
    )

    # Evaluate
    print("\nEvaluating model...")
    test_loss, test_acc = model.evaluate(X_test, y_test, verbose=0)
    print(f"Test accuracy: {test_acc*100:.2f}%")

    # Save Keras model
    model.save('fall_model_keras.h5')
    print("\nKeras model saved: fall_model_keras.h5")

    # Convert to TFLite
    print("\nConverting to TensorFlow Lite...")
    convert_to_tflite(model, 'fall_model.tflite')

    print("\n" + "=" * 60)
    print("Training complete!")
    print("=" * 60)
    print("\nNext steps:")
    print("1. Copy fall_model.tflite to: app/src/main/assets/model.tflite")
    print("2. Verify labels.txt matches the classes above")
    print("3. Rebuild and install the app")
    print("4. Test fall detection with your phone")

if __name__ == '__main__':
    main()

