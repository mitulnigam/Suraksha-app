# 🛡️ Suraksha: AI-Powered Personal Safety Tutorial

Welcome to the **Suraksha** (Safety) project! This tutorial provides a deep dive into how the app works, the problems it solves, and the advanced technology stack powering it.

---

## 🎯 The Problem: "The Golden Hour"
In many emergencies—such as a sudden fall, a medical crisis, or a personal threat—the victim often cannot:
1. Unlock their phone.
2. Find the right contact.
3. Type a message or explain their location.

**Suraksha** solves this by providing **discreet, hands-free, and automated** ways to trigger emergency alerts.

---

## 🚀 Core Features & How They Work

### 1. 🚨 Multi-Trigger SOS System
The app offers three primary ways to send for help instantly:
*   **Triple Shake**: Uses the phone's **Accelerometer**. If 3 rapid shakes are detected (even when the screen is off), the SOS process starts.
*   **Voice Hotword ("Bumblebee")**: Powered by **Picovoice Porcupine**. The app listens for the wake word "Bumblebee" locally on the device (100% private) to trigger SOS.
*   **Manual SOS**: A high-visibility button on the home screen with a 10-second "Oops" countdown to prevent accidental triggers.

### 2. 🤖 AI-Powered Fall Detection
Unlike simple impact sensors, Suraksha uses a **TensorFlow Lite Machine Learning Model** to distinguish between:
*   Dropping your phone on a table (Ignored).
*   A real human fall (Triggered).
*   **How it works**: It analyzes a 15-second window of accelerometer data (800 samples) and runs it through a neural network to predict the event with high confidence.

### 3. 🎭 App Disguise (Stealth Mode)
In sensitive situations, you might not want an attacker to know you have a safety app.
*   **The Switch**: Once SOS is triggered (or manually enabled), the app icon and name change to a **Calculator**.
*   **The Reveal**: To get back to the real "Suraksha" interface, the user must open the "Calculator" and enter a **secret 4-digit PIN**.

### 4. 📞 Smart Alerting
When SOS is triggered, the app doesn't just send a text:
*   **SMS Blast**: All emergency contacts receive your current **Google Maps coordinates**.
*   **Priority Call**: The first contact on your list receives an automatic **missed call** to grab their attention immediately (ringing their phone even if it's in their pocket).

---

## 🛠️ The Tech Stack: Behind the Scenes

| Technology | Use Case | Why it was chosen |
| :--- | :--- | :--- |
| **Kotlin & Compose** | Core App Logic & UI | Modern, concise, and allows for beautiful, reactive interfaces. |
| **TensorFlow Lite** | Fall Detection | Allows complex neural network inference to run locally without internet. |
| **Picovoice Porcupine** | Voice Activation | Best-in-class, battery-efficient, and 100% offline wake-word engine. |
| **Foreground Services** | 24/7 Monitoring | Ensures the app keeps listening for shakes/voice even when backgrounded. |
| **Crypto Library** | PIN Security | Uses AES-256 encryption to store your secret PIN via Android Keystore. |
| **Google Maps SDK** | Location Services | Provides accurate GPS coordinates and map visualization. |

---

## 📂 Understanding the Codebase

*   `app/services/`: The "Brain" of the app. Contains the `FallDetectorService` and `HotwordService` that run in the background.
*   `app/sensors/`: Contains the logic for interpreting raw data from the Accelerometer and Gyroscope.
*   `app/ui/`: All the screens built with Jetpack Compose (Home, Settings, Map, PIN Setup).
*   `train_fall_model.py`: A Python script used to train the AI model using real-world fall data.

---

## 🛠️ Setup & "Pro" Tips

1.  **Permissions**: Granting **SMS, Location, and Microphone** permissions is critical. Without them, the app is a "brain without hands."
2.  **Battery Optimization**: For 24/7 protection, ensure the app is "Not Optimized" in Android Battery settings so the system doesn't kill the background services.
3.  **API Keys**: You'll need a **Picovoice Access Key** (Free tier available) and a **Google Maps API Key** in your `local.properties`.

---

## 🤝 Contributing
Suraksha is built with modularity in mind. You can add new "Triggers" (e.g., Bluetooth panic buttons) or "Actions" (e.g., recording emergency audio) by extending the existing `AlertManager` and `BaseSensor` classes.

**Stay Safe. Stay Protected. Stay Suraksha.** 🛡️
