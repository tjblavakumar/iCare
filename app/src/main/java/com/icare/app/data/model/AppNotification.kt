package com.icare.app.data.model

import com.google.firebase.Timestamp

data class AppNotification(
    val id: String = "",
    val fromUserId: String = "",
    val fromDisplayName: String = "",
    val emoji: String = "",
    val label: String = "",
    val timestamp: Timestamp? = null,
    val read: Boolean = false
)
