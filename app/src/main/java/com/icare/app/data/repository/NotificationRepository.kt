package com.icare.app.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.icare.app.data.model.AppNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    private val uid get() = auth.currentUser?.uid ?: ""

    fun observeNotifications(): Flow<List<AppNotification>> = callbackFlow {
        if (uid.isEmpty()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = firestore.collection("users").document(uid)
            .collection("notifications")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val notifications = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(AppNotification::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                // Fetch missing display names
                CoroutineScope(Dispatchers.IO).launch {
                    val enrichedNotifications = notifications.map { notification ->
                        if (notification.fromDisplayName.isEmpty() && notification.fromUserId.isNotEmpty()) {
                            try {
                                val userDoc = firestore.collection("users")
                                    .document(notification.fromUserId)
                                    .get().await()
                                val displayName = userDoc.getString("displayName") ?: "Someone"
                                notification.copy(fromDisplayName = displayName)
                            } catch (e: Exception) {
                                notification
                            }
                        } else {
                            notification
                        }
                    }
                    trySend(enrichedNotifications)
                }
            }

        awaitClose { listener.remove() }
    }

    suspend fun markAsRead(notificationId: String): Result<Unit> = runCatching {
        if (uid.isEmpty()) throw Exception("Not logged in")
        firestore.collection("users").document(uid)
            .collection("notifications")
            .document(notificationId)
            .update("read", true)
            .await()
    }

    suspend fun getUnreadCount(): Int {
        if (uid.isEmpty()) return 0
        val snapshot = firestore.collection("users").document(uid)
            .collection("notifications")
            .whereEqualTo("read", false)
            .get().await()
        return snapshot.size()
    }

    suspend fun deleteNotification(notificationId: String): Result<Unit> = runCatching {
        if (uid.isEmpty()) throw Exception("Not logged in")
        firestore.collection("users").document(uid)
            .collection("notifications")
            .document(notificationId)
            .delete()
            .await()
    }

    suspend fun deleteAllNotifications(): Result<Unit> = runCatching {
        if (uid.isEmpty()) throw Exception("Not logged in")
        val snapshot = firestore.collection("users").document(uid)
            .collection("notifications")
            .get().await()
        
        val batch = firestore.batch()
        snapshot.documents.forEach { doc ->
            batch.delete(doc.reference)
        }
        batch.commit().await()
    }

    suspend fun deleteOldNotifications(): Result<Int> = runCatching {
        if (uid.isEmpty()) throw Exception("Not logged in")
        
        // Calculate timestamp for 7 days ago
        val sevenDaysAgo = Timestamp(
            System.currentTimeMillis() / 1000 - (7 * 24 * 60 * 60),
            0
        )
        
        val snapshot = firestore.collection("users").document(uid)
            .collection("notifications")
            .whereLessThan("timestamp", sevenDaysAgo)
            .get().await()
        
        if (snapshot.documents.isEmpty()) return@runCatching 0
        
        val batch = firestore.batch()
        snapshot.documents.forEach { doc ->
            batch.delete(doc.reference)
        }
        batch.commit().await()
        snapshot.documents.size
    }
}
