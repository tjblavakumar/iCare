package com.icare.app.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class User(
    val uid: String = "",
    @get:PropertyName("iCareId") @set:PropertyName("iCareId")
    var iCareId: String = "",  // Unique shareable ID like "iCare.1234.joh"
    val displayName: String = "",
    val email: String = "",
    val phone: String = "",
    val recoveryEmail: String = "",
    val emailHash: String = "",
    val phoneHash: String = "",
    val fcmToken: String = "",
    val photoUrl: String = "",
    val authProvider: String = "email",
    val emailVerified: Boolean = false,
    val currentStatus: CurrentStatus? = null,
    val customEmojis: List<String> = emptyList(),
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

data class CurrentStatus(
    val emojiId: String = "",
    val emoji: String = "",
    val label: String = "",
    val timestamp: Timestamp? = null,
    val timezone: String = ""
)
