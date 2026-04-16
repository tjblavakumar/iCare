# iCare

**Share how you feel with people who care. One tap. No words needed.**

iCare is a simple Android app that lets your loved ones know how you're feeling with a single tap. Your circle sees your mood in real time — and you see theirs.

## How It Works

1. **Tap your mood** — Choose from Happy, Feeling Low, or Feeling Bad (plus extras)
2. **Build your circle** — Add family and friends from your contacts
3. **Stay connected** — Get notified when someone feels low; grey means they haven't checked in for 48 hours

## Features

- Three default mood emojis + predefined extras
- Mutual connections with accept/reject flow
- Real-time status updates via Firestore
- Push notifications for negative moods (Cloud Functions + FCM)
- 7-day mood history per contact
- 48-hour inactivity indicator (grey)
- Quick call/text actions from contact list
- Onboarding walkthrough
- Account deletion (Play Store compliant)
- **Google Sign-In** for quick, passwordless authentication
- **Password reset** via Firebase (email-based)

## Tech Stack

| Layer | Technology |
|-------|------------|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Clean Architecture |
| DI | Hilt |
| Auth | Firebase Authentication (Email/Phone + Google Sign-In) |
| Database | Cloud Firestore |
| Push | Firebase Cloud Messaging + Cloud Functions |
| Min SDK | 26 (Android 8.0) |

## Project Structure

```
iCare/
├── app/
│   └── src/main/java/com/icare/app/
│       ├── data/
│       │   ├── model/          # Data classes (User, Connection, etc.)
│       │   └── repository/     # Firebase repositories
│       ├── di/                 # Hilt dependency injection
│       ├── service/            # FCM messaging service
│       └── ui/
│           ├── components/     # Reusable UI components
│           ├── navigation/     # Navigation graph
│           ├── screens/        # All app screens
│           └── theme/          # Colors, typography, theme
├── cloud-functions/            # Firebase Cloud Functions (notifications)
├── firestore.rules             # Firestore security rules
├── firestore.indexes.json      # Firestore indexes
├── LOCAL_TESTING_GUIDE.md      # Setup & testing instructions
└── PLAY_STORE_GUIDE.md         # Publishing guide
```

## Quick Start

1. Create a Firebase project at [console.firebase.google.com](https://console.firebase.google.com/)
2. Register an Android app with package name `com.icare.app`
3. Download `google-services.json` → place in `app/`
4. Enable Email/Password and Google Sign-In auth providers
5. Enable Cloud Firestore
6. Copy your Web Client ID from Firebase Console → Authentication → Sign-in method → Google
7. Update `app/src/main/res/values/strings.xml` with your Web Client ID
8. Open the project in Android Studio
9. Run on emulator or device

See [LOCAL_TESTING_GUIDE.md](LOCAL_TESTING_GUIDE.md) for detailed instructions.

## Publishing

See [PLAY_STORE_GUIDE.md](PLAY_STORE_GUIDE.md) for step-by-step Play Store publishing instructions.

## License

All rights reserved.
