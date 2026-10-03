# 🌾 KrishiAI

<p align="center">
  <img src="https://img.shields.io/badge/Android-Kotlin-3DDC84?style=for-the-badge&logo=android&logoColor=white">
  <img src="https://img.shields.io/badge/Jetpack%20Compose-UI-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white">
  <img src="https://img.shields.io/badge/Firebase-Backend-FFCA28?style=for-the-badge&logo=firebase&logoColor=black">
  <img src="https://img.shields.io/badge/AI%2FML-Powered-8E44AD?style=for-the-badge">
</p>

<p align="center">
  <b>AI-Powered Farmer Marketplace & Decision Support Application</b><br>
  Helping farmers make smarter decisions using AI, market intelligence and digital tools.
</p>

---

## 📌 Overview

**KrishiAI** is an AI-powered Android application designed to support farmers throughout important agricultural and marketplace decisions.

The application combines **Artificial Intelligence, Machine Learning, market-price intelligence, offline-first data handling and a farmer-friendly mobile interface** into a single platform.

### 🎯 Main Goals

- Help farmers identify crop diseases using AI.
- Predict possible crop/market prices.
- Estimate expected profit before selling.
- Assist farmers during price negotiations.
- Provide market-price information.
- Support Kannada/local-language interaction.
- Continue providing important functionality when internet connectivity is limited.

---

# 🚀 Key Features

## 🌱 1. AI Crop Disease Detection

Farmers can capture or upload an image of a crop/leaf.

The AI model analyzes the image and provides a disease prediction.

**Flow:**

```text
Camera / Gallery
      ↓
Image Preprocessing
      ↓
AI / ML Model
      ↓
Disease Prediction
      ↓
Result & Guidance
```

---

## 📈 2. AI Price Prediction

KrishiAI can provide an estimated market price using available market and historical information.

Possible factors include:

- Crop
- Market
- Historical prices
- Market trends
- Location
- Available market data

> The prediction is intended as decision-support information and should not be treated as a guaranteed selling price.

---

## 💰 3. AI Profit Estimator

The profit estimator helps farmers understand the possible financial outcome of a crop.

### Basic calculation

```text
Expected Revenue = Expected Selling Price × Quantity

Estimated Profit = Expected Revenue - Total Cost
```

Possible cost components:

- Seeds
- Fertilizers
- Pesticides
- Labour
- Transportation
- Other expenses

---

## 🤝 4. AI Negotiation Assistant

The negotiation assistant helps farmers prepare for marketplace negotiations.

It can provide:

- Suggested starting price
- Possible negotiation range
- Counter-offer suggestions
- Basic negotiation guidance
- Context-based responses

---

## 📊 5. Market Price Intelligence

KrishiAI is designed to provide useful market-price information to farmers.

The system can be extended to collect and process:

- APMC market prices
- Crop-wise prices
- Market-wise prices
- Historical market data
- Price trends

---

## 🗣️ 6. Kannada / Local Language Support

The application is designed with regional users in mind.

The interface and AI assistance can be extended to support **Kannada and other local languages**, making the system easier to use for farmers.

---

## 📡 7. Offline-First Support

Internet connectivity can be unreliable in rural areas.

KrishiAI therefore follows an **offline-first approach** where appropriate data can be cached locally.

```text
Internet Available
      ↓
Fetch / Sync Data
      ↓
Local Database
      ↓
Application

Internet Unavailable
      ↓
Read Cached Data
      ↓
Application Continues
```

---

# 🧠 AI Modules

KrishiAI is structured around multiple AI interfaces/modules:

```text
IAIPricePredictor
IAIDiseaseDetector
IAIProfitEstimator
IAINegotiationAssistant
```

This modular approach makes it easier to replace, improve or extend individual AI components without redesigning the entire application.

---

# 🏗️ Architecture

KrishiAI follows a modular Android architecture based around **Clean Architecture, MVVM and dependency injection**.

```text
                    ┌─────────────────────┐
                    │      UI Layer       │
                    │ Jetpack Compose     │
                    └──────────┬──────────┘
                               ↓
                    ┌─────────────────────┐
                    │    ViewModel Layer  │
                    │      MVVM           │
                    └──────────┬──────────┘
                               ↓
                    ┌─────────────────────┐
                    │    Domain Layer     │
                    │ Use Cases / Models  │
                    └──────────┬──────────┘
                               ↓
                    ┌─────────────────────┐
                    │     Data Layer      │
                    │ Repository / APIs   │
                    └──────────┬──────────┘
                               ↓
             ┌─────────────────┴─────────────────┐
             ↓                                   ↓
      ┌───────────────┐                  ┌───────────────┐
      │    Firebase   │                  │  Room / Local │
      │    Backend    │                  │    Storage    │
      └───────────────┘                  └───────────────┘
```

