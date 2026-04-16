package com.icare.app.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.icare.app.data.model.CurrentStatus
import com.icare.app.data.model.EmojiStatus
import com.icare.app.data.model.StatusEntry
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatusRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    suspend fun updateStatus(emojiStatus: EmojiStatus): Result<Unit> = runCatching {
        val uid = auth.currentUser?.uid ?: throw Exception("Not logged in")
        val timezone = TimeZone.getDefault().id
        val now = Timestamp.now()

        val currentStatus = CurrentStatus(
            emojiId = emojiStatus.id,
            emoji = emojiStatus.emoji,
            label = emojiStatus.label,
            timestamp = now,
            timezone = timezone
        )

        firestore.collection("users").document(uid)
            .update(
                "currentStatus", currentStatus,
                "updatedAt", now
            ).await()

        val historyEntry = StatusEntry(
            emojiId = emojiStatus.id,
            emoji = emojiStatus.emoji,
            label = emojiStatus.label,
            timestamp = now,
            timezone = timezone
        )

        firestore.collection("users").document(uid)
            .collection("statusHistory")
            .add(historyEntry).await()
    }

    fun observeCurrentStatus(): Flow<CurrentStatus?> = callbackFlow {
        val uid = auth.currentUser?.uid ?: run {
            trySend(null)
            close()
            return@callbackFlow
        }

        val listener = firestore.collection("users").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                val status = snapshot?.get("currentStatus") as? Map<*, *>
                if (status != null) {
                    trySend(
                        CurrentStatus(
                            emojiId = status["emojiId"] as? String ?: "",
                            emoji = status["emoji"] as? String ?: "",
                            label = status["label"] as? String ?: "",
                            timestamp = status["timestamp"] as? Timestamp,
                            timezone = status["timezone"] as? String ?: ""
                        )
                    )
                } else {
                    trySend(null)
                }
            }

        awaitClose { listener.remove() }
    }

    suspend fun getStatusHistory(userId: String, daysBack: Int = 7): List<StatusEntry> {
        val cutoff = Timestamp(
            Timestamp.now().seconds - (daysBack * 24 * 60 * 60),
            0
        )

        val snapshot = firestore.collection("users").document(userId)
            .collection("statusHistory")
            .whereGreaterThan("timestamp", cutoff)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(StatusEntry::class.java)?.copy(id = doc.id)
        }
    }
}
