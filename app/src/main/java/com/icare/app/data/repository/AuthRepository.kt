package com.icare.app.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.icare.app.data.model.User
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val messaging: FirebaseMessaging
) {
    val currentUser get() = auth.currentUser
    val isLoggedIn get() = auth.currentUser != null

    suspend fun signUp(
        emailOrPhone: String,
        passcode: String,
        displayName: String,
        recoveryEmail: String = ""
    ): Result<User> = runCatching {
        val email = normalizeToEmail(emailOrPhone)
        val result = auth.createUserWithEmailAndPassword(email, passcode).await()
        val uid = result.user?.uid ?: throw Exception("Failed to create user")

        val fcmToken = try {
            messaging.token.await()
        } catch (e: Exception) {
            ""
        }

        val isPhoneUser = !emailOrPhone.contains("@")
        val user = User(
            uid = uid,
            displayName = displayName,
            email = if (!isPhoneUser) emailOrPhone else "",
            phone = if (isPhoneUser) emailOrPhone else "",
            recoveryEmail = if (isPhoneUser) recoveryEmail else "",
            emailHash = if (!isPhoneUser) hashString(emailOrPhone.lowercase()) else "",
            phoneHash = if (isPhoneUser) hashString(normalizePhone(emailOrPhone)) else "",
            fcmToken = fcmToken,
            authProvider = "email",
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now()
        )

        firestore.collection("users").document(uid).set(user).await()
        user
    }

    suspend fun signInWithGoogle(idToken: String): Result<User> = runCatching {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).await()
        val firebaseUser = result.user ?: throw Exception("Google sign-in failed")
        val uid = firebaseUser.uid
        val isNewUser = result.additionalUserInfo?.isNewUser ?: false

        val fcmToken = try {
            messaging.token.await()
        } catch (e: Exception) {
            ""
        }

        if (isNewUser) {
            val user = User(
                uid = uid,
                displayName = firebaseUser.displayName ?: "User",
                email = firebaseUser.email ?: "",
                emailHash = hashString((firebaseUser.email ?: "").lowercase()),
                fcmToken = fcmToken,
                photoUrl = firebaseUser.photoUrl?.toString() ?: "",
                authProvider = "google",
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )
            firestore.collection("users").document(uid).set(user).await()
            user
        } else {
            if (fcmToken.isNotEmpty()) {
                firestore.collection("users").document(uid)
                    .update("fcmToken", fcmToken, "updatedAt", Timestamp.now()).await()
            }
            val doc = firestore.collection("users").document(uid).get().await()
            doc.toObject(User::class.java) ?: throw Exception("User data not found")
        }
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> = runCatching {
        auth.sendPasswordResetEmail(email).await()
    }

    suspend fun getRecoveryEmailForPhone(phone: String): String? {
        val normalizedPhone = normalizePhone(phone)
        val phoneHash = hashString(normalizedPhone)
        val snapshot = firestore.collection("users")
            .whereEqualTo("phoneHash", phoneHash)
            .limit(1)
            .get()
            .await()
        return snapshot.documents.firstOrNull()?.getString("recoveryEmail")
    }

    suspend fun login(emailOrPhone: String, passcode: String): Result<User> = runCatching {
        val email = normalizeToEmail(emailOrPhone)
        val result = auth.signInWithEmailAndPassword(email, passcode).await()
        val uid = result.user?.uid ?: throw Exception("Login failed")

        val fcmToken = try {
            messaging.token.await()
        } catch (e: Exception) {
            ""
        }
        if (fcmToken.isNotEmpty()) {
            firestore.collection("users").document(uid)
                .update("fcmToken", fcmToken, "updatedAt", Timestamp.now()).await()
        }

        val doc = firestore.collection("users").document(uid).get().await()
        doc.toObject(User::class.java) ?: throw Exception("User data not found")
    }

    fun logout() {
        auth.signOut()
    }

    suspend fun deleteAccount(): Result<Unit> = runCatching {
        val uid = currentUser?.uid ?: throw Exception("Not logged in")

        firestore.collection("users").document(uid).delete().await()

        val connections = firestore.collection("connections")
            .whereEqualTo("userA", uid).get().await()
        connections.documents.forEach { it.reference.delete().await() }

        val connectionsB = firestore.collection("connections")
            .whereEqualTo("userB", uid).get().await()
        connectionsB.documents.forEach { it.reference.delete().await() }

        currentUser?.delete()?.await()
    }

    suspend fun getCurrentUserData(): User? {
        val uid = currentUser?.uid ?: return null
        val doc = firestore.collection("users").document(uid).get().await()
        return doc.toObject(User::class.java)
    }

    suspend fun updateDisplayName(name: String): Result<Unit> = runCatching {
        val uid = currentUser?.uid ?: throw Exception("Not logged in")
        firestore.collection("users").document(uid)
            .update("displayName", name, "updatedAt", Timestamp.now()).await()
    }

    private fun normalizeToEmail(input: String): String {
        return if (input.contains("@")) {
            input.trim()
        } else {
            "${normalizePhone(input)}@icare.phone.auth"
        }
    }

    private fun normalizePhone(phone: String): String {
        return phone.replace(Regex("[^0-9+]"), "")
    }

    private fun hashString(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
