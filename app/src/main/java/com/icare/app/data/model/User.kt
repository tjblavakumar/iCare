package com.icare.app.data.model

import com.google.firebase.Timestamp

data class User(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val phone: String = "",
    val recoveryEmail: String = "",
    val emailHash: String = "",
    val phoneHash: String = "",
    val fcmToken: String = "",
    val photoUrl: String = "",
    val authProvider: String = "email",
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
