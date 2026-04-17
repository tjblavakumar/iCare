const { onDocumentWritten } = require("firebase-functions/v2/firestore");
const { onCall, HttpsError } = require("firebase-functions/v2/https");
const { initializeApp } = require("firebase-admin/app");
const { getFirestore, Timestamp, FieldValue } = require("firebase-admin/firestore");
const { getMessaging } = require("firebase-admin/messaging");
const nodemailer = require("nodemailer");

initializeApp();

const db = getFirestore();
const messaging = getMessaging();

// Configure email transporter (using Gmail SMTP - configure in Firebase environment)
// Set these in Firebase: firebase functions:config:set email.user="your@gmail.com" email.pass="app-password"
const getMailTransporter = () => {
  return nodemailer.createTransport({
    service: "gmail",
    auth: {
      user: process.env.EMAIL_USER || "noreply@icare.app",
      pass: process.env.EMAIL_PASS || "",
    },
  });
};

/**
 * Generate a random 4-digit OTP
 */
const generateOtp = () => {
  return Math.floor(1000 + Math.random() * 9000).toString();
};

/**
 * Generate a unique iCareId
 * Format: iCare.XXXX.abc (4 random digits + 3 chars from email)
 */
const generateICareId = async (email) => {
  const emailPrefix = email
    .split("@")[0]
    .toLowerCase()
    .replace(/[^a-z0-9]/g, "");
  
  let prefix = emailPrefix.substring(0, 3);
  
  // If email prefix is shorter than 3 chars, pad with random chars
  while (prefix.length < 3) {
    prefix += String.fromCharCode(97 + Math.floor(Math.random() * 26));
  }
  
  // Try to generate a unique ID (max 10 attempts)
  for (let i = 0; i < 10; i++) {
    const randomNum = Math.floor(1000 + Math.random() * 9000);
    const iCareId = `iCare.${randomNum}.${prefix}`;
    
    // Check if this ID already exists
    const existing = await db
      .collection("users")
      .where("iCareId", "==", iCareId)
      .limit(1)
      .get();
    
    if (existing.empty) {
      return iCareId;
    }
  }
  
  // Fallback: use timestamp to ensure uniqueness
  const timestamp = Date.now().toString().slice(-4);
  return `iCare.${timestamp}.${prefix}`;
};

/**
 * Send OTP to user's email for verification
 */
exports.sendOtp = onCall(async (request) => {
  const { email, displayName } = request.data;
  
  if (!email) {
    throw new HttpsError("invalid-argument", "Email is required");
  }
  
  // Validate email format
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRegex.test(email)) {
    throw new HttpsError("invalid-argument", "Invalid email format");
  }
  
  const otp = generateOtp();
  const expiresAt = Timestamp.fromDate(new Date(Date.now() + 5 * 60 * 1000)); // 5 minutes
  
  // Store OTP in Firestore
  const otpRef = db.collection("otpVerifications").doc(email.toLowerCase());
  await otpRef.set({
    otp: otp,
    email: email.toLowerCase(),
    expiresAt: expiresAt,
    attempts: 0,
    createdAt: Timestamp.now(),
  });
  
  // Send email
  try {
    const transporter = getMailTransporter();
    
    await transporter.sendMail({
      from: '"iCare App" <noreply@icare.app>',
      to: email,
      subject: "Your iCare Verification Code",
      html: `
        <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
          <h2 style="color: #5B9BD5;">Welcome to iCare${displayName ? `, ${displayName}` : ""}!</h2>
          <p>Your verification code is:</p>
          <div style="background-color: #f5f5f5; padding: 20px; text-align: center; border-radius: 8px; margin: 20px 0;">
            <span style="font-size: 32px; font-weight: bold; letter-spacing: 8px; color: #5B9BD5;">${otp}</span>
          </div>
          <p>This code will expire in <strong>5 minutes</strong>.</p>
          <p>If you didn't request this code, please ignore this email.</p>
          <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
          <p style="color: #888; font-size: 12px;">iCare - Stay connected with the people you love ❤️</p>
        </div>
      `,
    });
    
    console.log(`OTP sent to ${email}`);
    return { success: true, message: "OTP sent successfully" };
  } catch (error) {
    console.error("Failed to send OTP email:", error);
    throw new HttpsError("internal", "Failed to send verification email. Please try again.");
  }
});

/**
 * Verify OTP and return success status
 */
