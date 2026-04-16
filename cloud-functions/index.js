const { onDocumentWritten } = require("firebase-functions/v2/firestore");
const { initializeApp } = require("firebase-admin/app");
const { getFirestore, Timestamp } = require("firebase-admin/firestore");
const { getMessaging } = require("firebase-admin/messaging");

initializeApp();

const db = getFirestore();
const messaging = getMessaging();

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
      fromUserId: userId,
      fromDisplayName: displayName,
      emoji: newStatus.emoji,
      label: newStatus.label,
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
