# iCare — Google Play Store Publishing Guide

This guide walks you through publishing iCare on the Google Play Store as a paid app ($1).

---

## Prerequisites

1. **Google Play Developer Account** ($25 one-time fee)
   - Sign up: https://play.google.com/console/signup
2. **Signed release APK/AAB** (Android App Bundle)
3. **Privacy Policy** (hosted online — required for Play Store)
4. **App screenshots** (at least 2 phone screenshots)
5. **Feature graphic** (1024x500 px)
6. **App icon** (512x512 px hi-res)

---

## Step 1: Generate a Signing Key

You need a signing key to sign your release build. This key is **permanent** — if you lose it, you cannot update the app.

```bash
keytool -genkey -v -keystore icare-release-key.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias icare
```

You'll be prompted for:
- Keystore password (choose a strong one, save it securely)
- Key password
- Name, organization, etc.

**IMPORTANT:** Store `icare-release-key.jks` and its passwords somewhere safe (e.g., a password manager). Do NOT commit it to git.

---

## Step 2: Configure Signing in Gradle

Create a file `app/keystore.properties` (do NOT commit this):

```properties
storeFile=../icare-release-key.jks
storePassword=YOUR_KEYSTORE_PASSWORD
keyAlias=icare
keyPassword=YOUR_KEY_PASSWORD
```

Update `app/build.gradle.kts` to include signing config:

