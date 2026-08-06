# TutorDesk

TutorDesk is an Android app for private tutors to manage their tuition business — batches, students, finances, and routines — in one place. It also includes an AI-powered question paper generator for creating practice papers.

## Features

- **Authentication** — email/password sign up, login, email verification, and password recovery
- **Batch Management** — create and manage tuition batches, view batch details and rosters
- **Student Management** — add students, search and view individual student details
- **Finance Tracking** — track payments and fees per student/batch
- **Routine / Schedule** — view a weekly/daily class schedule
- **AI Question Generator** — upload material, set requirements, generate, preview, and save practice question papers
- **Profile & Settings** — manage profile, view help, about, and privacy screens

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose (Material 3)
- **Navigation:** Jetpack Navigation Compose
- **Architecture:** MVVM (ViewModel + Repository pattern)
- **Backend Services:** Firebase (via `google-services.json`)
- **Build System:** Gradle (Kotlin DSL), AGP 9.1.1, Kotlin 2.2.10
- **Min SDK:** 24 · **Target SDK:** 37

## Project Structure

```
TutorDesk/
├── app/
│   ├── src/main/java/com/digifello/tutordesk/
│   │   ├── data/
│   │   │   ├── model/        # Data classes (Batch, Student, Payment, User, etc.)
│   │   │   ├── remote/       # API clients (Question Generator API, network config)
│   │   │   └── repository/   # Repositories (Auth, Batch, Student, Payment, etc.)
│   │   ├── ui/
│   │   │   ├── Screens/      # Compose screens grouped by feature
│   │   │   └── theme/        # App theming (colors, typography)
│   │   ├── util/             # Utilities (date formatting, download notifier)
│   │   └── MainActivity.kt
│   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
└── gradle/
```

## Getting Started

### Prerequisites

- [Android Studio](https://developer.android.com/studio) (latest stable recommended)
- JDK 17+
- A Firebase project with a `google-services.json` file placed in `app/`

### Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/Mitul0000/tuition-manager.git
   cd tuition-manager
   ```
2. Open the project in Android Studio.
3. Add your own `app/google-services.json` (get this from your Firebase console — it is not committed to version control).
4. Let Gradle sync and download dependencies.
5. Run the app on an emulator or physical device (min SDK 24).

## Building

```bash
./gradlew assembleDebug
```

The debug APK will be output to `app/build/outputs/apk/debug/`.

## Contributing

1. Fork the repo and create a feature branch.
2. Make your changes with clear, focused commits.
3. Open a pull request describing what changed and why.

## License

No license has been specified yet for this project.
