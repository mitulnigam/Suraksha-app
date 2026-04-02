# Suraksha - AI-Powered Personal Safety App

**Suraksha** (meaning "Safety" or "Protection") is a powerful, discreet, and AI-driven personal safety application for Android. Designed to provide immediate assistance during emergencies, it leverages advanced sensor fusion, machine learning, and automated alerts to ensure users are never alone in a crisis.

---

## 🌟 Key Features

### 🚨 Instant SOS Triggering
Multiple ways to alert your trusted contacts without needing to unlock your phone:
- **Triple Shake Gesture**: Detects 3 rapid back-and-forth shakes using the accelerometer.
- **Voice Hotword Detection**: Triggers SOS by saying the wake word "**bumblebee**" (Powered by Picovoice Porcupine).
- **Manual SOS Button**: A large, easy-to-access button on the home screen with a 10-second cancellation countdown.

### 🤖 AI Fall Detection
- High-accuracy fall detection using **TensorFlow Lite**.
- Analyzes accelerometer patterns to distinguish between actual falls and normal phone movements.
- Automatically triggers SOS if a significant impact is detected following a sudden drop.

### 🕵️ App Disguise (Stealth Mode)
- **Calculator Alias**: To protect the user's privacy in sensitive situations, the app can be disguised as a fully functional calculator.
- Change the app icon and name in settings to hide its true purpose.

### 🛡️ Smart Alert System
- **GPS Integration**: Automatically includes your precise Google Maps location in every emergency SMS.
- **Trusted Contacts**: Manage a list of emergency contacts who will receive your alerts.
- **SOS SMS**: Sends a direct help message:  
  *`HELP! This is an emergency alert from Suraksha. My current location is: http://maps.google.com?q=LAT,LON`*

---

## 🛠️ Technology Stack

- **Platform**: Android (Android 8.0+ / API 26)
- **Language**: Kotlin & Jetpack Compose
- **Machine Learning**: TensorFlow Lite (for Fall Detection)
- **Voice Recognition**: Picovoice Porcupine (100% on-device wake word)
- **Maps**: Google Maps SDK for Android
- **Architecture**: MVVM with Foreground Services for 24/7 background protection.

---

## 🚀 Quick Setup Guide

### 1. Configure API Keys
Create a `local.properties` file in the root directory and add:
```properties
# Get from Google Cloud Console
MAPS_API_KEY=YOUR_GOOGLE_MAPS_API_KEY

# Get from https://console.picovoice.ai
PICOVOICE_ACCESS_KEY=YOUR_PICOVOICE_ACCESS_KEY
```

### 2. Permissions Required
Grant these permissions for full functionality:
- **SMS**: To send alerts to contacts.
- **Location**: To share your coordinates during SOS.
- **Microphone**: For voice command detection.
- **Foreground Service**: To keep monitoring active in the background.

---

## 📂 Project Structure

- `app/src/main/java/com/suraksha/app/services/`: Core background services for monitoring sensors.
- `app/src/main/java/com/suraksha/app/sensors/`: Gesture and Fall detection logic.
- `app/src/main/java/com/suraksha/app/ui/`: Modern Compose-based UI components.
- `train_fall_model.py`: Script for training the custom fall detection neural network.

---

## 🤝 Contributing
Contributions are welcome! Please read the `SETUP_GUIDE.md` for detailed technical information on how to build and test the project.

---

## 📄 License
This project is for personal safety and educational purposes. Check individual components for their respective licenses (e.g., Picovoice, TensorFlow).