---

# 🛠️ Technology Stack

## Android

| Technology | Purpose |
|---|---|
| Kotlin | Primary programming language |
| Jetpack Compose | Modern Android UI |
| Material 3 | UI components and design |
| Android SDK | Application platform |
| ViewModel | UI state management |
| Hilt | Dependency injection |
| Room | Local/offline database |

## Backend & Cloud

| Technology | Purpose |
|---|---|
| Firebase Authentication | User authentication |
| Firebase | Cloud/backend services |
| Cloud services | Application data/services |

## AI / ML

| Technology | Purpose |
|---|---|
| Python | AI/ML development |
| TensorFlow / Keras | Model development |
| Computer Vision | Crop image analysis |
| Machine Learning | Prediction and estimation |

---

# 📂 Project Structure

A typical KrishiAI Android structure is organized approximately as follows:

```text
KrishiAI/
│
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       │   └── ...
│   │       │
│   │       ├── res/
│   │       │   ├── drawable/
│   │       │   ├── mipmap/
│   │       │   └── values/
│   │       │
│   │       └── AndroidManifest.xml
│   │
│   ├── build.gradle / build.gradle.kts
│   └── ...
│
├── gradle/
├── build.gradle / build.gradle.kts
├── settings.gradle / settings.gradle.kts
├── gradle.properties
└── README.md
```

> The exact folder/package structure may differ depending on the current project implementation.

---

# 💻 How to Run KrishiAI

## 1️⃣ Prerequisites

Install the following before running the project:

- **Android Studio**
- **JDK** compatible with the project's Gradle/Android configuration
- **Android SDK**
- **Git**
- An Android emulator or physical Android device

For AI/backend components, install the Python environment and packages required by the specific model/service implementation.

> Use the versions specified by the actual project files when available. Do not randomly upgrade Gradle, Kotlin, Android Gradle Plugin or JDK versions because compatibility can break the build.

---

# 📥 2️⃣ Clone the Repository

Open a terminal:

```bash
git clone <YOUR_REPOSITORY_URL>
```

Move into the project:

```bash
cd KrishiAI
```

---

# 🧑‍💻 3️⃣ Open the Project

### Using Android Studio

1. Open **Android Studio**.
2. Select **Open**.
3. Select the `KrishiAI` project folder.
4. Wait for Gradle synchronization to complete.
5. Allow Android Studio to download required dependencies.
6. Check that there are no Gradle sync errors.

---

# 🔥 4️⃣ Configure Firebase

If the project uses Firebase:

1. Create/open the Firebase project.
2. Add the Android application.
3. Use the exact application/package ID from the project's Gradle configuration.
4. Download the Firebase configuration file.
5. Place it in the appropriate Android app module location.
6. Enable the Firebase services required by the project.
7. Sync the project.

### Authentication

If Firebase Authentication is used:

```text
Firebase Console
      ↓
Authentication
      ↓
Sign-in method
      ↓
Enable required provider
```

For example, if the project uses Email/Password authentication, enable that provider.

> Never upload private API keys, service-account files, passwords or other secrets to GitHub.

---

# 📱 5️⃣ Run on Android Emulator

In Android Studio:

```text
Device Manager
      ↓
Create / Select Virtual Device
      ↓
Start Emulator
      ↓
Select KrishiAI
      ↓
Run ▶
```

The application should build and install on the emulator.

---

# 📲 6️⃣ Run on a Physical Android Phone

### Enable Developer Options

On your Android phone:

```text
Settings
→ About Phone
→ Build Number
→ Tap several times
```

Then enable:

```text
Developer Options
→ USB Debugging
```

Connect the phone using USB.

Verify that Android Studio detects the device, select it from the device list, and press:

```text
Run ▶
```

---

# ⚙️ 7️⃣ Build Using Terminal

From the project root:

### Windows

```bash
gradlew.bat assembleDebug
```

### macOS / Linux

```bash
./gradlew assembleDebug
```

To install the debug build on a connected device:

```bash
gradlew.bat installDebug
```

or:

```bash
./gradlew installDebug
```

> The exact Gradle tasks depend on the project's modules and build configuration.

---

# 🧪 Testing the Features

After launching the application, test the major modules individually.

### Disease Detection

```text
Open Disease Detection
        ↓
Capture / Select Image
        ↓
Run Detection
        ↓
Check Prediction
```

### Price Prediction

```text
Select Crop
      ↓
Enter / Select Market Information
      ↓
Request Prediction
      ↓
View Estimated Price
```

### Profit Estimation

```text
Enter Crop
      ↓
Enter Quantity
      ↓
Enter Expected Price
      ↓
Enter Costs
      ↓
Calculate
      ↓
View Estimated Profit
```

