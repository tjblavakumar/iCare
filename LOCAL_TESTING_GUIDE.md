# iCare — Local Testing Guide

This guide walks you through setting up the iCare Android app for local development and testing.

---

## Prerequisites

1. **Android Studio** (latest stable — Ladybug or newer)
   - Download: https://developer.android.com/studio
2. **JDK 17** (bundled with Android Studio)
3. **Node.js 18+** (for Cloud Functions)
   - Download: https://nodejs.org/
4. **Firebase CLI**
   ```bash
   npm install -g firebase-tools
   ```
5. **A Google account** (for Firebase Console)

---

## Step 1: Firebase Project Setup

### 1.1 Create a Firebase Project
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click **"Create a project"**
3. Name it `iCare` (or any name you prefer)
4. Disable Google Analytics (optional for v1)
5. Click **Create project**

### 1.2 Register the Android App
1. In the Firebase project, click **"Add app"** → **Android**
2. Enter the package name: `com.icare.app`
3. Enter app nickname: `iCare`
4. Click **Register app**
5. Download the `google-services.json` file
6. Place it in the `app/` directory of the project:
   ```
   iCare/
   └── app/
       └── google-services.json   ← Place here
   ```

### 1.3 Enable Firebase Authentication
1. In Firebase Console → **Authentication** → **Sign-in method**
2. Enable **Email/Password** provider
3. Enable **Google** provider:
   - Click **Google** → **Enable**
   - Set a public-facing name for your app (e.g., "iCare")
   - Select a support email
   - Click **Save**
