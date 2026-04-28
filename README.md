# Suraksha — AI-Powered Personal Safety App

> **"Protection at your fingertips. Safety that never sleeps."**

**Suraksha** (meaning "Safety" or "Protection") is a powerful, discreet, and AI-driven personal safety application for Android. Designed to provide immediate assistance during emergencies, it leverages advanced sensor fusion, machine learning, and automated alerts to ensure users are never alone in a crisis.

---

## 🌟 Key Features

### 🚨 Instant SOS Triggering
Multiple ways to alert your trusted contacts without needing to unlock your phone:
- **Triple Shake Gesture** — Detects 3 rapid back-and-forth shakes via the accelerometer.
- **Voice Hotword Detection** — Triggers SOS by saying the wake word **"bumblebee"** (Powered by Picovoice Porcupine — 100% on-device).
- **Manual SOS Button** — A large pulsing button on the home screen with a 10-second cancellation countdown.

### 🤖 AI Fall Detection
- High-accuracy fall detection using **TensorFlow Lite**.
- Analyzes accelerometer patterns to distinguish actual falls from normal phone movements.
- Automatically triggers SOS if a significant impact is detected following a sudden drop.

### 🗺️ Safety Navigator (Safest Route)
- Enter start and destination — the app fetches multiple route alternatives via **Google Directions API**.
- Each route is scored using a real-world **crime/accident risk dataset** (`ai_training_dataset.csv`).
- **Safest Mode** — picks the route with the lowest average risk score.
- **Shortest Mode** — picks the fastest route regardless of risk.
- Displays **high-risk zones as red circles** on the map.
- **Live Navigation Mode** — tracks your location in real time, shows a live risk bar, and auto-recalculates if you go off-path.
- Route mode switcher uses a premium **segmented card selector** (🛡️ Safest / ⚡ Shortest).

### 💬 First Aid AI Chat Assistant
- Instant, structured first aid guidance for: Bleeding, Burns, Fractures, Choking, Fainting, Accidents.
- Quick-tap chips for common emergencies.
- Always reminds users to call **102/108** for life-threatening situations.

### 🔔 Community Alert Board
- Users can post nearby threat alerts: **Crime, Accident, Suspicious Activity**.
- Each alert automatically attaches your **live GPS coordinates** from the device.
- Alerts expire after **4 hours** (hyperlocal and time-bound).
- Posted to **Firebase Firestore** and visible to all users in real time.
- Back navigation button to return to Settings or Home.

### 🕵️ App Disguise (Stealth Mode)
- The app can be disguised as a fully functional **Calculator** on the home screen.
- Change the app icon and name in Settings to hide its true purpose in sensitive situations.
- PIN-protected — only the user can enable/disable disguise mode.

### 🔐 PIN-Protected Security
- Set a 4-digit PIN for an additional layer of access control.
- Required to activate or deactivate Stealth Mode.

### 🛡️ Smart Alert System
- **GPS Integration** — Automatically includes your precise Google Maps location in every emergency SMS.
- **Trusted Contacts** — Manage a list of emergency contacts who receive your alerts.
- **SOS SMS format:**
  > `HELP! This is an emergency alert from Suraksha. My current location is: http://maps.google.com?q=LAT,LON`

### ⚙️ Granular Settings
- Toggle each trigger independently: Shake Detection, Hotword Detection, AI Fall Detection.
- Light / Dark mode switch.
- Foreground service keeps all monitors active even when the app is closed.

---

## 🛠️ Technology Stack

| Category | Technology | Purpose |
|---|---|---|
| Platform | Android (API 26+) | Target OS |
| Language | Kotlin | Core development |
| UI Framework | Jetpack Compose | Declarative UI |
| Architecture | MVVM + Foreground Services | Clean arch, 24/7 monitoring |
| Machine Learning | TensorFlow Lite | On-device AI fall detection |
| Voice AI | Picovoice Porcupine | 100% on-device wake-word |
| Maps & Navigation | Google Maps SDK, Directions API, Places API | Routing & live navigation |
| Database | Firebase Firestore | Community alerts |
| Authentication | Firebase Auth | User login/signup |
| Risk Data | Custom CSV Dataset (~1.4 MB) | Crime/accident-based route scoring |
| Networking | OkHttp | API calls |
| Location | Android FusedLocationProvider | Real-time GPS |
| Sensors | Android Accelerometer | Shake + fall detection |
| SMS | Android SMS Manager | Emergency alert delivery |

---

## 🚀 Quick Setup Guide

### 1. Configure API Keys
Create a `local.properties` file in the root directory and add:
```properties
# Google Cloud Console
MAPS_API_KEY=YOUR_GOOGLE_MAPS_API_KEY

# https://console.picovoice.ai
PICOVOICE_ACCESS_KEY=YOUR_PICOVOICE_ACCESS_KEY
```

### 2. Firebase Setup
1. Create a Firebase project at [console.firebase.google.com](https://console.firebase.google.com)
2. Add an Android app with package name `com.suraksha.app`
3. Download `google-services.json` and place it in `app/`
4. Enable **Firestore Database** and **Authentication (Email/Password)**
5. Set Firestore security rules to allow authenticated reads/writes on `community_alerts`

### 3. Permissions Required
Grant these permissions for full functionality:
- **SMS** — To send alerts to contacts
- **Location** — To share your coordinates during SOS
- **Microphone** — For voice command detection
- **Foreground Service** — To keep monitoring active in the background

---

## 📂 Project Structure

```
app/src/main/java/com/suraksha/app/
├── services/          # SurakshaService, FallDetectorService (Foreground Services)
├── sensors/           # ShakeDetector, SensorLoggerActivity
├── screens/           # All Compose screens + ViewModels
│   ├── HomeScreen.kt
│   ├── RouteScreen.kt         # Safety Navigator with segmented mode selector
│   ├── RouteViewModel.kt      # Risk scoring engine
│   ├── FirstAidScreen.kt      # First Aid chat assistant
│   ├── CommunityAlertsScreen.kt  # Community alerts with GPS + back nav
│   ├── CommunityAlertViewModel.kt # Independent location fetching
│   ├── MapScreen.kt
│   ├── SettingsScreen.kt
│   └── ProfileScreen.kt
├── data/              # Data models (CommunityAlert, etc.)
├── utils/             # AlertManager, LocationManager, PinManager
├── sos/               # SosReceiver
└── ui/theme/          # Color palette, Typography, ThemeStore
```

---

## 🤝 Contributing
Contributions are welcome! Please read the `SETUP_GUIDE.md` for detailed technical information on how to build and test the project.

---

## 📄 License
This project is developed for the **Google Solution Challenge**. Check individual components for their respective licenses (Picovoice, TensorFlow, Firebase).