### Negotiation Assistant

```text
Select Crop / Market Context
        ↓
Enter Offered Price
        ↓
Request Assistance
        ↓
View Suggested Response
```

---

# 🗄️ Offline Data Flow

KrishiAI can use local storage to improve reliability.

```text
                ┌───────────────┐
                │     User      │
                └───────┬───────┘
                        ↓
                ┌───────────────┐
                │   ViewModel   │
                └───────┬───────┘
                        ↓
                ┌───────────────┐
                │  Repository   │
                └───────┬───────┘
                        ↓
             ┌──────────┴──────────┐
             ↓                     ↓
      ┌─────────────┐       ┌─────────────┐
      │    Remote   │       │    Local    │
      │   Firebase  │       │    Room     │
      └─────────────┘       └─────────────┘
```

---

# 🔐 Security

When publishing KrishiAI:

- Do not commit passwords.
- Do not commit private API keys.
- Do not commit Firebase service-account credentials.
- Use environment/local configuration for secrets.
- Apply appropriate Firebase security rules.
- Validate user input.
- Restrict database access using authentication and authorization.

Example `.gitignore` entries may include sensitive/local files:

```gitignore
.env
*.key
*.pem
local.properties
```

Only add files to `.gitignore` when they should actually remain local in your project.

---

# 🐛 Troubleshooting

## Gradle Sync Failed

Try:

```text
File
→ Sync Project with Gradle Files
```

If the problem continues, check:

- JDK version
- Android Gradle Plugin version
- Gradle wrapper version
- Kotlin version
- Internet/dependency access

Avoid changing multiple versions randomly.

---

## Firebase Error

Check:

- Correct Firebase project
- Correct Android package/application ID
- Correct Firebase configuration file
- Required Firebase services enabled
- Internet connection
- Firebase security rules

---

## App Does Not Install

Check:

- Emulator is running.
- Physical device is connected.
- USB debugging is enabled.
- Android SDK is installed.
- Device has enough storage.
- Build completed successfully.

---

## AI Model Does Not Load

Check:

- Model file exists.
- Correct model path is used.
- Required Python/ML dependencies are installed.
- Input image dimensions match the model.
- Model format is supported by the inference environment.

---

# 📦 Generate APK

For a debug APK:

```bash
gradlew.bat assembleDebug
```

The APK is generally generated inside the project's build output directory, for example:

```text
app/build/outputs/apk/debug/
```

For a release APK:

```bash
gradlew.bat assembleRelease
```

A release build should be properly signed before distribution.

---

# 🔄 Development Workflow

```text
Idea
 ↓
Requirement Analysis
 ↓
UI / UX Design
 ↓
Android Development
 ↓
AI / ML Integration
 ↓
Firebase / Database Integration
 ↓
Testing
 ↓
Bug Fixing
 ↓
APK Build
 ↓
Deployment
```

---

# 🗺️ Roadmap

- [x] Android application foundation
- [x] Jetpack Compose UI foundation
- [x] AI module interfaces
- [x] Data-layer foundation
- [x] Offline-first architecture foundation
- [ ] Improve disease detection model
- [ ] Improve price prediction
- [ ] Improve profit estimation
- [ ] Enhance negotiation assistant
- [ ] Expand market-price intelligence
- [ ] Improve Kannada/local-language support
- [ ] Field testing with users
- [ ] Production deployment

---

# 🌾 Future Scope

KrishiAI can be expanded with:

- Weather-based recommendations
- Soil analysis
- Crop recommendation
- Fertilizer recommendation
- Voice-based farmer assistant
- Multilingual AI chatbot
- Government scheme information
- Nearby market discovery
- Farm expense tracking
- Advanced market forecasting
- IoT-based farm monitoring
- Personalized farming recommendations

---

# 📸 Application Screenshots

Add screenshots of the application here:

```text
docs/
└── screenshots/
    ├── home.png
    ├── disease_detection.png
    ├── price_prediction.png
    ├── profit_estimator.png
    └── negotiation.png
```

Example:

```markdown
![Home Screen](docs/screenshots/home.png)
```

---

# 👥 Team

### KrishiAI

**Project:** AI-Powered Farmer Marketplace & Decision Support Application

**Developer:** Veeresh M V  
**Department:** CSE – AI & ML  
**College:** Gopalan College of Engineering and Management, Bengaluru

---

# 📄 License

Add your preferred license here.

Example:

```text
MIT License
```

---

# ⭐ Support

If you find the project useful:

- ⭐ Star the repository
- 🍴 Fork the project
- 🐛 Report issues
- 💡 Suggest improvements
- 🤝 Contribute to the project

---

<p align="center">
  <b>🌱 Technology for smarter farming.</b>
</p>