4. **Copy the Web Client ID** (you'll need this later):
   - After enabling Google, expand the Google provider
   - Copy the **Web client ID** (looks like `xxx.apps.googleusercontent.com`)
5. Click **Save**

### 1.3.1 Configure Web Client ID
1. Open `app/src/main/res/values/strings.xml`
2. Find the line: `<string name="default_web_client_id">YOUR_WEB_CLIENT_ID</string>`
3. Replace `YOUR_WEB_CLIENT_ID` with the Web Client ID you copied above

### 1.4 Enable Cloud Firestore
1. In Firebase Console → **Firestore Database**
2. Click **"Create database"**
3. Choose **"Start in test mode"** (we'll deploy proper rules later)
4. Select your preferred region (e.g., `us-central1`)
5. Click **Enable**

### 1.5 Enable Cloud Messaging (FCM)
1. In Firebase Console → **Cloud Messaging**
2. It should be enabled by default
3. Note: Push notifications only work on physical devices, not emulators

### 1.6 Deploy Firestore Security Rules
```bash
cd /path/to/iCare
firebase login
firebase use --add    # Select your project
firebase deploy --only firestore:rules
firebase deploy --only firestore:indexes
```

---

## Step 2: Open the Project in Android Studio

1. Open Android Studio
2. Select **"Open an Existing Project"**
3. Navigate to the `iCare/` directory and open it
4. Wait for Gradle sync to complete (this may take a few minutes the first time)
5. If prompted, accept any SDK license agreements

### Troubleshooting Gradle Sync
- If sync fails, check that `google-services.json` is in the `app/` directory
- Ensure you have Android SDK 35 installed (SDK Manager → SDK Platforms)
- Check that JDK 17 is selected (Settings → Build → Gradle → JDK)

---

## Step 3: Run on an Emulator

### 3.1 Create an Emulator
1. In Android Studio → **Device Manager** (right sidebar)
2. Click **"Create Virtual Device"**
3. Select a phone (e.g., **Pixel 8**)
4. Select a system image with **API 35** (download if needed)
5. Click **Finish**

### 3.2 Run the App
1. Select the emulator from the device dropdown (top toolbar)
2. Click the **Run** button (green play icon) or press `Shift+F10`
3. Wait for the build to complete and the app to launch

**Note:** Push notifications do NOT work on emulators. For testing notifications, use a physical device.

---

## Step 4: Run on a Physical Device

### 4.1 Enable Developer Mode on Your Android Phone
1. Go to **Settings → About phone**
2. Tap **"Build number"** 7 times
3. Go back to **Settings → Developer options**
4. Enable **"USB debugging"**

### 4.2 Connect and Run
1. Connect your phone via USB cable
2. Accept the debugging prompt on your phone
3. Select your device from the dropdown in Android Studio
4. Click **Run**

---

## Step 5: Deploy Cloud Functions (for Push Notifications)

Cloud Functions handle sending push notifications when someone updates their status.

### 5.1 Install Dependencies
```bash
cd cloud-functions
npm install
```

### 5.2 Deploy to Firebase
```bash
firebase login    # If not already logged in
firebase deploy --only functions
```

### 5.3 Test Notifications
1. Install the app on **two physical devices**
2. Create accounts on both devices
3. Add each other as contacts (Settings → Add Contact → search by email)
4. Accept the connection request on the other device
5. On Device A, tap "Feeling Low" or "Feeling Bad"
6. Device B should receive a push notification

---

## Step 6: Test All Features

### Authentication
- [ ] Sign up with email + passcode
- [ ] Log out and log back in
- [ ] Sign up with phone number + passcode (requires recovery email)
- [ ] Sign in with Google
- [ ] Test "Forgot Passcode?" flow (sends reset email)
- [ ] For phone users, verify reset email is sent to recovery email

### Home Screen (Reflection)
- [ ] Tap each of the 3 default emojis
- [ ] Verify status updates in Firestore (Firebase Console → Firestore)
- [ ] Tap "More" to see additional emojis
- [ ] Verify timestamp updates correctly

### My Circle
- [ ] View connected contacts' statuses
- [ ] Verify sorting (bad → low → happy → inactive)
- [ ] Tap a contact to view 7-day history
- [ ] Tap Call/Text buttons

### Notifications
- [ ] Trigger a "Feeling Low" status on a connected device
- [ ] Verify push notification appears
- [ ] Verify notification appears in the Notifications page
- [ ] Tap to mark as read

### Settings
- [ ] Edit display name
- [ ] Add a contact by email/phone search
- [ ] Accept/reject connection requests
- [ ] Remove a contact
- [ ] Log out
- [ ] Delete account

### Onboarding
- [ ] Uninstall and reinstall to see the onboarding walkthrough
- [ ] Verify "Skip" works
- [ ] Verify "Next" → "Get Started" flow

---

## Step 7: Run with Firebase Emulator (Optional)

For offline development without a live Firebase project:

```bash
# Install Firebase emulators
firebase init emulators
# Select: Auth, Firestore, Functions

# Start emulators
firebase emulators:start
```

Then in the Android app, you'd need to configure it to use the local emulator. Add this to `ICareApplication.kt` for debug builds:

```kotlin
override fun onCreate() {
    super.onCreate()
    if (BuildConfig.DEBUG) {
        FirebaseAuth.getInstance().useEmulator("10.0.2.2", 9099)
        FirebaseFirestore.getInstance().useEmulator("10.0.2.2", 8080)
    }
}
```

(`10.0.2.2` is the host machine's localhost from an Android emulator)

---

## Common Issues

| Issue | Solution |
|-------|----------|
| Gradle sync fails | Ensure `google-services.json` is in `app/`, SDK 35 is installed |
| App crashes on launch | Check Logcat for errors; ensure Firebase project is set up correctly |
| Push notifications don't work | Use a physical device; check FCM token in Firestore |
| "User data not found" on login | Ensure Firestore is enabled and user document exists |
| Contact search returns empty | Ensure the other user has signed up with the same email/phone |
| 48-hour grey not showing | Wait 48 hours or manually adjust timestamps in Firestore for testing |
| Google Sign-In fails | Verify Web Client ID in `strings.xml` matches Firebase Console |
| Google Sign-In shows "Developer error" | Ensure SHA-1 fingerprint is added to Firebase (see below) |
| Password reset email not received | Check spam folder; verify email exists in Firebase Auth |

### Adding SHA-1 Fingerprint (Required for Google Sign-In)

For Google Sign-In to work, you need to add your debug SHA-1 fingerprint to Firebase:

1. Get your debug SHA-1:
   ```bash
   cd ~/.android
   keytool -list -v -keystore debug.keystore -alias androiddebugkey -storepass android -keypass android
   ```
2. Copy the SHA-1 fingerprint
3. Go to Firebase Console → Project Settings → Your Apps → Android app
4. Click **"Add fingerprint"** and paste the SHA-1
5. Download the updated `google-services.json` and replace the old one

---

## Useful Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run unit tests
./gradlew test

# Run lint checks
./gradlew lint

# Deploy Firestore rules
firebase deploy --only firestore

# Deploy Cloud Functions
firebase deploy --only functions

# View Firestore data
# Go to: https://console.firebase.google.com/ → Your Project → Firestore
```
