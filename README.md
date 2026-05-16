# Evenizer

Evenizer is a comprehensive Android application designed for event management and discovery. It allows users to browse upcoming events, follow event organizers, manage tickets, and even create their own events and organizations.

## 🚀 Features

- **Event Discovery**: Browse a wide range of events filtered by date or category.
- **Organizer Management**: Follow your favorite event organizers and stay updated on their latest projects.
- **Ticketing System**: Keep track of your event tickets with status updates (Active/Used).
- **Create & Manage Events**: Registered users can create and manage their own events with custom banners and details.
- **Organization Creation**: Create your own organization to host events, complete with logo uploading and cropping functionality.
- **Multilingual Support**: Fully localized in 5 languages:
  - English 🇺🇸
  - Indonesian 🇮🇩
  - Spanish 🇪🇸
  - Russian 🇷🇺
  - Chinese 🇨🇳
- **User Authentication**: Secure login and registration system, including guest access.
- **Dark Mode**: Fully supports system-wide dark and light themes.

## 🛠 Tech Stack

- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) - Modern declarative UI.
- **Navigation**: [Compose Navigation](https://developer.android.com/jetpack/compose/navigation) - Type-safe navigation between screens.
- **Asynchronous Programming**: [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) & [Flow](https://kotlinlang.org/docs/flow.html).
- **Networking**: [Retrofit](https://square.github.io/retrofit/) with [OkHttp](https://square.github.io/okhttp/) & [Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization).
- **Image Loading**: [Coil (v3)](https://coil-kt.github.io/coil/) - Fast and lightweight image loading for Compose.
- **Data Persistence**: [DataStore Preferences](https://developer.android.com/topic/libraries/architecture/datastore) - Modern key-value storage for settings.
- **Architecture**: MVVM (Model-View-ViewModel) for clean separation of concerns.

## 📂 Project Structure

```text
app/src/main/java/com/dudek/evenizer/
├── data/           # Models, Repositories, and API Services
├── models/         # ViewModels for UI state management
├── pages/          # Individual screen content (HomePage, EventPage, etc.)
├── screens/        # High-level UI structures (MainScreen, SplashScreen)
├── utils/          # Helper classes (Date formatting, Image cropping)
└── ui/             # Theme and styling definitions
```

## ⚙️ Setup & Installation

1. **Clone the repository**:
   ```bash
   git clone https://github.com/yourusername/evenizer.git
   ```
2. **Open in Android Studio**:
   Import the project as a Gradle project.
3. **Build & Run**:
   Sync Gradle and run the `:app` module on an emulator or physical device (Android 8.0+ recommended).

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.
