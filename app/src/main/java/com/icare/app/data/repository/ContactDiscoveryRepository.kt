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
        val trimmedQuery = query.trim()
        
        if (trimmedQuery.isEmpty()) return emptyList()

        val results = mutableListOf<User>()

        // Check if it's an iCareID (format: iCare.XXXX.abc)
        if (trimmedQuery.startsWith("iCare.", ignoreCase = true) || 
            trimmedQuery.startsWith("icare.", ignoreCase = true)) {
            // Try exact match on iCareId field
            var snapshot = firestore.collection("users")
                .whereEqualTo("iCareId", trimmedQuery)
                .get().await()
            
            // Try with proper casing if no results
            if (snapshot.isEmpty) {
                snapshot = firestore.collection("users")
                    .whereEqualTo("iCareId", "iCare" + trimmedQuery.substring(5))
                    .get().await()
            }
            
            // Also try icareId (lowercase) for legacy data
            if (snapshot.isEmpty) {
                snapshot = firestore.collection("users")
                    .whereEqualTo("icareId", trimmedQuery)
                    .get().await()
            }
            
            results.addAll(snapshot.documents.mapNotNull { doc ->
                doc.toObject(User::class.java)?.takeIf { it.uid != currentUid }
            })
        } else if (trimmedQuery.contains("@")) {
            // Search by email - try multiple variations
            val emailLower = trimmedQuery.lowercase()
            val emailOriginal = trimmedQuery
            
            // Try lowercase
            var snapshot = firestore.collection("users")
                .whereEqualTo("email", emailLower)
                .get().await()
            results.addAll(snapshot.documents.mapNotNull { doc ->
                doc.toObject(User::class.java)?.takeIf { it.uid != currentUid }
            })
            
            // Try original case if no results
            if (results.isEmpty() && emailOriginal != emailLower) {
                snapshot = firestore.collection("users")
                    .whereEqualTo("email", emailOriginal)
                    .get().await()
                results.addAll(snapshot.documents.mapNotNull { doc ->
                    doc.toObject(User::class.java)?.takeIf { it.uid != currentUid }
                })
            }
            
            // Fallback to hash match
            if (results.isEmpty()) {
                val emailHash = hashString(emailLower)
                snapshot = firestore.collection("users")
                    .whereEqualTo("emailHash", emailHash)
                    .get().await()
                results.addAll(snapshot.documents.mapNotNull { doc ->
                    doc.toObject(User::class.java)?.takeIf { it.uid != currentUid }
                })
            }
        } else {
            // Search by phone - try multiple variations
            val normalizedPhone = trimmedQuery.replace(Regex("[^0-9+]"), "")
            
            // Try direct match
            var snapshot = firestore.collection("users")
                .whereEqualTo("phone", normalizedPhone)
                .get().await()
            results.addAll(snapshot.documents.mapNotNull { doc ->
                doc.toObject(User::class.java)?.takeIf { it.uid != currentUid }
            })
            
            // Try with + prefix if not present
            if (results.isEmpty() && !normalizedPhone.startsWith("+")) {
                snapshot = firestore.collection("users")
                    .whereEqualTo("phone", "+$normalizedPhone")
                    .get().await()
                results.addAll(snapshot.documents.mapNotNull { doc ->
                    doc.toObject(User::class.java)?.takeIf { it.uid != currentUid }
                })
            }
            
            // Try without + prefix if present
            if (results.isEmpty() && normalizedPhone.startsWith("+")) {
                snapshot = firestore.collection("users")
                    .whereEqualTo("phone", normalizedPhone.substring(1))
                    .get().await()
                results.addAll(snapshot.documents.mapNotNull { doc ->
                    doc.toObject(User::class.java)?.takeIf { it.uid != currentUid }
                })
            }
            
            // Fallback to hash match
            if (results.isEmpty()) {
                val phoneHash = hashString(normalizedPhone)
                snapshot = firestore.collection("users")
                    .whereEqualTo("phoneHash", phoneHash)
                    .get().await()
                results.addAll(snapshot.documents.mapNotNull { doc ->
                    doc.toObject(User::class.java)?.takeIf { it.uid != currentUid }
                })
            }
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
