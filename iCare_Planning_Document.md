# iCare — Planning Document v1.0

> **"I Care"** — A simple Android app that lets your loved ones know how you're feeling, without saying a word.

---

## 1. Product Overview

### Vision
People drift apart not because they stop caring, but because life gets busy. iCare bridges that gap with the simplest possible interaction — tap an emoji to share how you're feeling. Your circle sees it. No messages, no calls, no pressure. Just a gentle signal that says "I'm here."

### Target Audience
- Families living apart (especially parents & children)
- Close friends who want to stay emotionally connected
- Anyone who wants a low-friction way to check in on loved ones

### Monetization
- **$1 one-time purchase** on Google Play Store
- No ads
- Premium features may be added in future versions

---

## 2. Feature Specification (v1.0)

### 2.1 Authentication
| Detail | Decision |
|--------|----------|
| Sign-up method | Email or Phone Number |
| Auth mechanism | Simple passcode (4-6 digit PIN) created by user |
| Profile info | Display name only (no profile picture in v1.0) |
| Future (v2+) | OTP-based login |

**Flow:**
1. User opens app → Sign Up / Log In screen
2. **Sign Up:** Enter email or phone → create display name → set 4-6 digit passcode → done
3. **Log In:** Enter email or phone → enter passcode → done

---

### 2.2 Home Page — "Reflection"

The core of the app. A clean screen with 3 large emojis:

| Emoji | Label | Color | Push Notification? |
|-------|-------|-------|---------------------|
| 😊 | Happy | Green | ❌ No |
| 😔 | Feeling Low | Yellow/Amber | ✅ Yes |
| 😢 | Feeling Bad | Red | ✅ Yes |

**Behavior:**
- User taps one emoji to set their current "reflection"
- Can update **multiple times per day** — contacts see the **latest** status
- Full history is preserved (see §2.5)
- Timestamp is recorded in the **user's phone timezone**
- Below the emojis: option to pick from a **predefined set of additional emojis** (e.g., 😴 Tired, 🎉 Excited, 😤 Frustrated, 🤒 Sick, 😌 Peaceful)
  - Default 3 are always shown prominently
  - Additional emojis accessible via "More" or "+" button
  - Custom emojis are **silent** (no push notification) unless categorized as "low" or "bad"

---

### 2.3 Contacts Page — "My Circle"

Second main page showing all preferred contacts and their statuses.

**Layout:**
- List view, each row shows:
  - Contact display name
  - Current emoji status
  - Timestamp ("Today 8:32 AM", "Yesterday 6:15 PM")
  - Color indicator (green / yellow / red / grey)

**Sorting (top to bottom):**
1. 🔴 Feeling Bad (red) — most urgent at top
2. 🟡 Feeling Low (yellow/amber)
3. 🟢 Happy (green)
4. ⚪ Inactive 48hr+ (grey)

**48-Hour Inactivity Rule:**
- If a contact hasn't pressed any emoji in 48 hours, their row turns **grey**
- This serves as a visual nudge to reach out

**Quick Actions on Tap:**
- Tapping a contact name → opens their **7-day history** (see §2.5)
- Tapping a greyed-out contact → shows quick action buttons:
  - 📞 **Call** (opens phone dialer)
  - 💬 **Text** (opens SMS)
  - Option to also show these for non-grey contacts

---

### 2.4 Notifications Page

A dedicated page showing push notification history.

**Content:**
- Each notification entry shows:
  - Contact name
  - Emoji + label (e.g., "Mom is feeling low 😔")
  - Timestamp
- Only "Feeling Low" and "Feeling Bad" statuses generate notifications
- Sorted by most recent first

---

### 2.5 Contact History View

Accessible by tapping a contact name on the Contacts Page.

**Layout:**
- Simple chronological list for the **past 7 days**
- Each entry: Date, Time, Emoji, Label
- Example:
  ```
  Mon, Apr 14   8:32 AM   😊 Happy
  Mon, Apr 14   3:15 PM   😔 Feeling Low
  Tue, Apr 15   9:00 AM   😊 Happy
  ```

---

### 2.6 Settings Page

| Setting | Description |
|---------|-------------|
| **Manage Contacts** | Choose which contacts can see your reflection (from phone contacts or search by email/phone) |
| **Add Contact** | Search by email or phone number to send a connection request |
| **Pending Requests** | View and accept/reject incoming connection requests |
| **Custom Emojis** | Enable/disable additional emojis from predefined set |
| **Display Name** | Edit your display name |
| **Change Passcode** | Update your 4-6 digit PIN |
| **Delete Account** | Permanently delete account and all data |
| **About / Help** | App version, walkthrough replay |

