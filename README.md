# Hostel Hub Android App

Native Android application for Hostel Hub - built with Kotlin and Jetpack Compose.

## 🚀 Features

- **Authentication**: Login, Signup, Forgot Password with JWT tokens
- **Hostel Browsing**: Search, filter, view hostel details with image carousel
- **Booking System**: Create bookings with date picker, view booking history
- **User Profile**: View profile, trust score, logout
- **Chat**: Real-time messaging (Socket.IO ready)
- **Dark Mode**: Full dark theme support

## 🛠️ Tech Stack

| Component | Technology |
|-----------|------------|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Clean Architecture |
| Networking | Retrofit + OkHttp |
| DI | Hilt |
| Navigation | Compose Navigation |
| Image Loading | Coil |
| Token Storage | DataStore |
| Real-time | Socket.IO Client |

## 📱 Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK 34

### Setup

1. **Open in Android Studio**
   ```bash
   cd Hostel-Hub-App
   # Open this folder in Android Studio
   ```

2. **Configure Backend URL**
   
   Edit `app/build.gradle.kts` and update the BASE_URL:
   ```kotlin
   buildConfigField("String", "BASE_URL", "\"http://YOUR_SERVER_IP:5001/api/\"")
   ```
   
   For local development with emulator, use:
   - `http://10.0.2.2:5001/api/` (Android Emulator → localhost)
   - `http://YOUR_LOCAL_IP:5001/api/` (Physical device)

3. **Build and Run**
   ```bash
   ./gradlew assembleDebug
   # Or just click Run in Android Studio
   ```

## 🔗 Backend Connection

This app connects to the same backend as the Hostel Hub web app. Make sure:

1. Your backend server is running (`cd server && npm start`)
2. The server is accessible from your Android device/emulator
3. `android:usesCleartextTraffic="true"` is set in AndroidManifest.xml (already configured for HTTP)

## 📁 Project Structure

```
app/src/main/java/com/hostelhub/
├── data/
│   ├── api/           # Retrofit API interfaces
│   ├── local/         # TokenManager (DataStore)
│   ├── model/         # Data classes (User, Hostel, Booking, etc.)
│   └── repository/    # Repository layer
├── di/                # Hilt dependency injection modules
├── ui/
│   ├── auth/          # Login, Signup, ForgotPassword screens
│   ├── home/          # Home screen with hostel list
│   ├── hostel/        # Hostel detail screen
│   ├── booking/       # Booking form and history
│   ├── profile/       # User profile
│   ├── chat/          # Chat screens
│   ├── navigation/    # Navigation routes and graph
│   └── theme/         # Colors, Typography, Theme
├── HostelHubApplication.kt
└── MainActivity.kt
```

## 🎨 Theming

The app uses Material 3 with custom Hostel Hub branding:
- **Primary**: `#FF6B35` (Orange)
- **Secondary**: `#4ECDC4` (Teal)
- **Accent**: `#FFD93D` (Yellow)

Dark mode is automatically applied based on system settings.

## 🔐 Authentication Flow

1. User logs in with email/password
2. JWT tokens (access + refresh) are stored in DataStore
3. Auth interceptor automatically adds tokens to API requests
4. On 401 response, tokens are refreshed (pending implementation)

## 📝 Notes

- This is a separate repository from the web app
- Both apps share the same backend server
- Initialize as a separate Git repo before pushing:
  ```bash
  cd Hostel-Hub-App
  git init
  git add .
  git commit -m "Initial Android app"
  ```

## 📄 License

MIT License - Feel free to use for your FYP!
