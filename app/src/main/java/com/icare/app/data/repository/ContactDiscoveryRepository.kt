package com.icare.app.data.repository

import android.content.ContentResolver
import android.provider.ContactsContract
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.icare.app.data.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactDiscoveryRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    suspend fun searchByEmailOrPhone(query: String): List<User> {
        val currentUid = auth.currentUser?.uid ?: return emptyList()

        val results = mutableListOf<User>()

        if (query.contains("@")) {
            val emailHash = hashString(query.trim().lowercase())
            val snapshot = firestore.collection("users")
                .whereEqualTo("emailHash", emailHash)
                .get().await()
            results.addAll(snapshot.documents.mapNotNull { doc ->
                doc.toObject(User::class.java)?.takeIf { it.uid != currentUid }
            })
        } else {
            val normalizedPhone = query.replace(Regex("[^0-9+]"), "")
            val phoneHash = hashString(normalizedPhone)
            val snapshot = firestore.collection("users")
                .whereEqualTo("phoneHash", phoneHash)
                .get().await()
            results.addAll(snapshot.documents.mapNotNull { doc ->
                doc.toObject(User::class.java)?.takeIf { it.uid != currentUid }
            })
        }

        return results
    }

    suspend fun discoverContactsFromPhone(contentResolver: ContentResolver): List<User> =
        withContext(Dispatchers.IO) {
            val currentUid = auth.currentUser?.uid ?: return@withContext emptyList()
            val phoneHashes = mutableSetOf<String>()

            val cursor = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                null, null, null
            )

            cursor?.use {
                val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                while (it.moveToNext()) {
                    val number = it.getString(numberIndex)
                    if (number != null) {
                        val normalized = number.replace(Regex("[^0-9+]"), "")
                        if (normalized.isNotEmpty()) {
                            phoneHashes.add(hashString(normalized))
                        }
                    }
                }
            }

            if (phoneHashes.isEmpty()) return@withContext emptyList()

            val users = mutableListOf<User>()
            phoneHashes.chunked(10).forEach { chunk ->
                val snapshot = firestore.collection("users")
                    .whereIn("phoneHash", chunk)
                    .get().await()
                users.addAll(snapshot.documents.mapNotNull { doc ->
                    doc.toObject(User::class.java)?.takeIf { it.uid != currentUid }
                })
            }

            users
        }

    private fun hashString(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