**Contact Management Rules:**
- **Adding:** Search phone contacts (who also have the app) or manually search by email/phone → sends a **connection request**
- **Accepting:** Other person must accept the request → connection becomes **mutual** (both see each other's status)
- **Removing:** Either person can remove the connection → automatically removed from both sides
- **Mutual by default:** Once connected, both users share status with each other

---

### 2.7 Onboarding Walkthrough

First-time users see a simple 3-4 screen walkthrough:

1. **"Welcome to iCare"** — Share how you feel with people who care
2. **"Tap your mood"** — Show the 3 emojis, explain one-tap reflection
3. **"Build your circle"** — Explain how to add contacts from your phone
4. **"Stay connected"** — Explain notifications and the grey indicator

Skippable, with option to replay from Settings.

---

## 3. Tech Stack

| Layer | Technology | Reasoning |
|-------|------------|-----------|
| **Language** | Kotlin | Google's recommended language for Android |
| **UI Framework** | Jetpack Compose | Modern, declarative UI — Google's recommended approach |
| **Architecture** | MVVM + Clean Architecture | Industry standard, testable, maintainable |
| **Auth** | Firebase Authentication | Email/phone auth with passcode, easy OTP migration later |
| **Database** | Cloud Firestore | Real-time sync, offline support, serverless |
| **Push Notifications** | Firebase Cloud Messaging (FCM) | Industry standard for Android push |
| **Backend Logic** | Firebase Cloud Functions (Node.js) | Trigger notifications on status changes |
| **Contact Discovery** | Phone number hashing + Firestore lookup | Privacy-respecting contact matching |
| **Min SDK** | API 26 (Android 8.0) | Covers ~95% of active devices |
| **Build System** | Gradle (Kotlin DSL) | Standard Android build tool |

---

## 4. Architecture

```
┌─────────────────────────────────────────────┐
│                 Android App                  │
│  ┌──────────┐  ┌──────────┐  ┌───────────┐  │
│  │   UI     │  │ViewModel │  │Repository  │  │
│  │(Compose) │→ │  (MVVM)  │→ │  Layer     │  │
│  └──────────┘  └──────────┘  └───────────┘  │
│                                      │       │
└──────────────────────────────────────┼───────┘
                                       │
                        ┌──────────────┼──────────────┐
                        ▼              ▼              ▼
                 ┌────────────┐ ┌───────────┐ ┌────────────┐
                 │  Firebase   │ │ Firestore │ │    FCM     │
                 │    Auth     │ │ Database  │ │   (Push)   │
                 └────────────┘ └───────────┘ └────────────┘
                                       │
                                       ▼
                              ┌─────────────────┐
                              │ Cloud Functions  │
                              │ (Notification    │
                              │  Trigger Logic)  │
                              └─────────────────┘
```

---

## 5. Data Model (Firestore Schema)

### `users` collection
```
users/{userId}
├── displayName: string
├── email: string (optional)
├── phone: string (optional)
├── phoneHash: string (for contact discovery)
├── emailHash: string (for contact discovery)
├── fcmToken: string
├── currentStatus: {
│     emoji: string ("happy" | "low" | "bad" | custom)
│     label: string
│     timestamp: Timestamp
│   }
├── customEmojis: string[] (enabled predefined emoji IDs)
├── createdAt: Timestamp
└── updatedAt: Timestamp
```

### `users/{userId}/statusHistory` subcollection
```
statusHistory/{entryId}
├── emoji: string
├── label: string
├── timestamp: Timestamp
└── timezone: string
```

### `connections` collection
```
connections/{connectionId}
├── userA: string (userId)
├── userB: string (userId)
├── status: "pending" | "accepted"
├── initiatedBy: string (userId)
├── createdAt: Timestamp
└── updatedAt: Timestamp
```

### `notifications` collection (per user)
```
users/{userId}/notifications/{notifId}
├── fromUserId: string
├── fromDisplayName: string
├── emoji: string
├── label: string
├── timestamp: Timestamp
└── read: boolean
```

---

## 6. Push Notification Design

### Trigger Logic (Cloud Function)
```
When a user updates their status:
  1. Check if new status is "low" or "bad"
  2. If yes → query all accepted connections for this user
  3. For each connected user → send FCM push notification
  4. Store notification in recipient's notifications subcollection
```

### Notification Content
| Status | Title | Body |
|--------|-------|------|
| Feeling Low | iCare | "{DisplayName} is feeling low 😔" |
| Feeling Bad | iCare | "{DisplayName} is feeling bad 😢" |

### Custom Emoji Notifications
- Custom emojis are **silent by default** (no push)
- If a custom emoji is categorized by the app as "negative sentiment," it could optionally trigger a notification (future consideration)

---

## 7. Screen Map & Navigation

```
App Launch
    │
    ├── First Time → Onboarding Walkthrough → Sign Up
    │
    └── Returning User → Login
            │
            ▼
    ┌─ Bottom Navigation ──────────────────────┐
    │                                           │
    │  [Home]     [My Circle]    [Notifications]│
    │                                           │
    │  Home:                                    │
    │    - 3 default emojis                     │
    │    - "More" button for custom emojis      │
    │    - Current status display                │
    │                                           │
    │  My Circle:                               │
    │    - Contact list with statuses           │
    │    - Tap → 7-day history                  │
    │    - Grey = 48hr inactive                 │
    │    - Quick actions (call/text)            │
    │                                           │
    │  Notifications:                           │
    │    - Push notification history            │
    │    - "Low" and "Bad" updates only         │
    │                                           │
    │  [Settings] (gear icon / profile)         │
    │    - Manage contacts                      │
    │    - Pending requests                     │
    │    - Custom emojis                        │
    │    - Account settings                     │
    │    - Delete account                       │
    └───────────────────────────────────────────┘
```

---

## 8. App Icon & Color Theme Proposal

### Color Palette
| Role | Color | Hex | Usage |
|------|-------|-----|-------|
| Primary | Warm Coral | `#FF6B6B` | App bar, buttons, accents |
| Secondary | Soft Teal | `#4ECDC4` | Secondary actions, links |
| Happy | Green | `#2ECC71` | Happy status indicator |
| Low | Amber | `#F39C12` | Feeling low indicator |
| Bad | Red | `#E74C3C` | Feeling bad indicator |
| Inactive | Grey | `#BDC3C7` | 48hr inactive indicator |
| Background | Warm White | `#FFF9F5` | App background |
| Text | Dark Charcoal | `#2C3E50` | Primary text |

### App Icon Concept
- A **heart shape** with a gentle smile/face inside it
- Colors: Warm Coral (#FF6B6B) heart on white background
- Simple, recognizable at small sizes
- Conveys: care, warmth, emotional connection

---

## 9. Development Roadmap — v1.0

### Milestone 1: Project Setup & Auth (Week 1)
- [ ] Initialize Android project (Kotlin + Jetpack Compose)
- [ ] Set up Firebase project (Auth, Firestore, FCM, Cloud Functions)
- [ ] Implement Sign Up screen (email/phone + name + passcode)
- [ ] Implement Login screen
- [ ] Build passcode creation & validation

### Milestone 2: Home Page — Reflection (Week 2)
- [ ] Design and build home screen with 3 default emojis
- [ ] Implement emoji tap → save to Firestore
- [ ] Add "More" button with predefined emoji picker
- [ ] Display current status with timestamp
- [ ] Store status history in subcollection

### Milestone 3: Contact Management (Week 2-3)
- [ ] Phone contacts sync & hash-based discovery
- [ ] Search by email/phone
- [ ] Connection request send/accept/reject flow
- [ ] Mutual connection logic
- [ ] Remove connection (auto-remove on both sides)

### Milestone 4: My Circle Page (Week 3)
- [ ] Contact list with real-time status updates
- [ ] Color-coded status indicators
- [ ] Sorting: bad → low → happy → inactive
- [ ] 48-hour grey indicator
- [ ] Quick actions (call/text) on contact tap
- [ ] 7-day history view on contact name tap

### Milestone 5: Notifications (Week 4)
- [ ] Firebase Cloud Function for status change triggers
- [ ] FCM push notification for "low" and "bad" statuses
- [ ] Notification history page in-app
- [ ] Mark as read functionality

### Milestone 6: Settings & Polish (Week 4-5)
- [ ] Settings page (all options listed in §2.6)
- [ ] Account deletion flow
- [ ] Onboarding walkthrough (3-4 screens)
- [ ] Error handling & edge cases
- [ ] UI polish & animations

### Milestone 7: Testing & Launch (Week 5-6)
- [ ] Unit tests (ViewModels, repositories)
- [ ] UI tests (Compose testing)
- [ ] Beta testing (internal track on Play Store)
- [ ] Privacy policy & terms of service
- [ ] Play Store listing (screenshots, description)
- [ ] Set price to $1
- [ ] Launch 🚀

---

## 10. Key Technical Decisions Summary

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Auth method (v1) | Passcode | Simple for MVP; OTP in v2 |
| Connection model | Mutual with approval | Respects privacy, prevents unwanted sharing |
| Status visibility | Hidden until approved | No one sees your status without your consent |
| Notification trigger | Cloud Function | Server-side ensures reliability even when app is closed |
| Contact discovery | Phone hash + manual search | Balances convenience with privacy |
| History retention | 7 days visible, full history stored | Lightweight UI with full data preserved for future features |
| Custom emojis | Predefined set | Keeps experience consistent; avoids abuse |
| Go invisible | Not allowed (v1) | Core to the app's purpose — always share |
| Offline support | Firestore offline cache | Users can view cached data when offline; syncs when back |

---

## 11. Privacy & Play Store Compliance

- **Permissions required:** Contacts (for discovery), Internet, Push Notifications
- **Data collected:** Email/phone, display name, emoji status history
- **Data sharing:** Only with mutually connected users
- **Account deletion:** Full data wipe on request (GDPR/Play Store compliant)
- **Privacy policy:** Required before Play Store submission
- **No ads, no tracking SDKs**

---

## 12. Future Considerations (Post v1.0)

- OTP-based authentication
- iOS version (could share Firebase backend)
- Escalation for repeated "feeling bad" (helpline suggestion)
- Group circles (family group, friends group)
- Widget for home screen (one-tap emoji without opening app)
- Wearable support (Wear OS)
- Profile pictures
- Custom status messages (text alongside emoji)
- Weekly mood summary / trends
- Premium features for monetization

---

*Document prepared for: iCare Android App v1.0*
*Date: April 15, 2026*