exports.verifyOtp = onCall(async (request) => {
  const { email, otp } = request.data;
  
  if (!email || !otp) {
    throw new HttpsError("invalid-argument", "Email and OTP are required");
  }
  
  const otpRef = db.collection("otpVerifications").doc(email.toLowerCase());
  const otpDoc = await otpRef.get();
  
  if (!otpDoc.exists) {
    throw new HttpsError("not-found", "No verification code found. Please request a new one.");
  }
  
  const otpData = otpDoc.data();
  
  // Check if expired
  if (otpData.expiresAt.toDate() < new Date()) {
    await otpRef.delete();
    throw new HttpsError("deadline-exceeded", "Verification code has expired. Please request a new one.");
  }
  
  // Check attempts (max 5)
  if (otpData.attempts >= 5) {
    await otpRef.delete();
    throw new HttpsError("resource-exhausted", "Too many attempts. Please request a new code.");
  }
  
  // Verify OTP
  if (otpData.otp !== otp) {
    await otpRef.update({ attempts: FieldValue.increment(1) });
    throw new HttpsError("permission-denied", "Invalid verification code. Please try again.");
  }
  
  // OTP is valid - delete it and return success
  await otpRef.delete();
  
  console.log(`OTP verified for ${email}`);
  return { success: true, verified: true };
});

/**
 * Generate iCareId for a new user (called after successful signup)
 */
exports.generateICareId = onCall(async (request) => {
  const { email, userId } = request.data;
  
  if (!email || !userId) {
    throw new HttpsError("invalid-argument", "Email and userId are required");
  }
  
  const iCareId = await generateICareId(email);
  
  // Update user document with iCareId
  await db.collection("users").doc(userId).update({
    iCareId: iCareId,
    emailVerified: true,
  });
  
  console.log(`Generated iCareId ${iCareId} for user ${userId}`);
  return { success: true, iCareId: iCareId };
});

/**
 * Triggered when a user's document is updated.
 * Checks if the currentStatus changed to "low" or "bad" and sends
 * push notifications to all connected users.
 */
exports.onStatusChange = onDocumentWritten("users/{userId}", async (event) => {
  const beforeData = event.data.before?.data();
  const afterData = event.data.after?.data();

  if (!afterData || !afterData.currentStatus) {
    return null;
  }

  const newStatus = afterData.currentStatus;
  const oldStatus = beforeData?.currentStatus;

  // Only notify if status actually changed
  if (
    oldStatus &&
    oldStatus.emojiId === newStatus.emojiId &&
    oldStatus.timestamp?.seconds === newStatus.timestamp?.seconds
  ) {
    return null;
  }

  // Only send notifications for negative statuses
  const negativeStatuses = ["low", "bad", "angry", "sick", "anxious"];
  if (!negativeStatuses.includes(newStatus.emojiId)) {
    return null;
  }

  const userId = event.params.userId;
  const displayName = afterData.displayName || "Someone";

  // Find all accepted connections for this user
  const connectionsA = await db
    .collection("connections")
    .where("userA", "==", userId)
    .where("status", "==", "accepted")
    .get();

  const connectionsB = await db
    .collection("connections")
    .where("userB", "==", userId)
    .where("status", "==", "accepted")
    .get();

  const connectedUserIds = new Set();
  connectionsA.docs.forEach((doc) => {
    const data = doc.data();
    connectedUserIds.add(data.userB);
  });
  connectionsB.docs.forEach((doc) => {
    const data = doc.data();
    connectedUserIds.add(data.userA);
  });

  if (connectedUserIds.size === 0) {
    return null;
  }

  // Get FCM tokens for connected users and send notifications
  const notifications = [];

  for (const connectedUserId of connectedUserIds) {
    const userDoc = await db.collection("users").doc(connectedUserId).get();
    const userData = userDoc.data();

    if (!userData || !userData.fcmToken) {
      continue;
    }

    // Build notification
    const title = "iCare";
    const body = `${displayName} is ${newStatus.label.toLowerCase()} ${newStatus.emoji}`;

    // Send push notification
    const messagePayload = {
      token: userData.fcmToken,
      notification: {
        title: title,
        body: body,
      },
      data: {
        title: title,
        body: body,
        fromUserId: userId,
        emojiId: newStatus.emojiId,
        type: "status_update",
      },
      android: {
        priority: "high",
        notification: {
          channelId: "icare_notifications",
          priority: "high",
          defaultSound: true,
          defaultVibrateTimings: true,
        },
      },
    };

    try {
      await messaging.send(messagePayload);
    } catch (error) {
      console.error(
        `Failed to send notification to ${connectedUserId}:`,
        error
      );
    }

    // Store notification in recipient's subcollection
    const notificationDoc = {
      type: "status_update",
      fromUserId: userId,
      fromDisplayName: displayName,
      emoji: newStatus.emoji,
      label: newStatus.label,
      message: "",
      timestamp: Timestamp.now(),
      read: false,
    };

    notifications.push(
      db
        .collection("users")
        .doc(connectedUserId)
        .collection("notifications")
        .add(notificationDoc)
    );
  }

  await Promise.all(notifications);
  console.log(
    `Sent ${notifications.length} notifications for ${displayName}'s status: ${newStatus.label}`
  );

  return null;
});
