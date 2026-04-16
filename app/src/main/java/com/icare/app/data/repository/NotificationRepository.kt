package com.icare.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.icare.app.data.model.AppNotification
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
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

                trySend(notifications)
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
}
