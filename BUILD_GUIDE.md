# KrishiAI — Build & Setup Guide

## Quick Start (Android Studio — Recommended)

1. **Open Android Studio** (Koala 2024.1.1+ / Ladybug 2024.2.1+)
2. Open `File → Open` and select `e:\Codes\Templates\Farmer`
3. Android Studio will auto-detect the JDK from its bundled JBR (Java 21) — no manual configuration needed
4. Wait for Gradle sync to complete (~2-5 minutes on first run)
5. Click **▶ Run** or press `Shift+F10` to build and install on a connected device

---

## Command-Line Build (Resolved Configuration)

### Environment Setup

| Setting | Value |
|---|---|
| **Gradle** | 8.13 (cached at `%USERPROFILE%\.gradle\wrapper\dists\gradle-8.13-bin`) |
| **JDK** | OpenJDK 21 Temurin (extracted to `jdk21/jdk-21.0.5+11`) |
| **AGP** | 8.6.0 |
| **Kotlin** | 2.1.0 + Compose Compiler Plugin |
| **Target SDK** | 35 (Android 15) |
| **Min SDK** | 26 (Android 8.0) |

### Build Commands

```powershell
# Set JDK 21 (required – system JDK 25 is incompatible with Kotlin DSL)
$env:JAVA_HOME = "e:\Codes\Templates\Farmer\jdk21\jdk-21.0.5+11"

# Build Debug APK
.\gradlew.bat assembleDebug

# Build Release APK (unsigned)
.\gradlew.bat assembleRelease

# Install directly on connected device
.\gradlew.bat installDebug
```

### APK Output Locations

| Build Type | Path |
|---|---|
| **Debug APK** | `app\build\outputs\apk\debug\app-debug.apk` |
| **Release APK** | `app\build\outputs\apk\release\app-release-unsigned.apk` |

---

## Firebase Setup (Required for full functionality)

The project currently uses **placeholder** `google-services.json` values for compilation.  
To connect to a real Firebase project:

1. Go to [Firebase Console](https://console.firebase.google.com) → Create project → **KrishiAI**
2. Add Android app with package name: `com.krishiai.app`
3. Download `google-services.json` and replace `app/google-services.json`
4. Enable the following in Firebase Console:
   - **Authentication** → Email/Password sign-in method
   - **Firestore Database** → Start in test mode
   - **Storage** → Start in test mode

---

## Weather API Setup (Optional)

The app uses a **simulated weather fallback** when no API key is configured.  
To enable real weather data:

1. Sign up at [OpenWeatherMap](https://openweathermap.org/api) (free tier available)
2. Copy your API key
3. Open `app/src/main/java/com/krishiai/app/utils/Constants.kt`
4. Set: `const val WEATHER_API_KEY = "ADD_API_KEY_HERE"`

---

## Generating a Signed Release APK

### Step 1: Create a Keystore

```bash
keytool -genkeypair -v -keystore krishiai-release.jks -alias krishiai -keyalg RSA -keysize 2048 -validity 10000
```

### Step 2: Configure Signing in `app/build.gradle.kts`

```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("krishiai-release.jks")
            storePassword = "your_store_password"
            keyAlias = "krishiai"
            keyPassword = "your_key_password"
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
        }
    }
}
```

### Step 3: Build Signed Release APK

```powershell
$env:JAVA_HOME = "e:\Codes\Templates\Farmer\jdk21\jdk-21.0.5+11"
.\gradlew.bat assembleRelease
```

---

## Known Java 25 Compatibility Issue

> **If you encounter `IllegalArgumentException: 25` during Gradle build:**

This is caused by Gradle's embedded Kotlin compiler (used for `.kts` script compilation) being unable to parse single-integer Java version numbers like "25".

**Solution**: The project is pre-configured to use JDK 21 via `gradle.properties`:
```properties
org.gradle.java.home=e:/Codes/Templates/Farmer/jdk21/jdk-21.0.5+11
```

Always run builds with:
```powershell
$env:JAVA_HOME = "e:\Codes\Templates\Farmer\jdk21\jdk-21.0.5+11"
.\gradlew.bat assembleDebug
```

Or open in **Android Studio** which uses its own bundled JBR 21 automatically.