```kotlin
import java.util.Properties

// Load keystore properties
val keystorePropertiesFile = rootProject.file("app/keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(keystorePropertiesFile.inputStream())
}

android {
    // ... existing config ...

    signingConfigs {
        create("release") {
            if (keystorePropertiesFile.exists()) {
                storeFile = file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

---

## Step 3: Build the Release AAB

Google Play prefers Android App Bundles (.aab) over APKs.

```bash
# From the project root
./gradlew bundleRelease
```

The output will be at:
```
app/build/outputs/bundle/release/app-release.aab
```

---

## Step 4: Create a Privacy Policy

Google Play requires a privacy policy for apps that collect user data. Create one that covers:

- **Data collected:** Email/phone, display name, emoji status history, Google account info (if using Google Sign-In)
- **How data is used:** To show status to mutually connected users
- **Data sharing:** Only with users who have mutually accepted connections
- **Data storage:** Firebase (Google Cloud) servers
- **Data deletion:** Users can delete their account at any time
- **Third-party services:** Google Sign-In (optional authentication method)
- **Contact:** Your email for privacy inquiries

You can use a free privacy policy generator:
- https://app-privacy-policy-generator.nisrulz.com/
- https://www.freeprivacypolicy.com/

Host it on:
- GitHub Pages (free)
- Google Sites (free)
- Your own website

---

## Step 5: Prepare Store Assets

### Screenshots (Required)
- At least **2 phone screenshots**
- Recommended size: **1080x1920 px** (9:16 ratio)
- Take screenshots of:
  1. Home screen (3 emoji buttons)
  2. My Circle screen (contact statuses)
  3. Notifications screen
  4. Onboarding screen

**Tip:** Use Android Studio's screenshot tool or the emulator's camera button.

### Feature Graphic (Required)
- Size: **1024 x 500 px**
- Used for the app listing header
- Design a banner showing the app name, heart icon, and tagline

### Hi-Res Icon (Required)
- Size: **512 x 512 px**
- This is your app icon on the Play Store
- Export from the vector drawable or create a high-res version

---

## Step 6: Create the App on Google Play Console

### 6.1 Dashboard
1. Go to [Google Play Console](https://play.google.com/console)
2. Click **"Create app"**
3. Fill in:
   - **App name:** iCare
   - **Default language:** English (US)
   - **App or game:** App
   - **Free or paid:** Paid
4. Accept the declarations
5. Click **Create app**

### 6.2 Set Up Your App
Navigate through the setup checklist:

#### Store Listing
- **Short description** (80 chars max):
  ```
  Share how you feel with loved ones. One tap. No words needed.
  ```
- **Full description** (4000 chars max):
  ```
  iCare is a simple, heartfelt app that helps you stay emotionally connected
  with your family and close friends.

  HOW IT WORKS:
  • Open the app and tap one emoji to share how you're feeling — happy, low,
    or bad
  • Your loved ones see your status and you see theirs
  • Get notified when someone in your circle is feeling low or bad
  • If someone hasn't checked in for 48 hours, they appear in grey — a gentle
    reminder to reach out

  WHY iCARE:
  Life gets busy. You might not call your parents every day. Your best friend
  might be going through a tough time but doesn't know who to talk to. iCare
  bridges that gap with the simplest possible interaction.

  No long messages. No pressure. Just a tap that says "I'm here" or "I need
  someone."

  FEATURES:
  • Three default mood emojis + extra options
  • Mutual connections — both users agree to share
  • Real-time status updates
  • Push notifications for "feeling low" and "feeling bad"
  • 7-day mood history for each contact
  • 48-hour inactivity indicator
  • Quick call/text actions
  • Sign in with Google for quick access
  • Easy password reset via email
  • Account deletion for full data control

  PRIVACY:
  Your status is only visible to people you've mutually connected with. No
  ads. No tracking. Your data stays between you and your circle.
  ```
- Upload screenshots, feature graphic, and icon

#### Content Rating
- Fill out the questionnaire (iCare has no violence, gambling, etc.)
- You'll likely get an **"Everyone"** rating

#### Pricing
- Set the price to **$1.00 USD**
- Google takes a 15% commission (for first $1M revenue per year)
- Set pricing for other countries (Google auto-converts, but you can customize)

#### Target Audience
- Select **"18 and over"** (simplest — avoids COPPA requirements)

#### App Category
- Category: **Social**
- Tags: Communication, Family, Wellness

#### Privacy Policy
- Enter the URL where you hosted your privacy policy

---

## Step 7: Upload the AAB and Create a Release

### 7.1 Internal Testing (Recommended First)
1. Go to **Testing** → **Internal testing**
2. Click **"Create new release"**
3. Upload your `app-release.aab`
4. Add release notes:
   ```
   v1.0.0 — Initial release
   • Share your mood with 3 default emojis + extras
   • Build your circle from phone contacts
   • Get push notifications when someone feels low
   • View 7-day mood history
   • 48-hour inactivity indicator
   ```
5. Click **Review release** → **Start rollout**
6. Add testers by email (up to 100 for internal testing)

### 7.2 Production Release
Once testing is complete:
1. Go to **Production**
2. Click **"Create new release"**
3. Upload the same AAB (or a newer one)
4. Add release notes
5. Click **Review release** → **Start rollout to Production**

---

## Step 8: App Review

- Google reviews new apps within **1–7 days** (usually 1–3)
- Common rejection reasons:
  - Missing privacy policy
  - App crashes
  - Misleading description
  - Missing content rating
- If rejected, fix the issues and resubmit

---

## Step 8.1: Configure Google Sign-In for Production

Before releasing, ensure Google Sign-In works in production:

1. Go to Firebase Console → Project Settings → Your Apps
2. Add your **release SHA-1 fingerprint**:
   ```bash
   keytool -list -v -keystore icare-release-key.jks -alias icare
   ```
3. Copy the SHA-1 and add it to Firebase
4. Download the updated `google-services.json` and rebuild

---

## Step 9: Post-Launch Checklist

- [ ] Verify app appears in Play Store search
- [ ] Install from Play Store on a test device
- [ ] Verify all features work with the production Firebase project
- [ ] Test Google Sign-In on production build
- [ ] Test password reset flow
- [ ] Monitor Firebase Console for errors (Crashlytics recommended for future)
- [ ] Respond to user reviews on Play Console

---

## Updating the App

For future updates:

1. Increment `versionCode` and `versionName` in `app/build.gradle.kts`:
   ```kotlin
   defaultConfig {
       versionCode = 2          // Must be higher than previous
       versionName = "1.1.0"    // Display version
   }
   ```
2. Build a new AAB: `./gradlew bundleRelease`
3. Upload to Play Console → Production → Create new release
4. Add release notes describing what changed
5. Submit for review

---

## Cost Summary

| Item | Cost |
|------|------|
| Google Play Developer Account | $25 (one-time) |
| Firebase (Spark plan) | Free (generous limits) |
| Firebase (Blaze plan) | Pay-as-you-go if you exceed free tier |
| Total to launch | **$25** |

### Firebase Free Tier Limits (Spark Plan)
- **Firestore:** 50K reads, 20K writes, 20K deletes per day
- **Auth:** Unlimited users
- **Cloud Functions:** 2M invocations/month
- **Cloud Messaging:** Unlimited
- **Storage:** 5GB

These limits are very generous for an app starting out. You'll only need to upgrade to the Blaze plan if you get significant traction.

---

## Signing Up for Google Play — Quick Steps

1. Go to https://play.google.com/console/signup
2. Sign in with your Google account
3. Accept the Developer Distribution Agreement
4. Pay the $25 registration fee
5. Complete your developer profile (name, address, email)
6. Verify your identity (may require ID upload)
7. You're ready to publish!
