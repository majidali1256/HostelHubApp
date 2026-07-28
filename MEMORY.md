# Hostel Hub (Android App) - Project Memory & Context

This file serves as the memory and context guide for AI assistants and developers working on the `Hostel-Hub-App` Android codebase. Read this before making architectural or API integration changes to avoid mistakes.

## 🏗️ Architecture & Tech Stack
- **Language:** Kotlin
- **UI Toolkit:** Jetpack Compose (Modern Declarative UI)
- **Architecture:** MVVM (Model-View-ViewModel) pattern
- **Networking:** Retrofit + Gson (for API calls to the Node.js backend)
- **Local Storage:** Android DataStore / SharedPreferences for Auth Tokens

## 🔌 API & Networking Rules (CRITICAL)

### 1. Base URL & Execution
- Located in `build.gradle.kts` via `buildConfigField("String", "BASE_URL", ...)`.
- **Local Testing:** Uses `http://10.0.2.2:5003/api/` (pointing to the Node.js backend on port 5003).
- **Rule:** Always run both the **Frontend** (Android app on emulator) and **Backend** (`hostel-hub/server` on port 5003) together when testing or running the app.
- **Production:** Point to the deployed backend URL (e.g., `https://hostel-hub-xyz.onrender.com/api/`).

### 2. Data Model Parsing & Flexible ID Deserialization
- **Messages Endpoint (`senderId` / `conversationId`):** The backend returns `{ "messages": [...], "hasMore": boolean }`. Populated object IDs are dynamically converted to String IDs via `FlexibleStringDeserializer` in `Chat.kt`.
- **Bookings Endpoint (`hostelId` / `customerId`):** Handled flexibly in `Booking.kt` via `FlexibleBookingHostelInfoDeserializer` and `FlexibleBookingUserInfoDeserializer` to support both string ObjectIds and populated objects without throwing Gson `JsonSyntaxException`.
- **User Models (`stayHistory`):** `stayHistory` allows nullable elements (`List<String?>?`) to accommodate `null` entries in MongoDB user history arrays.

### 3. Authentication & Social Login
- **Rule:** Do NOT use `SocialAccountChooserDialog` for Google/Facebook logins.
- **Current Setup:** In `LoginScreen.kt` and `SignupScreen.kt`, the "Continue with Google" and "Continue with Facebook" buttons directly trigger `Intent.ACTION_VIEW` to launch the browser OAuth flow (`/api/auth/google?origin=hostelhub://oauth`).
- **Deep Linking:** The app is configured to capture the `hostelhub://` deep link when the OAuth flow completes, capturing the JWT token.

### 4. Demo Mode
- **Toggle:** `BuildConfig.DEMO_MODE` can be used to isolate the app from the backend.
- **Repository Impact:** When active, `ChatRepository.kt` fetches mock data instead of making live Retrofit calls. Ensure this is `false` when testing real server connectivity.

### 5. JWT Authentication & Chat Troubleshooting
- **Expired Token (`401 Unauthorized`):** If API calls (`/api/messages`, `/api/conversations`) or Socket connection return `401 Unauthorized`, the saved JWT token in DataStore is expired. Re-log in via the Login Screen or OAuth to generate a fresh token.
- **Error UI:** `ChatViewModel` now exposed room UI errors to display an in-app error banner when message transmission fails.
