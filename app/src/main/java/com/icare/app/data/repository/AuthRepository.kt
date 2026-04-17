package com.icare.app.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
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
    private val functions = FirebaseFunctions.getInstance()

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
        
        val result = try {
            auth.signInWithEmailAndPassword(email, passcode).await()
        } catch (e: Exception) {
            throw Exception("Invalid email/phone or passcode. Please try again.")
        }
        
        val uid = result.user?.uid ?: throw Exception("Login failed. Please try again.")

        val fcmToken = try {
            messaging.token.await()
        } catch (e: Exception) {
            ""
        }
        
        try {
            if (fcmToken.isNotEmpty()) {
                firestore.collection("users").document(uid)
                    .update("fcmToken", fcmToken, "updatedAt", Timestamp.now()).await()
            }
        } catch (e: Exception) {
            // Ignore FCM token update errors - user can still login
        }

        val doc = firestore.collection("users").document(uid).get().await()
        doc.toObject(User::class.java) ?: throw Exception("Account not found. Please sign up first.")
    }

    fun logout() {
        auth.signOut()
    }

    suspend fun deleteAccount(): Result<Unit> = runCatching {
        val uid = currentUser?.uid ?: throw Exception("Not logged in")
        
        // Get user's display name before deletion
        val userDoc = firestore.collection("users").document(uid).get().await()
        val displayName = userDoc.getString("displayName") ?: "A user"

        // Get all connected users to notify them
        val connectedUserIds = mutableSetOf<String>()
        
        val connectionsA = firestore.collection("connections")
            .whereEqualTo("userA", uid)
            .whereEqualTo("status", "accepted")
            .get().await()
        connectionsA.documents.forEach { doc ->
            doc.getString("userB")?.let { connectedUserIds.add(it) }
        }

        val connectionsB = firestore.collection("connections")
            .whereEqualTo("userB", uid)
            .whereEqualTo("status", "accepted")
            .get().await()
        connectionsB.documents.forEach { doc ->
            doc.getString("userA")?.let { connectedUserIds.add(it) }
        }

        // Notify all connected users about account deletion
        val notification = hashMapOf(
            "type" to "account_deleted",
            "fromUserId" to uid,
            "fromDisplayName" to displayName,
            "message" to "$displayName has deleted their iCare account and is no longer in your circle.",
            "timestamp" to Timestamp.now(),
            "read" to false
        )
        
        connectedUserIds.forEach { connectedUid ->
            firestore.collection("users").document(connectedUid)
                .collection("notifications")
                .add(notification).await()
        }

        // Delete all connections where user is userA
        val allConnectionsA = firestore.collection("connections")
            .whereEqualTo("userA", uid).get().await()
        allConnectionsA.documents.forEach { it.reference.delete().await() }

        // Delete all connections where user is userB
        val allConnectionsB = firestore.collection("connections")
            .whereEqualTo("userB", uid).get().await()
        allConnectionsB.documents.forEach { it.reference.delete().await() }

        // Delete user's notifications
        val userNotifications = firestore.collection("users").document(uid)
            .collection("notifications").get().await()
        userNotifications.documents.forEach { it.reference.delete().await() }

        // Delete user document
        firestore.collection("users").document(uid).delete().await()

        // Delete Firebase Auth account
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

    // OTP Methods
    suspend fun sendOtp(email: String, displayName: String): Result<Unit> = runCatching {
        val data = hashMapOf(
            "email" to email,
            "displayName" to displayName
        )
        functions.getHttpsCallable("sendOtp")
            .call(data)
            .await()
    }

    suspend fun verifyOtp(email: String, otp: String): Result<Boolean> = runCatching {
        val data = hashMapOf(
            "email" to email,
            "otp" to otp
        )
        val result = functions.getHttpsCallable("verifyOtp")
            .call(data)
            .await()
        
        @Suppress("UNCHECKED_CAST")
        val resultData = result.getData() as? Map<String, Any>
        resultData?.get("verified") as? Boolean ?: false
    }

    suspend fun generateICareId(email: String, userId: String): Result<String> = runCatching {
        val data = hashMapOf(
            "email" to email,
            "userId" to userId
        )
        val result = functions.getHttpsCallable("generateICareId")
            .call(data)
            .await()
        
        @Suppress("UNCHECKED_CAST")
        val resultData = result.getData() as? Map<String, Any>
        resultData?.get("iCareId") as? String ?: throw Exception("Failed to generate iCareId")
    }

    // Modified signUp to not create user until OTP is verified
    suspend fun signUpAfterOtpVerification(
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
        val verificationEmail = if (isPhoneUser) recoveryEmail else emailOrPhone
        
        // Generate iCareId
        val iCareId = try {
            generateICareId(verificationEmail, uid).getOrThrow()
        } catch (e: Exception) {
            // Fallback: generate locally if cloud function fails
            generateLocalICareId(verificationEmail)
        }

        val user = User(
            uid = uid,
            iCareId = iCareId,
            displayName = displayName,
            email = if (!isPhoneUser) emailOrPhone else "",
            phone = if (isPhoneUser) emailOrPhone else "",
            recoveryEmail = if (isPhoneUser) recoveryEmail else "",
            emailHash = if (!isPhoneUser) hashString(emailOrPhone.lowercase()) else "",
            phoneHash = if (isPhoneUser) hashString(normalizePhone(emailOrPhone)) else "",
            fcmToken = fcmToken,
            authProvider = "email",
            emailVerified = true,
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now()
        )

        firestore.collection("users").document(uid).set(user).await()
        user
    }

    private fun generateLocalICareId(email: String): String {
        val emailPrefix = email
            .substringBefore("@")
            .lowercase()
            .replace(Regex("[^a-z0-9]"), "")
        
        var prefix = emailPrefix.take(3)
        while (prefix.length < 3) {
            prefix += ('a'..'z').random()
        }
        
        val randomNum = (1000..9999).random()
        return "iCare.$randomNum.$prefix"
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
