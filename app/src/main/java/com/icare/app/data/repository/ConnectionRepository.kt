package com.icare.app.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.icare.app.data.model.Connection
import com.icare.app.data.model.ConnectionStatus
import com.icare.app.data.model.CurrentStatus
import com.icare.app.data.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

data class ContactWithStatus(
    val userId: String,
    val displayName: String,
    val phone: String,
    val email: String,
    val currentStatus: CurrentStatus?,
    val isInactive: Boolean
)

@Singleton
class ConnectionRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    private val uid get() = auth.currentUser?.uid ?: ""

    suspend fun sendConnectionRequest(targetUserId: String): Result<Unit> = runCatching {
        if (uid.isEmpty()) throw Exception("Not logged in")

        val existing = firestore.collection("connections")
            .whereEqualTo("userA", uid)
            .whereEqualTo("userB", targetUserId)
            .get().await()

        val existingReverse = firestore.collection("connections")
            .whereEqualTo("userA", targetUserId)
            .whereEqualTo("userB", uid)
            .get().await()

        if (existing.documents.isNotEmpty() || existingReverse.documents.isNotEmpty()) {
            throw Exception("Connection already exists")
        }

        // Get current user's display name
        val currentUserDoc = firestore.collection("users").document(uid).get().await()
        val currentUserName = currentUserDoc.getString("displayName") ?: "Someone"

        val connection = Connection(
            userA = uid,
            userB = targetUserId,
            status = ConnectionStatus.PENDING.name.lowercase(),
            initiatedBy = uid,
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now()
        )

        firestore.collection("connections").add(connection).await()

        // Create notification for the target user
        val notification = hashMapOf(
            "type" to "connection_request",
            "fromUserId" to uid,
            "fromDisplayName" to currentUserName,
            "emoji" to "👋",
            "label" to "wants to connect",
            "message" to "$currentUserName wants to add you to their circle",
            "timestamp" to Timestamp.now(),
            "read" to false
        )

        firestore.collection("users").document(targetUserId)
            .collection("notifications")
            .add(notification).await()
    }

    suspend fun acceptConnection(connectionId: String): Result<Unit> = runCatching {
        firestore.collection("connections").document(connectionId)
            .update(
                "status", ConnectionStatus.ACCEPTED.name.lowercase(),
                "updatedAt", Timestamp.now()
            ).await()
    }

    suspend fun rejectConnection(connectionId: String): Result<Unit> = runCatching {
        firestore.collection("connections").document(connectionId).delete().await()
    }

    suspend fun removeConnection(connectionId: String): Result<Unit> = runCatching {
        firestore.collection("connections").document(connectionId).delete().await()
    }

    fun observeAcceptedConnections(): Flow<List<ContactWithStatus>> = callbackFlow {
        if (uid.isEmpty()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listenerA = firestore.collection("connections")
            .whereEqualTo("userA", uid)
            .whereEqualTo("status", "accepted")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val contacts = loadContactsFromConnections(snapshot.documents.mapNotNull { doc ->
                            doc.toObject(Connection::class.java)?.copy(id = doc.id)
                        })
                        trySend(contacts)
                    }
                }
            }

        awaitClose { listenerA.remove() }
    }

    suspend fun getAcceptedContacts(): List<ContactWithStatus> {
        if (uid.isEmpty()) return emptyList()

        val connectionsA = firestore.collection("connections")
            .whereEqualTo("userA", uid)
            .whereEqualTo("status", "accepted")
            .get().await()

        val connectionsB = firestore.collection("connections")
            .whereEqualTo("userB", uid)
            .whereEqualTo("status", "accepted")
            .get().await()

        val allConnections = (connectionsA.documents + connectionsB.documents).mapNotNull { doc ->
            doc.toObject(Connection::class.java)?.copy(id = doc.id)
        }

        return loadContactsFromConnections(allConnections)
    }

    suspend fun getPendingRequestsForMe(): List<Pair<Connection, User>> {
        if (uid.isEmpty()) return emptyList()

        val pending = firestore.collection("connections")
            .whereEqualTo("userB", uid)
            .whereEqualTo("status", "pending")
            .get().await()

        return pending.documents.mapNotNull { doc ->
            val connection = doc.toObject(Connection::class.java)?.copy(id = doc.id) ?: return@mapNotNull null
            val userDoc = firestore.collection("users").document(connection.initiatedBy).get().await()
            val user = userDoc.toObject(User::class.java) ?: return@mapNotNull null
            Pair(connection, user)
        }
    }

    suspend fun getMyConnections(): List<Pair<Connection, User>> {
        if (uid.isEmpty()) return emptyList()

        val connectionsA = firestore.collection("connections")
            .whereEqualTo("userA", uid)
            .whereEqualTo("status", "accepted")
            .get().await()

        val connectionsB = firestore.collection("connections")
            .whereEqualTo("userB", uid)
            .whereEqualTo("status", "accepted")
            .get().await()

        val allConnections = (connectionsA.documents + connectionsB.documents).mapNotNull { doc ->
            doc.toObject(Connection::class.java)?.copy(id = doc.id)
        }

        return allConnections.mapNotNull { connection ->
            val otherUid = connection.getOtherUserId(uid)
            val userDoc = firestore.collection("users").document(otherUid).get().await()
            val user = userDoc.toObject(User::class.java) ?: return@mapNotNull null
            Pair(connection, user)
        }
    }

    private suspend fun loadContactsFromConnections(connections: List<Connection>): List<ContactWithStatus> {
        val now = Timestamp.now()
        val fortyEightHoursAgo = Timestamp(now.seconds - (48 * 60 * 60), 0)

        return connections.mapNotNull { connection ->
            val otherUid = connection.getOtherUserId(uid)
            val userDoc = firestore.collection("users").document(otherUid).get().await()
            val user = userDoc.toObject(User::class.java) ?: return@mapNotNull null

            val status = user.currentStatus
            val isInactive = status?.timestamp == null ||
                    status.timestamp.seconds < fortyEightHoursAgo.seconds

            ContactWithStatus(
                userId = otherUid,
                displayName = user.displayName,
                phone = user.phone,
                email = user.email,
                currentStatus = status,
                isInactive = isInactive
            )
        }
    }
}
